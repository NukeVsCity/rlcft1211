package jaredbgreat.dldungeons.planner.astar;

import jaredbgreat.dldungeons.pieces.Doorway;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.ThemeFlags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DoorChecker {
   static DoorChecker.CompareLists c = new DoorChecker.CompareLists();

   public static boolean validateTile(Dungeon dungeon, int x, int z) {
      if (x < 0 || x >= dungeon.size.width) {
         return false;
      } else {
         return z >= 0 && z < dungeon.size.width ? dungeon.map.room[x][z] > 0 : false;
      }
   }

   public static boolean validateDoor(Dungeon dungeon, Doorway door) {
      return validateTile(dungeon, door.x - 1, door.z)
         && validateTile(dungeon, door.x + 1, door.z)
         && validateTile(dungeon, door.x, door.z - 1)
         && validateTile(dungeon, door.x, door.z + 1)
         && validateTile(dungeon, door.x - 1, door.z - 1)
         && validateTile(dungeon, door.x + 1, door.z + 1)
         && validateTile(dungeon, door.x + 1, door.z - 1)
         && validateTile(dungeon, door.x - 1, door.z + 1);
   }

   public static ArrayList<Doorway> makeConnectionList(Room room, Random random) {
      ArrayList<Doorway> out = new ArrayList<>(room.connections.size());

      for (DoorQueue exits : room.connections) {
         out.add(exits.peek());
      }

      Collections.shuffle(out, random);
      return out;
   }

   public static int getOtherRoom(Doorway exit, Room room, Dungeon dungeon) {
      if (exit.xOriented) {
         if (dungeon.map.room[exit.x + 1][exit.z] == room.id) {
            return dungeon.map.room[exit.x - 1][exit.z];
         }

         if (dungeon.map.room[exit.x - 1][exit.z] == room.id) {
            return dungeon.map.room[exit.x + 1][exit.z];
         }
      } else {
         if (dungeon.map.room[exit.x][exit.z + 1] == room.id) {
            return dungeon.map.room[exit.x][exit.z - 1];
         }

         if (dungeon.map.room[exit.x][exit.z - 1] == room.id) {
            return dungeon.map.room[exit.x][exit.z + 1];
         }
      }

      return 0;
   }

   public static void checkConnections(ArrayList<Doorway> exits, Room room, Dungeon dungeon) {
      if (!exits.isEmpty()) {
         ArrayList<Doorway> connected = new ArrayList<>(exits.size());
         connected.add(exits.remove(exits.size() - 1));

         for (int i = exits.size() - 1; i >= 0; i--) {
            Doorway current = connected.get(dungeon.random.nextInt(connected.size()));
            Doorway next;
            connected.add(next = exits.get(i));
            new AStar(room, dungeon, current, next).seek();
         }
      }
   }

   public static void retestDoors(Dungeon dungeon, Room room) {
      if (!room.doors.isEmpty()) {
         for (Doorway door : room.doors) {
            if (!dungeon.map.astared[door.x][door.z]) {
               if (door.xOriented) {
                  if (dungeon.map.isWall[door.x + 1][door.z] || dungeon.map.isWall[door.x - 1][door.z]) {
                     dungeon.map.isDoor[door.x][door.z] = false;
                  }

                  if (dungeon.map.hasLiquid[door.x + 1][door.z] != dungeon.map.hasLiquid[door.x - 1][door.z]) {
                     dungeon.map.isDoor[door.x][door.z] = false;
                  }

                  if (dungeon.map.hasLiquid[door.x + 1][door.z]) {
                     dungeon.map.hasLiquid[door.x][door.z] = true;
                     if (dungeon.theme.flags.contains(ThemeFlags.SWAMPY)) {
                        dungeon.map.floorY[door.x][door.z] = (byte)(room.floorY - 1);
                     } else {
                        dungeon.map.floorY[door.x][door.z] = (byte)(room.floorY - 2);
                     }
                  }
               } else {
                  if (dungeon.map.isWall[door.x][door.z + 1] || dungeon.map.isWall[door.x][door.z - 1]) {
                     dungeon.map.isDoor[door.x][door.z] = false;
                  }

                  if (dungeon.map.hasLiquid[door.x][door.z + 1] != dungeon.map.hasLiquid[door.x][door.z - 1]) {
                     dungeon.map.isDoor[door.x][door.z] = false;
                  }

                  if (dungeon.map.hasLiquid[door.x][door.z + 1]) {
                     dungeon.map.hasLiquid[door.x][door.z] = true;
                     if (dungeon.theme.flags.contains(ThemeFlags.SWAMPY)) {
                        dungeon.map.floorY[door.x][door.z] = (byte)(room.floorY - 1);
                     } else {
                        dungeon.map.floorY[door.x][door.z] = (byte)(room.floorY - 2);
                     }
                  }
               }
            }
         }
      }
   }

   public static void processDoors1(Dungeon dungeon, Room room) {
      ArrayList<Tile> invalid = new ArrayList<>();

      for (Doorway door : room.doors) {
         boolean valid = validateDoor(dungeon, door);
         if (!valid) {
            invalid.add(door);
            dungeon.map.isDoor[door.x][door.z] = false;
         } else {
            door.prioritize(dungeon, room.id);
            room.addToConnections(door);
         }
      }

      room.doors.removeAll(invalid);
   }

   public static void checkConnectivity(Dungeon dungeon) {
      ArrayList<ArrayList<Room>> sections = new RoomBFS(dungeon).check();

      while (sections.size() > 1) {
         Collections.sort(sections, c);
         new AStar2(dungeon, sections.get(0).get(0), sections.get(1).get(0)).seek();
         sections.get(1).addAll(sections.get(0));
         sections.remove(0);
      }
   }

   public static void processDoors2(Dungeon dungeon, Room room) {
      room.topDoors = makeConnectionList(room, dungeon.random);

      for (Doorway exit : room.topDoors) {
         exit.priority -= 16;
         dungeon.rooms.get(getOtherRoom(exit, room, dungeon)).addToConnections(new Doorway(exit, room.id));
      }
   }

   public static void processDoors3(Dungeon dungeon, Room room) {
      checkConnections(room.topDoors, room, dungeon);
      retestDoors(dungeon, room);
   }

   public static void caveConnector(Dungeon dungeon, Room cave) {
      for (Doorway door : cave.doors) {
         new AStar(cave, dungeon, cave.midpoint, dungeon.rooms.get(door.otherside).midpoint).seek();
      }
   }

   public static class CompareLists implements Comparator<List> {
      public int compare(List o1, List o2) {
         return o1.size() - o2.size();
      }
   }
}
