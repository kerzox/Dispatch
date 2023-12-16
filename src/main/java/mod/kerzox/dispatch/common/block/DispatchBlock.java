package mod.kerzox.dispatch.common.block;

import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.ILevelNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.entity.SyncBlockEntity;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DispatchBlock extends Block implements EntityBlock {

    public DispatchBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof SyncBlockEntity onClick && pHand == InteractionHand.MAIN_HAND) {
            if (onClick.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getLevel() != null) {
            for (AbstractSubNetwork subNetwork : Objects.requireNonNull(LevelNetworkHandler.getHandler(be.getLevel())).getSubnetsFrom(LevelNode.of(pos))) {
                // just return the first subnet as the item
                return new ItemStack(DispatchRegistry.Items.DISPATCH_CABLES.get(subNetwork.getCapability()).get(subNetwork.getTier()).get());
            }
        }

        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void onPlace(BlockState state, Level pLevel, BlockPos pPos, BlockState pState, boolean moving) {
        if (pLevel.getBlockEntity(pPos) instanceof DynamicTilingEntity notified) {
            for (Direction direction : Direction.values()) {

                pLevel.getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability1 -> {
                    if (capability1 instanceof LevelNetworkHandler network) {

                        BlockEntity blockEntity = pLevel.getBlockEntity(pPos.relative(direction));

                        // TODO expand this for the io so if all subnets are set to none dont show visual connection

                        for (AbstractSubNetwork subNetwork : network.getSubnetsFrom(LevelNode.of(pPos))) {

                            LevelNode node = subNetwork.getNodeByPosition(pPos);
                            // check for other block entities and or get another cable and update it visually
                            if (blockEntity != null && !(blockEntity instanceof DynamicTilingEntity)) {
                                LazyOptional<?> capability = blockEntity.getCapability(subNetwork.getCapability(), direction.getOpposite());
                                if (capability.isPresent() && node.getDirectionalIO().get(direction) != LevelNode.IOTypes.NONE) {
                                    notified.addVisualConnection(direction);
                                }
                            }

                            Optional<AbstractSubNetwork> otherSubnet = network.getSubnetFromPos(subNetwork.getCapability(), LevelNode.of(pPos.relative(direction)));

                            if (otherSubnet.isPresent() && subNetwork.getTier() == otherSubnet.get().getTier() && node.getDirectionalIO().get(direction) != LevelNode.IOTypes.NONE) {
                                if (blockEntity instanceof DynamicTilingEntity dynamicTilingEntity) {
                                    if (otherSubnet.get().getNodeByPosition(dynamicTilingEntity.getBlockPos()).getDirectionalIO().get(direction.getOpposite()) != LevelNode.IOTypes.NONE) {
                                        dynamicTilingEntity.addVisualConnection(direction.getOpposite());
                                        notified.addVisualConnection(direction);
                                    }
                                }
                            }

                        }
                    }

                });


            }
        }

    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        if (pLevel.getBlockEntity(pPos) instanceof DynamicTilingEntity notifiedPipe) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pFromPos);
            BlockPos pos = pFromPos.subtract(pPos);
            Direction facing = Direction.fromDelta(pos.getX(), pos.getY(), pos.getZ());
            pLevel.getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability1 -> {
                findConnections(pPos, pFromPos, notifiedPipe, blockEntity, facing, capability1);
            });
        }

    }

    private void findConnections(BlockPos pPos, BlockPos pFromPos, DynamicTilingEntity notifiedPipe, BlockEntity blockEntity, Direction facing, ILevelNetwork capability1) {
        notifiedPipe.removeVisualConnection(facing);
        for (AbstractSubNetwork subNetwork : ((LevelNetworkHandler) capability1).getSubnetsFrom(LevelNode.of(pPos))) {
            if (blockEntity != null) {
                BlockPos pos1 = pPos.subtract(pFromPos);
                LazyOptional<?> capability = blockEntity.getCapability(subNetwork.getCapability());

                LevelNode node = subNetwork.getNodeByPosition(pPos);

                if (capability.isPresent() && !(blockEntity instanceof DynamicTilingEntity) && node.getDirectionalIO().get(facing) != LevelNode.IOTypes.NONE) {
                    notifiedPipe.addVisualConnection(facing);
                    capability.addListener(l -> notifiedPipe.removeVisualConnection(facing));
                }

                Optional<AbstractSubNetwork> otherSubnet = capability1.getSubnetFromPos(subNetwork.getCapability(), LevelNode.of(pFromPos));

                if (otherSubnet.isPresent() && subNetwork.getTier() == otherSubnet.get().getTier() && node.getDirectionalIO().get(facing) != LevelNode.IOTypes.NONE) {
                    if (blockEntity instanceof DynamicTilingEntity dynamicTilingEntity) {
                        if (otherSubnet.get().getNodeByPosition(dynamicTilingEntity.getBlockPos()).getDirectionalIO().get(facing.getOpposite()) != LevelNode.IOTypes.NONE) {
                            dynamicTilingEntity.addVisualConnection(facing.getOpposite());
                            notifiedPipe.addVisualConnection(facing);
                        }
                    }

                }

            } else {
                if (notifiedPipe.getConnectedSides().get(facing) == DynamicTilingEntity.Face.CONNECTION) {
                    notifiedPipe.removeVisualConnection(facing);
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {


        /*
            Multi subnets means we fake the block break and drop a cable but leave the rest of the subnets.

            TODO
                - when i add a wrench check for which capability type to remove and remove that instead of random.
         */

        List<AbstractSubNetwork> subNetworks = LevelNetworkHandler.getHandler(level).getSubnetsFrom(LevelNode.of(pos));

        if (subNetworks.size() > 1) {
            for (AbstractSubNetwork subNetwork : subNetworks) {
                LevelNetworkHandler.getHandler(level).detachFromCapability(subNetwork.getCapability(), pos);
                ItemStack drop = new ItemStack(DispatchRegistry.Items.DISPATCH_CABLES.get(subNetwork.getCapability()).get(subNetwork.getTier()).get());
                Block.popResource(level, pos, drop);
                level.updateNeighborsAt(pos, this);

                if (level.getBlockEntity(pos) instanceof DynamicTilingEntity dynamicTilingEntity) {

                    for (Direction direction : Direction.values()) {
                        level.getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability1 -> {
                            findConnections(pos, pos.relative(direction), dynamicTilingEntity, level.getBlockEntity(pos.relative(direction)), direction, capability1);
                        });
                    }

                }

                return false;
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
        BlockEntity blockentity = p_287596_.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockentity instanceof DynamicTilingEntity dynamicTilingEntity) {
            return List.of(dynamicTilingEntity.getDrop());
        }

        return List.of(ItemStack.EMPTY);

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oState, boolean p_60519_) {
        if (level.getBlockEntity(pos) instanceof DynamicTilingEntity pipe) {
            for (Direction direction : pipe.getConnectionsAsDirections()) {
                if (level.getBlockEntity(pos.relative(direction)) instanceof DynamicTilingEntity connectedTo) {
                    connectedTo.removeVisualConnection(direction.getOpposite());
                }
            }
            pipe.setSubnets(LevelNetworkHandler.getHandler(level).getSubnetsFrom(LevelNode.of(pos)));
        }
        super.onRemove(state, level, pos, oState, p_60519_);
        level.getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability -> {
            if (capability instanceof LevelNetworkHandler network) {
                network.detach(pos);
            }
        });
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new DynamicTilingEntity(p_153215_, p_153216_);
    }
}
