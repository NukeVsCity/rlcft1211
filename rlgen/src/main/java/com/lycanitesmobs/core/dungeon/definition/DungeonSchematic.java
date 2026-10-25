package com.lycanitesmobs.core.dungeon.definition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public class DungeonSchematic {
   public String name = "";
   public boolean enabled = true;
   public int sectorCountMin = 30;
   public int sectorCountMax = 50;
   public double corridorToCorridorChance = 0.1;
   public double roomToRoomChance = 0.1;
   public List<SpawnCondition> conditions = new ArrayList<>();
   public List<Biome> biomes = new ArrayList<>();
   public List<String> themes = new ArrayList<>();
   public List<String> entrances = new ArrayList<>();
   public List<String> rooms = new ArrayList<>();
   public List<String> corridors = new ArrayList<>();
   public List<String> stairs = new ArrayList<>();
   public List<String> towers = new ArrayList<>();
   public List<String> bossRooms = new ArrayList<>();
   public List<String> finishes = new ArrayList<>();
   public List<MobSpawn> mobSpawns = new ArrayList<>();
   public Map<Integer, List<String>> lootTables = new HashMap<>();
   public DungeonPlacer placer = new DungeonPlacer.RandomPlacer();
   public boolean canGenerateAsTower = true;

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString().toLowerCase();
      if (json.has("enabled")) {
         this.enabled = json.get("enabled").getAsBoolean();
      }

      if (json.has("sectorCountMin")) {
         this.sectorCountMin = json.get("sectorCountMin").getAsInt();
      }

      if (json.has("sectorCountMax")) {
         this.sectorCountMax = json.get("sectorCountMax").getAsInt();
      }

      if (json.has("corridorToCorridorChance")) {
         this.corridorToCorridorChance = json.get("corridorToCorridorChance").getAsDouble();
      }

      if (json.has("roomToRoomChance")) {
         this.roomToRoomChance = json.get("roomToRoomChance").getAsDouble();
      }

      if (json.has("lootTables")) {
         JsonArray jsonArray = json.get("lootTables").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject lootTableJson = jsonIterator.next().getAsJsonObject();
            int level = -1;
            if (lootTableJson.has("level")) {
               level = lootTableJson.get("level").getAsInt();
            }

            if (!this.lootTables.containsKey(level)) {
               this.lootTables.put(level, new ArrayList<>());
            }

            this.lootTables.get(level).add(lootTableJson.get("id").getAsString());
         }
      }

      if (json.has("conditions")) {
         JsonArray jsonArray = json.get("conditions").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
            SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
            this.conditions.add(spawnCondition);
         }
      }

      if (json.has("biomes")) {
         this.biomes = JSONHelper.getBiomesFromTags(JSONHelper.getJsonStrings(json.get("biomes").getAsJsonArray()));
      }

      if (json.has("themes")) {
         for (JsonElement jsonElement : json.get("themes").getAsJsonArray()) {
            String jsonString = jsonElement.getAsString().toLowerCase();
            if (!this.themes.contains(jsonString)) {
               this.themes.add(jsonString);
            }
         }
      }

      if (json.has("entrances")) {
         for (JsonElement jsonElementx : json.get("entrances").getAsJsonArray()) {
            String jsonString = jsonElementx.getAsString().toLowerCase();
            if (!this.entrances.contains(jsonString)) {
               this.entrances.add(jsonString);
            }
         }
      }

      if (json.has("rooms")) {
         for (JsonElement jsonElementxx : json.get("rooms").getAsJsonArray()) {
            String jsonString = jsonElementxx.getAsString().toLowerCase();
            if (!this.rooms.contains(jsonString)) {
               this.rooms.add(jsonString);
            }
         }
      }

      if (json.has("corridors")) {
         for (JsonElement jsonElementxxx : json.get("corridors").getAsJsonArray()) {
            String jsonString = jsonElementxxx.getAsString().toLowerCase();
            if (!this.corridors.contains(jsonString)) {
               this.corridors.add(jsonString);
            }
         }
      }

      if (json.has("stairs")) {
         for (JsonElement jsonElementxxxx : json.get("stairs").getAsJsonArray()) {
            String jsonString = jsonElementxxxx.getAsString().toLowerCase();
            if (!this.stairs.contains(jsonString)) {
               this.stairs.add(jsonString);
            }
         }
      }

      if (json.has("towers")) {
         for (JsonElement jsonElementxxxxx : json.get("towers").getAsJsonArray()) {
            String jsonString = jsonElementxxxxx.getAsString().toLowerCase();
            if (!this.towers.contains(jsonString)) {
               this.towers.add(jsonString);
            }
         }
      }

      if (json.has("bossRooms")) {
         for (JsonElement jsonElementxxxxxx : json.get("bossRooms").getAsJsonArray()) {
            String jsonString = jsonElementxxxxxx.getAsString().toLowerCase();
            if (!this.bossRooms.contains(jsonString)) {
               this.bossRooms.add(jsonString);
            }
         }
      } else {
         this.bossRooms.addAll(this.rooms);
      }

      if (json.has("finishes")) {
         for (JsonElement jsonElementxxxxxxx : json.get("finishes").getAsJsonArray()) {
            String jsonString = jsonElementxxxxxxx.getAsString().toLowerCase();
            if (!this.finishes.contains(jsonString)) {
               this.finishes.add(jsonString);
            }
         }
      }

      if (json.has("mobSpawns")) {
         JsonArray jsonArray = json.get("mobSpawns").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject mobSpawnJson = jsonIterator.next().getAsJsonObject();
            MobSpawn mobSpawn = MobSpawn.createFromJSON(mobSpawnJson);
            if (mobSpawn != null) {
               this.mobSpawns.add(mobSpawn);
            }
         }
      }

      if (json.has("placer")) {
         this.placer = DungeonPlacer.createFromJson(json.get("placer").getAsJsonObject());
      }

      if (json.has("canGenerateAsTower")) {
         this.canGenerateAsTower = json.get("canGenerateAsTower").getAsBoolean();
      }
   }

   public boolean canBuild(World world, BlockPos pos) {
      if (!this.enabled) {
         return false;
      } else {
         for (SpawnCondition condition : this.conditions) {
            if (!condition.isMet(world, null, pos)) {
               return false;
            }
         }

         return this.isValidBiome(world.getBiome(pos));
      }
   }

   public boolean isValidBiome(Biome biome) {
      return this.biomes.isEmpty() ? true : this.biomes.contains(biome);
   }

   public int getRandomSectorCount(Random random) {
      return this.sectorCountMax <= this.sectorCountMin
         ? this.sectorCountMin
         : this.sectorCountMin + random.nextInt(this.sectorCountMax - this.sectorCountMin + 1);
   }

   public DungeonSector getRandomSector(String type, Random random) {
      List<String> sectorList;
      if ("entrance".equalsIgnoreCase(type)) {
         sectorList = this.entrances;
      } else if ("corridor".equalsIgnoreCase(type)) {
         sectorList = this.corridors;
      } else if ("stairs".equalsIgnoreCase(type)) {
         sectorList = this.stairs;
      } else if ("tower".equalsIgnoreCase(type)) {
         sectorList = this.towers;
      } else if ("bossRoom".equalsIgnoreCase(type)) {
         sectorList = this.bossRooms;
      } else if ("finish".equalsIgnoreCase(type)) {
         sectorList = this.finishes;
      } else {
         sectorList = this.rooms;
      }

      List<DungeonSector> sectors = new ArrayList<>();
      int totalWeights = 0;

      for (String sectorName : sectorList) {
         DungeonSector sector = com.lycanitesmobs.core.dungeon.DungeonManager.getInstance().getSector(sectorName);
         if (sector != null && sector.weight > 0) {
            sectors.add(sector);
            totalWeights += sector.weight;
         }
      }

      if (sectors.isEmpty()) {
         Lyc.warn("Dungeon", "Unable to find any " + type + " sectors for the dungeon: " + this.name);
         return null;
      } else if (sectors.size() == 1) {
         return sectors.get(0);
      } else {
         int randomWeight = random.nextInt(totalWeights) + 1;
         int searchedWeight = 0;

         for (DungeonSector sector : sectors) {
            if (randomWeight <= sector.weight + searchedWeight) {
               return sector;
            }

            searchedWeight += sector.weight;
         }

         return sectors.get(sectors.size() - 1);
      }
   }

   public String getNextConnectingSector(String parentType, Random random) {
      if ("room".equalsIgnoreCase(parentType)) {
         return random.nextDouble() <= this.roomToRoomChance ? "room" : "corridor";
      } else if ("corridor".equalsIgnoreCase(parentType)) {
         return random.nextDouble() <= this.corridorToCorridorChance ? "corridor" : "room";
      } else if ("entrance".equalsIgnoreCase(parentType)) {
         return random.nextBoolean() ? "corridor" : "room";
      } else {
         return "room";
      }
   }

   public DungeonTheme getRandomTheme(Random random) {
      List<DungeonTheme> themes = new ArrayList<>();

      for (String themeName : this.themes) {
         DungeonTheme dungeonTheme = com.lycanitesmobs.core.dungeon.DungeonManager.getInstance().getTheme(themeName);
         if (dungeonTheme != null) {
            themes.add(dungeonTheme);
         }
      }

      if (themes.isEmpty()) {
         Lyc.warn("Dungeon", "No Dungeon Themes Found For " + this.name);
         return null;
      } else {
         return themes.size() == 1 ? themes.get(0) : themes.get(random.nextInt(themes.size()));
      }
   }

   public MobSpawn getRandomMobSpawn(int level, boolean boss, Random random) {
      int levelMin = level;
      int levelMax = level;
      if (level < 0) {
         levelMax = -level * 2;
         levelMin = levelMax - 1;
      }

      int totalWeights = 0;
      List<MobSpawn> mobSpawns = new ArrayList<>();

      for (MobSpawn mobSpawn : this.mobSpawns) {
         if (mobSpawn.dungeonBoss == boss
            && (mobSpawn.dungeonLevelMin < 0 || mobSpawn.dungeonLevelMin <= levelMax)
            && (mobSpawn.dungeonLevelMax < 0 || mobSpawn.dungeonLevelMax >= levelMin)
            && mobSpawn.getWeight() > 0) {
            mobSpawns.add(mobSpawn);
            totalWeights += mobSpawn.getWeight();
         }
      }

      if (totalWeights <= 0) {
         return null;
      } else {
         int randomWeight = 1;
         if (totalWeights > 1) {
            randomWeight = random.nextInt(totalWeights);
         }

         int searchWeight = 0;
         MobSpawn chosenMobSpawn = null;

         for (MobSpawn mobSpawnx : mobSpawns) {
            chosenMobSpawn = mobSpawnx;
            if (mobSpawnx.getWeight() + searchWeight > randomWeight) {
               break;
            }

            searchWeight += mobSpawnx.getWeight();
         }

         return chosenMobSpawn;
      }
   }

   public ResourceKey<LootTable> getRandomLootTable(int level, Random random) {
      if (level < 0) {
         level = -level * 2;
      }

      List<String> possibleLootTables = new ArrayList<>();
      if (this.lootTables.containsKey(-1)) {
         possibleLootTables.addAll(this.lootTables.get(-1));
      }

      for (int i = 0; i <= level; i++) {
         if (this.lootTables.containsKey(i)) {
            possibleLootTables.addAll(this.lootTables.get(i));
         }
      }

      if (possibleLootTables.isEmpty()) {
         return null;
      } else {
         String id = possibleLootTables.size() == 1 ? possibleLootTables.get(0) : possibleLootTables.get(random.nextInt(possibleLootTables.size()));
         // 1.12's blacksmith chest is the 1.21 weaponsmith's
         if (id.equals("minecraft:chests/village_blacksmith")) {
            id = "minecraft:chests/village/village_weaponsmith";
         }
         return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(id));
      }
   }
}
