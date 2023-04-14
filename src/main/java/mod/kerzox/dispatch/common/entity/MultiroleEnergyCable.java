package mod.kerzox.dispatch.common.entity;

import mod.kerzox.dispatch.common.entity.manager.PipeManager;
import mod.kerzox.dispatch.common.util.IServerTickable;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ENERGY_CABLE;

public class MultiroleEnergyCable extends MultirolePipe {

    public MultiroleEnergyCable(BlockPos pPos, BlockState pBlockState) {
        super(ENERGY_CABLE.get(), pPos, pBlockState);
        addType(PipeTypes.ENERGY, true);
    }

}
