package com.lycanitesmobs.core.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lycanitesmobs.core.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.dungeon.definition.DungeonStructure;
import com.lycanitesmobs.core.dungeon.definition.DungeonTheme;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DungeonManager {
   public static com.lycanitesmobs.core.dungeon.DungeonManager INSTANCE;
   public Map<String, DungeonTheme> themes = new HashMap<>();
   public Map<String, DungeonStructure> structures = new HashMap<>();
   public Map<String, DungeonSector> sectors = new HashMap<>();
   public Map<String, DungeonSchematic> schematics = new HashMap<>();

   public static com.lycanitesmobs.core.dungeon.DungeonManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new com.lycanitesmobs.core.dungeon.DungeonManager();
         INSTANCE.loadAllFromJSON();
      }

      return INSTANCE;
   }

   public void loadAllFromJSON() {
      Map<String, JsonObject> themeJSONs = this.loadDungeonsFromJSON("themes");
      

      for (String jsonName : themeJSONs.keySet()) {
         try {
            JsonObject json = themeJSONs.get(jsonName);
            
            DungeonTheme theme = new DungeonTheme();
            theme.loadFromJSON(json);
            this.addTheme(theme);
         } catch (JsonParseException var15) {
            Lyc.warn("", "Parsing error loading JSON Dungeon Theme: " + jsonName);
         } catch (Exception var16) {
            Lyc.warn("", "There was a problem loading JSON Dungeon Theme: " + jsonName);
         }
      }

      
      Map<String, JsonObject> structureJSONs = this.loadDungeonsFromJSON("structures");
      

      for (String jsonName : structureJSONs.keySet()) {
         try {
            JsonObject json = structureJSONs.get(jsonName);
            
            DungeonStructure structure = new DungeonStructure();
            structure.loadFromJSON(json);
            this.addStructure(structure);
         } catch (JsonParseException var13) {
            Lyc.warn("", "Parsing error loading JSON Dungeon Structure: " + jsonName);
         } catch (Exception var14) {
            Lyc.warn("", "There was a problem loading JSON Dungeon Structure: " + jsonName);
         }
      }

      
      Map<String, JsonObject> sectorJSONs = this.loadDungeonsFromJSON("sectors");
      

      for (String jsonName : sectorJSONs.keySet()) {
         try {
            JsonObject json = sectorJSONs.get(jsonName);
            
            DungeonSector sector = new DungeonSector();
            sector.loadFromJSON(json);
            this.addSector(sector);
         } catch (JsonParseException var11) {
            Lyc.warn("", "Parsing error loading JSON Dungeon Sector: " + jsonName);
         } catch (Exception var12) {
            Lyc.warn("", "There was a problem loading JSON Dungeon Sector: " + jsonName);
         }
      }

      
      Map<String, JsonObject> schematicJSONs = this.loadDungeonsFromJSON("schematics");
      

      for (String jsonName : schematicJSONs.keySet()) {
         try {
            JsonObject json = schematicJSONs.get(jsonName);
            
            DungeonSchematic schematic = new DungeonSchematic();
            schematic.loadFromJSON(json);
            this.addSchematic(schematic);
         } catch (JsonParseException var9) {
            Lyc.warn("", "Parsing error loading JSON Dungeon Schematics: " + jsonName);
         } catch (Exception var10) {
            Lyc.warn("", "There was a problem loading JSON Dungeon Schematics: " + jsonName);
         }
      }

      
   }

   /** The JSONs RLCraft loaded (scripts/gen_lyc_dungeons.py), in a HashMap by name like 1.12. */
   public Map<String, JsonObject> loadDungeonsFromJSON(String type) {
      Map<String, JsonObject> jsons = new HashMap<>();
      String dir = "/data/rlgen/lycanites/dungeons/" + type + "/";
      try (java.io.Reader r = new java.io.InputStreamReader(DungeonManager.class.getResourceAsStream(dir + "index.json"), java.nio.charset.StandardCharsets.UTF_8)) {
         for (com.google.gson.JsonElement e : com.google.gson.JsonParser.parseReader(r).getAsJsonArray()) {
            try (java.io.Reader r2 = new java.io.InputStreamReader(DungeonManager.class.getResourceAsStream(dir + e.getAsString() + ".json"), java.nio.charset.StandardCharsets.UTF_8)) {
               JsonObject json = com.google.gson.JsonParser.parseReader(r2).getAsJsonObject();
               jsons.put(json.get("name").getAsString(), json);
            }
         }
      } catch (java.io.IOException ex) {
         throw new IllegalStateException("rlgen: can't read Lycanites dungeon " + type, ex);
      }
      return jsons;
   }

   public void reload() {
      
      this.themes.clear();
      this.structures.clear();
      this.sectors.clear();
      this.schematics.clear();
      this.loadAllFromJSON();
   }

   public void addTheme(DungeonTheme theme) {
      if (this.themes.containsKey(theme.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Theme with a name that is already in use: " + theme.name);
      } else if (this.themes.values().contains(theme)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Theme that is already added: " + theme.name);
      } else {
         this.themes.put(theme.name, theme);
      }
   }

   public void removeTheme(DungeonTheme theme) {
      if (!this.themes.containsKey(theme.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to remove a Dungeon Theme that hasn't been added: " + theme.name);
      } else {
         this.themes.remove(theme.name);
      }
   }

   public DungeonTheme getTheme(String name) {
      if (!this.themes.containsKey(name)) {
         Lyc.warn("Dungeon", "Unable to find a theme called " + name);
         return null;
      } else {
         return this.themes.get(name);
      }
   }

   public void addStructure(DungeonStructure structure) {
      if (this.structures.containsKey(structure.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Structure with a name that is already in use: " + structure.name);
      } else if (this.structures.values().contains(structure)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Structure that is already added: " + structure.name);
      } else {
         this.structures.put(structure.name, structure);
      }
   }

   public void removeStructure(DungeonStructure structure) {
      if (!this.structures.containsKey(structure.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to remove a Dungeon Structure that hasn't been added: " + structure.name);
      } else {
         this.structures.remove(structure.name);
      }
   }

   public DungeonStructure getStructure(String name) {
      return !this.structures.containsKey(name) ? null : this.structures.get(name);
   }

   public void addSector(DungeonSector sector) {
      if (this.sectors.containsKey(sector.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Sector with a name that is already in use: " + sector.name);
      } else if (this.sectors.values().contains(sector)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Sector that is already added: " + sector.name);
      } else {
         this.sectors.put(sector.name, sector);
      }
   }

   public void removeSector(DungeonSector sector) {
      if (!this.sectors.containsKey(sector.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to remove a Dungeon Sector that hasn't been added: " + sector.name);
      } else {
         this.sectors.remove(sector.name);
      }
   }

   public DungeonSector getSector(String name) {
      if (!this.sectors.containsKey(name)) {
         Lyc.warn("Dungeon", "Unable to find a Dungeon Sector by the name: " + name);
         return null;
      } else {
         return this.sectors.get(name);
      }
   }

   public void addSchematic(DungeonSchematic schematic) {
      if (this.schematics.containsKey(schematic.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Schematic with a name that is already in use: " + schematic.name);
      } else if (this.schematics.values().contains(schematic)) {
         Lyc.warn("", "[Dungeon Manager] Tried to add a Dungeon Schematic that is already added: " + schematic.name);
      } else {
         this.schematics.put(schematic.name, schematic);
      }
   }

   public void removeSchematic(DungeonSchematic schematic) {
      if (!this.schematics.containsKey(schematic.name)) {
         Lyc.warn("", "[Dungeon Manager] Tried to remove a Dungeon Schematic that hasn't been added: " + schematic.name);
      } else {
         this.schematics.remove(schematic.name);
      }
   }

   public DungeonSchematic getSchematic(String name) {
      return !this.schematics.containsKey(name) ? null : this.schematics.get(name);
   }
}
