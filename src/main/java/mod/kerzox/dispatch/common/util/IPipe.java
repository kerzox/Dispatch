package mod.kerzox.dispatch.common.util;


import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.entity.manager.PipeManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.Map;

public interface IPipe {

    HashSet<PipeTypes> getSubtypes();
    PipeManager getManager();
    void setManager(PipeManager pipeManager);
    PipeManager createManager();
    void findCapabilityHolders();
    CompoundTag getNBT();
    void addVisualConnection(Direction direction, boolean entity);
    void removeVisualConnection(Direction direction);
    Map<Direction, Boolean> getVisualConnectionMap();

    MultirolePipe getAsBlockEntity();
}
