package mod.kerzox.dispatch;

import mod.kerzox.dispatch.client.RenderLevelNetworks;
import mod.kerzox.dispatch.client.gui.CableContainerScreen;
import mod.kerzox.dispatch.client.render.MultiroleCableRenderer;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Dispatch.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(MultiroleCableRenderer.CONNECTED);
        event.register(MultiroleCableRenderer.CONNECTED_BIG);
        event.register(MultiroleCableRenderer.CORE_BIG);
        event.register(MultiroleCableRenderer.CORE);
        event.register(MultiroleCableRenderer.CORE_ENERGY);
        event.register(MultiroleCableRenderer.ENERGY_CONNECTED);
        event.register(MultiroleCableRenderer.ITEM_CONNECTED);
        event.register(MultiroleCableRenderer.FLUID_CONNECTED);
        event.register(MultiroleCableRenderer.ENERGY_BEAM);
        event.register(MultiroleCableRenderer.CORE_FLUID);
        event.register(MultiroleCableRenderer.CORE_ITEM);
        event.register(MultiroleCableRenderer.BORDER);
    }

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(DispatchRegistry.Menus.CABLE_MENU.get(), CableContainerScreen::new);
            BlockEntityRenderers.register(DispatchRegistry.BlockEntities.DISPATCH_ENTITY.get(), MultiroleCableRenderer::new);
        });
        MinecraftForge.EVENT_BUS.register(new RenderLevelNetworks());
    }


}
