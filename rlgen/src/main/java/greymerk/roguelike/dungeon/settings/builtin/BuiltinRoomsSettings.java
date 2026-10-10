package greymerk.roguelike.dungeon.settings.builtin;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.base.RoomType;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import java.util.List;

public class BuiltinRoomsSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "rooms");

   public BuiltinRoomsSettings() {
      super(ID);
      this.addRoomsToLevel(0, this.getLevel0Rooms());
      this.addRoomsToLevel(1, this.getLevel1Rooms());
      this.addRoomsToLevel(2, this.getLevel2Rooms());
      this.addRoomsToLevel(3, this.getLevel3Rooms());
      this.addRoomsToLevel(4, this.getLevel4Rooms());
   }

   private void addRoomsToLevel(int level, List<RoomSetting> rooms) {
      rooms.forEach(this.getLevelSettings().get(level).getRooms()::add);
   }

   private List<RoomSetting> getLevel0Rooms() {
      return Lists.newArrayList(
         new RoomSetting[]{
            RoomType.CAKE.newSingleRoomSetting(),
            RoomType.FIRE.newSingleRoomSetting(),
            RoomType.REWARD.newSingleRoomSetting(),
            RoomType.BRICK.newRandomRoomSetting(2),
            RoomType.CORNER.newRandomRoomSetting(2)
         }
      );
   }

   private List<RoomSetting> getLevel1Rooms() {
      return Lists.newArrayList(
         new RoomSetting[]{
            RoomType.PIT.newSingleRoomSetting(),
            RoomType.REWARD.newSingleRoomSetting(),
            RoomType.BRICK.newRandomRoomSetting(3),
            RoomType.CORNER.newRandomRoomSetting(10),
            RoomType.PLATFORMS_2.newRandomRoomSetting(1),
            RoomType.SMALL_LIQUID_PIT.newRandomRoomSetting(1)
         }
      );
   }

   private List<RoomSetting> getLevel2Rooms() {
      return Lists.newArrayList(
         new RoomSetting[]{
            RoomType.OSSUARY.newSingleRoomSetting(),
            RoomType.CRYPT.newSingleRoomSetting(),
            RoomType.CREEPER.newSingleRoomSetting(),
            RoomType.FIRE.newSingleRoomSetting(),
            RoomType.SPIDER.newSingleRoomSetting(),
            RoomType.PRISON.newSingleRoomSetting(),
            RoomType.REWARD.newSingleRoomSetting(),
            RoomType.CRYPT.newRandomRoomSetting(5),
            RoomType.CORNER.newRandomRoomSetting(5),
            RoomType.BRICK.newRandomRoomSetting(3)
         }
      );
   }

   private List<RoomSetting> getLevel3Rooms() {
      return Lists.newArrayList(
         new RoomSetting[]{
            RoomType.OSSUARY.newSingleRoomSetting(),
            RoomType.CRYPT.newSingleRoomSetting(),
            RoomType.REWARD.newSingleRoomSetting(),
            RoomType.SLIME.newRandomRoomSetting(5),
            RoomType.PRISON.newRandomRoomSetting(3),
            RoomType.PLATFORMS.newRandomRoomSetting(2),
            RoomType.CREEPER.newRandomRoomSetting(1),
            RoomType.FOUNTAIN.newRandomRoomSetting(1),
            RoomType.SPIDER.newRandomRoomSetting(1),
            RoomType.PIT.newRandomRoomSetting(1),
            RoomType.ENDER.newRandomRoomSetting(1)
         }
      );
   }

   private List<RoomSetting> getLevel4Rooms() {
      return Lists.newArrayList(
         new RoomSetting[]{
            RoomType.REWARD.newSingleRoomSetting(),
            RoomType.OBSIDIAN.newSingleRoomSetting(),
            RoomType.NETHERFORT.newSingleRoomSetting(),
            RoomType.NETHER_PORTAL.newSingleRoomSetting(),
            RoomType.BLAZE.newRandomRoomSetting(1),
            RoomType.BRICK.newRandomRoomSetting(1),
            RoomType.CRYPT.newRandomRoomSetting(1),
            RoomType.CORNER.newRandomRoomSetting(1),
            RoomType.PLATFORMS.newRandomRoomSetting(1),
            RoomType.PLATFORMS_2.newRandomRoomSetting(1),
            RoomType.NETHER.newRandomRoomSetting(1),
            RoomType.PRISON.newRandomRoomSetting(1),
            RoomType.OBSIDIAN.newRandomRoomSetting(1),
            RoomType.SLIME.newRandomRoomSetting(1)
         }
      );
   }
}
