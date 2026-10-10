package greymerk.roguelike.dungeon.rooms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.base.RoomType;
import greymerk.roguelike.dungeon.settings.DungeonSettingsParser;
import greymerk.roguelike.dungeon.settings.level.LevelsParser;
import greymerk.roguelike.treasure.loot.ChestType;
import java.util.Optional;

public class RoomSettingParser {
   public static final String TYPE_KEY = "type";
   public static final String ROOM_FREQUENCY = "frequency";
   public static final String COUNT_KEY = "count";
   public static final String WEIGHT_KEY = "weight";
   public static final String SPAWNER_KEY = "spawnerId";
   public static final String CHEST_TYPE_KEY = "chestType";

   public static RoomSetting parse(JsonObject roomSettingJson) {
      return new RoomSetting(
         parseType(roomSettingJson),
         parseSpawnerId(roomSettingJson),
         parseRoomFrequency(roomSettingJson),
         parseWeight(roomSettingJson),
         parseCount(roomSettingJson),
         LevelsParser.parseLevelsOrDefault(roomSettingJson, DungeonSettingsParser.ALL_LEVELS),
         parseChestType(roomSettingJson)
      );
   }

   private static RoomType parseType(JsonObject roomSettingJson) {
      if (!roomSettingJson.has("type")) {
         throw new RuntimeException(String.format("Room setting is missing a value for key '%s' which is required", "type"));
      } else {
         return RoomType.valueOf(roomSettingJson.get("type").getAsString().toUpperCase());
      }
   }

   private static Frequency parseRoomFrequency(JsonObject roomSettingJson) {
      return roomSettingJson.has("frequency") ? Frequency.valueOf(roomSettingJson.get("frequency").getAsString().toUpperCase()) : Frequency.SINGLE;
   }

   private static int parseCount(JsonObject roomSettingJson) {
      return roomSettingJson.has("count") ? roomSettingJson.get("count").getAsInt() : 1;
   }

   private static int parseWeight(JsonObject roomSettingJson) {
      return roomSettingJson.has("weight") ? roomSettingJson.get("weight").getAsInt() : 1;
   }

   private static String parseSpawnerId(JsonObject roomSettingJson) {
      return roomSettingJson.has("spawnerId") ? roomSettingJson.get("spawnerId").getAsString() : null;
   }

   private static Optional<ChestType> parseChestType(JsonObject roomSettingJson) {
      if (!roomSettingJson.has("chestType")) {
         return Optional.empty();
      } else {
         JsonElement treasureTypeValue = roomSettingJson.get("chestType");
         return treasureTypeValue.isJsonNull() ? Optional.empty() : Optional.of(new ChestType(treasureTypeValue.getAsString()));
      }
   }
}
