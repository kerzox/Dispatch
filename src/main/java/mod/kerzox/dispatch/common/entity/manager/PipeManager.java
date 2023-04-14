package mod.kerzox.dispatch.common.entity.manager;

import cpw.mods.util.Lazy;
import mod.kerzox.dispatch.common.capability.energy.EnergyCableStorage;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PipeManager {

    private static final int ENERGY_CAPACITY = 100;
    private static final int ITEM_SLOT = 1;

    protected IPipe manager;
    protected HashSet<IPipe> network = new HashSet<>();
    protected HashSet<IPipe> subNetworks = new HashSet<>();

    protected HashSet<PipeTypes> subTypes = new HashSet<>();

    private EnergyCableStorage energyStorage = new EnergyCableStorage(ENERGY_CAPACITY);
    private ItemStackHandler itemStackHandler = new ItemStackHandler(ITEM_SLOT);

    protected Map<Capability<?>, LazyOptional<?>> capabilityMap = new HashMap<>() {{
        put(ForgeCapabilities.ENERGY, LazyOptional.of(() -> energyStorage));
        put(ForgeCapabilities.ITEM_HANDLER, LazyOptional.of(() -> itemStackHandler));
    }};

    public PipeManager(IPipe pipe) {
        this.manager = pipe;
        this.subTypes.addAll(pipe.getSubtypes());
        attach(pipe);
    }

    public void tick() {
        for (IPipe pipe : this.subNetworks) {
            if (pipe instanceof MultirolePipe multirolePipe) {
                for (PipeTypes subType : getSubTypes()) {
                    multirolePipe.getCachedByCapability(subType.getCap()).forEach((direction, lazyOptional) -> {
                        BlockEntity entity = manager.getAsBlockEntity().getLevel().getBlockEntity(multirolePipe.getBlockPos().relative(direction));
                        if (entity != null) {
                            entity.getCapability(subType.getCap(), direction).ifPresent(cap -> {
                                if (cap instanceof IEnergyStorage storage) {
                                    AtomicInteger currentEnergy = new AtomicInteger(getEnergyHandler().getEnergyStored());
                                    if (storage.canReceive() && currentEnergy.get() > 0) {
                                        int amount = Math.min(currentEnergy.get(), ENERGY_CAPACITY / countSubnetForCapability(subType.getCap()));
                                        int received = storage.receiveEnergy(amount, false);
                                        currentEnergy.addAndGet(-received);
                                        getEnergyHandler().useEnergy(received);
                                        multirolePipe.setChanged();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }

    }

    public int countSubnetForCapability(Capability<?> capability) {
        int count = 0;
        for (IPipe pipe : this.subNetworks) {
            if (pipe instanceof MultirolePipe mp) {
                count += mp.getCachedByCapability(capability).size();
            }
        }
        return count;
    }

    public EnergyCableStorage getEnergyHandler() {
        return (EnergyCableStorage) capabilityMap.get(ForgeCapabilities.ENERGY).resolve().get();
    }

    private void populateSubnet() {
        this.subNetworks.clear();
        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
            this.subNetworks.addAll(PipeNetworkUtil.traverseAndFindValidCapabilityHolders(getNetwork(), getTickingTile().getAsBlockEntity()));
        }
    }

    public void onNetworkUpdate() {
        if (getTickingTile().getAsBlockEntity().getLevel() != null) {
            System.out.println("Populating subnet");
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

        populateSubnet();
    }

    private void merge(IPipe mergingPipe) {

        PipeManager old = mergingPipe.getManager();

        for (IPipe pipe : mergingPipe.getManager().getNetwork()) {
            attach(pipe);
        }

        old.invalidate();

    }

    private void invalidate() {
        System.out.println("Invalidating manager");
        this.network.clear();
        for (LazyOptional<?> optional : capabilityMap.values()) {
            if (optional != null) {
                optional.invalidate();
            }
        }
    }

    public void attach(IPipe pipe) {
        this.network.add(pipe);
        pipe.setManager(this);

        populateSubnet();
    }

    public void detach(IPipe toDetach) {
        this.network.remove(toDetach);
        for (Direction inDirection : Direction.values()) {
            if (toDetach.getAsBlockEntity().getLevel().getBlockEntity(toDetach.getAsBlockEntity().getBlockPos().relative(inDirection)) instanceof MultirolePipe neighbouringPipe && this.network.contains(neighbouringPipe)) {
                separateNetworks(neighbouringPipe);
            }
        }
        invalidate();
    }

    public void separateNetworks(IPipe startFrom) {
        // create new manager
        PipeManager separatedManager = startFrom.createManager();

        // traverse valid nodes
        for (IPipe pipe : PipeNetworkUtil.traverseAndReturnAllNodes(this.network, startFrom)) {
            this.network.remove(pipe);
            separatedManager.attach(pipe);
        }
        separatedManager.onNetworkUpdate();
    }

    public @NotNull LazyOptional<?> getCapability(@NotNull Capability<?> cap) {
        return capabilityMap.get(cap);
    }

    public boolean hasCapability(@NotNull Capability<?> cap) {
        return capabilityMap.get(cap) != null && capabilityMap.get(cap).isPresent();
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

}
