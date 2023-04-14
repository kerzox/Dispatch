package mod.kerzox.dispatch.common.util;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.HashSet;

public enum PipeTypes {

    ENERGY(ForgeCapabilities.ENERGY),
    FLUID(ForgeCapabilities.FLUID_HANDLER),
    ITEM(ForgeCapabilities.ITEM_HANDLER);

    Capability<?> cap;

    PipeTypes(Capability<?> capability) {
        this.cap = capability;
    }

    public Capability<?> getCap() {
        return cap;
    }

    public static PipeTypes getTypeFromCapability(Capability<?> cap) {
        for (PipeTypes types : PipeTypes.values()) {
            if (types.getCap() == cap) return types;
        }
        return null;
    }

}
