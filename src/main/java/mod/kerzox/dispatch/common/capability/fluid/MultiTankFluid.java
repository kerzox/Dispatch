package mod.kerzox.dispatch.common.capability.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class MultiTankFluid implements IFluidHandler, INBTSerializable<CompoundTag> {

    private FluidStorageTank[] storageTanks;

    public MultiTankFluid(int tanks, int tankCapacities) {
        this.storageTanks = new FluidStorageTank[tanks];
        for (int i = 0; i < this.storageTanks.length; i++) {
            this.storageTanks[i] = new FluidStorageTank(tankCapacities);
        }
    }

    public CompoundTag write(CompoundTag tag) {
        for (int i = 0; i < storageTanks.length; i++) {
            CompoundTag tag1 = new CompoundTag();
            storageTanks[i].writeToNBT(tag1);
            tag.put("tank" + i, tag1);
        }
        return tag;
    }

    public void setCapacityOfTank(int tank, int amount) {
        this.getStorageTank(tank).setCapacity(amount);
    }

    public void read(CompoundTag tag) {
        for (int i = 0; i < storageTanks.length; i++) {
            storageTanks[i].readFromNBT(tag.getCompound("tank"+i));
        }
    }

    @Override
    public int getTanks() {
        return this.storageTanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.storageTanks[tank].getFluid();
    }

    public FluidStorageTank getStorageTank(int index) {
        return storageTanks[index];
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.storageTanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.storageTanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (action.simulate()) {
            for (int i = 0; i < this.storageTanks.length; i++) {
                if (!getFluidInTank(i).isEmpty()) {
                    if (getFluidInTank(i).isFluidEqual(resource)) {
                        return Math.min(getTankCapacity(i) - getFluidInTank(i).getAmount(), resource.getAmount());
                    }
                }
            }
            for (int i = 0; i < this.getTanks(); i++) {
                if (getFluidInTank(i).isEmpty()) {
                    return Math.min(getTankCapacity(i), resource.getAmount());
                }
            }
            return 0;
        }
        return doFill(resource);
    }

    private int doFill(FluidStack resource) {
        for (int i = 0; i < this.storageTanks.length; i++) {
            if (getFluidInTank(i).isFluidEqual(resource) || getFluidInTank(i).getFluid() == resource.getFluid()) {
                int filled = getTankCapacity(i) - getFluidInTank(i).getAmount();
                if (resource.getAmount() < filled) {
                    getFluidInTank(i).grow(resource.getAmount());
                    filled = resource.getAmount();
                } else {
                    getFluidInTank(i).setAmount((getTankCapacity(i)));
                }
                if (filled > 0) {
                    onContentsChanged();
                    return filled;
                }
            }
            else if (getFluidInTank(i).isEmpty()) {
                this.storageTanks[i].setFluid(new FluidStack(resource, Math.min(getTankCapacity(i), resource.getAmount())));
                onContentsChanged();
                return getFluidInTank(i).getAmount();
            }
        }
        return 0;
    }

    protected void onContentsChanged() {

    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        for (int i = 0; i < this.storageTanks.length; i++) {
            if (getFluidInTank(i).isFluidEqual(resource)) {
                return drain(resource.getAmount(), i, action);
            }
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drain(int maxDrain, int tank, FluidAction action)
    {
        int drained = maxDrain;
        if (getFluidInTank(tank).getAmount() < drained) {
            drained = getFluidInTank(tank).getAmount();
        }
        FluidStack stack = new FluidStack(getFluidInTank(tank), drained);
        if (action.execute() && drained > 0) {
            getFluidInTank(tank).shrink(drained);
            onContentsChanged();
        }
        return stack;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack stack = FluidStack.EMPTY;
        for (int i = 0; i < this.storageTanks.length; i++) {
            int drained = maxDrain;
            if (getFluidInTank(i).getAmount() < drained) {
                drained = getFluidInTank(i).getAmount();
            }
            stack = new FluidStack(getFluidInTank(i), drained);
            if (action.execute() && drained > 0) {
                getFluidInTank(i).shrink(drained);
                onContentsChanged();
                return stack;
            }
            if (!stack.isEmpty()) return stack;

        }
        return stack;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (FluidStorageTank tank : this.storageTanks) {
            CompoundTag tankTag = new CompoundTag();
            list.add(tank.writeToNBT(tankTag));
        }
        nbt.put("tanks", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("tanks")) {
            ListTag list = nbt.getList("tanks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                this.storageTanks[i].readFromNBT(list.getCompound(i));
            }
        }
        onLoad();
    }

    protected void onLoad() {

    }
}
