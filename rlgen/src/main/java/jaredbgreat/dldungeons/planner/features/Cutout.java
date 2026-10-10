package jaredbgreat.dldungeons.planner.features;

import jaredbgreat.dldungeons.pieces.Shape;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.Degree;

public class Cutout extends FeatureAdder {
   public Cutout(Degree chance) {
      super(chance);
   }

   @Override
   public boolean addFeature(Dungeon dungeon, Room room) {
      boolean built = this.chance.use(dungeon.random);
      if (built) {
         this.buildFeature(dungeon, room);
      }

      return built;
   }

   @Override
   public void buildFeature(Dungeon dungeon, Room room) {
      int rotation = dungeon.random.nextInt(4);
      float dimX = (float)(room.endX - room.beginX) * (dungeon.random.nextFloat() * 0.2F + 0.1F);
      float dimZ = (float)(room.endX - room.beginX) * (dungeon.random.nextFloat() * 0.2F + 0.1F);
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
         dimX = (float)((double)dimX * 0.7);
         dimZ = (float)((double)dimZ * 0.7);
      }

      centerX++;
      centerZ++;
      oppX++;
      oppZ++;
      Shape[] which;
      if (!dungeon.complexity.use(dungeon.random)) {
         which = Shape.xgroup;
      } else {
         which = Shape.allSolids[dungeon.random.nextInt(Shape.allSolids.length)];
      }

      which[rotation].drawCutout(dungeon, room, centerX, centerZ, dimX, dimZ, false, false);
      switch (room.sym) {
         case NONE:
         default:
            break;
         case TR1:
            oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
            oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
            which[(rotation + 1) % 4].drawCutout(dungeon, room, oppX, oppZ, dimX, dimZ, false, false);
            break;
         case TR2:
            oppX = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
            oppZ = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
            oppZ = (float)room.endZ - (oppZ - (float)room.beginZ);
            which[(rotation + 1) % 4].drawCutout(dungeon, room, oppX, oppZ, dimX, dimZ, false, true);
            break;
         case X:
            which[rotation].drawCutout(dungeon, room, oppX, centerZ, dimX, dimZ, true, false);
            break;
         case Z:
            which[rotation].drawCutout(dungeon, room, centerX, oppZ, dimX, dimZ, false, true);
            break;
         case XZ:
            which[rotation].drawCutout(dungeon, room, oppX, centerZ, dimX, dimZ, true, false);
            which[rotation].drawCutout(dungeon, room, centerX, oppZ, dimX, dimZ, false, true);
            which[rotation].drawCutout(dungeon, room, oppX, oppZ, dimX, dimZ, true, true);
            break;
         case R:
            which[(rotation + 2) % 4].drawCutout(dungeon, room, oppX, oppZ, dimX, dimZ, false, false);
            break;
         case SW:
            float swX1 = room.realX + (centerZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
            float swZ1 = room.realZ + (centerX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
            float swX2 = room.realX + (oppZ - room.realZ) / (float)(room.endZ - room.beginZ) * (float)(room.endX - room.beginX);
            float swZ2 = room.realZ + (oppX - room.realX) / (float)(room.endX - room.beginX) * (float)(room.endZ - room.beginZ);
            which[(rotation + 1) % 4].drawCutout(dungeon, room, swX2, swZ1, dimX, dimZ, false, false);
            which[(rotation + 3) % 4].drawCutout(dungeon, room, swX1, swZ2, dimX, dimZ, false, false);
            which[(rotation + 2) % 4].drawCutout(dungeon, room, oppX, oppZ, dimX, dimZ, false, false);
      }
   }
}
