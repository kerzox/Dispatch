package mod.kerzox.dispatch.common.block.multi;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.block.BasicEntityBlock;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;

import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class MultirolePipeBlock extends BasicEntityBlock<MultirolePipe> {

    private PipeTypes defaultType;
    private HashMap<Direction, VoxelShape> cache = new HashMap<>();
    private VoxelShape CORE = Shapes.or(Block.box(6, 6, 6, 10, 10, 10));
    private VoxelShape[] allValidSides = new VoxelShape[]{
            Shapes.or(Block.box(6, 0, 6, 10, 10, 10)), // down 0
            Shapes.or(Block.box(6, 6, 6, 10, 10+7, 10)), // up 1
            Shapes.or(Block.box(6, 6, 0, 10, 10, 6)), // north 2
            Shapes.or(Block.box(6, 6, 6, 10, 10, 10+7)), // south 3
            Shapes.or(Block.box(0, 6, 6, 10, 10, 10)), // west 4
            Shapes.or(Block.box(6, 6, 6, 10+7, 10, 10)), // east 5
    };

    private void initializeShapeCache() {
        for (Direction direction : Direction.values()) {
            VoxelShape combinedShape = CORE;
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
            cache.put(direction, combinedShape);
        }
    }

    public MultirolePipeBlock(Properties properties, PipeTypes defaultType) {
        super(DispatchRegistry.BlockEntities.MULTIROLE_PIPE, properties);
        initializeShapeCache();
        this.defaultType = defaultType;
    }

    public void updateVoxel() {
        allValidSides = new VoxelShape[]{
                Shapes.or(Block.box(6, 0, 6, 10, 10, 10)), // down 0
                Shapes.or(Block.box(6, 6, 6, 10, 10+6, 10)), // up 1
                Shapes.or(Block.box(6, 6, 0, 10, 10, 6)), // north 2
                Shapes.or(Block.box(6, 6, 6, 10, 10, 10+6)), // south 3
                Shapes.or(Block.box(0, 6, 6, 10, 10, 10)), // west 4
                Shapes.or(Block.box(6, 6, 6, 10+6, 10, 10)), // east 5
        };
    }

    public PipeTypes getDefaultType() {
        return defaultType;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pLevel.getBlockEntity(pPos) instanceof MultirolePipe pipe) {
            if (pipe.getSubtypes().size() > 1) {
                return Shapes.block();
            }
            VoxelShape combinedShape = CORE;
            for (Map.Entry<Direction, Boolean> entityEntry : pipe.getVisualConnectionMap().entrySet()) {
                if (entityEntry.getValue()) {
                    if (entityEntry.getKey() == Direction.UP) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[1]);
                    }
                    if (entityEntry.getKey() == Direction.DOWN) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[0]);
                    }
                    if (entityEntry.getKey() == Direction.WEST) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[4]);
                    }
                    if (entityEntry.getKey() == Direction.EAST) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[5]);
                    }
                    if (entityEntry.getKey() == Direction.NORTH) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[2]);
                    }
                    if (entityEntry.getKey() == Direction.SOUTH) {
                        combinedShape = Shapes.or(combinedShape, allValidSides[3]);
                    }

                }
            }
            return combinedShape;
        }
       return CORE;
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        super.destroy(pLevel, pPos, pState);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        if (pLevel.getBlockEntity(pPos) instanceof IPipe notifiedPipe) {
            if (pLevel.getBlockEntity(pFromPos) instanceof IPipe updater) {
                notifiedPipe.getManager().attemptConnectionFrom(notifiedPipe, updater);
                for (PipeTypes subtype : notifiedPipe.getSubtypes()) {
                    if (updater.getSubtypes().contains(subtype)) {
                        notifiedPipe.addVisualConnection(Direction.fromNormal(pFromPos.subtract(pPos)), true);
                        updater.addVisualConnection(Direction.fromNormal(pPos.subtract(pFromPos)), true);
                        notifiedPipe.getAsBlockEntity().syncBlockEntity();
                        updater.getAsBlockEntity().syncBlockEntity();
                        break;
                    } else if (notifiedPipe.getVisualConnectionMap().get(Direction.fromNormal(pFromPos.subtract(pPos)))) {
                        notifiedPipe.removeVisualConnection(Direction.fromNormal(pFromPos.subtract(pPos)));
                        updater.removeVisualConnection(Direction.fromNormal(pPos.subtract(pFromPos)));
                    }
                }
            } else if (pLevel.getBlockEntity(pFromPos) != null) {
                notifiedPipe.addVisualConnection(Direction.fromNormal(pFromPos.subtract(pPos)), true);
                notifiedPipe.getAsBlockEntity().findCapabilityHolders();
            }
            notifiedPipe.getManager().onNetworkUpdate(notifiedPipe);
        }

    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pLevel.getBlockEntity(pCurrentPos) instanceof IPipe notifiedPipe) {
            if (pLevel.getBlockEntity(pNeighborPos) instanceof IPipe updater) {
                for (PipeTypes subtype : notifiedPipe.getSubtypes()) {
                    if (updater.getSubtypes().contains(subtype)) {
                        notifiedPipe.addVisualConnection(pDirection, true);
                        updater.addVisualConnection(pDirection.getOpposite(), true);
                        notifiedPipe.getAsBlockEntity().syncBlockEntity();
                        updater.getAsBlockEntity().syncBlockEntity();
                        break;
                    }
                }
            } else if (pLevel.getBlockEntity(pNeighborPos) != null) {
                notifiedPipe.addVisualConnection(pDirection, true);
            } else if (notifiedPipe.getVisualConnectionMap().get(pDirection)) {
                notifiedPipe.removeVisualConnection(pDirection);
            }
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof IPipe pipe) {
            pipe.getManager().detach(pipe);
            pipe.getVisualConnectionMap().forEach((direction, bool) -> {
                if (pLevel.getBlockEntity(pPos.relative(direction)) instanceof IPipe connectedTo) {
                    connectedTo.removeVisualConnection(direction.getOpposite());
                }
            });
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

}
