package mod.kerzox.dispatch.common.entity;

import mod.kerzox.dispatch.common.capability.energy.EnergyCableStorage;
import mod.kerzox.dispatch.common.util.IServerTickable;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class Generator extends BasicBlockEntity implements IServerTickable {

    private EnergyCableStorage storage = new EnergyCableStorage(Integer.MAX_VALUE, 5000);
    private LazyOptional<EnergyStorage> lazy = LazyOptional.of(() -> storage);

    private boolean generator = false;

    public Generator(BlockPos pPos, BlockState pBlockState) {
        super(DispatchRegistry.BlockEntities.ENERGY_GENERATOR.get(), pPos, pBlockState);
    }

    @Override
    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            if (pPlayer.isShiftKeyDown()) {
                generator = !generator;
            }
            else {
                pPlayer.sendSystemMessage(Component.literal("Is generating: " + generator));
                pPlayer.sendSystemMessage(Component.literal("Storage: " + storage.getEnergyStored()));
            }
        }
        return super.onPlayerClick(pLevel, pPlayer, pPos, pHand, pHit);
    }

    @Override
    public void onServer() {
        if (level != null) {
            if (generator) {
                AtomicInteger currentEnergy = new AtomicInteger(this.storage.getEnergyStored());
                if (currentEnergy.get() > 0) {
                    for (Direction direction : Direction.values()) {
                        if (level.getBlockEntity(worldPosition.relative(direction)) != null) {
                            level.getBlockEntity(worldPosition.relative(direction)).getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
                                if (cap.canReceive()) {
                                    int received = cap.receiveEnergy(Math.min(currentEnergy.get(), 500), false);
                                    currentEnergy.addAndGet(-received);
                                    this.storage.useEnergy(received);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.lazy.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ENERGY && !generator ? lazy.cast() : LazyOptional.empty();
    }
}
