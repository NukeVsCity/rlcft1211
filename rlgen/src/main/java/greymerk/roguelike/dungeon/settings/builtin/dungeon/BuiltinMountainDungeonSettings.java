package greymerk.roguelike.dungeon.settings.builtin.dungeon;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.base.RoomType;
import greymerk.roguelike.dungeon.base.RoomsSetting;
import greymerk.roguelike.dungeon.base.SecretsSetting;
import greymerk.roguelike.dungeon.segment.Segment;
import greymerk.roguelike.dungeon.segment.SegmentGenerator;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import greymerk.roguelike.dungeon.settings.TowerSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinBaseSettings;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.worldgen.filter.Filter;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class BuiltinMountainDungeonSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "mountain");

   public BuiltinMountainDungeonSettings() {
      super(ID);
      this.setExclusive(true);
      this.getInherit().add(BuiltinBaseSettings.ID);
      this.getCriteria().setBiomeTypes(Lists.newArrayList(new Type[]{Type.MOUNTAIN}));
      this.setTowerSettings(new TowerSettings(TowerType.ENIKO, Themes.OAK));
      Themes[] themes = new Themes[]{Themes.ENIKO, Themes.ENIKO2, Themes.SEWER, Themes.MOSSY, Themes.NETHER};

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = this.getLevelSettings(level);
         levelSettings.setTheme(themes[level].getThemeBase());
         if (level == 0) {
            levelSettings.setScatter(16);
            levelSettings.setRange(60);
            levelSettings.setNumRooms(10);
            RoomsSetting factory = new RoomsSetting();
            factory.add(RoomType.LIBRARY.newSingleRoomSetting());
            factory.add(RoomType.FIRE.newSingleRoomSetting());
            RoomType.ENIKO.newRandomRoomSetting(10);
            RoomType.CORNER.newRandomRoomSetting(3);
            levelSettings.setRooms(factory);
            SecretsSetting secrets = new SecretsSetting();
            secrets.add(RoomType.BEDROOM.newSingleRoomSetting());
            secrets.add(RoomType.BEDROOM.newSingleRoomSetting());
            secrets.add(RoomType.SMITH.newSingleRoomSetting());
            levelSettings.setSecrets(secrets);
            SegmentGenerator segments = new SegmentGenerator(Segment.ARCH);
            segments.add(Segment.DOOR, 7);
            segments.add(Segment.ANKH, 2);
            segments.add(Segment.PLANT, 3);
            segments.add(Segment.LAMP, 1);
            segments.add(Segment.FLOWERS, 1);
            levelSettings.setSegments(segments);
         }

         if (level == 1) {
            levelSettings.setScatter(16);
            levelSettings.setRange(80);
            levelSettings.setNumRooms(20);
            RoomsSetting factory = new RoomsSetting();
            factory.add(RoomType.FIRE.newSingleRoomSetting());
            factory.add(RoomType.MESS.newSingleRoomSetting());
            factory.add(RoomType.LIBRARY.newSingleRoomSetting());
            factory.add(RoomType.LAB.newSingleRoomSetting());
            factory.add(RoomType.ENIKO.newRandomRoomSetting(10));
            factory.add(RoomType.CORNER.newRandomRoomSetting(3));
            levelSettings.setRooms(factory);
            SecretsSetting secrets = new SecretsSetting();
            secrets.add(RoomType.ENCHANT.newSingleRoomSetting());
            levelSettings.setSecrets(secrets);
         }

         if (level == 2) {
            levelSettings.setLevel(4);
            SegmentGenerator segments = new SegmentGenerator(Segment.SEWERARCH);
            segments.add(Segment.SEWER, 7);
            segments.add(Segment.SEWERDRAIN, 4);
            segments.add(Segment.SEWERDOOR, 2);
            levelSettings.setSegments(segments);
            RoomsSetting factory = new RoomsSetting();
            factory.add(RoomType.BRICK.newRandomRoomSetting(4));
            factory.add(RoomType.SLIME.newRandomRoomSetting(7));
            factory.add(RoomType.CORNER.newRandomRoomSetting(3));
            factory.add(RoomType.SPIDER.newRandomRoomSetting(2));
            factory.add(RoomType.PIT.newRandomRoomSetting(2));
            factory.add(RoomType.PRISON.newRandomRoomSetting(3));
            levelSettings.setRooms(factory);
         }
      }

      this.getLevelSettings().get(3).addFilter(Filter.VINE);
   }
}
