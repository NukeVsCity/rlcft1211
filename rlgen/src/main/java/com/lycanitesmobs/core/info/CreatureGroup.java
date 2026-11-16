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
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CreatureGroup {
   public List<CreatureInfo> creatures = new ArrayList<>();
   public String name;
   public List<CreatureGroup> huntGroups = new ArrayList<>();
   public List<String> huntGroupNames = new ArrayList<>();
   public List<CreatureGroup> packGroups = new ArrayList<>();
   public List<String> packGroupNames = new ArrayList<>();
   public List<CreatureGroup> waryGroups = new ArrayList<>();
   public List<String> waryGroupNames = new ArrayList<>();
   public List<CreatureGroup> fleeGroups = new ArrayList<>();
   public List<String> fleeGroupNames = new ArrayList<>();
   public List<CreatureGroup> ignoreGroups = new ArrayList<>();
   public List<String> ignoreGroupNames = new ArrayList<>();
   public CreatureGroup.Interaction defaultInteraction = CreatureGroup.Interaction.HUNT;
   public boolean animals = false;
   public boolean humanoids = false;
   public boolean raiders = false;
   public boolean frozen = false;
   public boolean inferno = false;
   public List<String> entityIds = new ArrayList<>();

   public void loadFromJson(JsonObject json) {
      this.name = json.get("name").getAsString();
      if (json.has("hunt")) {
         this.huntGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("hunt"));
      }

      if (json.has("pack")) {
         this.packGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("pack"));
      }

      if (json.has("wary")) {
         this.waryGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("wary"));
      }

      if (json.has("flee")) {
         this.fleeGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("flee"));
      }

      if (json.has("ignore")) {
         this.ignoreGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("ignore"));
      }

      if (json.has("default")) {
         this.defaultInteraction = CreatureGroup.Interaction.valueOf(json.get("default").getAsString().toUpperCase(Locale.ENGLISH));
      }

      if (json.has("animals")) {
         this.animals = json.get("animals").getAsBoolean();
      }

      if (json.has("humanoids")) {
         this.humanoids = json.get("humanoids").getAsBoolean();
      }

      if (json.has("raiders")) {
         this.raiders = json.get("raiders").getAsBoolean();
      }

      if (json.has("frozen")) {
         this.frozen = json.get("frozen").getAsBoolean();
      }

      if (json.has("inferno")) {
         this.inferno = json.get("inferno").getAsBoolean();
      }

      if (json.has("entityIds")) {
         this.entityIds = JSONHelper.getJsonStrings(json.get("entityIds").getAsJsonArray());
      }
   }

   public void init() {
      for (String groupName : this.huntGroupNames) {
         CreatureGroup group = CreatureManager.getInstance().getCreatureGroup(groupName);
         if (groupName.equals(this.name)) {
            group = this;
         }

         if (group != null) {
            this.huntGroups.add(group);
         }
      }

      for (String groupName : this.packGroupNames) {
         CreatureGroup groupx = CreatureManager.getInstance().getCreatureGroup(groupName);
         if (groupName.equals(this.name)) {
            groupx = this;
         }

         if (groupx != null) {
            this.packGroups.add(groupx);
         }
      }

      for (String groupName : this.waryGroupNames) {
         CreatureGroup groupxx = CreatureManager.getInstance().getCreatureGroup(groupName);
         if (groupName.equals(this.name)) {
            groupxx = this;
         }

         if (groupxx != null) {
            this.waryGroups.add(groupxx);
         }
      }

      for (String groupName : this.fleeGroupNames) {
         CreatureGroup groupxxx = CreatureManager.getInstance().getCreatureGroup(groupName);
         if (groupName.equals(this.name)) {
            groupxxx = this;
         }

         if (groupxxx != null) {
            this.fleeGroups.add(groupxxx);
         }
      }

      for (String groupName : this.ignoreGroupNames) {
         CreatureGroup groupxxxx = CreatureManager.getInstance().getCreatureGroup(groupName);
         if (groupName.equals(this.name)) {
            groupxxxx = this;
         }

         if (groupxxxx != null) {
            this.ignoreGroups.add(groupxxxx);
         }
      }

      LycanitesMobs.logDebug("Creature Group", "Loaded Creature Group: " + this.getName());
   }

   public String getName() {
      return this.name;
   }

   public void addCreature(CreatureInfo creature) {
      if (!this.creatures.contains(creature)) {
         this.creatures.add(creature);
      }
   }

   public boolean hasEntity(Entity entity) {
      if (entity instanceof BaseCreatureEntity) {
         return ((BaseCreatureEntity)entity).creatureInfo.groups.contains(this);
      } else if (!(entity instanceof LivingEntity)) {
         return false;
      } else if (this.animals && entity instanceof Animal) {
         return true;
      } else if (!this.humanoids
         || !(entity instanceof Player)
            && !(entity instanceof Villager)
            && !(entity instanceof AbstractIllager)
            && !(entity instanceof Witch)) {
         if (!this.raiders || !(entity instanceof AbstractIllager) && !(entity instanceof Witch)) {
            if (this.frozen && entity instanceof SnowGolem) {
               return true;
            } else if (!this.inferno || !(entity instanceof Blaze) && !(entity instanceof MagmaCube)) {
               ResourceLocation entityResourceLocation = EntityList.getKey(entity);
               if (entityResourceLocation == null) {
                  return false;
               } else {
                  String entityId = entityResourceLocation.toString();
                  return this.entityIds.contains(entityId);
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public boolean shouldRevenge(Entity entity) {
      for (CreatureGroup group : this.ignoreGroups) {
         if (group.hasEntity(entity)) {
            return false;
         }
      }

      for (CreatureGroup groupx : this.waryGroups) {
         if (groupx.hasEntity(entity)) {
            return false;
         }
      }

      for (CreatureGroup groupxx : this.fleeGroups) {
         if (groupxx.hasEntity(entity)) {
            return false;
         }
      }

      return this.defaultInteraction == CreatureGroup.Interaction.HUNT || this.defaultInteraction == CreatureGroup.Interaction.RETALIATE;
   }

   public boolean shouldHunt(Entity entity) {
      for (CreatureGroup group : this.ignoreGroups) {
         if (group.hasEntity(entity)) {
            return false;
         }
      }

      for (CreatureGroup groupx : this.huntGroups) {
         if (groupx.hasEntity(entity)) {
            return true;
         }
      }

      return this.defaultInteraction == CreatureGroup.Interaction.HUNT;
   }

   public boolean shouldPackHunt(Entity entity) {
      for (CreatureGroup group : this.ignoreGroups) {
         if (group.hasEntity(entity)) {
            return false;
         }
      }

      for (CreatureGroup groupx : this.packGroups) {
         if (groupx.hasEntity(entity)) {
            return true;
         }
      }

      return this.defaultInteraction == CreatureGroup.Interaction.PACKHUNT;
   }

   public boolean shouldFlee(Entity entity) {
      for (CreatureGroup group : this.fleeGroups) {
         if (group.hasEntity(entity)) {
            return true;
         }
      }

      return this.defaultInteraction == CreatureGroup.Interaction.FLEE;
   }

   static enum Interaction {
      HUNT("hunt"),
      PACKHUNT("pack"),
      WARY("wary"),
      FLEE("flee"),
      IGNORE("ignore"),
      RETALIATE("retaliate");

      public final String name;

      private Interaction(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }
   }
}
