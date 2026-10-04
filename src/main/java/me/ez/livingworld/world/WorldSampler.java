package me.ez.livingworld.world;

import me.ez.livingworld.Config;
import me.ez.livingworld.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Shared world sampler. Every {@code sample_interval} ticks it draws a random position
 * around each player and fans the result out to the ambience features, so all of them
 * share one cheap sampling pass instead of each scanning the world on their own.
 */
@EventBusSubscriber(modid = Main.MOD_ID)
public final class WorldSampler {

    private WorldSampler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !Config.ENABLED.get()) {
            return;
        }
        int interval = Config.SAMPLE_INTERVAL.get();
        if (!AmbienceRules.sampleDue(level.getGameTime(), interval) || level.players().isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        boolean moss = Config.MOSS_ENABLED.get();
        boolean vine = Config.VINE_ENABLED.get();
        boolean snow = Config.SNOW_ENABLED.get();

        if (moss || vine || snow) {
            int radius = Config.SAMPLE_RADIUS.get();
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator()) continue;
                BlockPos sample = sampleAround(player.blockPosition(), radius, random);
                if (moss || vine) {
                    MossVineSpread.tick(level, sample, random, moss, vine);
                }
                if (snow) {
                    SnowAccumulation.tick(level, sample, random);
                }
            }
        }

        if (Config.MIGRATION_ENABLED.get()) {
            AnimalMigration.tick(level, random);
        }
    }

    private static BlockPos sampleAround(BlockPos center, int radius, RandomSource random) {
        return center.offset(
                random.nextInt(radius * 2 + 1) - radius,
                random.nextInt(9) - 4,
                random.nextInt(radius * 2 + 1) - radius);
    }
}
