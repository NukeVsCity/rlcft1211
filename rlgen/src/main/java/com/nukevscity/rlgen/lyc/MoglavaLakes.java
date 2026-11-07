package com.nukevscity.rlgen.lyc;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.deco.Gen;
import com.nukevscity.rlgen.quark.QuarkGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Lycanites Mobs WorldGenMoglavaLakes with RLCraft's worldgen.cfg (surface 0, underground 0.01, dimensions "-1" whitelist), on the
 * 1.21 nether (biome modifier rlgen:moglava_lakes). 1.12 ran it from WorldGeneratorFluids after ooze, poison and acid, which are
 * blacklisted in the nether, so the FML chunk random is fresh. Only in worlds whose overworld is the 1.12 one.
 */
public final class MoglavaLakes extends Feature<NoneFeatureConfiguration> {
   private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, RLGen.MODID);
   private static final double UNDERGROUND = 0.01;

   static {
      FEATURES.register("moglava_lakes", MoglavaLakes::new);
   }

   private MoglavaLakes() {
      super(NoneFeatureConfiguration.CODEC);
   }

   static void register(IEventBus modBus) {
      FEATURES.register(modBus);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!(context.level().getLevel().getServer().overworld().getChunkSource().getGenerator() instanceof LegacyChunkGenerator)) {
         return false;
      }
      ChunkPos cp = new ChunkPos(context.origin());
      DWorld w = new DWorld(context.level(), context.level().getSeed(), cp);
      Random random = QuarkGen.fmlRandom(w.seed(), cp.x, cp.z);
      // onWorldGen: the surface chance is 0, so no draw for it
      if (random.nextDouble() <= UNDERGROUND) {
         int x = cp.x * 16 + 16;
         int z = cp.z * 16 + 16;
         int top = Math.max(1, topSolidOrLiquid(w, x, z) - 10);
         int y = random.nextInt(top);
         return Gen.lake(w, random, new BlockPos(x, y, z), LycFluids.MOGLAVA.block.get().defaultBlockState(), true);
      }
      return false;
   }

   /** World.getTopSolidOrLiquidBlock: from the top of the chunk's highest filled section down to above a block that blocks movement. */
   private static int topSolidOrLiquid(DWorld w, int x, int z) {
      int top = 0;
      for (int y = 255; y >= 0; y--) {
         if (!w.get(new BlockPos(x, y, z)).isAir()) {
            top = (y & ~15) + 16;
            break;
         }
      }
      BlockPos pos = new BlockPos(x, top, z);
      while (pos.getY() >= 0) {
         BlockPos below = pos.below();
         var s = w.get(below);
         if (DWorld.blocksMovement(s) && !DWorld.isLeaves(s) && !DWorld.isFoliage(s)) {
            break;
         }
         pos = below;
      }
      return pos.getY();
   }
}
