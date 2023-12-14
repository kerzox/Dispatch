package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemNetworkHandler extends AbstractNetwork<ItemSubNetwork> {

    public ItemNetworkHandler(Level level) {
        super(level);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected void splitData(ItemSubNetwork modifyingNetwork, List<ItemSubNetwork> newNetworks) {

    }

    @Override
    protected ItemSubNetwork createSubnetAtPosition(BlockPos pos) {
        return new ItemSubNetwork(this, pos);
    }
}
