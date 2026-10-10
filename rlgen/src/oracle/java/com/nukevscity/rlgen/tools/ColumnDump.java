package com.nukevscity.rlgen.tools;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;

/** Prints the ported 1.12 chunk as "x y z id" lines: args seed chunkX chunkZ. */
public final class ColumnDump {
   public static void main(String[] args) {
      LegacyGenerator gen = new LegacyGenerator(Long.parseLong(args[0]), BiomeLists.rlcraft());
      ChunkPrimer p = gen.generateChunk(Integer.parseInt(args[1]), Integer.parseInt(args[2])).primer;
      StringBuilder sb = new StringBuilder();
      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 0; y < 256; y++) {
               sb.append(x).append(' ').append(y).append(' ').append(z).append(' ').append(p.getBlockState(x, y, z).modernId).append('\n');
            }
         }
      }
      System.out.print(sb);
   }
}
