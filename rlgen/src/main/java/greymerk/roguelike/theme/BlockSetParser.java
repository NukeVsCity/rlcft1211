package greymerk.roguelike.theme;

import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.redstone.DoorBlock;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockProvider;
import java.util.Optional;

class BlockSetParser {
   public static BlockSet parseBlockSet(JsonObject json, BlockSet baseBlockSet) throws DungeonSettingParseException {
      return new BlockSet(
         parseFloor(json).orElse(baseBlockSet.getFloor()),
         parseWalls(json).orElse(baseBlockSet.getWall()),
         parseStair(json).orElse(baseBlockSet.getStair()),
         parsePillar(json).orElse(baseBlockSet.getPillar()),
         parseDoor(json).orElse(baseBlockSet.getDoor()),
         parseLightBlock(json).orElse(baseBlockSet.getLightBlock()),
         parseLiquid(json).orElse(baseBlockSet.getLiquid())
      );
   }

   private static Optional<BlockBrush> parseFloor(JsonObject json) throws DungeonSettingParseException {
      return json.has("floor") ? Optional.ofNullable(BlockProvider.create(json.get("floor").getAsJsonObject())) : Optional.empty();
   }

   private static Optional<BlockBrush> parseWalls(JsonObject json) throws DungeonSettingParseException {
      return json.has("walls") ? Optional.ofNullable(BlockProvider.create(json.get("walls").getAsJsonObject())) : Optional.empty();
   }

   private static Optional<StairsBlock> parseStair(JsonObject json) throws DungeonSettingParseException {
      return json.has("stair") ? Optional.of(somethingAboutStairWithData(json)) : Optional.empty();
   }

   private static StairsBlock somethingAboutStairWithData(JsonObject json) throws DungeonSettingParseException {
      JsonObject stairData = json.get("stair").getAsJsonObject();
      JsonObject jsonObject = stairData.has("data") ? stairData.get("data").getAsJsonObject() : stairData;
      return new StairsBlock(jsonObject);
   }

   private static Optional<BlockBrush> parsePillar(JsonObject json) throws DungeonSettingParseException {
      return json.has("pillar") ? Optional.ofNullable(BlockProvider.create(json.get("pillar").getAsJsonObject())) : Optional.empty();
   }

   private static Optional<DoorBlock> parseDoor(JsonObject json) throws DungeonSettingParseException {
      return json.has("door") ? Optional.of(new DoorBlock(json.get("door"))) : Optional.empty();
   }

   private static Optional<BlockBrush> parseLightBlock(JsonObject json) throws DungeonSettingParseException {
      return json.has("lightblock") ? Optional.ofNullable(BlockProvider.create(json.get("lightblock").getAsJsonObject())) : Optional.empty();
   }

   private static Optional<BlockBrush> parseLiquid(JsonObject json) throws DungeonSettingParseException {
      return json.has("liquid") ? Optional.ofNullable(BlockProvider.create(json.get("liquid").getAsJsonObject())) : Optional.empty();
   }
}
