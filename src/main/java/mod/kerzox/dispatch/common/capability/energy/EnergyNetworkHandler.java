package mod.kerzox.dispatch.common.capability.energy;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    protected EnergySubNetwork createSubnetAtPosition(BlockPos pos) {
        return new EnergySubNetwork(this, pos);
    }

}
