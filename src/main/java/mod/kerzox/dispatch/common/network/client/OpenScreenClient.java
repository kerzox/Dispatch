package mod.kerzox.dispatch.common.network.client;

import mod.kerzox.dispatch.client.gui.CableScreen;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.network.OpenScreen;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenScreenClient {

    public static void handleOnClient(OpenScreen packet, Supplier<NetworkEvent.Context> ctx) {
        switch (packet.getScreen()) {
            case CABLE_SCREEN -> CableScreen.draw(packet.getPos());
        }
    }

}
