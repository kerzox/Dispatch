package mod.kerzox.dispatch.common.entity.manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;

public interface IDispatchCapability {
    IDispatchCapability get();
    CompoundTag serialize();
    void deserialize(CompoundTag tag);
    void merge(IDispatchCapability capability);
}
