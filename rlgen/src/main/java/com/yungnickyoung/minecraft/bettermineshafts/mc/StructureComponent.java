package com.yungnickyoung.minecraft.bettermineshafts.mc;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/** 1.12 StructureComponent, the parts Better Mineshafts uses. */
public abstract class StructureComponent {
   protected StructureBoundingBox boundingBox;
   @Nullable
   private Direction coordBaseMode;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   protected int componentType;

   public StructureComponent() {
   }

   protected StructureComponent(int i) {
      this.componentType = i;
   }

   public void buildComponent(StructureComponent structurecomponent1, List<StructureComponent> list, Random random) {
   }

   public abstract boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox);

   public StructureBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getComponentType() {
      return this.componentType;
   }

   public static StructureComponent findIntersecting(List<StructureComponent> list, StructureBoundingBox box) {
      for (StructureComponent c : list) {
         if (c.getBoundingBox() != null && c.getBoundingBox().intersectsWith(box)) {
            return c;
         }
      }
      return null;
   }

   protected boolean isLiquidInStructureBoundingBox(World world, StructureBoundingBox box) {
      int i = Math.max(this.boundingBox.minX - 1, box.minX);
      int j = Math.max(this.boundingBox.minY - 1, box.minY);
      int k = Math.max(this.boundingBox.minZ - 1, box.minZ);
      int l = Math.min(this.boundingBox.maxX + 1, box.maxX);
      int i1 = Math.min(this.boundingBox.maxY + 1, box.maxY);
      int j1 = Math.min(this.boundingBox.maxZ + 1, box.maxZ);
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
      for (int k1 = i; k1 <= l; k1++) {
         for (int l1 = k; l1 <= j1; l1++) {
            if (liquid(world.getBlockState(m.set(k1, j, l1))) || liquid(world.getBlockState(m.set(k1, i1, l1)))) {
               return true;
            }
         }
      }
      for (int i2 = i; i2 <= l; i2++) {
         for (int k2 = j; k2 <= i1; k2++) {
            if (liquid(world.getBlockState(m.set(i2, k2, k))) || liquid(world.getBlockState(m.set(i2, k2, j1)))) {
               return true;
            }
         }
      }
      for (int j2 = k; j2 <= j1; j2++) {
         for (int l2 = j; l2 <= i1; l2++) {
            if (liquid(world.getBlockState(m.set(i, l2, j2))) || liquid(world.getBlockState(m.set(l, l2, j2)))) {
               return true;
            }
         }
      }
      return false;
   }

   /** Material.isLiquid: water and lava (Lycanites fluids too, they were Material.WATER). */
   protected static boolean liquid(BlockState s) {
      return s.liquid();
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

   protected void setBlockState(World world, BlockState state, int i, int j, int k, StructureBoundingBox box) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      if (box.isVecInside(pos)) {
         if (this.mirror != Mirror.NONE) {
            state = state.mirror(this.mirror);
         }
         if (this.rotation != Rotation.NONE) {
            state = state.rotate(this.rotation);
         }
         world.setBlockState(pos, state, 2);
      }
   }

   protected BlockState getBlockStateFromPos(World world, int i, int j, int k, StructureBoundingBox box) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      return !box.isVecInside(pos) ? Blocks.AIR.defaultBlockState() : world.getBlockState(pos);
   }

   protected void fillWithAir(World world, StructureBoundingBox box, int i, int j, int k, int l, int i1, int j1) {
      for (int k1 = j; k1 <= i1; k1++) {
         for (int l1 = i; l1 <= l; l1++) {
            for (int i2 = k; i2 <= j1; i2++) {
               this.setBlockState(world, Blocks.AIR.defaultBlockState(), l1, k1, i2, box);
            }
         }
      }
   }

   protected boolean generateChest(World world, StructureBoundingBox box, Random random, int i, int j, int k, ResourceKey<LootTable> loot) {
      BlockPos pos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      return this.generateChest(world, box, random, pos, loot, null);
   }

   protected boolean generateChest(World world, StructureBoundingBox box, Random random, BlockPos pos, ResourceKey<LootTable> loot, @Nullable BlockState state) {
      if (box.isVecInside(pos) && !world.getBlockState(pos).is(Blocks.CHEST)) {
         if (state == null) {
            state = correctFacing(world, pos);
         }
         world.setBlockState(pos, state, 2);
         long seed = random.nextLong();
         world.withTileEntity(pos, be -> {
            if (be instanceof RandomizableContainerBlockEntity c) {
               c.setLootTable(loot, seed);
            }
         });
         return true;
      }
      return false;
   }

   /** BlockChest.correctFacing on a north-facing chest (EnumFacing.Plane.HORIZONTAL order: north, east, south, west). */
   private static BlockState correctFacing(World world, BlockPos pos) {
      BlockState chest = Blocks.CHEST.defaultBlockState();
      Direction facing = null;
      for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
         BlockState s = world.getBlockState(pos.relative(d));
         if (s.is(Blocks.CHEST)) {
            return chest;
         }
         if (fullBlock(world, pos.relative(d), s)) {
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
      if (fullBlock(world, pos.relative(f), null)) {
         f = f.getOpposite();
      }
      if (fullBlock(world, pos.relative(f), null)) {
         f = f.getClockWise();
      }
      if (fullBlock(world, pos.relative(f), null)) {
         f = f.getOpposite();
      }
      return chest.setValue(ChestBlock.FACING, f);
   }

   /** IBlockState.isFullBlock: a full opaque cube. */
   protected static boolean fullBlock(World world, BlockPos pos, @Nullable BlockState s) {
      if (s == null) {
         s = world.getBlockState(pos);
      }
      return s.isSolidRender(world.level, pos);
   }

   public void offset(int i, int j, int k) {
      this.boundingBox.offset(i, j, k);
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
