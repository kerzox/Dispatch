package mod.kerzox.dispatch.common.entity;

import mod.kerzox.dispatch.common.entity.manager.PipeManager;
import mod.kerzox.dispatch.common.util.IServerTickable;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.EnumMap;
import java.util.Map;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ITEM_CABLE;

public class MultiroleItemCable extends MultirolePipe{

    private final Map<Direction, LazyOptional<IEnergyStorage>> cache = new EnumMap<>(Direction.class);

    public MultiroleItemCable(BlockPos pPos, BlockState pBlockState) {
        super(ITEM_CABLE.get(), pPos, pBlockState);
        addType(PipeTypes.ITEM, true);
    }

}
