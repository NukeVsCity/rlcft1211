package greymerk.roguelike.dungeon.base;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class RoomsSetting {
   private List<RoomSetting> singleRoomSettings = new LinkedList<>();
   private WeightedRandomizer<RoomSetting> randomRooms = new WeightedRandomizer<>();

   public RoomsSetting() {
   }

   public RoomsSetting(RoomsSetting toCopy) {
      this.singleRoomSettings = Lists.newLinkedList(toCopy.singleRoomSettings);
      this.randomRooms = new WeightedRandomizer<>(toCopy.randomRooms);
   }

   public RoomsSetting(RoomsSetting parent, RoomsSetting child) {
      child.inherit(parent);
   }

   public static RoomsSetting getRandom(Random random, int numRooms) {
      RoomsSetting rooms = new RoomsSetting();
      IntStream.range(0, numRooms).forEach(i -> rooms.add(RoomType.getRandomIntersection(random).newRandomRoomSetting(1)));
      return rooms;
   }

   public RoomsSetting inherit(RoomsSetting toInherit) {
      RoomsSetting roomsSetting = new RoomsSetting();
      roomsSetting.singleRoomSettings.addAll(this.singleRoomSettings);
      roomsSetting.singleRoomSettings.addAll(toInherit.singleRoomSettings);
      roomsSetting.randomRooms.merge(this.getRandomRooms());
      roomsSetting.randomRooms.merge(toInherit.getRandomRooms());
      return roomsSetting;
   }

   public void add(RoomSetting roomSetting) {
      if (roomSetting.isSingle()) {
         this.addSingleRoom(roomSetting);
      }

      if (roomSetting.isRandom()) {
         this.addRandomRoom(roomSetting);
      }
   }

   private void addSingleRoom(RoomSetting roomSetting) {
      IntStream.range(0, roomSetting.getCount()).mapToObj(operand -> roomSetting).forEach(this.singleRoomSettings::add);
   }

   private void addRandomRoom(RoomSetting roomSetting) {
      this.randomRooms.add(new WeightedChoice<>(roomSetting, roomSetting.getWeight()));
   }

   @Override
   public String toString() {
      return "RoomsSetting(singleRoomSettings=" + this.getSingleRoomSettings() + ", randomRooms=" + this.getRandomRooms() + ")";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RoomsSetting)) {
         return false;
      } else {
         RoomsSetting other = (RoomsSetting)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$singleRoomSettings = this.getSingleRoomSettings();
            Object other$singleRoomSettings = other.getSingleRoomSettings();
            if (this$singleRoomSettings == null ? other$singleRoomSettings == null : this$singleRoomSettings.equals(other$singleRoomSettings)) {
               Object this$randomRooms = this.getRandomRooms();
               Object other$randomRooms = other.getRandomRooms();
               return this$randomRooms == null ? other$randomRooms == null : this$randomRooms.equals(other$randomRooms);
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof RoomsSetting;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $singleRoomSettings = this.getSingleRoomSettings();
      result = result * 59 + ($singleRoomSettings == null ? 43 : $singleRoomSettings.hashCode());
      Object $randomRooms = this.getRandomRooms();
      return result * 59 + ($randomRooms == null ? 43 : $randomRooms.hashCode());
   }

   public List<RoomSetting> getSingleRoomSettings() {
      return this.singleRoomSettings;
   }

   public WeightedRandomizer<RoomSetting> getRandomRooms() {
      return this.randomRooms;
   }
}
