package com.nukevscity.rlgen.oracle;

import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;
import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.storage.WorldInfo;

/**
 * Runs the real 1.12.2 (vanilla, no Forge) generator next to the port and compares biome layers and
 * chunk blocks. Vanilla 1.12 equals Forge with no biome mods, so this uses BiomeLists.vanilla().
 */
public final class Oracle {
   static final class FakeWorld extends World {
      FakeWorld(WorldInfo info) {
         super(null, info, new WorldProviderSurface(), new net.minecraft.profiler.Profiler(), false);
         this.provider.setWorld(this);
      }

      @Override
      protected IChunkProvider createChunkProvider() {
         return null;
      }

      @Override
      protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
         return false;
      }

      @Override
      public Biome getBiome(BlockPos pos) {
         return this.provider.getBiomeProvider().getBiome(pos);
      }
   }

   public static void main(String[] args) throws Exception {
      Bootstrap.register();
      long[] seeds = {192984777546635623L, -3090796961423839953L, -3571440325882671706L, 12345L, 0L};
      int layerFails = 0;
      int blockFails = 0;
      Random r = new Random(42);
      for (long seed : seeds) {
         GenLayer[] real = GenLayer.initializeAllBiomeGenerators(seed, WorldType.DEFAULT, null);
         com.nukevscity.rlgen.legacy.layer.GenLayer[] mine =
            com.nukevscity.rlgen.legacy.layer.GenLayer.initializeAllBiomeGenerators(seed, BiomeLists.vanilla());
         for (int t = 0; t < 6; t++) {
            int x = r.nextInt(20000) - 10000;
            int z = r.nextInt(20000) - 10000;
            for (int layer = 0; layer < 2; layer++) {
               int[] a = real[layer].getInts(x, z, 64, 64).clone();
               int[] b = mine[layer].getInts(x, z, 64, 64);
               for (int i = 0; i < 64 * 64; i++) {
                  if (a[i] != b[i]) {
                     layerFails++;
                     if (layerFails < 10) {
                        System.out.println("LAYER " + layer + " seed " + seed + " @" + x + "," + z + " i=" + i + " real=" + a[i] + " mine=" + b[i]);
                     }
                     break;
                  }
               }
            }
         }

         WorldInfo info = new WorldInfo(new WorldSettings(seed, GameType.SURVIVAL, true, false, WorldType.DEFAULT), "oracle");
         FakeWorld world = new FakeWorld(info);
         ChunkGeneratorOverworld gen = new ChunkGeneratorOverworld(world, seed, true, "");
         Field randF = ChunkGeneratorOverworld.class.getDeclaredField("rand");
         randF.setAccessible(true);
         Random genRand = (Random)randF.get(gen);
         Field caveF = ChunkGeneratorOverworld.class.getDeclaredField("caveGenerator");
         caveF.setAccessible(true);
         Field ravF = ChunkGeneratorOverworld.class.getDeclaredField("ravineGenerator");
         ravF.setAccessible(true);
         LegacyGenerator port = new LegacyGenerator(seed, BiomeLists.vanilla());
         for (int t = 0; t < 40; t++) {
            int cx = r.nextInt(1000) - 500;
            int cz = r.nextInt(1000) - 500;
            genRand.setSeed((long)cx * 341873128712L + (long)cz * 132897987541L);
            ChunkPrimer primer = new ChunkPrimer();
            gen.setBlocksInChunk(cx, cz, primer);
            Biome[] biomes = world.getBiomeProvider().getBiomes(null, cx * 16, cz * 16, 16, 16);
            gen.replaceBiomeBlocks(cx, cz, primer, biomes);
            ((MapGenBase)caveF.get(gen)).generate(world, cx, cz, primer);
            ((MapGenBase)ravF.get(gen)).generate(world, cx, cz, primer);
            LegacyGenerator.GeneratedChunk mineChunk = port.generateChunk(cx, cz);
            int diff = 0;
            String first = null;
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  for (int y = 0; y < 256; y++) {
                     String a = name(primer.getBlockState(x, y, z));
                     String b = mineChunk.primer.getBlockState(x, y, z).modernId;
                     boolean forgeCaveTop = a.equals("minecraft:grass_block") && b.equals("minecraft:dirt")
                        || a.equals("minecraft:mycelium") && b.equals("minecraft:dirt");
                     if (!a.equals(b) && !forgeCaveTop) {
                        diff++;
                        if (first == null) {
                           first = x + "," + y + "," + z + " real=" + a + " mine=" + b;
                        }
                     }
                  }
               }
            }
            if (diff > 0) {
               blockFails++;
               if (blockFails < 15) {
                  System.out.println("CHUNK seed " + seed + " " + cx + "," + cz + " diffs=" + diff + " first " + first
                     + " biome=" + biomes[0].getBiomeName());
               }
            }
         }
      }
      System.out.println("layer mismatches: " + layerFails + ", chunk mismatches: " + blockFails);
   }

   private static String name(IBlockState s) {
      String n = net.minecraft.block.Block.REGISTRY.getNameForObject(s.getBlock()).getPath();
      int meta = s.getBlock().getMetaFromState(s);
      switch (n) {
         case "air": return "minecraft:air";
         case "grass": return "minecraft:grass_block";
         case "dirt": return meta == 1 ? "minecraft:coarse_dirt" : meta == 2 ? "minecraft:podzol" : "minecraft:dirt";
         case "sand": return meta == 1 ? "minecraft:red_sand" : "minecraft:sand";
         case "water": case "flowing_water": return "minecraft:water";
         case "lava": case "flowing_lava": return "minecraft:lava";
         case "snow": return "minecraft:snow_block";
         case "snow_layer": return "minecraft:snow";
         case "hardened_clay": return "minecraft:terracotta";
         case "stained_hardened_clay": {
            String[] c = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
            return "minecraft:" + c[meta] + "_terracotta";
         }
         case "waterlily": return "minecraft:lily_pad";
         default: return "minecraft:" + n;
      }
   }
}
