package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.entity.manager.IDispatchCapability;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ItemStackCableStorage extends ItemStackHandler implements IDispatchCapability {

    private Map<MultirolePipe, HashSet<Direction>> insertedFrom = new HashMap<>();

    public Map<MultirolePipe, HashSet<Direction>> getInsertedFrom() {
        return insertedFrom;
    }

    public ItemStackCableStorage(int slot) {
        super(slot);
    }

    public ItemStackCableStorage recreate(int slots) {
        ItemStackCableStorage cs = new ItemStackCableStorage(slots);
        cs.deserializeNBTWithoutSize(this.serialize().getCompound("itemHandler"));
        System.out.println(cs.getSlots() + " : " + slots);
        return cs;
    }

    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, MultirolePipe pipe, Direction direction) {
        if (!insertedFrom.containsKey(pipe)) {
           insertedFrom.put(pipe, new HashSet<>());
        }
        insertedFrom.get(pipe).add(direction);
        return super.insertItem(slot, stack, simulate);
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

    public void deserializeNBTWithoutSize(CompoundTag nbt)
    {
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
        onLoad();
    }

    @Override
    public void merge(IDispatchCapability capability) {
        if (capability instanceof ItemStackCableStorage storage) {
            // handle item
        }
    }

    @Override
    public IDispatchCapability updateFrom(Map<IPipe, Map<PipeTypes, List<IPipe>>> subNetworks) {
        int total = 0;
        for (Map<PipeTypes, List<IPipe>> map : subNetworks.values()) {
            if (map.get(PipeTypes.ITEM) != null) total += 1;
        }
        return recreate(total);
    }

    @Override
    public void clear() {

    }
}
