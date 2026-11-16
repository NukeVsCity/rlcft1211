package com.lycanitesmobs.core;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.info.ModInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


public abstract class JSONLoader {
   public void loadAllJson(ModInfo groupInfo, String name, String assetPath, String mapKey, boolean loadCustom) {
      LycanitesMobs.logDebug(name, "Loading JSON " + name + "...");
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      Map<String, JsonObject> jsons = new HashMap<>();
      Path path = Utilities.getAssetPath(groupInfo.getClass(), groupInfo.modid, assetPath);
      Map<String, JsonObject> defaultJsons = new HashMap<>();
      this.loadJsonObjects(gson, path, defaultJsons, mapKey, null);
      String configPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs" + "/";
      File customDir = new File(configPath + assetPath);
      customDir.mkdirs();
      path = customDir.toPath();
      Map<String, JsonObject> customJsons = new HashMap<>();
      this.loadJsonObjects(gson, path, customJsons, mapKey, null);
      this.writeDefaultJSONObjects(gson, defaultJsons, customJsons, jsons, loadCustom, assetPath);
      LycanitesMobs.logDebug(name, "Loading " + jsons.size() + " " + name + "...");

      for (String jsonName : jsons.keySet()) {
         try {
            JsonObject json = jsons.get(jsonName);
            LycanitesMobs.logDebug(name, "Loading " + name + " JSON: " + json);
            this.parseJson(groupInfo, name, json);
         } catch (JsonParseException var16) {
            LycanitesMobs.logWarning("", "Parsing error loading JSON " + name + ": " + jsonName);
            var16.printStackTrace();
         } catch (Exception var17) {
            LycanitesMobs.logWarning("", "There was a problem loading JSON " + name + ": " + jsonName);
            var17.printStackTrace();
         }
      }
   }

   public abstract void parseJson(ModInfo var1, String var2, JsonObject var3);

   public void loadJsonObjects(Gson gson, Path path, Map<String, JsonObject> jsonObjectMap, String mapKey, String jsonType) {
      if (path != null) {
         try {
            Iterator<Path> iterator = Files.walk(path).iterator();

            while (iterator.hasNext()) {
               Path filePath = iterator.next();
               Path relativePath = path.relativize(filePath);
               if ("json".equals(FilenameUtils.getExtension(filePath.toString()))) {
                  BufferedReader reader = null;

                  try {
                     reader = Files.newBufferedReader(filePath);
                     JsonObject json = (JsonObject)JsonUtils.fromJson(gson, reader, JsonObject.class);
                     boolean validJSON = true;
                     if (jsonType != null) {
                        if (!json.has("type")) {
                           validJSON = false;
                        } else {
                           validJSON = jsonType.equalsIgnoreCase(json.get("type").getAsString());
                        }
                     }

                     if (validJSON) {
                        jsonObjectMap.put(json.get(mapKey).getAsString(), json);
                     }
                  } catch (JsonParseException var17) {
                     LycanitesMobs.logWarning("", "Parsing error loading JSON " + relativePath + "\n" + var17.toString());
                     var17.printStackTrace();
                  } catch (Exception var18) {
                     LycanitesMobs.logWarning("", "There was a problem loading JSON " + relativePath + "\n" + var18.toString());
                     var18.printStackTrace();
                  } finally {
                     IOUtils.closeQuietly(reader);
                  }
               }
            }
         } catch (Exception var20) {
            LycanitesMobs.logWarning("", "Unable to read files from directory.\n" + var20.toString());
         }
      }
   }

   public void writeDefaultJSONObjects(
      Gson gson,
      Map<String, JsonObject> defaultJSONs,
      Map<String, JsonObject> customJSONs,
      Map<String, JsonObject> mixedJSONs,
      boolean custom,
      String assetPath
   ) {
      for (String jsonName : defaultJSONs.keySet()) {
         try {
            JsonObject defaultJSON = defaultJSONs.get(jsonName);
            boolean loadDefault = true;
            JsonObject customJSON = null;
            if (customJSONs.containsKey(jsonName)) {
               loadDefault = false;
               customJSON = customJSONs.get(jsonName);
               if (customJSON.has("loadDefault")) {
                  loadDefault = customJSON.get("loadDefault").getAsBoolean();
               }
            }

            if (loadDefault) {
               this.saveJsonObject(gson, defaultJSON, jsonName, assetPath);
               mixedJSONs.put(jsonName, defaultJSON);
            } else if (customJSON != null) {
               mixedJSONs.put(jsonName, customJSON);
            }
         } catch (JsonParseException var12) {
            LycanitesMobs.logWarning("", "Parsing error loading JSON: " + jsonName);
            var12.printStackTrace();
         } catch (Exception var13) {
            LycanitesMobs.logWarning("", "There was a problem loading JSON: " + jsonName);
            var13.printStackTrace();
         }
      }

      if (custom) {
         for (String jsonName : customJSONs.keySet()) {
            if (!defaultJSONs.containsKey(jsonName)) {
               mixedJSONs.put(jsonName, customJSONs.get(jsonName));
            }
         }
      }
   }

   public JsonObject loadJsonObject(Gson gson, Path path) {
      if (path == null) {
         return null;
      } else {
         try {
            Path relativePath = path.relativize(path);
            BufferedReader reader = null;

            JsonObject var6;
            try {
               reader = Files.newBufferedReader(path);
               JsonObject json = (JsonObject)JsonUtils.fromJson(gson, reader, JsonObject.class);
               var6 = json;
            } catch (JsonParseException var12) {
               LycanitesMobs.logWarning("", "Parsing error loading JSON " + relativePath + "\n" + var12.toString());
               var12.printStackTrace();
               return null;
            } catch (Exception var13) {
               LycanitesMobs.logWarning("", "There was a problem loading JSON " + relativePath + "\n" + var13.toString());
               var13.printStackTrace();
               return null;
            } finally {
               IOUtils.closeQuietly(reader);
            }

            return var6;
         } catch (Exception var15) {
            LycanitesMobs.logWarning("", "Unable to read file from path.\n" + var15.toString());
            return null;
         }
      }
   }

   public JsonObject writeDefaultJSONObject(Gson gson, String jsonName, JsonObject defaultJSON, JsonObject customJSON) {
      try {
         boolean loadDefault = true;
         if (customJSON != null) {
            loadDefault = false;
            if (customJSON.has("loadDefault")) {
               loadDefault = customJSON.get("loadDefault").getAsBoolean();
            }
         }

         if (loadDefault) {
            this.saveJsonObject(gson, defaultJSON, jsonName, "");
            return defaultJSON;
         }

         if (customJSON != null) {
            return customJSON;
         }
      } catch (JsonParseException var6) {
         LycanitesMobs.logWarning("", "Parsing error loading JSON: " + jsonName);
         var6.printStackTrace();
      } catch (Exception var7) {
         LycanitesMobs.logWarning("", "There was a problem loading JSON: " + jsonName);
         var7.printStackTrace();
      }

      return null;
   }

   public void saveJsonObject(Gson gson, JsonObject jsonObject, String name, String assetPath) {
      String configPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs" + "/";

      try {
         File jsonFile = new File(configPath + (!"".equals(assetPath) ? assetPath + "/" : "") + name + ".json");
         jsonFile.getParentFile().mkdirs();
         jsonFile.createNewFile();
         FileOutputStream outputStream = new FileOutputStream(jsonFile);
         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
         outputStreamWriter.append(gson.toJson(jsonObject));
         outputStreamWriter.close();
         outputStream.close();
      } catch (Exception var9) {
         LycanitesMobs.logWarning("", "Unable to save JSON into the config folder.");
         var9.printStackTrace();
      }
   }
}
