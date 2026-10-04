package me.ez.livingworld.world;

import java.util.Set;
import me.ez.livingworld.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Passive overgrowth: moss creeps across soft ground next to existing moss blocks, and
 * vines grow down a wall or spread onto a nearby free face.
 */
public final class MossVineSpread {

    /** Ground blocks a moss block can creep onto. */
    private static final Set<Block> MOSSY_GROUND = Set.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.GRAVEL,
            Blocks.SAND, Blocks.CLAY, Blocks.MOSSY_COBBLESTONE);

    private static final int SEARCH_ATTEMPTS = 16;
    private static final int MAX_TARGETS = 4;

    private MossVineSpread() {}

    public static void tick(ServerLevel level, BlockPos center, RandomSource random, boolean moss, boolean vine) {
        if (moss) {
            spreadMoss(level, center, random);
        }
        if (vine && level.getGameRules().get(GameRules.SPREAD_VINES)) {
            spreadVines(level, center, random);
        }
    }

    private static void spreadMoss(ServerLevel level, BlockPos center, RandomSource random) {
        int radius = Config.MOSS_RADIUS.get();
        int found = 0;
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS && found < MAX_TARGETS; attempt++) {
            BlockPos pos = sample(center, radius, 2, random);
            if (!level.isAreaLoaded(pos, 2) || !level.getBlockState(pos).is(Blocks.MOSS_BLOCK)) {
                continue;
            }
            found++;
            if (random.nextInt(100) < Config.MOSS_CHANCE.get()) {
                growMoss(level, pos, random);
            }
        }
    }

    private static void growMoss(ServerLevel level, BlockPos moss, RandomSource random) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos target = moss.relative(direction);
        if (random.nextBoolean()) {
            target = target.below();
        }
        if (!level.isAreaLoaded(target, 1) || !level.isInsideBuildHeight(target.getY())
                || !level.isInsideBuildHeight(target.above().getY())) {
            return;
        }
        BlockState state = level.getBlockState(target);
        if (!MOSSY_GROUND.contains(state.getBlock()) || !level.getBlockState(target.above()).isAir()) {
            return;
        }
        level.setBlockAndUpdate(target, Blocks.MOSS_BLOCK.defaultBlockState());

        BlockPos above = target.above();
        if (!level.getBlockState(above).isAir()) {
            return;
        }
        BlockState carpet = Blocks.MOSS_CARPET.defaultBlockState();
        if (random.nextInt(100) < 40 && carpet.canSurvive(level, above)) {
            level.setBlockAndUpdate(above, carpet);
            return;
        }
        BlockState grass = Blocks.SHORT_GRASS.defaultBlockState();
        if (random.nextInt(100) < 25 && grass.canSurvive(level, above)) {
            level.setBlockAndUpdate(above, grass);
        }
    }

    private static void spreadVines(ServerLevel level, BlockPos center, RandomSource random) {
        int radius = Config.MOSS_RADIUS.get();
        int found = 0;
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS && found < MAX_TARGETS; attempt++) {
            BlockPos pos = sample(center, radius, 3, random);
            if (!level.isAreaLoaded(pos, 2) || !level.getBlockState(pos).is(Blocks.VINE)) {
                continue;
            }
            found++;
            if (random.nextInt(100) < Config.VINE_CHANCE.get()) {
                growVine(level, pos, random);
            }
        }
    }

    private static void growVine(ServerLevel level, BlockPos vine, RandomSource random) {
        BlockState vineState = level.getBlockState(vine);

        // Grow straight down while the wall the vine hangs from continues.
        BlockPos below = vine.below();
        if (level.isInsideBuildHeight(below.getY()) && level.getBlockState(below).isAir()) {
            BlockState hanging = Blocks.VINE.defaultBlockState();
            boolean attached = false;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                if (vineState.getValue(VineBlock.PROPERTY_BY_DIRECTION.get(face))) {
                    hanging = hanging.setValue(VineBlock.PROPERTY_BY_DIRECTION.get(face), true);
                    attached = true;
                }
            }
            if (attached && hanging.canSurvive(level, below)) {
                level.setBlockAndUpdate(below, hanging);
                return;
            }
        }

        // Otherwise crawl onto a free face of a nearby wall.
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos side = vine.relative(direction);
        if (!level.getBlockState(side).isAir()) {
            return;
        }
        for (Direction face : Direction.Plane.HORIZONTAL) {
            if (!VineBlock.isAcceptableNeighbour(level, side.relative(face), face)) {
                continue;
            }
            BlockState spread = Blocks.VINE.defaultBlockState()
                    .setValue(VineBlock.PROPERTY_BY_DIRECTION.get(face), true);
            if (spread.canSurvive(level, side)) {
                level.setBlockAndUpdate(side, spread);
            }
            return;
        }
    }

    private static BlockPos sample(BlockPos center, int radius, int vertical, RandomSource random) {
        return center.offset(
                random.nextInt(radius * 2 + 1) - radius,
                random.nextInt(vertical * 2 + 1) - vertical,
                random.nextInt(radius * 2 + 1) - radius);
    }
}
