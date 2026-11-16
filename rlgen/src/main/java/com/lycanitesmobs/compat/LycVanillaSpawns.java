package com.lycanitesmobs.compat;

import com.lycanitesmobs.LycEntities;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12 EntityRegistry.addSpawn (Lycanites "monster"/"creature"/"watercreature"/"ambient" spawners): entries added to the
 * biome spawn lists by the biome modifier lycanitesmobs:vanilla_spawns (data/lycanitesmobs/neoforge/biome_modifier). Biomes are
 * the ids of {@link LycBiomes}; the 1.12 hell and sky biomes stand for every nether and end biome.
 */
public final class LycVanillaSpawns {
   private record Entry(Class<?> entityClass, int weight, int min, int max, MobCategory category) {
   }

   private static final Map<ResourceLocation, List<Entry>> SPAWNS = new LinkedHashMap<>();
   private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
      DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, "lycanitesmobs");

   private LycVanillaSpawns() {
   }

   public static void init(IEventBus modBus) {
      SERIALIZERS.register("vanilla_spawns", () -> MapCodec.unit(Modifier.INSTANCE));
      SERIALIZERS.register(modBus);
   }

   /** 1.12 EntityRegistry.addSpawn(entityClass, weight, min, max, type, biomes...) */
   public static void addSpawn(Class<?> entityClass, int weight, int min, int max, MobCategory category, ResourceLocation... biomes) {
      for (ResourceLocation biome : biomes) {
         if (biome != null) {
            SPAWNS.computeIfAbsent(biome, k -> new ArrayList<>()).add(new Entry(entityClass, weight, min, max, category));
         }
      }
   }

   public enum Modifier implements BiomeModifier {
      INSTANCE;

      @Override
      public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
         if (phase != Phase.ADD) {
            return;
         }
         java.util.Set<ResourceLocation> ids = new java.util.LinkedHashSet<>();
         biome.unwrapKey().ifPresent(key -> ids.add(key.location()));
         if (biome.is(BiomeTags.IS_NETHER)) {
            ids.add(LycBiomes.HELL);
         }
         if (biome.is(BiomeTags.IS_END)) {
            ids.add(LycBiomes.SKY);
         }
         List<Entry> entries = new ArrayList<>();
         for (ResourceLocation id : ids) {
            entries.addAll(SPAWNS.getOrDefault(id, List.of()));
         }
         for (Entry entry : entries) {
            EntityType<?> type = LycEntities.type((Class)entry.entityClass());
            if (type != null && entry.weight() > 0) {
               builder.getMobSpawnSettings().addSpawn(entry.category(),
                  new MobSpawnSettings.SpawnerData(type, entry.weight(), entry.min(), Math.max(entry.min(), entry.max())));
            }
         }
      }

      @Override
      public MapCodec<? extends BiomeModifier> codec() {
         return MapCodec.unit(INSTANCE);
      }
   }
}
