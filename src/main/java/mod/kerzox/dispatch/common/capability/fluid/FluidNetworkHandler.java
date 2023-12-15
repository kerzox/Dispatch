package mod.kerzox.dispatch.common.capability.fluid;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.energy.EnergySubNetwork;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
        FluidStack fluid = modifyingNetwork.getTank().getFluid();
        if (fluid.isEmpty()) return;
        for (FluidSubNetwork newNetwork : newNetworks) {
            int received = newNetwork.getTank().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            fluid.shrink(received);
        }
    }

    @Override
    protected FluidSubNetwork createSubnetAtPosition(DispatchItem.Tiers tier, BlockPos pos) {
        return new FluidSubNetwork(this, tier, pos);
    }
}
