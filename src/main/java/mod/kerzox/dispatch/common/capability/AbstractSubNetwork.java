package mod.kerzox.dispatch.common.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashSet;

/**
 * An individual sub network
 */

public abstract class AbstractSubNetwork implements INBTSerializable<CompoundTag> {

    protected AbstractNetwork<?> network;
    protected NodeList nodes = new NodeList();
    protected Capability<?> capability;

    public AbstractSubNetwork(AbstractNetwork<?> network, Capability<?> capability) {
        this.network = network;
        this.capability = capability;
    }

    public abstract void tick();

    public void attach(LevelNode pos) {
        preAttachment(pos.getPos());
        this.nodes.addNode(pos);
        postAttachment(pos.getPos());
    }

    public void attach(BlockPos pos) {
        preAttachment(pos);
        this.nodes.addByPosition(pos);
        postAttachment(pos);
    }

    public void detach(BlockPos pos) {
        preDetachment(pos);
        this.nodes.removeNodeByPosition(pos);
        postDetachment(pos);
    }

    private void preAttachment(BlockPos pos) {

    }

    private void postAttachment(BlockPos pos) {

    }

    private void preDetachment(BlockPos pos) {

    }

    private void postDetachment(BlockPos pos) {
    }

    public Level getLevel() {
        return this.network.getLevel();
    }

    public LevelNode getNodeByPosition(BlockPos pos) {
        return nodes.getByPos(pos);
    }

    public HashSet<LevelNode> getNodes() {
        return nodes.getNodes();
    }

    public boolean contains(LevelNode node) {
        return nodes.hasPosition(node.getPos());
    }

    public boolean contains(BlockPos node) {
        return nodes.hasPosition(node);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        getNodes().forEach(node -> {
            list.add(node.serialize());
        });
        tag.put("nodes", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readPositionsFromTag(tag);
    }

    private void readPositionsFromTag(CompoundTag tag) {
        if (tag.contains("nodes")) {
            ListTag list = tag.getList("nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                LevelNode node = new LevelNode(list.getCompound(i));
                attach(node);
            }
        }
    }

    public Capability<?> getCapability() {
        return capability;
    }

    public abstract int getRenderingColour();

    public abstract <T> LazyOptional<T> getHandler(Direction side);
}
