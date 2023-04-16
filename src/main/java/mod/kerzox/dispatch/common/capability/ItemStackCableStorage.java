package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.entity.manager.IDispatchCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;

public class ItemStackCableStorage extends ItemStackHandler implements IDispatchCapability {

    public ItemStackCableStorage(int slot) {
        super(slot);
    }



    @Override
    public IDispatchCapability get() {
        return this;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("itemHandler", this.serializeNBT());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (tag.contains("itemHandler")) {
            this.deserializeNBT(tag.getCompound("itemHandler"));
        }
    }

    @Override
    public void merge(IDispatchCapability capability) {
        if (capability instanceof ItemStackCableStorage storage) {
            // handle item
        }
    }
}
