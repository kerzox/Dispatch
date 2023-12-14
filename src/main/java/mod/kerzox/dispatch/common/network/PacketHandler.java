package mod.kerzox.dispatch.common.network;

import mod.kerzox.dispatch.Dispatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Dispatch.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextID() {
        return ID++;
    }

    public static void register() {
        INSTANCE.messageBuilder(LevelNetworkPacket.class, nextID())
                .encoder(LevelNetworkPacket::toBytes)
                .decoder(LevelNetworkPacket::new)
                .consumerMainThread(LevelNetworkPacket::handle)
                .add();
    }

    public static void sendToClientPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllClientsFromChunk(Object packet, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

}
