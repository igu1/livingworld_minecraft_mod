package me.ez.livingworld.world;

import me.ez.livingworld.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Snow accumulation: while it is snowing in a cold biome, exposed surfaces slowly gain a
 * snow layer and existing layers deepen, up to a full block.
 */
public final class SnowAccumulation {

    private static final int ATTEMPTS = 6;

    private SnowAccumulation() {}

    public static void tick(ServerLevel level, BlockPos center, RandomSource random) {
        if (!level.isRaining()) {
            return;
        }
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos column = center.offset(
                    random.nextInt(9) - 4,
                    0,
                    random.nextInt(9) - 4);
            if (!level.hasChunkAt(column)) {
                continue;
            }
            if (random.nextInt(100) >= Config.SNOW_CHANCE.get()) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            // The heightmap lies above existing snow, not inside its layer block.
            BlockPos pos = level.getBlockState(surface.below()).is(Blocks.SNOW)
                    ? surface.below() : surface;
            if (level.precipitationAt(surface) != Biome.Precipitation.SNOW
                    || !level.getBiome(pos).value().shouldSnow(level, pos)) {
                continue;
            }
            accumulate(level, pos);
        }
    }

    private static void accumulate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.SNOW)) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers < 8) {
                level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, AmbienceRules.nextSnowLayers(layers)));
            }
            return;
        }

        if (!state.isAir()) {
            return;
        }
        BlockState snow = Blocks.SNOW.defaultBlockState();
        if (snow.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, snow);
        }
    }
}
