package com.nukevscity.rlgen.bms;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.world.PopulateStarts;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureBoundingBox;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureComponent;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.slf4j.Logger;

/**
 * The MINESHAFT step of 1.12 populate: MapGenBetterMineshaft.generateStructure builds, for every start whose box meets the populate area
 * (in structureMap order), each component meeting it, in list order, with the shared populate random (StructureStart.generateStructure).
 * The whole populate area is written at once; it lies inside the 1.21 region of the decorated chunk.
 */
public final class MineshaftPopulate {
   private static final Logger LOGGER = LogUtils.getLogger();

   private MineshaftPopulate() {
   }

   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      PopulateStarts.Starts starts = PopulateStarts.get(cp);
      if (starts == null || starts.mineshafts().isEmpty()) {
         return;
      }
      long seed = w.seed();
      StructureBoundingBox area = new StructureBoundingBox(cp.x * 16 + 8, cp.z * 16 + 8, cp.x * 16 + 23, cp.z * 16 + 23);
      World world = new World(w.level, starts.generator(), cp, true);
      try {
         for (StructureStart start : starts.mineshafts()) {
            List<StructureComponent> components = MineshaftStructure.start(starts.generator(), seed, start.getChunkPos().x, start.getChunkPos().z);
            if (components == null) {
               continue;
            }
            for (StructureComponent c : components) {
               // a component returning false was removed from the start in 1.12 (later populate areas skip it); not kept here
               if (c.getBoundingBox().intersectsWith(area)) {
                  c.addComponentParts(world, rand, area);
               }
            }
         }
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: mineshaft populate failed in chunk {}", cp, e);
      }
      MineshaftHolderPiece.fixShapes(w.level, world.flush());
   }
}
