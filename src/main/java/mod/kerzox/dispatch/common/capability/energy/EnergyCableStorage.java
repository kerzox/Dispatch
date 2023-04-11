package mod.kerzox.dispatch.common.capability.energy;

import net.minecraftforge.energy.EnergyStorage;

public class EnergyCableStorage extends EnergyStorage {

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
}
