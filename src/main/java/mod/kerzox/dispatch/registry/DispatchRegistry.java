package mod.kerzox.dispatch.registry;

import mod.kerzox.dispatch.common.block.DispatchBlock;
import mod.kerzox.dispatch.common.entity.DispatchNetworkEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

import static mod.kerzox.dispatch.Dispatch.MODID;

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
            .icon(() -> Items.ENERGY_CABLES.get(DispatchItem.Tiers.BASIC).get().getDefaultInstance())
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

        public static Map<Capability<?>, Map<DispatchItem.Tiers, RegistryObject<Item>>> DISPATCH_CABLES = new HashMap<>();
        public static Map<DispatchItem.Tiers, RegistryObject<Item>> ENERGY_CABLES = new HashMap<>();
        public static Map<DispatchItem.Tiers, RegistryObject<Item>> ITEM_CABLES = new HashMap<>();
        public static Map<DispatchItem.Tiers, RegistryObject<Item>> FLUID_CABLES = new HashMap<>();

        public static void init() {
            for (DispatchItem.Tiers tier : DispatchItem.Tiers.values()) {
                ENERGY_CABLES.put(tier, ITEMS.register(tier.getSerializedName()+"_energy_cable_item", () -> new DispatchItem(ForgeCapabilities.ENERGY, tier, new Item.Properties())));
                ITEM_CABLES.put(tier, ITEMS.register(tier.getSerializedName()+"_item_cable_item", () -> new DispatchItem(ForgeCapabilities.ITEM_HANDLER, tier, new Item.Properties())));
                FLUID_CABLES.put(tier, ITEMS.register(tier.getSerializedName()+"_fluid_cable_item", () -> new DispatchItem(ForgeCapabilities.FLUID_HANDLER, tier, new Item.Properties())));
            }

            DISPATCH_CABLES.put(ForgeCapabilities.ENERGY, ENERGY_CABLES);
            DISPATCH_CABLES.put(ForgeCapabilities.ITEM_HANDLER, ITEM_CABLES);
            DISPATCH_CABLES.put(ForgeCapabilities.FLUID_HANDLER, FLUID_CABLES);

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

        public static final RegistryObject<BlockEntityType<DispatchNetworkEntity>> DISPATCH_ENTITY
                = BLOCK_ENTITIES.register("dispatch_entity",
                () -> BlockEntityType.Builder.of(DispatchNetworkEntity::new, Blocks.DISPATCH_BLOCK.get()).build(null));


        public static void init() {

        }

    }


}
