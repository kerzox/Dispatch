package mod.kerzox.dispatch.common.event;

import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DispatchNetworkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Objects;

public class CommonEvents {
    public static int serverTick;
    public static int clientRenderTick;

    public static int prevServerTick;
    public static int preVclientRenderTick;

    public static int secondsToTicks(int seconds) {
        return 20 * seconds;
    }

    public static int minutesToTicks(int minutes) {
        return minutes * 60 * secondsToTicks(1);
    }

    public static int hoursToTicks(int hours) {
        return hours * 60 * 60 * secondsToTicks(1);
    }

    public static String abbreviateNumber(int number, boolean notation) {
        if (number < 1000) {
            return number + (notation ? " FE" : "");
        } else if (number < 1000000) {
            double abbreviatedValue = number / 1000.0;
            return String.format("%.1f" + (notation ? " kFE" : ""), abbreviatedValue);
        } else {
            double abbreviatedValue = number / 1000000.0;
            return String.format("%.1f" + (notation ? " mFE" : ""), abbreviatedValue);
        }
    }

    public static String readableTime(int ticks) {

        int seconds = ticks / 20;

        // Calculate hours, minutes, and remaining seconds
        int hours = seconds / 3600;
        int remainingMinutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        int ticks2 = seconds % 20;

        // Build the formatted string
        StringBuilder formattedString = new StringBuilder();

        if (hours > 0) {
            formattedString.append(hours).append(" hour");
            if (hours > 1) {
                formattedString.append("s");
            }
            if (remainingMinutes > 0 || remainingSeconds > 0) {
                formattedString.append(", ");
            }
        }

        if (remainingMinutes > 0) {
            formattedString.append(remainingMinutes).append(" minute");
            if (remainingMinutes > 1) {
                formattedString.append("s");
            }
            if (remainingSeconds > 0) {
                formattedString.append(" and ");
            }
        }

        if (remainingSeconds > 0) {
            formattedString.append(remainingSeconds).append(" second");
            if (remainingSeconds > 1) {
                formattedString.append("s");
            }
        }

        return formattedString.toString();
    }

    @SubscribeEvent
    public void onWorldTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            if(event.side == LogicalSide.SERVER) {
                serverTick = (serverTick + 1) % 1_728_000;
                prevServerTick = serverTick;
                event.level.getCapability(LevelNetworkHandler.NETWORK).ifPresent(capability -> {
                    if (capability instanceof LevelNetworkHandler network) {
                        network.tick();
                    }
                });
            }
            else if (event.side == LogicalSide.CLIENT) {
                clientRenderTick = (clientRenderTick + 1) % 1_728_000;
                preVclientRenderTick = clientRenderTick;
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();

        if (!level.isClientSide) {
            if (player instanceof ServerPlayer player1) Objects.requireNonNull(LevelNetworkHandler.getHandler(level)).openScreenAt(player1, event.getPos());
        }

    }

    @SubscribeEvent
    public void onBlockDestroyed(BlockEvent.BreakEvent event) {
        LevelAccessor levelAcc = event.getLevel();
        BlockPos pos = event.getPos();

        if (levelAcc instanceof Level level) {

            level.getCapability(LevelNetworkHandler.NETWORK).ifPresent(cap -> {
                if (cap instanceof LevelNetworkHandler levelNetwork) {

                    if (event.getLevel().getBlockEntity(event.getPos()) instanceof DispatchNetworkEntity) return;


                    if (levelNetwork.getSubnetsFrom(LevelNode.of(pos)).isEmpty()) return;

                    // spawn block entity
                    levelNetwork.addToSpawnInWorld(pos);



                }
            });

            for (Direction direction : Direction.values()) {
                level.getCapability(LevelNetworkHandler.NETWORK).map(h->h.getSubnetsFrom(LevelNode.of(event.getPos().relative(direction)))).ifPresent(abstractSubNetworks -> {
                    for (AbstractSubNetwork subNetwork : abstractSubNetworks) {
                        if (subNetwork != null) subNetwork.update();
                    }
                });
            }
        }

    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor levelAcc = event.getLevel();
        BlockPos pos = event.getPos();

        if (levelAcc instanceof Level level) {

            for (Direction direction : Direction.values()) {
                level.getCapability(LevelNetworkHandler.NETWORK).map(h->h.getSubnetsFrom(LevelNode.of(event.getPos().relative(direction)))).ifPresent(abstractSubNetworks -> {
                    for (AbstractSubNetwork subNetwork : abstractSubNetworks) {
                        subNetwork.update();
                    }
                });
            }
        }

    }

    public static int getClientTick() {
        return clientRenderTick;
    }

    public static int getLastClientTick() {
        return preVclientRenderTick;
    }

    public static int getLastServerTick() {
        return prevServerTick;
    }

    public static int getServerTick() {
        return serverTick;
    }

    public static int getAsOverflowSafeTick(int tick) {
        return (tick + 1) % 1_728_000;
    }

    public static boolean every(int tick, int seconds) {
        return tick % (20 * seconds) == 0;
    }

}
