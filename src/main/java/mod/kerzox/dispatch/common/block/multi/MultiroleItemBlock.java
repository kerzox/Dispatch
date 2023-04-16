package mod.kerzox.dispatch.common.block.multi;

import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class MultiroleItemBlock extends MultirolePipeBlock {

    public MultiroleItemBlock(Properties properties) {
        super(properties, PipeTypes.ITEM);
    }

}
