package greymerk.roguelike.dungeon.settings;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.segment.Segment;
import greymerk.roguelike.dungeon.segment.SegmentGenerator;
import greymerk.roguelike.dungeon.settings.level.LevelsParser;
import java.util.Map;

class SegmentsParser {
   public static Map<Integer, SegmentGenerator> parseSegments(JsonElement segmentsElement) {
      if (!segmentsElement.isJsonArray()) {
         throw new DungeonSettingParseException("Expected field \"segments\" to contain a list of segments, but instead it was: " + segmentsElement.toString());
      } else {
         JsonArray segmentsArray = segmentsElement.getAsJsonArray();
         Map<Integer, SegmentGenerator> segmentsByLevel = Maps.newHashMap();

         for (JsonElement segmentElement : segmentsArray) {
            if (!segmentElement.isJsonObject()) {
               throw new DungeonSettingParseException("Expected list \"segments\" to contain segments, but instead it was: " + segmentElement.toString());
            }

            JsonObject segmentObject = segmentElement.getAsJsonObject();

            for (Integer level : LevelsParser.parseLevelsOrDefault(segmentObject, DungeonSettingsParser.ALL_LEVELS)) {
               SegmentGenerator segmentGenerator = getSegmentGeneratorEnsuringly(level, segmentsByLevel);
               Segment segment = parseSegment(segmentObject);
               int weight = parseWeight(segmentObject);
               segmentGenerator.add(segment, weight);
            }
         }

         return segmentsByLevel;
      }
   }

   public static SegmentGenerator getSegmentGeneratorEnsuringly(Integer level, Map<Integer, SegmentGenerator> segmentsByLevel) {
      if (!segmentsByLevel.containsKey(level)) {
         segmentsByLevel.put(level, new SegmentGenerator());
      }

      return segmentsByLevel.get(level);
   }

   public static boolean parseArch(JsonObject segmentObject, SegmentGenerator segmentGenerator) {
      if (segmentObject.has("arch")) {
         boolean a = segmentObject.get("arch").getAsBoolean();
         return true;
      } else {
         return false;
      }
   }

   public static Segment parseSegment(JsonObject segmentObject) {
      return Segment.valueOf(segmentObject.get("type").getAsString());
   }

   public static int parseWeight(JsonObject segmentObject) {
      return segmentObject.has("weight") ? segmentObject.get("weight").getAsInt() : 1;
   }
}
