package com.nukevscity.rlgen.village;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.world.PopulateStarts;
import java.util.Random;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * The VILLAGE step of 1.12 populate: MapGenVillage.generateStructure builds, for every start whose box meets the populate area (in
 * structureMap order), each component meeting it, in list order, with the shared populate random (StructureStart.generateStructure).
 * The box is the 1.12 one: the populate area, Y 1 to 512.
 */
public final class VillagePopulate {
   private VillagePopulate() {
   }

   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      PopulateStarts.Starts starts = PopulateStarts.get(cp);
      if (starts == null || starts.villages().isEmpty()) {
         return;
      }
      int x = cp.x * 16 + 8;
      int z = cp.z * 16 + 8;
      BoundingBox area = new BoundingBox(x, 1, z, x + 15, 512, z + 15);
      for (StructureStart start : starts.villages()) {
         for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof VillagePieces.Village v && piece.getBoundingBox().intersects(area)) {
               v.place(w.level, rand, area, starts.generator());
            }
         }
      }
   }
}
