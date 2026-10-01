package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.runtime.CableTickets;
import dev.coasterslaunchsystems.runtime.CableRecovery;
import dev.coasterslaunchsystems.runtime.NativeFinClamp;
import dev.coasterslaunchsystems.runtime.NativeCableRig;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/** Two separate GameTest server processes inspect one saved native carriage world. */
@GameTestHolder("coasterslaunchsystems_cable")
public final class CableRestartGameTests {
  private static final int SLOT = 19;
  private static final BlockPos FROM = new BlockPos(32 + SLOT * 48, 224, -896);
  private static final BlockPos FLYWHEEL = FROM.offset(-1, -2, 8);
  private static final List<ActiveFixture> ACTIVE = new ArrayList<>();
  private static boolean listenerRegistered;
  private static final class ActiveFixture {
    final net.minecraft.server.level.ServerLevel level;
    final EquipmentCabinetEntity cabinet;
    final ServerSubLevel cart;
    final DirectedTrack track;
    NativeCableRig rig;
    boolean engageRequested;
    String failure = "";
    ActiveFixture(net.minecraft.server.level.ServerLevel level, EquipmentCabinetEntity cabinet, ServerSubLevel cart, DirectedTrack track) {
      this.level = level; this.cabinet = cabinet; this.cart = cart; this.track = track;
    }
  }
  private CableRestartGameTests() {}

  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var fixture : List.copyOf(ACTIVE)) {
      if (fixture.level != event.getPhysicsSystem().getLevel() || !fixture.failure.isEmpty()) continue;
      try {
        if (fixture.rig == null) {
          var contact = EngagedContacts.current(fixture.level, fixture.cart).orElse(null);
          if (contact != null) {
            double home = fixture.track.distance(contact.hit());
            fixture.rig = new NativeCableRig(fixture.level, fixture.track, home, 12, 20, FLYWHEEL);
            fixture.cabinet.installCable(fixture.rig);
          }
        }
        if (fixture.engageRequested && fixture.rig != null) {
          fixture.engageRequested = false;
          fixture.cabinet.engageCable(fixture.rig, fixture.cart.getUniqueId());
        }
      } catch (RuntimeException failure) { fixture.failure = failure.toString(); failure.printStackTrace(); }
    }
  }

  @GameTestGenerator public static List<TestFunction> restart() {
    String phase = System.getProperty("coasterslaunchsystems.cableRestartPhase", "");
    if (phase.isEmpty()) return List.of();
    return List.of(new TestFunction("launchsystems_cable_restart", "coasterslaunchsystems_cable.saved_carriage_restart",
        "coasterslaunchsystems_cable:empty", phase.equals("prepareActive") ? 500 : 140, 0, true, CableRestartGameTests::run));
  }

  private static void run(GameTestHelper helper) {
    String phase = System.getProperty("coasterslaunchsystems.cableRestartPhase", "");
    helper.assertTrue(phase.equals("prepare") || phase.equals("verify") || phase.equals("reclaim")
        || phase.equals("verifyRetired") || phase.equals("prepareActive") || phase.equals("verifyActive"),
        "Set lsCableRestartPhase to an idle or active restart phase");
    if (phase.equals("prepare")) prepare(helper);
    else if (phase.equals("prepareActive")) prepareActive(helper);
    else if (phase.equals("verifyActive")) verifyActive(helper);
    else if (phase.equals("verifyRetired")) verifyRetired(helper);
    else verify(helper, phase.equals("reclaim"));
  }

  private static void prepareActive(GameTestHelper helper) {
    var scene = new OrdinaryPassengerGameTests.Scene(helper, SLOT, 224, -896);
    var level = scene.level;
    var cart = scene.cart(8);
    var preparation = new NativeFinClamp[1];
    var fixture = new ActiveFixture[1];
    if (!listenerRegistered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CableRestartGameTests::beforePhysics); listenerRegistered = true; }
    helper.runAtTickTime(25, () -> {
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      for (int step = 0; step <= 128; step++)
        level.setBlockAndUpdate(NativeCableRig.guideBlockAt(level, track, track.length() * step / 128),
            EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      helper.assertTrue(cart.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(2),
          EquipmentContent.CATCH_SOCKET.get().defaultBlockState(), 3), "Loaded cart socket did not install");
    });
    helper.runAtTickTime(40, () -> { for (int seat = 0; seat < 4; seat++) scene.board(cart, seat); });
    helper.runAtTickTime(55, () -> {
      level.setBlockAndUpdate(FLYWHEEL, EquipmentContent.FLYWHEEL.get().defaultBlockState()
          .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      cabinet.bind(level, scene.from, scene.to);
      preparation[0] = new NativeFinClamp(SubLevelContainer.getContainer(level).physicsSystem().getPipeline(), cart,
          VehicleInterfaces.brakePoint(cart), new Vector3d(0, 0, 1), 128);
    });
    helper.runAtTickTime(75, () -> {
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      fixture[0] = new ActiveFixture(level, cabinet, cart, track); ACTIVE.add(fixture[0]);
    });
    helper.runAtTickTime(420, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(fixture[0].rig != null && fixture[0].failure.isEmpty()
          && fixture[0].rig.phase() == NativeCableRig.Phase.HOME
          && Math.abs(fixture[0].rig.travel()) < .03 && Math.abs(fixture[0].rig.speed()) < .03,
          "Native carriage was not home before occupied catch engagement: " + fixture[0].failure);
      fixture[0].engageRequested = true;
    });
    helper.runAtTickTime(430, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(cabinet.cableInstalled() && cabinet.cableInUse() && cart.getUniqueId().equals(cabinet.cableChassis())
          && fixture[0].rig.phase() == NativeCableRig.Phase.ENGAGED && scene.riders.stream().allMatch(pig -> pig.isPassenger())
          && fixture[0].failure.isEmpty(),
          "Occupied active cable journal or native catch was not established: " + fixture[0].failure);
      System.out.println("LS_CABLE_ACTIVE_RESTART prepared carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " socket_error=" + fixture[0].rig.socketError()
          + " guide_error=" + fixture[0].rig.guideError());
      ACTIVE.remove(fixture[0]);
      helper.succeed();
    });
  }

  private static void verifyActive(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Saved active flywheel cabinet is missing after restart");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var container = SubLevelContainer.getContainer(level);
      helper.assertTrue(cabinet.cableInstalled() && cabinet.cableInUse() && cabinet.cableCarriage() != null
          && cabinet.cableChassis() != null && !cabinet.armed() && !cabinet.fault().isEmpty(),
          "Saved active catch did not reload as a disarmed inspected journal: " + cabinet.status());
      helper.assertTrue(container.getSubLevel(cabinet.cableCarriage()) instanceof ServerSubLevel
          && container.getSubLevel(cabinet.cableChassis()) instanceof ServerSubLevel,
          "Captured train or carriage native body did not reload: carriage=" + container.getSubLevel(cabinet.cableCarriage())
              + " chassis=" + container.getSubLevel(cabinet.cableChassis())
              + " carriage_ticket=" + container.getAllTickets().get(cabinet.cableCarriage())
              + " chassis_ticket=" + container.getAllTickets().get(cabinet.cableChassis()));
      helper.assertTrue(CableRecovery.stabilized(level, cabinet.cableCarriage()),
          "Reloaded active catch has no passive native carriage guide and brake");
      var carriage = (ServerSubLevel) container.getSubLevel(cabinet.cableCarriage());
      var chassis = (ServerSubLevel) container.getSubLevel(cabinet.cableChassis());
      var center = carriage.getPlot().getCenterBlock().getCenter();
      var point = NativeCableRig.worldPoint(carriage, new Vector3d(center.x, center.y, center.z));
      var socket = NativeCableRig.worldPoint(chassis, VehicleInterfaces.catchPoint(chassis));
      helper.assertTrue(point.distance(socket) < .03,
          "Reloaded occupied catch socket separated from its passively held carriage");
      System.out.println("LS_CABLE_ACTIVE_RESTART verified carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " separation=" + point.distance(socket)
          + " carriage_point=" + point + " socket=" + socket + " fault=" + cabinet.fault());
      helper.succeed();
    });
  }

  private static void prepare(GameTestHelper helper) {
    var scene = new OrdinaryPassengerGameTests.Scene(helper, SLOT, 224, -896);
    var level = scene.level;
    helper.runAtTickTime(25, () -> {
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      for (int step = 0; step <= 128; step++)
        level.setBlockAndUpdate(NativeCableRig.guideBlockAt(level, track, 8 + 20.0 * step / 128),
            EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      level.setBlockAndUpdate(FLYWHEEL, EquipmentContent.FLYWHEEL.get().defaultBlockState()
          .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      cabinet.bind(level, scene.from, scene.to);
      var rig = new NativeCableRig(level, track, 8, 12, 20, FLYWHEEL);
      cabinet.installCable(rig);
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse()
          && SubLevelContainer.getContainer(level).getAllTickets().containsKey(cabinet.cableCarriage()),
          "The installed idle carriage was not ticketed before save");
      System.out.println("LS_CABLE_RESTART prepared id=" + cabinet.cableCarriage() + " home=" + cabinet.cableHome()
          + " point=" + rig.carriagePoint());
      helper.succeed();
    });
  }

  private static void verify(GameTestHelper helper, boolean reclaim) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Saved flywheel cabinet is missing; the GameTest world was not retained");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var id = cabinet.cableCarriage();
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse() && id != null
          && !cabinet.armed() && cabinet.fault().contains("inspected recovery") && Math.abs(cabinet.cableHome() - 8) < 1e-8,
          "Saved idle carriage journal did not reload disarmed for inspection: " + cabinet.status());
      var container = SubLevelContainer.getContainer(level);
      var info = container.getAllTickets().get(id);
      helper.assertTrue(info != null && info.tickets().stream().anyMatch(ticket -> ticket.getType().equals(CableTickets.CARRIAGE)
          && new CableTickets.Key(id, FLYWHEEL).equals(ticket.getKey())), "Native carriage loading ticket did not reload");
      helper.assertTrue(container.getSubLevel(id) instanceof ServerSubLevel,
          "Sable did not load the saved native carriage body");
      var body = (ServerSubLevel) container.getSubLevel(id);
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO).is(EquipmentContent.CATCH_CARRIAGE.get()),
          "Reloaded native body is not the saved catch carriage");
      var track = DirectedTrack.resolve(level, FROM, FROM.offset(0, 0, 64)).orElseThrow();
      var hit = track.hitAtDistance(level, cabinet.cableHome());
      var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
      var expected = hit.point().add(up.scale(.3125 - 2));
      var center = body.getPlot().getCenterBlock().getCenter();
      var actual = NativeCableRig.worldPoint(body, new org.joml.Vector3d(center.x, center.y, center.z));
      helper.assertTrue(CableRecovery.stabilized(level, id) && actual.distance(expected.x, expected.y, expected.z) < .15,
          "Reloaded idle carriage was not passively held at its saved physical home: " + actual + " expected=" + expected);
      if (reclaim) {
        CableRecovery.inspectRetireIdle(level, cabinet);
        helper.assertTrue(!cabinet.cableInstalled() && cabinet.cableCarriage() == null && !cabinet.fault().isEmpty()
            && !container.getAllTickets().containsKey(id) && container.getSubLevel(id) == null,
            "Inspected retirement left the carriage, ticket or installation journal behind");
      }
      System.out.println("LS_CABLE_RESTART verified id=" + id + " home=" + cabinet.cableHome()
          + " point=" + actual + " reclaimed=" + reclaim + " fault=" + cabinet.fault());
      helper.succeed();
    });
  }

  private static void verifyRetired(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Retired cabinet is missing after the second restart");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(!cabinet.cableInstalled() && !cabinet.cableInUse() && cabinet.cableCarriage() == null
          && !cabinet.armed() && cabinet.fault().contains("reclaimed by inspection"),
          "Inspected retirement did not persist disarmed across another restart: " + cabinet.status());
      System.out.println("LS_CABLE_RESTART retired journal persisted fault=" + cabinet.fault());
      helper.succeed();
    });
  }
}
