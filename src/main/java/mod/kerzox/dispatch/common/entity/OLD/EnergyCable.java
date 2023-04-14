package mod.kerzox.dispatch.common.entity.OLD;

import mod.kerzox.dispatch.common.util.IServerTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ENERGY_CABLE;

public class EnergyCable extends BasicPipeBlockEntity implements IServerTickable, IConnectablePipe<IEnergyStorage> {

    private final Map<Direction, LazyOptional<IEnergyStorage>> cache = new EnumMap<>(Direction.class);

    public EnergyCable(BlockPos pPos, BlockState pBlockState) {
        super(ENERGY_CABLE.get(), pPos, pBlockState);
    }

    @Override
    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        if (hasManager() && !pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            pPlayer.sendSystemMessage(Component.literal("Manager Pos" + getManager().getManagerTile().getBlockPos().toShortString()));
            pPlayer.sendSystemMessage(Component.literal("Network Size" + getManager().getNetwork().size()));
            pPlayer.sendSystemMessage(Component.literal("Energy Buffer: " + getManager().getCache().getInternalCableStorage().getEnergyStored()));
            pPlayer.sendSystemMessage(Component.literal("Energy Capacity: " + getManager().getCache().getInternalCableStorage().getMaxEnergyStored()));
            int total = 0;

            for (EnergyCable cable : getManager().getCache().cablesWithCapabilities()) {
                total += cable.getAllCapabilites().size();
            }
            pPlayer.sendSystemMessage(Component.literal("Energy Storages on network: " +total));
            pPlayer.sendSystemMessage(Component.literal("Energy Storages on this pipe" + getAllValidStorages().size()));
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

    @Override
    public void createManager() {
        this.setManager(new OldEnergyCableManager(this));

    }

    @Override
    public OldEnergyCableManager getManager() {
        return (OldEnergyCableManager) super.getManager();
    }

    @Override
    public void checkInventoryAt(BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof IConnectablePipe pipe) {
            return;
        }
        LazyOptional<IEnergyStorage> energy_capability = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
        if (energy_capability.resolve().isPresent()) {
            this.cache.put(direction,energy_capability);
            energy_capability.addListener(self -> test(direction));
        }
        if (hasManager()) {
            getManager().getCache().addToCache(this);
            getManager().updateCache();
        }
    }

    @Override
    public void onServer() {
        if (hasManager() && this.getManager().getManagerTile() == this) {
            getManager().tick();
        }
    }

    @Override
    protected void write(CompoundTag pTag) {
        pTag.putBoolean("find", this.hasValidStorages());
    }

    @Override
    protected void read(CompoundTag pTag) {

    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (nbt != null) {
            this.getManager().deserializeNBT(nbt);
            if (nbt.getBoolean("find")) {
                for (Direction direction : Direction.values()) {
                    BlockPos pNeighborPos = worldPosition.relative(direction);
                    if (level.getBlockEntity(pNeighborPos) != null) {
                        if (level.getBlockEntity(worldPosition) instanceof IConnectablePipe basic) {
                            basic.checkInventoryAt(level.getBlockEntity(pNeighborPos), direction);
                        }
                    }
                }
            }
        }
    }

    @Override
    public EnergyCable getPipe() {
        return this;
    }

    @Override
    public Map<Direction, LazyOptional<IEnergyStorage>> getCache() {
        return cache;
    }

    @Override
    public LazyOptional<IEnergyStorage> fromCache(Direction direction) {
        return cache.get(direction);
    }

    @Override
    public void toCache(Direction direction, LazyOptional<IEnergyStorage> cap) {
        this.cache.put(direction, cap);
        cap.addListener(self -> test(direction));
    }

    public void test(Direction direction) {
        cache.put(direction, LazyOptional.empty());
        System.out.println("Removing cache");
        this.getManager().updateCache();
    }

    public List<IEnergyStorage> getAllValidStorages() {
        List<IEnergyStorage> storages = new ArrayList<>();
        for (LazyOptional<IEnergyStorage> lazyOptional : getAllCapabilites()) {
            if (lazyOptional.resolve().isPresent()) {
                storages.add(lazyOptional.resolve().get());
            }
        }
        return storages;
    }

    public boolean hasValidStorages() {
        return getAllCapabilites().stream().anyMatch(LazyOptional::isPresent);
    }

    @Override
    public Collection<LazyOptional<IEnergyStorage>> getAllCapabilites() {
        return this.cache.values();
    }
}
