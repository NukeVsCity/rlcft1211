package jaredbgreat.dldungeons.pieces.entrances;

import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.compat.World;

public class TopRoom extends AbstractEntrance {
   int wx;
   int wz;
   int bottom;
   int top;
   int xdim;
   int ydim;
   int zdim;
   int ymod;
   int xmin;
   int xmax;
   int zmin;
   int zmax;
   int below;

   public TopRoom(int x, int z) {
      super(x, z);
   }

   @Override
   public void build(Dungeon dungeon, World world) {
      this.wx = this.x + dungeon.map.chunkX * 16 - dungeon.map.room.length / 2 + 8;
      this.wz = this.z + dungeon.map.chunkZ * 16 - dungeon.map.room.length / 2 + 8;
      this.bottom = dungeon.map.floorY[this.x][this.z];
      this.top = world.getActualHeight();

      while (!RegisteredBlock.isGroundBlock(world, this.wx, this.top, this.wz)) {
         this.top--;
      }

      this.xdim = dungeon.random.nextInt(7) + 6;
      this.zdim = dungeon.random.nextInt(7) + 6;
      this.ymod = this.xdim <= this.zdim ? (int)Math.sqrt((double)this.xdim) : (int)Math.sqrt((double)this.zdim);
      this.ydim = dungeon.random.nextInt(dungeon.verticle.value / 2 + this.ymod / 2 + 2) + 2;
      this.xmin = this.wx - this.xdim / 2;
      this.xmax = this.wx + this.xdim / 2;
      this.zmin = this.wz - this.zdim / 2;
      this.zmax = this.wz + this.zdim / 2;
      if (!dungeon.random.nextBoolean() && !dungeon.degeneracy.use(dungeon.random)) {
         this.buidBuilding(dungeon, world);
      } else {
         this.buildRuin(dungeon, world);
      }

      if (dungeon.random.nextBoolean()) {
         this.buildLatter(world, dungeon);
      } else {
         this.buildStair(world, dungeon);
      }
   }

   private void buidBuilding(Dungeon dungeon, World world) {
      for (int i = this.xmin + 1; i < this.xmax; i++) {
         for (int j = this.zmin + 1; j < this.zmax; j++) {
            RegisteredBlock.place(world, i, this.top, j, dungeon.floorBlock);

            for (int k = this.top + this.ydim * 2; k >= this.top + 1; k--) {
               RegisteredBlock.deleteBlock(world, i, k, j);
            }

            for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, j); this.below--) {
               RegisteredBlock.place(world, i, this.below, j, dungeon.floorBlock);
            }
         }
      }

      for (int i = this.xmin; i <= this.xmax; i++) {
         for (int j = this.top + this.ydim; j >= this.top; j--) {
            RegisteredBlock.place(world, i, j, this.zmax, dungeon.wallBlock1);
            RegisteredBlock.place(world, i, j, this.zmin, dungeon.wallBlock1);
         }

         if (dungeon.random.nextInt(8) == 0) {
            for (int j = this.top + 2; j > this.top; j--) {
               RegisteredBlock.deleteBlock(world, i, j, this.zmax);
               RegisteredBlock.deleteBlock(world, i, j, this.zmin);
            }
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, this.zmax); this.below--) {
            RegisteredBlock.place(world, i, this.below, this.zmax, dungeon.wallBlock1);
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, this.zmin); this.below--) {
            RegisteredBlock.place(world, i, this.below, this.zmin, dungeon.floorBlock);
         }
      }

      for (int i = this.zmin; i <= this.zmax; i++) {
         for (int j = this.top + this.ydim; j >= this.top; j--) {
            RegisteredBlock.place(world, this.xmin, j, i, dungeon.wallBlock1);
            RegisteredBlock.place(world, this.xmax, j, i, dungeon.wallBlock1);
         }

         if (dungeon.random.nextInt(8) == 0) {
            for (int j = this.top + 2; j > this.top; j--) {
               RegisteredBlock.deleteBlock(world, this.xmax, j, i);
               RegisteredBlock.deleteBlock(world, this.xmin, j, i);
            }
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, this.xmax, this.below, i); this.below--) {
            RegisteredBlock.place(world, this.xmax, this.below, i, dungeon.wallBlock1);
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, this.xmin, this.below, i); this.below--) {
            RegisteredBlock.place(world, this.xmin, this.below, i, dungeon.floorBlock);
         }
      }

      for (int i = this.xmin; i <= this.xmax; i++) {
         for (int j = this.zmin; j <= this.zmax; j++) {
            RegisteredBlock.place(world, i, this.top + this.ydim + 1, j, dungeon.cielingBlock);
         }
      }
   }

   private void buildRuin(Dungeon dungeon, World world) {
      for (int i = this.xmin + 1; i < this.xmax; i++) {
         for (int j = this.zmin + 1; j < this.zmax; j++) {
            RegisteredBlock.place(world, i, this.top, j, dungeon.floorBlock);

            for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, j); this.below--) {
               RegisteredBlock.place(world, i, this.below, j, dungeon.floorBlock);
            }
         }
      }

      for (int i = this.xmin; i <= this.xmax; i++) {
         for (int j = this.top + this.ydim - dungeon.random.nextInt(3); j >= this.top; j--) {
            RegisteredBlock.place(world, i, j, this.zmax, dungeon.wallBlock1);
            RegisteredBlock.place(world, i, j, this.zmin, dungeon.wallBlock1);
         }

         if (dungeon.random.nextInt(8) == 0) {
            for (int j = this.top + 2; j > this.top; j--) {
               RegisteredBlock.deleteBlock(world, i, j, this.zmax);
               RegisteredBlock.deleteBlock(world, i, j, this.zmin);
            }
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, this.zmax); this.below--) {
            RegisteredBlock.place(world, i, this.below, this.zmax, dungeon.wallBlock1);
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, i, this.below, this.zmin); this.below--) {
            RegisteredBlock.place(world, i, this.below, this.zmin, dungeon.floorBlock);
         }
      }

      for (int i = this.zmin; i <= this.zmax; i++) {
         for (int j = this.top + this.ydim - dungeon.random.nextInt(3); j >= this.top; j--) {
            RegisteredBlock.place(world, this.xmin, j, i, dungeon.wallBlock1);
            RegisteredBlock.place(world, this.xmax, j, i, dungeon.wallBlock1);
         }

         if (dungeon.random.nextInt(8) == 0) {
            for (int j = this.top + 2; j > this.top; j--) {
               RegisteredBlock.deleteBlock(world, this.xmax, j, i);
               RegisteredBlock.deleteBlock(world, this.xmin, j, i);
            }
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, this.xmax, this.below, i); this.below--) {
            RegisteredBlock.place(world, this.xmax, this.below, i, dungeon.wallBlock1);
         }

         for (this.below = this.top - 1; !RegisteredBlock.isGroundBlock(world, this.xmin, this.below, i); this.below--) {
            RegisteredBlock.place(world, this.xmin, this.below, i, dungeon.floorBlock);
         }
      }
   }

   private void buildLatter(World world, Dungeon dungeon) {
      this.top = this.top + this.ydim;
      int side = dungeon.random.nextInt(4);
      switch (side) {
         case 0:
            for (int i = this.bottom; i <= this.top; i++) {
               RegisteredBlock.place(world, this.wx, i, this.wz, dungeon.wallBlock1);
               RegisteredBlock.placeBlock(world, this.wx + 1, i, this.wz, LADDER, 5, 3);
            }
            break;
         case 1:
            for (int i = this.bottom; i <= this.top; i++) {
               RegisteredBlock.place(world, this.wx, i, this.wz, dungeon.wallBlock1);
               RegisteredBlock.placeBlock(world, this.wx, i, this.wz + 1, LADDER, 3, 3);
            }
            break;
         case 2:
            for (int i = this.bottom; i <= this.top; i++) {
               RegisteredBlock.place(world, this.wx, i, this.wz, dungeon.wallBlock1);
               RegisteredBlock.placeBlock(world, this.wx - 1, i, this.wz, LADDER, 4, 3);
            }
            break;
         case 3:
            for (int i = this.bottom; i <= this.top; i++) {
               RegisteredBlock.place(world, this.wx, i, this.wz, dungeon.wallBlock1);
               RegisteredBlock.placeBlock(world, this.wx, i, this.wz - 1, LADDER, 2, 3);
            }
      }
   }

   private void buildStair(World world, Dungeon dungeon) {
      this.top++;
      int side = dungeon.random.nextInt(4);

      for (int i = this.bottom; i < this.top; i++) {
         RegisteredBlock.place(world, this.wx, i, this.wz, dungeon.wallBlock1);
         switch (side) {
            case 0:
               RegisteredBlock.placeBlock(world, this.wx + 1, i, this.wz, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, this.wx + 1, i, this.wz + 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz - 1);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz - 1);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz - 1);
               break;
            case 1:
               RegisteredBlock.placeBlock(world, this.wx, i, this.wz + 1, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, this.wx - 1, i, this.wz + 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz - 1);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz - 1);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz - 1);
               break;
            case 2:
               RegisteredBlock.placeBlock(world, this.wx - 1, i, this.wz, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, this.wx - 1, i, this.wz - 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz - 1);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz - 1);
               break;
            case 3:
               RegisteredBlock.placeBlock(world, this.wx, i, this.wz - 1, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, this.wx + 1, i, this.wz - 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx + 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz + 1);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz);
               RegisteredBlock.deleteBlock(world, this.wx - 1, i, this.wz - 1);
         }

         side = (side + 1) % 4;
      }
   }
}
