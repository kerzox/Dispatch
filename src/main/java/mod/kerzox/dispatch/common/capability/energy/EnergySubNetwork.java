package mod.kerzox.dispatch.common.capability.energy;

import mod.kerzox.dispatch.Config;
import mod.kerzox.dispatch.common.capability.*;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the actual sub network of the energy network handler
 * This is where the code for individual networks go, so moving energy around extracting/pushing etc.
 */

public class EnergySubNetwork extends AbstractSubNetwork {

    private ForgeEnergyStorage storage = new ForgeEnergyStorage(1000);
    private LazyOptional<ForgeEnergyStorage> handler = LazyOptional.of(() -> storage);

    // Nodes that have inventories around them
    private HashSet<LevelNode> nodesWithInventories = new HashSet<>();
    private HashSet<LevelNode> nodesWithExtraction = new HashSet<>();
    private HashSet<LevelNode> nodesWithInsertion = new HashSet<>();

    public EnergySubNetwork(EnergyNetworkHandler network, BlockPos pos, DispatchItem.Tiers tier) {
        super(network, ForgeCapabilities.ENERGY, tier, pos);
    }

    @Override
    public void tick() {
        AtomicInteger current = new AtomicInteger(this.storage.getEnergyStored());
        // try to extract first
        if (current.get() < this.storage.getMaxEnergyStored())
            tryExtraction();

        if (current.get() <= 0) return;

        Set<IEnergyStorage> consumers = getAvailableConsumers();
        if (consumers.size() == 0) return;

        for (IEnergyStorage consumer : consumers) {
            long amount = Math.min(current.get(), Config.getEnergyTransfer(getTier()) / consumers.size());
            if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
            int received = consumer.receiveEnergy((int) amount, false);
            storage.consumeEnergy(received);
            current.set(storage.getEnergyStored());
        }

    }

    @Override
    public void update() {
        findInventories();
    }

    /**
     * Method loops through all the cables that are on extraction mode and checks if the direction is set to a extract
     * If so then it will attempt to extract energy from this direction.
     */

    public void tryExtraction() {
        for (LevelNode node : this.nodesWithExtraction) {
            for (Direction direction : Direction.values()) {
                if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT || node.getDirectionalIO().get(direction) == LevelNode.IOTypes.ALL) {
                    BlockPos pos = node.getPos().relative(direction);
                    BlockEntity blockEntity = getLevel().getBlockEntity(pos);
                    if (blockEntity != null) {
                        LazyOptional<IEnergyStorage> energyCapability = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
                        energyCapability.ifPresent(cap -> {
                            if (cap.canExtract() && cap.getEnergyStored() != 0) {
                                long amount = Config.getEnergyTransfer(getTier());
                                if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
                                int simulated = cap.extractEnergy((int) amount, true);
                                int toExtract = this.storage.receiveEnergy(simulated, true);
                                this.storage.receiveEnergy(cap.extractEnergy(toExtract, false), false);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Finds any valid inventory (energy capability) and adds the node next to it to nodesWithInventories set
     */

    private void findInventories() {

        nodesWithExtraction.clear();
        nodesWithInsertion.clear();
        nodesWithInventories.clear();

        for (LevelNode node : getNodes()) {

            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = node.getPos().relative(direction);
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);

                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                        nodesWithInventories.add(node);
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ENERGY, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isPresent() && subNetwork.get() != this) nodesWithInventories.add(node);
                });

            }

            for (LevelNode.IOTypes type : node.getDirectionalIO().values()) {
                if ((type == LevelNode.IOTypes.EXTRACT || type == LevelNode.IOTypes.ALL) && nodesWithInventories.contains(node))
                    nodesWithExtraction.add(node);
                if ((type == LevelNode.IOTypes.PUSH || type == LevelNode.IOTypes.ALL) && nodesWithInventories.contains(node))
                    nodesWithInsertion.add(node);
                if (type == LevelNode.IOTypes.NONE) nodesWithInventories.remove(node);
            }

        }
    }

    public Set<IEnergyStorage> getAvailableConsumers() {
        Set<IEnergyStorage> consumers = new HashSet<>();
        for (LevelNode node : nodesWithInventories) {
            BlockPos position = node.getPos();
            for (Direction direction : Direction.values()) {
                if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT) continue;
                BlockPos neighbourPos = position.relative(direction);

                // check for block entities
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);
                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                        if (handler.canReceive() && handler.getEnergyStored() < handler.getMaxEnergyStored())
                            consumers.add(handler);
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ENERGY, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isEmpty()) return;
                    AbstractSubNetwork subNet = subNetwork.get();
                    if (subNet != this && subNet instanceof EnergySubNetwork subNetwork1) {
                        if (subNetwork1.getStorage().getEnergyStored() < subNetwork1.getStorage().getMaxEnergyStored())
                            consumers.add(subNetwork1.getStorage());
                    }
                });


            }
        }
        return consumers;
    }

    @Override
    protected void postAttachment(LevelNode pos) {
        // change capacity based on number of cables
        storage.capacity = nodes.size() * Config.getEnergyCapacity(getTier());
        storage.maxExtract = storage.capacity;
        storage.maxReceive = storage.capacity;
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", this.storage.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundTag tag) {
        this.storage.read(tag);
    }

    @Override
    public int getRenderingColour() {
        return 0x00ff61;
    }

    @Override
    public <T> LazyOptional<T> getHandler(BlockPos worldPosition, Direction side) {

        // for invalidation
        if (worldPosition == null) return handler.cast();

        if (getNodeByPosition(worldPosition).getDirectionalIO().get(side) != LevelNode.IOTypes.NONE) return handler.cast();
        else return LazyOptional.empty();
    }

    public ForgeEnergyStorage getStorage() {
        return storage;
    }

    @Override
    public void mergeData(BlockPos chosenPosition, AbstractSubNetwork network) {
        if (network instanceof EnergySubNetwork subNetwork) {
            this.storage.addEnergy(subNetwork.storage.energy);
        }
    }

}
