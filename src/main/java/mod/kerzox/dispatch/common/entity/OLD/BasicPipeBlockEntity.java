package mod.kerzox.dispatch.common.entity.OLD;

import mod.kerzox.dispatch.common.entity.BasicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasicPipeBlockEntity extends BasicBlockEntity {

    private OldPipeManager manager;
    protected CompoundTag nbt;

    public BasicPipeBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.createManager();
    }

    public void setManager(OldPipeManager manager) {
        this.manager = manager;
    }

    public void syncBlockEntity() {
        if (level != null) {
            this.setChanged(); // mark dirty
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL); // call update to the block
        }
    }




    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (this.manager == null) return LazyOptional.empty();
        return this.manager.getCapability(cap, side);
    }

    public OldPipeManager getManager() {
        return manager;
    }

    public boolean hasManager() {
        return this.manager != null;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        addToUpdateTag(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        nbt = pTag;
        read(pTag);
    }


    public void update() {
        syncBlockEntity();
    }

    /** DON"T OVERRIDE THIS USE WRITE
     *
     * @param pTag
     */

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (hasManager()) {
            this.getManager().serializeNBT(pTag);
        }
        write(pTag);
    }

    /* overwrite these ones!
        makes sure we don't fuck up super calls etc.
     */


    protected void write(CompoundTag pTag) {

    }

    /** read
     * When load is called this will be called, (on world load, nbt read and updateBlockEntity function)
     * @param pTag nbt data
     */
    public void read(CompoundTag pTag) {

    }

    /** addToUpdateTag
     *  Allows you to add nbt data to the update tag such as variables.
     * @param tag
     */

    protected void addToUpdateTag(CompoundTag tag) {
        write(tag);
    }

    public boolean onPlayerClick(Level pLevel, Player pPlayer, BlockPos pPos, InteractionHand pHand, BlockHitResult pHit) {
        return false;
    }

    public boolean onPlayerAttack(Level pLevel, Player pPlayer, BlockPos pPos) {
        return false;
    }


    public abstract void createManager();

    public void attemptConnection(BlockPos pPos, BlockPos pFromPos, IConnectablePipe blockEntity, IConnectablePipe blockEntity1) {
        this.manager.doNetworkModification(pPos, pFromPos, blockEntity, blockEntity1);
    }
}
