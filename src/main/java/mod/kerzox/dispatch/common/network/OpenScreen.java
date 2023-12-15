package mod.kerzox.dispatch.common.network;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static mod.kerzox.dispatch.common.network.client.OpenScreenClient.handleOnClient;

public class OpenScreen {

    private Screens data;
    private BlockPos pos;

    public enum Screens {
        CABLE_SCREEN
    }

    public OpenScreen(FriendlyByteBuf buf) {
        data = Screens.valueOf(buf.readUtf());
        pos = buf.readBlockPos();
    }

    public OpenScreen(Screens screen, BlockPos pos) {
        data = screen;
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(data.toString());
        buf.writeBlockPos(pos);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Screens getScreen() {
        return data;
    }

    public static boolean handle(OpenScreen packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleOnClient(packet, ctx));
        });
        return true;
    }


}
