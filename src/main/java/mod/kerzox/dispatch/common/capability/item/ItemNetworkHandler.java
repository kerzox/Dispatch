package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemNetworkHandler extends AbstractNetwork<ItemSubNetwork> {

    public ItemNetworkHandler(Level level) {
        super(level);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected void splitData(BlockPos detachingPos, ItemSubNetwork modifyingNetwork, List<ItemSubNetwork> newNetworks) {

        // just drop items not going to bother splitting between the networks (also doubles to just drop items still in the cable when entire network is removed
        // might change later

        ItemStack stack = modifyingNetwork.getItemStackHandler().extractItem(0, modifyingNetwork.getItemStackHandler().getStackInSlot(0).getCount(), false);
        Block.popResource(getLevel(), detachingPos, stack);

    }

    @Override
    protected ItemSubNetwork createSubnetAtPosition(DispatchItem.Tiers tier, BlockPos pos) {
        return new ItemSubNetwork(this, pos, tier);
    }
}
