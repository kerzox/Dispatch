package mod.kerzox.dispatch.common.capability.energy;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
    protected void postAttachment(BlockPos pos) {
        // change capacity based on number of cables
        storage.capacity = nodes.size() * 1000L;
    }

    @Override
    protected CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", this.storage.serializeNBT());
        return tag;
    }

    @Override
    protected void read(CompoundTag tag) {
        this.storage.read(tag);
    }

    @Override
    public int getRenderingColour() {
        return 0x00ff61;
    }

    @Override
    public <T> LazyOptional<T> getHandler(Direction side) {
        return handler.cast();
    }

    public ForgeEnergyStorage getStorage() {
        return storage;
    }

    @Override
    public void mergeData(AbstractSubNetwork network) {
        if (network instanceof EnergySubNetwork subNetwork) {
            this.storage.addEnergy(subNetwork.storage.energy);
        }
    }
}
