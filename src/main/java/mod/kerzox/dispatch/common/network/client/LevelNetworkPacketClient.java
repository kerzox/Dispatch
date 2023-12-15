package mod.kerzox.dispatch.common.network.client;

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
                    network.deserializeNBT(packet.getNbtTag());
                }
            });
        }
    }

}
