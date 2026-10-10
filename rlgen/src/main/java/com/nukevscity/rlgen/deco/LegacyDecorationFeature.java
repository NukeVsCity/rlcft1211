package com.nukevscity.rlgen.deco;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;

/** One step of the 1.12 population (placed once per chunk in every rlgen biome, see {@link LegacyPopulator}). */
public final class LegacyDecorationFeature extends Feature<NoneFeatureConfiguration> {
   private static final Logger LOGGER = LogUtils.getLogger();

   public enum Step {
      LAKES,
      ORES,
      PLANTS
   }

   private final Step step;

   public LegacyDecorationFeature(Step step) {
      super(NoneFeatureConfiguration.CODEC);
      this.step = step;
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator)) {
         return false;
      }
      ChunkPos cp = new ChunkPos(context.origin());
      DWorld w = new DWorld(context.level(), generator.legacyGenerator(), cp);
      try {
         switch (this.step) {
            case LAKES -> LegacyPopulator.lakes(w, cp, generator.legacyGenerator());
            case ORES -> LegacyPopulator.ores(w, cp);
            case PLANTS -> LegacyPopulator.plants(w, cp);
         }
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: 1.12 decoration ({}) failed in chunk {}", this.step, cp, e);
      }
      return true;
   }
}
