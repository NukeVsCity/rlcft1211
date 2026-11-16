package com.lycanitesmobs.core.info.projectile.behaviours;

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
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.network.MessageGUIRequest;
import com.lycanitesmobs.core.pets.SummonSet;


public class ProjectileBehaviourSummon extends ProjectileBehaviour {
   public String summonMobId;
   public boolean summonMinion = false;
   public double summonChance = 0.05;
   public int summonDuration = 60;
   public int summonCountMin = 1;
   public int summonCountMax = 1;
   public double sizeScale = 1.0;

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("summonMobId")) {
         this.summonMobId = json.get("summonMobId").getAsString();
      }

      if (json.has("summonMinion")) {
         this.summonMinion = json.get("summonMinion").getAsBoolean();
      }

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
   public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
      if (projectile != null && !projectile.level().isClientSide) {
         if (!(projectile.getThrower() instanceof Player) || CreatureManager.getInstance().config.isSummoningAllowed(world)) {
            Class entityClass = null;
            SummonSet summonSet = null;
            if (this.summonMinion) {
               if (!(projectile.getThrower() instanceof Player)) {
                  return;
               }

               Player player = (Player)projectile.getThrower();
               ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
               if (extendedPlayer == null) {
                  return;
               }

               summonSet = extendedPlayer.getSelectedSummonSet();
               if (summonSet == null || summonSet.getCreatureClass() == null) {
                  if (player instanceof ServerPlayer) {
                     MessageGUIRequest messageGUIRequest = new MessageGUIRequest(GuiHandler.GuiType.BEASTIARY.id);
                     LycanitesMobs.packetHandler.sendToPlayer(messageGUIRequest, (ServerPlayer)player);
                  }

                  return;
               }

               entityClass = summonSet.getCreatureClass();
            }

            if (entityClass == null && this.summonMobId != null) {
               CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(this.summonMobId);
               if (creatureInfo != null) {
                  entityClass = creatureInfo.entityClass;
               } else {
                  // 1.12 ForgeRegistries.ENTITIES entry class: only Lycanites creatures are ever spawned below
               }
            }

            if (entityClass != null) {
               int summonCount = this.summonCountMin;
               if (this.summonCountMax > this.summonCountMin) {
                  summonCount = this.summonCountMin + projectile.level().random.nextInt(this.summonCountMax - this.summonCountMin);
               }

               for (int i = 0; i < summonCount; i++) {
                  if (projectile.level().random.nextDouble() <= this.summonChance) {
                     try {
                        Mob entity = (Mob)entityClass.getConstructor(Level.class).newInstance(projectile.level());
                        if (entity instanceof BaseCreatureEntity) {
                           BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
                           entityCreature.setMinion(true);
                           entityCreature.setTemporary(this.summonDuration);
                           entityCreature.setSizeScale(this.sizeScale);
                           if (projectile.getThrower() instanceof Player && entityCreature instanceof TameableCreatureEntity) {
                              TameableCreatureEntity entityTameable = (TameableCreatureEntity)entityCreature;
                              entityTameable.setPlayerOwner((Player)projectile.getThrower());
                              entityTameable.setSitting(false);
                              entityTameable.setFollowing(true);
                              entityTameable.setPassive(false);
                              entityTameable.setAssist(true);
                              entityTameable.setAggressive(true);
                              if (summonSet != null) {
                                 summonSet.applyBehaviour(entityTameable);
                                 entityTameable.setSubspecies(summonSet.subspecies);
                                 entityTameable.applyVariant(summonSet.variant);
                              }
                           }

                           float randomAngle = 45.0F + 45.0F * projectile.level().random.nextFloat();
                           if (projectile.level().random.nextBoolean()) {
                              randomAngle = -randomAngle;
                           }

                           BlockPos spawnPos = entityCreature.getFacingPosition(projectile, -1.0, (double)randomAngle);
                           entity.moveTo(
                              (double)spawnPos.getX(),
                              (double)spawnPos.getY(),
                              (double)spawnPos.getZ(),
                              projectile.getYRot(),
                              0.0F
                           );
                           projectile.level().addFreshEntity(entity);
                        }
                     } catch (Exception var12) {
                        var12.printStackTrace();
                     }
                  }
               }
            }
         }
      }
   }
}
