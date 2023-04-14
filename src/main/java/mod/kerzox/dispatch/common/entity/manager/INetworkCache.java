package mod.kerzox.dispatch.common.entity.manager;

import net.minecraftforge.common.capabilities.Capability;

public interface INetworkCache<T> {

    void doCapabilityWork();
    T getCapability();

}
