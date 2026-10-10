package jaredbgreat.dldungeons.planner.features;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.RoomSeed;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.Degree;

public class IslandRoom extends FeatureAdder {
   private boolean built;

   public IslandRoom(Degree chance) {
      super(chance);
   }

   @Override
   public boolean addFeature(Dungeon dungeon, Room room) {
      this.built = this.chance.use(dungeon.random);
      if (this.built) {
         this.buildFeature(dungeon, room);
      }

      return this.built;
   }

   @Override
   public void buildFeature(Dungeon dungeon, Room room) {
      this.built = this.buildSubroom(dungeon, room);
   }

   private boolean buildSubroom(Dungeon dungeon, Room room) {
      int dimX = (int)((float)(room.endX - room.beginX) * (0.2F + 0.3F * dungeon.random.nextFloat()));
      int dimZ = (int)((float)(room.endZ - room.beginZ) * (0.2F + 0.3F * dungeon.random.nextFloat()));
      float centerX = (float)(dungeon.random.nextInt(room.endX - room.beginX) + room.beginX);
      float centerZ = (float)(dungeon.random.nextInt(room.endZ - room.beginZ) + room.beginZ);
      float oppX = (float)room.endX - (centerX - (float)room.beginX);
      float oppZ = (float)room.endZ - (centerZ - (float)room.beginZ);
      if (room.sym.halfX) {
         dimX *= 2;
         dimX /= 3;
         oppX = (float)room.endX - (centerX - (float)room.beginX) / 2.0F;
         centerX = (centerX - (float)room.beginX) / 2.0F + (float)room.beginX;
      }

      if (room.sym.halfZ) {
         dimZ *= 2;
         dimZ /= 3;
         oppZ = (float)room.endZ - (centerZ - (float)room.beginZ) / 2.0F;
         centerZ = (centerZ - (float)room.beginZ) / 2.0F + (float)room.beginZ;
      }

      if (room.sym.doubler) {
         dimX = (int)((double)dimX * 0.75);
         dimZ = (int)((double)dimZ * 0.75);
      }

      if (dimX >= 5 && dimZ >= 5) {
         int ymod = dimX <= dimZ ? (int)Math.sqrt((double)dimX) : (int)Math.sqrt((double)dimZ);
         int height = dungeon.random.nextInt(dungeon.verticle.value / 2 + ymod + 1) + 2;
         Room created = new RoomSeed((int)centerX, room.floorY, (int)centerZ).growRoom(dimX, dimZ, height, dungeon, room, room);
         if (created == null) {
            return false;
         } else {
            switch (room.sym) {
               case NONE:
               default:
                  break;
               case TR1:
                  oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
                  oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
                  created = new RoomSeed((int)oppX, room.floorY, (int)oppZ).growRoom(dimZ, dimX, height, dungeon, room, room);
                  break;
               case TR2:
                  oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
                  oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
                  oppZ = (float)room.endZ - (oppZ - (float)room.beginZ);
                  created = new RoomSeed((int)oppX, room.floorY, (int)oppZ).growRoom(dimZ, dimX, height, dungeon, room, room);
                  break;
               case X:
                  created = new RoomSeed((int)oppX, room.floorY, (int)centerZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  break;
               case Z:
                  created = new RoomSeed((int)centerX, room.floorY, (int)oppZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  break;
               case XZ:
                  created = new RoomSeed((int)oppX, room.floorY, (int)centerZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  created = new RoomSeed((int)centerX, room.floorY, (int)oppZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  created = new RoomSeed((int)oppX, room.floorY, (int)oppZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  break;
               case R:
                  created = new RoomSeed((int)oppX, room.floorY, (int)oppZ).growRoom(dimX, dimZ, height, dungeon, room, room);
                  break;
               case SW:
                  float swX1 = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
                  float swZ1 = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
                  float swX2 = room.realX + (oppZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
                  float swZ2 = room.realZ + (oppX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
                  created = new RoomSeed((int)swX2, room.floorY, (int)swZ1).growRoom(dimZ, dimX, height, dungeon, room, room);
                  created = new RoomSeed((int)swX1, room.floorY, (int)swZ2).growRoom(dimZ, dimX, height, dungeon, room, room);
                  created = new RoomSeed((int)oppX, room.floorY, (int)oppZ).growRoom(dimX, dimZ, height, dungeon, room, room);
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
