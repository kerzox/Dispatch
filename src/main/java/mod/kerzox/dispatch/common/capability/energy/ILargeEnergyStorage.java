package mod.kerzox.dispatch.common.capability.energy;

public interface ILargeEnergyStorage {

    long getLargeEnergyStored();

    long getLargeMaxEnergyStored();

    long receiveEnergy(long maxReceive, boolean simulate);

    long extractEnergy(long maxExtract, boolean simulate);

}
