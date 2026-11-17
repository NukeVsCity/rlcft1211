package com.lycanitesmobs.core.mobevent.trigger;

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
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class MobEventTrigger {
   public MobEvent mobEvent;
   public List<SpawnCondition> conditions = new ArrayList<>();
   public int conditionsRequired = 0;
   public int subspecies = -1;

   public static MobEventTrigger createFromJSON(JsonObject json, MobEvent mobEvent) {
      String type = json.get("type").getAsString();
      MobEventTrigger mobEventTrigger = null;
      if ("random".equalsIgnoreCase(type)) {
         mobEventTrigger = new RandomMobEventTrigger(mobEvent);
      } else if ("tick".equalsIgnoreCase(type)) {
         mobEventTrigger = new TickMobEventTrigger(mobEvent);
      } else if ("altar".equalsIgnoreCase(type)) {
         mobEventTrigger = new AltarMobEventTrigger(mobEvent);
      }

      mobEventTrigger.loadFromJSON(json);
      return mobEventTrigger;
   }

   public MobEventTrigger(MobEvent mobEvent) {
      this.mobEvent = mobEvent;
   }

   public void loadFromJSON(JsonObject json) {
      if (json.has("conditionsRequired")) {
         this.conditionsRequired = json.get("conditionsRequired").getAsInt();
      }

      if (json.has("subspecies")) {
         this.subspecies = json.get("subspecies").getAsInt();
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
   }

   public boolean canTrigger(Level world, Player player) {
      return !this.mobEvent.canStart(world, player) ? false : this.triggerConditionsMet(world, player);
   }

   public boolean triggerConditionsMet(Level world, Player player) {
      if (this.conditions.size() == 0) {
         return true;
      } else {
         int conditionsMet = 0;
         int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();

         for (SpawnCondition condition : this.conditions) {
            BlockPos position = null;
            if (player != null) {
               position = player.blockPosition();
            }

            boolean met = condition.isMet(world, player, position);
            if (met) {
               if (++conditionsMet >= conditionsRequired) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean trigger(Level world, Player player, BlockPos pos, int level, int variant) {
      LycanitesMobs.logDebug("MobEvents", "Trigger Fired: " + this + " for: " + this.mobEvent.name + " channel: " + this.mobEvent.channel);
      return this.mobEvent.trigger(world, player, pos, level, variant);
   }
}
