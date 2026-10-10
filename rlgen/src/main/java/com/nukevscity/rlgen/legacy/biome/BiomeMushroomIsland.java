package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.LBlock;

public class BiomeMushroomIsland extends Biome {
   public BiomeMushroomIsland(Biome.BiomeProperties p) {
      super(p);
      this.topBlock = LBlock.MYCELIUM;
   }
}
