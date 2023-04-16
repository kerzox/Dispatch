package mod.kerzox.dispatch;

import com.mojang.logging.LogUtils;
import mod.kerzox.dispatch.client.render.MultiroleCableRenderer;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.event.BlockEvents;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Dispatch.MODID)
public class Dispatch
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dispatch";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Dispatch()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEntityRenderRegister);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        DispatchRegistry.init(modEventBus);
        PacketHandler.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockEvents());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(ClientSetup::init));

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void onEntityRenderRegister(EntityRenderersEvent.RegisterRenderers e) {
        System.out.println("Registering Entity Renderers");
        e.registerBlockEntityRenderer(DispatchRegistry.BlockEntities.MULTIROLE_PIPE.get(), MultiroleCableRenderer::new);
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
