package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.legacy.LegacyGenerator;
import java.util.Random;
import net.minecraft.world.level.ChunkPos;

/**
 * 1.12 ChunkGeneratorOverworld.populate calls, right after the Recurrent Complex draws (PopulateChunkEvent.Pre) and before the
 * lakes: mineshaft, village, stronghold, scatteredFeature, oceanMonument, woodlandMansion .generateStructure(world, rand, chunkPos).
 * Each one builds the pieces of its structure starts that intersect the populate box (cx*16+8 .. +23) with the shared populate
 * random, so both the blocks and the random's state afterwards (lakes, dungeons, decoration) match 1.12.
 * Each structure port sets its own step below; NONE draws nothing.
 */
public final class LegacyStructures {
   private LegacyStructures() {
   }

   @FunctionalInterface
   public interface Step {
      void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy);
   }

   public static final Step NONE = (w, rand, cp, legacy) -> {
   };

   // ---- mineshaft (Better Mineshafts replaces MapGenMineshaft)
   static Step MINESHAFT = NONE;

   // ---- village (MapGenVillage)
   static Step VILLAGE = NONE;

   // ---- stronghold (MapGenStronghold)
   static Step STRONGHOLD = NONE;

   // ---- scattered features: desert pyramid, jungle temple, witch hut, igloo (MapGenScatteredFeature)
   static Step SCATTERED = NONE;

   // ---- ocean monument (StructureOceanMonument)
   static Step MONUMENT = NONE;

   // ---- woodland mansion (WoodlandMansion)
   static Step MANSION = com.nukevscity.rlgen.vanilla.mansion.MansionGen::generate;

   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      MINESHAFT.generate(w, rand, cp, legacy);
      VILLAGE.generate(w, rand, cp, legacy);
      STRONGHOLD.generate(w, rand, cp, legacy);
      SCATTERED.generate(w, rand, cp, legacy);
      MONUMENT.generate(w, rand, cp, legacy);
      MANSION.generate(w, rand, cp, legacy);
   }
}
