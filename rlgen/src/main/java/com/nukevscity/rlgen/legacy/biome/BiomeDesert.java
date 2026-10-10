package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.LBlock;

public class BiomeDesert extends Biome {
   public BiomeDesert(Biome.BiomeProperties p) {
      super(p);
      this.topBlock = LBlock.SAND;
      this.fillerBlock = LBlock.SAND;
   }
}
