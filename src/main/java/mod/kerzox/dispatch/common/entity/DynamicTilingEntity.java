package mod.kerzox.dispatch.common.entity;

import com.google.common.graph.Network;
import mod.kerzox.dispatch.common.capability.*;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicTilingEntity extends SyncBlockEntity {

    public enum Face {
        NONE,
        CONNECTION
    }

    private HashMap<Direction, Face> connectedSides = new HashMap<>(Map.of(
            Direction.NORTH, Face.NONE,
            Direction.SOUTH, Face.NONE,
            Direction.EAST, Face.NONE,
            Direction.WEST, Face.NONE,
            Direction.UP, Face.NONE,
            Direction.DOWN, Face.NONE));

    public DynamicTilingEntity(BlockPos pos, BlockState state) {
        super(DispatchRegistry.BlockEntities.DISPATCH_ENTITY.get(), pos, state);
    }

    @Override
    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            pLevel.getCapability(NetworkHandler.NETWORK).ifPresent(cap -> {
                if (cap instanceof NetworkHandler networkHandler) {

                    int total = 0;
                    for (AbstractNetwork<?> network : networkHandler.getNetworkMap().values()) {
                        total += network.getSubNetworks().size();
                    }

                    pPlayer.sendSystemMessage(Component.literal("Total Subnet Count: " + total));
                    pPlayer.sendSystemMessage(Component.literal("Subnets at this position: " + networkHandler.getSubnetsFrom(LevelNode.of(pPos)).size()));
                    for (AbstractSubNetwork subNetwork : networkHandler.getSubnetsFrom(LevelNode.of(pPos))) {
                        pPlayer.sendSystemMessage(Component.literal("Network: " + subNetwork));
                        pPlayer.sendSystemMessage(Component.literal("Network size: " + subNetwork.getNodes().size()));
                        //pPlayer.sendSystemMessage(Component.literal("Tag: " + subNetwork.serializeNBT()));
                        System.out.println(subNetwork.serializeNBT());
                    }
                }
            });
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<ILevelNetwork> levelNetworkLazyOptional = level.getCapability(NetworkHandler.NETWORK);
        if (levelNetworkLazyOptional.resolve().isPresent()) {
            if (levelNetworkLazyOptional.resolve().get() instanceof NetworkHandler handler) {
                Optional<AbstractSubNetwork> subNetwork = handler.getSubnetFromPos(cap, LevelNode.of(worldPosition));
                if (subNetwork.isPresent()) return subNetwork.get().getHandler(side);
            }
        }

        return super.getCapability(cap, side);
    }

    public void addVisualConnection(Direction facing) {
        connectedSides.put(facing, Face.CONNECTION);
        syncBlockEntity();
    }

    public void removeVisualConnection(Direction facing) {
        connectedSides.put(facing, Face.NONE);
        syncBlockEntity();
    }

    public HashMap<Direction, Face> getConnectedSides() {
        return connectedSides;
    }

    public Set<Direction> getConnectionsAsDirections() {
        HashSet<Direction> directions = new HashSet<>();
        connectedSides.forEach((d, c) -> {
            if (c == Face.CONNECTION) directions.add(d);
        });
        return directions;
    }

    @Override
    protected void write(CompoundTag pTag) {
        ListTag directions = new ListTag();
        connectedSides.forEach((d, c) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("direction", d.getSerializedName());
            tag.putBoolean("connection", c == Face.CONNECTION);
            directions.add(tag);
        });
        pTag.put("pipe_connections", directions);
    }

    @Override
    protected void read(CompoundTag pTag) {
        ListTag list = pTag.getList("pipe_connections", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag listItem = list.getCompound(i);
            connectedSides.put(Direction.byName(listItem.getString("direction")),
                    listItem.getBoolean("connection") ? Face.CONNECTION : Face.NONE);
        }
    }

}
