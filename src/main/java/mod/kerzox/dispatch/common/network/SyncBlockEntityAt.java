package mod.kerzox.dispatch.common.network;

import mod.kerzox.dispatch.common.entity.BasicBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBlockEntityAt {


    private CompoundTag tag;

    public SyncBlockEntityAt(FriendlyByteBuf buf) {
        this.tag = buf.readAnySizeNbt();
    }

    public SyncBlockEntityAt(CompoundTag tag) {
        this.tag = tag;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public static boolean handle(SyncBlockEntityAt packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleOnClient(packet, ctx));
            else handleOnServer(packet, ctx);
        });
        return true;
    }

    private static void handleOnServer(SyncBlockEntityAt packet, Supplier<NetworkEvent.Context> ctx) {
        BlockPos pos = NbtUtils.readBlockPos(packet.tag.getCompound("pos"));
        Level level = ctx.get().getSender().getLevel();
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BasicBlockEntity entity) {
            entity.handleUpdateTag(packet.tag);
        }
    }

    private static void handleOnClient(SyncBlockEntityAt packet, Supplier<NetworkEvent.Context> ctx) {
        BlockPos pos = NbtUtils.readBlockPos(packet.tag.getCompound("pos"));
        Level level = Minecraft.getInstance().player.getLevel();
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof BasicBlockEntity entity) {
            entity.handleUpdateTag(packet.tag);
        }
    }
}
