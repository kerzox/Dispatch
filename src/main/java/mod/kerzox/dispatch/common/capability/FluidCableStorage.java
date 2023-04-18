package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.entity.manager.IDispatchCapability;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Map;

public class FluidCableStorage extends FluidTank implements IDispatchCapability {

    public FluidCableStorage(int capacity) {
        super(capacity);
    }

    @Override
    public IDispatchCapability get() {
        return this;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (tag.contains("fluid_handler")) {

        }
    }

    @Override
    public void merge(IDispatchCapability capability) {
        if (capability instanceof FluidCableStorage storage) {

        }
    }

    @Override
    public IDispatchCapability updateFrom(Map<IPipe, Map<PipeTypes, List<IPipe>>> subNetworks) {
        return null;
    }

    @Override
    public void clear() {

    }
}
