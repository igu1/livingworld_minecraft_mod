# Living World

Ambience: moss and vine overgrowth, snow accumulation and animal migration.

Minecraft **26.1.2**, NeoForge **26.1.2.108**, Java **25**.
Uses vanilla blocks and animals; no custom models or textures are needed.

## Build
```bash
./gradlew build            # compile + jar
./gradlew runClient        # launch the dev client
./gradlew buildAndCollect  # copy jar into builds/26.1.2-neoforge/
```

## Behaviour and configuration

Settings are in `config/livingworld-common.toml`, or Mods → Living World → Config.
On dedicated servers, edit the server's configuration; client settings do not control it.

- **Overgrowth:** random samples near non-spectator players find existing moss/vines.
  Moss converts adjacent stone, cobblestone, dirt, gravel, sand, clay and mossy
  cobblestone. This can change builds made from these blocks; there is no claim-plugin
  integration. Disable moss spreading if you want to preserve these materials.
  Extra vine growth respects `spread_vines`.
- **Snow:** during snowfall, samples use the exposed surface height and vanilla
  biome/light/survival rules. Existing snow increases up to eight layers, not towers.
- **Migration:** idle wild adults receive short terrain-adjusted waypoints. Pets,
  tamed horses, named/leashed/ridden/sleeping/breeding/busy animals are skipped.
  Animals are deduplicated across nearby players; pathfinding is capped at 16
  attempts per level per sampling interval. Fences still constrain navigation.

Sampling defaults to every 40 ticks (two seconds). Changes are probabilistic and
location-dependent: **no guaranteed seconds/minutes estimate** for isolated moss or
vines. `random_tick_speed` does not speed up this sampler. Lower `sample_interval`
and increase chances for testing; very aggressive settings cost more server time.

## Verification

`./gradlew check` runs 20 dependency-free policy checks (snow cap, sampling interval,
heading bounds/continuity). These are not full in-world GameTests. Build and startup
checks do not establish that every terrain/animal interaction works correctly.
