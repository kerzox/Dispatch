package mod.kerzox.dispatch.common.capability.fluid;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class FluidSubNetwork extends AbstractSubNetwork {

    public FluidSubNetwork(AbstractNetwork<?> network, DispatchItem.Tiers tier, BlockPos pos) {
        super(network, ForgeCapabilities.FLUID_HANDLER, tier, pos);
    }

    @Override
    public void tick() {

    }

    @Override
    protected CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    protected void read(CompoundTag tag) {

    }

    @Override
    public int getRenderingColour() {
        return 0;
    }

    @Override
    public <T> LazyOptional<T> getHandler(Direction side) {
        return null;
    }

    @Override
    public void mergeData(BlockPos chosenPosition, AbstractSubNetwork network) {

    }
}
