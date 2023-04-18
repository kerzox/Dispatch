package mod.kerzox.dispatch.common.entity.manager;

import mod.kerzox.dispatch.common.capability.EnergyCableStorage;
import mod.kerzox.dispatch.common.capability.FluidCableStorage;
import mod.kerzox.dispatch.common.capability.ItemStackCableStorage;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeSettings;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.Subject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PipeManager {

    private static final int ENERGY_CAPACITY = 100;
    private static final int ITEM_SLOT = 1;

    protected IPipe manager;

    protected Queue<IPipe> queue = new LinkedList<>();
    protected HashSet<IPipe> network = new HashSet<>();
    protected Map<IPipe, Map<PipeTypes, List<IPipe>>> subNetworks = new HashMap<>();

    protected HashSet<PipeTypes> subTypes = new HashSet<>();

    protected int[] indexes = new int[PipeTypes.values().length];

    public PipeManager(IPipe pipe) {
        this.manager = pipe;
        this.subTypes.addAll(pipe.getSubtypes());
        attach(pipe);
    }

    public void tick() {
        if (!queue.isEmpty()) {
            IPipe pipe = queue.poll();
            if (pipe != null) {
                pipe.getAsBlockEntity().syncBlockEntity();
                pipe.getAsBlockEntity().getLevel().updateNeighborsAt(pipe.getAsBlockEntity().getBlockPos(), pipe.getAsBlockEntity().getBlockState().getBlock());
                onNetworkUpdate(pipe);
                //     onNetworkUpdate();
            }
        }

        // populateSubnet();

        if (this.subNetworks.size() > 0) {
            for (Map.Entry<IPipe, Map<PipeTypes, List<IPipe>>> mapEntry : this.subNetworks.entrySet()) {
                Map<PipeTypes, List<IPipe>> sub_network = mapEntry.getValue();
                for (PipeTypes subtype : subTypes) {
                    if (sub_network.get(subtype) == null) continue;
                    for (IPipe currentPipe : sub_network.get(subtype)) {
                        currentPipe.getAsBlockEntity().getCachedByCapability(subtype.getCap()).forEach((direction, lazyOptional) -> this.doCapabilityTick(mapEntry.getKey(), currentPipe, subtype, direction, lazyOptional));
                    }
                }
            }
        }
    }

    private void doCapabilityTick(IPipe capabilityProviderPipe, IPipe currentPipe, PipeTypes type, Direction direction, LazyOptional<?>
            lazyOptional) {
        BlockEntity entity = currentPipe.getAsBlockEntity().getLevel().getBlockEntity(currentPipe.getAsBlockEntity().getBlockPos().relative(direction));

        if (entity != null) {
            // ignore pipes and hopper entities while in direction up
            if (entity instanceof MultirolePipe || (entity instanceof HopperBlockEntity && direction == Direction.UP))
                return;
            LazyOptional<?> capability = entity.getCapability(type.getCap(), direction);
            capability.ifPresent(cap -> {
                if (cap instanceof IEnergyStorage energyHandler) doEnergyMethod(capabilityProviderPipe, currentPipe, energyHandler);
                else if (cap instanceof IItemHandler itemHandler) doItemMethod(capabilityProviderPipe, currentPipe, itemHandler);
            });
        }

    }

    private void doItemMethod(IPipe capabilityProviderPipe, IPipe currentPipe, IItemHandler itemHandler) {
        Optional<ItemStack> stack = capabilityProviderPipe.getAsBlockEntity().getItemHandler().map(h -> h.getStackInSlot(0));
        if (stack.isEmpty()) return;
        if (stack.get().isEmpty()) return;
        int itemsPerTick = 1;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemHandler.insertItem(i, capabilityProviderPipe.getAsBlockEntity().getItemHandler().resolve().get().extractItem(0, itemsPerTick, true), true).isEmpty()) {
                itemHandler.insertItem(i, capabilityProviderPipe.getAsBlockEntity().getItemHandler().resolve().get().extractItem(0, itemsPerTick, false), false);
                break;
            }
        }

    }

    private void doEnergyMethod(IPipe capabilityProviderPipe, IPipe currentPipe, IEnergyStorage energy) {
        AtomicInteger currentEnergy = new AtomicInteger(capabilityProviderPipe.getAsBlockEntity().getEnergyHandler().map(EnergyStorage::getEnergyStored).orElse(0));
        if (energy.canReceive() && currentEnergy.get() > 0) {
            int received = energy.receiveEnergy(Math.min(currentEnergy.get(), ENERGY_CAPACITY), false);
            currentEnergy.addAndGet(-received);
            capabilityProviderPipe.getAsBlockEntity().getEnergyHandler().ifPresent(handler -> handler.useEnergy(received));
            currentPipe.getAsBlockEntity().setChanged();
            capabilityProviderPipe.getAsBlockEntity().setChanged();
        }
    }


//                if (indexes[subtype.ordinal()] > subNetworks.get(subtype).size() - 1) indexes[subtype.ordinal()] = 0;
//                if (subNetworks.get(subtype).isEmpty()) continue;
//                IPipe currentPipe = this.subNetworks.get(subtype).get(indexes[subtype.ordinal()]);
    // if this pipe doesn't have this type we loop over
//                    if (!currentPipe.getSubtypes().contains(subtype)) continue;
//
//                    currentPipe.getAsBlockEntity().getCachedByCapability(subtype.getCap()).forEach((direction, lazyOptional) -> {
//                        BlockEntity entity = manager.getAsBlockEntity().getLevel().getBlockEntity(currentPipe.getAsBlockEntity().getBlockPos().relative(direction));
//                        if (entity != null) {
//                            if (entity instanceof MultirolePipe pipe) {
//                                return;
//
//                            } else if (!(currentPipe.getAsBlockEntity().getSetting() == PipeSettings.PUSH || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.BOTH)) {
//                                return;
//                            }
//
//                            entity.getCapability(subtype.getCap(), direction).ifPresent(cap -> {
//                                if (cap instanceof IEnergyStorage energy) {
//                                    doEnergyMethod(currentPipe, energy);
//                                } else if (cap instanceof IItemHandler itemHandler) {
//
//                                }
//                            });
//                        }
//
//                    });
//                    indexes[subtype.ordinal()] += 1;
//                }

//
//    public void pushToSubnetworks() {
//        if (this.subNetworks.size() > 0) {
//            if (index > subNetworks.size() - 1) index = 0;
//            IPipe currentPipe = (IPipe) this.subNetworks.toArray()[index];
//        }
//        index++;
//    }

    /*
          if (this.subNetworks.size() > 0) {
            if (index > subNetworks.size() - 1) index = 0;
            IPipe currentPipe = (IPipe) this.subNetworks.toArray()[index];
            for (PipeTypes subtype : currentPipe.getSubtypes()) {
                currentPipe.getAsBlockEntity().getCachedByCapability(subtype.getCap()).forEach((direction, lazyOptional) -> {
                    BlockEntity entity = manager.getAsBlockEntity().getLevel().getBlockEntity(currentPipe.getAsBlockEntity().getBlockPos().relative(direction));
                    if (entity != null) {
                        if (entity instanceof MultirolePipe pipe) {
//                            if (pipe.getManager() == this) return;
//                            if (!(currentPipe.getAsBlockEntity().getSetting() == PipeSettings.PUSH || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.DEFAULT || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.BOTH)) {
//                                return;
//                            }
                            return;

                        } else if (!(currentPipe.getAsBlockEntity().getSetting() == PipeSettings.PUSH || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.BOTH)) {
                            return;
                        }

                        entity.getCapability(subtype.getCap(), direction).ifPresent(cap -> {
                            if (cap instanceof IEnergyStorage energy) {
                                doEnergyMethod(currentPipe, energy);
                            } else if (cap instanceof IItemHandler itemHandler) {
                                Optional<ItemStack> stack = getItemHandler().map(h -> h.getStackInSlot(0));
                                if (stack.isEmpty()) return;
                                if (stack.get().isEmpty()) return;
                                int itemsPerTick = 1;

                                for (int i = 0; i < itemHandler.getSlots(); i++) {
                                    if (itemHandler instanceof ItemStackCableStorage cable) {
                                        if (cable.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, true), true, currentPipe.getAsBlockEntity(), direction).isEmpty()) {
                                            cable.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, false), false, currentPipe.getAsBlockEntity(), direction);
                                            break;
                                        }
                                    } else {
                                        if (itemHandler.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, true), true).isEmpty()) {
                                            itemHandler.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, false), false);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    }

                });
            }
            index++;
        }
     */


    /* old
            for (IPipe pipe : this.subNetworks) {
            if (pipe instanceof MultirolePipe multirolePipe) {
                for (PipeTypes subType : getSubTypes()) {
                    multirolePipe.getCachedByCapability(subType.getCap()).forEach((direction, lazyOptional) -> {
                        BlockEntity entity = manager.getAsBlockEntity().getLevel().getBlockEntity(multirolePipe.getBlockPos().relative(direction));
                        if (entity != null) {
                            entity.getCapability(subType.getCap(), direction).ifPresent(cap -> {
                                if (cap instanceof IEnergyStorage storage) {
                                    AtomicInteger currentEnergy = new AtomicInteger(getEnergyHandler().map(EnergyStorage::getEnergyStored).orElse(0));
                                    if (storage.canReceive() && currentEnergy.get() > 0) {
                                        int amount = Math.min(currentEnergy.get(), ENERGY_CAPACITY / countSubnetForCapability(subType.getCap()));
                                        int received = storage.receiveEnergy(amount, false);
                                        currentEnergy.addAndGet(-received);
                                        getEnergyHandler().ifPresent(handler -> handler.useEnergy(received));
                                        multirolePipe.setChanged();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }
     */

//    public int countSubnetForCapability(Capability<?> capability) {
//        int count = 0;
//        for (IPipe pipe : this.subNetworks) {
//            if (pipe instanceof MultirolePipe mp) {
//                count += mp.getCachedByCapability(capability).size();
//            }
//        }
//        return count;
//    }

//    public LazyOptional<EnergyCableStorage> getEnergyHandler() {
//        return getCapabilityHandler(ForgeCapabilities.ENERGY).cast();
//    }
//
//    public LazyOptional<FluidCableStorage> getFluidHandler() {
//        return getCapabilityHandler(ForgeCapabilities.FLUID_HANDLER).cast();
//    }
//
//    public LazyOptional<ItemStackCableStorage> getItemHandler() {
//        return getCapabilityHandler(ForgeCapabilities.ITEM_HANDLER).cast();
//    }

    private void populateSubnet(IPipe updateFrom) {
        System.out.println("Repopulating");
        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
            this.subNetworks.clear();
            this.subNetworks = PipeNetworkUtil.traverseReturnAllSubnets2(this);

            for (IPipe pipe : this.subNetworks.keySet()) {
                pipe.getAsBlockEntity().getCapabilitiesInstances().forEach((capability, instance) -> {
                    for (IPipe iPipe : this.network) {
                        if (iPipe == pipe) continue;
                        iPipe.getAsBlockEntity().getCapabilitiesInstances().forEach((capability2, instance2) -> {
                            instance.merge(instance2);
                        });
                    }
                });
            }

//            capabilitiesInstances.forEach((capability, capability2) -> {
//                capabilityMap.get(capability).invalidate();
//                capabilityMap.put(capability, LazyOptional.of(() -> capability2.updateFrom()));
//            });

        }
//        this.network.forEach(pipe -> {
//            pipe.findCapabilityHolders();
//            if (pipe.getAsBlockEntity().hasCachedInventories()) subNetworks.add(pipe);
//        });
    }

//    public void removeInvalidSubnetsWithoutTraversal() {
//        this.subNetworks.removeIf(p -> p.getAsBlockEntity().hasCachedInventories());
//    }

    public void onNetworkUpdate(IPipe updateFrom) {
        System.out.println("Network update");
        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
            populateSubnet(updateFrom);
        }
    }

    private boolean hasSameSubtypes(IPipe pipe, IPipe pipe2) {

        for (PipeTypes subtype : pipe2.getSubtypes()) {
            for (PipeTypes managerTypes : pipe.getSubtypes()) {
                if (subtype == managerTypes)
                    return true;
            }
        }

        return false;

    }

    public void attemptConnectionFrom(IPipe inNetwork, IPipe connectingPipe) {

        if (hasSameSubtypes(inNetwork, connectingPipe)) {
            merge(connectingPipe);
        }


    }

    private void merge(IPipe mergingPipe) {

        PipeManager old = mergingPipe.getManager();

        for (IPipe pipe : mergingPipe.getManager().getNetwork()) {
            attach(pipe);
        }

        dataMerge(old);

        this.getSubTypes().addAll(old.subTypes);

    }

    private void dataMerge(PipeManager oldManager) {
        for (IPipe pipe : this.subNetworks.keySet()) {
            pipe.getAsBlockEntity().getCapabilitiesInstances().forEach((capability, instance) -> {
            System.out.println("Merging " + capability.getName());
                for (IPipe iPipe : oldManager.subNetworks.keySet()) {
                    iPipe.getAsBlockEntity().getCapabilitiesInstances().get(capability).merge(instance);
                }
        });
        }
    }

    private void invalidate() {
        this.network.clear();
//        for (LazyOptional<?> optional : capabilityMap.values()) {
//            if (optional != null) {
//                optional.invalidate();
//            }
//        }
    }

    public void attach(IPipe pipe) {
        this.network.add(pipe);
        pipe.setManager(this);
        this.subTypes.addAll(pipe.getSubtypes());

    }

    public void detach(IPipe toDetach) {
        this.network.remove(toDetach);
        List<PipeManager> managers = new ArrayList<>();

        for (Direction inDirection : Direction.values()) {
            if (toDetach.getAsBlockEntity().getLevel().getBlockEntity(toDetach.getAsBlockEntity().getBlockPos().relative(inDirection)) instanceof MultirolePipe neighbouringPipe && this.network.contains(neighbouringPipe)) {
                managers.add(separateNetworks(neighbouringPipe));
            }
        }

        managers.removeIf(m -> m.getNetwork().size() < 1);

        separateCapabilityCaches(managers);

        invalidate();
    }

    public PipeManager separateNetworks(IPipe startFrom) {
        // create new manager
        PipeManager separatedManager = startFrom.createManager();

        // traverse valid nodes
        for (IPipe pipe : PipeNetworkUtil.traverseAndReturnAllNodes(this.network, startFrom)) {
            this.network.remove(pipe);
            separatedManager.attach(pipe);
        }

//        separatedManager.onNetworkUpdate();
        return separatedManager;
    }

    private void separateCapabilityCaches(List<PipeManager> managers) {
        int networkCount = managers.size();
        for (PipeManager pipeManager : managers) {
            // divide energy and fluids up

//            Optional<Integer> energy = getEnergyHandler().map(EnergyStorage::getEnergyStored);
//            if (energy.isPresent()) {
//                int toReceive = energy.get() / networkCount;
//                pipeManager.getEnergyHandler().ifPresent(handler -> handler.receiveEnergy(toReceive, false));
//                getEnergyHandler().ifPresent(h -> h.useEnergy(toReceive));
//                networkCount--;
//            }


            // just drop items.
        }

    }

    public @NotNull LazyOptional<?> getCapability(MultirolePipe pipe, PipeTypes types) {
        for (Map.Entry<IPipe, Map<PipeTypes, List<IPipe>>> entry : this.subNetworks.entrySet()) {
            Map<PipeTypes, List<IPipe>> map = entry.getValue();
            if (map.get(types) != null) {
                if (map.get(types).contains(pipe)) {
                    return entry.getKey().getAsBlockEntity().getCapabilityHandler(types.getCap());
                }
            }
        }
        return LazyOptional.empty();
    }

    public boolean hasCapability(@NotNull Capability<?> cap) {
        return subTypes.stream().anyMatch(types -> types.getCap() == cap);
    }


    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("managerPos", NbtUtils.writeBlockPos(getTickingTile().getAsBlockEntity().getBlockPos()));
        tag.put("network", savePositions());
        return tag;
    }

    /**
     * Read nbt data
     * Reads the nbt data sent from a multirole pipe. Only the pipe that was the manager previously will run the position code.
     *
     * @param tag
     */

    public void read(CompoundTag tag) {
        readPositionsFromTag(tag);
    }

    private CompoundTag savePositions() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (IPipe pipe : this.network) {
            list.add(NbtUtils.writeBlockPos(pipe.getAsBlockEntity().getBlockPos()));
        }
        tag.put("positions", list);
        return tag;
    }

    private void readPositionsFromTag(CompoundTag tag) {
        BlockEntity possibleManager = getTickingTile().getAsBlockEntity().getLevel().getBlockEntity(NbtUtils.readBlockPos(tag.getCompound("managerPos")));
        if (possibleManager instanceof MultirolePipe oldManager) {
            CompoundTag network = tag.getCompound("network");

            if (network.contains("positions")) {
                this.network.clear();
                ListTag list = network.getList("positions", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    BlockPos pos = NbtUtils.readBlockPos(list.getCompound(i));
                    if (oldManager.getLevel().getBlockEntity(pos) instanceof IPipe pipe) {
                        attach(pipe);
                    }
                }

                onNetworkUpdate(manager);


            }
        }
    }

    public HashSet<IPipe> getNetwork() {
        return network;
    }

    public Map<IPipe, Map<PipeTypes, List<IPipe>>> getSubNetworks() {
        return subNetworks;
    }

    public IPipe getTickingTile() {
        return manager;
    }

    public HashSet<PipeTypes> getSubTypes() {
        return subTypes;
    }

    public void addToUpdateQueue(MultirolePipe pipe) {
        this.queue.add(pipe);
    }


}
