package mod.kerzox.dispatch.common.capability.item;

import mod.kerzox.dispatch.common.capability.*;
import mod.kerzox.dispatch.common.entity.DispatchNetworkEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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

    /*
                for (ItemNodeOperation operation : operationsAvailable) {

                //TODO replace this with tiered insert amounts
                int maxInsert = Math.min(8, inventoryStack.getCount());

                for (int i = 0; i < consumer.getSlots(); i++) {
                    ItemStack insertStack = itemStackHandler.extractItem(0, maxInsert, true);
                    ItemStack ret = consumer.insertItem(i, insertStack, false);
                    itemStackHandler.extractItem(0, maxInsert, false);
                    if (ret.isEmpty()) return;
                    itemStackHandler.insertItem(0, ret, false);

                }
     */

    @Override
    public void tick() {
        ItemStack inventoryStack = itemStackHandler.getStackInSlot(0).copy();

//        // try to extract first
        if (inventoryStack.getCount() < itemStackHandler.getSlotLimit(0))
            tryExtraction();

        if (inventoryStack.isEmpty()) return;

        // try to insert into any available inventories

        for (LevelNode node : nodesWithInventories) {
            for (Direction direction : Direction.values()) {
                List<NodeOperation> operationsAvailable = node.getOperations().get(direction);
                IItemHandler handler = getConsumerInDirection(node, direction);
                if (handler == null) continue;
                if (operationsAvailable != null) {
                    boolean directionHasOperation = false;
                    for (NodeOperation operation : operationsAvailable) {
                        if (operation instanceof ItemNodeOperation itemNodeOperation) {
                            directionHasOperation = true;
                            // check if we are blacklisting items or whitelisting
                            if (!itemNodeOperation.isBlackList()) {
                                // we are whitelisting items so we only want to transfer this if its the same
                                if (itemNodeOperation.getItemStacks().stream().noneMatch(item -> item.is(inventoryStack.getItem())))
                                    continue; // our operation doesn't have the same itemstack so we ignore this operation.
                            } else {
                                // we are blacklisting items so we never want to transfer the item if its the same
                                if (itemNodeOperation.getItemStacks().stream().anyMatch(item -> item.is(inventoryStack.getItem())))
                                    continue; // our operation doesn't have the same itemstack so we ignore this operation.
                            }

                            //TODO match nbt and size

                            //TODO replace this with tiered insert amounts
                            if (doInsertion(inventoryStack, handler)) return;

                        }
                        if (!directionHasOperation) {
                            if (doInsertion(inventoryStack, handler)) return;
                        }
                    }
                } else {
                    if (doInsertion(inventoryStack, handler)) return;
                }
            }

        }

    }

    private boolean doInsertion(ItemStack inventoryStack, IItemHandler handler) {
        int maxInsert = Math.min(8, inventoryStack.getCount());

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack insertStack = itemStackHandler.extractItem(0, maxInsert, true);
            ItemStack ret = handler.insertItem(i, insertStack, false);
            itemStackHandler.extractItem(0, maxInsert, false);
            if (ret.isEmpty()) return true;
            itemStackHandler.insertItem(0, ret, false);
        }
        return false;
    }

    private IItemHandler getConsumerInDirection(LevelNode node, Direction direction) {
        AtomicReference<IItemHandler> handler = new AtomicReference<>();
        if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT) return handler.get();
        BlockPos neighbourPos = node.getPos().relative(direction);

        // check for block entities
        BlockEntity be = getLevel().getBlockEntity(neighbourPos);
        if (be != null && !(be instanceof DispatchNetworkEntity)) {
            return be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).resolve().isPresent() ? be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).resolve().get() : null;
        }

        getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ITEM_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
            if (subNetwork.isEmpty()) return;
            AbstractSubNetwork subNet = subNetwork.get();
            if (subNet != this && subNet instanceof ItemSubNetwork subNetwork1) {
                if (subNetwork1.getNodeByPosition(neighbourPos).getDirectionalIO().get(direction.getOpposite()) != LevelNode.IOTypes.NONE) {
                    handler.set(subNetwork1.itemStackHandler);
                }
            }
        });

        return handler.get();
    }

    @Override
    public void update() {
        findInventories();

        // update priorities here
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

                if (be != null && !(be instanceof DispatchNetworkEntity)) {
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

                                ItemStack copied = stack.copyWithCount(Math.min(8, stack.getCount()));

                                if (itemStackHandler.insertItem(0, copied, true).isEmpty()) {
                                    itemStackHandler.insertItem(0, cap.extractItem(i, copied.getCount(), false), false);
                                }
                            }

                        });
                    }
                }
            }
        }
    }

    public Map<LevelNode, List<IItemHandler>> getAvailableConsumers() {
        Map<LevelNode, List<IItemHandler>> consumers = new HashMap<>();
        for (LevelNode node : nodesWithInventories) {
            BlockPos position = node.getPos();
            for (Direction direction : Direction.values()) {
                if (node.getDirectionalIO().get(direction) == LevelNode.IOTypes.EXTRACT) continue;
                BlockPos neighbourPos = position.relative(direction);

                // check for block entities
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);
                if (be != null && !(be instanceof DispatchNetworkEntity)) {
                    be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(cap -> consumers.computeIfAbsent(node, node1 -> new ArrayList<>()).add(cap));
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.ITEM_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isEmpty()) return;
                    AbstractSubNetwork subNet = subNetwork.get();
                    if (subNet != this && subNet instanceof ItemSubNetwork subNetwork1) {
                        if (subNetwork1.getNodeByPosition(neighbourPos).getDirectionalIO().get(direction.getOpposite()) != LevelNode.IOTypes.NONE)
                            consumers.computeIfAbsent(node, node1 -> new ArrayList<>()).add(subNetwork1.itemStackHandler);
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
        // for invalidation
        if (worldPosition == null) return handlerLazyOptional.cast();
        if (getNodeByPosition(worldPosition) == null) return LazyOptional.empty();


        if (getNodeByPosition(worldPosition).getDirectionalIO().get(side) != LevelNode.IOTypes.NONE)
            return handlerLazyOptional.cast();
        else return LazyOptional.empty();
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
