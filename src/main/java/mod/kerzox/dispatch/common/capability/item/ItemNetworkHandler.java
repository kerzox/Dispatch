package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemNetworkHandler extends AbstractNetwork<ItemSubNetwork> {

    public ItemNetworkHandler(Level level) {
        super(level);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected ItemSubNetwork createSubnetAtPosition(BlockPos pos) {
        return new ItemSubNetwork(this, pos);
    }
}
