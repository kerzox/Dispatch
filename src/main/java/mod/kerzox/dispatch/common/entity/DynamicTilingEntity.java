package mod.kerzox.dispatch.common.entity;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
//            pLevel.getCapability(LevelNetworkHandler.NETWORK).ifPresent(cap -> {
//                if (cap instanceof LevelNetworkHandler networkHandler) {
//
//                    int total = 0;
//                    for (AbstractNetwork<?> network : networkHandler.getNetworkMap().values()) {
//                        total += network.getSubNetworks().size();
//                    }
//
//                    pPlayer.sendSystemMessage(Component.literal("Total Subnet Count: " + total));
//                    pPlayer.sendSystemMessage(Component.literal("Subnets at this position: " + networkHandler.getSubnetsFrom(LevelNode.of(pPos)).size()));
//                    for (AbstractSubNetwork subNetwork : networkHandler.getSubnetsFrom(LevelNode.of(pPos))) {
//                        pPlayer.sendSystemMessage(Component.literal("Network: " + subNetwork));
//                        pPlayer.sendSystemMessage(Component.literal("Network size: " + subNetwork.getNodes().size()));
//                        //pPlayer.sendSystemMessage(Component.literal("Tag: " + subNetwork.serializeNBT()));
//                        pPlayer.sendSystemMessage(Component.literal("Node Data: " + subNetwork.getNodeByPosition(worldPosition).serialize()));
//                        System.out.println(subNetwork.serializeNBT());
//                    }
//                }
//            });
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<ILevelNetwork> levelNetworkLazyOptional = level.getCapability(LevelNetworkHandler.NETWORK);
        if (levelNetworkLazyOptional.resolve().isPresent()) {
            if (levelNetworkLazyOptional.resolve().get() instanceof LevelNetworkHandler handler) {
                Optional<AbstractSubNetwork> subNetwork = handler.getSubnetFromPos(cap, LevelNode.of(worldPosition));
                if (subNetwork.isPresent()) return subNetwork.get().getHandler(worldPosition, side);
            }
        }

        return super.getCapability(cap, side);
    }

    public void updateVisualConnections() {

        for (Direction direction : Direction.values()) {
            removeVisualConnection(direction);
        }

        for (AbstractSubNetwork subNetwork : LevelNetworkHandler.getHandler(level).getSubnetsFrom(LevelNode.of(worldPosition))) {
            for (Direction direction : Direction.values()) {

                if (subNetwork.getNodeByPosition(worldPosition).getDirectionalIO().get(direction) == LevelNode.IOTypes.NONE) continue;

                BlockPos neighbour = worldPosition.relative(direction);
                BlockEntity be = level.getBlockEntity(neighbour);

                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    // show a visual connection
                    if (be.getCapability(subNetwork.getCapability()).isPresent()) addVisualConnection(direction);
                }

                LevelNetworkHandler.getHandler(level).getSubnetFromPos(subNetwork.getCapability(), LevelNode.of(neighbour)).ifPresent(otherSub -> {

                    if (otherSub.getNodeByPosition(neighbour).getDirectionalIO().get(direction.getOpposite()) != LevelNode.IOTypes.NONE) {
                        addVisualConnection(direction);
//
//                        if (be instanceof DynamicTilingEntity dynamicTilingEntity) {
//                            dynamicTilingEntity.addVisualConnection(direction.getOpposite());
//                        }

                    }

                });

            }
        }

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

    // this is only used when the block is removed the world and we need to get the drop
    private List<AbstractSubNetwork> subnets = new ArrayList<>();

    public void setSubnets(List<AbstractSubNetwork> subnets) {
        this.subnets = subnets;
    }

    public ItemStack getDrop() {
        for (AbstractSubNetwork subnet : subnets) {
            return new ItemStack(DispatchRegistry.Items.DISPATCH_CABLES.get(subnet.getCapability()).get(subnet.getTier()).get());
        }
        return ItemStack.EMPTY;
    }
}
