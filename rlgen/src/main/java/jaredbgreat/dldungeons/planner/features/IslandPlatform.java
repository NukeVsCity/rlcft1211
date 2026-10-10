package jaredbgreat.dldungeons.planner.features;

import jaredbgreat.dldungeons.pieces.Shape;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.Degree;

public class IslandPlatform extends FeatureAdder {
   protected boolean isDepression = this instanceof Depression;

   public IslandPlatform(Degree chance) {
      super(chance);
   }

   @Override
   public void buildFeature(Dungeon dungeon, Room room) {
      int available = room.ceilY - room.floorY;
      if (available >= 4) {
         int rotation = dungeon.random.nextInt(4);
         float dimX = (float)(room.endX - room.beginX) * (dungeon.random.nextFloat() * 0.25F + 0.15F);
         float dimZ = (float)(room.endX - room.beginX) * (dungeon.random.nextFloat() * 0.25F + 0.15F);
         float centerX = (float)(dungeon.random.nextInt(room.endX - room.beginX - 1) + room.beginX + 1);
         float centerZ = (float)(dungeon.random.nextInt(room.endZ - room.beginZ - 1) + room.beginZ + 1);
         float oppX = (float)room.endX - (centerX - (float)room.beginX);
         float oppZ = (float)room.endZ - (centerZ - (float)room.beginZ);
         if (room.sym.halfX) {
            dimX *= 2.0F;
            dimX /= 3.0F;
            oppX = (float)room.endX - (centerX - (float)room.beginX) / 2.0F;
            centerX = (centerX - (float)room.beginX) / 2.0F + (float)room.beginX;
         }

         if (room.sym.halfZ) {
            dimZ *= 2.0F;
            dimZ /= 3.0F;
            oppZ = (float)room.endZ - (centerZ - (float)room.beginZ) / 2.0F;
            centerZ = (centerZ - (float)room.beginZ) / 2.0F + (float)room.beginZ;
         }

         if (room.sym.doubler) {
            dimX = (float)((double)dimX * 0.75);
            dimZ = (float)((double)dimZ * 0.75);
         }

         centerX++;
         centerZ++;
         oppX++;
         oppZ++;
         byte platY;
         if (this.isDepression) {
            available -= 2;
            if (available > dungeon.verticle.value / 2 + 1) {
               available = dungeon.verticle.value / 2 + 1;
            }

            platY = (byte)(room.floorY - dungeon.random.nextInt(dungeon.verticle.value / 2 + 1) - 1);
         } else {
            platY = (byte)(room.floorY + 1 + dungeon.random.nextInt(2));
            if (available > 4) {
               platY += (byte)dungeon.random.nextInt(available - 3);
            }
         }

         Shape[] which;
         if (!dungeon.random.nextBoolean() && dungeon.complexity.use(dungeon.random)) {
            which = Shape.allSolids[dungeon.random.nextInt(Shape.allSolids.length)];
         } else {
            which = Shape.xgroup;
         }

         if (platY > room.nFloorY) {
            room.nFloorY = platY;
         }

         which[rotation].drawPlatform(dungeon, room, platY, centerX, centerZ, dimX, dimZ, false, false);
         switch (room.sym) {
            case NONE:
            default:
               break;
            case TR1:
               oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
               oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
               which[(rotation + 3) % 4].drawPlatform(dungeon, room, platY, oppX, oppZ, dimX, dimZ, false, false);
               break;
            case TR2:
               oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
               oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
               oppZ = (float)room.endZ - (oppZ - (float)room.beginZ);
               which[(rotation + 3) % 4].drawPlatform(dungeon, room, platY, oppX, oppZ, dimX, dimZ, false, true);
               break;
            case X:
               which[rotation].drawPlatform(dungeon, room, platY, oppX, centerZ, dimX, dimZ, true, false);
               break;
            case Z:
               which[rotation].drawPlatform(dungeon, room, platY, centerX, oppZ, dimX, dimZ, false, true);
               break;
            case XZ:
               which[rotation].drawPlatform(dungeon, room, platY, oppX, centerZ, dimX, dimZ, true, false);
               which[rotation].drawPlatform(dungeon, room, platY, centerX, oppZ, dimX, dimZ, false, true);
               which[rotation].drawPlatform(dungeon, room, platY, oppX, oppZ, dimX, dimZ, true, true);
               break;
            case R:
               which[(rotation + 2) % 4].drawPlatform(dungeon, room, platY, oppX, oppZ, dimX, dimZ, false, false);
               break;
            case SW:
               float swX1 = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
               float swZ1 = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
               float swX2 = room.realX + (oppZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
               float swZ2 = room.realZ + (oppX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
               which[(rotation + 1) % 4].drawPlatform(dungeon, room, platY, swX2, swZ1, dimX, dimZ, false, false);
               which[(rotation + 3) % 4].drawPlatform(dungeon, room, platY, swX1, swZ2, dimX, dimZ, false, false);
               which[(rotation + 2) % 4].drawPlatform(dungeon, room, platY, oppX, oppZ, dimX, dimZ, false, false);
         }
      }
   }
}
