package greymerk.roguelike.dungeon.settings;

import com.github.fnar.minecraft.block.spawner.Spawner;
import com.github.fnar.minecraft.block.spawner.SpawnerSettings;
import com.github.fnar.roguelike.settings.RequiredModMissingException;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.dungeon.base.RoomsSetting;
import greymerk.roguelike.dungeon.base.SecretsSetting;
import greymerk.roguelike.dungeon.layout.LayoutGenerator;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.rooms.RoomSettingParser;
import greymerk.roguelike.dungeon.settings.level.LevelsParser;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.theme.ThemeParser;
import greymerk.roguelike.treasure.loot.LootRulesParser;
import greymerk.roguelike.treasure.loot.LootTableRule;
import greymerk.roguelike.worldgen.filter.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DungeonSettingsParser {
   private static final java.util.Set<String> RLCRAFT_MODS = java.util.Set.of("minecraft", "roguelike", "iceandfire", "locks", "mujmajnkraftsbettersurvival",
      "notreepunching", "potioncore", "qualitytools", "simpledifficulty", "spartanweaponry", "trumpetskeleton", "wolfarmor", "waystones", "charm", "quark",
      "lycanitesmobs", "defiledlands", "traverse", "dynamictrees", "baubles", "bountifulbaubles", "scalinghealth", "firstaid", "reccomplex");
   public static final List<Integer> ALL_LEVELS = Collections.unmodifiableList(Lists.newArrayList(new Integer[]{0, 1, 2, 3, 4}));
   private static final String REQUIRES_KEY = "requires";

   public static Optional<DungeonSettings> parseJson(String content) throws Exception {
      try {
         JsonParser jsonParser = new JsonParser();

         JsonObject parse;
         try {
            parse = jsonParser.parse(content).getAsJsonObject();
         } catch (JsonSyntaxException var4) {
            throw new Exception(var4.getCause());
         }

         return parseDungeonSettings(parse);
      } catch (RequiredModMissingException | DungeonSettingParseException var5) {
         throw var5;
      } catch (Exception var6) {
         throw new Exception("An unknown error occurred while parsing json: " + var6.getClass().toString() + " " + var6.getMessage());
      }
   }

   public static Optional<DungeonSettings> parseDungeonSettings(JsonObject root) throws Exception {
      boolean hasRequiredMods = parseRequires(root);
      if (!hasRequiredMods) {
         return Optional.empty();
      } else {
         DungeonSettings dungeonSettings = new DungeonSettings();

         for (int level = 0; level < 5; level++) {
            LevelSettings setting = new LevelSettings(level);
            dungeonSettings.getLevelSettings().put(level, setting);
         }

         parseId(root, dungeonSettings);
         parseExclusive(root, dungeonSettings);
         parseCriteria(root, dungeonSettings);
         parseTower(root, dungeonSettings);
         parseLootRules(root, dungeonSettings);
         parseOverrides(root, dungeonSettings);
         parseInherit(root, dungeonSettings);
         parseLootTables(root, dungeonSettings);
         parseRoomCount(root, dungeonSettings);
         parseScatter(root, dungeonSettings);
         parseLayouts(root, dungeonSettings);
         parseSpawners(root, dungeonSettings);
         parseRooms(root, dungeonSettings);
         parseThemes(root, dungeonSettings);
         parseSegments(root, dungeonSettings);
         parseFilters(root, dungeonSettings);
         return Optional.of(dungeonSettings);
      }
   }

   private static boolean parseRequires(JsonObject root) throws Exception {
      if (!root.has("requires")) {
         return true;
      } else {
         JsonElement requiresElement = root.get("requires");
         if (requiresElement.isJsonObject()) {
            throw new DungeonSettingParseException("Expected field 'requires' to be list of modid's but instead found a single object.");
         } else if (!requiresElement.isJsonArray()) {
            throw new DungeonSettingParseException("Expected field 'requires' to be list of modid's but it wasn't.");
         } else {
            for (JsonElement requiredModElement : requiresElement.getAsJsonArray()) {
               String requiredModName = requiredModElement.getAsString();
               if (!RLCRAFT_MODS.contains(requiredModName)) { // rlgen: RLCraft 2.9.3 had these; their items, mobs and blocks are skipped when missing
                  if (RogueConfig.BREAK_IF_REQUIRED_MOD_IS_MISSING.getBoolean()) {
                     throw new RequiredModMissingException(requiredModName);
                  }

                  return false;
               }
            }

            return true;
         }
      }
   }

   private static void parseId(JsonObject root, DungeonSettings dungeonSettings) throws Exception {
      if (!root.has("name")) {
         throw new Exception("Setting missing name");
      } else {
         if (root.has("namespace")) {
            String name = root.get("name").getAsString();
            String namespace = root.get("namespace").getAsString();
            dungeonSettings.setId(new SettingIdentifier(namespace, name));
         } else {
            dungeonSettings.setId(new SettingIdentifier(root.get("name").getAsString()));
         }
      }
   }

   private static void parseExclusive(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("exclusive")) {
         dungeonSettings.setExclusive(root.get("exclusive").getAsBoolean());
      }
   }

   private static void parseCriteria(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("criteria")) {
         dungeonSettings.setSpawnCriteria(new SpawnCriteria(root.get("criteria").getAsJsonObject()));
      }
   }

   private static void parseTower(JsonObject root, DungeonSettings dungeonSettings) throws Exception {
      if (root.has("tower")) {
         dungeonSettings.setTowerSettings(new TowerSettings(root.get("tower")));
      }
   }

   private static void parseLootRules(JsonObject root, DungeonSettings dungeonSettings) throws Exception {
      if (root.has("loot_rules")) {
         throw new DungeonSettingParseException("Please update field \"loot_rules\" to \"lootRules\" to conform to JSON standards.");
      } else {
         if (root.has("lootRules")) {
            dungeonSettings.getLootRules().addAll(new LootRulesParser().parseLootRules(root.get("lootRules")));
         }
      }
   }

   private static void parseOverrides(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("overrides")) {
         for (JsonElement jsonElement : root.get("overrides").getAsJsonArray()) {
            if (!jsonElement.isJsonNull()) {
               dungeonSettings.getOverrides().add(SettingsType.valueOf(jsonElement.getAsString()));
            }
         }
      }
   }

   private static void parseInherit(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("inherit")) {
         for (JsonElement jsonElement : root.get("inherit").getAsJsonArray()) {
            if (!jsonElement.isJsonNull()) {
               dungeonSettings.getInherit().add(new SettingIdentifier(jsonElement.getAsString()));
            }
         }
      }
   }

   private static void parseLootTables(JsonObject root, DungeonSettings dungeonSettings) throws Exception {
      if (root.has("loot_tables")) {
         throw new DungeonSettingParseException("Please update field \"loot_tables\" to \"lootTables\" to conform to JSON standards.");
      } else {
         if (root.has("lootTables")) {
            for (JsonElement jsonElement : root.get("lootTables").getAsJsonArray()) {
               if (!jsonElement.isJsonNull()) {
                  dungeonSettings.getLootTables().add(new LootTableRule(jsonElement.getAsJsonObject()));
               }
            }
         }
      }
   }

   private static void parseRoomCount(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("num_rooms")) {
         throw new DungeonSettingParseException("Please update field \"num_rooms\" to \"numRooms\" to conform to JSON standards.");
      } else {
         if (root.has("numRooms")) {
            JsonArray arr = root.get("numRooms").getAsJsonArray();

            for (int i = 0; i < arr.size(); i++) {
               JsonElement jsonElement = arr.get(i);
               if (!jsonElement.isJsonNull()) {
                  LevelSettings setting = dungeonSettings.getLevelSettings().get(i);
                  setting.setNumRooms(jsonElement.getAsInt());
               }
            }
         }
      }
   }

   private static void parseScatter(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("scatter")) {
         JsonArray arr = root.get("scatter").getAsJsonArray();

         for (int i = 0; i < arr.size(); i++) {
            JsonElement jsonElement = arr.get(i);
            if (!jsonElement.isJsonNull()) {
               LevelSettings setting = dungeonSettings.getLevelSettings().get(i);
               setting.setScatter(jsonElement.getAsInt());
            }
         }
      }
   }

   private static void parseLayouts(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("layouts")) {
         for (JsonElement jsonElement : root.get("layouts").getAsJsonArray()) {
            if (!jsonElement.isJsonNull()) {
               JsonObject layout = jsonElement.getAsJsonObject();
               if (layout.has("level")) {
                  for (Integer level : LevelsParser.parseLevelsOrDefault(layout, ALL_LEVELS)) {
                     if (dungeonSettings.getLevelSettings().containsKey(level)) {
                        LevelSettings setting = dungeonSettings.getLevelSettings().get(level);
                        setting.setGenerator(LayoutGenerator.Type.valueOf(layout.get("type").getAsString().toUpperCase()));
                     }
                  }
               }
            }
         }
      }
   }

   private static void parseRooms(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("rooms")) {
         JsonArray roomArray = root.get("rooms").getAsJsonArray();

         for (int floorLevel : dungeonSettings.getLevelSettings().keySet()) {
            RoomsSetting roomsSetting = new RoomsSetting();
            SecretsSetting secretsSetting = new SecretsSetting();

            for (JsonElement roomSettingElement : roomArray) {
               if (!roomSettingElement.isJsonNull()) {
                  JsonObject roomSettingJson = roomSettingElement.getAsJsonObject();
                  RoomSetting roomSetting = RoomSettingParser.parse(roomSettingJson);
                  if (roomSetting.isOnFloorLevel(floorLevel)) {
                     if (roomSetting.isSecret()) {
                        secretsSetting.add(roomSetting);
                     } else {
                        roomsSetting.add(roomSetting);
                     }
                  }
               }
            }

            LevelSettings level = dungeonSettings.getLevelSettings().get(floorLevel);
            level.setRooms(roomsSetting);
            level.setSecrets(secretsSetting);
         }
      }
   }

   private static void parseThemes(JsonObject rootJsonObject, DungeonSettings dungeonSettings) throws DungeonSettingParseException {
      if (rootJsonObject.has("themes")) {
         JsonElement themesJsonElement = rootJsonObject.get("themes");
         if (themesJsonElement.isJsonObject()) {
            throw new DungeonSettingParseException("Expected field 'themes' to be list of themes but instead found a single object.");
         } else if (!themesJsonElement.isJsonArray()) {
            throw new DungeonSettingParseException("Expected field 'themes' to be list of themes but it wasn't.");
         } else {
            for (JsonElement themeJsonElement : themesJsonElement.getAsJsonArray()) {
               if (!themeJsonElement.isJsonNull()) {
                  JsonObject themeJsonObject = themeJsonElement.getAsJsonObject();
                  List<Integer> levels = LevelsParser.parseLevelsOrDefault(themeJsonObject, ALL_LEVELS);
                  if (levels != null) {
                     for (int level : levels) {
                        if (dungeonSettings.getLevelSettings().containsKey(level)) {
                           LevelSettings settings = dungeonSettings.getLevelSettings().get(level);
                           Theme theme = ThemeParser.parse(themeJsonObject);
                           settings.setTheme(theme);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void parseSegments(JsonObject dungeonSettingsJson, DungeonSettings dungeonSettings) {
      if (dungeonSettingsJson.has("segments")) {
         JsonElement segmentsElement = dungeonSettingsJson.get("segments");
         SegmentsParser.parseSegments(segmentsElement).forEach((level, segments) -> dungeonSettings.getLevelSettings(level).getSegments().add(segments));
      }
   }

   private static void parseSpawners(JsonObject root, DungeonSettings dungeonSettings) throws Exception {
      if (root.has("spawners")) {
         for (JsonElement spawnerJsonElement : root.get("spawners").getAsJsonArray()) {
            JsonObject spawnerJson = spawnerJsonElement.getAsJsonObject();
            List<Integer> levels = LevelsParser.parseLevelsOrDefault(spawnerJson, ALL_LEVELS);
            Spawner spawner = SpawnerSettings.parseSpawnPotentials(spawnerJson);
            int weight = SpawnerSettings.parseWeight(spawnerJson);
            levels.stream()
               .mapToInt(level -> level)
               .filter(level -> dungeonSettings.getLevelSettings().containsKey(level))
               .forEach(level -> dungeonSettings.getLevelSettings().get(level).getSpawnerSettings().add(spawner, weight));
         }
      }
   }

   private static void parseFilters(JsonObject root, DungeonSettings dungeonSettings) {
      if (root.has("filters")) {
         for (JsonElement filterElement : root.get("filters").getAsJsonArray()) {
            JsonObject filterObject = filterElement.getAsJsonObject();

            for (int level : LevelsParser.parseLevelsOrDefault(filterObject, ALL_LEVELS)) {
               if (dungeonSettings.getLevelSettings().containsKey(level)) {
                  String name = filterObject.get("name").getAsString();
                  Filter type = Filter.valueOf(name.toUpperCase());
                  dungeonSettings.getLevelSettings().get(level).addFilter(type);
               }
            }
         }
      }
   }

   private static List<RoomSetting> parseRoomSettings(JsonArray roomArray) {
      List<RoomSetting> roomSettings = Lists.newArrayList();

      for (JsonElement roomSettingElement : roomArray) {
         if (!roomSettingElement.isJsonNull()) {
            JsonObject roomSettingJson = roomSettingElement.getAsJsonObject();
            roomSettings.add(RoomSettingParser.parse(roomSettingJson));
         }
      }

      return roomSettings;
   }
}
