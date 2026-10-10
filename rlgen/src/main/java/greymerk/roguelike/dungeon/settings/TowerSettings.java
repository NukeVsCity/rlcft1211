package greymerk.roguelike.dungeon.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.theme.ThemeParser;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.theme.builtin.ThemeTower;

public class TowerSettings {
   private final TowerType type;
   private final Theme theme;

   public TowerSettings(TowerType type, Themes theme) {
      this.type = type;
      this.theme = theme.getThemeBase();
   }

   public TowerSettings(JsonElement e) throws Exception {
      JsonObject data = e.getAsJsonObject();
      this.type = data.has("type") ? TowerType.get(data.get("type").getAsString()) : null;

      try {
         this.theme = data.has("theme") ? ThemeParser.parse(data.get("theme").getAsJsonObject()) : null;
      } catch (DungeonSettingParseException var4) {
         throw new DungeonSettingParseException("While parsing tower: " + var4.getMessage());
      }
   }

   public TowerSettings(TowerSettings base, TowerSettings override) {
      if (base == null) {
         this.type = override.type;
         this.theme = override.theme;
      } else if (override == null) {
         this.type = base.type;
         this.theme = base.theme;
      } else {
         this.type = override.type == null ? base.type : override.type;
         this.theme = override.theme == null ? base.theme : override.theme;
      }
   }

   public TowerSettings(TowerSettings toCopy) {
      this.type = toCopy.type;
      this.theme = toCopy.theme;
   }

   public TowerType getType() {
      return this.type == null ? TowerType.ROGUE : this.type;
   }

   public Theme getTheme() {
      return (Theme)(this.theme == null ? new ThemeTower() : this.theme);
   }
}
