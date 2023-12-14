package mod.kerzox.dispatch.common.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;

public class LevelNode {


    public enum IOTypes implements StringRepresentable {

        DEFAULT("default"),
        PUSH("push"),
        EXTRACT("extract"),
        ALL("all"),
        NONE("none");

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
    }

}
