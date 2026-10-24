package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 World as Ice and Fire's StructureGenerator saw it, for planning one chunk: reads fall through to the 1.12 terrain
 * (no decoration), writes are kept and recorded as operations that are replayed on the real world later (IafGen.apply).
 */
final class IafWorld {
   static final byte SET = 0;
   static final byte BURN = 1;
   static final byte FREEZE = 2;
   /** WorldGenMinable / isReplaceableOreGen: only if the block is stone then. */
   static final byte ORE = 3;
   static final byte CHEST = 4;
   /** A lily on top of the column, if it can be placed then. */
   static final byte LILY = 5;
   /** A 1.12 structure template placed with its min corner here (loot = template id, lootSeed = rotation * 4 + mirror). */
   static final byte TEMPLATE = 6;

   record Op(int x, int y, int z, byte kind, BlockState state, String loot, long lootSeed) {
   }

   private final LegacyChunkGenerator gen;
   private final Map<Long, BlockState> blocks = new HashMap<>();
   /** World.getHeight per column, kept up to date with the writes (1.12 kept its height map current). */
   private final Map<Long, Integer> heights = new HashMap<>();
   final List<Op> ops = new ArrayList<>();
   /** What was generated, for /rlgen iaf. */
   final List<String> found = new ArrayList<>();

   IafWorld(LegacyChunkGenerator gen) {
      this.gen = gen;
   }

   BlockState get(BlockPos p) {
      if (p.getY() < 0 || p.getY() > 255) {
         return Blocks.AIR.defaultBlockState();
      }
      BlockState s = this.blocks.get(p.asLong());
      return s != null ? s : this.gen.legacyState(p);
   }

   boolean isAir(BlockPos p) {
      return this.get(p).isAir();
   }

   private void put(BlockPos p, BlockState s) {
      this.blocks.put(p.asLong(), s);
      long col = ((long)p.getX() << 32) ^ (p.getZ() & 0xFFFFFFFFL);
      Integer h = this.heights.get(col);
      if (h != null) {
         if (!s.isAir() && p.getY() + 1 > h) {
            this.heights.put(col, p.getY() + 1);
         } else if (s.isAir() && p.getY() + 1 == h) {
            this.heights.remove(col);
         }
      }
   }

   void set(BlockPos p, BlockState s) {
      if (p.getY() >= 0 && p.getY() <= 255) {
         this.put(p, s);
         this.ops.add(new Op(p.getX(), p.getY(), p.getZ(), SET, s, null, 0L));
      }
   }

   void chest(BlockPos p, BlockState s, String loot, long seed) {
      if (p.getY() >= 0 && p.getY() <= 255) {
         this.put(p, s);
         this.ops.add(new Op(p.getX(), p.getY(), p.getZ(), CHEST, s, loot, seed));
      }
   }

   void convert(BlockPos p, boolean fire) {
      if (p.getY() < 0 || p.getY() > 255) {
         return;
      }
      BlockState s = this.get(p);
      BlockState n = fire ? Mat112.burn(s) : Mat112.freeze(s, s.is(Blocks.SNOW_BLOCK) && this.canSeeSky(p));
      if (n != null) {
         this.put(p, n);
      }
      this.ops.add(new Op(p.getX(), p.getY(), p.getZ(), fire ? BURN : FREEZE, null, null, 0L));
   }

   void ore(BlockPos p, BlockState ore) {
      if (p.getY() >= 0 && p.getY() <= 255) {
         if (Mat112.stone(this.get(p))) {
            this.put(p, ore);
         }
         this.ops.add(new Op(p.getX(), p.getY(), p.getZ(), ORE, ore, null, 0L));
      }
   }

   void template(BlockPos p, String id, int rotation, int mirror) {
      this.ops.add(new Op(p.getX(), p.getY(), p.getZ(), TEMPLATE, null, id, rotation * 4L + mirror));
   }

   void lily(int x, int z, BlockState lily) {
      this.ops.add(new Op(x, 0, z, LILY, lily, null, 0L));
   }

   /** World.getHeight: above the highest block that is not air (the 1.12 terrain has no transparent blocks). */
   int height(int x, int z) {
      long col = ((long)x << 32) ^ (z & 0xFFFFFFFFL);
      Integer h = this.heights.get(col);
      if (h == null) {
         h = this.scanHeight(x, z);
         this.heights.put(col, h);
      }
      return h;
   }

   private int scanHeight(int x, int z) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, 255, z);
      for (int y = 255; y >= 0; y--) {
         p.setY(y);
         if (!this.get(p).isAir()) {
            return y + 1;
         }
      }
      return 0;
   }

   BlockPos height(BlockPos p) {
      return new BlockPos(p.getX(), this.height(p.getX(), p.getZ()), p.getZ());
   }

   boolean canSeeSky(BlockPos p) {
      return p.getY() >= this.height(p.getX(), p.getZ());
   }

   Biome biome(BlockPos p) {
      return this.gen.legacyBiome(p.getX(), p.getZ());
   }

   boolean hasType(BlockPos p, String type) {
      Set<String> t = BiomeDict.TYPES.get(this.biome(p).getRegistryName());
      return t != null && t.contains(type);
   }
}
