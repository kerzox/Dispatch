package mod.kerzox.dispatch.common.capability.fluid;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class FluidNetworkHandler extends AbstractNetwork<FluidSubNetwork> {

    public FluidNetworkHandler(Level level) {
        super(level);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected void splitData(BlockPos pos, FluidSubNetwork modifyingNetwork, List<FluidSubNetwork> newNetworks) {

    }

    @Override
    protected FluidSubNetwork createSubnetAtPosition(DispatchItem.Tiers tier, BlockPos pos) {
        return new FluidSubNetwork(this, tier, pos);
    }
}
