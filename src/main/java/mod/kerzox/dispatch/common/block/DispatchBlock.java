package mod.kerzox.dispatch.common.block;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.NetworkHandler;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.entity.SyncBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

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
    public void onPlace(BlockState state, Level pLevel, BlockPos pPos, BlockState pState, boolean moving) {
        if (pLevel.getBlockEntity(pPos) instanceof DynamicTilingEntity notified) {
            for (Direction direction : Direction.values()) {

                pLevel.getCapability(NetworkHandler.NETWORK).ifPresent(capability1 -> {
                    if (capability1 instanceof NetworkHandler network) {

                        BlockEntity blockEntity = pLevel.getBlockEntity(pPos.relative(direction));

                        // TODO expand this for the io so if all subnets are set to none dont show visual connection

                        for (AbstractSubNetwork subNetwork : network.getSubnetsFrom(LevelNode.of(pPos))) {
                            // check for other block entities and or get another cable and update it visually
                            if (blockEntity != null) {
                                LazyOptional<?> capability = blockEntity.getCapability(subNetwork.getCapability());
                                if (capability.isPresent()) {
                                    if (blockEntity instanceof DynamicTilingEntity dynamicTilingEntity) {
                                        dynamicTilingEntity.addVisualConnection(direction.getOpposite());
                                    }
                                }
                            }

                            if (network.getSubnetFromPos(subNetwork.getCapability(), LevelNode.of(pPos.relative(direction))).isPresent()) {
                                notified.addVisualConnection(direction);
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
            pLevel.getCapability(NetworkHandler.NETWORK).ifPresent(capability1 -> {
                for (AbstractSubNetwork subNetwork : ((NetworkHandler) capability1).getSubnetsFrom(LevelNode.of(pPos))) {
                    if (blockEntity != null) {
                        BlockPos pos1 = pPos.subtract(pFromPos);
                        LazyOptional<?> capability = blockEntity.getCapability(subNetwork.getCapability());
                        if (capability.isPresent()) {
                            notifiedPipe.addVisualConnection(facing);
                            capability.addListener(l -> notifiedPipe.removeVisualConnection(facing));
                            if (blockEntity instanceof DynamicTilingEntity energyCableEntity) {
                                energyCableEntity.addVisualConnection(facing.getOpposite());
                            }
                        }

                    } else {
                        if (notifiedPipe.getConnectedSides().get(facing) == DynamicTilingEntity.Face.CONNECTION) {
                            notifiedPipe.removeVisualConnection(facing);
                        }
                    }
                }
            });
        }

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oState, boolean p_60519_) {
        if (level.getBlockEntity(pos) instanceof DynamicTilingEntity pipe) {
            for (Direction direction : pipe.getConnectionsAsDirections()) {
                if (level.getBlockEntity(pos.relative(direction)) instanceof DynamicTilingEntity connectedTo) {
                    connectedTo.removeVisualConnection(direction.getOpposite());
                }
            }
        }
        super.onRemove(state, level, pos, oState, p_60519_);
        level.getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
            if (capability instanceof NetworkHandler network) {
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
