package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity.State;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.StoppingEnvelope;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** A placed controller owns exactly one physical carriage and one stored-energy cable drive. */
final class CableControllers {
  private record Key(ServerLevel level, BlockPos position, UUID identity) {}
  private record Installation(BlockPos cabinetPosition, UUID cabinet, double home, NativeCableRig rig) {}
  private record Occupant(ServerSubLevel cart, MotionVehicle motion, double distance, double speed) {}
  private static final Map<Key, Installation> INSTALLED = new HashMap<>();
  private static final double RELEASE_TRAVEL = 12, GUIDE_TRAVEL = 20;
  private CableControllers() {}

  static boolean applies(ServerLevel level, RideControllerEntity controller) {
    return INSTALLED.containsKey(new Key(level, controller.getBlockPos(), controller.identity()))
        || controller.modules().stream().anyMatch(m -> level.getBlockEntity(m.position()) instanceof EquipmentCabinetEntity cabinet
        && cabinet.mechanism().cable());
  }

  static void unload(ServerLevel level) { INSTALLED.keySet().removeIf(k -> k.level() == level); }
  static boolean hasLiveCarriage(ServerLevel level, UUID carriage) {
    return INSTALLED.entrySet().stream().anyMatch(entry -> entry.getKey().level() == level
        && entry.getValue().rig().carriage().getUniqueId().equals(carriage));
  }

  static void audit(ServerLevel level, Collection<RideControllerEntity> live) {
    for (var entry : INSTALLED.entrySet()) {
      var key = entry.getKey(); if (key.level() != level) continue;
      if (live.stream().anyMatch(c -> c.getBlockPos().equals(key.position()) && c.identity().equals(key.identity()))) continue;
      entry.getValue().rig().emergencyHold("Cable controller is unavailable; native catch needs inspection");
      if (level.hasChunkAt(entry.getValue().cabinetPosition())
          && level.getBlockEntity(entry.getValue().cabinetPosition()) instanceof EquipmentCabinetEntity cabinet
          && cabinet.identity().equals(entry.getValue().cabinet()))
        cabinet.fail("Cable controller unavailable; inspect the retained carriage");
    }
  }

  static void idle(ServerLevel level, RideControllerEntity controller) {
    var key = new Key(level, controller.getBlockPos(), controller.identity());
    var installed = INSTALLED.get(key);
    if (installed == null) return;
    var cabinet = cabinet(level, controller, installed.cabinet());
    if (cabinet == null) { installed.rig().emergencyHold("Cable cabinet or controller identity is unavailable"); return; }
    if (cabinet.cableInUse() || installed.rig().phase() == NativeCableRig.Phase.FAULT
        || controller.phase() == State.FAULT && installed.rig().phase() != NativeCableRig.Phase.HOME) {
      installed.rig().emergencyHold("Controller is disarmed or faulted with a retained catch");
      cabinet.fail("Cable catch needs inspected recovery");
      return;
    }
    if (!controller.armed() && installed.rig().phase() == NativeCableRig.Phase.HOME) {
      cabinet.retireCable(installed.rig()); INSTALLED.remove(key);
    }
  }

  static void evaluate(ServerLevel level, RideControllerEntity controller, DirectedTrack track, double dt) {
    var cabinets = new ArrayList<EquipmentCabinetEntity>();
    for (var linked : controller.modules()) {
      if (!level.hasChunkAt(linked.position()) || !(level.getBlockEntity(linked.position()) instanceof EquipmentCabinetEntity cabinet)
          || !linked.identity().equals(cabinet.identity()) || RideControllers.owner(level, cabinet) != controller
          || !cabinet.armed() || !cabinet.fault().isEmpty()) {
        controller.fail("Cable controller linked equipment is missing, disarmed or faulted"); return;
      }
      if (!EquipmentRuntime.available(level, cabinet)) {
        if (RideControllers.awaitingRegistration(level, controller)) { controller.consumeRequest(); return; }
        controller.fail("Cable equipment physics registration was lost at " + linked.position()); return;
      }
      var local = cabinet.resolve(level);
      if (local == null || !local.edge().equals(track.edge())) { controller.fail("Cable equipment rail support changed"); return; }
      cabinets.add(cabinet);
    }
    RideControllers.registrationReady(level, controller);
    var drives = cabinets.stream().filter(c -> c.mechanism() != Mechanism.FIN_BRAKE).toList();
    if (drives.size() != 1 || !drives.getFirst().mechanism().cable()
        || controller.destination() != null || controller.incoming() != null) {
      controller.fail("Cable section requires one cable drive, friction fins and no transfer connection"); return;
    }
    var cabinet = drives.getFirst();
    var key = new Key(level, controller.getBlockPos(), controller.identity());
    var installed = INSTALLED.get(key);
    if (cabinet.cableInstalled() && installed == null) { controller.fail("Saved cable carriage needs inspected recovery"); return; }
    if (installed != null && (!installed.cabinet().equals(cabinet.identity())
        || !installed.rig().carriage().getUniqueId().equals(cabinet.cableCarriage())
        || Math.abs(installed.home() - cabinet.cableHome()) > 1e-6)) {
      controller.fail("Cable cabinet or carriage identity changed"); return;
    }

    var container = SubLevelContainer.getContainer(level);
    if (container == null) { controller.fail("Native cable physics unavailable"); return; }
    var occupying = new ArrayList<Occupant>();
    for (var body : container.getAllSubLevels()) {
      var contact = EngagedContacts.current(level, body).orElse(null);
      if (contact == null || !track.matches(contact.hit())) continue;
      var motion = MotionProviders.resolve(level, body.getUniqueId()).orElse(null);
      if (motion == null) { controller.fail("Unidentified native cart occupies the cable section"); return; }
      var direction = track.forward(level, contact.hit());
      var socket = VehicleInterfaces.catchPoint(body);
      if (socket == null) { controller.fail("Occupied cable car has no native catch socket"); return; }
      double speed = NativeForces.pointDynamics(body, container.physicsSystem().getPipeline(), socket,
          new Vector3d(direction.x, direction.y, direction.z)).speed();
      occupying.add(new Occupant(body, motion, track.distance(contact.hit()), speed));
    }
    if (occupying.isEmpty()) {
      if (!controller.reservation().isEmpty()) controller.fail("Reserved cable train left the section");
      else { controller.state(State.HOLD); controller.consumeRequest(); }
      return;
    }
    var lead = occupying.stream().max(Comparator.comparingDouble(Occupant::distance)).orElseThrow();
    var members = Set.copyOf(lead.motion().consist());
    if (!controller.reservation().isEmpty() && !controller.reservation().equals(members)
        || occupying.stream().anyMatch(o -> !members.contains(o.cart().getUniqueId()))) {
      controller.fail("Another consist occupies the reserved cable section"); return;
    }
    controller.reserve(members);
    boolean held = true, ready = true; double mass = 0, brakeForce = 256, minimum = Double.POSITIVE_INFINITY, maximum = 0, speed = 0, radius = 0;
    var bodies = new HashSet<UUID>();
    for (var id : members) {
      var occupant = occupying.stream().filter(o -> o.cart().getUniqueId().equals(id)).findFirst().orElse(null);
      if (occupant == null || !Set.copyOf(occupant.motion().consist()).equals(members)
          || !occupant.motion().interfaces().containsAll(Set.of(cabinet.mechanism(), Mechanism.FIN_BRAKE))
          || !occupant.motion().exclusiveOperator().isEmpty()
          || ChainLiftSpanQuery.findAtGraphHit(level, EngagedContacts.current(level, occupant.cart()).orElseThrow().hit(), 0) != null) {
        controller.fail("Cable consist lost native support, certification or exclusive ownership"); return;
      }
      for (var body : occupant.motion().bodies()) if (!bodies.add(body)) { controller.fail("Duplicate articulated cable body"); return; }
      double loaded = NativeForces.mass(level, occupant.motion());
      if (loaded <= 0) { controller.fail("Cable loaded mass unavailable"); return; }
      mass += loaded; brakeForce = Math.min(brakeForce, occupant.motion().maximumBrakeForce());
      radius = Math.max(radius, RideControllers.radius(level, occupant.motion()));
      minimum = Math.min(minimum, occupant.distance()); maximum = Math.max(maximum, occupant.distance());
      speed = Math.max(speed, Math.abs(occupant.speed()));
      held &= Math.abs(occupant.speed()) < .03 && (EquipmentRuntime.activeFinClamps(level, id) > 0
          || !occupant.motion().propulsionAllowed() && occupant.motion().dispatchReady());
      ready &= occupant.motion().dispatchReady();
    }
    if (mass > 64 || brakeForce <= 0) { controller.fail("Cable consist exceeds its mass or friction-fin rating"); return; }
    double home = installed == null ? lead.distance() : installed.home();
    if (installed == null && Math.abs(lead.distance() - (cabinet.interval().start() + 1)) > 1.1) {
      controller.fail("Catch must start beside its cable cabinet"); return;
    }
    double releaseEnd = home + RELEASE_TRAVEL;
    var launchFins = cabinets.stream().filter(c -> c.mechanism() == Mechanism.FIN_BRAKE
        && c.interval().start() <= cabinet.interval().end() && c.interval().end() >= cabinet.interval().start()).toList();
    var stops = cabinets.stream().filter(c -> c.mechanism() == Mechanism.FIN_BRAKE && c.interval().start() > releaseEnd)
        .sorted(Comparator.comparingDouble(c -> c.interval().start())).toList();
    if (launchFins.isEmpty() || stops.isEmpty() || cabinets.size() != 1 + launchFins.size() + stops.size()) {
      controller.fail("Cable fins must be at the catch or wholly downstream of physical release"); return;
    }
    double stopStart = stops.getFirst().interval().start(), stopEnd = stops.getFirst().interval().end();
    for (var stop : stops.subList(1, stops.size())) {
      if (stop.interval().start() > stopEnd + 1e-6) { controller.fail("Cable stopping fin coverage has a gap"); return; }
      stopEnd = Math.max(stopEnd, stop.interval().end());
    }
    var envelope = new StoppingEnvelope(mass, Math.max(speed, cabinet.targetSpeed()), brakeForce, 0, .1, maximum - minimum + 2 * radius);
    controller.envelope(envelope.requiredLength(), stopEnd - stopStart);
    if (!envelope.fits(stopEnd - stopStart)) { controller.fail("Cable stopping bank is too short for the loaded train"); return; }
    boolean shaft = DriveEquipment.forwardPowered(cabinet);
    boolean launchPower = cabinet.mechanism() == Mechanism.FLYWHEEL ? shaft && cabinet.cableReady() : cabinet.cableReady();
    boolean pullPower = cabinet.mechanism() == Mechanism.FLYWHEEL ? shaft : cabinet.cableEnergy() > 1e-9;
    boolean padPower = launchFins.stream().allMatch(c -> Math.abs(c.getSpeed()) >= 32);
    if (installed == null && held && ready) {
      try {
        var rig = new NativeCableRig(level, track, lead.distance(), RELEASE_TRAVEL, GUIDE_TRAVEL, cabinet.getBlockPos(), cabinet.mechanism());
        try { cabinet.installCable(rig); } catch (RuntimeException failure) { rig.close(); throw failure; }
        installed = new Installation(cabinet.getBlockPos(), cabinet.identity(), lead.distance(), rig); INSTALLED.put(key, installed);
      } catch (RuntimeException failure) { controller.fail("Cable carriage installation failed: " + failure.getMessage()); return; }
    }
    if (installed == null) { controller.state(State.HOLD); controller.consumeRequest(); return; }
    var rig = installed.rig();
    if (rig.phase() == NativeCableRig.Phase.FAULT) { controller.fail("Cable carriage fault: " + rig.fault()); return; }
    if (controller.phase() == State.RUNNING || controller.phase() == State.BRAKING) {
      if (rig.phase() == NativeCableRig.Phase.PULLING && (!pullPower || !padPower || !cabinet.cableInUse())) {
        rig.emergencyHold("Cable release power or catch ownership lost"); controller.fail("Cable release power or catch ownership lost"); return;
      }
      if (speed > cabinet.targetSpeed() + .1 && controller.phase() == State.RUNNING) {
        rig.emergencyHold("Cable speed exceeded its certificate"); controller.fail("Cable speed exceeded its certificate"); return;
      }
      if (maximum >= stopStart - .6) controller.state(State.BRAKING);
      if (controller.phase() == State.BRAKING && held && speed < .03) controller.state(State.HOLD);
    } else if (rig.phase() == NativeCableRig.Phase.HOME && Math.abs(lead.distance() - home) < .6 && held && ready && launchPower && padPower
        && !cabinet.cableInUse()
        && Math.abs(rig.travel()) < .03 && Math.abs(rig.speed()) < .03) {
      try { cabinet.engageCable(rig, lead.cart().getUniqueId()); }
      catch (RuntimeException failure) { controller.fail("Physical cable catch could not engage: " + failure.getMessage()); return; }
    }
    if (controller.phase() != State.RUNNING && controller.phase() != State.BRAKING) {
      boolean dispatchable = held && ready && launchPower && padPower
          && cabinet.cableInUse() && rig.phase() == NativeCableRig.Phase.ENGAGED && maximum + radius < stopStart;
      controller.state(dispatchable ? State.READY : State.HOLD);
      if (controller.consumeRequest() && dispatchable) {
        try { rig.beginPull(); controller.state(State.RUNNING); }
        catch (RuntimeException failure) { controller.fail("Cable dispatch failed: " + failure.getMessage()); return; }
      }
    } else controller.consumeRequest();
    if (cabinet.cableInUse()) {
      cabinet.stepCable(rig, dt);
      if (rig.phase() == NativeCableRig.Phase.FAULT) controller.fail("Cable catch fault: " + rig.fault());
    }
  }

  private static EquipmentCabinetEntity cabinet(ServerLevel level, RideControllerEntity controller, UUID identity) {
    for (var linked : controller.modules()) if (linked.identity().equals(identity)
        && level.getBlockEntity(linked.position()) instanceof EquipmentCabinetEntity cabinet) return cabinet;
    return null;
  }
}
