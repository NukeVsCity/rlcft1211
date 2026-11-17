package com.lycanitesmobs.core.spawner.condition;

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
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.util.ArrayList;
import java.util.List;


public class PlayerSpawnCondition extends SpawnCondition {
   public String username = "";
   public float difficultyMin = -1.0F;
   public float difficultyMax = -1.0F;
   public float spawnDistanceMin = -1.0F;
   public float spawnDistanceMax = -1.0F;
   public BlockPos position = null;
   public float positionDistanceMin = -1.0F;
   public float positionDistanceMax = -1.0F;
   public int levelMin = -1;
   public int levelMax = -1;
   public int timeMin = -1;
   public int timeMax = -1;
   public int lightLevelMin = -1;
   public int lightLevelMax = -1;
   public boolean grounded = false;
   public boolean notGrounded = false;
   public boolean inWater = false;
   public boolean notInWater = false;
   public List<Item> heldItems = new ArrayList<>();
   public String heldItemsListType = "blacklist";

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("username")) {
         this.username = json.get("username").getAsString();
      }

      if (json.has("difficultyMin")) {
         this.difficultyMin = json.get("difficultyMin").getAsFloat();
      }

      if (json.has("difficultyMax")) {
         this.difficultyMax = json.get("difficultyMax").getAsFloat();
      }

      if (json.has("spawnDistanceMin")) {
         this.spawnDistanceMin = json.get("spawnDistanceMin").getAsFloat();
      }

      if (json.has("spawnDistanceMax")) {
         this.spawnDistanceMax = json.get("spawnDistanceMax").getAsFloat();
      }

      if (json.has("position")) {
         this.position = LycPos.of(JSONHelper.getVec3i(json, "position"));
      }

      if (json.has("positionDistanceMin")) {
         this.positionDistanceMin = json.get("positionDistanceMin").getAsFloat();
      }

      if (json.has("positionDistanceMax")) {
         this.positionDistanceMax = json.get("positionDistanceMax").getAsFloat();
      }

      if (json.has("levelMin")) {
         this.levelMin = json.get("levelMin").getAsInt();
      }

      if (json.has("levelMax")) {
         this.levelMax = json.get("levelMax").getAsInt();
      }

      if (json.has("timeMin")) {
         this.timeMin = json.get("timeMin").getAsInt();
      }

      if (json.has("timeMax")) {
         this.timeMax = json.get("timeMax").getAsInt();
      }

      if (json.has("lightLevelMin")) {
         this.lightLevelMin = json.get("lightLevelMin").getAsInt();
      }

      if (json.has("lightLevelMax")) {
         this.lightLevelMax = json.get("lightLevelMax").getAsInt();
      }

      if (json.has("grounded")) {
         this.grounded = json.get("grounded").getAsBoolean();
      }

      if (json.has("notGrounded")) {
         this.notGrounded = json.get("notGrounded").getAsBoolean();
      }

      if (json.has("inWater")) {
         this.inWater = json.get("inWater").getAsBoolean();
      }

      if (json.has("notInWater")) {
         this.notInWater = json.get("notInWater").getAsBoolean();
      }

      if (json.has("heldItems")) {
         this.heldItems = JSONHelper.getJsonItems(json.get("heldItems").getAsJsonArray());
      }

      if (json.has("heldItemsListType")) {
         this.heldItemsListType = json.get("heldItemsListType").getAsString();
      }

      super.loadFromJSON(json);
   }

   @Override
   public boolean isMet(Level world, Player player, BlockPos position) {
      if (player == null) {
         return false;
      } else {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         if (!"".equals(this.username) && !this.username.equalsIgnoreCase(player.getName().getString())) {
            return false;
         } else {
            if (this.difficultyMin >= 0.0F || this.difficultyMax >= 0.0F) {
               DifficultyInstance difficultyInstance = world.getCurrentDifficultyAt(player.blockPosition());
               float difficulty = difficultyInstance.getEffectiveDifficulty();
               if (this.difficultyMin >= 0.0F && difficulty < this.difficultyMin) {
                  return false;
               }

               if (this.difficultyMax >= 0.0F && difficulty > this.difficultyMax) {
                  return false;
               }
            }

            double spawnDistance = Math.sqrt(player.blockPosition().distSqr(world.getSharedSpawnPos()));
            if (this.spawnDistanceMin >= 0.0F && spawnDistance < (double)this.spawnDistanceMin) {
               return false;
            } else if (this.spawnDistanceMax >= 0.0F && spawnDistance > (double)this.spawnDistanceMax) {
               return false;
            } else {
               if (this.position != null) {
                  double positionDistance = LycMath.dist(player.blockPosition()
                     , this.position.getX(), this.position.getY(), this.position.getZ());
                  if (this.positionDistanceMin >= 0.0F && positionDistance < (double)this.positionDistanceMin) {
                     return false;
                  }

                  if (this.positionDistanceMax >= 0.0F && positionDistance > (double)this.positionDistanceMax) {
                     return false;
                  }
               }

               if (this.levelMin >= 0 && player.experienceLevel < this.levelMin) {
                  return false;
               } else if (this.levelMax >= 0 && player.experienceLevel > this.levelMax) {
                  return false;
               } else if (this.grounded && !player.onGround()) {
                  return false;
               } else if (this.notGrounded && player.onGround()) {
                  return false;
               } else if (this.inWater && !player.isInWater()) {
                  return false;
               } else if (this.notInWater && player.isInWater()) {
                  return false;
               } else {
                  if (playerExt != null) {
                     if (this.timeMin >= 0 && playerExt.timePlayed < (long)this.timeMin) {
                        return false;
                     }

                     if (this.timeMax >= 0 && playerExt.timePlayed > (long)this.timeMax) {
                        return false;
                     }
                  }

                  int lightLevel = world.getRawBrightness(player.blockPosition(), 0); // 1.12 World.getLight
                  if (this.lightLevelMin >= 0 && lightLevel < this.lightLevelMin) {
                     return false;
                  } else if (this.lightLevelMax >= 0 && lightLevel > this.lightLevelMax) {
                     return false;
                  } else {
                     if (this.heldItems.size() > 0) {
                        boolean holdingItem = false;
                        if (!player.getMainHandItem().isEmpty() && this.heldItems.contains(player.getMainHandItem().getItem())) {
                           holdingItem = true;
                           if ("blacklist".equalsIgnoreCase(this.heldItemsListType)) {
                              return false;
                           }
                        }

                        if (!player.getOffhandItem().isEmpty() && this.heldItems.contains(player.getOffhandItem().getItem())) {
                           holdingItem = true;
                           if ("blacklist".equalsIgnoreCase(this.heldItemsListType)) {
                              return false;
                           }
                        }

                        if (!holdingItem && "whitelist".equalsIgnoreCase(this.heldItemsListType)) {
                           return false;
                        }
                     } else if ("whitelist".equalsIgnoreCase(this.heldItemsListType)) {
                        return false;
                     }

                     return super.isMet(world, player, position);
                  }
               }
            }
         }
      }
   }
}
