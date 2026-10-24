package com.yungnickyoung.minecraft.bettercaves.mc;

import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;

/** rlgen: 1.12 World as Better Caves reads it, over the legacy generator. */
public final class World {
   public final LegacyWorld legacy;

   public World(LegacyWorld legacy) {
      this.legacy = legacy;
   }

   public long getSeed() {
      return this.legacy.getSeed();
   }

   public int getSeaLevel() {
      return this.legacy.getSeaLevel();
   }

   public Biome getBiome(BlockPos pos) {
      return this.legacy.getBiome(pos.getX(), pos.getZ());
   }
}
