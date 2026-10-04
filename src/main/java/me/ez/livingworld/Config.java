package me.ez.livingworld;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common configuration for Living World. */
public class Config {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue SAMPLE_INTERVAL;
    public static final ModConfigSpec.IntValue SAMPLE_RADIUS;

    public static final ModConfigSpec.BooleanValue MOSS_ENABLED;
    public static final ModConfigSpec.IntValue MOSS_CHANCE;
    public static final ModConfigSpec.IntValue MOSS_RADIUS;

    public static final ModConfigSpec.BooleanValue VINE_ENABLED;
    public static final ModConfigSpec.IntValue VINE_CHANCE;

    public static final ModConfigSpec.BooleanValue SNOW_ENABLED;
    public static final ModConfigSpec.IntValue SNOW_CHANCE;

    public static final ModConfigSpec.BooleanValue MIGRATION_ENABLED;
    public static final ModConfigSpec.IntValue MIGRATION_CHANCE;
    public static final ModConfigSpec.IntValue MIGRATION_RADIUS;
    public static final ModConfigSpec.DoubleValue MIGRATION_SPEED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Master switch for Living World").translation("livingworld.configuration.general").push("general");
        ENABLED = builder.comment("Enable Living World").translation("livingworld.configuration.general.enabled")
                .define("enabled", true);
        SAMPLE_INTERVAL = builder.comment("Ticks between world samples")
                .translation("livingworld.configuration.general.sample_interval")
                .defineInRange("sample_interval", 40, 1, 1200);
        SAMPLE_RADIUS = builder.comment("Radius (in blocks) around each player that is sampled")
                .translation("livingworld.configuration.general.sample_radius")
                .defineInRange("sample_radius", 32, 4, 128);
        builder.pop();

        builder.comment("Moss & Vine Spread").translation("livingworld.configuration.overgrowth").push("overgrowth");
        MOSS_ENABLED = builder.comment("Enable passive moss spreading")
                .translation("livingworld.configuration.overgrowth.moss_enabled").define("moss_enabled", true);
        MOSS_CHANCE = builder.comment("Chance (percent) for a moss block to spread per sample")
                .translation("livingworld.configuration.overgrowth.moss_chance")
                .defineInRange("moss_chance", 25, 1, 100);
        MOSS_RADIUS = builder.comment("Radius searched around a sample for moss and vine blocks")
                .translation("livingworld.configuration.overgrowth.moss_radius")
                .defineInRange("moss_radius", 6, 1, 16);
        VINE_ENABLED = builder.comment("Enable passive vine growth and spreading")
                .translation("livingworld.configuration.overgrowth.vine_enabled").define("vine_enabled", true);
        VINE_CHANCE = builder.comment("Chance (percent) for a vine to grow per sample")
                .translation("livingworld.configuration.overgrowth.vine_chance")
                .defineInRange("vine_chance", 20, 1, 100);
        builder.pop();

        builder.comment("Snow Accumulation").translation("livingworld.configuration.snow").push("snow");
        SNOW_ENABLED = builder.comment("Enable snow accumulation in cold biomes")
                .translation("livingworld.configuration.snow.enabled").define("enabled", true);
        SNOW_CHANCE = builder.comment("Chance (percent) per sample to add or deepen a snow layer")
                .translation("livingworld.configuration.snow.chance").defineInRange("chance", 30, 1, 100);
        builder.pop();

        builder.comment("Animal Migration").translation("livingworld.configuration.migration").push("migration");
        MIGRATION_ENABLED = builder.comment("Enable animal migration")
                .translation("livingworld.configuration.migration.enabled").define("enabled", true);
        MIGRATION_CHANCE = builder.comment("Chance (percent) per animal per sample to pick a migration target")
                .translation("livingworld.configuration.migration.chance").defineInRange("chance", 5, 1, 100);
        MIGRATION_RADIUS = builder.comment("Radius (in blocks) around each player in which animals migrate")
                .translation("livingworld.configuration.migration.radius").defineInRange("radius", 48, 8, 128);
        MIGRATION_SPEED = builder.comment("Pathfinding speed multiplier for migrating animals")
                .translation("livingworld.configuration.migration.speed").defineInRange("speed", 1.0D, 0.1D, 2.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {}
}
