package mod.kerzox.dispatch.common.capability;

import mod.kerzox.dispatch.common.capability.energy.EnergyNetworkHandler;
import mod.kerzox.dispatch.common.capability.fluid.FluidNetworkHandler;
import mod.kerzox.dispatch.common.capability.item.ItemNetworkHandler;
import mod.kerzox.dispatch.common.item.DispatchItem;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraftforge.common.capabilities.CapabilityManager.get;

public class LevelNetworkHandler implements ILevelNetwork, ICapabilitySerializable<CompoundTag> {

    public static final Capability<ILevelNetwork> NETWORK = get(new CapabilityToken<>(){});

    public static LevelNetworkHandler getHandler(Level level) {
        LazyOptional<ILevelNetwork> networkLazyOptional = level.getCapability(NETWORK);
        if (networkLazyOptional.isPresent()) return (LevelNetworkHandler) level.getCapability(NETWORK).resolve().get();
        else return null;
    }

    private LazyOptional<LevelNetworkHandler> lazyOptional = LazyOptional.of(() -> this);
    private Level level;
    private Map<Capability<?>, AbstractNetwork<?>> networkMap = new HashMap<>();
    private Queue<BlockPos> positionsAsNewCables = new LinkedList<>();

    public LevelNetworkHandler(Level level) {
        this.level = level;
        // declare networks to add to the map
        networkMap.put(ForgeCapabilities.ENERGY, new EnergyNetworkHandler(level));
        networkMap.put(ForgeCapabilities.ITEM_HANDLER, new ItemNetworkHandler(level));
        networkMap.put(ForgeCapabilities.FLUID_HANDLER, new FluidNetworkHandler(level));
    }
    
    public void tick() {
        for (AbstractNetwork<?> abstractNetwork : networkMap.values()) {
            abstractNetwork.tickManager();
        }
        if (!positionsAsNewCables.isEmpty()) {
            spawnCablesInWorld(positionsAsNewCables.poll());
        }
    }

    private void spawnCablesInWorld(BlockPos pos) {
        if (level.getBlockEntity(pos) == null) {
            if (level.getBlockState(pos).getBlock() == Blocks.AIR) {
                level.setBlockAndUpdate(pos, DispatchRegistry.Blocks.DISPATCH_BLOCK.get().defaultBlockState());
            }
        }
    }

    public void createOrAttachToCapabilityNetwork(Capability<?> capability, DispatchItem.Tiers tier, BlockPos pos, boolean updateNeighbours) {
        networkMap.get(capability).createOrAttachTo(tier, pos, updateNeighbours);
    }

    public void detachFromCapability(Capability<?> capability, BlockPos pos) {
        getNetworkByCapability(capability).detachAt(pos);
    }

    public void detach(BlockPos pos) {
        for (AbstractNetwork<?> network : networkMap.values()) {
            if (network.isInASubnet(pos)) network.detachAt(pos);
        }
    }

    public Map<Capability<?>, AbstractNetwork<?>> getNetworkMap() {
        return networkMap;
    }

    /**
     * Gets the network handler of the all sub networks related to this capability
     * @param capability network type
     * @return the network handler of specified capability.
     */

    public AbstractNetwork<?> getNetworkByCapability(Capability<?> capability) {
        return networkMap.get(capability);
    }

    public List<AbstractSubNetwork> getSubnetsFrom(LevelNode pos) {
        List<AbstractSubNetwork> subNetworks = new ArrayList<>();
        for (AbstractNetwork<?> network : networkMap.values()) {
            AbstractSubNetwork subNetwork = network.getSubnetByPosition(pos);
            if (subNetwork != null) subNetworks.add(subNetwork);
        }
        return subNetworks;
    }

    public Optional<AbstractSubNetwork> getSubnetFromPos(Capability<?> capability, LevelNode pos) {
        if (!networkMap.containsKey(capability)) return Optional.empty();
        AbstractSubNetwork subNetwork = networkMap.get(capability).getSubnetByPosition(pos);
        return subNetwork != null ? Optional.of(subNetwork) : Optional.empty();
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LevelNetworkHandler.NETWORK.orEmpty(cap, lazyOptional.cast());
    }

    @Override
    public CompoundTag serializeNBT() {

        CompoundTag tag = new CompoundTag();

        getNetworkMap().forEach((capability, network) -> {
            tag.put(capability.getName(), network.serializeNBT());
        });

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

        getNetworkMap().forEach((capability, network) -> {
            if (nbt.contains(capability.getName())) {
                network.deserializeNBT(nbt.getCompound(capability.getName()));
            }
        });

    }

    public void addToSpawnInWorld(BlockPos pos) {
        positionsAsNewCables.add(pos);
    }
}
