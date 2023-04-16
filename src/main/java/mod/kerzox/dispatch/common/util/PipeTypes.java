package mod.kerzox.dispatch.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.nio.channels.Pipe;
import java.util.HashSet;

public enum PipeTypes implements StringRepresentable {

    ENERGY(ForgeCapabilities.ENERGY, 0xf8dd72),
    FLUID(ForgeCapabilities.FLUID_HANDLER, 0x5bbcf4),
    ITEM(ForgeCapabilities.ITEM_HANDLER, 0x43e88d);

    Capability<?> cap;
    int tint;

    PipeTypes(Capability<?> capability, int tint) {
        this.cap = capability;
        this.tint = tint;
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

    public static PipeTypes getTypeFromNBT(CompoundTag nbt) {
        return PipeTypes.valueOf(nbt.getAsString());
    }

    public static PipeTypes getTypeFromCapabilityNBT(CompoundTag nbt) {
        String cap = nbt.getAsString();
        for (PipeTypes types : PipeTypes.values()) {
            if (types.getCap().getName().toLowerCase().equals(cap)) {
                return types;
            }
        }
        return null;
    }

    public int getTint() {
        return tint;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
