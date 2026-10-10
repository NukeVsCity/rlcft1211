package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.LBlock;

public class BiomeBeach extends Biome {
   public BiomeBeach(Biome.BiomeProperties p) {
      super(p);
      this.topBlock = LBlock.SAND;
      this.fillerBlock = LBlock.SAND;
   }
}
