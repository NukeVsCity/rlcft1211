package jaredbgreat.dldungeons.rooms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class RoomList extends ArrayList<Room> {
   private int numRooms;
   private int index;
   private Room[] rooms;

   public RoomList(int slots) {
      super(slots);
      this.add(0, Room.roomNull);
      this.numRooms = 0;
   }

   public int realSize() {
      return this.numRooms;
   }

   public boolean isReallyEmpty() {
      return this.numRooms == 0;
   }

   @Override
   public ListIterator listIterator() {
      return super.listIterator(1);
   }

   @Override
   public Iterator iterator() {
      return super.listIterator(1);
   }

   public boolean add(Room room) {
      boolean out = super.add(room);
      if (out) {
         this.numRooms++;
      }

      return out;
   }

   public Room remove(int index) {
      System.err.println("[DLDUNGEONS] WARNING! Trying to remove room from list (Rooms cannot be removed)!");
      return null;
   }
}
