package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
    TODO
        - Upgrades (More slots?)
        - Different distribution (round robin, until full etc.)
 */

public class ItemSubNetwork extends AbstractSubNetwork {

    private ItemStackHandler itemStackHandler = new ItemStackHandler(1);
    private LazyOptional<ItemStackHandler> handlerLazyOptional = LazyOptional.of(() -> itemStackHandler);

    // Nodes that have inventories around them
    private HashSet<LevelNode> nodesWithInventories = new HashSet<>();
    private HashSet<LevelNode> nodesWithExtraction = new HashSet<>();
    private HashSet<LevelNode> nodesWithInsertion = new HashSet<>();

    public ItemSubNetwork(AbstractNetwork<?> network, BlockPos pos, DispatchItem.Tiers tier) {
        super(network, ForgeCapabilities.ITEM_HANDLER, tier, pos);
    }

    @Override
    public void tick() {
        ItemStack inventoryStack = itemStackHandler.getStackInSlot(0).copy();

        // try to extract first
        if (inventoryStack.getCount() < itemStackHandler.getSlotLimit(0))
            tryExtraction();

        if (inventoryStack.isEmpty()) return;

        List<IItemHandler> consumers = getAvailableConsumers();
        if (consumers.size() == 0) return;

        for (IItemHandler consumer : consumers) {

            // replace this with tiered insert amounts
            int maxInsert = Math.min(8, inventoryStack.getCount());

            for (int i = 0; i < consumer.getSlots(); i++) {
                ItemStack insertStack =  itemStackHandler.extractItem(0, maxInsert, true);
                ItemStack ret = consumer.insertItem(i, insertStack, false);
                itemStackHandler.extractItem(0, maxInsert, false);
                if (ret.isEmpty()) return;
                itemStackHandler.insertItem(0, ret, false);

            }

        }

    }

    @Override
    public void update() {
        findInventories();
    }


    /**
     * Finds any valid inventory (item capability) and adds the node next to it to nodesWithInventories set
     */

    private void findInventories() {

        nodesWithExtraction.clear();
        nodesWithInsertion.clear();
        nodesWithInventories.clear();

        for (LevelNode node : getNodes()) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = node.getPos().relative(direction);
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);

                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                        nodesWithInventories.add(node);
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ITEM_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isPresent() && subNetwork.get() != this) nodesWithInventories.add(node);
                });

                for (LevelNode.IOTypes type : node.getDirectionalIO().values()) {
                    if ((type == LevelNode.IOTypes.EXTRACT || type == LevelNode.IOTypes.ALL) && nodesWithInventories.contains(node))
                        nodesWithExtraction.add(node);
                    if ((type == LevelNode.IOTypes.PUSH || type == LevelNode.IOTypes.ALL) && nodesWithInventories.contains(node))
                        nodesWithInsertion.add(node);
                    if (type == LevelNode.IOTypes.NONE) nodesWithInventories.remove(node);
                }

            }
        }
    }

    public void tryExtraction() {
        for (LevelNode node : this.nodesWithExtraction) {
            for (Direction direction : Direction.values()) {
                if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT || node.getDirectionalIO().get(direction) == LevelNode.IOTypes.ALL) {
                    BlockPos pos = node.getPos().relative(direction);
                    BlockEntity blockEntity = getLevel().getBlockEntity(pos);
                    if (blockEntity != null) {
                        LazyOptional<IItemHandler> energyCapability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite());
                        energyCapability.ifPresent(cap -> {

                            for (int i = 0; i < cap.getSlots(); i++) {
                                ItemStack stack = cap.getStackInSlot(i);
                                if (stack.isEmpty()) continue;

                                stack = stack.copyWithCount(Math.min(8, stack.getCount()));

                                if (itemStackHandler.insertItem(0, stack, true).isEmpty()) {
                                    itemStackHandler.insertItem(0, cap.extractItem(i, stack.getCount(), false), false);
                                }
                            }

                        });
                    }
                }
            }
        }
    }

    public List<IItemHandler> getAvailableConsumers() {
        List<IItemHandler> consumers = new ArrayList<>();
        for (LevelNode node : nodesWithInventories) {
            BlockPos position = node.getPos();
            for (Direction direction : Direction.values()) {
                if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT) continue;
                BlockPos neighbourPos = position.relative(direction);

                // check for block entities
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);
                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                        for (int i = 0; i < handler.getSlots(); i++) {

                            if (direction == Direction.UP && be instanceof HopperBlockEntity) continue;

                            if (handler.getStackInSlot(i).isEmpty()) {
                                consumers.add(handler);
                                break;
                            }
                        }
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ITEM_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isEmpty()) return;
                    AbstractSubNetwork subNet = subNetwork.get();
                    if (subNet != this && subNet instanceof ItemSubNetwork subNetwork1) {
                        if (subNetwork1.getItemStackHandler().getStackInSlot(0).isEmpty()) consumers.add(subNetwork1.itemStackHandler);
                    }
                });


            }
        }
        return consumers;
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("item", this.itemStackHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundTag tag) {
        this.itemStackHandler.deserializeNBT(tag.getCompound("item"));
    }

    public ItemStackHandler getItemStackHandler() {
        return itemStackHandler;
    }

    @Override
    public <T> LazyOptional<T> getHandler(BlockPos worldPosition, Direction side) {
        return handlerLazyOptional.cast();
    }

    @Override
    public void mergeData(BlockPos positionBeingMerged, AbstractSubNetwork network) {
        if (network instanceof ItemSubNetwork subNetwork) {

            /*
                get items and insert into this subnets inventory
                then drop the remainder items
             */

            ItemStack extracted = subNetwork.getItemStackHandler().extractItem(0, subNetwork.itemStackHandler.getStackInSlot(0).getCount(), false);
            ItemStack ret = itemStackHandler.insertItem(0, extracted.copy(), false);
            extracted.shrink(extracted.getCount() - ret.getCount());

            if (extracted.isEmpty()) return;

            if (getLevel().isAreaLoaded(positionBeingMerged, 1)) {
                Vec3 position = Vec3.atCenterOf(positionBeingMerged);
                getLevel().addFreshEntity(new ItemEntity(
                        getLevel(),
                        position.x,
                        position.y + .5,
                        position.z,
                        extracted
                ));
            }
        }
    }

    @Override
    public int getRenderingColour() {
        return 0xff1800;
    }
}
