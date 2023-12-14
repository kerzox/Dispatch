package mod.kerzox.dispatch.common.capability.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

// TODO Convert energy storage to longs, allow for transfer of long when using my capabilities while passing a safe int to normal energy storages.

public class ForgeEnergyStorage implements IEnergyStorage, INBTSerializable<Tag>, ILargeEnergyStorage
{
    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;

    public ForgeEnergyStorage(long capacity)
    {
        this(capacity, capacity, capacity, 0);
    }

    public ForgeEnergyStorage(long capacity, long maxTransfer)
    {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public ForgeEnergyStorage(long capacity, long maxReceive, long maxExtract)
    {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public ForgeEnergyStorage(long capacity, long maxReceive, long maxExtract, long energy)
    {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0 , Math.min(capacity, energy));
    }

    // allow longs

    @Override
    public long getLargeEnergyStored() {
        return energy;
    }

    @Override
    public long getLargeMaxEnergyStored() {
        return capacity;
    }

    public long receiveEnergy(long maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            onContentsChanged();
        }
        return energyReceived;
    }

    public long extractEnergy(long maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            onContentsChanged();
        }
        return  energyExtracted;
    }

    // these are called through the capability IEnergyStorage we don't really care as they shouldn't return a value larger than an integer

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            onContentsChanged();
        }
        return (int) energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            onContentsChanged();
        }
        return (int) energyExtracted;
    }

    protected void onContentsChanged() {

    }

    @Override
    public int getEnergyStored()
    {
        if (energy > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        if (capacity > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) capacity;
    }

    @Override
    public boolean canExtract()
    {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive()
    {
        return this.maxReceive > 0;
    }

    @Override
    public Tag serializeNBT()
    {
        return LongTag.valueOf(this.getLargeEnergyStored());
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        if (!(nbt instanceof LongTag intNbt))
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        this.energy = intNbt.getAsLong();
    }

    public void addEnergy(long amount) {
        this.energy += Math.min(capacity - energy, amount);
    }

    public long addEnergyWithReturn(long amount) {
        long energyReceived = Math.min(capacity - energy, amount);
        addEnergy(energyReceived);
        return energyReceived;
    }

    public void consumeEnergy(long amount) {
        energy -= Math.min(amount, energy);
    }

    public void addEnergy(int amount) {
        this.energy += Math.min(capacity - energy, amount);
    }

    public void consumeEnergy(int amount) {
        energy -= Math.min(amount, energy);
    }

    public void setEnergy(long energy) {
        this.energy = Math.min(energy, capacity);
    }

    public void read(CompoundTag tag) {
        this.energy = tag.getLong("energy");
    }


    public void setExtractLimit(int transfer) {
        this.maxExtract = transfer;
    }

    public void setInsertLimit(int transfer) {
        this.maxReceive = transfer;
    }
}
