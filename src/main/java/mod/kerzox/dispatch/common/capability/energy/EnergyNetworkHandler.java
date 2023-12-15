package mod.kerzox.dispatch.common.capability.energy;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * This is an energy network build upon the level network system.
 */

public class EnergyNetworkHandler extends AbstractNetwork<EnergySubNetwork> {

    public EnergyNetworkHandler(Level level) {
        super(level);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected void splitData(BlockPos pos, EnergySubNetwork oldNetwork, List<EnergySubNetwork> newNetworks) {
        long energyAmount = oldNetwork.getStorage().getEnergyStored();
        for (EnergySubNetwork subNetwork : newNetworks) {
            long received = subNetwork.getStorage().addEnergyWithReturn(energyAmount);
            energyAmount -= received;
        }
    }

    @Override
    protected EnergySubNetwork createSubnetAtPosition(DispatchItem.Tiers tier, BlockPos pos) {
        return new EnergySubNetwork(this, pos, tier);
    }

}
