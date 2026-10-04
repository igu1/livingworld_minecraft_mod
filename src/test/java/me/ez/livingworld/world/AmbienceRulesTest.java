package me.ez.livingworld.world;

/** Dependency-free checks; run by Gradle check via regressionTest. */
public final class AmbienceRulesTest {
    public static void main(String[] args) {
        int checks = 0;
        for (int layers = 1; layers <= 8; layers++) {
            require(AmbienceRules.nextSnowLayers(layers) == Math.min(8, layers + 1), "snow layer cap");
            checks++;
        }
        require(AmbienceRules.sampleDue(0, 40), "first sample");
        require(!AmbienceRules.sampleDue(39, 40), "before interval");
        require(AmbienceRules.sampleDue(40, 40), "interval boundary");
        require(!AmbienceRules.sampleDue(40, 0), "zero interval");
        require(!AmbienceRules.sampleDue(40, -1), "negative interval");
        for (long seed : new long[]{0, 1, -1, Long.MIN_VALUE, Long.MAX_VALUE}) {
            double heading = AmbienceRules.migrationHeading(seed, 24000);
            require(Double.isFinite(heading) && heading >= 0 && heading < 2 * Math.PI, "heading bounds");
            checks++;
        }
        double before = AmbienceRules.migrationHeading(0, 23999);
        double after = AmbienceRules.migrationHeading(0, 24000);
        require(Math.abs(after - before) < 0.0001D, "continuous day boundary");
        require(Math.abs(AmbienceRules.migrationHeading(0, 24000) - Math.toRadians(37)) < 1e-9, "daily drift");
        System.out.println("Passed " + (checks + 7) + " ambience policy checks");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
