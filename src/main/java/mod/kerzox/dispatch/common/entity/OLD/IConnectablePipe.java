package mod.kerzox.dispatch.common.entity.OLD;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Collection;
import java.util.Map;

public interface IConnectablePipe<Capability> {

    BasicPipeBlockEntity getPipe();
    Map<Direction, LazyOptional<Capability>> getCache();
    LazyOptional<Capability> fromCache(Direction direction);
    void toCache(Direction direction, LazyOptional<Capability> cap);
    Collection<LazyOptional<Capability>> getAllCapabilites();

    default void setManager(OldPipeManager manager) {
        if (getPipe() != null) {
            getPipe().setManager(manager);
        }
    }

    default boolean hasManager() {
        if (getPipe() != null) {
            return getPipe().hasManager();
        } else return false;
    }

     default OldPipeManager getManager() {
        return getPipe().getManager();
     }

    void checkInventoryAt(BlockEntity blockEntity, Direction direction);
}
