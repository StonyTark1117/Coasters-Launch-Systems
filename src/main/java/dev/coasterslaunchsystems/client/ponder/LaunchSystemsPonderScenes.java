package dev.coasterslaunchsystems.client.ponder;

import dev.coasterslaunchsystems.content.EquipmentContent;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Short, current-state operating guides. They deliberately avoid claiming unimplemented route or restart behavior. */
final class LaunchSystemsPonderScenes {
  private LaunchSystemsPonderScenes() {}

  static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    helper.forComponents(EquipmentContent.TOOL.getId(), EquipmentContent.CONTROLLER.getId(), EquipmentContent.TIRE.getId())
        .addStoryBoard("controller_binding", LaunchSystemsPonderScenes::controllerBinding);
    helper.forComponents(EquipmentContent.LSM.getId(), EquipmentContent.LIM.getId(),
            EquipmentContent.LSM_PLATE.getId(), EquipmentContent.LIM_PLATE.getId())
        .addStoryBoard("linear_motors", LaunchSystemsPonderScenes::linearMotors);
    helper.forComponents(EquipmentContent.FIN.getId(), EquipmentContent.EDDY.getId(), EquipmentContent.SKID.getId(),
            EquipmentContent.BRAKE_FIN.getId(), EquipmentContent.CONDUCTIVE_FIN.getId(), EquipmentContent.SKID_SHOE.getId())
        .addStoryBoard("brake_hardware", LaunchSystemsPonderScenes::brakeHardware);
    helper.forComponents(EquipmentContent.FLYWHEEL.getId(), EquipmentContent.HYDRAULIC.getId(), EquipmentContent.PNEUMATIC.getId(),
            EquipmentContent.CATCH_SOCKET.getId(), EquipmentContent.CATCH_CARRIAGE.getId(), EquipmentContent.CABLE_GUIDE.getId())
        .addStoryBoard("cable_launch", LaunchSystemsPonderScenes::cableLaunch);
  }

  private static void controllerBinding(SceneBuilder scene, SceneBuildingUtil util) {
    scene.title("controller_binding", "Binding a local launch section");
    scene.configureBasePlate(0, 0, 5);
    scene.showBasePlate();
    scene.idle(10);
    scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
    scene.overlay().showText(70).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(2, 1, 2))
        .text("Mount both anchors on floor supports so they face up, then place modules beside the level rail.");
    scene.idle(80);
    scene.overlay().showText(90).attachKeyFrame().placeNearTarget().pointAt(util.vector().centerOf(2, 1, 2))
        .text("Use the Span Tool on two coaster anchors in travel order, then use it on the controller.");
    scene.idle(100);
    scene.overlay().showText(90).colored(PonderPalette.GREEN).attachKeyFrame().placeNearTarget().pointAt(util.vector().centerOf(1, 1, 2))
        .text("Shift-use the controller with the tool selected, then use the tool on each bound module to link it.");
    scene.idle(100);
    scene.overlay().showText(90).colored(PonderPalette.RED).attachKeyFrame().placeNearTarget().pointAt(util.vector().centerOf(3, 1, 2))
        .text("Inspect bindings and safety hardware before arming. A loaded controller or cabinet always comes back disarmed.");
    scene.idle(100);
    scene.markAsFinished();
  }

  private static void linearMotors(SceneBuilder scene, SceneBuildingUtil util) {
    scene.title("linear_motors", "Linear motor hardware");
    scene.configureBasePlate(0, 0, 5);
    scene.showBasePlate();
    scene.idle(10);
    scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
    scene.overlay().showText(80).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(1, 1, 2))
        .text("LSM and LIM use separate cabinets and matching reaction plates. Do not mix their plates.");
    scene.idle(90);
    scene.overlay().showText(100).attachKeyFrame().placeNearTarget().pointAt(util.vector().centerOf(1, 1, 3))
        .text("Install the matching plate on the certified chassis interface, then bind the cabinet over level fixed rail.");
    scene.idle(110);
    scene.overlay().showText(100).colored(PonderPalette.RED).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(3, 1, 2))
        .text("Charge with a real Create shaft and tune while empty and disarmed. Linear braking cannot provide a static hold.");
    scene.idle(110);
    scene.markAsFinished();
  }

  private static void brakeHardware(SceneBuilder scene, SceneBuildingUtil util) {
    scene.title("brake_hardware", "Choosing braking hardware");
    scene.configureBasePlate(0, 0, 5);
    scene.showBasePlate();
    scene.idle(10);
    scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
    scene.overlay().showText(80).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(1, 1, 2))
        .text("A spring fin clamp uses the chassis brake fin for final stop and hold.");
    scene.idle(90);
    scene.overlay().showText(90).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(2, 1, 2))
        .text("The magnetic brake uses the conductive fin and dissipates motion; it does not hold a stopped train.");
    scene.idle(100);
    scene.overlay().showText(100).colored(PonderPalette.RED).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(3, 1, 2))
        .text("Skid hardware needs its matching shoe. Bind every cabinet to its local span and leave a friction hold downstream.");
    scene.idle(110);
    scene.markAsFinished();
  }

  private static void cableLaunch(SceneBuilder scene, SceneBuildingUtil util) {
    scene.title("cable_launch", "Preparing a cable launch");
    scene.configureBasePlate(0, 0, 5);
    scene.showBasePlate();
    scene.idle(10);
    scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
    scene.overlay().showText(90).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(1, 1, 2))
        .text("Install a socket, carriage and a continuous verified guide along the straight launch route.");
    scene.idle(100);
    scene.overlay().showText(100).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(2, 1, 2))
        .text("Flywheel, hydraulic and pneumatic cabinets are distinct finite-energy cable stores. Bind only one to the section.");
    scene.idle(110);
    scene.overlay().showText(110).colored(PonderPalette.RED).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(3, 1, 2))
        .text("After any interruption, inspect the retained carriage and guide before rebinding or arming. Never bypass the downstream fin hold.");
    scene.idle(120);
    scene.markAsFinished();
  }
}
