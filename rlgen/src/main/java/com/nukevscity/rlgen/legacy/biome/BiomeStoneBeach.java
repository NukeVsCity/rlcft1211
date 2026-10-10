package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.LBlock;

public class BiomeStoneBeach extends Biome {
   public BiomeStoneBeach(Biome.BiomeProperties p) {
      super(p);
      this.topBlock = LBlock.STONE;
      this.fillerBlock = LBlock.STONE;
   }
}
