package mod.kerzox.dispatch.common.capability.fluid;

import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FluidSubNetwork extends AbstractSubNetwork {

    private FluidStorageTank tank = new FluidStorageTank(1000);
    private LazyOptional<FluidStorageTank> handler = LazyOptional.of(() -> tank);

    // Nodes that have inventories around them
    private HashSet<LevelNode> nodesWithInventories = new HashSet<>();
    private HashSet<LevelNode> nodesWithExtraction = new HashSet<>();
    private HashSet<LevelNode> nodesWithInsertion = new HashSet<>();

    public FluidSubNetwork(AbstractNetwork<?> network, DispatchItem.Tiers tier, BlockPos pos) {
        super(network, ForgeCapabilities.FLUID_HANDLER, tier, pos);
    }

    @Override
    public void tick() {

        FluidStack stack = tank.getFluid();
        if (tank.isEmpty()) return;

        List<IFluidHandler> consumers = getAvailableConsumers();
        if (consumers.size() == 0) return;

        for (IFluidHandler consumer : consumers) {
            int returned = consumer.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(returned);
        }


    }


    @Override
    public void update() {
        super.update();
        findInventories();
    }


    /**
     * Finds any valid inventory (item capability) and adds the node next to it to nodesWithInventories set
     */

    private void findInventories() {
        for (LevelNode node : getNodes()) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = node.getPos().relative(direction);
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);

                if (be != null) {
                    be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                        nodesWithInventories.add(node);
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.FLUID_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isPresent()) nodesWithInventories.add(node);
                });

            }
        }
    }

    public List<IFluidHandler> getAvailableConsumers() {
        List<IFluidHandler> consumers = new ArrayList<>();
        for (LevelNode node : nodesWithInventories) {
            BlockPos position = node.getPos();
            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = position.relative(direction);

                // check for block entities
                BlockEntity be = getLevel().getBlockEntity(neighbourPos);
                if (be != null && !(be instanceof DynamicTilingEntity)) {
                    be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                        for (int i = 0; i < handler.getTanks(); i++) {

                            if (handler.getFluidInTank(i).getAmount() < handler.getTankCapacity(i)) {
                                consumers.add(handler);
                                break;
                            }
                        }
                    });
                }

                getLevel().getCapability(LevelNetworkHandler.NETWORK).map(h -> h.getSubnetFromPos(ForgeCapabilities.FLUID_HANDLER, LevelNode.of(neighbourPos))).ifPresent(subNetwork -> {
                    if (subNetwork.isEmpty()) return;
                    AbstractSubNetwork subNet = subNetwork.get();
                    if (subNet != this && subNet instanceof FluidSubNetwork subNetwork1) {
                        if (subNetwork1.getTank().getFluid().isEmpty()) consumers.add(subNetwork1.tank);
                    }
                });


            }
        }
        return consumers;
    }

    @Override
    protected void postAttachment(LevelNode pos) {
        // change capacity based on number of cables
        tank.setCapacity(nodes.size() * 16000);
    }

    public FluidStorageTank getTank() {
        return tank;
    }

    @Override
    protected CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tank.writeToNBT(tag);
        return tag;
    }

    @Override
    protected void read(CompoundTag tag) {
        tank.readFromNBT(tag);
    }

    @Override
    public int getRenderingColour() {
        return 0x6fb0fc;
    }

    @Override
    public <T> LazyOptional<T> getHandler(Direction side) {
        return handler.cast();
    }

    @Override
    public void mergeData(BlockPos chosenPosition, AbstractSubNetwork network) {
        if (network instanceof FluidSubNetwork subNetwork) {
            this.tank.fill(subNetwork.getTank().getFluid(), IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
