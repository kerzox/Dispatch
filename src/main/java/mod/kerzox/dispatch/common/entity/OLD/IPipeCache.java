package mod.kerzox.dispatch.common.entity.OLD;

import net.minecraft.nbt.CompoundTag;

public interface IPipeCache {

    void mergeCache(IPipeCache cache);
    void addToCache(IConnectablePipe pipe);
    void readNBT(CompoundTag tag);
    CompoundTag writeNBT();

}
