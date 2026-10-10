package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.RLGen;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Defiled Lands BiomeDefiled.decorate: defiled dungeons, altars, vilespine, blastem, scuronotte and the corruption of every column. */
final class DefiledDeco extends BiomeDeco {
   /** DungeonDefiledList defaults (Defiled Lands' own mobs become their closest vanilla look-alikes). */
   private static final EntityType<?>[] MOBS = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.HUSK, EntityType.STRAY};

   int vilespinePerChunk = 50;
   int scuronottePerChunk = 2;

   static BiomeDeco create(String name) {
      DefiledDeco d = new DefiledDeco();
      switch (name) {
         case "defiledlands:desert_defiled" -> d.treesPerChunk = -999;
         case "defiledlands:forest_tenebra" -> d.treesPerChunk = 10;
         case "defiledlands:forest_vilespine" -> {
            d.treesPerChunk = 0;
            d.extraTreeChance = 0.05F;
            d.vilespinePerChunk = 500;
         }
         case "defiledlands:plains_defiled" -> {
            d.treesPerChunk = 0;
            d.extraTreeChance = 0.05F;
         }
         case "defiledlands:swamp_defiled" -> {
            d.treesPerChunk = 2;
            d.clayPerChunk = 1;
            d.waterlilyPerChunk = 4;
            d.sandPatchesPerChunk = 0;
            d.gravelPatchesPerChunk = 0;
            d.scuronottePerChunk = 8;
         }
         default -> {
         }
      }
      return d;
   }

   @Override
   void pre(DWorld w, Random r, BlockPos pos, Counts c) {
      BlockState bricks = RLGen.STONE_BRICKS_DEFILED.get().defaultBlockState();
      BlockState cracked = RLGen.STONE_BRICKS_DEFILED_CRACKED.get().defaultBlockState();
      for (int i = 0; i < 80; i++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int y = r.nextInt(256);
         BlockPos at = pos.offset(x, y, z);
         if (Gen.dungeon(w, r, at, bricks, null, cracked, Gen.loot("minecraft:chests/simple_dungeon"), MOBS, true)) {
            scuronotte(w, r, at);
         }
      }
   }

   @Override
   void post(DWorld w, Random r, BlockPos pos) {
      if (w.chunkRandom(pos, 119432789L).nextInt(50) == 0) {
         int i = r.nextInt(16) + 8;
         int j = r.nextInt(16) + 8;
         altar(w, r, w.height(pos.offset(i, 0, j)).above());
      }
      for (int j5 = 0; j5 < this.vilespinePerChunk; j5++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            vilespine(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      int i = r.nextInt(16) + 8;
      int j = r.nextInt(16) + 8;
      int height = Math.max(1, w.height(pos.getX() + i, pos.getZ() + j) * 2);
      blastem(w, r, pos.offset(i, r.nextInt(height), j));
      for (int l3 = 0; l3 < this.scuronottePerChunk; l3++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         scuronotte(w, r, w.height(pos.offset(x, 0, z)));
      }
      for (int x = 8; x < 24; x++) {
         for (int z = 8; z < 24; z++) {
            BlockPos top = w.height(pos.offset(x, 0, z));
            if (BiomeDeco.of(w.biome(top)) instanceof DefiledDeco) {
               for (BlockPos p = top; p.getY() > 0; p = p.below()) {
                  BlockState out = corrupted(w.get(p));
                  if (out != null) {
                     w.set(p, out);
                  }
               }
            }
         }
      }
   }

   private static Map<Block, BlockState> corruption;

   /** CorruptionRecipes */
   static BlockState corrupted(BlockState s) {
      Map<Block, BlockState> m = corruption;
      if (m == null) {
         BlockState stone = RLGen.STONE_DEFILED.get().defaultBlockState();
         m = Map.ofEntries(
            Map.entry(Blocks.STONE, stone), Map.entry(Blocks.ANDESITE, stone), Map.entry(Blocks.DIORITE, stone), Map.entry(Blocks.GRANITE, stone),
            Map.entry(Blocks.COBBLESTONE, stone), Map.entry(Blocks.SAND, RLGen.SAND_DEFILED.get().defaultBlockState()),
            Map.entry(Blocks.SANDSTONE, RLGen.SANDSTONE_DEFILED.get().defaultBlockState()), Map.entry(Blocks.DIRT, RLGen.DIRT_DEFILED.get().defaultBlockState()),
            Map.entry(Blocks.GRASS_BLOCK, RLGen.GRASS_DEFILED.get().defaultBlockState()), Map.entry(Blocks.GRAVEL, RLGen.GRAVEL_DEFILED.get().defaultBlockState()),
            Map.entry(Blocks.STONE_BRICKS, RLGen.STONE_BRICKS_DEFILED.get().defaultBlockState()),
            Map.entry(Blocks.CRACKED_STONE_BRICKS, RLGen.STONE_BRICKS_DEFILED_CRACKED.get().defaultBlockState()),
            Map.entry(Blocks.MOSSY_STONE_BRICKS, RLGen.STONE_BRICKS_DEFILED_MOSSY.get().defaultBlockState()),
            Map.entry(Blocks.MOSSY_COBBLESTONE, RLGen.STONE_DEFILED_MOSSY.get().defaultBlockState()),
            Map.entry(Blocks.COAL_ORE, RLGen.HEPHAESTITE_ORE.get().defaultBlockState()), Map.entry(Blocks.IRON_ORE, RLGen.UMBRIUM_ORE.get().defaultBlockState()),
            Map.entry(Blocks.DIAMOND_ORE, RLGen.SCARLITE_ORE.get().defaultBlockState()));
         corruption = m;
      }
      return m.get(s.getBlock());
   }

   /** WorldGenVilespine */
   static void vilespine(DWorld w, Random r, BlockPos pos) {
      BlockState spine = RLGen.VILESPINE.get().defaultBlockState();
      for (int i = 0; i < 10; i++) {
         BlockPos b = Gen.scatter(pos, r);
         if (w.isAir(b)) {
            int j = 1 + r.nextInt(r.nextInt(7) + 1);
            for (int k = 0; k < j; k++) {
               if (spine.canSurvive(w.level, b) && w.inRegion(b)) {
                  if (k > 0) {
                     w.set(b.above(k - 1), spine.setValue(com.nukevscity.rlgen.block.VilespineBlock.TOPMOST, false));
                  }
                  w.set(b.above(k), spine);
               }
            }
         }
      }
   }

   /** WorldGenBlastem (fully grown) */
   static void blastem(DWorld w, Random r, BlockPos pos) {
      BlockState b = RLGen.BLASTEM.get().defaultBlockState();
      for (int i = 0; i < 64; i++) {
         BlockPos p = Gen.scatter(pos, r);
         if (Gen.canStay(w, p, b) && w.isAir(p)) {
            w.set(p, b);
         }
      }
   }

   /** WorldGenScuronotte */
   static void scuronotte(DWorld w, Random r, BlockPos pos) {
      BlockState s = RLGen.SCURONOTTE.get().defaultBlockState();
      for (int i = 0; i < 8; i++) {
         BlockPos p = Gen.scatter(pos, r);
         if (w.isAir(p) && Gen.canStay(w, p, s)) {
            w.set(p, s);
         }
      }
   }

   /** WorldGenConjuringAltar */
   static void altar(DWorld w, Random r, BlockPos pos) {
      if (!(BiomeDeco.of(w.biome(pos)) instanceof DefiledDeco)) {
         return;
      }
      while (w.isAir(pos) && pos.getY() > 2) {
         pos = pos.below();
      }
      for (int i = -2; i <= 2; i++) {
         for (int j = -2; j <= 2; j++) {
            if (w.isAir(pos.offset(i, -1, j)) && w.isAir(pos.offset(i, -2, j))) {
               return;
            }
         }
      }
      BlockState sandstone = RLGen.SANDSTONE_DEFILED.get().defaultBlockState();
      for (int l1 = -2; l1 <= 2; l1++) {
         for (int k = -2; k <= 2; k++) {
            w.set(pos.offset(l1, -1, k), brick(r));
            w.set(pos.offset(l1, 0, k), l1 <= 1 && l1 >= -1 && k <= 1 && k >= -1 ? sandstone : brick(r));
         }
      }
      for (int i = 1; i <= 3; i++) {
         for (int l1 = -2; l1 <= 2; l1++) {
            for (int kx = -2; kx <= 2; kx++) {
               if (l1 != -2 && l1 != 2 || kx != -2 && kx != 2) {
                  w.set(pos.offset(l1, i, kx), Blocks.AIR.defaultBlockState());
               } else {
                  w.set(pos.offset(l1, i, kx), brick(r));
               }
            }
         }
      }
      w.set(pos.offset(0, 1, 0), RLGen.CONJURING_ALTAR.get().defaultBlockState());
   }

   private static BlockState brick(Random r) {
      return r.nextInt(4) == 0 ? RLGen.STONE_BRICKS_DEFILED_CRACKED.get().defaultBlockState() : RLGen.STONE_BRICKS_DEFILED.get().defaultBlockState();
   }
}
