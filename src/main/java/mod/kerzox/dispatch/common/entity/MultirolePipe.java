package mod.kerzox.dispatch.common.entity;

import mod.kerzox.dispatch.common.entity.manager.PipeManager;
import mod.kerzox.dispatch.common.entity.manager.PipeNetworkUtil;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.IServerTickable;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MultirolePipe extends BasicBlockEntity implements IPipe, IServerTickable {

    protected PipeManager manager;
    protected HashSet<PipeTypes> types = new HashSet<>();

    protected Map<Direction, Map<Capability<?>, LazyOptional<?>>> connBlockEntities = new HashMap<>() {{
        put(Direction.NORTH, new HashMap<>());
        put(Direction.SOUTH, new HashMap<>());
        put(Direction.EAST, new HashMap<>());
        put(Direction.WEST, new HashMap<>());
        put(Direction.UP, new HashMap<>());
        put(Direction.DOWN, new HashMap<>());
    }};

    public MultirolePipe(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        createManager();
    }

    @Override
    public void onServer() {
        if (getManager() != null && getManager().getTickingTile() == this) {
            getManager().tick();
        }
    }

    private void clearConnections() {
        for (Direction value : Direction.values()) {
            connBlockEntities.get(value).clear();
        }
    }

    @Override
    public void findCapabilityHolders() {
        if (level != null) {
            clearConnections();
            for (PipeTypes subtype : getSubtypes()) {
                for (Direction direction : Direction.values()) {
                    BlockEntity entity = level.getBlockEntity(worldPosition.relative(direction));
                    if (entity instanceof MultirolePipe pipe) {
                        if (getManager().getNetwork().contains(pipe)) continue;
                        LazyOptional<?> capability = entity.getCapability(subtype.getCap(), direction.getOpposite());
                        if (capability.isPresent()) {
                            connBlockEntities.get(direction).put(subtype.getCap(), capability);
                            capability.addListener(self -> onCapabilityInvalidation(subtype.getCap(), self, direction));
                        }
                        continue;
                    }
                    if (entity != null) {
                        LazyOptional<?> capability = entity.getCapability(subtype.getCap(), direction.getOpposite());
                        if (capability.isPresent()) {
                            connBlockEntities.get(direction).put(subtype.getCap(), capability);
                            capability.addListener(self -> onCapabilityInvalidation(subtype.getCap(), self, direction));
                        }
                        continue;
                    }
                }
            }
        }
    }

    /*
                        if (entity != null && !(entity instanceof MultirolePipe)) {
                        LazyOptional<?> capability = entity.getCapability(subtype.getCap(), direction.getOpposite());
                        if (capability.isPresent()) {
                            connBlockEntities.get(direction).put(subtype.getCap(), capability);
                            capability.addListener(self -> onCapabilityInvalidation(subtype.getCap(), self, direction));
                        }
                    }
                    if (entity instanceof MultirolePipe pipe && !getManager().getNetwork().contains(pipe)) {
                        LazyOptional<?> capability = entity.getCapability(subtype.getCap(), direction.getOpposite());
                        if (capability.isPresent()) {
                            connBlockEntities.get(direction).put(subtype.getCap(), capability);
                            capability.addListener(self -> onCapabilityInvalidation(subtype.getCap(), self, direction));
                        }
                    }
     */

    protected void onCapabilityInvalidation(Capability<?> capability, LazyOptional<?> self, Direction direction) {
        getCachedByDirection(direction).put(capability, null);
        // notify the manager
        getManager().onNetworkUpdate();
    }

    @Override
    public MultirolePipe getAsBlockEntity() {
        return this;
    }

    @Override
    public void setManager(PipeManager pipeManager) {
        this.manager = pipeManager;
    }

    @Override
    public HashSet<PipeTypes> getSubtypes() {
        return this.types;
    }

    @Override
    public PipeManager getManager() {
        return this.manager;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        findCapabilityHolders();
        this.getManager().onNetworkUpdate();
    }

    @Override
    public PipeManager createManager() {
        PipeManager pipeManager = new PipeManager(this);
        this.setManager(pipeManager);
        return pipeManager;
    }

    public void addType(PipeTypes type, boolean avoidDetachment) {
        this.types.add(type);
        if (!avoidDetachment) {
            this.manager.detach(this);
        }
        createManager();
    }

    public boolean hasCachedInventories() {
        for (Direction direction : Direction.values()) {
            for (LazyOptional<?> optional : getCachedByDirection(direction).values()) {
                if (optional != null && optional.isPresent()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Direction, Map<Capability<?>, LazyOptional<?>>> getConnBlockEntities() {
        return connBlockEntities;
    }

    public Map<Capability<?>, LazyOptional<?>> getCachedByDirection(Direction direction) {
        return connBlockEntities.get(direction);
    }

    public Map<Direction, LazyOptional<?>> getCachedByCapability(Capability<?> capability) {
        Map<Direction, LazyOptional<?>> map = new HashMap<>();
        for (Map.Entry<Direction, Map<Capability<?>, LazyOptional<?>>> mapEntry : connBlockEntities.entrySet()) {
            for (Map.Entry<Capability<?>, LazyOptional<?>> capabilityLazyOptionalEntry : mapEntry.getValue().entrySet()) {
                if (capabilityLazyOptionalEntry.getKey() == capability) {
                    map.put(mapEntry.getKey(), capabilityLazyOptionalEntry.getValue());
                }
            }
        }
        return map;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getManager().getCapability(cap).cast();
    }

    @Override
    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            pPlayer.sendSystemMessage(Component.literal("Manager Pos: " + getManager().getTickingTile().getAsBlockEntity().getBlockPos().toShortString()));
            pPlayer.sendSystemMessage(Component.literal("Network Size: " + getManager().getNetwork().size()));
            pPlayer.sendSystemMessage(Component.literal("Subnets: " + getManager().getSubNetworks().size()));

            int connections = 0;
            for (Direction value : Direction.values()) {
               connections += getCachedByDirection(value).size();
            }

            pPlayer.sendSystemMessage(Component.literal("Connections on this pipe: " + connections));

            if (getManager().hasCapability(ForgeCapabilities.ENERGY)) {
                pPlayer.sendSystemMessage(Component.literal("Energy Stored: " + getManager().getEnergyHandler().getEnergyStored()));
                int total = 0;
                for (IPipe pipe : getManager().getSubNetworks()) {
                    if (pipe instanceof MultirolePipe multirolePipe) {
                        total += multirolePipe.getCachedByCapability(ForgeCapabilities.ENERGY).size();
                    }
                }
                pPlayer.sendSystemMessage(Component.literal("Subnet connections: " + total));


            }

            pPlayer.sendSystemMessage(Component.literal("Sub types: "));
            for (PipeTypes type : getManager().getSubTypes()) {
                pPlayer.sendSystemMessage(Component.literal(" --> " + type.name()));
            }

            if (pPlayer.isShiftKeyDown()) {
                getManager().onNetworkUpdate();
            }
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

}
