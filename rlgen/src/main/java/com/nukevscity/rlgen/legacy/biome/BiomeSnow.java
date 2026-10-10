package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.LBlock;

public class BiomeSnow extends Biome {
   public BiomeSnow(boolean flag, Biome.BiomeProperties p) {
      super(p);
      if (flag) {
         this.topBlock = LBlock.SNOW;
      }
   }
}
