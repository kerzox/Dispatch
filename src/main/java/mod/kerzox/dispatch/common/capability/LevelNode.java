package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.capability.item.ItemNodeOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelNode {


    public enum IOTypes implements StringRepresentable {

        DEFAULT("default"), // normal
        PUSH("push"), // output
        EXTRACT("extract"), // input
        ALL("all"), // combined
        NONE("none"); // no connection

        String name;

        IOTypes(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return getName().toLowerCase();
        }

    }

    public enum Colour implements StringRepresentable {
        DEFAULT,
        RED,
        BLUE,
        PURPLE,
        BLACK,
        YELLOW,
        GREEN;

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }
    }

    private Map<Direction, List<NodeOperation>> operations = new HashMap<>();

    private HashMap<Direction, IOTypes> directionalIO = new HashMap<>(Map.of(
            Direction.NORTH, IOTypes.DEFAULT,
            Direction.SOUTH, IOTypes.DEFAULT,
            Direction.EAST, IOTypes.DEFAULT,
            Direction.WEST, IOTypes.DEFAULT,
            Direction.UP, IOTypes.DEFAULT,
            Direction.DOWN, IOTypes.DEFAULT));

    private BlockPos worldPosition;

    public LevelNode(BlockPos pos) {
        this.worldPosition = pos;
    }

    public LevelNode(CompoundTag tag) {
        read(tag);
    }

    public static LevelNode of(BlockPos chosenPosition) {
        return new LevelNode(chosenPosition);
    }

    public BlockPos getPos() {
        return worldPosition;
    }

    public HashMap<Direction, IOTypes> getDirectionalIO() {
        return directionalIO;
    }

    public Map<Direction, List<NodeOperation>> getOperations() {
        return operations;
    }

    public void addOperation(NodeOperation operation) {
        getOperations().computeIfAbsent(operation.getDirection(), direction -> new ArrayList<>()).add(operation);
    }

    public void removeOperation(NodeOperation operation) {
        getOperations().get(operation.getDirection()).removeIf(operation1 -> operation1.id == operation.id);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        tag.put("position", NbtUtils.writeBlockPos(this.worldPosition));
        directionalIO.forEach((d, t) -> {
            CompoundTag tag1 = new CompoundTag();
            tag1.putString("direction", d.getSerializedName().toLowerCase());
            tag1.putString("type", t.getSerializedName());
            list.add(tag1);
        });
        tag.put("io", list);
        ListTag list2 = new ListTag();
        operations.forEach((d, ops) -> {
            CompoundTag tag1 = new CompoundTag();
            ListTag list3 = new ListTag();
            tag1.putString("direction", d.getSerializedName().toLowerCase());
            for (NodeOperation operation : ops) {
                list3.add(operation.serializeNBT());
            }
            tag1.put("operations", list3);
            list2.add(tag1);
        });
        tag.put("node_operations", list2);
        return tag;
    }

    public void read(CompoundTag tag) {
        ListTag list = tag.getList("io", Tag.TAG_COMPOUND);
        this.worldPosition = NbtUtils.readBlockPos(tag.getCompound("position"));
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag1 = list.getCompound(i);
            Direction direction = Direction.valueOf(tag1.getString("direction").toUpperCase());
            IOTypes type = IOTypes.valueOf(tag1.getString("type").toUpperCase());
            directionalIO.put(direction, type);
        }
        operations.clear();
        ListTag list2 = tag.getList("node_operations", Tag.TAG_COMPOUND);
        for (int i = 0; i < list2.size(); i++) {
            CompoundTag tag1 = list2.getCompound(i);
            Direction direction = Direction.valueOf(tag1.getString("direction").toUpperCase());
            ListTag list3 = tag1.getList("operations", Tag.TAG_COMPOUND);
            for (int j = 0; j < list3.size(); j++) {
                operations.computeIfAbsent(direction, direction1 -> new ArrayList<>()).add(ItemNodeOperation.from(list3.getCompound(j)));
            }
        }
    }

}
