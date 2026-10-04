package me.ez.livingworld.world;

/** Pure, regression-tested limits shared by the world features. */
public final class AmbienceRules {
    private AmbienceRules() {}

    public static int nextSnowLayers(int layers) {
        return Math.min(8, layers + 1);
    }

    public static boolean sampleDue(long tick, int interval) {
        return interval > 0 && tick % interval == 0;
    }

    public static double migrationHeading(long seed, long tick) {
        double degrees = Math.floorMod(seed, 360L) + tick / 24000.0D * 37.0D;
        return Math.toRadians(degrees % 360.0D);
    }
}
