package jaredbgreat.dldungeons.planner.astar;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class RoomBFS {
   private ArrayDeque<Room> roomQueue = new ArrayDeque<>();
   private Dungeon dungeon;
   private ArrayList<Room> nodes;
   private boolean[] checked;

   public RoomBFS(Dungeon dungeon) {
      this.dungeon = dungeon;
      this.nodes = new ArrayList<>(dungeon.numNodes);
      this.checked = new boolean[dungeon.roomCount + 1];

      for (int i = dungeon.numNodes + 1; i > 0; i--) {
         Room room = dungeon.rooms.get(i);
         if (room.isNode) {
            this.nodes.add(room);
         }
      }
   }

   public ArrayList<ArrayList<Room>> check() {
      ArrayList<ArrayList<Room>> sections = new ArrayList<>();

      while (!this.nodes.isEmpty()) {
         sections.add(this.search(this.nodes.get(0)));
      }

      return sections;
   }

   public ArrayList<Room> search(Room room) {
      this.roomQueue.add(room);
      ArrayList<Room> found = new ArrayList<>();

      while (!this.nodes.isEmpty() && !this.roomQueue.isEmpty()) {
         Room next = this.roomQueue.poll();
         this.checked[next.id] = true;
         if (next.isNode) {
            this.nodes.remove(next);
            found.add(next);
         }

         for (DoorQueue exit : next.connections) {
            if (!this.checked[exit.peek().otherside]) {
               this.roomQueue.add(this.dungeon.rooms.get(exit.peek().otherside));
            }
         }
      }

      return found;
   }
}
