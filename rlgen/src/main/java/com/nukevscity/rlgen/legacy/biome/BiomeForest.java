package com.nukevscity.rlgen.legacy.biome;

public class BiomeForest extends Biome {
   public BiomeForest(Biome.BiomeProperties p) {
      super(p);
   }

   @Override
   public Class<? extends Biome> getBiomeClass() {
      return BiomeForest.class;
   }
}
