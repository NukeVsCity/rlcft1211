package com.lycanitesmobs.core.spawner.trigger;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.spawner.Spawner;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public abstract class SpawnTrigger {
   public Spawner spawner;
   public int count = 1;
   public double chance = 1.0;
   public List<SpawnCondition> conditions = new ArrayList<>();
   public int conditionsRequired = 0;
   public int cooldown = 0;
   protected Map<Player, Long> playerUsedTicks = new HashMap<>();

   public static SpawnTrigger createFromJSON(JsonObject json, Spawner spawner) {
      String type = json.get("type").getAsString();
      SpawnTrigger spawnTrigger = null;
      if ("world".equalsIgnoreCase(type)) {
         spawnTrigger = new WorldSpawnTrigger(spawner);
      } else if ("player".equalsIgnoreCase(type) || "tick".equalsIgnoreCase(type)) {
         spawnTrigger = new PlayerSpawnTrigger(spawner);
      } else if ("entitySpawned".equalsIgnoreCase(type)) {
         spawnTrigger = new EntitySpawnedSpawnTrigger(spawner);
      } else if ("kill".equalsIgnoreCase(type)) {
         spawnTrigger = new KillSpawnTrigger(spawner);
      } else if ("chunk".equalsIgnoreCase(type)) {
         spawnTrigger = new ChunkSpawnTrigger(spawner);
      } else if ("block".equalsIgnoreCase(type)) {
         spawnTrigger = new BlockSpawnTrigger(spawner);
      } else if ("ore".equalsIgnoreCase(type)) {
         spawnTrigger = new OreBlockSpawnTrigger(spawner);
      } else if ("crop".equalsIgnoreCase(type)) {
         spawnTrigger = new CropBlockSpawnTrigger(spawner);
      } else if ("tree".equalsIgnoreCase(type)) {
         spawnTrigger = new TreeBlockSpawnTrigger(spawner);
      } else if ("sleep".equalsIgnoreCase(type)) {
         spawnTrigger = new SleepSpawnTrigger(spawner);
      } else if ("fishing".equalsIgnoreCase(type)) {
         spawnTrigger = new FishingSpawnTrigger(spawner);
      } else if ("explosion".equalsIgnoreCase(type)) {
         spawnTrigger = new ExplosionSpawnTrigger(spawner);
      } else if ("mobEvent".equalsIgnoreCase(type)) {
         spawnTrigger = new MobEventSpawnTrigger(spawner);
      } else if ("mix".equalsIgnoreCase(type)) {
         spawnTrigger = new MixBlockSpawnTrigger(spawner);
      }

      spawnTrigger.loadFromJSON(json);
      return spawnTrigger;
   }

   public SpawnTrigger(Spawner spawner) {
      this.spawner = spawner;
   }

   public void loadFromJSON(JsonObject json) {
      if (json.has("count")) {
         this.count = json.get("count").getAsInt();
      }

      if (json.has("chance")) {
         this.chance = json.get("chance").getAsDouble();
      }

      if (json.has("conditionsRequired")) {
         this.conditionsRequired = json.get("conditionsRequired").getAsInt();
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

      if (json.has("cooldown")) {
         this.cooldown = json.get("cooldown").getAsInt();
      }
   }

   public boolean trigger(Level world, Player player, BlockPos triggerPos, int level, int chain) {
      ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
      if (this.cooldown > 0 && playerExt != null) {
         long lastUsedTicks = 0L;
         if (!this.playerUsedTicks.containsKey(player)) {
            this.playerUsedTicks.put(player, lastUsedTicks);
         } else {
            lastUsedTicks = this.playerUsedTicks.get(player);
         }

         if (playerExt.timePlayed < lastUsedTicks + (long)this.cooldown) {
            return false;
         }
      }

      if (!this.triggerConditionsMet(world, player, triggerPos)) {
         return false;
      } else {
         if ("".equals(this.spawner.eventName)) {
            LycanitesMobs.logDebug("JSONSpawner", "Trigger Fired: " + this + " for: " + this.spawner.name);
         }

         return this.spawner.trigger(world, player, this, triggerPos, level, this.count, chain);
      }
   }

   public boolean triggerConditionsMet(Level world, Player player, BlockPos triggerPos) {
      if (this.conditions.size() == 0) {
         return true;
      } else {
         int conditionsMet = 0;
         int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();

         for (SpawnCondition condition : this.conditions) {
            boolean met = condition.isMet(world, player, triggerPos);
            if (met) {
               if (++conditionsMet >= conditionsRequired) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void applyToEntity(Mob entityLiving) {
   }
}
