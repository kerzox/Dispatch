package mod.kerzox.dispatch.common.item;

import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DispatchNetworkEntity;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;

public class DispatchItem extends BlockItem {

    public enum Tiers implements StringRepresentable {
        BASIC,
        ADVANCED,
        SUPERIOR,
        ELITE,
        ULTIMATE;

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }
    }

    private Capability<?> capability;
    private Tiers tier;

    public DispatchItem(Capability<?> capability, Tiers tier, Properties p_40566_) {
        super(DispatchRegistry.Blocks.DISPATCH_BLOCK.get(), p_40566_);
        this.capability = capability;
        this.tier = tier;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext ctx, BlockState p_40579_) {
        if (ctx.getPlayer().isShiftKeyDown()) return false;
        if (super.placeBlock(ctx, p_40579_)) {
            ctx.getLevel().getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability -> {
                Player player = ctx.getPlayer();
                if (capability instanceof LevelNetworkHandler handler) {
                    handler.createOrAttachToCapabilityNetwork(this.capability, this.tier, ctx.getClickedPos(), true);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide && ctx.getPlayer().isShiftKeyDown()) {
            ctx.getLevel().getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability -> {
                Player player = ctx.getPlayer();
                if (capability instanceof LevelNetworkHandler handler) {
                    if (handler.getSubnetFromPos(this.capability, LevelNode.of(ctx.getClickedPos())).isEmpty()) {
                        handler.createOrAttachToCapabilityNetwork(this.capability, this.tier, ctx.getClickedPos(), true);
                        for (Direction direction : Direction.values()) {
                            BlockPos pos = ctx.getClickedPos().relative(direction);
                            if (ctx.getLevel().getBlockEntity(pos) instanceof DispatchNetworkEntity entity) {
                                entity.updateVisualConnections();
                                ctx.getLevel().updateNeighborsAt(entity.getBlockPos(), entity.getBlockState().getBlock());
                            }
                        }
                    }
                }
            });
        }
        return super.useOn(ctx);
    }

    public Capability<?> getCapability() {
        return capability;
    }
}
