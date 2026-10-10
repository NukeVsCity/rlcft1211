package greymerk.roguelike.worldgen;

import com.nukevscity.rlgen.legacy.biome.Biome;

public interface PositionInfo {
   int getDimension();

   Biome getBiome();
}
