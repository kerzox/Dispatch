package mod.kerzox.dispatch.common.capability.energy;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

/**
 * This is the actual sub network of the energy network handler
 * This is where the code for individual networks go, so moving energy around extracting/pushing etc.
 */

public class EnergySubNetwork extends AbstractSubNetwork {

    private ForgeEnergyStorage storage = new ForgeEnergyStorage(1000);
    private LazyOptional<ForgeEnergyStorage> handler = LazyOptional.of(() -> storage);

    public EnergySubNetwork(EnergyNetworkHandler network, BlockPos pos) {
        super(network, ForgeCapabilities.ENERGY);
        nodes.addByPosition(pos);
    }

    @Override
    public void tick() {

    }

    @Override
    public int getRenderingColour() {
        return 0x00ff61;
    }

    @Override
    public <T> LazyOptional<T> getHandler(Direction side) {
        return handler.cast();
    }
}
