package com.nukevscity.rlgen.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * Biomes from the 1.12 GenLayer stack (1:1 voronoi biome at each quart's corner block). Below Y 0 the
 * 1.21 cave biomes (deep dark, lush caves, dripstone caves) are used where 1.21 would put them.
 */
public final class LegacyBiomeSource extends BiomeSource {
   public static final MapCodec<LegacyBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            RegistryOps.retrieveGetter(Registries.BIOME),
            Codec.STRING.optionalFieldOf("biome_lists", "rlcraft").forGetter(s -> s.listsName),
            BiomeSource.CODEC.fieldOf("underground").forGetter(s -> s.underground)
         )
         .apply(i, i.stable(LegacyBiomeSource::new))
   );

   private final String listsName;
   private final Map<Biome, Holder<net.minecraft.world.level.biome.Biome>> holders = new IdentityHashMap<>();
   /** 1.21 overworld biomes, used below Y 0 only, and only for the cave biomes. */
   private final BiomeSource underground;
   private final Set<Holder<net.minecraft.world.level.biome.Biome>> caveBiomes = new LinkedHashSet<>();
   private volatile LegacyGenerator generator;
   private volatile Climate.Sampler undergroundSampler;

   private LegacyBiomeSource(HolderGetter<net.minecraft.world.level.biome.Biome> biomes, String listsName, BiomeSource underground) {
      this.listsName = listsName;
      this.underground = underground;
      for (ResourceKey<net.minecraft.world.level.biome.Biome> key : List.of(
         net.minecraft.world.level.biome.Biomes.DEEP_DARK, net.minecraft.world.level.biome.Biomes.LUSH_CAVES,
         net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES)) {
         this.caveBiomes.add(biomes.getOrThrow(key));
      }
      Biomes.init();
      for (Biome b : Biome.all()) {
         this.holders.put(b, biomes.getOrThrow(LegacyBiomeMap.modern(b)));
      }
   }

   public BiomeLists createLists() {
      return "vanilla".equals(this.listsName) ? BiomeLists.vanilla() : BiomeLists.rlcraft();
   }

   void setGenerator(LegacyGenerator generator, Climate.Sampler undergroundSampler) {
      this.undergroundSampler = undergroundSampler;
      this.generator = generator;
   }

   LegacyGenerator generator() {
      LegacyGenerator g = this.generator;
      if (g == null) {
         throw new IllegalStateException("rlgen: biome source used before the world seed was known");
      }
      return g;
   }

   public Holder<net.minecraft.world.level.biome.Biome> holder(Biome legacy) {
      return this.holders.get(legacy);
   }

   @Override
   protected MapCodec<? extends BiomeSource> codec() {
      return CODEC;
   }

   @Override
   protected Stream<Holder<net.minecraft.world.level.biome.Biome>> collectPossibleBiomes() {
      Set<Holder<net.minecraft.world.level.biome.Biome>> out = new LinkedHashSet<>();
      for (Biome b : Biome.all()) {
         if (b == Biomes.HELL || b == Biomes.SKY || b == Biomes.VOID) {
            continue;
         }
         if (!"vanilla".equals(this.listsName) || !b.getRegistryName().contains(":")) {
            out.add(this.holders.get(b));
         }
      }
      out.addAll(this.caveBiomes);
      return out.stream();
   }

   @Override
   public Holder<net.minecraft.world.level.biome.Biome> getNoiseBiome(int qx, int qy, int qz, Climate.Sampler sampler) {
      LegacyGenerator g = this.generator();
      if (qy < 0) {
         Holder<net.minecraft.world.level.biome.Biome> modern = this.underground.getNoiseBiome(qx, qy, qz, this.undergroundSampler);
         if (this.caveBiomes.contains(modern)) {
            return modern;
         }
      }
      Biome b;
      synchronized (LegacyGenerator.LOCK) {
         b = g.getBiome(QuartPos.toBlock(qx), QuartPos.toBlock(qz));
      }
      return this.holders.get(b);
   }
}
