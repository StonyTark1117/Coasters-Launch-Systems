package dev.coasterslaunchsystems.client.ponder;

import dev.coasterslaunchsystems.LaunchSystems;
import dev.coasterslaunchsystems.content.EquipmentContent;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

final class LaunchSystemsPonderTags {
  static final ResourceLocation SYSTEMS = ResourceLocation.fromNamespaceAndPath(LaunchSystems.ID, "launch_systems");

  private LaunchSystemsPonderTags() {}

  static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
    helper.registerTag(SYSTEMS)
        .title("Coaster launch systems")
        .description("Bind and operate physical coaster launch, transport and braking equipment")
        .item(EquipmentContent.TOOL.get())
        .addToIndex()
        .register();
    var systems = helper.addToTag(SYSTEMS);
    systems.add(EquipmentContent.TOOL.getId())
        .add(EquipmentContent.CONTROLLER.getId())
        .add(EquipmentContent.TIRE.getId())
        .add(EquipmentContent.FIN.getId())
        .add(EquipmentContent.SKID.getId())
        .add(EquipmentContent.EDDY.getId())
        .add(EquipmentContent.LSM.getId())
        .add(EquipmentContent.LIM.getId())
        .add(EquipmentContent.FLYWHEEL.getId())
        .add(EquipmentContent.HYDRAULIC.getId())
        .add(EquipmentContent.PNEUMATIC.getId())
        .add(EquipmentContent.LSM_PLATE.getId())
        .add(EquipmentContent.LIM_PLATE.getId())
        .add(EquipmentContent.CATCH_SOCKET.getId())
        .add(EquipmentContent.CATCH_CARRIAGE.getId())
        .add(EquipmentContent.CABLE_GUIDE.getId())
        .add(EquipmentContent.TIRE_SURFACE.getId())
        .add(EquipmentContent.BRAKE_FIN.getId())
        .add(EquipmentContent.SKID_SHOE.getId())
        .add(EquipmentContent.CONDUCTIVE_FIN.getId());
  }
}
