package com.lycanitesmobs.core.spawner;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SpawnerManager extends JSONLoader {
   public static SpawnerManager INSTANCE;
   public Map<String, Spawner> spawners = new HashMap<>();
   public List<SpawnCondition> globalSpawnConditions = new ArrayList<>();

   public static SpawnerManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new SpawnerManager();
      }

      return INSTANCE;
   }

   public void loadAllFromJSON() {
      LycanitesMobs.logDebug("JSONSpawner", "Loading JSON Spawners!");
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      Map<String, JsonObject> spawnerJSONs = new HashMap<>();
      Path path = Utilities.getAssetPath(this.getClass(), LycanitesMobs.modInfo.modid, "spawners");
      Map<String, JsonObject> defaultSpawnerJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, defaultSpawnerJSONs, "name", "spawner");
      path = Utilities.getAssetPath(this.getClass(), LycanitesMobs.modInfo.modid, "mobevents");
      Map<String, JsonObject> defaultMobEventsJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, defaultMobEventsJSONs, "name", "spawner");
      String configPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs" + "/";
      File customSpawnersDir = new File(configPath + "spawners");
      customSpawnersDir.mkdirs();
      path = customSpawnersDir.toPath();
      Map<String, JsonObject> customSpawnerJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, customSpawnerJSONs, "name", "spawner");
      File customMobEventsDir = new File(configPath + "mobevents");
      path = customMobEventsDir.toPath();
      Map<String, JsonObject> customMobEventsJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, customMobEventsJSONs, "name", "spawner");
      this.writeDefaultJSONObjects(gson, defaultSpawnerJSONs, customSpawnerJSONs, spawnerJSONs, true, "spawners");
      this.writeDefaultJSONObjects(gson, defaultMobEventsJSONs, customMobEventsJSONs, spawnerJSONs, true, "mobevents");
      LycanitesMobs.logDebug("", "Loading " + spawnerJSONs.size() + " JSON Spawners...");

      for (String spawnerJSONName : spawnerJSONs.keySet()) {
         try {
            JsonObject spawnerJSON = spawnerJSONs.get(spawnerJSONName);
            LycanitesMobs.logDebug("JSONSpawner", "Loading Spawner JSON: " + spawnerJSON);
            Spawner spawner = new Spawner();
            spawner.loadFromJSON(spawnerJSON);
            this.addSpawner(spawner);
         } catch (JsonParseException var20) {
            LycanitesMobs.logWarning("", "Parsing error loading JSON Spawner: " + spawnerJSONName);
            var20.printStackTrace();
         } catch (Exception var21) {
            LycanitesMobs.logWarning("", "There was a problem loading JSON Spawner: " + spawnerJSONName);
            var21.printStackTrace();
         }
      }

      LycanitesMobs.logDebug("", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");
      this.globalSpawnConditions.clear();
      Path defaultGlobalPath = Utilities.getAssetPath(this.getClass(), LycanitesMobs.modInfo.modid, "globalspawner.json");
      JsonObject defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
      File customGlobalFile = new File(configPath + "globalspawner.json");
      JsonObject customGlobalJson = null;
      if (customGlobalFile.exists()) {
         customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
      }

      JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalspawner", defaultGlobalJson, customGlobalJson);
      if (globalJson.has("conditions")) {
         JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
            SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
            this.globalSpawnConditions.add(spawnCondition);
         }
      }

      if (this.globalSpawnConditions.size() > 0) {
         LycanitesMobs.logDebug("JSONSpawner", "Loaded " + this.globalSpawnConditions.size() + " Global Spawn Conditions.");
      }
   }

   @Override
   public void parseJson(ModInfo groupInfo, String name, JsonObject json) {
   }

   public void reload() {
      LycanitesMobs.logDebug("JSONSpawner", "Destroying JSON Spawners!");

      for (Spawner spawner : this.spawners.values().toArray(new Spawner[this.spawners.size()])) {
         spawner.destroy();
      }

      this.loadAllFromJSON();
   }

   public void addSpawner(Spawner spawner) {
      if (this.spawners.containsKey(spawner.name)) {
         LycanitesMobs.logWarning("", "[Spawner Manager] Tried to add a Spawner with a name that is already in use: " + spawner.name);
      } else if (this.spawners.values().contains(spawner)) {
         LycanitesMobs.logWarning("", "[Spawner Manager] Tried to add a Spawner that is already added: " + spawner.name);
      } else {
         this.spawners.put(spawner.name, spawner);
      }
   }

   public void removeSpawner(Spawner spawner) {
      if (!this.spawners.containsKey(spawner.name)) {
         LycanitesMobs.logWarning("", "[Spawner Manager] Tried to remove a Spawner that hasn't been added: " + spawner.name);
      } else {
         this.spawners.remove(spawner.name);
      }
   }
}
