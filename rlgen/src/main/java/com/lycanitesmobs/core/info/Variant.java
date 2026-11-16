package com.lycanitesmobs.core.info;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.entity.CreatureStats;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Variant {
   public static int BASE_WEIGHT = 400;
   public static Map<String, Integer> COMMON_WEIGHTS = new HashMap<String, Integer>() {
      {
         this.put("common", Integer.valueOf(100));
         this.put("uncommon", Integer.valueOf(20));
         this.put("rare", Integer.valueOf(2));
         this.put("legendary", Integer.valueOf(1));
      }
   };
   public static Map<String, Double> STAT_MULTIPLIERS = new HashMap<>();
   public static int UNCOMMON_DROP_SCALE = 2;
   public static int RARE_DROP_SCALE = 5;
   public static double UNCOMMON_EXPERIENCE_SCALE = 2.0;
   public static double RARE_EXPERIENCE_SCALE = 10.0;
   public static int UNCOMMON_SPAWN_DAY_MIN = 0;
   public static int RARE_SPAWN_DAY_MIN = 0;
   public static boolean RARE_DESPAWNING = false;
   public static boolean RARE_HEALTH_BARS = false;
   public int index;
   public double scale = 1.0;
   public String color;
   public String rarity = "uncommon";
   public int weight = BASE_WEIGHT;

   public static void loadGlobalSettings(ConfigBase config) {
      BASE_WEIGHT = config.getInt("Mob Variations", "Variant Base Weight", BASE_WEIGHT, "The weight of base variants (regular mob colors).");
      COMMON_WEIGHTS.put(
         "uncommon",
         config.getInt(
            "Mob Variations",
            "Variant Uncommon Weight",
            COMMON_WEIGHTS.get("uncommon"),
            "The weight of uncommon variants (such as Azure, Verdant, Scarlet, etc)."
         )
      );
      COMMON_WEIGHTS.put(
         "rare",
         config.getInt("Mob Variations", "Variant Rare Weight", COMMON_WEIGHTS.get("rare"), "The weight of rare variants (such as Lunar or Celestial).")
      );
      String[] subspeciesNames = new String[]{"uncommon", "rare"};
      STAT_MULTIPLIERS = new HashMap<>();
      boolean foundOldStatsCategory = config.setCategoryReplacedComment("Subspecies Multipliers", "Variant Multipliers");
      config.setCategoryComment("Variant Multipliers", "Here you can scale the stats of every mob on a per variant basis.");

      for (String subspeciesName : subspeciesNames) {
         for (String statName : CreatureStats.STAT_NAMES) {
            double defaultValue = 1.0;
            if ("uncommon".equals(subspeciesName) && "health".equals(statName)) {
               defaultValue = 2.0;
            }

            if ("rare".equals(subspeciesName)) {
               if ("health".equals(statName)) {
                  defaultValue = 20.0;
               } else if ("attackSpeed".equals(statName)) {
                  defaultValue = 2.0;
               } else if ("rangedSpeed".equals(statName)) {
                  defaultValue = 2.0;
               } else if ("effect".equals(statName)) {
                  defaultValue = 2.0;
               }
            }

            if (foundOldStatsCategory) {
               defaultValue = config.getDouble("Subspecies Multipliers", subspeciesName + " " + statName, defaultValue);
            }

            STAT_MULTIPLIERS.put(
               (subspeciesName + "-" + statName).toUpperCase(Locale.ENGLISH),
               config.getDouble("Variant Multipliers", subspeciesName + " " + statName, defaultValue)
            );
         }
      }

      UNCOMMON_DROP_SCALE = config.getInt(
         "Mob Variations",
         "Variant Uncommon Item Drops Scale",
         UNCOMMON_DROP_SCALE,
         "When a creature with the uncommon subspecies (Azure, Verdant, etc) dies, its item drops amount is multiplied by this value."
      );
      RARE_DROP_SCALE = config.getInt(
         "Mob Variations",
         "Variant Rare Item Drops Scale",
         RARE_DROP_SCALE,
         "When a creature with the rare subspecies (Celestial, Lunar, etc) dies, its item drops amount is multiplied by this value."
      );
      UNCOMMON_EXPERIENCE_SCALE = config.getDouble(
         "Mob Variations",
         "Variant Uncommon Experience Scale",
         UNCOMMON_EXPERIENCE_SCALE,
         "When a creature with the uncommon subspecies (Azure, Verdant, etc) dies, its experience amount is multiplied by this value."
      );
      RARE_EXPERIENCE_SCALE = config.getDouble(
         "Mob Variations",
         "Variant Rare Experience Scale",
         RARE_EXPERIENCE_SCALE,
         "When a creature with the rare subspecies (Celestial, Lunar, etc) dies, its experience amount is multiplied by this value."
      );
      UNCOMMON_SPAWN_DAY_MIN = config.getInt(
         "Mob Variations", "Variant Uncommon Spawn Day Min", UNCOMMON_SPAWN_DAY_MIN, "The minimum amount of days before uncommon species start to spawn."
      );
      RARE_SPAWN_DAY_MIN = config.getInt(
         "Mob Variations", "Variant Rare Spawn Day Min", RARE_SPAWN_DAY_MIN, "The minimum amount of days before rare species start to spawn."
      );
      RARE_DESPAWNING = config.getBool(
         "Mob Variations",
         "Variant Rare Despawning",
         RARE_DESPAWNING,
         "If set to true, rare subspecies such as the Lunar Grue will despawn naturally over time."
      );
      RARE_HEALTH_BARS = config.getBool(
         "Mob Variations",
         "Variant Rare Health Bars",
         RARE_HEALTH_BARS,
         "If set to true, rare subspecies such as the Lunar Grue or Celestial Geonach will display boss health bars."
      );
   }

   public static Variant createFromJSON(CreatureInfo creatureInfo, JsonObject json) {
      String rarity = "uncommon";
      if (json.has("rarity")) {
         rarity = json.get("rarity").getAsString().toLowerCase();
      } else if (json.has("type")) {
         rarity = json.get("type").getAsString().toLowerCase();
      }

      String color = null;
      if (json.has("color")) {
         color = json.get("color").getAsString().toLowerCase();
      } else if (json.has("name")) {
         color = json.get("name").getAsString().toLowerCase();
      }

      if (color == null) {
         throw new RuntimeException("Invalid subspecies variant added with no color defined!");
      } else {
         Variant variant = new Variant(color, rarity, json.get("index").getAsInt());
         if (json.has("scale")) {
            variant.scale = json.get("scale").getAsDouble();
         }

         return variant;
      }
   }

   public Variant(String color, String rarity, int index) {
      this.color = color;
      this.rarity = rarity;
      this.index = index;
      this.weight = COMMON_WEIGHTS.get(rarity);
   }

   public String getTitle() {
      return LanguageManager.translate("subspecies." + this.color + ".name");
   }

   public double getScale() {
      return this.scale;
   }

   public boolean canSpawn(LivingEntity entityLiving) {
      if (entityLiving != null) {
         Level world = entityLiving.level();
         int day = (int)Math.floor((double)world.getGameTime() / 23999.0);
         int spawnDayMin = 0;
         if ("uncommon".equalsIgnoreCase(this.rarity)) {
            spawnDayMin = UNCOMMON_SPAWN_DAY_MIN;
         } else if ("rare".equalsIgnoreCase(this.rarity)) {
            spawnDayMin = RARE_SPAWN_DAY_MIN;
         }

         if (day < spawnDayMin) {
            return false;
         }
      }

      return true;
   }

   @Override
   public String toString() {
      return this.color + " - " + this.weight;
   }

   public static int getIndexFromOld(int oldIndex) {
      return oldIndex % 4;
   }
}
