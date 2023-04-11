package mod.kerzox.dispatch.registry;

import mod.kerzox.dispatch.common.block.BasicEntityBlock;
import mod.kerzox.dispatch.common.block.transfer.EnergyCableBlock;
import mod.kerzox.dispatch.common.entity.EnergyCable;
import mod.kerzox.dispatch.common.entity.Generator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static mod.kerzox.dispatch.Dispatch.MODID;
import static mod.kerzox.dispatch.registry.DispatchRegistry.Blocks.ENERGY_CABLE_BLOCK;
import static mod.kerzox.dispatch.registry.DispatchRegistry.Blocks.ENERGY_MANIPULATOR_BLOCK;

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
        public static void init() {
        }

 }

    public static final class Blocks {

        public static final makeBlock<EnergyCableBlock> ENERGY_CABLE_BLOCK = makeBlock.buildCustomSuppliedItem("energy_cable_block", EnergyCableBlock::new, BlockBehaviour.Properties.of(Material.AIR).sound(SoundType.WOOL),
                () -> new EnergyCableBlock.Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

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
        }
    }

    public static final class BlockEntities {

        public static final makeBlockEntity<EnergyCable> ENERGY_CABLE = makeBlockEntity.build("energy_cable", EnergyCable::new, ENERGY_CABLE_BLOCK);

        public static final makeBlockEntity<Generator> ENERGY_GENERATOR = makeBlockEntity.build("energy_generator", Generator::new, ENERGY_MANIPULATOR_BLOCK);


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
