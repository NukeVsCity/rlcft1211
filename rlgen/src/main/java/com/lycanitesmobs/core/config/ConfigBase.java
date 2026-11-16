package com.lycanitesmobs.core.config;

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
import net.minecraft.world.level.pathfinder.*;
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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.ItemDrop;
import com.lycanitesmobs.core.info.ModInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;


public class ConfigBase {
   public static Map<String, ConfigBase> configs = new HashMap<>();
   public Configuration config;
   public ModInfo group;
   public String configName;
   public String fileName;
   public List<IConfigListener> updateListeners = new ArrayList<>();

   public static void versionCheck(String minVersion, String currentVersion) {
      ConfigBase versionConfig = getConfig(LycanitesMobs.modInfo, "version");
      String configVersion = versionConfig.getString(
         "Version",
         "Config Version",
         "0.0.0.0",
         "The version that this config was last read from, manually update this if you do not want your config to be cleared, although it is recommended not to unless you are aware of the changes."
      );
      String[] minVersions = minVersion.split("\\.");
      String[] configVersions = configVersion.split("\\.");
      if (configVersions.length != 4) {
         configVersions = "0.0.0.0".split("\\.");
      }

      boolean oldVersion = false;

      for (int i = 0; i < 4; i++) {
         int minVerNum = 0;
         int currentVerNum = 0;

         try {
            minVerNum = Integer.parseInt(minVersions[i].replaceAll("[^\\d.]", ""));
            currentVerNum = Integer.parseInt(configVersions[i].replaceAll("[^\\d.]", ""));
         } catch (NumberFormatException var12) {
         }

         if (currentVerNum < minVerNum) {
            oldVersion = true;
            break;
         }

         if (currentVerNum > minVerNum) {
            break;
         }
      }

      if (oldVersion) {
         String configDirPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs";
         File configDir = new File(configDirPath);
         configDir.mkdir();
         LycanitesMobs.logWarning("", "[Config] The current configs are too old, clearing all configs now...");

         try {
            FileUtils.cleanDirectory(configDir);
         } catch (IOException var11) {
            LycanitesMobs.logWarning("", "[Config] Unable to clear the config directory! This could be a file permissions issue!");
            var11.printStackTrace();
         }
      }

      currentVersion = currentVersion.replace(" ", "");
      currentVersion = currentVersion.split("-")[0];
      if (!configVersion.equals(currentVersion)) {
         versionConfig.putString("Version", "Config Version", currentVersion);
      }
   }

   public static void registerConfig(ConfigBase config) {
      if (config != null) {
         configs.put(config.fileName, config);
      }
   }

   public static ConfigBase getConfig(ModInfo group, String configName) {
      String configFileName = configName.toLowerCase();
      if (!"lycanitesmobs".equalsIgnoreCase(group.modid)) {
         configFileName = group.modid + "-" + configFileName;
      }

      if (!configs.containsKey(configFileName)) {
         registerConfig(new ConfigBase(group, configName, configFileName));
      }

      return configs.get(configFileName);
   }

   public ConfigBase(ModInfo group, String name, String filename) {
      this.group = group;
      this.configName = name;
      this.fileName = filename;
      this.init();
   }

   public void init() {
      String configDirPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs";
      File configDir = new File(configDirPath);
      configDir.mkdir();
      File configFile = new File(configDirPath + "/" + this.fileName + ".cfg");

      try {
         configFile.createNewFile();
         LycanitesMobs.logInfo("", "Config " + this.fileName + " created successfully.");
      } catch (IOException var5) {
         LycanitesMobs.logWarning("", "Config " + this.fileName + " could not be created:");
         System.out.println(var5);
         LycanitesMobs.logWarning(
            "",
            "Make sure the config folder has write permissions or (if using Windows) isn't read only and that Minecraft is not in Program Files on a non-administrator account."
         );
      }

      this.config = new Configuration(configFile);
      this.config.load();
   }

   public void update() {
      this.config.save();

      for (IConfigListener updateListener : this.updateListeners) {
         if (updateListener != null) {
            updateListener.onConfigUpdate(this);
         }
      }
   }

   public void addListener(IConfigListener listener) {
      if (!this.updateListeners.contains(listener)) {
         this.updateListeners.add(listener);
      }
   }

   public void setCategoryComment(String category, String comment) {
      category = category.toLowerCase();
      this.config.addCustomCategoryComment(category, comment);
   }

   public boolean setCategoryReplacedComment(String category, String newCategory) {
      category = category.toLowerCase();
      if (this.config.hasCategory(category)) {
         this.config
            .addCustomCategoryComment(
               category,
               "This category has been replaced with the "
                  + newCategory
                  + " category, your settings have been copied to the new category for you and this category can now safely be deleted."
            );
         return true;
      } else {
         return false;
      }
   }

   public boolean getBool(String category, String key) {
      return this.getBool(category, key, false);
   }

   public boolean getBool(String category, String key, boolean defaultValue) {
      return this.getBool(category, key, defaultValue, null);
   }

   public boolean getBool(String category, String key, boolean defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getBoolean(defaultValue);
   }

   public int getInt(String category, String key) {
      return this.getInt(category, key, 0);
   }

   public int getInt(String category, String key, int defaultValue) {
      return this.getInt(category, key, defaultValue, null);
   }

   public int getInt(String category, String key, int defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getInt(defaultValue);
   }

   public double getDouble(String category, String key) {
      return this.getDouble(category, key, 0.0);
   }

   public double getDouble(String category, String key, double defaultValue) {
      return this.getDouble(category, key, defaultValue, null);
   }

   public double getDouble(String category, String key, double defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getDouble(defaultValue);
   }

   public String getString(String category, String key) {
      return this.getString(category, key, "");
   }

   public String getString(String category, String key, String defaultValue) {
      return this.getString(category, key, defaultValue, null);
   }

   public String getString(String category, String key, String defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getString();
   }

   public ItemDrop getItemDrop(String category, String key) {
      return this.getItemDrop(category, key, null);
   }

   public ItemDrop getItemDrop(String category, String key, ItemDrop defaultValue) {
      return this.getItemDrop(category, key, defaultValue, null);
   }

   public ItemDrop getItemDrop(String category, String key, ItemDrop defaultValue, String comment) {
      String defaultValueString = "";
      if (defaultValue != null) {
         defaultValueString = defaultValue.toConfigString();
      }

      String itemDropString = this.getString(category, key, defaultValueString, comment).replace(" ", "");
      return ItemDrop.createFromConfigString(itemDropString);
   }

   public boolean[] getBoolList(String category, String key) {
      return this.getBoolList(category, key, new boolean[]{false});
   }

   public boolean[] getBoolList(String category, String key, boolean[] defaultValue) {
      return this.getBoolList(category, key, defaultValue, null);
   }

   public boolean[] getBoolList(String category, String key, boolean[] defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getBooleanList();
   }

   public int[] getIntList(String category, String key) {
      return this.getIntList(category, key, new int[]{0});
   }

   public int[] getIntList(String category, String key, int[] defaultValue) {
      return this.getIntList(category, key, defaultValue, null);
   }

   public int[] getIntList(String category, String key, int[] defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getIntList();
   }

   public double[] getDoubleList(String category, String key) {
      return this.getDoubleList(category, key, new double[]{0.0});
   }

   public double[] getDoubleList(String category, String key, double[] defaultValue) {
      return this.getDoubleList(category, key, defaultValue, null);
   }

   public double[] getDoubleList(String category, String key, double[] defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getDoubleList();
   }

   public String[] getStringList(String category, String key) {
      return this.getStringList(category, key, new String[]{""});
   }

   public String[] getStringList(String category, String key, String[] defaultValue) {
      return this.getStringList(category, key, defaultValue, null);
   }

   public String[] getStringList(String category, String key, String[] defaultValue, String comment) {
      category = category.toLowerCase();
      boolean newEntry = !this.config.getCategory(category).containsKey(key);
      Property property = this.config.get(category, key, defaultValue);
      if (comment != null) {
         property.setComment(comment);
      }

      if (newEntry) {
         this.config.save();
      }

      return property.getStringList();
   }

   public void setBool(String category, String key, boolean value) {
      this.setBool(category, key, value, null);
   }

   public void setBool(String category, String key, boolean value, String comment) {
      category = category.toLowerCase();
      Property property = this.config.get(category, key, value);
      if (comment != null) {
         property.setComment(comment);
      }

      property.set(value);
      this.update();
   }

   public void setInt(String category, String key, int value) {
      this.setInt(category, key, value, null);
   }

   public void setInt(String category, String key, int value, String comment) {
      category = category.toLowerCase();
      Property property = this.config.get(category, key, value);
      if (comment != null) {
         property.setComment(comment);
      }

      property.set(value);
      this.update();
   }

   public void setDouble(String category, String key, double value) {
      this.putDouble(category, key, value, null);
   }

   public void setDouble(String category, String key, double value, String comment) {
      category = category.toLowerCase();
      Property property = this.config.get(category, key, value);
      if (comment != null) {
         property.setComment(comment);
      }

      property.set(value);
      this.update();
   }

   public void setString(String category, String key, String value) {
      this.putString(category, key, value, null);
   }

   public void setString(String category, String key, String value, String comment) {
      category = category.toLowerCase();
      Property property = this.config.get(category, key, value);
      if (comment != null) {
         property.setComment(comment);
      }

      property.set(value);
      this.update();
   }

   public void setList(String category, String key, Object[] objValues) {
      this.setList(category, key, objValues, null);
   }

   public void setList(String category, String key, Object[] objValues, String comment) {
      category = category.toLowerCase();
      List<String> valuesList = new ArrayList<>();

      for (Object objValue : objValues) {
         valuesList.add(objValue.toString());
      }

      String[] values = valuesList.toArray(new String[valuesList.size()]);
      Property property = this.config.get(category, key, values);
      if (comment != null) {
         property.setComment(comment);
      }

      property.set(values);
      this.update();
   }
}
