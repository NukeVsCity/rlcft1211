package com.nukevscity.rlgen.legacy;

import com.nukevscity.rlgen.legacy.biome.Biome;

/** What the 1.12 generators read from net.minecraft.world.World. */
public interface LegacyWorld {
   long getSeed();

   default int getSeaLevel() {
      return 63;
   }

   /** World.getBiome(pos): the 1:1 (voronoi) biome at a block column. */
   Biome getBiome(int x, int z);
}
