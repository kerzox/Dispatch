package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.entity.manager.IDispatchCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.capability.templates.FluidTank;

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
}
