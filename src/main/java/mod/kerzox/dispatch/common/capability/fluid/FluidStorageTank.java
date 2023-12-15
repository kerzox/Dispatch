package mod.kerzox.dispatch.common.capability.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class FluidStorageTank extends FluidTank {

    public FluidStorageTank(int capacity) {
        super(capacity);
    }

    public FluidStorageTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
    }

    public boolean isFull() {
        return this.getFluidAmount() == this.capacity;
    }

    @Override
    public FluidTank setCapacity(int capacity) {
        return super.setCapacity(capacity);
    }


}
