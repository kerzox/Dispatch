package mod.kerzox.dispatch.common.capability.item;

import mekanism.common.util.NBTUtils;
import mod.kerzox.dispatch.common.capability.NodeOperation;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemNodeOperation extends NodeOperation {

    private List<ResourceLocation> resourceLocations;
    private boolean matchSize;

    public ItemNodeOperation(List<ResourceLocation> resourceLocations, boolean isBlackList, boolean matchingNBT, int priority, Direction direction) {
        super(isBlackList, matchingNBT, priority, direction);
        this.resourceLocations = resourceLocations;
    }

    public void setResourceLocations(List<ResourceLocation> resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    public static ItemNodeOperation from(TagKey<Item> tag, boolean isBlackList, boolean matchingNBT, int priority, Direction direction) {
        List<ResourceLocation> items = new ArrayList<>();
        for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            itemHolder.unwrapKey().ifPresent(key -> items.add(key.location()));
        }
        if (items.isEmpty()) throw new IllegalArgumentException("Tag is not valild");
        return new ItemNodeOperation(items, isBlackList, matchingNBT, priority, direction);
    }

    public static ItemNodeOperation from(ItemStack stack, boolean isBlackList, boolean matchingNBT, int priority, Direction direction) {
        ResourceLocation location = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (location == null) throw new IllegalArgumentException("Item is invalid");
        return new ItemNodeOperation(List.of(location), isBlackList, matchingNBT, priority, direction);
    }

    public static ItemNodeOperation from(CompoundTag tag) {
        ItemNodeOperation nodeOperation = new ItemNodeOperation(new ArrayList<>(), false, false, 0, null);
        nodeOperation.deserializeNBT(tag);
        return nodeOperation;
    }

    public List<ItemStack> getItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (ResourceLocation resource : getResources()) {
            stacks.add(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(resource)), 1));
        }
        return stacks;
    }

    @Override
    protected List<ResourceLocation> getResources() {
        return resourceLocations;
    }

    public void setMatchSize(boolean state) {
        matchSize = state;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id",getId());
        tag.putBoolean("blacklist", isBlackList());
        tag.putBoolean("matchNBT", shouldMatchNBT());
        tag.putBoolean("matchSize", matchSize);
        tag.putInt("priority", getPriority());
        tag.putString("direction", getDirection().getSerializedName());

        ListTag list = new ListTag();
        for (ResourceLocation resourceLocation : resourceLocations) {
            list.add(StringTag.valueOf(resourceLocation.toString()));
        }

        tag.put("resources", list);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.id = nbt.getInt("id");
        setBlackList(nbt.getBoolean("blacklist"));
        setMatchNBT(nbt.getBoolean("matchNBT"));
        setMatchSize(nbt.getBoolean("matchSize"));
        setPriority(nbt.getInt("priority"));
        setDirection(Direction.valueOf(nbt.getString("direction").toUpperCase()));
        ListTag list = nbt.getList("resources", Tag.TAG_STRING);
        List<ResourceLocation> locations = new ArrayList<>();
        for (Tag tag : list) {
            locations.add(new ResourceLocation(tag.getAsString()));
        }
        setResourceLocations(locations);

    }
}
