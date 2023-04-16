package mod.kerzox.dispatch.common.entity;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.block.multi.MultirolePipeBlock;
import mod.kerzox.dispatch.common.entity.manager.PipeManager;
import mod.kerzox.dispatch.common.entity.manager.PipeNetworkUtil;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.IServerTickable;
import mod.kerzox.dispatch.common.util.PipeSettings;
import mod.kerzox.dispatch.common.util.PipeTypes;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MultirolePipe extends BasicBlockEntity implements IPipe, IServerTickable {

    protected CompoundTag nbt;

    protected PipeManager manager;
    protected HashSet<PipeTypes> types = new HashSet<>();
    protected PipeSettings setting = PipeSettings.DEFAULT;

    protected Map<Direction, Boolean> visuallyConnected = new HashMap<>() {{
        put(Direction.NORTH, false);
        put(Direction.SOUTH, false);
        put(Direction.EAST, false);
        put(Direction.WEST, false);
        put(Direction.UP, false);
        put(Direction.DOWN, false);
    }};

    protected Map<Direction, Map<Capability<?>, LazyOptional<?>>> connBlockEntities = new HashMap<>() {{
        put(Direction.NORTH, new HashMap<>());
        put(Direction.SOUTH, new HashMap<>());
        put(Direction.EAST, new HashMap<>());
        put(Direction.WEST, new HashMap<>());
        put(Direction.UP, new HashMap<>());
        put(Direction.DOWN, new HashMap<>());
    }};

    public MultirolePipe(BlockPos pPos, BlockState pBlockState) {
        super(DispatchRegistry.BlockEntities.MULTIROLE_PIPE.get(), pPos, pBlockState);
    }

    @Override
    public void onServer() {
        if (getManager() != null && getManager().getTickingTile() == this) {
            getManager().tick();
        }
        if (getSetting() == PipeSettings.PULL) {
            for (PipeTypes subtype : getSubtypes()) {

            }
        }
    }

    private void clearConnections() {
        for (Direction value : Direction.values()) {
            connBlockEntities.get(value).clear();
        }
    }

    @Override
    public void addVisualConnection(Direction direction, boolean entity) {
        getVisualConnectionMap().put(direction, entity);
    }

    @Override
    public void removeVisualConnection(Direction direction) {
        getVisualConnectionMap().put(direction, false);
    }

    @Override
    public Map<Direction, Boolean> getVisualConnectionMap() {
        return visuallyConnected;
    }

    public HashSet<Direction.Axis> getAxis() {
        HashSet<Direction.Axis> axes = new HashSet<>();
        visuallyConnected.forEach((direction, entity) -> {
            if (direction.getAxis() != Direction.Axis.Y && entity != null) {
                axes.add(direction.getAxis());
            }
        });
        return axes;
    }

    @Override
    public void findCapabilityHolders() {
        if (level != null) {
            clearConnections();
            for (PipeTypes subtype : getSubtypes()) {
                for (Direction direction : Direction.values()) {
                    BlockEntity entity = level.getBlockEntity(worldPosition.relative(direction));
                    if (entity instanceof MultirolePipe pipe) {
                        if (getManager().getNetwork().contains(pipe)) {
                            if (pipe.getManager().getNetwork().contains(this)) {
                                connBlockEntities.get(direction).put(subtype.getCap(), null);
                            }
                            continue;
                        };
                        System.out.println("new listener");
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
                            LazyOptional<?> temp = connBlockEntities.get(direction).get(subtype.getCap());
                            connBlockEntities.get(direction).put(subtype.getCap(), capability);
                            if (temp != null && temp.isPresent() && temp == capability) continue;
                            System.out.println("new listener");
                            capability.addListener(self -> {
                                System.out.println("on invalidation");
                                onCapabilityInvalidation(subtype.getCap(), self, direction);
                            });
                        }
                        continue;
                    }
                }
            }
        }
    }

    protected void onCapabilityInvalidation(Capability<?> capability, LazyOptional<?> self, Direction direction) {
        getCachedByDirection(direction).put(capability, null);
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
        if (nbt != null) { // only read nbt on load if we were a manager of a network
            BlockEntity possibleManager = getLevel().getBlockEntity(NbtUtils.readBlockPos(nbt.getCompound("managerPos")));
            if (possibleManager instanceof MultirolePipe oldManager && oldManager == this) {
                createManager();
                getManager().read(nbt);
            }
        }
        if (getManager() == null) createManager();
        this.getManager().onNetworkUpdate();
        doVisualConnections();
        getManager().addToUpdateQueue(this);
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
//        findCapabilityHolders();
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

    public int countCachedByDirection(Direction direction) {
        return connBlockEntities.get(direction).values().stream().filter(Objects::nonNull).toList().size();
    }

    public int countCachedByCapabilities(Capability<?> capability) {
        Map<Direction, LazyOptional<?>> map = new HashMap<>();
        for (Map.Entry<Direction, Map<Capability<?>, LazyOptional<?>>> mapEntry : connBlockEntities.entrySet()) {
            for (Map.Entry<Capability<?>, LazyOptional<?>> capabilityLazyOptionalEntry : mapEntry.getValue().entrySet()) {
                if (capabilityLazyOptionalEntry.getKey() == capability && capabilityLazyOptionalEntry.getValue() != null) {
                    map.put(mapEntry.getKey(), capabilityLazyOptionalEntry.getValue());
                }
            }
        }
        return map.size();
    }

    public Map<Capability<?>, LazyOptional<?>> getCachedByDirection(Direction direction) {
//        Map<Capability<?>, LazyOptional<?>> map = new HashMap<>();
//        connBlockEntities.get(direction).forEach((capability, lazyOptional) -> {
//            if (lazyOptional != null) map.put(capability, lazyOptional);
//        });
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
        if (getManager() == null) return super.getCapability(cap, side);
        return getManager().getCapability(cap).cast();
    }

    @Override
    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            if (pPlayer.getMainHandItem().getItem() == Items.GOLD_INGOT) {
                changePipeSetting(PipeSettings.PULL);
                return false;
            }
            if (pPlayer.getMainHandItem().getItem() == Items.IRON_INGOT) {
                changePipeSetting(PipeSettings.PUSH);
                return false;
            }
            if (pPlayer.getMainHandItem().getItem() == Items.COPPER_INGOT) {
                changePipeSetting(PipeSettings.DISABLED);
                return false;
            }
            pPlayer.sendSystemMessage(Component.literal("Pipe Setting" + getSetting()));
            pPlayer.sendSystemMessage(Component.literal("Manager Pos: " + getManager().getTickingTile().getAsBlockEntity().getBlockPos().toShortString()));
            pPlayer.sendSystemMessage(Component.literal("Network Size: " + getManager().getNetwork().size()));
            pPlayer.sendSystemMessage(Component.literal("Subnets: " + getManager().getSubNetworks().size()));

            visuallyConnected.entrySet().stream().filter(Map.Entry::getValue).forEach(directionBlockEntityEntry ->  pPlayer.sendSystemMessage(Component.literal("Connected in " + directionBlockEntityEntry.getKey())));

            int connections = 0;
            for (Direction value : Direction.values()) {
               connections += countCachedByDirection(value);
            }

            pPlayer.sendSystemMessage(Component.literal("Connections on this pipe: " + connections));

            if (getManager().hasCapability(ForgeCapabilities.ENERGY)) {
                pPlayer.sendSystemMessage(Component.literal("Energy Stored: " + getManager().getEnergyHandler().map(EnergyStorage::getEnergyStored)));
                int total = 0;
                for (IPipe pipe : getManager().getSubNetworks()) {
                    if (pipe instanceof MultirolePipe multirolePipe) {
                        total += multirolePipe.countCachedByCapabilities(ForgeCapabilities.ENERGY);
                    }
                }
                pPlayer.sendSystemMessage(Component.literal(" --> Subnet connections: " + total));
            }

            if (getManager().hasCapability(ForgeCapabilities.ITEM_HANDLER)) {
                pPlayer.sendSystemMessage(Component.literal("Items Stored: " + getManager().getItemHandler().map(h -> h.getStackInSlot(0))));
                int total = 0;
                for (IPipe pipe : getManager().getSubNetworks()) {
                    if (pipe instanceof MultirolePipe multirolePipe) {
                        total += multirolePipe.countCachedByCapabilities(ForgeCapabilities.ITEM_HANDLER);
                    }
                }
                pPlayer.sendSystemMessage(Component.literal(" --> Subnet connections: " + total));
            }

            if (getManager().hasCapability(ForgeCapabilities.FLUID_HANDLER)) {
                pPlayer.sendSystemMessage(Component.literal("Fluid Stored: " + getManager().getFluidHandler().map(fluidCableStorage -> fluidCableStorage.getFluid().getTranslationKey())));
                int total = 0;
                for (IPipe pipe : getManager().getSubNetworks()) {
                    if (pipe instanceof MultirolePipe multirolePipe) {
                        total += multirolePipe.countCachedByCapabilities(ForgeCapabilities.FLUID_HANDLER);
                    }
                }
                pPlayer.sendSystemMessage(Component.literal(" --> Subnet connections: " + total));
            }

//            pPlayer.sendSystemMessage(Component.literal("Sub types: "));
//            for (PipeTypes type : getManager().getSubTypes()) {
//                pPlayer.sendSystemMessage(Component.literal(" --> " + type.name()));
//            }

            if (pPlayer.isShiftKeyDown()) {
                getManager().onNetworkUpdate();
                if (pLevel.getBlockState(pPos).getBlock() instanceof MultirolePipeBlock multirolePipeBlock) {
                    multirolePipeBlock.updateVoxel();
                }
                syncBlockEntity();
            }
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

    public void doVisualConnections() {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof IPipe updater) {
                for (PipeTypes subtype : this.getSubtypes()) {
                    if (updater.getSubtypes().contains(subtype)) {
                        this.addVisualConnection(direction, true);
                        updater.addVisualConnection(direction.getOpposite(), true);
                        break;
                    }
                }
            } else if (level.getBlockEntity(worldPosition.relative(direction)) != null) {
                this.addVisualConnection(direction, true);
            } else if (this.getVisualConnectionMap().get(direction) != null) {
                this.removeVisualConnection(direction);
            }
        }
    }

    @Override
    protected void write(CompoundTag pTag) {
        CompoundTag tag = new CompoundTag();
        tag.put("managerData", getManager().write());
        ListTag list = new ListTag();
        for (PipeTypes subtype : getSubtypes()) {
            CompoundTag tag1 = new CompoundTag();
            tag1.putString("subtype", subtype.getSerializedName());
            list.add(tag1);
        }
        ListTag list2 = new ListTag();
        getVisualConnectionMap().forEach((direction, aBoolean) -> {
            CompoundTag tag1 = new CompoundTag();
            tag1.putString("direction", direction.getSerializedName());
            tag1.putBoolean("status", aBoolean);
            list2.add(tag1);
        });
        tag.put("connections", list2);
        tag.put("pipeTypes", list);
        pTag.put("pipe", tag);
    }

    @Override
    public void read(CompoundTag pTag) {
       if (pTag.contains("pipe")) {
           this.getSubtypes().clear();
           CompoundTag tag = pTag.getCompound("pipe");
           nbt = tag.getCompound("managerData");
           ListTag list = tag.getList("pipeTypes", Tag.TAG_COMPOUND);
           for (int i = 0; i < list.size(); i++) {
               getSubtypes().add(PipeTypes.valueOf(list.getCompound(i).getString("subtype").toUpperCase()));
           }
           ListTag list2 = tag.getList("connections", Tag.TAG_COMPOUND);
           for (int i = 0; i < list2.size(); i++) {
               CompoundTag tag1 = list2.getCompound(i);
               Direction direction = Direction.valueOf(tag1.getString("direction").toUpperCase());
               boolean connected = tag1.getBoolean("status");
               addVisualConnection(direction, connected);
           }
       }
    }

    public PipeSettings getSetting() {
        return setting;
    }

    public void changePipeSetting(PipeSettings setting) {
        this.setting = setting;
    }

    @Override
    public CompoundTag getNBT() {
        return nbt;
    }

}
