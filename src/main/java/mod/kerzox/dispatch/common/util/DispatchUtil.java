package mod.kerzox.dispatch.common.util;

import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

public class DispatchUtil {

    public static int getTintFromTier(DispatchItem.Tiers tier) {
        switch (tier){
            case ADVANCED ->{
                return 0xFFfff751;
            }
            case SUPERIOR -> {
                return 0xFF51f7ff;
            }
            case ELITE -> {
                return 0xFFff9351;
            }
            case ULTIMATE -> {
                return 0xFFff51a4;
            }
            default -> {
                return 0xFF51ff62;
            }
        }
    }

    public static <T> LazyOptional<T> getCapabilityFromDirection(Level level, BlockPos pos, Direction direction, Capability<T> capabilityType) {

        BlockEntity be = level.getBlockEntity(pos.relative(direction));

        if (be != null) {
            return be.getCapability(capabilityType);
        }

        return (LazyOptional<T>) LevelNetworkHandler.getHandler(level).getSubnetFromPos(capabilityType, LevelNode.of(pos.relative(direction))).map(subNetwork -> subNetwork.getHandler(pos, direction)).orElse(LazyOptional.empty());
    }


}
