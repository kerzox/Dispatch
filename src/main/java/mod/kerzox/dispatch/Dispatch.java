package mod.kerzox.dispatch;

import com.mojang.logging.LogUtils;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.event.CommonEvents;
import mod.kerzox.dispatch.common.item.DispatchItem;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Dispatch.MODID)
public class Dispatch
{

    /*

        DONE
            - Tiers
            - Gui
            - IO on cables

        TODO
            - Upgrades
                - More slots for item/fluids?
                - Speed increase (extraction / insertion)
            - Filters
            - Wrench (for cables)
     */











    // Define mod id in a common place for everything to reference
    public static final String MODID = "dispatch";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Dispatch()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.GENERAL_SPEC);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        DispatchRegistry.init(modEventBus);
        PacketHandler.register();

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommonEvents());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(ClientSetup::init));



    }

    @SubscribeEvent
    public void onCapabilityAttachLevel(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(MODID, "level_network"), new LevelNetworkHandler(event.getObject()));
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == DispatchRegistry.TAB.getKey()) {
            for (Map<DispatchItem.Tiers, RegistryObject<Item>> tieredCables : DispatchRegistry.Items.DISPATCH_CABLES.values()) {
                for (RegistryObject<Item> item : tieredCables.values()) {
                    event.accept(item.get());
                }
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
