package dev.coasterslaunchsystems.content;

import dev.coasterslaunchsystems.LaunchSystems;
import dev.coasterslaunchsystems.math.Mechanism;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class EquipmentContent {
  private EquipmentContent() {}
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LaunchSystems.ID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LaunchSystems.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LaunchSystems.ID);
  public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, LaunchSystems.ID);

  public static final DeferredBlock<RideControllerBlock> CONTROLLER = BLOCKS.register("ride_controller",
      () -> new RideControllerBlock(BlockBehaviour.Properties.of().strength(3)));
  public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.register("ride_controller",
      () -> new BlockItem(CONTROLLER.get(), new Item.Properties()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RideControllerEntity>> CONTROLLER_ENTITY =
      ENTITIES.register("ride_controller", () -> BlockEntityType.Builder.of(RideControllerEntity::new, CONTROLLER.get()).build(null));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> CONTROLLER_SELECTION = selection("controller_selection");
  public static final DeferredBlock<EquipmentCabinetBlock> TIRE = cabinet("tire_drive", Mechanism.TIRE);
  public static final DeferredBlock<EquipmentCabinetBlock> FIN = cabinet("fin_brake", Mechanism.FIN_BRAKE);
  public static final DeferredBlock<EquipmentCabinetBlock> EDDY = cabinet("eddy_brake", Mechanism.EDDY_BRAKE);
  public static final DeferredBlock<EquipmentCabinetBlock> LSM = cabinet("lsm_drive", Mechanism.LSM);
  public static final DeferredBlock<EquipmentCabinetBlock> LIM = cabinet("lim_drive", Mechanism.LIM);
  public static final DeferredBlock<LinearReactionBlock> LSM_PLATE = reaction("lsm_reaction_plate");
  public static final DeferredBlock<LinearReactionBlock> LIM_PLATE = reaction("lim_reaction_plate");
  public static final DeferredBlock<TireSurfaceBlock> TIRE_SURFACE = BLOCKS.register("tire_surface",
      () -> new TireSurfaceBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredItem<BlockItem> TIRE_SURFACE_ITEM = ITEMS.register("tire_surface",
      () -> new BlockItem(TIRE_SURFACE.get(), new Item.Properties()));
  public static final DeferredBlock<BrakeFinBlock> BRAKE_FIN = BLOCKS.register("brake_fin",
      () -> new BrakeFinBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredItem<BlockItem> BRAKE_FIN_ITEM = ITEMS.register("brake_fin",
      () -> new BlockItem(BRAKE_FIN.get(), new Item.Properties()));
  public static final DeferredBlock<ConductiveFinBlock> CONDUCTIVE_FIN = BLOCKS.register("conductive_fin",
      () -> new ConductiveFinBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredItem<BlockItem> CONDUCTIVE_FIN_ITEM = ITEMS.register("conductive_fin",
      () -> new BlockItem(CONDUCTIVE_FIN.get(), new Item.Properties()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EquipmentCabinetEntity>> ENTITY =
      ENTITIES.register("equipment_cabinet", () -> BlockEntityType.Builder.of(EquipmentCabinetEntity::new,
          TIRE.get(), FIN.get(), EDDY.get(), LSM.get(), LIM.get()).build(null));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> FROM = selection("span_from");
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> TO = selection("span_to");
  public static final DeferredItem<SpanToolItem> TOOL = ITEMS.register("span_tool", () -> new SpanToolItem(new Item.Properties().stacksTo(1)));

  private static DeferredBlock<EquipmentCabinetBlock> cabinet(String id, Mechanism mechanism) {
    var block = BLOCKS.register(id, () -> new EquipmentCabinetBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion(), mechanism));
    ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    return block;
  }
  private static DeferredBlock<LinearReactionBlock> reaction(String id) {
    var block = BLOCKS.register(id, () -> new LinearReactionBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
    ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    return block;
  }

  private static DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> selection(String name) {
    return COMPONENTS.register(name, () -> DataComponentType.<GlobalPos>builder()
        .persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC).build());
  }

  public static void register(IEventBus bus) {
    BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); COMPONENTS.register(bus);
    bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) -> {
      if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.REDSTONE_BLOCKS)) {
        event.accept(CONTROLLER.get()); event.accept(TIRE.get()); event.accept(FIN.get()); event.accept(EDDY.get()); event.accept(TIRE_SURFACE.get()); event.accept(BRAKE_FIN.get()); event.accept(CONDUCTIVE_FIN.get());
        event.accept(LSM.get()); event.accept(LIM.get()); event.accept(LSM_PLATE.get()); event.accept(LIM_PLATE.get());
      }
      if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES)) event.accept(TOOL.get());
    });
  }
}
