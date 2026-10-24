package com.nukevscity.rlgen.lyc;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.deco.Gen;
import com.nukevscity.rlgen.quark.QuarkGen;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;

/**
 * Lycanites Mobs WorldGeneratorFluids (IWorldGenerator weight 0) with RLCraft's worldgen.cfg: ooze, poison, acid lakes, each a WorldGenLakes
 * with a water-material fluid; surface 0.08 in matching biomes, underground 0.01 anywhere. Moglava is nether-only (whitelist -1).
 */
public final class FluidLakes {
   private static final double SURFACE = 0.08;
   private static final double UNDERGROUND = 0.01;

   private FluidLakes() {
   }

   public static void generate(DWorld w, int cx, int cz) {
      Random random = QuarkGen.fmlRandom(w.seed(), cx, cz);
      Set<String> types = BiomeDict.TYPES.getOrDefault(w.biome(new BlockPos(cx * 16, 0, cz * 16)).getRegistryName(), Set.of());
      // ooze: COLD or SNOWY, not HOT
      lakes(w, random, cx, cz, (types.contains("COLD") || types.contains("SNOWY")) && !types.contains("HOT"), LycFluids.OOZE);
      // poison: SWAMP or SPOOKY, not COLD/SNOWY
      lakes(w, random, cx, cz, (types.contains("SWAMP") || types.contains("SPOOKY")) && !types.contains("COLD") && !types.contains("SNOWY"), LycFluids.POISON);
      // acid: WASTELAND or DEAD, not COLD/SNOWY
      lakes(w, random, cx, cz, (types.contains("WASTELAND") || types.contains("DEAD")) && !types.contains("COLD") && !types.contains("SNOWY"), LycFluids.ACID);
   }

   private static void lakes(DWorld w, Random random, int cx, int cz, boolean typeValid, LycFluids.Kind kind) {
      int x = cx * 16 + 16;
      int z = cz * 16 + 16;
      if (typeValid && random.nextDouble() <= SURFACE) {
         Gen.lake(w, random, new BlockPos(x, random.nextInt(128), z), kind.block.get().defaultBlockState(), false);
      }
      if (random.nextDouble() <= UNDERGROUND) {
         int top = Math.max(1, w.topSolidOrLiquid(new BlockPos(x, 0, z)).getY() - 10);
         Gen.lake(w, random, new BlockPos(x, random.nextInt(top), z), kind.block.get().defaultBlockState(), false);
      }
   }
}
