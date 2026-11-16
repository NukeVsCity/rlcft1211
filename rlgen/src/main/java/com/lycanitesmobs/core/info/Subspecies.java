package com.lycanitesmobs.core.info;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Subspecies {
   public Map<Integer, Variant> variants = new HashMap<>();
   public int index;
   public int priority = 0;
   public String name;
   public Class<?> modelClass; // 1.12 ModelBase class (client)
   public List<ElementInfo> elements = new ArrayList<>();
   public List<SpawnCondition> spawnConditions = new ArrayList<>();
   public Vec3 mountOffset = new Vec3(0.0, 1.0, 0.0);

   public static Subspecies createFromJSON(CreatureInfo creatureInfo, JsonObject json) {
      String name = null;
      if (json.has("name")) {
         name = json.get("name").getAsString().toLowerCase();
      }

      Subspecies subspecies = new Subspecies(name, json.get("index").getAsInt());
      if (json.has("variants")) {
         for (JsonElement jsonElement : json.get("variants").getAsJsonArray()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Variant variant = Variant.createFromJSON(creatureInfo, jsonObject);
            subspecies.variants.put(variant.index, variant);
         }
      }

      if (json.has("modelClass")) {
         try {
            LycanitesMobs.proxy.loadSubspeciesModel(subspecies, json.get("modelClass").getAsString());
         } catch (Exception var9) {
            LycanitesMobs.logWarning(
               "",
               "[Creature] Unable to find a valid Java Model Class: "
                  + json.get("modelClass").getAsString()
                  + " for subspecies: "
                  + subspecies.getTitle()
                  + " entity: "
                  + creatureInfo.getTitle()
            );
         }
      }

      if (json.has("priority")) {
         subspecies.priority = json.get("priority").getAsInt();
      }

      List<String> elementNames = new ArrayList<>();
      if (json.has("element")) {
         elementNames.add(json.get("element").getAsString());
      }

      if (json.has("elements")) {
         elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
      }

      subspecies.elements.clear();

      for (String elementName : elementNames) {
         ElementInfo element = ElementManager.getInstance().getElement(elementName);
         if (element == null) {
            throw new RuntimeException("[Creature] The element " + elementName + " cannot be found for subspecies: " + subspecies.name);
         }

         subspecies.elements.add(element);
      }

      if (json.has("conditions")) {
         JsonArray jsonArray = json.get("conditions").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
            SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
            subspecies.spawnConditions.add(spawnCondition);
         }
      }

      subspecies.mountOffset = JSONHelper.getVec3d(json, "mountOffset", null);
      return subspecies;
   }

   public Subspecies(String name, int index) {
      this.name = name;
      this.index = index;
   }

   public void load(CreatureInfo creatureInfo) {
      if (this.name != null && !creatureInfo.loadedSubspeciesSkins.contains(this.name)) {
         creatureInfo.addSounds("." + this.name);
         creatureInfo.loadedSubspeciesSkins.add(this.name);
      }
   }

   public String getTitle() {
      return this.name != null ? LanguageManager.translate("subspecies." + this.name + ".name") : "";
   }

   public boolean canSpawn(LivingEntity entityLiving) {
      if (entityLiving != null) {
         Level world = entityLiving.level();

         for (SpawnCondition condition : this.spawnConditions) {
            if (!condition.isMet(world, null, entityLiving.blockPosition())) {
               return false;
            }
         }
      }

      return true;
   }

   public Variant getRandomVariant(LivingEntity entity, boolean rare) {
      LycanitesMobs.logDebug("Subspecies", "~0===== Variant =====0~");
      LycanitesMobs.logDebug("Subspecies", "Selecting random variant for: " + entity);
      if (rare) {
         LycanitesMobs.logDebug("Subspecies", "The conditions have been set to rare increasing the chances of a variant being picked.");
      }

      if (this.variants.isEmpty()) {
         LycanitesMobs.logDebug("Subspecies", "No variants available, will be base variant.");
         return null;
      } else {
         LycanitesMobs.logDebug("Subspecies", "Variants Available: " + this.variants.size());
         int baseSpeciesWeightScaled = Variant.BASE_WEIGHT;
         if (rare) {
            baseSpeciesWeightScaled = Math.round((float)baseSpeciesWeightScaled / 4.0F);
         }

         int totalWeight = baseSpeciesWeightScaled;

         for (Variant variant : this.variants.values()) {
            totalWeight += variant.weight;
         }

         LycanitesMobs.logDebug("Subspecies", "Total Weight: " + totalWeight);
         int roll = entity.getRandom().nextInt(totalWeight) + 1;
         LycanitesMobs.logDebug("Subspecies", "Rolled: " + roll);
         if (roll <= baseSpeciesWeightScaled) {
            LycanitesMobs.logDebug("Subspecies", "Base variant selected: " + baseSpeciesWeightScaled);
            return null;
         } else {
            int checkWeight = baseSpeciesWeightScaled;

            for (Variant variant : this.variants.values()) {
               checkWeight += variant.weight;
               if (roll <= checkWeight) {
                  LycanitesMobs.logDebug("Subspecies", "Variant selected: " + variant.toString());
                  return variant;
               }
            }

            LycanitesMobs.logWarning("", "The roll was higher than the Total Weight, this shouldn't happen.");
            return null;
         }
      }
   }

   public Variant getVariant(int index) {
      return !this.variants.containsKey(index) ? null : this.variants.get(index);
   }

   public Variant getChildVariant(LivingEntity entity, Variant hostVariant, Variant partnerVariant) {
      if (hostVariant == null) {
         boolean var10000 = false;
      }

      if (partnerVariant == null) {
         boolean var9 = false;
      }

      if (hostVariant == partnerVariant) {
         return hostVariant;
      } else {
         int hostWeight = hostVariant != null ? hostVariant.weight : Variant.BASE_WEIGHT;
         int partnerWeight = partnerVariant != null ? partnerVariant.weight : Variant.BASE_WEIGHT;
         int roll = entity.getRandom().nextInt(hostWeight + partnerWeight);
         return roll > hostWeight ? partnerVariant : hostVariant;
      }
   }

   @Override
   public String toString() {
      return this.name != null ? this.name : "normal";
   }

   public static int getIndexFromOld(int oldIndex) {
      return oldIndex > 3 ? 1 : 0;
   }
}
