package greymerk.roguelike.dungeon.settings.builtin.dungeon;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import greymerk.roguelike.dungeon.settings.TowerSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinBaseSettings;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Themes;
import java.util.stream.IntStream;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class BuiltinMesaDungeonSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "mesa");

   public BuiltinMesaDungeonSettings() {
      super(ID);
      this.setExclusive(true);
      this.getInherit().add(BuiltinBaseSettings.ID);
      this.getCriteria().setBiomeTypes(Lists.newArrayList(new Type[]{Type.MESA}));
      this.setTowerSettings(new TowerSettings(TowerType.ETHO, Themes.ETHOTOWER));
      Themes[] themes = new Themes[]{Themes.ETHOTOWER, Themes.ETHOTOWER, Themes.CRYPT, Themes.CRYPT, Themes.NETHER};
      IntStream.range(0, 5).forEach(level -> this.getLevelSettings(level).setTheme(themes[level].getThemeBase()));
   }
}
