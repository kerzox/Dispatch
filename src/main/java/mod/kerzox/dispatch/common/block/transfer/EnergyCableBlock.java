package mod.kerzox.dispatch.common.block.transfer;

import mod.kerzox.dispatch.common.block.multi.MultirolePipeBlock;
import mod.kerzox.dispatch.common.entity.MultiroleEnergyCable;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ENERGY_CABLE;

public class EnergyCableBlock extends MultirolePipeBlock<MultiroleEnergyCable> {

    public EnergyCableBlock(Properties properties) {
        super(ENERGY_CABLE.getType(), properties);
    }

    public static class Item extends BlockItem {
        public Item(Properties pProperties) {
            super(DispatchRegistry.Blocks.ENERGY_CABLE_BLOCK.get(), pProperties);
        }

        @Override
        public InteractionResult useOn(UseOnContext pContext) {

            if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof MultirolePipe clickedOn) {
                if (!clickedOn.getSubtypes().contains(PipeTypes.ENERGY)) {
                    clickedOn.addType(PipeTypes.ENERGY, false);
                    clickedOn.getLevel().updateNeighborsAt(clickedOn.getBlockPos(), clickedOn.getBlockState().getBlock());
                    return InteractionResult.PASS;
                }
            }

            return super.useOn(pContext);
        }
    }
}
