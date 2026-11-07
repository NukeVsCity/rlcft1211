package com.nukevscity.rlgen.deco;

import java.util.Random;
import net.minecraft.core.BlockPos;

/**
 * The Forge 1.12 DecorateBiomeEvent.Decorate handlers RLCraft had that draw from the decoration random or deny a type. getPos() of the
 * event is the chunk corner (cx * 16, cz * 16), even for the events that carry a placement position.
 * <ul>
 * <li>Dynamic Trees TreeGenCancelEventHandler: DENY for TREE, BIG_SHROOM and CACTUS (vanillaCactusWorldGen=false) where the biome at
 * getPos() cancels vanilla trees: every minecraft:* biome (default populator "cancelvanilla") and every traverse:* biome (DT Traverse).
 * Defiled Lands' DT add-on denies TREE in its biomes only.</li>
 * <li>Recurrent Complex RCTerrainGenEventHandler (decorationHacks): for TREE, BIG_SHROOM, CACTUS, DESERT_WELL, FOSSIL, unless the biome
 * at getPos() + 16 is a roofed forest (Biomes.ROOFED_FOREST / MUTATED_ROOFED_FOREST: then ALLOW, and RC's handler, registered in
 * preInit after DT's (mod order), overrides Dynamic Trees' DENY): VanillaDecorationAdapter.amount, which rolls
 * nextFloat (TREE), nextInt(100) (DESERT_WELL) or nextInt(64) (FOSSIL); then baseWeight_tree 0 and no decoration structures of the
 * other types give up. BIG_SHROOM rolls nextDouble per vanilla big mushroom (RcDecoration).</li>
 * <li>Quark MushroomsInSwamps: only events with a placement position (roofed forest addMushrooms), in swamps at that position.</li>
 * <li>Charm VillageDecorations: the first FLOWERS event of a village chunk ({@link LegacyPopulator#flowersEvent}).</li>
 * </ul>
 * Events nobody handles that draw or deny: none (GRASS, ROCK, PUMPKIN, LAKE_*, SAND, CLAY, REED, SHROOM, LILYPAD, DEAD_BUSH, ICE).
 */
public final class DecorateEvents {
   private DecorateEvents() {
   }

   /** Dynamic Trees cancels vanilla tree generation in the biome at the chunk corner. */
   static boolean dtCancels(DWorld w, BlockPos corner) {
      String n = w.biome(corner).getRegistryName();
      return !n.contains(":") || n.startsWith("traverse:");
   }

   /** The biome RC looks at (getPos() + 16) is a roofed forest: RC allows the event and draws nothing (vanilla trees then grow). */
   static boolean roofedForest(DWorld w, BlockPos corner) {
      String n = w.biome(corner.offset(16, 0, 16)).getRegistryName();
      return n.equals("roofed_forest") || n.equals("mutated_roofed_forest");
   }

   /**
    * BIG_SHROOM from the decorator (no placement position): how many vanilla big mushrooms grow. RC's handler runs after DT's: in a roofed
    * forest it allows them all; otherwise RcDecoration rolls its structures, and when it changed nothing DT's result stands, else RC
    * generates the remaining vanilla ones itself (same draws as the decorator loop) and denies.
    */
   static int bigShroom(DWorld w, Random r, net.minecraft.world.level.ChunkPos cp, com.nukevscity.rlgen.legacy.biome.Biome biome,
                        BlockPos corner, int count) {
      if (roofedForest(w, corner)) {
         return count;
      }
      int left = com.nukevscity.rlgen.rc.RcDecoration.decorateBigMushrooms(w, r, cp, biome, count);
      if (left == count || left < 0) {
         return dtCancels(w, corner) ? 0 : count;
      }
      return left;
   }

   /**
    * TREE outside a roofed forest: RC re-rolls the extra-tree chance; Dynamic Trees (or Defiled Lands' add-on) denies it, so no vanilla
    * tree grows. In a roofed forest the caller grows the vanilla tree ({@link VanillaTrees}).
    */
   static void tree(DWorld w, Random r, BlockPos corner) {
      if (!roofedForest(w, corner)) {
         r.nextFloat();
      }
   }

   /** DESERT_WELL: RC's nextInt(100). */
   static void desertWell(DWorld w, Random r, BlockPos corner) {
      if (!roofedForest(w, corner)) {
         r.nextInt(100);
      }
   }

   /** FOSSIL: RC's nextInt(64). */
   static void fossil(DWorld w, Random r, BlockPos corner) {
      if (!roofedForest(w, corner)) {
         r.nextInt(64);
      }
   }

   /** BIG_SHROOM or CACTUS without a placement position: allowed unless Dynamic Trees denies it (RC allows it in mushroom islands). */
   static boolean bigShroomOrCactus(DWorld w, BlockPos corner) {
      return roofedForest(w, corner) || !dtCancels(w, corner);
   }

   /**
    * BIG_SHROOM with a placement position (BiomeForest.addMushrooms): Quark's swamp mushrooms (0.5 per event) take it when the biome there
    * is a swamp; returns whether the vanilla big mushroom may still grow.
    */
   static boolean bigShroomAt(DWorld w, Random r, BlockPos corner, BlockPos placement) {
      String n = w.biome(placement).getRegistryName();
      boolean quark = false;
      if ((n.equals("swampland") || n.equals("mutated_swampland")) && !(r.nextDouble() > 0.5)) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         Gen.bigMushroom(w, r, w.height(placement.offset(x, 0, z)), null);
         quark = true;
      }
      return !quark && bigShroomOrCactus(w, corner);
   }
}
