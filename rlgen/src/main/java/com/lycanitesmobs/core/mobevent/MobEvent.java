package com.lycanitesmobs.core.mobevent;

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
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.mobevent.effects.MobEventEffect;
import com.lycanitesmobs.core.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MobEvent {
   protected List<SpawnCondition> conditions = new ArrayList<>();
   protected List<MobEventTrigger> triggers = new ArrayList<>();
   protected List<MobEventEffect> effects = new ArrayList<>();
   public String name = "mobevent";
   public String title = "mobevent";
   protected boolean enabled = true;
   public String channel = "world";
   public int duration = 1200;
   protected int conditionsRequired = 0;
   protected boolean interuptWorldEvents = false;
   protected int levelBoostMin = 0;
   protected int levelBoostMax = 9;

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();
      this.title = this.name;
      if (json.has("title")) {
         this.title = json.get("title").getAsString();
      }

      if (json.has("enabled")) {
         this.enabled = json.get("enabled").getAsBoolean();
      }

      if (json.has("channel")) {
         this.channel = json.get("channel").getAsString();
      }

      if (json.has("duration")) {
         this.duration = json.get("duration").getAsInt();
      }

      if (json.has("conditionsRequired")) {
         this.conditionsRequired = json.get("conditionsRequired").getAsInt();
      }

      if (json.has("interuptWorldEvents")) {
         this.interuptWorldEvents = json.get("interuptWorldEvents").getAsBoolean();
      }

      if (json.has("levelBoostMin")) {
         this.levelBoostMin = json.get("levelBoostMin").getAsInt();
      }

      if (json.has("levelBoostMax")) {
         this.levelBoostMax = json.get("levelBoostMax").getAsInt();
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

      if (json.has("triggers")) {
         JsonArray jsonArray = json.get("triggers").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
            MobEventTrigger mobEventTrigger = MobEventTrigger.createFromJSON(triggerJson, this);
            this.triggers.add(mobEventTrigger);
            if (this.enabled) {
               MobEventListener.getInstance().addTrigger(mobEventTrigger);
            }
         }
      }

      if (json.has("effects")) {
         JsonArray jsonArray = json.get("effects").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject effectJson = jsonIterator.next().getAsJsonObject();
            MobEventEffect mobEventEffect = MobEventEffect.createFromJSON(effectJson);
            this.effects.add(mobEventEffect);
         }
      }
   }

   public void destroy() {
      for (MobEventTrigger mobEventTrigger : this.triggers) {
         MobEventListener.getInstance().removeTrigger(mobEventTrigger);
      }

      MobEventManager.getInstance().removeMobEvent(this);
   }

   public String getTitle() {
      return LanguageManager.translate("mobevent." + this.title + ".name");
   }

   public String getDisplayTitle() {
      String title = this.getTitle().replaceAll(" ", "").toLowerCase();
      return title.equalsIgnoreCase(this.title) ? "" : title;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean canStart(Level world, Player player) {
      if (world == null) {
         return false;
      } else if (this.enabled && MobEventManager.getInstance().mobEventsEnabled) {
         ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
         if (worldExt == null) {
            return false;
         } else if ("world".equalsIgnoreCase(this.channel) && worldExt.getWorldEvent() != null && !this.interuptWorldEvents) {
            return false;
         } else {
            if (!MobEventManager.getInstance().globalEventConditions.isEmpty()) {
               LycanitesMobs.logDebug("MobEvents", "Global Conditions Required: " + MobEventManager.getInstance().globalEventConditions.size());

               for (SpawnCondition condition : MobEventManager.getInstance().globalEventConditions) {
                  if (!condition.isMet(world, player, player != null ? player.blockPosition() : BlockPos.ZERO)) {
                     LycanitesMobs.logDebug("MobEvents", "Global Condition: " + condition + "Failed");
                     return false;
                  }
               }
            }

            if (this.conditions.isEmpty()) {
               LycanitesMobs.logDebug("MobEvents", "No Conditions");
               return true;
            } else {
               LycanitesMobs.logDebug("MobEvents", "Conditions Required: " + (this.conditionsRequired > 0 ? this.conditionsRequired : "All"));
               int conditionsMet = 0;
               int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();

               for (SpawnCondition conditionx : this.conditions) {
                  BlockPos position = null;
                  if (player != null) {
                     position = player.blockPosition();
                  }

                  boolean met = conditionx.isMet(world, player, position);
                  LycanitesMobs.logDebug("MobEvents", "Condition: " + conditionx + " " + (met ? "Passed" : "Failed"));
                  if (met) {
                     if (++conditionsMet >= conditionsRequired) {
                        LycanitesMobs.logDebug("MobEvents", "Sufficient Conditions Met");
                        return true;
                     }
                  }
               }

               LycanitesMobs.logDebug("MobEvents", "Insufficient Conditions Met: " + conditionsMet + "/" + conditionsRequired);
               return false;
            }
         }
      } else {
         LycanitesMobs.logDebug("MobEvents", "Mob Events System Disabled");
         return false;
      }
   }

   public boolean trigger(Level world, Player player, BlockPos pos, int level, int variant) {
      LycanitesMobs.logDebug("MobEvents", "~O==================== Mob Event Triggered: " + this.name + " ====================O~");
      ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
      if (worldExt == null) {
         return false;
      } else {
         if ("world".equalsIgnoreCase(this.channel)) {
            worldExt.startWorldEvent(this);
         } else if (pos != null) {
            worldExt.startMobEvent(this, player, pos, level, variant);
         }

         return true;
      }
   }

   public void onUpdate(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
      for (MobEventEffect mobEventEffect : this.effects) {
         mobEventEffect.onUpdate(world, player, pos, level, ticks, variant);
      }
   }

   public void onSpawn(Mob entity, Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
      for (MobEventEffect mobEventEffect : this.effects) {
         mobEventEffect.onSpawn(entity, world, player, pos, level, ticks);
      }

      if (entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
         entityCreature.onFirstSpawn();
         int levelBoost = this.levelBoostMin;
         if (this.levelBoostMax > this.levelBoostMin) {
            levelBoost += entityCreature.getRandom().nextInt(this.levelBoostMax - this.levelBoostMin + 1);
         }

         entityCreature.addLevel(levelBoost);
         if (!entityCreature.isTemporary && !"boss".equalsIgnoreCase(this.channel)) {
            entityCreature.setTemporary(MobEventManager.getInstance().defaultMobDuration);
         }

         if (variant >= 0) {
            entityCreature.applyVariant(variant);
         }
      }
   }

   public MobEventPlayerServer getServerEventPlayer(Level world) {
      return new MobEventPlayerServer(this, world);
   }

   public MobEventPlayerClient getClientEventPlayer(Level world) {
      return new MobEventPlayerClient(this, world);
   }
}
