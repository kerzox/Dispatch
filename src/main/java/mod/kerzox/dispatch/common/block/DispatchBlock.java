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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DispatchBlock extends Block implements EntityBlock {

    public DispatchBlock(Properties p_49795_) {
        super(p_49795_);
    }
    private VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private VoxelShape[] allValidSides = new VoxelShape[]{
            Shapes.or(Block.box(5, 0, 5, 11, 11, 11)), // down 0
            Shapes.or(Block.box(5, 5, 5, 11, 11+5, 11)), // up 1
            Shapes.or(Block.box(5, 5, 0, 11, 11, 5)), // north 2
            Shapes.or(Block.box(5, 5, 5, 11, 11, 11+5)), // south 3
            Shapes.or(Block.box(0, 5, 5, 11, 11, 11)), // west 4
            Shapes.or(Block.box(5, 5, 5, 11+5, 11, 11)), // east 5
    };

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pLevel.getBlockEntity(pPos) instanceof DynamicTilingEntity pipe) {
            VoxelShape combinedShape = CORE;
            for (Direction direction : pipe.getConnectionsAsDirections()) {
                if (direction == Direction.UP) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[1]);
                }
                if (direction == Direction.DOWN) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[0]);
                }
                if (direction == Direction.WEST) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[4]);
                }
                if (direction == Direction.EAST) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[5]);
                }
                if (direction == Direction.NORTH) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[2]);
                }
                if (direction == Direction.SOUTH) {
                    combinedShape = Shapes.or(combinedShape, allValidSides[3]);
                }
            }

            return combinedShape;
        }
        return CORE;
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
            notified.updateVisualConnections();
            for (Direction direction : Direction.values()) {
                if (pLevel.getBlockEntity(pPos.relative(direction)) instanceof DynamicTilingEntity tilingEntity) tilingEntity.updateVisualConnections();
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
            if (pLevel.getBlockEntity(pPos) instanceof DynamicTilingEntity notified) {
                notified.updateVisualConnections();
                for (Direction direction : Direction.values()) {
                    if (pLevel.getBlockEntity(pPos.relative(direction)) instanceof DynamicTilingEntity tilingEntity) tilingEntity.updateVisualConnections();
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
                    dynamicTilingEntity.updateVisualConnections();
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
