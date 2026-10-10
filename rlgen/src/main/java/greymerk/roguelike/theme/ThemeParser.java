package greymerk.roguelike.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ThemeParser {
   public static final String THEME_BASE_KEY = "base";
   public static final String PRIMARY_KEY = "primary";
   public static final String SECONDARY_KEY = "secondary";

   public static Theme parse(JsonObject json) throws DungeonSettingParseException {
      Theme theme = parseThemeBase(json);
      BlockSet primaryBlockSet = Optional.ofNullable(parsePrimaryBlockSet(json, theme)).orElse(Optional.ofNullable(theme).map(Theme::getPrimary).orElse(null));
      BlockSet secondaryBlockSet = Optional.ofNullable(parseSecondaryBlockSet(json, theme))
         .orElse(Optional.ofNullable(theme).map(Theme::getSecondary).orElse(null));
      return new Theme(primaryBlockSet, secondaryBlockSet);
   }

   private static Theme parseThemeBase(JsonObject json) {
      if (!json.has("base")) {
         return Themes.OAK.getThemeBase();
      } else {
         JsonElement baseElement = json.get("base");
         if (baseElement.isJsonNull()) {
            return Themes.OAK.getThemeBase();
         } else {
            String baseString = baseElement.getAsString();
            return baseString.isEmpty() ? Themes.OAK.getThemeBase() : get(baseString).getThemeBase();
         }
      }
   }

   private static BlockSet parsePrimaryBlockSet(JsonObject json, Theme base) throws DungeonSettingParseException {
      return parseBlockSet(json, base, "primary", Theme::getPrimary);
   }

   private static BlockSet parseSecondaryBlockSet(JsonObject json, Theme base) throws DungeonSettingParseException {
      return parseBlockSet(json, base, "secondary", Theme::getSecondary);
   }

   private static BlockSet parseBlockSet(JsonObject json, Theme baseTheme, String key, Function<Theme, BlockSet> getBlockSetFunction) throws DungeonSettingParseException {
      if (!json.has(key)) {
         return null;
      } else {
         JsonObject data = json.get(key).getAsJsonObject();
         BlockSet baseBlockSet = Optional.ofNullable(baseTheme).map(getBlockSetFunction).orElse(getEmptyBlockSet());
         return BlockSetParser.parseBlockSet(data, baseBlockSet);
      }
   }

   private static BlockSet getEmptyBlockSet() {
      return new BlockSet(null, null, null, null, null, null, null);
   }

   public static Themes get(String name) throws DungeonSettingParseException {
      if (!contains(name.toUpperCase())) {
         throw new DungeonSettingParseException("No such theme: " + name);
      } else {
         return Themes.valueOf(name.toUpperCase());
      }
   }

   public static boolean contains(String name) {
      return Arrays.stream(Themes.values()).anyMatch(value -> value.toString().equals(name));
   }
}
