package com.nukevscity.rlgen.scattered;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.quark.Templates;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/** 1.12 ComponentScatteredFeaturePieces (and the parts of StructureComponent they use), placed on a {@link DWorld} at populate time. */
public final class ScatteredPieces {
   private ScatteredPieces() {
   }

   /** EnumFacing.Plane.HORIZONTAL.facings() */
   static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

   /** 1.12 StructureBoundingBox (mutable, min/max inclusive). */
   static final class Box {
      int minX, minY, minZ, maxX, maxY, maxZ;

      Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      /** new StructureBoundingBox(x0, z0, x1, z1): the populate box, Y 1..512. */
      static Box populate(int x0, int z0, int x1, int z1) {
         return new Box(x0, 1, z0, x1, 512, z1);
      }

      boolean inside(int x, int y, int z) {
         return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
      }

      boolean inside(BlockPos p) {
         return this.inside(p.getX(), p.getY(), p.getZ());
      }

      boolean intersects(Box b) {
         return this.maxX >= b.minX && this.minX <= b.maxX && this.maxZ >= b.minZ && this.minZ <= b.maxZ && this.maxY >= b.minY && this.minY <= b.maxY;
      }

      BoundingBox toBoundingBox() {
         return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
      }
   }

   /** 1.12 StructureComponent.BlockSelector */
   interface BlockSelector {
      BlockState select(Random random, int x, int y, int z, boolean edge);
   }

   /** ComponentScatteredFeaturePieces.Feature + StructureComponent */
   public abstract static class Feature {
      final String id;
      Box boundingBox;
      Direction coordBaseMode;
      Mirror mirror;
      Rotation rotation;
      final int width;
      final int height;
      final int depth;
      int horizontalPos = -1;

      Feature(String id, Random random, int x, int y, int z, int width, int height, int depth) {
         this.id = id;
         this.width = width;
         this.height = height;
         this.depth = depth;
         this.setCoordBaseMode(HORIZONTAL[random.nextInt(HORIZONTAL.length)]);
         if (this.coordBaseMode.getAxis() == Direction.Axis.Z) {
            this.boundingBox = new Box(x, y, z, x + width - 1, y + height - 1, z + depth - 1);
         } else {
            this.boundingBox = new Box(x, y, z, x + depth - 1, y + height - 1, z + width - 1);
         }
      }

      public String id() {
         return this.id;
      }

      public BoundingBox box() {
         return this.boundingBox.toBoundingBox();
      }

      public int width() {
         return this.width;
      }

      public int depth() {
         return this.depth;
      }

      public abstract boolean addComponentParts(DWorld world, Random random, Box box);

      void save(CompoundTag tag) {
         tag.putInt("HPos", this.horizontalPos);
         tag.putIntArray("BB", new int[]{this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX,
            this.boundingBox.maxY, this.boundingBox.maxZ});
      }

      void load(CompoundTag tag) {
         this.horizontalPos = tag.getInt("HPos");
         int[] bb = tag.getIntArray("BB");
         if (bb.length == 6) {
            this.boundingBox = new Box(bb[0], bb[1], bb[2], bb[3], bb[4], bb[5]);
         }
      }

      final void setCoordBaseMode(Direction facing) {
         this.coordBaseMode = facing;
         switch (facing) {
            case SOUTH -> {
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
            }
            case WEST -> {
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
            }
            case EAST -> {
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
            }
            default -> {
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
            }
         }
      }

      /** Feature.offsetToAverageGroundLevel: from the columns of the first populate box that reached it. */
      protected boolean offsetToAverageGroundLevel(DWorld world, Box box, int yOffset) {
         if (this.horizontalPos >= 0) {
            return true;
         }
         int sum = 0;
         int count = 0;
         for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
            for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
               if (box.inside(x, 64, z)) {
                  // WorldProvider.getAverageGroundLevel: sea level + 1
                  sum += Math.max(world.topSolidOrLiquid(new BlockPos(x, 64, z)).getY(), 64);
                  count++;
               }
            }
         }
         if (count == 0) {
            return false;
         }
         this.horizontalPos = sum / count;
         int dy = this.horizontalPos - this.boundingBox.minY + yOffset;
         this.boundingBox.minY += dy;
         this.boundingBox.maxY += dy;
         return true;
      }

      protected int getXWithOffset(int x, int z) {
         return switch (this.coordBaseMode) {
            case NORTH, SOUTH -> this.boundingBox.minX + x;
            case WEST -> this.boundingBox.maxX - z;
            case EAST -> this.boundingBox.minX + z;
            default -> x;
         };
      }

      protected int getYWithOffset(int y) {
         return y + this.boundingBox.minY;
      }

      protected int getZWithOffset(int x, int z) {
         return switch (this.coordBaseMode) {
            case NORTH -> this.boundingBox.maxZ - z;
            case SOUTH -> this.boundingBox.minZ + z;
            case WEST, EAST -> this.boundingBox.minZ + x;
            default -> z;
         };
      }

      private BlockPos worldPos(int x, int y, int z) {
         return new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
      }

      protected void setBlockState(DWorld world, BlockState state, int x, int y, int z, Box box) {
         BlockPos pos = this.worldPos(x, y, z);
         if (box.inside(pos)) {
            if (this.mirror != Mirror.NONE) {
               state = state.mirror(this.mirror);
            }
            if (this.rotation != Rotation.NONE) {
               state = state.rotate(this.rotation);
            }
            place(world, pos, state);
         }
      }

      /** 1.12 computed fence, stair, wire and tripwire connections when drawn; 1.21 stores them, so they are fixed at chunk post-processing. */
      static void place(DWorld world, BlockPos pos, BlockState state) {
         world.set(pos, state);
         Block b = state.getBlock();
         if ((b instanceof FenceBlock || b instanceof StairBlock || b instanceof RedStoneWireBlock || b instanceof TripWireBlock) && world.get(pos) == state) {
            world.level.getChunk(pos).markPosForPostprocessing(pos);
         }
      }

      protected BlockState getBlockStateFromPos(DWorld world, int x, int y, int z, Box box) {
         BlockPos pos = this.worldPos(x, y, z);
         return !box.inside(pos) ? Blocks.AIR.defaultBlockState() : world.get(pos);
      }

      protected void fillWithAir(DWorld world, Box box, int x0, int y0, int z0, int x1, int y1, int z1) {
         for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
               for (int z = z0; z <= z1; z++) {
                  this.setBlockState(world, Blocks.AIR.defaultBlockState(), x, y, z, box);
               }
            }
         }
      }

      protected void fillWithBlocks(DWorld world, Box box, int x0, int y0, int z0, int x1, int y1, int z1, BlockState edge, BlockState inside,
         boolean existingOnly) {
         for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
               for (int z = z0; z <= z1; z++) {
                  if (!existingOnly || !this.getBlockStateFromPos(world, x, y, z, box).isAir()) {
                     if (y != y0 && y != y1 && x != x0 && x != x1 && z != z0 && z != z1) {
                        this.setBlockState(world, inside, x, y, z, box);
                     } else {
                        this.setBlockState(world, edge, x, y, z, box);
                     }
                  }
               }
            }
         }
      }

      protected void fillWithRandomizedBlocks(DWorld world, Box box, int x0, int y0, int z0, int x1, int y1, int z1, boolean existingOnly,
         Random random, BlockSelector selector) {
         for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
               for (int z = z0; z <= z1; z++) {
                  if (!existingOnly || !this.getBlockStateFromPos(world, x, y, z, box).isAir()) {
                     BlockState s = selector.select(random, x, y, z, y == y0 || y == y1 || x == x0 || x == x1 || z == z0 || z == z1);
                     this.setBlockState(world, s, x, y, z, box);
                  }
               }
            }
         }
      }

      protected void replaceAirAndLiquidDownwards(DWorld world, BlockState state, int x, int y, int z, Box box) {
         int wx = this.getXWithOffset(x, z);
         int wy = this.getYWithOffset(y);
         int wz = this.getZWithOffset(x, z);
         if (box.inside(wx, wy, wz)) {
            while (true) {
               BlockPos p = new BlockPos(wx, wy, wz);
               BlockState s = world.get(p);
               if (!(s.isAir() || DWorld.isLiquid(s)) || wy <= 1) {
                  break;
               }
               world.set(p, state);
               wy--;
            }
         }
      }

      protected boolean generateChest(DWorld world, Box box, Random random, int x, int y, int z, ResourceKey<LootTable> loot) {
         BlockPos pos = this.worldPos(x, y, z);
         if (box.inside(pos) && !world.get(pos).is(Blocks.CHEST)) {
            world.set(pos, correctFacing(world, pos, Blocks.CHEST.defaultBlockState()));
            if (world.level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity be) {
               be.setLootTable(loot, random.nextLong());
            }
            return true;
         }
         return false;
      }

      protected boolean createDispenser(DWorld world, Box box, Random random, int x, int y, int z, Direction facing, ResourceKey<LootTable> loot) {
         BlockPos pos = this.worldPos(x, y, z);
         if (box.inside(pos) && !world.get(pos).is(Blocks.DISPENSER)) {
            this.setBlockState(world, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, facing), x, y, z, box);
            if (world.level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity be) {
               be.setLootTable(loot, random.nextLong());
            }
            return true;
         }
         return false;
      }
   }

   /** IBlockState.isFullBlock (1.12 full opaque cube) */
   static boolean fullBlock(DWorld world, BlockPos pos) {
      BlockState s = world.get(pos);
      return s.isSolidRender(world.level, pos);
   }

   /** BlockChest.correctFacing */
   static BlockState correctFacing(DWorld world, BlockPos pos, BlockState state) {
      Direction facing = null;
      for (Direction d : HORIZONTAL) {
         BlockPos n = pos.relative(d);
         if (world.get(n).is(Blocks.CHEST)) {
            return state;
         }
         if (fullBlock(world, n)) {
            if (facing != null) {
               facing = null;
               break;
            }
            facing = d;
         }
      }
      if (facing != null) {
         return state.setValue(ChestBlock.FACING, facing.getOpposite());
      }
      Direction f = state.getValue(ChestBlock.FACING);
      if (fullBlock(world, pos.relative(f))) {
         f = f.getOpposite();
      }
      if (fullBlock(world, pos.relative(f))) {
         f = f.getClockWise();
      }
      if (fullBlock(world, pos.relative(f))) {
         f = f.getOpposite();
      }
      return state.setValue(ChestBlock.FACING, f);
   }

   private static BlockState stairs(Block b, Direction d) {
      return b.defaultBlockState().setValue(StairBlock.FACING, d);
   }

   // ------------------------------------------------------------------ DesertPyramid

   public static final class DesertPyramid extends Feature {
      private final boolean[] hasPlacedChest = new boolean[4];

      DesertPyramid(Random random, int x, int z) {
         super("desert_pyramid", random, x, 64, z, 21, 15, 21);
      }

      @Override
      void save(CompoundTag tag) {
         super.save(tag);
         for (int i = 0; i < 4; i++) {
            tag.putBoolean("hasPlacedChest" + i, this.hasPlacedChest[i]);
         }
      }

      @Override
      void load(CompoundTag tag) {
         super.load(tag);
         for (int i = 0; i < 4; i++) {
            this.hasPlacedChest[i] = tag.getBoolean("hasPlacedChest" + i);
         }
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, Box box) {
         BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
         BlockState smooth = Blocks.CUT_SANDSTONE.defaultBlockState();
         BlockState chiseled = Blocks.CHISELED_SANDSTONE.defaultBlockState();
         BlockState air = Blocks.AIR.defaultBlockState();
         this.fillWithBlocks(world, box, 0, -4, 0, this.width - 1, 0, this.depth - 1, sandstone, sandstone, false);
         for (int i = 1; i <= 9; i++) {
            this.fillWithBlocks(world, box, i, i, i, this.width - 1 - i, i, this.depth - 1 - i, sandstone, sandstone, false);
            this.fillWithBlocks(world, box, i + 1, i, i + 1, this.width - 2 - i, i, this.depth - 2 - i, air, air, false);
         }
         for (int i2 = 0; i2 < this.width; i2++) {
            for (int j = 0; j < this.depth; j++) {
               this.replaceAirAndLiquidDownwards(world, sandstone, i2, -5, j, box);
            }
         }
         BlockState north = stairs(Blocks.SANDSTONE_STAIRS, Direction.NORTH);
         BlockState south = stairs(Blocks.SANDSTONE_STAIRS, Direction.SOUTH);
         BlockState east = stairs(Blocks.SANDSTONE_STAIRS, Direction.EAST);
         BlockState west = stairs(Blocks.SANDSTONE_STAIRS, Direction.WEST);
         // ~EnumDyeColor.ORANGE.getDyeDamage() & 15 = 1 (orange), ~BLUE & 15 = 11 (blue)
         BlockState orange = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
         BlockState blue = Blocks.BLUE_TERRACOTTA.defaultBlockState();
         this.fillWithBlocks(world, box, 0, 0, 0, 4, 9, 4, sandstone, air, false);
         this.fillWithBlocks(world, box, 1, 10, 1, 3, 10, 3, sandstone, sandstone, false);
         this.setBlockState(world, north, 2, 10, 0, box);
         this.setBlockState(world, south, 2, 10, 4, box);
         this.setBlockState(world, east, 0, 10, 2, box);
         this.setBlockState(world, west, 4, 10, 2, box);
         this.fillWithBlocks(world, box, this.width - 5, 0, 0, this.width - 1, 9, 4, sandstone, air, false);
         this.fillWithBlocks(world, box, this.width - 4, 10, 1, this.width - 2, 10, 3, sandstone, sandstone, false);
         this.setBlockState(world, north, this.width - 3, 10, 0, box);
         this.setBlockState(world, south, this.width - 3, 10, 4, box);
         this.setBlockState(world, east, this.width - 5, 10, 2, box);
         this.setBlockState(world, west, this.width - 1, 10, 2, box);
         this.fillWithBlocks(world, box, 8, 0, 0, 12, 4, 4, sandstone, air, false);
         this.fillWithBlocks(world, box, 9, 1, 0, 11, 3, 4, air, air, false);
         this.setBlockState(world, smooth, 9, 1, 1, box);
         this.setBlockState(world, smooth, 9, 2, 1, box);
         this.setBlockState(world, smooth, 9, 3, 1, box);
         this.setBlockState(world, smooth, 10, 3, 1, box);
         this.setBlockState(world, smooth, 11, 3, 1, box);
         this.setBlockState(world, smooth, 11, 2, 1, box);
         this.setBlockState(world, smooth, 11, 1, 1, box);
         this.fillWithBlocks(world, box, 4, 1, 1, 8, 3, 3, sandstone, air, false);
         this.fillWithBlocks(world, box, 4, 1, 2, 8, 2, 2, air, air, false);
         this.fillWithBlocks(world, box, 12, 1, 1, 16, 3, 3, sandstone, air, false);
         this.fillWithBlocks(world, box, 12, 1, 2, 16, 2, 2, air, air, false);
         this.fillWithBlocks(world, box, 5, 4, 5, this.width - 6, 4, this.depth - 6, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, 9, 4, 9, 11, 4, 11, air, air, false);
         this.fillWithBlocks(world, box, 8, 1, 8, 8, 3, 8, smooth, smooth, false);
         this.fillWithBlocks(world, box, 12, 1, 8, 12, 3, 8, smooth, smooth, false);
         this.fillWithBlocks(world, box, 8, 1, 12, 8, 3, 12, smooth, smooth, false);
         this.fillWithBlocks(world, box, 12, 1, 12, 12, 3, 12, smooth, smooth, false);
         this.fillWithBlocks(world, box, 1, 1, 5, 4, 4, 11, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, this.width - 5, 1, 5, this.width - 2, 4, 11, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, 6, 7, 9, 6, 7, 11, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, this.width - 7, 7, 9, this.width - 7, 7, 11, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, 5, 5, 9, 5, 7, 11, smooth, smooth, false);
         this.fillWithBlocks(world, box, this.width - 6, 5, 9, this.width - 6, 7, 11, smooth, smooth, false);
         this.setBlockState(world, air, 5, 5, 10, box);
         this.setBlockState(world, air, 5, 6, 10, box);
         this.setBlockState(world, air, 6, 6, 10, box);
         this.setBlockState(world, air, this.width - 6, 5, 10, box);
         this.setBlockState(world, air, this.width - 6, 6, 10, box);
         this.setBlockState(world, air, this.width - 7, 6, 10, box);
         this.fillWithBlocks(world, box, 2, 4, 4, 2, 6, 4, air, air, false);
         this.fillWithBlocks(world, box, this.width - 3, 4, 4, this.width - 3, 6, 4, air, air, false);
         this.setBlockState(world, north, 2, 4, 5, box);
         this.setBlockState(world, north, 2, 3, 4, box);
         this.setBlockState(world, north, this.width - 3, 4, 5, box);
         this.setBlockState(world, north, this.width - 3, 3, 4, box);
         this.fillWithBlocks(world, box, 1, 1, 3, 2, 2, 3, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, this.width - 3, 1, 3, this.width - 2, 2, 3, sandstone, sandstone, false);
         this.setBlockState(world, sandstone, 1, 1, 2, box);
         this.setBlockState(world, sandstone, this.width - 2, 1, 2, box);
         this.setBlockState(world, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, box);
         this.setBlockState(world, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, box);
         this.setBlockState(world, west, 2, 1, 2, box);
         this.setBlockState(world, east, this.width - 3, 1, 2, box);
         this.fillWithBlocks(world, box, 4, 3, 5, 4, 3, 18, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, this.width - 5, 3, 5, this.width - 5, 3, 17, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, 3, 1, 5, 4, 2, 16, air, air, false);
         this.fillWithBlocks(world, box, this.width - 6, 1, 5, this.width - 5, 2, 16, air, air, false);
         for (int j1 = 5; j1 <= 17; j1 += 2) {
            this.setBlockState(world, smooth, 4, 1, j1, box);
            this.setBlockState(world, chiseled, 4, 2, j1, box);
            this.setBlockState(world, smooth, this.width - 5, 1, j1, box);
            this.setBlockState(world, chiseled, this.width - 5, 2, j1, box);
         }
         this.setBlockState(world, orange, 10, 0, 7, box);
         this.setBlockState(world, orange, 10, 0, 8, box);
         this.setBlockState(world, orange, 9, 0, 9, box);
         this.setBlockState(world, orange, 11, 0, 9, box);
         this.setBlockState(world, orange, 8, 0, 10, box);
         this.setBlockState(world, orange, 12, 0, 10, box);
         this.setBlockState(world, orange, 7, 0, 10, box);
         this.setBlockState(world, orange, 13, 0, 10, box);
         this.setBlockState(world, orange, 9, 0, 11, box);
         this.setBlockState(world, orange, 11, 0, 11, box);
         this.setBlockState(world, orange, 10, 0, 12, box);
         this.setBlockState(world, orange, 10, 0, 13, box);
         this.setBlockState(world, blue, 10, 0, 10, box);
         for (int j2 = 0; j2 <= this.width - 1; j2 += this.width - 1) {
            this.setBlockState(world, smooth, j2, 2, 1, box);
            this.setBlockState(world, orange, j2, 2, 2, box);
            this.setBlockState(world, smooth, j2, 2, 3, box);
            this.setBlockState(world, smooth, j2, 3, 1, box);
            this.setBlockState(world, orange, j2, 3, 2, box);
            this.setBlockState(world, smooth, j2, 3, 3, box);
            this.setBlockState(world, orange, j2, 4, 1, box);
            this.setBlockState(world, chiseled, j2, 4, 2, box);
            this.setBlockState(world, orange, j2, 4, 3, box);
            this.setBlockState(world, smooth, j2, 5, 1, box);
            this.setBlockState(world, orange, j2, 5, 2, box);
            this.setBlockState(world, smooth, j2, 5, 3, box);
            this.setBlockState(world, orange, j2, 6, 1, box);
            this.setBlockState(world, chiseled, j2, 6, 2, box);
            this.setBlockState(world, orange, j2, 6, 3, box);
            this.setBlockState(world, orange, j2, 7, 1, box);
            this.setBlockState(world, orange, j2, 7, 2, box);
            this.setBlockState(world, orange, j2, 7, 3, box);
            this.setBlockState(world, smooth, j2, 8, 1, box);
            this.setBlockState(world, smooth, j2, 8, 2, box);
            this.setBlockState(world, smooth, j2, 8, 3, box);
         }
         for (int k2 = 2; k2 <= this.width - 3; k2 += this.width - 3 - 2) {
            this.setBlockState(world, smooth, k2 - 1, 2, 0, box);
            this.setBlockState(world, orange, k2, 2, 0, box);
            this.setBlockState(world, smooth, k2 + 1, 2, 0, box);
            this.setBlockState(world, smooth, k2 - 1, 3, 0, box);
            this.setBlockState(world, orange, k2, 3, 0, box);
            this.setBlockState(world, smooth, k2 + 1, 3, 0, box);
            this.setBlockState(world, orange, k2 - 1, 4, 0, box);
            this.setBlockState(world, chiseled, k2, 4, 0, box);
            this.setBlockState(world, orange, k2 + 1, 4, 0, box);
            this.setBlockState(world, smooth, k2 - 1, 5, 0, box);
            this.setBlockState(world, orange, k2, 5, 0, box);
            this.setBlockState(world, smooth, k2 + 1, 5, 0, box);
            this.setBlockState(world, orange, k2 - 1, 6, 0, box);
            this.setBlockState(world, chiseled, k2, 6, 0, box);
            this.setBlockState(world, orange, k2 + 1, 6, 0, box);
            this.setBlockState(world, orange, k2 - 1, 7, 0, box);
            this.setBlockState(world, orange, k2, 7, 0, box);
            this.setBlockState(world, orange, k2 + 1, 7, 0, box);
            this.setBlockState(world, smooth, k2 - 1, 8, 0, box);
            this.setBlockState(world, smooth, k2, 8, 0, box);
            this.setBlockState(world, smooth, k2 + 1, 8, 0, box);
         }
         this.fillWithBlocks(world, box, 8, 4, 0, 12, 6, 0, smooth, smooth, false);
         this.setBlockState(world, air, 8, 6, 0, box);
         this.setBlockState(world, air, 12, 6, 0, box);
         this.setBlockState(world, orange, 9, 5, 0, box);
         this.setBlockState(world, chiseled, 10, 5, 0, box);
         this.setBlockState(world, orange, 11, 5, 0, box);
         this.fillWithBlocks(world, box, 8, -14, 8, 12, -11, 12, smooth, smooth, false);
         this.fillWithBlocks(world, box, 8, -10, 8, 12, -10, 12, chiseled, chiseled, false);
         this.fillWithBlocks(world, box, 8, -9, 8, 12, -9, 12, smooth, smooth, false);
         this.fillWithBlocks(world, box, 8, -8, 8, 12, -1, 12, sandstone, sandstone, false);
         this.fillWithBlocks(world, box, 9, -11, 9, 11, -1, 11, air, air, false);
         this.setBlockState(world, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, box);
         this.fillWithBlocks(world, box, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), air, false);
         this.setBlockState(world, air, 8, -11, 10, box);
         this.setBlockState(world, air, 8, -10, 10, box);
         this.setBlockState(world, chiseled, 7, -10, 10, box);
         this.setBlockState(world, smooth, 7, -11, 10, box);
         this.setBlockState(world, air, 12, -11, 10, box);
         this.setBlockState(world, air, 12, -10, 10, box);
         this.setBlockState(world, chiseled, 13, -10, 10, box);
         this.setBlockState(world, smooth, 13, -11, 10, box);
         this.setBlockState(world, air, 10, -11, 8, box);
         this.setBlockState(world, air, 10, -10, 8, box);
         this.setBlockState(world, chiseled, 10, -10, 7, box);
         this.setBlockState(world, smooth, 10, -11, 7, box);
         this.setBlockState(world, air, 10, -11, 12, box);
         this.setBlockState(world, air, 10, -10, 12, box);
         this.setBlockState(world, chiseled, 10, -10, 13, box);
         this.setBlockState(world, smooth, 10, -11, 13, box);
         for (Direction d : HORIZONTAL) {
            if (!this.hasPlacedChest[d.get2DDataValue()]) {
               int k1 = d.getStepX() * 2;
               int l1 = d.getStepZ() * 2;
               this.hasPlacedChest[d.get2DDataValue()] = this.generateChest(world, box, random, 10 + k1, -11, 10 + l1, BuiltInLootTables.DESERT_PYRAMID);
            }
         }
         return true;
      }
   }

   // ------------------------------------------------------------------ Igloo

   public static final class Igloo extends Feature {
      Igloo(Random random, int x, int z) {
         super("igloo", random, x, 64, z, 7, 5, 8);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, Box box) {
         if (!this.offsetToAverageGroundLevel(world, box, -1)) {
            return false;
         }
         var manager = world.level.getLevel().getStructureManager();
         StructureTemplate top = Templates.get(manager, "rlgen:igloo/igloo_top").orElse(null);
         StructureTemplate middle = Templates.get(manager, "rlgen:igloo/igloo_middle").orElse(null);
         StructureTemplate bottom = Templates.get(manager, "rlgen:igloo/igloo_bottom").orElse(null);
         if (top == null || middle == null || bottom == null) {
            return true;
         }
         BlockPos pos = new BlockPos(this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ);
         Rotation[] rotations = Rotation.values();
         Rotation rotation = rotations[random.nextInt(rotations.length)];
         // addBlocksToWorldChunk drops the component box (PlacementSettings.chunk is null): the whole igloo is placed at once
         // (clipped only to the chunks 1.21 lets this populate step write to)
         StructurePlaceSettings settings = Templates.settings(rotation, Mirror.NONE, Templates.region(world, box.minX >> 4, box.minZ >> 4));
         StructurePlaceSettings unclipped = Templates.settings(rotation, Mirror.NONE, null);
         Templates.place(world, top, pos, settings, 0L);
         if (random.nextDouble() < 0.5) {
            int i = random.nextInt(8) + 4;
            for (int j = 0; j < i; j++) {
               BlockPos p = top.calculateConnectedPosition(unclipped, new BlockPos(3, -1 - j * 3, 5), unclipped, new BlockPos(1, 2, 1));
               Templates.place(world, middle, pos.offset(p), settings, 0L);
            }
            BlockPos p4 = pos.offset(top.calculateConnectedPosition(unclipped, new BlockPos(3, -1 - i * 3, 5), unclipped, new BlockPos(3, 5, 7)));
            Templates.place(world, bottom, p4, settings, 0L);
            for (Map.Entry<BlockPos, String> e : Templates.dataBlocks(bottom, p4, unclipped).entrySet()) {
               if ("chest".equals(e.getValue())) {
                  BlockPos p2 = e.getKey();
                  world.set(p2, Blocks.AIR.defaultBlockState());
                  if (world.level.getBlockEntity(p2.below()) instanceof RandomizableContainerBlockEntity chest) {
                     chest.setLootTable(BuiltInLootTables.IGLOO_CHEST, random.nextLong());
                  }
               }
            }
         } else {
            BlockPos p3 = StructureTemplate.calculateRelativePosition(unclipped, new BlockPos(3, 0, 5));
            world.set(pos.offset(p3), Blocks.SNOW_BLOCK.defaultBlockState());
         }
         return true;
      }
   }

   // ------------------------------------------------------------------ JunglePyramid

   public static final class JunglePyramid extends Feature {
      private boolean placedMainChest;
      private boolean placedHiddenChest;
      private boolean placedTrap1;
      private boolean placedTrap2;
      /** JunglePyramid.Stones */
      private static final BlockSelector COBBLESTONE_SELECTOR = (random, x, y, z, edge) -> random.nextFloat() < 0.4F
         ? Blocks.COBBLESTONE.defaultBlockState()
         : Blocks.MOSSY_COBBLESTONE.defaultBlockState();

      JunglePyramid(Random random, int x, int z) {
         super("jungle_pyramid", random, x, 64, z, 12, 10, 15);
      }

      @Override
      void save(CompoundTag tag) {
         super.save(tag);
         tag.putBoolean("placedMainChest", this.placedMainChest);
         tag.putBoolean("placedHiddenChest", this.placedHiddenChest);
         tag.putBoolean("placedTrap1", this.placedTrap1);
         tag.putBoolean("placedTrap2", this.placedTrap2);
      }

      @Override
      void load(CompoundTag tag) {
         super.load(tag);
         this.placedMainChest = tag.getBoolean("placedMainChest");
         this.placedHiddenChest = tag.getBoolean("placedHiddenChest");
         this.placedTrap1 = tag.getBoolean("placedTrap1");
         this.placedTrap2 = tag.getBoolean("placedTrap2");
      }

      private static BlockState hook(Direction d) {
         return Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, d).setValue(TripWireHookBlock.ATTACHED, true);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, Box box) {
         if (!this.offsetToAverageGroundLevel(world, box, 0)) {
            return false;
         }
         BlockSelector sel = COBBLESTONE_SELECTOR;
         BlockState air = Blocks.AIR.defaultBlockState();
         BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
         BlockState wire = Blocks.REDSTONE_WIRE.defaultBlockState();
         BlockState tripwire = Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, true);
         this.fillWithRandomizedBlocks(world, box, 0, -4, 0, this.width - 1, 0, this.depth - 1, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 1, 2, 9, 2, 2, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 1, 12, 9, 2, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 1, 3, 2, 2, 11, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 9, 1, 3, 9, 2, 11, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 1, 3, 1, 10, 6, 1, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 1, 3, 13, 10, 6, 13, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 1, 3, 2, 1, 6, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 10, 3, 2, 10, 6, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 3, 2, 9, 3, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 6, 2, 9, 6, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 3, 7, 3, 8, 7, 11, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 4, 8, 4, 7, 8, 10, false, random, sel);
         this.fillWithAir(world, box, 3, 1, 3, 8, 2, 11);
         this.fillWithAir(world, box, 4, 3, 6, 7, 3, 9);
         this.fillWithAir(world, box, 2, 4, 2, 9, 5, 12);
         this.fillWithAir(world, box, 4, 6, 5, 7, 6, 9);
         this.fillWithAir(world, box, 5, 7, 6, 6, 7, 8);
         this.fillWithAir(world, box, 5, 1, 2, 6, 2, 2);
         this.fillWithAir(world, box, 5, 2, 12, 6, 2, 12);
         this.fillWithAir(world, box, 5, 5, 1, 6, 5, 1);
         this.fillWithAir(world, box, 5, 5, 13, 6, 5, 13);
         this.setBlockState(world, air, 1, 5, 5, box);
         this.setBlockState(world, air, 10, 5, 5, box);
         this.setBlockState(world, air, 1, 5, 9, box);
         this.setBlockState(world, air, 10, 5, 9, box);
         for (int i = 0; i <= 14; i += 14) {
            this.fillWithRandomizedBlocks(world, box, 2, 4, i, 2, 5, i, false, random, sel);
            this.fillWithRandomizedBlocks(world, box, 4, 4, i, 4, 5, i, false, random, sel);
            this.fillWithRandomizedBlocks(world, box, 7, 4, i, 7, 5, i, false, random, sel);
            this.fillWithRandomizedBlocks(world, box, 9, 4, i, 9, 5, i, false, random, sel);
         }
         this.fillWithRandomizedBlocks(world, box, 5, 6, 0, 6, 6, 0, false, random, sel);
         for (int l = 0; l <= 11; l += 11) {
            for (int j = 2; j <= 12; j += 2) {
               this.fillWithRandomizedBlocks(world, box, l, 4, j, l, 5, j, false, random, sel);
            }
            this.fillWithRandomizedBlocks(world, box, l, 6, 5, l, 6, 5, false, random, sel);
            this.fillWithRandomizedBlocks(world, box, l, 6, 9, l, 6, 9, false, random, sel);
         }
         this.fillWithRandomizedBlocks(world, box, 2, 7, 2, 2, 9, 2, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 9, 7, 2, 9, 9, 2, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 2, 7, 12, 2, 9, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 9, 7, 12, 9, 9, 12, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 4, 9, 4, 4, 9, 4, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 7, 9, 4, 7, 9, 4, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 4, 9, 10, 4, 9, 10, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 7, 9, 10, 7, 9, 10, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 5, 9, 7, 6, 9, 7, false, random, sel);
         BlockState east = stairs(Blocks.COBBLESTONE_STAIRS, Direction.EAST);
         BlockState west = stairs(Blocks.COBBLESTONE_STAIRS, Direction.WEST);
         BlockState south = stairs(Blocks.COBBLESTONE_STAIRS, Direction.SOUTH);
         BlockState north = stairs(Blocks.COBBLESTONE_STAIRS, Direction.NORTH);
         this.setBlockState(world, north, 5, 9, 6, box);
         this.setBlockState(world, north, 6, 9, 6, box);
         this.setBlockState(world, south, 5, 9, 8, box);
         this.setBlockState(world, south, 6, 9, 8, box);
         this.setBlockState(world, north, 4, 0, 0, box);
         this.setBlockState(world, north, 5, 0, 0, box);
         this.setBlockState(world, north, 6, 0, 0, box);
         this.setBlockState(world, north, 7, 0, 0, box);
         this.setBlockState(world, north, 4, 1, 8, box);
         this.setBlockState(world, north, 4, 2, 9, box);
         this.setBlockState(world, north, 4, 3, 10, box);
         this.setBlockState(world, north, 7, 1, 8, box);
         this.setBlockState(world, north, 7, 2, 9, box);
         this.setBlockState(world, north, 7, 3, 10, box);
         this.fillWithRandomizedBlocks(world, box, 4, 1, 9, 4, 1, 9, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 7, 1, 9, 7, 1, 9, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 4, 1, 10, 7, 2, 10, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 5, 4, 5, 6, 4, 5, false, random, sel);
         this.setBlockState(world, east, 4, 4, 5, box);
         this.setBlockState(world, west, 7, 4, 5, box);
         for (int k = 0; k < 4; k++) {
            this.setBlockState(world, south, 5, 0 - k, 6 + k, box);
            this.setBlockState(world, south, 6, 0 - k, 6 + k, box);
            this.fillWithAir(world, box, 5, 0 - k, 7 + k, 6, 0 - k, 9 + k);
         }
         this.fillWithAir(world, box, 1, -3, 12, 10, -1, 13);
         this.fillWithAir(world, box, 1, -3, 1, 3, -1, 13);
         this.fillWithAir(world, box, 1, -3, 1, 9, -1, 5);
         for (int i1 = 1; i1 <= 13; i1 += 2) {
            this.fillWithRandomizedBlocks(world, box, 1, -3, i1, 1, -2, i1, false, random, sel);
         }
         for (int j1 = 2; j1 <= 12; j1 += 2) {
            this.fillWithRandomizedBlocks(world, box, 1, -1, j1, 3, -1, j1, false, random, sel);
         }
         this.fillWithRandomizedBlocks(world, box, 2, -2, 1, 5, -2, 1, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 7, -2, 1, 9, -2, 1, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 6, -3, 1, 6, -3, 1, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 6, -1, 1, 6, -1, 1, false, random, sel);
         this.setBlockState(world, hook(Direction.EAST), 1, -3, 8, box);
         this.setBlockState(world, hook(Direction.WEST), 4, -3, 8, box);
         this.setBlockState(world, tripwire, 2, -3, 8, box);
         this.setBlockState(world, tripwire, 3, -3, 8, box);
         this.setBlockState(world, wire, 5, -3, 7, box);
         this.setBlockState(world, wire, 5, -3, 6, box);
         this.setBlockState(world, wire, 5, -3, 5, box);
         this.setBlockState(world, wire, 5, -3, 4, box);
         this.setBlockState(world, wire, 5, -3, 3, box);
         this.setBlockState(world, wire, 5, -3, 2, box);
         this.setBlockState(world, wire, 5, -3, 1, box);
         this.setBlockState(world, wire, 4, -3, 1, box);
         this.setBlockState(world, mossy, 3, -3, 1, box);
         if (!this.placedTrap1) {
            this.placedTrap1 = this.createDispenser(world, box, random, 3, -2, 1, Direction.NORTH, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }
         this.setBlockState(world, Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true), 3, -2, 2, box);
         this.setBlockState(world, hook(Direction.NORTH), 7, -3, 1, box);
         this.setBlockState(world, hook(Direction.SOUTH), 7, -3, 5, box);
         this.setBlockState(world, tripwire, 7, -3, 2, box);
         this.setBlockState(world, tripwire, 7, -3, 3, box);
         this.setBlockState(world, tripwire, 7, -3, 4, box);
         this.setBlockState(world, wire, 8, -3, 6, box);
         this.setBlockState(world, wire, 9, -3, 6, box);
         this.setBlockState(world, wire, 9, -3, 5, box);
         this.setBlockState(world, mossy, 9, -3, 4, box);
         this.setBlockState(world, wire, 9, -2, 4, box);
         if (!this.placedTrap2) {
            this.placedTrap2 = this.createDispenser(world, box, random, 9, -2, 3, Direction.WEST, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }
         this.setBlockState(world, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true), 8, -1, 3, box);
         this.setBlockState(world, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true), 8, -2, 3, box);
         if (!this.placedMainChest) {
            this.placedMainChest = this.generateChest(world, box, random, 8, -3, 3, BuiltInLootTables.JUNGLE_TEMPLE);
         }
         this.setBlockState(world, mossy, 9, -3, 2, box);
         this.setBlockState(world, mossy, 8, -3, 1, box);
         this.setBlockState(world, mossy, 4, -3, 5, box);
         this.setBlockState(world, mossy, 5, -2, 5, box);
         this.setBlockState(world, mossy, 5, -1, 5, box);
         this.setBlockState(world, mossy, 6, -3, 5, box);
         this.setBlockState(world, mossy, 7, -2, 5, box);
         this.setBlockState(world, mossy, 7, -1, 5, box);
         this.setBlockState(world, mossy, 8, -3, 5, box);
         this.fillWithRandomizedBlocks(world, box, 9, -1, 1, 9, -1, 5, false, random, sel);
         this.fillWithAir(world, box, 8, -3, 8, 10, -1, 10);
         BlockState chiseledBricks = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
         this.setBlockState(world, chiseledBricks, 8, -2, 11, box);
         this.setBlockState(world, chiseledBricks, 9, -2, 11, box);
         this.setBlockState(world, chiseledBricks, 10, -2, 11, box);
         BlockState lever = Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, Direction.NORTH);
         this.setBlockState(world, lever, 8, -2, 12, box);
         this.setBlockState(world, lever, 9, -2, 12, box);
         this.setBlockState(world, lever, 10, -2, 12, box);
         this.fillWithRandomizedBlocks(world, box, 8, -3, 8, 8, -3, 10, false, random, sel);
         this.fillWithRandomizedBlocks(world, box, 10, -3, 8, 10, -3, 10, false, random, sel);
         this.setBlockState(world, mossy, 10, -2, 9, box);
         this.setBlockState(world, wire, 8, -2, 9, box);
         this.setBlockState(world, wire, 8, -2, 10, box);
         this.setBlockState(world, wire, 10, -1, 9, box);
         this.setBlockState(world, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP), 9, -2, 8, box);
         this.setBlockState(world, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -2, 8, box);
         this.setBlockState(world, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -1, 8, box);
         this.setBlockState(world, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.NORTH), 10, -2, 10, box);
         if (!this.placedHiddenChest) {
            this.placedHiddenChest = this.generateChest(world, box, random, 9, -3, 10, BuiltInLootTables.JUNGLE_TEMPLE);
         }
         return true;
      }
   }

   // ------------------------------------------------------------------ SwampHut

   public static final class SwampHut extends Feature {
      private boolean hasWitch;

      SwampHut(Random random, int x, int z) {
         super("swamp_hut", random, x, 64, z, 7, 7, 9);
      }

      @Override
      void save(CompoundTag tag) {
         super.save(tag);
         tag.putBoolean("Witch", this.hasWitch);
      }

      @Override
      void load(CompoundTag tag) {
         super.load(tag);
         this.hasWitch = tag.getBoolean("Witch");
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, Box box) {
         if (!this.offsetToAverageGroundLevel(world, box, 0)) {
            return false;
         }
         BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
         BlockState log = Blocks.OAK_LOG.defaultBlockState();
         BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
         BlockState air = Blocks.AIR.defaultBlockState();
         this.fillWithBlocks(world, box, 1, 1, 1, 5, 1, 7, planks, planks, false);
         this.fillWithBlocks(world, box, 1, 4, 2, 5, 4, 7, planks, planks, false);
         this.fillWithBlocks(world, box, 2, 1, 0, 4, 1, 0, planks, planks, false);
         this.fillWithBlocks(world, box, 2, 2, 2, 3, 3, 2, planks, planks, false);
         this.fillWithBlocks(world, box, 1, 2, 3, 1, 3, 6, planks, planks, false);
         this.fillWithBlocks(world, box, 5, 2, 3, 5, 3, 6, planks, planks, false);
         this.fillWithBlocks(world, box, 2, 2, 7, 4, 3, 7, planks, planks, false);
         this.fillWithBlocks(world, box, 1, 0, 2, 1, 3, 2, log, log, false);
         this.fillWithBlocks(world, box, 5, 0, 2, 5, 3, 2, log, log, false);
         this.fillWithBlocks(world, box, 1, 0, 7, 1, 3, 7, log, log, false);
         this.fillWithBlocks(world, box, 5, 0, 7, 5, 3, 7, log, log, false);
         this.setBlockState(world, fence, 2, 3, 2, box);
         this.setBlockState(world, fence, 3, 3, 7, box);
         this.setBlockState(world, air, 1, 3, 4, box);
         this.setBlockState(world, air, 5, 3, 4, box);
         this.setBlockState(world, air, 5, 3, 5, box);
         this.setBlockState(world, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, box);
         this.setBlockState(world, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, box);
         this.setBlockState(world, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, box);
         this.setBlockState(world, fence, 1, 2, 1, box);
         this.setBlockState(world, fence, 5, 2, 1, box);
         BlockState north = stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH);
         BlockState east = stairs(Blocks.SPRUCE_STAIRS, Direction.EAST);
         BlockState west = stairs(Blocks.SPRUCE_STAIRS, Direction.WEST);
         BlockState south = stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH);
         this.fillWithBlocks(world, box, 0, 4, 1, 6, 4, 1, north, north, false);
         this.fillWithBlocks(world, box, 0, 4, 2, 0, 4, 7, east, east, false);
         this.fillWithBlocks(world, box, 6, 4, 2, 6, 4, 7, west, west, false);
         this.fillWithBlocks(world, box, 0, 4, 8, 6, 4, 8, south, south, false);
         for (int i = 2; i <= 7; i += 5) {
            for (int j = 1; j <= 5; j += 4) {
               this.replaceAirAndLiquidDownwards(world, log, j, -1, i, box);
            }
         }
         if (!this.hasWitch) {
            int l = this.getXWithOffset(2, 5);
            int i1 = this.getYWithOffset(2);
            int k = this.getZWithOffset(2, 5);
            if (box.inside(l, i1, k)) {
               this.hasWitch = true;
               ServerLevel level = world.level.getLevel();
               Witch witch = EntityType.WITCH.create(level);
               if (witch != null) {
                  witch.setPersistenceRequired();
                  witch.moveTo(l + 0.5, i1, k + 0.5, 0.0F, 0.0F);
                  witch.finalizeSpawn(world.level, world.level.getCurrentDifficultyAt(new BlockPos(l, i1, k)), MobSpawnType.STRUCTURE, null);
                  world.level.addFreshEntityWithPassengers(witch);
               }
            }
         }
         return true;
      }
   }
}
