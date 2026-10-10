package jaredbgreat.dldungeons.builder;

import jaredbgreat.dldungeons.compat.World;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import jaredbgreat.dldungeons.util.debug.Logging;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

public final class RegisteredBlock extends AbstractBlock {
   private final String id;
   private final IBlockPlacer block;
   public static final Block quartz = Blocks.QUARTZ_BLOCK;
   public static final Block lapis = Blocks.LAPIS_BLOCK;
   public static final Block water = Blocks.WATER;
   public static final Block air = Blocks.AIR;
   public static final ArrayList<RegisteredBlock> registry = new ArrayList<>();
   private static HashSet<String> names = new HashSet<>();

   private RegisteredBlock(String id) throws NoSuchElementException {
      names.add(id);
      this.id = id;
      this.block = DBlock.makeDBlock(id);
      if (this.block.toString().contains("minecraft:air") && !id.contains("minecraft:air")) {
         String error = "[DLDUNGEONS] ERROR! Block read as \"" + id + "\" parsed into an air block!";
         Logging.logError(error);
         throw new NoSuchElementException(error);
      }
   }

   public static void listDBlocks(File listsDir) {
      ArrayList<String> namel = new ArrayList<>();
      names.forEach(namex -> namel.add(namex));
      Collections.sort(namel);
      File itemlist = new File(listsDir.toString() + File.separator + "DBlocks.txt");
      if (itemlist.exists()) {
         itemlist.delete();
      }

      try {
         BufferedWriter outstream = new BufferedWriter(new FileWriter(itemlist.toString()));

         for (String name : namel) {
            outstream.write(name);
            outstream.newLine();
         }

         if (outstream != null) {
            outstream.close();
         }
      } catch (IOException var6) {
         System.err.println("Error: Could not write file blocks.txt");
         var6.printStackTrace();
      }
   }

   private RegisteredBlock(BlockFamily family) throws NoSuchElementException {
      this.id = family.getName();
      this.block = family;
   }

   public static void place(World world, int x, int y, int z, int block) {
      if (!isProtectedBlock(world, x, y, z)) {
         registry.get(block).place(world, x, y, z);
      }
   }

   public static int add(String id) throws NoSuchElementException {
      if (id.startsWith("$")) {
         return add(BlockFamily.getBlockFamily(id));
      } else {
         RegisteredBlock block = new RegisteredBlock(id);
         if (!registry.contains(block)) {
            registry.add(block);
         }

         return registry.indexOf(block);
      }
   }

   public static int add(BlockFamily family) throws NoSuchElementException {
      RegisteredBlock blocks = new RegisteredBlock(family);
      if (!registry.contains(blocks)) {
         registry.add(blocks);
      }

      return registry.indexOf(blocks);
   }

   public static RegisteredBlock get(int id) throws NoSuchElementException {
      return registry.get(id);
   }

   public static IBlockPlacer getPlacer(int id) throws NoSuchElementException {
      return registry.get(id);
   }

   public static boolean placeBlock(World world, int x, int y, int z, Block block) {
      if (isProtectedBlock(world, x, y, z)) {
         return false;
      } else {
         world.setBlockState(new BlockPos(x, y, z), block.defaultBlockState());
         return true;
      }
   }

   public static void placeBlock(World world, int x, int y, int z, Block block, int a, int b) {
      if (!isProtectedBlock(world, x, y, z)) {
         world.setBlockState(new BlockPos(x, y, z), block.defaultBlockState());
      }
   }

   /** rlgen: a 1.12 block name with metadata (ladders facing, top slabs). */
   public static void placeBlock(World world, int x, int y, int z, String block, int a, int b) {
      BlockState state = com.nukevscity.rlgen.legacy.LegacyStates.block(block, a);
      if (state != null && !isProtectedBlock(world, x, y, z)) {
         world.setBlockState(new BlockPos(x, y, z), state);
      }
   }

   public static void deleteBlock(World world, int x, int y, int z) {
      if (!isProtectedBlock(world, x, y, z)) {
         world.setBlockToAir(new BlockPos(x, y, z));
      }
   }

   public static void deleteBlock(World world, int x, int y, int z, boolean flooded) {
      if (!isProtectedBlock(world, x, y, z)) {
         if (flooded) {
            placeBlock(world, x, y, z, water);
         } else {
            world.setBlockToAir(new BlockPos(x, y, z));
         }
      }
   }

   public static void deleteBlock(World world, int x, int y, int z, int block) {
      if (!isProtectedBlock(world, x, y, z)) {
         if (block > 0) {
            registry.get(block).place(world, x, y, z);
         } else {
            world.setBlockToAir(new BlockPos(x, y, z));
         }
      }
   }

   public static void placeChest(World world, int x, int y, int z) {
      placeBlock(world, x, y, z, chest);
   }

   public static void placeSpawner(World world, int x, int y, int z, String mob) {
      BlockPos pos = new BlockPos(x, y, z);
      if (!isProtectedBlock(world, x, y, z)) {
         if (placeBlock(world, x, y, z, spawner)) {
            world.setSpawner(pos, mob);
         }
      }
   }

   /** rlgen: 1.12 materials grass, iron, ground, sand, rock and clay; 1.21 has no materials, so pickaxe and shovel blocks stand in. */
   public static boolean isGroundBlock(World world, int x, int y, int z) {
      if (y < 0) {
         return true;
      }
      BlockState bs = world.getBlockState(new BlockPos(x, y, z));
      if (bs.isAir() || bs.is(BlockTags.ICE) || bs.is(BlockTags.SNOW) || bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK)) {
         return false;
      }
      return bs.is(BlockTags.MINEABLE_WITH_SHOVEL) || bs.is(BlockTags.MINEABLE_WITH_PICKAXE) && bs.isSolid();
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof RegisteredBlock) ? false : this.block.equals(((RegisteredBlock)other).block);
   }

   @Override
   public int hashCode() {
      return this.block.hashCode();
   }

   @Override
   public void placeNoMeta(World world, int x, int y, int z) {
      this.block.placeNoMeta(world, x, y, z);
   }

   @Override
   public void place(World world, int x, int y, int z) {
      this.block.place(world, x, y, z);
   }

   @Override
   public Object getContents() {
      return this.block;
   }
}
