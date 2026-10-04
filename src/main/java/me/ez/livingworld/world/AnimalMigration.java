package me.ez.livingworld.world;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import me.ez.livingworld.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Animal migration: periodically nudges nearby wild animals toward a slowly rotating
 * heading, so herds drift across the land instead of milling around their spawn chunk.
 */
public final class AnimalMigration {

    /** How far ahead of the animal the waypoint is placed. */
    private static final double WAYPOINT_DISTANCE = 12.0D;

    private AnimalMigration() {}

    public static void tick(ServerLevel level, RandomSource random) {
        double heading = heading(level);
        double speed = Config.MIGRATION_SPEED.get();
        int chance = Config.MIGRATION_CHANCE.get();
        int radius = Config.MIGRATION_RADIUS.get();
        Set<Integer> visited = new HashSet<>();
        int attempts = 0;

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            AABB box = player.getBoundingBox().inflate(radius);
            List<Animal> animals = level.getEntitiesOfClass(Animal.class, box);
            for (Animal animal : animals) {
                if (!visited.add(animal.getId()) || !canMigrate(animal) || random.nextInt(100) >= chance) {
                    continue;
                }
                Vec3 target = animal.position().add(
                        Math.cos(heading) * WAYPOINT_DISTANCE,
                        0.0D,
                        Math.sin(heading) * WAYPOINT_DISTANCE);
                BlockPos targetPos = BlockPos.containing(target);
                if (!level.isAreaLoaded(targetPos, 16)) {
                    continue;
                }
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetPos);
                if (Math.abs(surface.getY() - animal.getY()) > 4
                        || !level.getFluidState(surface).isEmpty()
                        || !level.getFluidState(surface.below()).isEmpty()) continue;
                if (++attempts > 16) return; // Bound expensive pathfinding per level/sample.
                animal.getNavigation().moveTo(target.x, surface.getY(), target.z, speed);
            }
        }
    }

    private static boolean canMigrate(Animal animal) {
        if (!animal.isAlive() || animal.isPassenger() || animal.isLeashed() || animal.isNoAi()) {
            return false;
        }
        if (animal.getTarget() != null || animal.isBaby() || animal.isInLove()
                || animal.hasCustomName() || animal.isVehicle() || animal.isInWater() || animal.isSleeping()
                || !animal.getNavigation().isDone() || animal.getLastHurtByMob() != null) {
            return false;
        }
        // Pets stay with their owner.
        return !(animal instanceof TamableAnimal tame && tame.isTame())
                && !(animal instanceof AbstractHorse horse && horse.isTamed());
    }

    /** @return the current migration heading in radians, drifting slowly over time. */
    private static double heading(ServerLevel level) {
        return AmbienceRules.migrationHeading(level.getSeed(), level.getGameTime());
    }
}
