package mod.kerzox.dispatch.common.network;

import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.network.client.LevelNetworkPacketClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelNetworkPacket {

    CompoundTag nbtTag;

    public LevelNetworkPacket(CompoundTag up) {
        this.nbtTag = up;
    }

    public CompoundTag getNbtTag() {
        return nbtTag;
    }

    public LevelNetworkPacket(FriendlyByteBuf buf) {
        this.nbtTag = buf.readAnySizeNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(nbtTag);
    }

    public static boolean handle(LevelNetworkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) handleOnServer(packet, ctx);
            else DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LevelNetworkPacketClient.handleOnClient(packet, ctx));
        });
        return true;
    }

    private static void handleOnServer(LevelNetworkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            Level level = player.getCommandSenderWorld();
            level.getCapability(LevelNetworkHandler.NETWORK).ifPresent(cap -> {
                if (cap instanceof LevelNetworkHandler network) {
                    if (packet.nbtTag.contains("node_to_update")) {
                        //TODO
                    }
                    else PacketHandler.sendToClientPlayer(new LevelNetworkPacket(network.serializeNBT()), player);
                }
            });
        }
    }



}
