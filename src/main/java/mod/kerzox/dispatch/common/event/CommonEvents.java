package mod.kerzox.dispatch.common.event;

import com.google.common.graph.Network;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.NetworkHandler;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

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
                event.level.getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                    if (capability instanceof NetworkHandler network) {
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
    public void onBlockDestroyed(BlockEvent.BreakEvent event) {
        LevelAccessor levelAcc = event.getLevel();
        BlockPos pos = event.getPos();

        if (levelAcc instanceof Level level) {
            level.getCapability(NetworkHandler.NETWORK).ifPresent(cap -> {
                if (cap instanceof NetworkHandler levelNetwork) {

                    if (event.getLevel().getBlockEntity(event.getPos()) instanceof DynamicTilingEntity) return;


                    if (levelNetwork.getSubnetsFrom(LevelNode.of(pos)).isEmpty()) return;

                    // spawn block entity
                    levelNetwork.addToSpawnInWorld(pos);
                }
            });
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
