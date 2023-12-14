package mod.kerzox.dispatch.registry;

import mod.kerzox.dispatch.common.block.DispatchBlock;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.NetworkHandler;
import mod.kerzox.dispatch.common.capability.energy.EnergyNetworkHandler;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static mod.kerzox.dispatch.Dispatch.MODID;
import static net.minecraftforge.versions.forge.ForgeVersion.MOD_ID;

public class DispatchRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ITEMS.register(bus);
        FLUIDS.register(bus);
        FLUID_TYPES.register(bus);
        RECIPE_TYPES.register(bus);
        RECIPES.register(bus);
        MENUS.register(bus);
        EFFECTS.register(bus);
        PARTICLE_TYPES.register(bus);
        CREATIVE_MODE_TABS.register(bus);

        Effects.init();
        Blocks.init();
        Items.init();
        BlockEntities.init();
        Recipes.init();
        Menus.init();

    }

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("dispatch_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Dispatch"))
            .icon(() -> Items.ENERGY_CABLE_ITEM.get().getDefaultInstance())
            .build());


    public static class Particles {

        public static void init() {

        }


    }

    public static final class Effects {
        public static void init() {


        }


    }

    public static final class Menus {
        public static void init() {

        }


    }

    public static final class Recipes {
        public static void init() {

        }


    }

    public static final class Items {

        public static final RegistryObject<Item> ENERGY_CABLE_ITEM = ITEMS.register("dispatch_item", () ->
                new BlockItem(Blocks.DISPATCH_BLOCK.get(), new Item.Properties()) {
                    @Override
                    protected boolean placeBlock(BlockPlaceContext ctx, BlockState p_40579_) {
                        if (ctx.getPlayer().isShiftKeyDown()) return false;
                        if (super.placeBlock(ctx, p_40579_)) {
                            ctx.getLevel().getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                                Player player = ctx.getPlayer();
                                if (capability instanceof NetworkHandler handler) {
                                    handler.createOrAttachToCapabilityNetwork(ForgeCapabilities.ENERGY, ctx.getClickedPos(), true);
                                }
                            });
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public InteractionResult useOn(UseOnContext ctx) {
                        if (!ctx.getLevel().isClientSide && ctx.getPlayer().isShiftKeyDown()) {
                            ctx.getLevel().getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                                Player player = ctx.getPlayer();
                                if (capability instanceof NetworkHandler handler) {
                                    AbstractSubNetwork network1 = handler.getSubnetFromPos(ForgeCapabilities.ENERGY, LevelNode.of(ctx.getClickedPos()));
                                    if (network1 == null) {
                                        handler.createOrAttachToCapabilityNetwork(ForgeCapabilities.ENERGY, ctx.getClickedPos(), true);
                                        for (Direction direction : Direction.values()) {
                                            BlockPos pos = ctx.getClickedPos().relative(direction);
                                            if (ctx.getLevel().getBlockEntity(pos) instanceof DynamicTilingEntity entity) {
                                                entity.addVisualConnection(direction.getOpposite());
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        return super.useOn(ctx);
                    }
                });

        public static final RegistryObject<Item> ITEM_CABLE_ITEM = ITEMS.register("dispatch_2_item", () ->
                new BlockItem(Blocks.DISPATCH_BLOCK.get(), new Item.Properties()) {
                    @Override
                    protected boolean placeBlock(BlockPlaceContext ctx, BlockState p_40579_) {
                        if (ctx.getPlayer().isShiftKeyDown()) return false;
                        if (super.placeBlock(ctx, p_40579_)) {
                            ctx.getLevel().getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                                Player player = ctx.getPlayer();
                                if (capability instanceof NetworkHandler handler) {
                                    handler.createOrAttachToCapabilityNetwork(ForgeCapabilities.ITEM_HANDLER, ctx.getClickedPos(), true);
                                }
                            });
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public InteractionResult useOn(UseOnContext ctx) {
                        if (!ctx.getLevel().isClientSide && ctx.getPlayer().isShiftKeyDown()) {
                            ctx.getLevel().getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                                Player player = ctx.getPlayer();
                                if (capability instanceof NetworkHandler handler) {
                                    AbstractSubNetwork network1 = handler.getSubnetFromPos(ForgeCapabilities.ITEM_HANDLER, LevelNode.of(ctx.getClickedPos()));
                                    if (network1 == null) {
                                        handler.createOrAttachToCapabilityNetwork(ForgeCapabilities.ITEM_HANDLER, ctx.getClickedPos(), true);
                                        for (Direction direction : Direction.values()) {
                                            BlockPos pos = ctx.getClickedPos().relative(direction);
                                            if (ctx.getLevel().getBlockEntity(pos) instanceof DynamicTilingEntity entity) {
                                                entity.addVisualConnection(direction.getOpposite());
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        return super.useOn(ctx);
                    }
                });

        public static void init() {

        }

    }

    public static final class Blocks {

        public static final RegistryObject<Block> DISPATCH_BLOCK = BLOCKS.register("dispatch_block", () -> new DispatchBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .noOcclusion()
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .strength(1.5F, 3.0F)));

//        public static final RegistryObject<Block> ITEM_CABLE_BLOCK = BLOCKS.register("item_cable_block", () -> new DispatchBlock(
//                ForgeCapabilities.ITEM_HANDLER,
//                BlockBehaviour.Properties.of()
//                        .mapColor(MapColor.METAL)
//                        .instrument(NoteBlockInstrument.BASEDRUM)
//                        .strength(1.5F, 3.0F)));

        public static void init() {


        }


    }

    public static final class BlockEntities {

        public static final RegistryObject<BlockEntityType<DynamicTilingEntity>> DISPATCH_ENTITY
                = BLOCK_ENTITIES.register("dispatch_entity",
                () -> BlockEntityType.Builder.of(DynamicTilingEntity::new, Blocks.DISPATCH_BLOCK.get()).build(null));


        public static void init() {

        }

    }


}
