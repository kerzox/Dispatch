package mod.kerzox.dispatch;

import mod.kerzox.dispatch.common.item.DispatchItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Set;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Dispatch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec GENERAL_SPEC;

    public static boolean DEBUG_MODE;

    public static long BASIC_TIER_TRANSFER_SPEED;
    public static long ADV_TIER_TRANSFER_SPEED;
    public static long SUP_TIER_TRANSFER_SPEED;
    public static long ELITE_TIER_TRANSFER_SPEED;
    public static long ULTIMATE_TIER_TRANSFER_SPEED;

    public static long BASIC_TIER_CAPACITY;
    public static long ADV_TIER_CAPACITY;
    public static long SUP_TIER_CAPACITY;
    public static long ELITE_TIER_CAPACITY;
    public static long ULTIMATE_TIER_CAPACITY;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    public static ForgeConfigSpec.BooleanValue DEBUG_MODE_SPEC;

    private static ForgeConfigSpec.LongValue BASIC_TIER_TRANSFER;
    private static ForgeConfigSpec.LongValue ADV_TIER_TRANSFER;
    private static ForgeConfigSpec.LongValue SUP_TIER_TRANSFER;
    private static ForgeConfigSpec.LongValue ELITE_TIER_TRANSFER;
    private static ForgeConfigSpec.LongValue ULTIMATE_TIER_TRANSFER;

    private static ForgeConfigSpec.LongValue BASIC_TIER_CAP;
    private static ForgeConfigSpec.LongValue ADV_TIER_CAP;
    private static ForgeConfigSpec.LongValue SUP_TIER_CAP;
    private static ForgeConfigSpec.LongValue ELITE_TIER_CA;
    private static ForgeConfigSpec.LongValue ULTIMATE_TIER_CAP;

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Energy Cable");
        builder.comment("Energy Cable Tier Options");

        builder.push("Basic Tier");
        BASIC_TIER_TRANSFER = builder
                .comment("Basic tier level transfer speed")
                .defineInRange("fe/tick", 250, 0, Long.MAX_VALUE);
        BASIC_TIER_CAP = builder
                .comment("Basic tier capacity")
                .defineInRange("capacity/cable", 250 * 4, 0, Long.MAX_VALUE);
        builder.pop();

        builder.push("Advanced Tier");
        ADV_TIER_TRANSFER = builder
                .comment("Advanced tier level transfer speed")
                .defineInRange("fe/tick", 500, 0, Long.MAX_VALUE);
        ADV_TIER_CAP = builder
                .comment("Advanced tier capacity")
                .defineInRange("capacity/cable", 500 * 4, 0, Long.MAX_VALUE);
        builder.pop();

        builder.push("Superior Tier");
        SUP_TIER_TRANSFER = builder
                .comment("Superior tier level transfer speed")
                .defineInRange("fe/tick", 2500, 0, Long.MAX_VALUE);
        SUP_TIER_CAP = builder
                .comment("Advanced tier capacity")
                .defineInRange("capacity/cable", 2500 * 4, 0, Long.MAX_VALUE);
        builder.pop();

        builder.push("Elite Tier");
        ELITE_TIER_TRANSFER = builder
                .comment("Elite tier level transfer speed")
                .defineInRange("fe/tick", 5000, 0, Long.MAX_VALUE);
        ELITE_TIER_CA = builder
                .comment("Advanced tier capacity")
                .defineInRange("capacity/cable", 5000 * 4, 0, Long.MAX_VALUE);
        builder.pop();

        builder.push("Ultimate Tier");
        ULTIMATE_TIER_TRANSFER = builder
                .comment("Ultimate tier level transfer speed")
                .defineInRange("fe/tick", 10000, 0, Long.MAX_VALUE);
        ULTIMATE_TIER_CAP = builder
                .comment("Advanced tier capacity")
                .defineInRange("capacity/cable", 10000 * 4, 0, Long.MAX_VALUE);
        builder.pop();

        builder.push("Hidden Features");
        DEBUG_MODE_SPEC = builder
                .comment("Turn on debug mode (Gives debug information on dispatch blocks)")
                .define("debug mode", false);
        builder.pop();

        builder.pop();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        BASIC_TIER_TRANSFER_SPEED = BASIC_TIER_TRANSFER.get();
        BASIC_TIER_CAPACITY = BASIC_TIER_CAP.get();
        ADV_TIER_TRANSFER_SPEED = ADV_TIER_TRANSFER.get();
        ADV_TIER_CAPACITY = ADV_TIER_CAP.get();
        SUP_TIER_TRANSFER_SPEED = SUP_TIER_TRANSFER.get();
        SUP_TIER_CAPACITY = SUP_TIER_CAP.get();
        ELITE_TIER_TRANSFER_SPEED = ELITE_TIER_TRANSFER.get();
        ELITE_TIER_CAPACITY = ELITE_TIER_CA.get();
        ULTIMATE_TIER_TRANSFER_SPEED = ULTIMATE_TIER_TRANSFER.get();
        ULTIMATE_TIER_CAPACITY = ULTIMATE_TIER_CAP.get();
        DEBUG_MODE = DEBUG_MODE_SPEC.get();
    }

    public static long getEnergyCapacity(DispatchItem.Tiers tier) {
        switch (tier) {
            case ADVANCED -> {
                return ADV_TIER_CAPACITY;
            }
            case SUPERIOR -> {
                return SUP_TIER_CAPACITY;
            }
            case ELITE -> {
                return ELITE_TIER_CAPACITY;
            }
            case ULTIMATE -> {
                return ULTIMATE_TIER_CAPACITY;
            }
            default -> {
                return BASIC_TIER_CAPACITY;
            }
        }
    }

    public static long getEnergyTransfer(DispatchItem.Tiers tier) {
        switch (tier) {
            case ADVANCED -> {
                return ADV_TIER_TRANSFER_SPEED;
            }
            case SUPERIOR -> {
                return SUP_TIER_TRANSFER_SPEED;
            }
            case ELITE -> {
                return ELITE_TIER_TRANSFER_SPEED;
            }
            case ULTIMATE -> {
                return ULTIMATE_TIER_TRANSFER_SPEED;
            }
            default -> {
                return BASIC_TIER_TRANSFER_SPEED;
            }
        }
    }
}
