package mod.kerzox.dispatch;

import mod.kerzox.dispatch.client.render.MultiroleCableRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Dispatch.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        System.out.println("Adding model to registry");
        event.register(MultiroleCableRenderer.CONNECTED);
        event.register(MultiroleCableRenderer.CONNECTED_BIG);
        event.register(MultiroleCableRenderer.CORE_BIG);
    }


    public static void init(FMLClientSetupEvent event) {

    }
}
