package mod.kerzox.dispatch.common.block;

import mod.kerzox.dispatch.common.entity.BasicBlockEntity;
import mod.kerzox.dispatch.common.util.IClientTickable;
import mod.kerzox.dispatch.common.util.IServerTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BasicEntityBlock<T extends BlockEntity> extends BasicBlock implements net.minecraft.world.level.block.EntityBlock {

    protected RegistryObject<BlockEntityType<T>> type;
    protected boolean shouldTick;

    public BasicEntityBlock(RegistryObject<BlockEntityType<T>> type, Properties properties) {
        super(properties);
        this.type = type;
        shouldTick = true;
    }

    public BasicEntityBlock(RegistryObject<BlockEntityType<T>> type, Properties properties, boolean shouldTick) {
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
