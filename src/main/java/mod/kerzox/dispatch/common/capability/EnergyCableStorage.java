package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.entity.manager.IDispatchCapability;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;

import java.util.List;
import java.util.Map;

public class EnergyCableStorage extends EnergyStorage implements IDispatchCapability {

    public EnergyCableStorage(int capacity) {
        super(capacity);
    }
    public EnergyCableStorage(int capacity, int energy) {
        super(capacity, capacity, 10, energy);
    }

    public void addEnergy(int energyStored) {
        this.energy += Math.min(capacity - energy, energyStored);
    }

    public void useEnergy(int received) {
        if (this.energy == 0) return;
        this.energy -= received;
    }

    @Override
    public IDispatchCapability get() {
        return this;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", this.serializeNBT());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (tag.contains("energy")) {
            this.deserializeNBT(tag.get("energy"));
        }
    }

    @Override
    public void merge(IDispatchCapability capability) {
        if (capability instanceof EnergyCableStorage storage) {
            this.addEnergy(storage.extractEnergy(storage.getEnergyStored(), false));
        }
    }

    @Override
    public IDispatchCapability updateFrom(Map<IPipe, Map<PipeTypes, List<IPipe>>> subNetworks) {
        return null;
    }

    @Override
    public void clear() {
        this.energy = 0;
    }

}
