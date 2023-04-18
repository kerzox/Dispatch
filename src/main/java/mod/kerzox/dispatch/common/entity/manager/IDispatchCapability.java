package mod.kerzox.dispatch.common.entity.manager;

import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.Map;

public interface IDispatchCapability {
    IDispatchCapability get();
    CompoundTag serialize();
    void deserialize(CompoundTag tag);
    void merge(IDispatchCapability capability);

    IDispatchCapability updateFrom(Map<IPipe, Map<PipeTypes, List<IPipe>>> subNetworks);

    void clear();
}
