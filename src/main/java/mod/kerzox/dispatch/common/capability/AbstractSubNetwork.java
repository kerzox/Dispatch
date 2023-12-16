package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.item.DispatchItem;
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
    protected DispatchItem.Tiers tier;

    public AbstractSubNetwork(AbstractNetwork<?> network, Capability<?> capability, DispatchItem.Tiers tier, BlockPos pos) {
        this.network = network;
        this.capability = capability;
        this.tier = tier;
        nodes.addByPosition(pos);
    }

    public abstract void tick();


    /**
     * Called when a block is placed/destroyed next to a node, during attach and detach and in the gui config.
     */

    public void update() {

    }

    public void attach(LevelNode pos) {
        preAttachment(pos);
        this.nodes.addNode(pos);
        postAttachment(pos);
        if (!getLevel().isClientSide) update();
    }

    public void attach(BlockPos pos) {
        attach(LevelNode.of(pos));
    }

    public void detach(BlockPos pos) {
        preDetachment(pos);
        this.nodes.removeNodeByPosition(pos);
        postDetachment(pos);
        if (!getLevel().isClientSide) update();
    }

    protected void preAttachment(LevelNode pos) {

    }

    protected void postAttachment(LevelNode pos) {

    }

    protected void preDetachment(BlockPos pos) {

    }

    protected void postDetachment(BlockPos pos) {

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

    protected abstract CompoundTag write();
    protected abstract void read(CompoundTag tag);

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        getNodes().forEach(node -> {
            list.add(node.serialize());
        });
        tag.put("nodes", list);
        tag.putString("tier", this.tier.getSerializedName());
        tag.put("data", write());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readPositionsFromTag(tag);
        this.tier = DispatchItem.Tiers.valueOf(tag.getString("tier").toUpperCase());
        read(tag.getCompound("data"));
    }

    private void readPositionsFromTag(CompoundTag tag) {
        nodes.getNodes().clear();
        if (tag.contains("nodes")) {
            ListTag list = tag.getList("nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                LevelNode node = new LevelNode(list.getCompound(i));
                nodes.addNode(node);
            }
        }
    }

    public DispatchItem.Tiers getTier() {
        return tier;
    }

    public Capability<?> getCapability() {
        return capability;
    }

    public abstract int getRenderingColour();

    public abstract <T> LazyOptional<T> getHandler(Direction side);

    public abstract void mergeData(BlockPos positionBeingMerged, AbstractSubNetwork network);


}
