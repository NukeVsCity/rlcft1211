package com.nukevscity.rlgen.legacy.biome;

public class BiomeOcean extends Biome {
   public BiomeOcean(Biome.BiomeProperties p) {
      super(p);
   }

   @Override
   public Biome.TempCategory getTempCategory() {
      return Biome.TempCategory.OCEAN;
   }
}
