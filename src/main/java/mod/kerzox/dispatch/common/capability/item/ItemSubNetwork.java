package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class ItemSubNetwork extends AbstractSubNetwork {

    private ItemStackHandler itemStackHandler = new ItemStackHandler(1);
    private LazyOptional<ItemStackHandler> handlerLazyOptional = LazyOptional.of(() -> itemStackHandler);

    public ItemSubNetwork(AbstractNetwork<?> network, BlockPos pos) {
        super(network, ForgeCapabilities.ITEM_HANDLER);
        nodes.addByPosition(pos);
    }

    @Override
    public void tick() {

    }

    @Override
    public <T> LazyOptional<T> getHandler(Direction side) {
        return handlerLazyOptional.cast();
    }

    @Override
    public void mergeData(CompoundTag serializeNBT) {

    }

    @Override
    public int getRenderingColour() {
        return 0xff1800;
    }
}
