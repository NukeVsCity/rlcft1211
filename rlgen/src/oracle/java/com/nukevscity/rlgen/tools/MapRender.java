package com.nukevscity.rlgen.tools;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

/** Top-down map of the ported 1.12 terrain: args seed centerX centerZ sizeBlocks out.png [vanilla]. */
public final class MapRender {
   public static void main(String[] args) throws Exception {
      long seed = Long.parseLong(args[0]);
      int cx = Integer.parseInt(args[1]);
      int cz = Integer.parseInt(args[2]);
      int size = Integer.parseInt(args[3]);
      boolean vanilla = args.length > 5 && args[5].equals("vanilla");
      LegacyGenerator gen = new LegacyGenerator(seed, vanilla ? BiomeLists.vanilla() : BiomeLists.rlcraft());
      Map<LBlock, Integer> col = new EnumMap<>(LBlock.class);
      col.put(LBlock.GRASS, 0x5FA040);
      col.put(LBlock.DIRT, 0x866043);
      col.put(LBlock.COARSE_DIRT, 0x77553B);
      col.put(LBlock.PODZOL, 0x5A3D1F);
      col.put(LBlock.STONE, 0x8A8A8A);
      col.put(LBlock.GRAVEL, 0x857F7E);
      col.put(LBlock.SAND, 0xDBD3A0);
      col.put(LBlock.RED_SAND, 0xBE6621);
      col.put(LBlock.SANDSTONE, 0xD8CB9B);
      col.put(LBlock.WATER, 0x3F76E4);
      col.put(LBlock.ICE, 0x9EB8F8);
      col.put(LBlock.SNOW, 0xF4FCFC);
      col.put(LBlock.MYCELIUM, 0x6F6265);
      col.put(LBlock.HARDENED_CLAY, 0x985E43);
      col.put(LBlock.STAINED_HARDENED_CLAY, 0xD1B1A1);
      col.put(LBlock.ORANGE_STAINED_HARDENED_CLAY, 0xA15325);
      col.put(LBlock.TRAVERSE_RED_ROCK, 0xA0502D);
      col.put(LBlock.TRAVERSE_BLUE_ROCK, 0x5B6B7A);
      col.put(LBlock.DEFILED_GRASS, 0x5E3F6E);
      col.put(LBlock.DEFILED_DIRT, 0x4A3554);
      col.put(LBlock.DEFILED_SAND, 0x6B5470);
      col.put(LBlock.WATERLILY, 0x208030);
      col.put(LBlock.LAVA, 0xD25A12);
      col.put(LBlock.FLOWING_LAVA, 0xD25A12);
      BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
      int x0 = cx - size / 2;
      int z0 = cz - size / 2;
      int[] heights = new int[size * size];
      for (int chx = Math.floorDiv(x0, 16); chx <= Math.floorDiv(x0 + size - 1, 16); chx++) {
         for (int chz = Math.floorDiv(z0, 16); chz <= Math.floorDiv(z0 + size - 1, 16); chz++) {
            ChunkPrimer p = gen.generateChunk(chx, chz).primer;
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  int px = chx * 16 + x - x0;
                  int pz = chz * 16 + z - z0;
                  if (px < 0 || pz < 0 || px >= size || pz >= size) {
                     continue;
                  }
                  int y = p.topNonAir(x, z);
                  LBlock b = y < 0 ? LBlock.AIR : p.getBlockState(x, y, z);
                  heights[px + pz * size] = y;
                  int c = col.getOrDefault(b, 0xFF00FF);
                  if (b == LBlock.WATER) {
                     int d = y;
                     while (d > 0 && p.getBlockState(x, d, z) == LBlock.WATER) {
                        d--;
                     }
                     float f = Math.max(0.35F, 1.0F - (y - d) / 40.0F);
                     c = shade(c, f);
                  }
                  img.setRGB(px, pz, c);
               }
            }
         }
      }
      // hill shading + height tint
      for (int z = 1; z < size; z++) {
         for (int x = 0; x < size; x++) {
            int c = img.getRGB(x, z) & 0xFFFFFF;
            int h = heights[x + z * size];
            int hn = heights[x + (z - 1) * size];
            float f = 1.0F + Math.max(-0.25F, Math.min(0.25F, (h - hn) * 0.06F));
            f *= 0.75F + Math.min(1.0F, Math.max(0.0F, (h - 50) / 160.0F)) * 0.45F;
            img.setRGB(x, z, shade(c, f));
         }
      }
      ImageIO.write(img, "png", new File(args[4]));
   }

   private static int shade(int c, float f) {
      int r = Math.min(255, (int)(((c >> 16) & 255) * f));
      int g = Math.min(255, (int)(((c >> 8) & 255) * f));
      int b = Math.min(255, (int)((c & 255) * f));
      return r << 16 | g << 8 | b;
   }
}
