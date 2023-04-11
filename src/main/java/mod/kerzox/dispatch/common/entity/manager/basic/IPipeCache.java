package mod.kerzox.dispatch.common.entity.manager.basic;

import net.minecraft.nbt.CompoundTag;

import java.awt.*;

public interface IPipeCache {

    void mergeCache(IPipeCache cache);
    void addToCache(IConnectablePipe pipe);
    void readNBT(CompoundTag tag);
    CompoundTag writeNBT();

}
