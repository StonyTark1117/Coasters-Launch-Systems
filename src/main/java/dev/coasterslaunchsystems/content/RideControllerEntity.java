package dev.coasterslaunchsystems.content;

import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.DirectedRoute;
import dev.coasterslaunchsystems.runtime.RideControllers;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

/** Local fixed-edge controller. Configuration/reservations persist; commands and native handles do not. */
public final class RideControllerEntity extends BlockEntity {
  public enum State { INSPECTION, HOLD, READY, RUNNING, BRAKING, READY_EXIT, EXITING, RECEIVING, TRANSFER_BRAKING, CAPTURING, BOARDING, FAULT }
  public record Module(BlockPos position, UUID identity, boolean exitDrive) {
    public Module(BlockPos position, UUID identity) { this(position, identity, false); }
  }
  public record Peer(BlockPos position, UUID identity) {}
  private Peer destination, incoming, requestedRepair;
  private final List<Module> receiverManifest = new ArrayList<>();
  private boolean receiverStationCapture;
  private boolean transferring;
  private final List<DirectedRoute.Span> transferSpans = new ArrayList<>();
  private DirectedRoute cachedTransfer;
  private int transferRevision = Integer.MIN_VALUE;
  private long transferHandleRevision = Long.MIN_VALUE;
  private double transferSpeedLimit;
  private UUID identity = UUID.randomUUID();
  private BlockPos from, to;
  private long geometry;
  private DirectedTrack cachedTrack;
  private int graphRevision = Integer.MIN_VALUE;
  private long handleRevision = Long.MIN_VALUE;
  private final Set<BlockPos> routeChunks = new LinkedHashSet<>();
  private final List<Module> modules = new ArrayList<>();
  private final Set<UUID> reservation = new LinkedHashSet<>();
  private boolean stationCapture, boardingReady;
  private boolean armed, high, inputObserved, launchRequested;
  private State phase = State.INSPECTION;
  private String fault = "";
  private double required, available;

  public RideControllerEntity(BlockPos pos, BlockState state) { super(EquipmentContent.CONTROLLER_ENTITY.get(), pos, state); }
  public void tick() {
    if (!(level instanceof ServerLevel server)) return;
    RideControllers.register(server, this);
    // Only the top is a request input; outputs cannot feed their own request latch.
    boolean powered = server.getSignal(worldPosition.above(), Direction.UP) > 0;
    if (inputObserved && powered && !high && (phase == State.READY || phase == State.READY_EXIT)) launchRequested = true;
    high = powered; inputObserved = true;
  }
  public BlockPos from() { return from; }
  public BlockPos to() { return to; }
  public Peer destination() { return destination; }
  public Peer incoming() { return incoming; }
  public boolean transferring() { return transferring; }
  public double transferSpeedLimit() { return transferSpeedLimit; }
  public List<Module> receiverManifest() { return List.copyOf(receiverManifest); }
  public List<DirectedRoute.Span> savedTransferSpans() { return List.copyOf(transferSpans); }
  private void rememberReceiver(RideControllerEntity target) {
    receiverManifest.clear(); receiverManifest.addAll(target.modules()); receiverStationCapture = target.stationCapture;
  }
  public void requestReceiverRepair(RideControllerEntity target) {
    if (armed || phase != State.INSPECTION || reservation.isEmpty() || !transferring || destination == null
        || target.getLevel() != level || !destination.position().equals(target.getBlockPos()) || destination.identity().equals(target.identity()))
      throw new IllegalArgumentException("Disarm the retained source and select its replacement receiver before inspection");
    requestedRepair = new Peer(target.getBlockPos(), target.identity());
    fault = "Receiver inspection queued for the next native guide observation"; changed();
  }
  /** Requests are transient and consumed once, with this step's accepted native guide observations. */
  public void inspectRequestedReceiver(ServerLevel server) {
    var requested = requestedRepair; requestedRepair = null;
    if (requested == null) return;
    try {
      if (!server.hasChunkAt(requested.position()) || !(server.getBlockEntity(requested.position()) instanceof RideControllerEntity target)
          || !requested.identity().equals(target.identity())) throw new IllegalArgumentException("Replacement receiver became unavailable");
      restoreReceiver(target);
    } catch (IllegalArgumentException failure) {
      launchRequested = false; boardingReady = false;
      fault = "Receiver inspection refused: " + failure.getMessage(); changed();
    }
  }
  /** Explicit inspection repair; the runtime proves the entire journal before any ownership mutation. */
  private void restoreReceiver(RideControllerEntity target) {
    if (!(level instanceof ServerLevel server)) throw new IllegalArgumentException("Receiver repair requires a server inspection");
    dev.coasterslaunchsystems.runtime.RideTransfers.inspectReceiverRepair(server, this, target);
    var previous = destination;
    // All participating block entities are verified together on the server thread; neither controller grants motion.
    for (var module : receiverManifest)
      ((EquipmentCabinetEntity) server.getBlockEntity(module.position())).restoreController(previous, target);
    target.modules.addAll(receiverManifest); target.stationCapture = receiverStationCapture;
    target.reservation.addAll(reservation); target.incoming = new Peer(worldPosition, identity);
    target.armed = false; target.phase = State.INSPECTION; target.boardingReady = false;
    target.high = false; target.inputObserved = false; target.launchRequested = false;
    target.fault = "Receiver restored by inspection; arm both controllers and make a fresh request";
    destination = new Peer(target.getBlockPos(), target.identity());
    cachedTransfer = null; transferRevision = Integer.MIN_VALUE; transferHandleRevision = Long.MIN_VALUE;
    boardingReady = false; high = false; inputObserved = false; launchRequested = false;
    fault = target.fault; target.changed(); changed();
  }
  public List<DirectedRoute.Span> claimedSpans() {
    if (from == null) return List.of();
    return transferring && !reservation.isEmpty() && !transferSpans.isEmpty() ? List.copyOf(transferSpans)
        : List.of(new DirectedRoute.Span(from, to, geometry));
  }
  public DirectedRoute transferRoute(ServerLevel server, RideControllerEntity target) {
    if (destination == null || !destination.identity().equals(target.identity()) || resolve(server) == null || target.resolve(server) == null)
      throw new IllegalStateException("Transfer route endpoints are unavailable");
    if (!transferSpans.isEmpty() && (!transferSpans.getLast().from().equals(target.from()) || !transferSpans.getLast().to().equals(target.to())))
      throw new IllegalStateException("Transfer receiver binding changed");
    int revision = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(server).revision();
    long handles = dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision();
    if (cachedTransfer == null || transferRevision != revision || transferHandleRevision != handles) {
      var route = DirectedRoute.between(server, from, to, target.from(), target.to());
      // Old saves only described an adjacent pair; migration cannot silently acquire a longer route.
      if (transferSpans.isEmpty()) {
        if (!to.equals(target.from())) throw new IllegalStateException("Legacy destination requires inspected route relinking");
        transferSpans.addAll(route.spans()); changed();
      }
      if (!transferSpans.equals(route.spans())) throw new IllegalStateException("Reserved transfer route geometry changed");
      cachedTransfer = route; transferRevision = revision; transferHandleRevision = handles;
    }
    cachedTransfer.requireLoaded(server); return cachedTransfer;
  }
  public boolean exitDrive(EquipmentCabinetEntity module) {
    return modules.stream().anyMatch(m -> m.position().equals(module.getBlockPos()) && m.identity().equals(module.identity()) && m.exitDrive());
  }
  public void connectDestination(RideControllerEntity target) {
    if (armed || !reservation.isEmpty() || incoming != null || transferring)
      throw new IllegalArgumentException("Disarm an empty source controller before linking its destination");
    if (target == this || target.getLevel() != level || !(level instanceof ServerLevel server)) throw new IllegalArgumentException("Select another controller in this dimension");
    if (resolve(server) == null || target.resolve(server) == null) throw new IllegalArgumentException("Both sections must be supported");
    var route = DirectedRoute.between(server, from, to, target.from(), target.to());
    destination = new Peer(target.getBlockPos(), target.identity()); rememberReceiver(target);
    transferSpans.clear(); transferSpans.addAll(route.spans()); cachedTransfer = null; transferRevision = Integer.MIN_VALUE; changed();
  }
  public void beginTransfer(RideControllerEntity target, double speedLimit) {
    if (destination == null || !destination.identity().equals(target.identity()) || reservation.isEmpty())
      throw new IllegalArgumentException("No reserved destination configured");
    dev.coasterslaunchsystems.math.Numbers.positive(speedLimit, "transfer speed limit");
    rememberReceiver(target);
    target.reserve(reservation()); target.incoming = new Peer(worldPosition, identity); target.phase = State.RECEIVING; target.boardingReady = false; target.changed();
    boardingReady = false; transferSpeedLimit = speedLimit; transferring = true; phase = State.EXITING; changed();
  }
  public void finishTransfer(RideControllerEntity target) {
    if (!transferring || target.incoming == null || !identity.equals(target.incoming.identity()) || !reservation().equals(target.reservation()))
      throw new IllegalArgumentException("Transfer ownership changed before tail clearance");
    target.incoming = null; target.phase = State.RUNNING; target.changed();
    transferring = false; transferSpeedLimit = 0; clearVerifiedReservation();
  }
  public boolean endpoint(BlockPos pos) { return claimedSpans().stream().anyMatch(s -> pos.equals(s.from()) || pos.equals(s.to())); }
  public UUID identity() { return identity; }
  public List<Module> modules() { return List.copyOf(modules); }
  public Set<UUID> reservation() { return Set.copyOf(reservation); }
  public State phase() { return phase; }
  public boolean armed() { return armed; }
  public boolean stationCapture() { return stationCapture; }
  public boolean boardingReady() { return boardingReady; }
  public void boardingReady(boolean value) { if (boardingReady != value) { boardingReady = value; changed(); } }
  public void setStationCapture(boolean value) {
    if (armed || !reservation.isEmpty() || transferring || incoming != null || from == null)
      throw new IllegalArgumentException("Disarm an empty bound controller before changing station capture");
    stationCapture = value; boardingReady = false; changed();
  }
  public String fault() { return fault; }
  public boolean consumeRequest() { boolean value = launchRequested; launchRequested = false; return value; }
  public void setArmed(boolean value) {
    if (value && (from == null || modules.isEmpty())) throw new IllegalArgumentException("Bind a rail section and link its drive/fin modules first");
    if (value && level instanceof ServerLevel server && resolve(server) == null) throw new IllegalArgumentException(fault);
    if (!value && level instanceof ServerLevel server && modules.stream().anyMatch(m -> server.getBlockEntity(m.position()) instanceof EquipmentCabinetEntity cabinet && cabinet.cableInUse()))
      throw new IllegalArgumentException("Retained cable catch requires physical return or inspected recovery");
    requestedRepair = null; armed = value; boardingReady = false; launchRequested = false; inputObserved = false; fault = "";
    phase = value ? State.HOLD : State.INSPECTION; changed();
  }
  public void bind(ServerLevel level, BlockPos first, BlockPos second) {
    if (armed || !reservation.isEmpty() || !modules.isEmpty()) throw new IllegalArgumentException("Disarm an empty controller and unlink its modules before rebinding");
    if (dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.findSubLevelAt(level, worldPosition) != null)
      throw new IllegalArgumentException("Controller requires fixed world support");
    var track = DirectedTrack.resolve(level, first, second).orElseThrow(() -> new IllegalArgumentException("Select one loaded fixed rail edge"));
    for (int i = 0; i <= 64; i++) {
      var hit = track.hitAtDistance(level, track.length() * i / 64);
      var tangent = track.forward(level, hit);
      var firstDirection = track.forward(level, track.hitAtDistance(level, 0));
      var up = dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
      if (Math.abs(tangent.y) > .001 || tangent.dot(firstDirection) < .99999 || up.y < .99999)
        throw new IllegalArgumentException("This controller prototype requires a straight, level, unbanked section");
    }
    from = first.immutable(); to = second.immutable(); geometry = track.fingerprint();
    cachedTrack = null; graphRevision = Integer.MIN_VALUE; routeChunks.clear();
    modules.clear(); destination = null; incoming = null; transferring = false; transferSpans.clear(); receiverManifest.clear(); receiverStationCapture = false; cachedTransfer = null;
    transferRevision = Integer.MIN_VALUE; transferSpeedLimit = 0; phase = State.INSPECTION; fault = ""; changed();
  }
  public DirectedTrack resolve(ServerLevel level) {
    if (from == null || to == null) return null;
    if (dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.findSubLevelAt(level, worldPosition) != null) { fail("Controller lost fixed support"); return null; }
    int current = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(level).revision();
    long handles = dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision();
    if (cachedTrack == null || graphRevision != current || handleRevision != handles) {
      var track = DirectedTrack.resolve(level, from, to).orElse(null);
      if (track == null || track.fingerprint() != geometry) { fail("Reserved rail support or geometry changed"); return null; }
      routeChunks.clear(); routeChunks.add(from); routeChunks.add(to);
      for (double s = 0; s <= track.length(); s += 1) {
        var pos = BlockPos.containing(track.hitAtDistance(level, s).point());
        routeChunks.add(new BlockPos(Math.floorDiv(pos.getX(), 16) * 16, pos.getY(), Math.floorDiv(pos.getZ(), 16) * 16));
      }
      cachedTrack = track; graphRevision = current; handleRevision = handles;
    }
    for (var pos : routeChunks) if (!level.hasChunkAt(pos)) { fail("Reserved route chunk is unavailable"); return null; }
    return cachedTrack;
  }
  public void link(EquipmentCabinetEntity cabinet) { link(cabinet, false); }
  public void link(EquipmentCabinetEntity cabinet, boolean exitDrive) {
    if (armed || !reservation.isEmpty()) throw new IllegalArgumentException("Disarm an empty controller before linking equipment");
    if (!(level instanceof ServerLevel server) || from == null) throw new IllegalArgumentException("Bind the controller's section first");
    var track = resolve(server); var other = cabinet.resolve(server);
    if (track == null || other == null || !track.edge().equals(other.edge()) || track.forward(server, track.hitAtDistance(server, 0)).dot(other.forward(server, other.hitAtDistance(server, 0))) < .99)
      throw new IllegalArgumentException("Module must use this section's anchor order");
    if (!cabinet.mechanism().drive() && cabinet.mechanism() != dev.coasterslaunchsystems.math.Mechanism.FLYWHEEL
        && cabinet.mechanism() != dev.coasterslaunchsystems.math.Mechanism.FIN_BRAKE)
      throw new IllegalArgumentException("Controller currently certifies tire/linear/flywheel drives and friction fins");
    if (cabinet.armed()) throw new IllegalArgumentException("Disarm the module before linking it");
    if (exitDrive && cabinet.mechanism() != dev.coasterslaunchsystems.math.Mechanism.TIRE)
      throw new IllegalArgumentException("Exit transport currently requires a tire drive; linear transfer is not certified");
    cabinet.linkController(worldPosition, identity);
    modules.removeIf(m -> m.position().equals(cabinet.getBlockPos()));
    modules.add(new Module(cabinet.getBlockPos(), cabinet.identity(), exitDrive)); changed();
  }
  public void unlink(EquipmentCabinetEntity cabinet) {
    if (armed || !reservation.isEmpty() || !(level instanceof ServerLevel server))
      throw new IllegalArgumentException("Disarm an empty controller before unlinking");
    if (!identity.equals(cabinet.controllerIdentity()) || !worldPosition.equals(cabinet.controllerPosition()))
      throw new IllegalArgumentException("Module belongs to another controller");
    cabinet.unlinkController(server);
    modules.removeIf(m -> m.position().equals(cabinet.getBlockPos()) && m.identity().equals(cabinet.identity())); changed();
  }
  public void reserve(Set<UUID> members) {
    if (!reservation.isEmpty() && !reservation.equals(members)) throw new IllegalArgumentException("Reservation belongs to another consist");
    if (reservation.isEmpty()) { reservation.addAll(members); changed(); }
  }
  public void clearVerifiedReservation() { reservation.clear(); boardingReady = false; phase = State.HOLD; changed(); }
  public void state(State next) { if (phase != next) { phase = next; if (next == State.RUNNING || next == State.BRAKING || next == State.EXITING || next == State.RECEIVING || next == State.FAULT || next == State.INSPECTION) boardingReady = false; changed(); } }
  public void envelope(double needed, double usable) { required = needed; available = usable; }
  public void fail(String reason) {
    String message = reason == null || reason.isBlank() ? "Controller validation failed" : reason;
    requestedRepair = null; launchRequested = false; boardingReady = false;
    if (phase != State.FAULT || !fault.equals(message)) { phase = State.FAULT; fault = message; changed(); }
  }
  public int signal(Direction face) {
    return switch (face) {
      case NORTH -> phase == State.READY || phase == State.READY_EXIT ? 15 : 0;
      case SOUTH -> reservation.isEmpty() ? 0 : 15;
      case EAST -> phase == State.FAULT || phase == State.INSPECTION ? 15 : 0;
      case WEST -> phase == State.HOLD || phase == State.READY || phase == State.READY_EXIT || phase == State.CAPTURING || phase == State.BOARDING ? 15 : 0;
      case DOWN -> boardingReady ? 15 : 0;
      default -> 0;
    };
  }
  public String status() { return "Ride controller: " + phase + ", reserved chassis " + reservation.size()
      + ", required brake length " + String.format(java.util.Locale.ROOT, "%.2f/%.2f", required, available) + ", transfer " + transferring + ", station capture " + stationCapture + ", boarding ready " + boardingReady + ". " + fault; }
  private void changed() {
    setChanged();
    if (level != null) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); level.updateNeighborsAt(worldPosition, getBlockState().getBlock()); }
  }
  @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
  @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("ls_controller_schema", 5);
    tag.putBoolean("receiver_station_capture", receiverStationCapture);
    tag.put("receiver_manifest", writeModules(receiverManifest));
    tag.putBoolean("station_capture", stationCapture); tag.putBoolean("boarding_ready", boardingReady);
    var route = new ListTag();
    for (var span : transferSpans) { var entry = new CompoundTag(); entry.putLong("from", span.from().asLong()); entry.putLong("to", span.to().asLong()); entry.putLong("geometry", span.geometry()); route.add(entry); }
    tag.put("transfer_route", route); tag.putDouble("transfer_speed_limit", transferSpeedLimit);
    writePeer(tag, "destination", destination); writePeer(tag, "incoming", incoming); tag.putBoolean("transferring", transferring); tag.putUUID("identity", identity); tag.putString("phase", phase.name()); tag.putString("fault", fault);
    tag.putBoolean("armed", armed); tag.putDouble("required", required); tag.putDouble("available", available);
    if (from != null) { tag.putLong("from", from.asLong()); tag.putLong("to", to.asLong()); tag.putLong("geometry", geometry); }
    tag.put("modules", writeModules(modules)); var occupied = new ListTag();
    for (var member : reservation) { var entry = new CompoundTag(); entry.putUUID("id", member); occupied.add(entry); }
    tag.put("reservation", occupied);
  }
  private static ListTag writeModules(List<Module> modules) {
    var linked = new ListTag();
    for (var module : modules) { var entry = new CompoundTag(); entry.putLong("pos", module.position().asLong()); entry.putUUID("id", module.identity()); entry.putBoolean("exit_drive", module.exitDrive()); linked.add(entry); }
    return linked;
  }
  private static void readModules(CompoundTag tag, String key, List<Module> modules) {
    modules.clear(); var positions = new HashSet<BlockPos>(); var identities = new HashSet<UUID>();
    for (var entry : tag.getList(key, 10)) {
      var e = (CompoundTag) entry; var module = new Module(BlockPos.of(e.getLong("pos")), e.getUUID("id"), e.getBoolean("exit_drive"));
      if (!positions.add(module.position()) || !identities.add(module.identity())) throw new IllegalArgumentException("Duplicated saved module identity or position");
      modules.add(module);
    }
    if (modules.size() > 2048) throw new IllegalArgumentException("Saved equipment manifest is too large");
  }
  private static void writePeer(CompoundTag tag, String key, Peer peer) {
    if (peer == null) return;
    var entry = new CompoundTag(); entry.putLong("pos", peer.position().asLong()); entry.putUUID("id", peer.identity()); tag.put(key, entry);
  }
  private static Peer readPeer(CompoundTag tag, String key) {
    if (!tag.contains(key, 10)) return null;
    var entry = tag.getCompound(key); return new Peer(BlockPos.of(entry.getLong("pos")), entry.getUUID("id"));
  }
  @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    if (tag.getInt("ls_controller_schema") < 1 || tag.getInt("ls_controller_schema") > 5) throw new IllegalArgumentException("Unsupported controller schema");
    identity = tag.getUUID("identity"); from = tag.contains("from") ? BlockPos.of(tag.getLong("from")) : null;
    to = tag.contains("to") ? BlockPos.of(tag.getLong("to")) : null; geometry = tag.getLong("geometry");
    destination = readPeer(tag, "destination"); incoming = readPeer(tag, "incoming"); transferring = tag.getBoolean("transferring");
    transferSpans.clear();
    for (var entry : tag.getList("transfer_route", 10)) { var span = (CompoundTag) entry; transferSpans.add(new DirectedRoute.Span(BlockPos.of(span.getLong("from")), BlockPos.of(span.getLong("to")), span.getLong("geometry"))); }
    if (transferSpans.size() > 128) throw new IllegalArgumentException("Saved transfer route is too long");
    transferSpeedLimit = tag.getDouble("transfer_speed_limit");
    if (!Double.isFinite(transferSpeedLimit) || transferSpeedLimit < 0) throw new IllegalArgumentException("Invalid saved transfer speed limit");
    if (transferring && destination == null) throw new IllegalArgumentException("Transferred reservation lacks a destination");
    readModules(tag, "modules", modules);
    receiverManifest.clear(); receiverStationCapture = false;
    if (tag.getInt("ls_controller_schema") >= 5) { readModules(tag, "receiver_manifest", receiverManifest); receiverStationCapture = tag.getBoolean("receiver_station_capture"); }
    reservation.clear(); for (var entry : tag.getList("reservation", 10)) reservation.add(((CompoundTag) entry).getUUID("id"));
    required = tag.getDouble("required"); available = tag.getDouble("available");
    // Client packets display authoritative state. Server loads always require deliberate inspection.
    boolean client = level != null && level.isClientSide;
    stationCapture = tag.getBoolean("station_capture"); boardingReady = client && tag.getBoolean("boarding_ready");
    armed = client && tag.getBoolean("armed"); phase = client ? State.valueOf(tag.getString("phase")) : State.INSPECTION;
    fault = client ? tag.getString("fault") : "Reloaded controller requires inspection; reservation retained";
    cachedTrack = null; graphRevision = Integer.MIN_VALUE; routeChunks.clear();
    cachedTransfer = null; transferRevision = Integer.MIN_VALUE;
    requestedRepair = null; high = false; inputObserved = false; launchRequested = false;
  }
}
