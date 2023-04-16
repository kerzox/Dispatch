package mod.kerzox.dispatch.common.entity.manager;

import com.google.common.io.Files;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PipeManager {

    private static final int ENERGY_CAPACITY = 100;
    private static final int ITEM_SLOT = 1;

    protected IPipe manager;

    protected Queue<IPipe> queue = new LinkedList<>();
    protected HashSet<IPipe> network = new HashSet<>();
    protected HashSet<IPipe> subNetworks = new HashSet<>();

    protected HashSet<PipeTypes> subTypes = new HashSet<>();

    protected Map<Capability<?>, IDispatchCapability> capabilitiesInstances = new HashMap<>() {
        {
            put(ForgeCapabilities.ENERGY, new EnergyCableStorage(ENERGY_CAPACITY));
            put(ForgeCapabilities.ITEM_HANDLER, new ItemStackCableStorage(ITEM_SLOT));
            put(ForgeCapabilities.FLUID_HANDLER, new FluidCableStorage(1000));
        }
    };

    protected Map<Capability<?>, LazyOptional<?>> capabilityMap = new HashMap<>();

    protected int index = 0;

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
                onNetworkUpdate();
            }
        }

        if (this.subNetworks.size() > 0) {
            if (index > subNetworks.size() - 1) index = 0;
            IPipe currentPipe = (IPipe) this.subNetworks.toArray()[index];
            for (PipeTypes subtype : currentPipe.getSubtypes()) {
                currentPipe.getAsBlockEntity().getCachedByCapability(subtype.getCap()).forEach((direction, lazyOptional) -> {
                    BlockEntity entity = manager.getAsBlockEntity().getLevel().getBlockEntity(currentPipe.getAsBlockEntity().getBlockPos().relative(direction));
                    if (entity != null) {
                        if (entity instanceof MultirolePipe pipe) {
                            if (pipe.getManager() == this) return;
                            if (!(currentPipe.getAsBlockEntity().getSetting() == PipeSettings.PUSH || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.DEFAULT || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.BOTH)) {
                                return;
                            }

                        }
                        if (!(currentPipe.getAsBlockEntity().getSetting() == PipeSettings.PUSH || currentPipe.getAsBlockEntity().getSetting() == PipeSettings.BOTH)) {
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
                                    if (itemHandler.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, true), true).isEmpty()) {
                                        itemHandler.insertItem(i, getItemHandler().resolve().get().extractItem(0, itemsPerTick, false), false);
                                        break;
                                    }
                                }
                            }
                        });
                    }

                });
            }
            index++;
        }


    }

    private void doEnergyMethod(IPipe currentPipe, IEnergyStorage energy) {
        AtomicInteger currentEnergy = new AtomicInteger(getEnergyHandler().map(EnergyStorage::getEnergyStored).orElse(0));
        if (energy.canReceive() && currentEnergy.get() > 0) {
            int received = energy.receiveEnergy(Math.min(currentEnergy.get(), ENERGY_CAPACITY), false);
            currentEnergy.addAndGet(-received);
            getEnergyHandler().ifPresent(handler -> handler.useEnergy(received));
            currentPipe.getAsBlockEntity().setChanged();
        }
    }

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

    public int countSubnetForCapability(Capability<?> capability) {
        int count = 0;
        for (IPipe pipe : this.subNetworks) {
            if (pipe instanceof MultirolePipe mp) {
                count += mp.getCachedByCapability(capability).size();
            }
        }
        return count;
    }

    public LazyOptional<EnergyCableStorage> getEnergyHandler() {
        return getCapabilityHandler(ForgeCapabilities.ENERGY).cast();
    }

    public LazyOptional<FluidCableStorage> getFluidHandler() {
        return getCapabilityHandler(ForgeCapabilities.FLUID_HANDLER).cast();
    }

    public LazyOptional<ItemStackCableStorage> getItemHandler() {
        return getCapabilityHandler(ForgeCapabilities.ITEM_HANDLER).cast();
    }

    private void populateSubnet() {
        this.subNetworks.clear();
//        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
//            //this.subNetworks.addAll(PipeNetworkUtil.traverseAndFindValidCapabilityHolders(getNetwork(), getTickingTile().getAsBlockEntity()));
//        }

        this.network.forEach(pipe -> {
            pipe.findCapabilityHolders();
            if (pipe.getAsBlockEntity().hasCachedInventories()) subNetworks.add(pipe);
        });
    }

    public void removeInvalidSubnetsWithoutTraversal() {
        this.subNetworks.removeIf(p -> p.getAsBlockEntity().hasCachedInventories());
    }

    public void onNetworkUpdate() {
        System.out.println("Network update");
        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
            //removeInvalidSubnetsWithoutTraversal();
            populateSubnet();
        }
    }

    private boolean hasSameSubtypes(IPipe pipe) {

        if (pipe.getSubtypes().size() != subTypes.size()) return false;

        int count = subTypes.size();

        for (PipeTypes subtype : pipe.getSubtypes()) {
            for (PipeTypes managerTypes : getSubTypes()) {
                if (subtype == managerTypes) count--;
            }
        }

        return count == 0;

    }

    public void attemptConnectionFrom(IPipe inNetwork, IPipe connectingPipe) {

        if (hasSameSubtypes(connectingPipe)) {
            merge(connectingPipe);
        }

        onNetworkUpdate();
    }

    private void merge(IPipe mergingPipe) {

        PipeManager old = mergingPipe.getManager();

        for (IPipe pipe : mergingPipe.getManager().getNetwork()) {
            attach(pipe);
            pipe.findCapabilityHolders();
        }

        dataMerge(old);

        onNetworkUpdate();

    }

    private void dataMerge(PipeManager oldManager) {
        oldManager.capabilitiesInstances.forEach((capability, instance) -> {
            System.out.println("Merging " + capability.getName());
            capabilitiesInstances.get(capability).merge(instance);
        });
    }

    private void invalidate() {
        this.network.clear();
        for (LazyOptional<?> optional : capabilityMap.values()) {
            if (optional != null) {
                optional.invalidate();
            }
        }
        onNetworkUpdate();
    }

    public void attach(IPipe pipe) {

        this.network.add(pipe);
        pipe.setManager(this);

        onNetworkUpdate();
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
        onNetworkUpdate();
    }

    public PipeManager separateNetworks(IPipe startFrom) {
        // create new manager
        PipeManager separatedManager = startFrom.createManager();

        // traverse valid nodes
        for (IPipe pipe : PipeNetworkUtil.traverseAndReturnAllNodes(this.network, startFrom)) {
            this.network.remove(pipe);
            separatedManager.attach(pipe);
        }

        separatedManager.onNetworkUpdate();
        return separatedManager;
    }

    private void separateCapabilityCaches(List<PipeManager> managers) {
        int networkCount = managers.size();
        for (PipeManager pipeManager : managers) {
            // split energy and fluids up

            Optional<Integer> energy = getEnergyHandler().map(EnergyStorage::getEnergyStored);
            if (energy.isPresent()) {
                int toReceive = energy.get() / networkCount;
                pipeManager.getEnergyHandler().ifPresent(handler -> handler.receiveEnergy(toReceive, false));
                getEnergyHandler().ifPresent(h -> h.useEnergy(toReceive));
                networkCount--;
            }


            // just drop items.
        }

    }

    public @NotNull LazyOptional<?> getCapability(@NotNull Capability<?> cap) {
        boolean match = false;
        for (PipeTypes type : this.subTypes) {
            if (cap == type.getCap()) {
                match = true;
            }
        }
        if (!match) return LazyOptional.empty();
        return getCapabilityHandler(cap);
    }

    public boolean hasCapability(@NotNull Capability<?> cap) {
        return subTypes.stream().anyMatch(types -> types.getCap() == cap);
    }

    public LazyOptional<?> getCapabilityHandler(Capability<?> cap) {
        return capabilityMap.computeIfAbsent(cap, capability -> {
            System.out.println("Computing a new " + capability.getName() + " instance for " + this.manager.getAsBlockEntity().getBlockPos().toShortString());
            return LazyOptional.of(() -> capabilitiesInstances.get(capability));
        });
    }

    private CompoundTag saveCapabilitiesToNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        capabilitiesInstances.forEach((capability, instance) -> {
            CompoundTag tag1 = new CompoundTag();
            tag1.put(capability.getName().toLowerCase(), instance.serialize());
            list.add(tag1);
        });
        tag.put("capabilities", list);
        return tag;
    }

    private void readCapabilitiesFromNBT(CompoundTag tag) {
        ListTag list = tag.getList("capabilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag1 = list.getCompound(i);
            capabilitiesInstances.forEach((capability, instance) -> {
                System.out.println("Deserializing capability: " + capability.getName());
                instance.deserialize(tag1.getCompound(capability.getName().toLowerCase()));
            });
        }
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("managerPos", NbtUtils.writeBlockPos(getTickingTile().getAsBlockEntity().getBlockPos()));
        tag.put("network", savePositions());
        // tag.put("handlers", saveCapabilitiesToNBT());
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
        readCapabilitiesFromNBT(tag.getCompound("handlers"));
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
            }
        }
    }

    public HashSet<IPipe> getNetwork() {
        return network;
    }

    public HashSet<IPipe> getSubNetworks() {
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
