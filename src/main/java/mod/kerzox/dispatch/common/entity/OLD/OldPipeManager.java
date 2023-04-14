package mod.kerzox.dispatch.common.entity.OLD;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

public abstract class OldPipeManager {

    private IConnectablePipe managerTile;
    private HashSet<IConnectablePipe> network = new HashSet<>();

    private IPipeCache cache;

    public OldPipeManager(IConnectablePipe entity, IPipeCache cache) {
        this.managerTile = entity;
        this.cache = cache;
        attach(entity);
    }

    private void writeInternalNBT(CompoundTag pTag) {
        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();
        tag2.put("managerPos", NbtUtils.writeBlockPos(managerTile.getPipe().getBlockPos()));
        savePositions(tag2);
        tag2.put("cache", this.getCache().writeNBT());
        tag1.put("internal", tag2);
        writeNBT(tag1);
        pTag.put("managerData", tag1);
    }

    private void savePositions(CompoundTag tag) {
        ListTag list = new ListTag();
        for (IConnectablePipe pipe : this.network) {
            list.add(NbtUtils.writeBlockPos(pipe.getPipe().getBlockPos()));
        }
        tag.put("positions", list);
    }

    private void readPositionsFromTag(CompoundTag tag) {
        if (tag.contains("positions")) {
            this.network.clear();
            ListTag list = tag.getList("positions", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                BlockPos pos = NbtUtils.readBlockPos(list.getCompound(i));
                if (managerTile.getPipe().getLevel().getBlockEntity(pos) instanceof IConnectablePipe pipe) {
                    attach(pipe);
                }
            }
        }
    }

    private void readInternalNBT(CompoundTag tag) {
        CompoundTag tag1 = tag.getCompound("managerData");
        CompoundTag tag2 = tag1.getCompound("internal");

        readPositionsFromTag(tag2);
        if (tag2.contains("cache")) this.cache.readNBT(tag2.getCompound("cache"));

        readNBT(tag1);
    }

    public abstract void writeNBT(CompoundTag tag);

    public abstract void readNBT(CompoundTag tag);

    public IPipeCache getCache() {
        return cache;
    }

    public void setCache(IPipeCache cache) {
        this.cache = cache;
    }

    public abstract void tick();

    public abstract void onNetworkChange(IPipeCache cache);

    public void doNetworkModification(BlockPos pPos, BlockPos pFromPos, IConnectablePipe updating, IConnectablePipe neighbourEntity) {

        System.out.println(pPos + " <-- " + pFromPos);

        if (neighbourEntity != null && isConnectable(neighbourEntity)) {

            if (neighbourEntity.hasManager() && neighbourEntity.getManager() != this && neighbourEntity.getManager().network.size() > 1) {
                System.out.println("Attempting Merge");
                doMerge(neighbourEntity);
            }

            attach(neighbourEntity);
        }

    }

    protected void setDirty() {
        this.managerTile.getPipe().setChanged();
    }

    private void doMerge(IConnectablePipe dispatchEntity) {

        onNetworkChange(getCache());

        getCache().mergeCache(dispatchEntity.getManager().getCache());

        for (IConnectablePipe entity : dispatchEntity.getPipe().getManager().network) {
            entity.setManager(this);
            this.network.add(entity);
        }
    }

    protected abstract boolean isConnectable(IConnectablePipe entity);


    public void attach(IConnectablePipe attachingNode) {
        attachingNode.setManager(this);
        this.network.add(attachingNode);
        getCache().addToCache(attachingNode);
        onNetworkChange(getCache());
    }

    public void detach(IConnectablePipe detachingNode) {

        // remove ourselves from the network
        this.network.remove(detachingNode);

        for (Direction direction : Direction.values()) {
            BlockEntity neighbour = detachingNode.getPipe().getLevel().getBlockEntity(detachingNode.getPipe().getBlockPos().relative(direction));
            if (neighbour instanceof IConnectablePipe neighb && network.contains(neighb)) {
                separateNetworks(neighb);
            }
        }
        onNetworkChange(getCache());
    }

    private HashSet<IConnectablePipe> traverse(IConnectablePipe start) {
        Queue<IConnectablePipe> queue = new LinkedList<>();
        HashSet<IConnectablePipe> visited = new HashSet<>();
        queue.add(start);

        Level level = start.getPipe().getLevel();

        while (!queue.isEmpty()) {
            IConnectablePipe current = queue.poll();
            System.out.print(current.getPipe().getBlockPos().toShortString() + " ");

            for (Direction direction : Direction.values()) {

                BlockEntity neighbour = level.getBlockEntity(current.getPipe().getBlockPos().relative(direction));

                if (neighbour instanceof IConnectablePipe neigh && network.contains(neigh) && !visited.contains(neigh)) {
                    visited.add(neigh);
                    queue.add(neigh);
                }
            }
        }

        return visited;

    }

    private void separateNetworks(IConnectablePipe startFrom) {

        startFrom.getPipe().createManager();



        for (IConnectablePipe entity : traverse(startFrom)) {
            startFrom.getPipe().getManager().attach(entity);
        }

        updateCache();
        startFrom.getManager().onNetworkChange(getCache());

    }

    public IConnectablePipe getManagerTile() {
        return this.managerTile;
    }

    public Set<IConnectablePipe> getNetwork() {
        return network;
    }

//    public abstract void handlePossibleCapability(IConnectablePipe pipe, ICapabilityProvider blockEntity, Direction interactingSide);

    public abstract void updateCache();

    public abstract <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side);

    public void serializeNBT(CompoundTag pTag) {
        writeInternalNBT(pTag);
    }

    public void deserializeNBT(CompoundTag pTag) {
        readInternalNBT(pTag);
    }
}

