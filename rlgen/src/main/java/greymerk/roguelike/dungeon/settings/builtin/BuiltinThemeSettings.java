package greymerk.roguelike.dungeon.settings.builtin;

import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import greymerk.roguelike.dungeon.settings.TowerSettings;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.theme.Themes;

public class BuiltinThemeSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "theme");
   private static final Themes[] themes = new Themes[]{Themes.OAK, Themes.SPRUCE, Themes.CRYPT, Themes.MOSSY};
   private static final Themes[] level4Themes = new Themes[]{Themes.CAVE, Themes.HELL, Themes.ICE, Themes.NETHER, Themes.NETHER_FORTRESS};

   public BuiltinThemeSettings() {
      super(ID);
      this.setTowerSettings(new TowerSettings(TowerType.ROGUE, Themes.TOWER));

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = new LevelSettings(level);
         Theme theme = level == 4 ? this.randomTheme().getThemeBase() : themes[level].getThemeBase();
         levelSettings.setTheme(theme);
         this.getLevelSettings().put(level, levelSettings);
      }
   }

   private Themes randomTheme() {
      int choice = (int)(greymerk.roguelike.util.RldRandom.random() * (double)level4Themes.length);
      return level4Themes[choice];
   }
}
