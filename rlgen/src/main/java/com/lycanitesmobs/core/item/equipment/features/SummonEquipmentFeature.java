package com.lycanitesmobs.core.item.equipment.features;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;


public class SummonEquipmentFeature extends EquipmentFeature {
   public String summonMobId;
   public double summonChance = 0.05;
   public int summonDuration = 60;
   public int summonCountMin = 1;
   public int summonCountMax = 1;
   public double sizeScale = 1.0;

   @Override
   public void loadFromJSON(JsonObject json) {
      super.loadFromJSON(json);
      this.summonMobId = json.get("summonMobId").getAsString();
      if (json.has("summonChance")) {
         this.summonChance = json.get("summonChance").getAsDouble();
      }

      if (json.has("summonDuration")) {
         this.summonDuration = json.get("summonDuration").getAsInt();
      }

      if (json.has("summonCountMin")) {
         this.summonCountMin = json.get("summonCountMin").getAsInt();
      }

      if (json.has("summonCountMax")) {
         this.summonCountMax = json.get("summonCountMax").getAsInt();
      }

      if (json.has("sizeScale")) {
         this.sizeScale = json.get("sizeScale").getAsDouble();
      }
   }

   @Override
   public String getDescription(ItemStack itemStack, int level) {
      if (!this.isActive(itemStack, level)) {
         return null;
      } else {
         String description = LanguageManager.translate("equipment.feature." + this.featureType)
            + " "
            + LanguageManager.translate("entity." + this.summonMobId.replace("lycanitesmobs.", "") + ".name");
         if (this.summonCountMin != this.summonCountMax) {
            description = description + " x" + this.summonCountMin + " - " + this.summonCountMax;
         } else {
            description = description + " x" + this.summonCountMax;
         }

         description = description + " " + Math.round(this.summonChance * 100.0) + "%";
         if (this.summonDuration > 0) {
            description = description + " " + (float)this.summonDuration / 20.0F + "s";
         }

         return description;
      }
   }

   @Override
   public String getSummary(ItemStack itemStack, int level) {
      return !this.isActive(itemStack, level) ? null : LanguageManager.translate("entity." + this.summonMobId.replace("lycanitesmobs.", "") + ".name");
   }

   public boolean onHitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
      if (target != null && attacker != null && !attacker.level().isClientSide) {
         int summonCount = this.summonCountMin;
         if (this.summonCountMax > this.summonCountMin) {
            summonCount = this.summonCountMin + attacker.getRandom().nextInt(this.summonCountMax - this.summonCountMin);
         }

         int summonedCreatures = 0;

         for (int i = 0; i < summonCount; i++) {
            if (attacker.getRandom().nextDouble() <= this.summonChance) {
               try {
                  Class entityClass = null;
                  CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(this.summonMobId);
                  if (creatureInfo != null) {
                     entityClass = creatureInfo.entityClass;
                  } else {
                     EntityEntry entry = (EntityEntry)ForgeRegistries.ENTITIES.getValue(ResourceLocation.parse(this.summonMobId));
                     if (entry != null) {
                        entityClass = entry.getEntityClass();
                     }
                  }

                  if (entityClass != null) {
                     Mob entity = (Mob)entityClass.getConstructor(Level.class).newInstance(attacker.level());
                     if (entity instanceof BaseCreatureEntity) {
                        BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
                        entityCreature.setMinion(true);
                        entityCreature.setTemporary(this.summonDuration * 20);
                        entityCreature.setSizeScale(this.sizeScale);
                        if (attacker instanceof Player && entityCreature instanceof TameableCreatureEntity) {
                           TameableCreatureEntity entityTameable = (TameableCreatureEntity)entityCreature;
                           entityTameable.setPlayerOwner((Player)attacker);
                           entityTameable.setSitting(false);
                           entityTameable.setFollowing(true);
                           entityTameable.setPassive(false);
                           entityTameable.setAssist(true);
                           entityTameable.setAggressive(true);
                           entityTameable.setPVP(target instanceof Player);
                        }

                        float randomAngle = 45.0F + 45.0F * attacker.getRandom().nextFloat();
                        if (attacker.getRandom().nextBoolean()) {
                           randomAngle = -randomAngle;
                        }

                        BlockPos spawnPos = entityCreature.getFacingPosition(attacker, -1.0, (double)randomAngle);
                        entity.moveTo(
                           (double)spawnPos.getX(), (double)spawnPos.getY(), (double)spawnPos.getZ(), attacker.getYRot(), 0.0F
                        );
                        attacker.level().addFreshEntity(entity);
                        entityCreature.setTarget(target);
                        summonedCreatures++;
                     }
                  }
               } catch (Exception var13) {
                  var13.printStackTrace();
               }
            }
         }

         return summonedCreatures > 0;
      } else {
         return false;
      }
   }
}
