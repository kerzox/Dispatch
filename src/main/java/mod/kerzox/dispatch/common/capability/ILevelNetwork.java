package mod.kerzox.dispatch.common.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AutoRegisterCapability
public interface ILevelNetwork {

    AbstractNetwork<?> getNetworkByCapability(Capability<?> capability);
    List<AbstractSubNetwork> getSubnetsFrom(LevelNode pos);
    Optional<AbstractSubNetwork> getSubnetFromPos(Capability<?> capability, LevelNode pos);
}
