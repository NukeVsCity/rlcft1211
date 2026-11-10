package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.IafLandMobs;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12 ModEntities.init natural spawns, by the 1.12 BiomeDictionary types of each biome (LegacyBiomeTypes):
 * death worm CREATURE 2 (1-3) in SANDY + DRY, not BEACH or MESA; troll MONSTER 500 (1-1) once per troll type the biome matches
 * (FOREST, SNOWY, MOUNTAIN); cockatrice CREATURE 4 (1-2) in SAVANNA + SPARSE. Gorgons, cyclopes and myrmex only come from their
 * structures. Used by data/iceandfire/neoforge/biome_modifier/land_spawns.json.
 */
public record IafLandSpawns() implements BiomeModifier {
   public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, IceAndFire.MODID);
   public static final MapCodec<IafLandSpawns> CODEC = MapCodec.unit(IafLandSpawns::new);

   static {
      SERIALIZERS.register("land_spawns", () -> CODEC);
   }

   public static void register(IEventBus modBus) {
      SERIALIZERS.register(modBus);
   }

   @Override
   public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
      if (phase != Phase.ADD) {
         return;
      }
      Set<String> t = LegacyBiomeTypes.types(biome);
      MobSpawnSettingsBuilderView spawns = new MobSpawnSettingsBuilderView(builder);
      if (IceAndFire.CONFIG.spawnDeathWorm && t.contains("SANDY") && t.contains("DRY") && !t.contains("BEACH") && !t.contains("MESA")) {
         spawns.add(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(IafLandMobs.DEATHWORM.get(), IceAndFire.CONFIG.deathWormSpawnRate, 1, 3));
      }
      if (IceAndFire.CONFIG.spawnTrolls) {
         for (String type : new String[]{"FOREST", "SNOWY", "MOUNTAIN"}) {
            if (t.contains(type)) {
               spawns.add(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(IafLandMobs.TROLL.get(), IceAndFire.CONFIG.trollSpawnRate, 1, 1));
            }
         }
      }
      if (IceAndFire.CONFIG.spawnCockatrices && t.contains("SAVANNA") && t.contains("SPARSE")) {
         spawns.add(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(IafLandMobs.COCKATRICE.get(), IceAndFire.CONFIG.cockatriceSpawnRate, 1, 2));
      }
   }

   @Override
   public MapCodec<? extends BiomeModifier> codec() {
      return CODEC;
   }

   private record MobSpawnSettingsBuilderView(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
      void add(MobCategory category, MobSpawnSettings.SpawnerData data) {
         this.builder.getMobSpawnSettings().addSpawn(category, data);
      }
   }
}
