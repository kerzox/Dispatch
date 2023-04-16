package mod.kerzox.dispatch.registry;

import mod.kerzox.dispatch.common.block.BasicEntityBlock;
import mod.kerzox.dispatch.common.block.multi.MultiroleEnergyBlock;
import mod.kerzox.dispatch.common.block.multi.MultiroleFluidBlock;
import mod.kerzox.dispatch.common.block.multi.MultiroleItemBlock;
import mod.kerzox.dispatch.common.block.multi.MultirolePipeBlock;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.entity.Generator;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static mod.kerzox.dispatch.Dispatch.MODID;
import static mod.kerzox.dispatch.registry.DispatchRegistry.Blocks.*;

public class DispatchRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

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

        Items.init();
        Effects.init();
        Blocks.init();
        BlockEntities.init();
        Recipes.init();
        Menus.init();

   }

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

//        public static final RegistryObject<MultirolePipeBlock.Item> ENERGY_CABLE = ITEMS.register("energy_cable_item", () -> new MultirolePipeBlock.Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC), PipeTypes.ENERGY));
//        public static final RegistryObject<MultirolePipeBlock.Item> ITEM_CABLE = ITEMS.register("item_cable_item", () -> new MultirolePipeBlock.Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC), PipeTypes.ITEM));

        public static void init() {
        }

 }

    public static final class Blocks {

        public static final makeBlock<MultiroleEnergyBlock> ENERGY_CABLE_BLOCK = makeBlock.buildPipe("energy_cable_block", MultiroleEnergyBlock::new, BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.WOOL), PipeTypes.ENERGY);
        public static final makeBlock<MultiroleItemBlock> ITEM_CABLE_BLOCK = makeBlock.buildPipe("item_cable_block", MultiroleItemBlock::new, BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.WOOL), PipeTypes.ITEM);
        public static final makeBlock<MultiroleFluidBlock> FLUID_CABLE_BLOCK = makeBlock.buildPipe("fluid_cable_block", MultiroleFluidBlock::new, BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.WOOL), PipeTypes.FLUID);

        public static final makeBlock<BasicEntityBlock<Generator>> ENERGY_MANIPULATOR_BLOCK = makeBlock.build("energy_gen_block", prop -> new BasicEntityBlock<Generator>(BlockEntities.ENERGY_GENERATOR.getType(),
                BlockBehaviour.Properties.of(Material.AIR).sound(SoundType.WOOL)), BlockBehaviour.Properties.of(Material.AIR).sound(SoundType.WOOL), true);

        public static void init() {


        }


        public static class makeBlock<T extends Block> implements Supplier<T> {

            public static final List<makeBlock<?>> ENTRIES = new ArrayList<>();

            private final RegistryObject<T> block;

            private final String name;

            private makeBlock(String name, RegistryObject<T> block) {
                this.name = name;
                this.block = block;
                ENTRIES.add(this);
            }

            public static <T extends Block> makeBlock<T> build(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties prop, boolean asItem) {
                RegistryObject<T> ret = BLOCKS.register(name, () -> block.apply(prop));
                if (asItem)
                    ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
                return new makeBlock<>(name, ret);
            }

            public static <T extends Block> makeBlock<T> buildPipe(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties prop, PipeTypes type) {
                RegistryObject<T> ret = BLOCKS.register(name, () -> block.apply(prop));
                ITEMS.register(name, () -> new PipeItem(ret.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC), type));
                return new makeBlock<>(name, ret);
            }

            public static <T extends Block> makeBlock<T> buildCustomSuppliedItem(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties prop, Supplier<BlockItem> itemSupplier) {
                RegistryObject<T> ret = BLOCKS.register(name, () -> block.apply(prop));
                ITEMS.register(name, itemSupplier);
                return new makeBlock<>(name, ret);
            }

            @Override
            public T get() {
                return this.block.get();
            }

            public String getName() {
                return name;
            }

            public static class PipeItem extends BlockItem {

                private PipeTypes type;

                public PipeItem(Block block, Properties pProperties, PipeTypes types) {
                    super(block, pProperties);
                    this.type = types;
                }

                @Override
                protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
                    if (super.placeBlock(pContext, pState)) {
                        if (!pContext.getLevel().isClientSide) {
                            if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof MultirolePipe pipe) {
                                pipe.addType(type, true);
                            }
                        }
                        return true;
                    }
                    return false;
                }

                @Override
                public InteractionResult useOn(UseOnContext pContext) {

                    if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof MultirolePipe clickedOn) {
                        if (!clickedOn.getSubtypes().contains(type)) {
                            clickedOn.addType(type, false);
                            clickedOn.findCapabilityHolders();
                            clickedOn.getManager().addToUpdateQueue(clickedOn);
                            return InteractionResult.PASS;
                        }
                    }

                    return super.useOn(pContext);
                }
            }

        }
    }

    public static final class BlockEntities {

        //public static final makeBlockEntity<MultirolePipe> MULTIROLE_PIPE = makeBlockEntity.build("multirole_cable", MultirolePipe::new, ENERGY_CABLE_BLOCK.get(), ITEM_CABLE_BLOCK.get());
        public static final makeBlockEntity<Generator> ENERGY_GENERATOR = makeBlockEntity.build("energy_generator", Generator::new, ENERGY_MANIPULATOR_BLOCK);

        public static final RegistryObject<BlockEntityType<MultirolePipe>> MULTIROLE_PIPE = BLOCK_ENTITIES.register("multirole_cable_entity", () -> BlockEntityType.Builder.of(MultirolePipe::new, ENERGY_CABLE_BLOCK.get(), ITEM_CABLE_BLOCK.get(), FLUID_CABLE_BLOCK.get()).build(null));


        public static void init() {
        }


        public static class makeBlockEntity<T extends BlockEntity> implements Supplier<BlockEntityType<T>> {

            private final RegistryObject<BlockEntityType<T>> type;

            public static <T extends BlockEntity> makeBlockEntity<T> build(
                    String name,
                    BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier,
                    Supplier<? extends Block> valid) {
                return new makeBlockEntity<T>(BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(blockEntitySupplier, valid.get()).build(null)));
            }

            public static <T extends BlockEntity> makeBlockEntity<T> buildEntityAndBlock(
                    String name,
                    BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier,
                    Blocks.makeBlock<?> valid) {
                return new makeBlockEntity<T>(BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(blockEntitySupplier, valid.get()).build(null)));
            }

            public makeBlockEntity(RegistryObject<BlockEntityType<T>> type) {
                this.type = type;
            }

            @Override
            public BlockEntityType<T> get() {
                return this.getType().get();
            }

            public RegistryObject<BlockEntityType<T>> getType() {
                return type;
            }
        }

    }


}
