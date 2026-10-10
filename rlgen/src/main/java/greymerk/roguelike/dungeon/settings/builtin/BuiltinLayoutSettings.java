package greymerk.roguelike.dungeon.settings.builtin;

import greymerk.roguelike.dungeon.layout.LayoutGenerator;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;

public class BuiltinLayoutSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "layout");

   public BuiltinLayoutSettings() {
      super(ID);
      int[] numRooms = new int[]{10, 15, 15, 20, 15};
      int[] scatter = new int[]{15, 15, 17, 12, 15};
      int[] range = new int[]{50, 50, 80, 70, 50};
      LayoutGenerator.Type[] generator = new LayoutGenerator.Type[]{
         LayoutGenerator.Type.CLASSIC, LayoutGenerator.Type.CLASSIC, LayoutGenerator.Type.MST, LayoutGenerator.Type.CLASSIC, LayoutGenerator.Type.CLASSIC
      };

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = new LevelSettings(level);
         levelSettings.setNumRooms(numRooms[level]);
         levelSettings.setRange(range[level]);
         levelSettings.setScatter(scatter[level]);
         levelSettings.setGenerator(generator[level]);
         this.getLevelSettings().put(level, levelSettings);
      }
   }
}
