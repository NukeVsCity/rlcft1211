package com.nukevscity.rlgen.stronghold;

import com.nukevscity.rlgen.deco.DWorld;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/** 1.12 StructureComponent (the parts strongholds use), placing into the populate world. */
public abstract class Component {
   protected SBox boundingBox;
   @Nullable
   private Direction coordBaseMode;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   protected int componentType;

   protected Component(int i) {
      this.componentType = i;
   }

   public void buildComponent(Component start, List<Component> list, Random random) {
   }

   public abstract boolean addComponentParts(DWorld world, Random random, SBox box);

   public SBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getComponentType() {
      return this.componentType;
   }

   public static Component findIntersecting(List<Component> list, SBox box) {
      for (Component c : list) {
         if (c.getBoundingBox() != null && c.getBoundingBox().intersectsWith(box)) {
            return c;
         }
      }
      return null;
   }

   protected boolean isLiquidInStructureBoundingBox(DWorld world, SBox box) {
      int i = Math.max(this.boundingBox.minX - 1, box.minX);
      int j = Math.max(this.boundingBox.minY - 1, box.minY);
      int k = Math.max(this.boundingBox.minZ - 1, box.minZ);
      int l = Math.min(this.boundingBox.maxX + 1, box.maxX);
      int i1 = Math.min(this.boundingBox.maxY + 1, box.maxY);
      int j1 = Math.min(this.boundingBox.maxZ + 1, box.maxZ);
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
      for (int k1 = i; k1 <= l; k1++) {
         for (int l1 = k; l1 <= j1; l1++) {
            if (DWorld.isLiquid(world.get(m.set(k1, j, l1)))) {
               return true;
            }
            if (DWorld.isLiquid(world.get(m.set(k1, i1, l1)))) {
               return true;
            }
         }
      }
      for (int i2 = i; i2 <= l; i2++) {
         for (int k2 = j; k2 <= i1; k2++) {
            if (DWorld.isLiquid(world.get(m.set(i2, k2, k)))) {
               return true;
            }
            if (DWorld.isLiquid(world.get(m.set(i2, k2, j1)))) {
               return true;
            }
         }
      }
      for (int j2 = k; j2 <= j1; j2++) {
         for (int l2 = j; l2 <= i1; l2++) {
            if (DWorld.isLiquid(world.get(m.set(i, l2, j2)))) {
               return true;
            }
            if (DWorld.isLiquid(world.get(m.set(l, l2, j2)))) {
               return true;
            }
         }
      }
      return false;
   }

   protected int getXWithOffset(int i, int j) {
      Direction f = this.getCoordBaseMode();
      if (f == null) {
         return i;
      }
      switch (f) {
         case NORTH:
         case SOUTH:
            return this.boundingBox.minX + i;
         case WEST:
            return this.boundingBox.maxX - j;
         case EAST:
            return this.boundingBox.minX + j;
         default:
            return i;
      }
   }

   protected int getYWithOffset(int i) {
      return this.getCoordBaseMode() == null ? i : i + this.boundingBox.minY;
   }

   protected int getZWithOffset(int i, int j) {
      Direction f = this.getCoordBaseMode();
      if (f == null) {
         return j;
      }
      switch (f) {
         case NORTH:
            return this.boundingBox.maxZ - j;
         case SOUTH:
            return this.boundingBox.minZ + j;
         case WEST:
         case EAST:
            return this.boundingBox.minZ + i;
         default:
            return j;
      }
   }

   /** Blocks whose 1.12 connections were render-time (getActualState); 1.21 stores them, so the chunk fixes them when it finishes. */
   private static boolean needsShape(Block b) {
      return b instanceof FenceBlock || b instanceof WallBlock || b instanceof IronBarsBlock || b instanceof StairBlock;
   }

   /** World.setBlockState(pos, state, 2) during population. */
   static void place(DWorld world, BlockPos pos, BlockState state) {
      world.set(pos, state);
      if (pos.getY() < 0 || pos.getY() > 255) {
         return;
      }
      if (needsShape(state.getBlock())) {
         world.level.getChunk(pos).markPosForPostprocessing(pos);
      }
      if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
         // flowing_water / flowing_lava scheduled their update in onBlockAdded
         world.scheduleFluidTick(pos, state.getBlock() == Blocks.WATER);
      }
   }

   protected void setBlockState(DWorld world, BlockState state, int i, int j, int k, SBox box) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      if (box.isVecInside(pos)) {
         if (this.mirror != Mirror.NONE) {
            state = state.mirror(this.mirror);
         }
         if (this.rotation != Rotation.NONE) {
            state = state.rotate(this.rotation);
         }
         place(world, pos, state);
      }
   }

   protected BlockState getBlockStateFromPos(DWorld world, int i, int j, int k, SBox box) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      return !box.isVecInside(pos) ? Blocks.AIR.defaultBlockState() : world.get(pos);
   }

   protected void fillWithBlocks(DWorld world, SBox box, int i, int j, int k, int l, int i1, int j1, BlockState edge, BlockState inside, boolean existingOnly) {
      for (int k1 = j; k1 <= i1; k1++) {
         for (int l1 = i; l1 <= l; l1++) {
            for (int i2 = k; i2 <= j1; i2++) {
               if (!existingOnly || !this.getBlockStateFromPos(world, l1, k1, i2, box).isAir()) {
                  if (k1 != j && k1 != i1 && l1 != i && l1 != l && i2 != k && i2 != j1) {
                     this.setBlockState(world, inside, l1, k1, i2, box);
                  } else {
                     this.setBlockState(world, edge, l1, k1, i2, box);
                  }
               }
            }
         }
      }
   }

   /** StructureComponent.BlockSelector, returning the block instead of keeping it in a field. */
   @FunctionalInterface
   public interface BlockSelector {
      BlockState selectBlocks(Random random, int x, int y, int z, boolean wall);
   }

   protected void fillWithRandomizedBlocks(DWorld world, SBox box, int i, int j, int k, int l, int i1, int j1, boolean existingOnly, Random random,
                                           BlockSelector selector) {
      for (int k1 = j; k1 <= i1; k1++) {
         for (int l1 = i; l1 <= l; l1++) {
            for (int i2 = k; i2 <= j1; i2++) {
               if (!existingOnly || !this.getBlockStateFromPos(world, l1, k1, i2, box).isAir()) {
                  BlockState s = selector.selectBlocks(random, l1, k1, i2, k1 == j || k1 == i1 || l1 == i || l1 == l || i2 == k || i2 == j1);
                  this.setBlockState(world, s, l1, k1, i2, box);
               }
            }
         }
      }
   }

   /** generateMaybeBox; the sky light test (last argument above 0) is never used by strongholds. */
   protected void generateMaybeBox(DWorld world, SBox box, Random random, float f, int i, int j, int k, int l, int i1, int j1, BlockState edge,
                                   BlockState inside, boolean existingOnly, int light) {
      for (int l1 = j; l1 <= i1; l1++) {
         for (int i2 = i; i2 <= l; i2++) {
            for (int j2 = k; j2 <= j1; j2++) {
               if (!(random.nextFloat() > f) && (!existingOnly || !this.getBlockStateFromPos(world, i2, l1, j2, box).isAir()) && light <= 0) {
                  if (l1 != j && l1 != i1 && i2 != i && i2 != l && j2 != k && j2 != j1) {
                     this.setBlockState(world, inside, i2, l1, j2, box);
                  } else {
                     this.setBlockState(world, edge, i2, l1, j2, box);
                  }
               }
            }
         }
      }
   }

   protected void randomlyPlaceBlock(DWorld world, SBox box, Random random, float f, int i, int j, int k, BlockState state) {
      if (random.nextFloat() < f) {
         this.setBlockState(world, state, i, j, k, box);
      }
   }

   protected boolean generateChest(DWorld world, SBox box, Random random, int i, int j, int k, ResourceKey<LootTable> loot) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      if (box.isVecInside(pos) && !world.get(pos).is(Blocks.CHEST)) {
         place(world, pos, correctFacing(world, pos));
         long seed = random.nextLong();
         if (pos.getY() >= 0 && pos.getY() <= 255) {
            BlockEntity be = world.level.getBlockEntity(pos);
            if (be instanceof RandomizableContainerBlockEntity c) {
               c.setLootTable(loot, seed);
            }
         }
         return true;
      }
      return false;
   }

   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

   /** BlockChest.correctFacing on a default (north) chest. */
   private static BlockState correctFacing(DWorld world, BlockPos pos) {
      BlockState chest = Blocks.CHEST.defaultBlockState();
      Direction facing = null;
      for (Direction d : HORIZONTAL) {
         BlockState s = world.get(pos.relative(d));
         if (s.is(Blocks.CHEST)) {
            return chest;
         }
         if (fullBlock(world, pos.relative(d))) {
            if (facing != null) {
               facing = null;
               break;
            }
            facing = d;
         }
      }
      if (facing != null) {
         return chest.setValue(ChestBlock.FACING, facing.getOpposite());
      }
      Direction f = Direction.NORTH;
      if (fullBlock(world, pos.relative(f))) {
         f = f.getOpposite();
      }
      if (fullBlock(world, pos.relative(f))) {
         f = f.getClockWise();
      }
      if (fullBlock(world, pos.relative(f))) {
         f = f.getOpposite();
      }
      return chest.setValue(ChestBlock.FACING, f);
   }

   /** IBlockState.isFullBlock: a full opaque cube. */
   private static boolean fullBlock(DWorld world, BlockPos pos) {
      return world.get(pos).isSolidRender(world.level, pos);
   }

   public void offset(int x, int y, int z) {
      this.boundingBox.offset(x, y, z);
   }

   @Nullable
   public Direction getCoordBaseMode() {
      return this.coordBaseMode;
   }

   public void setCoordBaseMode(@Nullable Direction f) {
      this.coordBaseMode = f;
      if (f == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch (f) {
            case SOUTH:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
               break;
            case WEST:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
         }
      }
   }
}
