package mod.kerzox.dispatch.common.block;

import mod.kerzox.dispatch.common.entity.BasicBlockEntity;
import mod.kerzox.dispatch.common.entity.OLD.IConnectablePipe;
import mod.kerzox.dispatch.common.util.IClientTickable;
import mod.kerzox.dispatch.common.util.IServerTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class DefaultPipeBlock<T extends BlockEntity> extends BasicBlock implements EntityBlock {

    protected RegistryObject<BlockEntityType<T>> type;
    protected boolean shouldTick;

    public static final EnumProperty<PipeConnections> NORTH = EnumProperty.create("north", PipeConnections.class);
    public static final EnumProperty<PipeConnections> SOUTH = EnumProperty.create("south", PipeConnections.class);
    public static final EnumProperty<PipeConnections> WEST = EnumProperty.create("west", PipeConnections.class);
    public static final EnumProperty<PipeConnections> EAST = EnumProperty.create("east", PipeConnections.class);
    public static final EnumProperty<PipeConnections> UP = EnumProperty.create("up", PipeConnections.class);
    public static final EnumProperty<PipeConnections> DOWN = EnumProperty.create("down", PipeConnections.class);


    public DefaultPipeBlock(RegistryObject<BlockEntityType<T>> type, Properties properties) {
        super(properties);
        this.type = type;
        shouldTick = true;
    }

    public DefaultPipeBlock(RegistryObject<BlockEntityType<T>> type, Properties properties, boolean shouldTick) {
        super(properties);
        this.type = type;
        this.shouldTick = shouldTick;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof BasicBlockEntity onClick && pHand == InteractionHand.MAIN_HAND) {
            if (onClick.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);

        // just find a valid capability
        for (Direction direction : Direction.values()) {
            BlockPos pNeighborPos = pPos.relative(direction);
            if (pLevel.getBlockEntity(pNeighborPos) != null) {
                if (pLevel.getBlockEntity(pPos) instanceof IConnectablePipe basic) {
                    basic.checkInventoryAt(pLevel.getBlockEntity(pNeighborPos), direction);
                }
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        BlockState state = super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
        if (pLevel.getBlockEntity(pNeighborPos) != null) {
            if (pLevel.getBlockEntity(pCurrentPos) instanceof IConnectablePipe basic) {
                if (pLevel.getBlockEntity(pNeighborPos) instanceof IConnectablePipe neighbourPipe) {
                    basic.getPipe().attemptConnection(pCurrentPos, pNeighborPos, basic, neighbourPipe);
                    return state;
                }
                // if we don't find a pipe check if it has a capability
                basic.checkInventoryAt(pLevel.getBlockEntity(pNeighborPos), pDirection);
            }
        }
        return state;
    }

    //    @Override
//    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
//        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
//        if (pLevel.getBlockEntity(pFromPos) != null) {
//            if (pLevel.getBlockEntity(pPos) instanceof IConnectablePipe basic) {
//                if (pLevel.getBlockEntity(pFromPos) instanceof IConnectablePipe neighbourPipe) {
//                    basic.getPipe().attemptConnection(pPos, pFromPos, basic, neighbourPipe);
//                    return;
//                }
//                // if we don't find a pipe check if it has a capability
//                basic.getManager().handlePossibleCapability(basic, pLevel.getBlockEntity(pFromPos));
//            }
//        }
//    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof IConnectablePipe basic) {
            if (basic.getPipe().hasManager()) {
                basic.getPipe().getManager().detach(basic);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return this.type.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return shouldTick ? (pLevel1, pPos, pState1, pBlockEntity) -> {
            if (!pLevel1.isClientSide && pBlockEntity instanceof IServerTickable tick) {
                tick.onServer();
            }
            if (pLevel1.isClientSide && pBlockEntity instanceof IClientTickable tick) {
                tick.onClient();
            }
        } : null;
    }

}
