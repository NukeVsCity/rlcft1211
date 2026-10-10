package com.nukevscity.rlgen.legacy.biome;

public class BiomeSavanna extends Biome {
   public BiomeSavanna(Biome.BiomeProperties p) {
      super(p);
   }

   @Override
   public Class<? extends Biome> getBiomeClass() {
      return BiomeSavanna.class;
   }
}
