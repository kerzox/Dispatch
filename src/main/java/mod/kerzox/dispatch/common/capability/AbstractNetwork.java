package mod.kerzox.dispatch.common.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper of all sub networks to form a big encompassing object
 * You can find subnets by block position through this class
 * @param <T> The Subnet class this network is working on.
 */

public abstract class AbstractNetwork<T extends AbstractSubNetwork> implements INBTSerializable<CompoundTag> {

    private Level level;
    private HashSet<T> subNetworks = new HashSet<>();
    private Queue<T> toRemove = new LinkedList<>();
    private Queue<BlockPos> updateSurrounding = new LinkedList<>();

    public AbstractNetwork(Level level) {
        this.level = level;
    }

    /**
     * Called every tick (don't override this one use the other abstract tick unless you have too)
     */

    public void tickManager() {

        if (!updateSurrounding.isEmpty()) {
            BlockPos position = updateSurrounding.poll();

            for (Direction direction : Direction.values()) {
                T network = getSubnetByPosition(position.relative(direction));
                if (network != null && getSubnetByPosition(position) != network) {
                    // cause update in this network.
                    for (LevelNode node : network.getNodes()) {
                        createOrAttachTo(node.getPos(), false);
                    }
                }
            }
        }

        if (!toRemove.isEmpty()) {
            T network = toRemove.poll();
            this.subNetworks.remove(network);
        }

        tick();

        // once the handler has reached end of the tick call every subnetwork tick

        for (T subNetwork : subNetworks) {
            subNetwork.tick();
        }

    }

    protected abstract void tick();

    /**
     * Called when an item either places a block or asks for a position to be added to a sub network.
     * This will attempt to firstly connect to an existing network (merging networks together and removing old ones)
     * If it fails to find a network to connect to it will then create a new subnet at the positon chosen.
     * @param chosenPosition position being added
     * @param updateNeighbours whether or not to update surrounding positions in subnets.
     */

    public void createOrAttachTo(BlockPos chosenPosition, boolean updateNeighbours) {

        // Loop through all the individual networks
        for (T individualNetwork : getSubNetworks()) {

            for (Direction direction : Direction.values()) {

                BlockPos neighbourPosition = chosenPosition.relative(direction);

                if (individualNetwork.contains(neighbourPosition)) {
                    // we found a neighbouring position that is in an existing network we should connect to it.

                    // check if we already have a network because we might have to merge.
                    for (T network : getSubnetsByPosition(chosenPosition)) {
                        if (network != individualNetwork) {
                            //TODO this is where data should be transferred into next one

                            for (LevelNode node : network.getNodes()) {
                                individualNetwork.attach(node);
                            }

                            markNetworkForDeletion(network);
                        }
                    }

                    individualNetwork.attach(chosenPosition);
                     if (updateNeighbours) setUpdateSurroundingFromPosition(chosenPosition);
                     // updatingNetworkClient.add(level.getChunkAt(chosenPosition));
                    return;

                }

            }
        }

        // if we get here create a network
        if (getSubnetByPosition(chosenPosition) == null) {
            createNetwork(chosenPosition);
        }

    }

    /**
     * Add this position to a neighbour update queue
     * @param chosenPosition position to update around
     */

    private void setUpdateSurroundingFromPosition(BlockPos chosenPosition) {
        updateSurrounding.add(chosenPosition);
    }

    /**
     * Detach the position provided from the network.
     * In reality, this will destroy the current subnet and create n subnets for each position that is orphaned after
     * the position is detached
     * @param pos position being detached.
     */

    public void detachAt(BlockPos pos) {
        // subnet the position is located in
        T modifyingNetwork = getSubnetByPosition(pos);

        // remove us from it
        modifyingNetwork.detach(pos);

        // list of new subnets that will be created

        List<T> newNetworks = new ArrayList<>();

        // in every direction
        for (Direction direction : Direction.values()) {
            // get the block position of a neighbour then check whether our subnet contains this neighbour
            BlockPos neighbour = pos.relative(direction);
            if (modifyingNetwork.contains(neighbour)) {
                // add to the new subnet list from the return of separateNetworks function
                newNetworks.add(separateNetworks(modifyingNetwork, neighbour));
            }
        }
        // remove the subnet
        getSubNetworks().remove(modifyingNetwork);

        // add the new ones
        getSubNetworks().addAll(newNetworks);
    }

    /**
     * Separate networks uses a DFS to find all positions connected from the starting position (starting node in DFS) and creates a new subnet
     * @param old the old subnet we are separating from
     * @param startingFrom the position to start DFS from (neighbouring position in the detach function)
     * @return the list of positions connected together.
     */

    public T separateNetworks(T old, BlockPos startingFrom) {
        // create a subnet from starting position
        T separated = createSubnetAtPosition(startingFrom);

        for (BlockPos pos : DepthFirstSearch(old, startingFrom)) {
            // get tag data from this position (to save serialized data on each level Node)
            CompoundTag tag = old.getNodeByPosition(pos).serialize();

            // old subnet detach (mostly a precaution)
            old.detach(pos);

            // now attach this position to our newly created subnet.
            separated.attach(new LevelNode(tag));
        }

        return separated;
    }

    /**
     * A Depth First Search algorithm, checks in every direction from each position (node) whether they are a valid position
     * Valid positions are if they are part of the same subnet
     *
     * @param old original subnet
     * @param startingNode starting from this position
     * @return a set of all positions
     */

    public HashSet<BlockPos> DepthFirstSearch(T old, BlockPos startingNode) {
        Queue<BlockPos> queue = new LinkedList<>();
        HashSet<BlockPos> visited = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            BlockPos current = queue.poll();

            for (Direction direction : Direction.values()) {
                BlockPos neighbour = current.relative(direction);
                T network = getSubnetByPosition(neighbour);
                if (network != null && !visited.contains(neighbour)
                        && old == network) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return visited;
    }

    /**
     * Create a subnet from provided position
     * @param pos position to create network at
     */

    public void createNetwork(BlockPos pos) {
        T network = createSubnetAtPosition(pos);
        getSubNetworks().add(network);
    }

    /**
     * Create subnet at position (this will be created in the actual network classes (energy handler, etc.)
     * @param pos position to create network at
     * @return newly created sub network
     */

    protected abstract T createSubnetAtPosition(BlockPos pos);

    /**
     * Get subnet by position
     * @param node levelNode to check
     * @return a subnet or null
     */

    public T getSubnetByPosition(LevelNode node) {
        return getSubnetByPosition(node.getPos());
    }

    /**
     * Get subnet by position
     * @param node position to check
     * @return a subnet or null
     */

    public T getSubnetByPosition(BlockPos node) {
        for (T network : subNetworks) {
            if (network.contains(node)) return network;
        }
        return null;
    }

    /**
     * Set of subnets at this position (this is from the position being merged)
     * @param pos position to check
     * @return set of all subnets or null
     */

    public Set<T> getSubnetsByPosition(BlockPos pos) {
        return subNetworks.stream().filter(n -> n.contains(pos)).collect(Collectors.toSet());
    }

    public Level getLevel() {
        return level;
    }

    public HashSet<T> getSubNetworks() {
        return subNetworks;
    }

    /**
     * Mark network for deletion at a later tick
     * @param network network to delete
     */

    public void markNetworkForDeletion(T network) {
        this.toRemove.add(network);
    }

    public boolean isInASubnet(BlockPos pos) {
        return getSubnetByPosition(LevelNode.of(pos)) != null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        if (getSubNetworks() != null) {
            getSubNetworks().forEach((net -> {
                list.add(net.serializeNBT());
            }));
            tag.put("networks", list);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getSubNetworks().clear();
        if (nbt.contains("networks")) {
            ListTag list = nbt.getList("networks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                this.getSubNetworks().add(createNetworkFromTag(tag));
            }
        }
    }

    protected T createNetworkFromTag(CompoundTag tag) {
        T network = createSubnetAtPosition(BlockPos.ZERO);
        network.deserializeNBT(tag);
        return network;
    }

}
