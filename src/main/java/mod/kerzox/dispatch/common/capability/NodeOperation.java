package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.capability.item.ItemNodeOperation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeOperation implements INBTSerializable<CompoundTag> {

    private static int ID = 0;

    protected int id = ID++;
    private boolean blackList = false;
    private boolean matchNBT = false;
    private int priority = 0;
    private CompoundTag tag;
    private Direction direction;

    public NodeOperation(boolean isBlackList, boolean matchingNBT, int priority, Direction direction) {
        this.blackList = isBlackList;
        this.matchNBT = matchingNBT;
        this.priority = priority;
        this.direction = direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setBlackList(boolean blackList) {
        this.blackList = blackList;
    }

    public void setMatchNBT(boolean matchNBT) {
        this.matchNBT = matchNBT;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }

    public boolean isBlackList() {
        return blackList;
    }

    public boolean shouldMatchNBT() {
        return matchNBT;
    }

    public CompoundTag getNBT() {
        return tag;
    }

    protected abstract List<ResourceLocation> getResources();

    public int getPriority() {
        return this.priority;
    }

    public int getId() {
        return id;
    }
}
