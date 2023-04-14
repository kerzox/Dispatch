package mod.kerzox.dispatch.common.block.multi;

import mod.kerzox.dispatch.common.block.BasicEntityBlock;
import mod.kerzox.dispatch.common.util.IPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class MultirolePipeBlock<T extends BlockEntity> extends BasicEntityBlock<T> {

    public MultirolePipeBlock(RegistryObject<BlockEntityType<T>> type, Properties properties) {
        super(type, properties);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        if (!pLevel.isClientSide) {
            if (pLevel.getBlockEntity(pPos) instanceof IPipe notifiedPipe) {
                if (pLevel.getBlockEntity(pFromPos) instanceof IPipe updater) notifiedPipe.getManager().attemptConnectionFrom(notifiedPipe, updater);
                else if (pLevel.getBlockEntity(pFromPos) != null) {
                    notifiedPipe.getManager().onNetworkUpdate();
                }
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return false;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof IPipe pipe) {
            pipe.getManager().detach(pipe);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

}
