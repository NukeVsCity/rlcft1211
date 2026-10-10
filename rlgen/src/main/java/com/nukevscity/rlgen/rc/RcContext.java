package com.nukevscity.rlgen.rc;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * RC StructurePrepareContext / StructureSpawnContext. While preparing, {@link #world} is the 1.12 terrain oracle and nothing is written;
 * while spawning it is the real chunk and {@link #genBB} (the chunk being decorated) limits writes, like 1.12's partial generation.
 */
public final class RcContext {
   public final RcStructureDef def;
   public final RcTransform transform;
   public final BlockPos lower;
   public final BoundingBox bb;
   public final RcEnv env;
   public final Random random;
   public final RcWorldView world;
   public final int layer;
   public final boolean suggest;
   public final BoundingBox genBB;

   public RcContext(RcStructureDef def, RcTransform transform, BlockPos lower, RcEnv env, Random random, RcWorldView world, int layer, boolean suggest,
                    BoundingBox genBB) {
      this.def = def;
      this.transform = transform;
      this.lower = lower;
      int[] s = transform.applySize(def.size);
      this.bb = new BoundingBox(lower.getX(), lower.getY(), lower.getZ(), lower.getX() + s[0] - 1, lower.getY() + s[1] - 1, lower.getZ() + s[2] - 1);
      this.env = env;
      this.random = random;
      this.world = world;
      this.layer = layer;
      this.suggest = suggest;
      this.genBB = genBB;
   }

   public int[] size() {
      return this.def.size;
   }

   public BlockPos worldPos(BlockPos src) {
      return this.transform.apply(src, this.def.size).offset(this.lower);
   }

   public BlockPos worldPos(int x, int y, int z) {
      return this.transform.apply(x, y, z, this.def.size).offset(this.lower);
   }

   public boolean includes(BlockPos p) {
      return this.genBB == null || this.genBB.isInside(p);
   }

   public boolean setBlock(BlockPos p, BlockState s) {
      return this.genBB != null && this.genBB.isInside(p) && this.world.set(p, s);
   }

   public WorldGenLevel level() {
      return this.world.level();
   }
}
