package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.ConfigHandler;
import jaredbgreat.dldungeons.rooms.Room;
import java.util.Random;

public class Node {
   Room hubRoom;

   public Node(int x, int y, int z, Random random, Dungeon dungeon) {
      int xdim = random.nextInt(dungeon.size.maxRoomSize / 2 - 3) + dungeon.size.maxRoomSize / 2 + 4;
      int zdim = random.nextInt(dungeon.size.maxRoomSize / 2 - 3) + dungeon.size.maxRoomSize / 2 + 4;
      int ymod = xdim <= zdim ? (int)Math.sqrt((double)xdim) : (int)Math.sqrt((double)zdim);
      int height = random.nextInt(dungeon.verticle.value / 2 + ymod + 1) + 2;
      if (ConfigHandler.bigHubs) {
         height = Math.min(12, Math.max(7, Math.max(height * 2, height + dungeon.random.nextInt(3) + 2)));
      }

      this.hubRoom = new RoomSeed(x, y, z).growRoom(xdim, zdim, height, dungeon, null, null);
   }
}
