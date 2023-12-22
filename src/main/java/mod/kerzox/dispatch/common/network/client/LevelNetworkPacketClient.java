package mod.kerzox.dispatch.common.network.client;

import mod.kerzox.dispatch.client.gui.CableContainerScreen;
import mod.kerzox.dispatch.client.menu.CableMenu;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelNetworkPacketClient {

    public static void handleOnClient(LevelNetworkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.level().getCapability(LevelNetworkHandler.NETWORK).ifPresent(cap -> {
                if (cap instanceof LevelNetworkHandler network) {
                    if (packet.getNbtTag().contains("node_refresh")) {
                        if (player.containerMenu instanceof CableMenu cableMenu) {
                            cableMenu.refreshOperationList();
                        }
                    }
                    network.deserializeNBT(packet.getNbtTag());
                }
            });
        }
    }

}
