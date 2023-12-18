package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.item.DispatchItem;
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
 *
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
                        createOrAttachToWithNodeData(network.getTier(), node, false);
                    }
                }
            }
        }

        if (!toRemove.isEmpty()) {
            T network = toRemove.poll();
            this.subNetworks.remove(network);
            network.getHandler(null, null).invalidate();
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
     *
     * @param tier             subnet tier
     * @param chosenPosition   position being added
     * @param updateNeighbours whether or not to update surrounding positions in subnets.
     */

    public void createOrAttachTo(DispatchItem.Tiers tier, BlockPos chosenPosition, boolean updateNeighbours) {

        for (Direction direction : Direction.values()) {

            AbstractSubNetwork subNetwork = getSubnetByPosition(chosenPosition);
            BlockPos neighbourPos = chosenPosition.relative(direction);
            T neighbouringSubnet = getSubnetByPosition(neighbourPos);

            if (neighbouringSubnet == null) continue;

            if (tier == neighbouringSubnet.tier) {

                // ignore this neighbour cable as its closed
                if (neighbouringSubnet.getNodeByPosition(neighbourPos).getDirectionalIO().get(direction.getOpposite()) == LevelNode.IOTypes.NONE)
                    continue;

                // if we have a subnet now we should merge any other connected networks
                if (subNetwork != null) {
                    // make sure the network is not the same one
                    if (subNetwork != neighbouringSubnet) {
                        for (LevelNode node : neighbouringSubnet.getNodes()) {
                            subNetwork.attach(node);
                        }

                        if (!toRemove.contains(neighbouringSubnet)) subNetwork.mergeData(chosenPosition, neighbouringSubnet);
                        markNetworkForDeletion(neighbouringSubnet);
                    }
                } else {
                    neighbouringSubnet.attach(chosenPosition);
                }
            }
        }


        // if we get here create a network
        if (getSubnetByPosition(chosenPosition) == null) {
            createNetwork(tier, chosenPosition);
        }

    }

    /**
     * THIS ONE WORKS WITH NBT DATA AS IT CONNECTS A LEVELNODE (LevelNodes can be already existing)
     * Called when an item either places a block or asks for a position to be added to a sub network.
     * This will attempt to firstly connect to an existing network (merging networks together and removing old ones)
     * If it fails to find a network to connect to it will then create a new subnet at the positon chosen.
     *
     * @param tier             subnet tier
     * @param chosenPosition   position being added
     * @param updateNeighbours whether or not to update surrounding positions in subnets.
     */

    public void createOrAttachToWithNodeData(DispatchItem.Tiers tier, LevelNode chosenPosition, boolean updateNeighbours) {

        for (Direction direction : Direction.values()) {

            AbstractSubNetwork subNetwork = getSubnetByPosition(chosenPosition);
            BlockPos neighbourPos = chosenPosition.getPos().relative(direction);
            T neighbouringSubnet = getSubnetByPosition(neighbourPos);

            if (neighbouringSubnet == null) continue;

            if (tier == neighbouringSubnet.tier) {

                // ignore this neighbour cable as its closed
                if (neighbouringSubnet.getNodeByPosition(neighbourPos).getDirectionalIO().get(direction.getOpposite()) == LevelNode.IOTypes.NONE)
                    continue;

                // if we have a subnet now we should merge any other connected networks
                if (subNetwork != null) {
                    // make sure the network is not the same one
                    if (subNetwork != neighbouringSubnet) {
                        for (LevelNode node : neighbouringSubnet.getNodes()) {
                            subNetwork.attach(node);
                        }

                        if (!toRemove.contains(neighbouringSubnet)) subNetwork.mergeData(chosenPosition.getPos(), neighbouringSubnet);
                        markNetworkForDeletion(neighbouringSubnet);
                    }
                } else {
                    neighbouringSubnet.attach(chosenPosition);
                }
            }
        }

        if (getSubnetByPosition(chosenPosition) == null) {
            createNetwork(tier, chosenPosition.getPos());
        }

    }

    /**
     * Add this position to a neighbour update queue
     *
     * @param chosenPosition position to update around
     */

    private void setUpdateSurroundingFromPosition(BlockPos chosenPosition) {
        updateSurrounding.add(chosenPosition);
    }

    /**
     * Detach the position provided from the network.
     * In reality, this will destroy the current subnet and create n subnets for each position that is orphaned after
     * the position is detached
     *
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

        splitData(pos, modifyingNetwork, newNetworks);

        // remove the subnet
        getSubNetworks().remove(modifyingNetwork);

        // add the new ones
        getSubNetworks().addAll(newNetworks);
    }

    /**
     * Split data between networks
     *
     * @param pos
     * @param modifyingNetwork
     * @param newNetworks      list of newly created networks
     */
    protected abstract void splitData(BlockPos pos, T modifyingNetwork, List<T> newNetworks);

    /**
     * Separate networks uses a DFS to find all positions connected from the starting position (starting node in DFS) and creates a new subnet
     *
     * @param old          the old subnet we are separating from
     * @param startingFrom the position to start DFS from (neighbouring position in the detach function)
     * @return the list of positions connected together.
     */

    public T separateNetworks(T old, BlockPos startingFrom) {
        // create a subnet from starting position
        T separated = createNetworkAndReturn(old.tier, startingFrom);

        for (BlockPos pos : depthFirstSearch(old, startingFrom)) {
            // get tag data from this position (to save serialized data on each level Node)
            CompoundTag tag = old.getNodeByPosition(pos).serialize();

            // old subnet detach
            old.detach(pos);

            // create node
            LevelNode node = new LevelNode(tag);

            // read the tag data for this node as the next attachment function will skip this node
            if (node.getPos().equals(startingFrom)) separated.getNodeByPosition(startingFrom).read(tag);

            // now attach this position to our newly created subnet.
            separated.attach(node);


        }

        return separated;
    }

    public void updateNetwork(LevelNode oldNode, LevelNode newNode) {
        T subNetwork = this.getSubnetByPosition(newNode);

        List<T> newNetworks = new ArrayList<>();

        newNode.getDirectionalIO().forEach((direction, ioTypes) -> {

            // check if the previous iteration of this node has a different io type
            if (oldNode.getDirectionalIO().get(direction) != ioTypes) {

                // if the io type is now none
                if (ioTypes == LevelNode.IOTypes.NONE) {
                    HashSet<BlockPos> existingPositions = depthFirstSearch(subNetwork, newNode.getPos());

                    for (Direction face : Direction.values()) {
                        BlockPos neighbour = newNode.getPos().relative(face);

                        if (subNetwork.contains(neighbour) && !existingPositions.contains(neighbour)) {
                            newNetworks.add(separateNetworks(subNetwork, neighbour));
                        }

                    }

                } else {

                    if (oldNode.getDirectionalIO().get(direction) == LevelNode.IOTypes.NONE) {
                        createOrAttachToWithNodeData(subNetwork.tier, newNode, true);
                    }

                }


            }

        });

        if (!newNetworks.isEmpty()) getSubNetworks().addAll(newNetworks);

    }

    /**
     * A Depth First Search algorithm, checks in every direction from each position (node) whether they are a valid position
     * Valid positions are if they are part of the same subnet
     *
     * @param old          original subnet
     * @param startingNode starting from this position
     * @return a set of all positions
     */

    public HashSet<BlockPos> depthFirstSearch(AbstractSubNetwork old, BlockPos startingNode) {
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

                    // check for io
                    if (old.getNodeByPosition(current).getDirectionalIO().get(direction) == LevelNode.IOTypes.NONE || old.getNodeByPosition(neighbour).getDirectionalIO().get(direction.getOpposite()) == LevelNode.IOTypes.NONE)
                        continue;

                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return visited;
    }

    /**
     * Create a subnet from provided position
     *
     * @param tier
     * @param pos  position to create network at
     */

    public void createNetwork(DispatchItem.Tiers tier, BlockPos pos) {
        T network = createSubnetAtPosition(tier, pos);
        if (!level.isClientSide) network.update();
        getSubNetworks().add(network);
    }

    /**
     * Create a subnet from provided position
     *
     * @param tier
     * @param pos  position to create network at
     * @return returns new network
     */

    public T createNetworkAndReturn(DispatchItem.Tiers tier, BlockPos pos) {
        T network = createSubnetAtPosition(tier, pos);
        //getSubNetworks().add(network);
        return network;
    }

    /**
     * Create subnet at position (this will be created in the actual network classes (energy handler, etc.)
     *
     * @param pos position to create network at
     * @return newly created sub network
     */

    protected abstract T createSubnetAtPosition(DispatchItem.Tiers tier, BlockPos pos);

    /**
     * Get subnet by position
     *
     * @param node levelNode to check
     * @return a subnet or null
     */

    public T getSubnetByPosition(LevelNode node) {
        return getSubnetByPosition(node.getPos());
    }

    /**
     * Get subnet by position
     *
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
     *
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
     *
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
        T network = createSubnetAtPosition(DispatchItem.Tiers.valueOf(tag.getString("tier").toUpperCase()), BlockPos.ZERO);
        network.deserializeNBT(tag);
        if (!level.isClientSide) network.update();
        return network;
    }


}
