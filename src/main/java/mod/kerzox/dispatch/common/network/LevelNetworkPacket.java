package mod.kerzox.dispatch.common.network;

import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.network.client.LevelNetworkPacketClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class LevelNetworkPacket {

    CompoundTag nbtTag;

    public static LevelNetworkPacket of(Capability<?> capability, LevelNode node) {
        CompoundTag tag = new CompoundTag();
        CompoundTag tag1 = new CompoundTag();
        tag1.put("node", node.serialize());
        tag1.putString("cap", capability.getName());
        tag.put("node_to_update", tag1);
        return new LevelNetworkPacket(tag);
    }

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
                        CompoundTag data = packet.nbtTag.getCompound("node_to_update");
                        String capabilityName = data.getString("cap");
                        LevelNode node = new LevelNode(data.getCompound("node"));

                        for (Capability<?> capability : network.getNetworkMap().keySet()) {
                            if (capability.getName().equals(capabilityName)) {
                                network.getSubnetFromPos(capability, node).ifPresent(
                                        subNetwork -> {
                                            LevelNode oldNode = new LevelNode(subNetwork.getNodeByPosition(node.getPos()).serialize());
                                            subNetwork.getNodeByPosition(node.getPos()).read(data.getCompound("node"));
                                            subNetwork.update();
                                            network.getNetworkByCapability(subNetwork.getCapability()).updateNetwork(oldNode, node);
                                            if (level.getBlockEntity(oldNode.getPos()) instanceof DynamicTilingEntity tilingEntity) {
                                                tilingEntity.updateVisualConnections();
                                                level.updateNeighborsAt(tilingEntity.getBlockPos(), tilingEntity.getBlockState().getBlock());
                                            }
                                        }
                                );
                            }
                        }

                        PacketHandler.sendToClientPlayer(new LevelNetworkPacket(new CompoundTag()), ctx.get().getSender());

                    }
                    else PacketHandler.sendToClientPlayer(new LevelNetworkPacket(network.serializeNBT()), player);
                }
            });
        }
    }



}
