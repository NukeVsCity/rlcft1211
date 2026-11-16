package com.lycanitesmobs.core.spawner.condition;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class WorldSpawnCondition extends SpawnCondition {
   public int[] dimensionIds;
   public String dimensionListType = "whitelist";
   public String biomeTagListType = "whitelist";
   public List<String> biomeTags = new ArrayList<>();
   public List<Biome> biomesFromTags = null;
   public String biomeIdListType = "whitelist";
   public List<String> biomeIds = new ArrayList<>();
   public List<Biome> biomes = new ArrayList<>();
   public double worldDayMin = -1.0;
   public double worldDayMax = -1.0;
   public double worldDayN = -1.0;
   public int dayTimeMin = -1;
   public int dayTimeMax = -1;
   public String weather = "any";
   public short difficultyMin = -1;
   public short difficultyMax = -1;
   public int moonPhase = -1;

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("dimensionIds")) {
         JsonArray jsonArray = json.get("dimensionIds").getAsJsonArray();
         this.dimensionIds = new int[jsonArray.size()];
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         for (int i = 0; jsonIterator.hasNext(); i++) {
            this.dimensionIds[i] = jsonIterator.next().getAsInt();
         }
      }

      if (json.has("dimensionListType")) {
         this.dimensionListType = json.get("dimensionListType").getAsString();
      }

      if (json.has("biomeTags")) {
         this.biomeTags.clear();
         this.biomesFromTags = null;
         this.biomeTags = JSONHelper.getJsonStrings(json.get("biomeTags").getAsJsonArray());
      } else if (json.has("biomes")) {
         this.biomeTags.clear();
         this.biomesFromTags = null;
         this.biomeTags = JSONHelper.getJsonStrings(json.get("biomes").getAsJsonArray());
      }

      if (json.has("biomeTagListType")) {
         this.biomeTagListType = json.get("biomeTagListType").getAsString();
      }

      if (json.has("biomeIds")) {
         this.biomeIds.clear();
         this.biomes = null;
         this.biomeIds = JSONHelper.getJsonStrings(json.get("biomeIds").getAsJsonArray());
      }

      if (json.has("biomeIdListType")) {
         this.biomeIdListType = json.get("biomeIdListType").getAsString();
      }

      if (json.has("worldDayMin")) {
         this.worldDayMin = (double)json.get("worldDayMin").getAsInt();
      }

      if (json.has("worldDayMax")) {
         this.worldDayMax = (double)json.get("worldDayMax").getAsInt();
      }

      if (json.has("worldDayN")) {
         this.worldDayN = (double)json.get("worldDayN").getAsInt();
      }

      if (json.has("dayTimeMin")) {
         this.dayTimeMin = json.get("dayTimeMin").getAsInt();
      }

      if (json.has("dayTimeMax")) {
         this.dayTimeMax = json.get("dayTimeMax").getAsInt();
      }

      if (json.has("weather")) {
         this.weather = json.get("weather").getAsString();
      }

      if (json.has("difficultyMin")) {
         this.difficultyMin = json.get("difficultyMin").getAsShort();
      }

      if (json.has("difficultyMax")) {
         this.difficultyMax = json.get("difficultyMax").getAsShort();
      }

      if (json.has("moonPhase")) {
         this.moonPhase = json.get("moonPhase").getAsInt();
      }

      super.loadFromJSON(json);
   }

   @Override
   public boolean isMet(Level world, Player player, BlockPos position) {
      ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
      int time = (int)Math.floor((double)world.getDayTime() % 24000.0);
      int day = (int)Math.floor((double)(worldExt.useTotalWorldTime ? world.getGameTime() : world.getDayTime()) / 23999.0);
      if (this.dimensionIds != null && this.dimensionIds.length > 0) {
         boolean dimensionIdFound = false;

         for (int dimensionId : this.dimensionIds) {
            if (LycDim.id(world) == dimensionId) {
               dimensionIdFound = true;
               break;
            }
         }

         if (!dimensionIdFound && "whitelist".equalsIgnoreCase(this.dimensionListType)) {
            return false;
         }

         if (dimensionIdFound && "blacklist".equalsIgnoreCase(this.dimensionListType)) {
            return false;
         }
      }

      if (this.worldDayMin >= 0.0 && (double)day < this.worldDayMin) {
         return false;
      } else if (this.worldDayMax >= 0.0 && (double)day > this.worldDayMax) {
         return false;
      } else if (!(this.worldDayN >= 0.0) || day != 0 && (double)day % this.worldDayN == 0.0) {
         if (this.dayTimeMin >= 0 && time < this.dayTimeMin) {
            return false;
         } else if (this.dayTimeMax >= 0 && time > this.dayTimeMax) {
            return false;
         } else if (!"clear".equalsIgnoreCase(this.weather) || !world.isRaining() && !world.isThundering()) {
            if (!"rain".equalsIgnoreCase(this.weather) || world.isRaining() && !world.isThundering()) {
               if ("storm".equalsIgnoreCase(this.weather) && !world.isThundering()) {
                  return false;
               } else if (!"rainstorm".equalsIgnoreCase(this.weather) || world.isRaining() && world.isThundering()) {
                  if ("notclear".equalsIgnoreCase(this.weather) && !world.isRaining() && !world.isThundering()) {
                     return false;
                  } else if (this.difficultyMin >= 0 && world.getDifficulty().getId() < this.difficultyMin) {
                     return false;
                  } else if (this.difficultyMax >= 0 && world.getDifficulty().getId() > this.difficultyMax) {
                     return false;
                  } else if (this.moonPhase >= 0 && world.provider.getMoonPhase(world.getDayTime()) != this.moonPhase) {
                     return false;
                  } else {
                     return !this.isAllowedBiome(world, position) ? false : super.isMet(world, player, position);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isAllowedBiome(Level world, BlockPos position) {
      if (position != null) {
         Biome biome = world.getBiome(position).value();
         if (!this.biomeIds.isEmpty()) {
            if (this.biomes == null) {
               this.biomes = JSONHelper.getBiomes(this.biomeIds);
            }

            if (this.biomes.contains(biome)) {
               return !"blacklist".equalsIgnoreCase(this.biomeIdListType);
            }
         }

         if (!this.biomeTags.isEmpty()) {
            if (this.biomesFromTags == null) {
               this.biomesFromTags = JSONHelper.getBiomesFromTags(this.biomeTags);
            }

            if (this.biomesFromTags.contains(biome)) {
               return !"blacklist".equalsIgnoreCase(this.biomeTagListType);
            }
         }
      }

      return this.biomeIds.isEmpty() && this.biomeTags.isEmpty()
         ? true
         : "blacklist".equalsIgnoreCase(this.biomeIdListType) && "blacklist".equalsIgnoreCase(this.biomeTagListType);
   }
}
