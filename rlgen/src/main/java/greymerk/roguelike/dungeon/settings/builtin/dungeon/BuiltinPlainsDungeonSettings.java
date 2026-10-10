package greymerk.roguelike.dungeon.settings.builtin.dungeon;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.base.RoomType;
import greymerk.roguelike.dungeon.base.RoomsSetting;
import greymerk.roguelike.dungeon.base.SecretsSetting;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import greymerk.roguelike.dungeon.settings.TowerSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinBaseSettings;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.worldgen.filter.Filter;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class BuiltinPlainsDungeonSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "grassland");

   public BuiltinPlainsDungeonSettings() {
      super(ID);
      this.setExclusive(true);
      this.getInherit().add(BuiltinBaseSettings.ID);
      this.getCriteria().setBiomeTypes(Lists.newArrayList(new Type[]{Type.PLAINS}));
      this.setTowerSettings(new TowerSettings(TowerType.HOUSE, Themes.HOUSE));

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = this.getLevelSettings(level);
         SecretsSetting secrets = new SecretsSetting();
         RoomsSetting rooms = new RoomsSetting();
         switch (level) {
            case 0:
               secrets.add(RoomType.BEDROOM.newSingleRoomSetting());
               secrets.add(RoomType.SMITH.newSingleRoomSetting());
               secrets.add(RoomType.FIREWORK.newSingleRoomSetting());
               levelSettings.setSecrets(secrets);
               break;
            case 1:
               secrets.add(RoomType.BTEAM.newSingleRoomSetting());
               rooms.add(RoomType.MUSIC.newSingleRoomSetting());
               rooms.add(RoomType.PIT.newSingleRoomSetting());
               rooms.add(RoomType.MESS.newSingleRoomSetting());
               rooms.add(RoomType.LAB.newSingleRoomSetting());
               rooms.add(RoomType.CORNER.newRandomRoomSetting(10));
               rooms.add(RoomType.BRICK.newRandomRoomSetting(3));
               levelSettings.setSecrets(secrets);
               levelSettings.setRooms(rooms);
            case 2:
            case 3:
            case 4:
         }

         this.getLevelSettings().put(level, levelSettings);
      }

      this.getLevelSettings().get(3).addFilter(Filter.VINE);
   }
}
