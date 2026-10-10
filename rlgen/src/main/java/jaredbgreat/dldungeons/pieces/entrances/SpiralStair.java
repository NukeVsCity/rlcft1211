package jaredbgreat.dldungeons.pieces.entrances;

import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.compat.World;

public class SpiralStair extends AbstractEntrance {
   public SpiralStair(int x, int z) {
      super(x, z);
   }

   @Override
   public void build(Dungeon dungeon, World world) {
      int wx = this.x + dungeon.map.chunkX * 16 - dungeon.map.room.length / 2 + 8;
      int wz = this.z + dungeon.map.chunkZ * 16 - dungeon.map.room.length / 2 + 8;
      int bottom = dungeon.map.floorY[this.x][this.z];
      int top = world.getActualHeight();

      while (!RegisteredBlock.isGroundBlock(world, wx, top, wz)) {
         top--;
      }

      top++;
      int side = dungeon.random.nextInt(4);

      for (int i = bottom; i < top; i++) {
         RegisteredBlock.place(world, wx, i, wz, dungeon.wallBlock1);
         switch (side) {
            case 0:
               RegisteredBlock.placeBlock(world, wx + 1, i, wz, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, wx + 1, i, wz + 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, wx, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz - 1);
               RegisteredBlock.deleteBlock(world, wx, i, wz - 1);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz - 1);
               break;
            case 1:
               RegisteredBlock.placeBlock(world, wx, i, wz + 1, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, wx - 1, i, wz + 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz - 1);
               RegisteredBlock.deleteBlock(world, wx, i, wz - 1);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz - 1);
               break;
            case 2:
               RegisteredBlock.placeBlock(world, wx - 1, i, wz, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, wx - 1, i, wz - 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx, i, wz - 1);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz - 1);
               break;
            case 3:
               RegisteredBlock.placeBlock(world, wx, i, wz - 1, STAIR_SLAB, 0, 3);
               RegisteredBlock.placeBlock(world, wx + 1, i, wz - 1, STAIR_SLAB, 8, 3);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx + 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz + 1);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz);
               RegisteredBlock.deleteBlock(world, wx - 1, i, wz - 1);
         }

         side = (side + 1) % 4;
      }
   }
}
