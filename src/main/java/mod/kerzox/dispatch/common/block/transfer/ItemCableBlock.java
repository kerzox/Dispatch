package mod.kerzox.dispatch.common.block.transfer;

import mod.kerzox.dispatch.common.block.multi.MultirolePipeBlock;
import mod.kerzox.dispatch.common.entity.MultiroleItemCable;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ITEM_CABLE;

public class ItemCableBlock extends MultirolePipeBlock<MultiroleItemCable> {

    public ItemCableBlock(Properties properties) {
        super(ITEM_CABLE.getType(), properties);
    }

    public static class Item extends BlockItem {
        public Item(Properties pProperties) {
            super(DispatchRegistry.Blocks.ITEM_CABLE_BLOCK.get(), pProperties);
        }
        @Override
        public InteractionResult useOn(UseOnContext pContext) {

            if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof MultirolePipe clickedOn) {
                if (!clickedOn.getSubtypes().contains(PipeTypes.ITEM)) {
                    clickedOn.addType(PipeTypes.ITEM, false);
                    clickedOn.getLevel().updateNeighborsAt(clickedOn.getBlockPos(), clickedOn.getBlockState().getBlock());
                    return InteractionResult.PASS;
                }
            }

            return super.useOn(pContext);
        }
    }
}
