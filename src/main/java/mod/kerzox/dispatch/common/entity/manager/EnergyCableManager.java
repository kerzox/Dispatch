package mod.kerzox.dispatch.common.entity.manager;

import mod.kerzox.dispatch.common.capability.energy.EnergyCableStorage;
import mod.kerzox.dispatch.common.entity.EnergyCable;
import mod.kerzox.dispatch.common.entity.manager.basic.IConnectablePipe;
import mod.kerzox.dispatch.common.entity.manager.basic.IPipeCache;
import mod.kerzox.dispatch.common.entity.manager.basic.PipeManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EnergyCableManager extends PipeManager {

    public static class EnergyCache implements IPipeCache {

        private EnergyCableStorage internalCableStorage;
        private LazyOptional<EnergyCableStorage> lazyOptional;
        private Set<EnergyCable> cables = new HashSet<>();

        @Override
        public void mergeCache(IPipeCache cache) {
            if (cache instanceof EnergyCache other) {
                System.out.println("Caches are being merged");
                cables.addAll(other.cablesWithCapabilities());
                this.internalCableStorage.addEnergy(other.getInternalCableStorage().getEnergyStored());
            }
        }

        @Override
        public void addToCache(IConnectablePipe pipe) {
            if (pipe instanceof EnergyCable cable) {
                if (cable.getCache() != null) {
                    if (cable.getAllValidStorages().size() >= 1) {
                        cables.add(cable);
                    }
                }
            }
        }

        @Override
        public void readNBT(CompoundTag tag) {
            this.internalCableStorage.deserializeNBT(tag.get("energy"));
        }

        @Override
        public CompoundTag writeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("energy", this.internalCableStorage.serializeNBT());
            return tag;
        }

        public Set<EnergyCable> cablesWithCapabilities() {
            return cables;
        }

        public EnergyCableStorage getInternalCableStorage() {
            return internalCableStorage;
        }

        public EnergyCableStorage createEnergyStorage(int capacity, int transferredEnergy) {
            this.internalCableStorage = new EnergyCableStorage(capacity * 100, transferredEnergy);
            this.lazyOptional = LazyOptional.of(() -> internalCableStorage);
            return internalCableStorage;
        }

        public EnergyCableStorage createEnergyStorage(int capacity) {
            return createEnergyStorage(capacity, 0);
        }

        public void invalidateCapability() {
            if (this.lazyOptional != null) {
                this.lazyOptional.invalidate();
            }
        }
    }

    private int prevNetworkSize = 1;

    public EnergyCableManager(EnergyCable managerTile) {
        super(managerTile, new EnergyCache());
    }

    @Override
    public void writeNBT(CompoundTag tag) {

    }

    @Override
    public void readNBT(CompoundTag tag) {

    }

    @Override
    public EnergyCache getCache() {
        return (EnergyCache) super.getCache();
    }

    @Override
    public void tick() {
        AtomicInteger currentEnergy = new AtomicInteger(getCache().getInternalCableStorage().getEnergyStored());
        if (currentEnergy.get() > 0) {
            for (EnergyCable cable : getCache().cablesWithCapabilities()) {
                for (LazyOptional<IEnergyStorage> storage : cable.getAllCapabilites()) {
                    storage.ifPresent(cap -> {
                        if (cap.canReceive()) {
                            int received = cap.receiveEnergy(Math.min(currentEnergy.get(), 10 / (getCache().cablesWithCapabilities().size()) / cable.getAllValidStorages().size()), false);
                            currentEnergy.addAndGet(-received);
                            getCache().getInternalCableStorage().useEnergy(received);
                            setDirty();
                        }
                    });
                }
            }
        }


    }

    @Override
    public void onNetworkChange(IPipeCache cache) {

        if (cache instanceof EnergyCache ec) {
            // update the internal energy buffer
            if (this.getNetwork().size() != this.prevNetworkSize) {

                EnergyCableStorage storage = ec.getInternalCableStorage();
                this.getCache().invalidateCapability();

                if (storage != null) {
                    this.getCache().createEnergyStorage(this.getNetwork().size(), storage.getEnergyStored());
                } else {
                    this.getCache().createEnergyStorage(this.getNetwork().size());
                }
            }
        }

    }

    @Override
    protected boolean isConnectable(IConnectablePipe entity) {
        return entity instanceof EnergyCable;
    }

    @Override
    public EnergyCable getManagerTile() {
        return (EnergyCable) super.getManagerTile();
    }

    @Override
    public void updateCache() {
        Iterator<EnergyCable> it = this.getCache().cablesWithCapabilities().iterator();
        while(it.hasNext()) {
            EnergyCable cable = it.next();
            boolean hasCap = false;
            for (Direction direction : Direction.values()) {
                if (cable.fromCache(direction) != null && cable.fromCache(direction).isPresent()) {
                    hasCap = true;
                }
            }

            if (!hasCap) it.remove();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return this.getCache().lazyOptional.cast();
        }
        return LazyOptional.empty();
    }
}
