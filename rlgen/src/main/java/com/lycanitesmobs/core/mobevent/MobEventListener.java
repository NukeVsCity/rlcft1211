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
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.core.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.RandomMobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.TickMobEventTrigger;
import java.util.ArrayList;
import java.util.List;


public class MobEventListener {
   protected static MobEventListener INSTANCE;
   public List<RandomMobEventTrigger> randomMobEventTriggers = new ArrayList<>();
   public List<TickMobEventTrigger> tickMobEventTriggers = new ArrayList<>();

   public static MobEventListener getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MobEventListener();
      }

      return INSTANCE;
   }

   public boolean addTrigger(MobEventTrigger mobEventTrigger) {
      if (mobEventTrigger instanceof RandomMobEventTrigger && !this.randomMobEventTriggers.contains(mobEventTrigger)) {
         this.randomMobEventTriggers.add((RandomMobEventTrigger)mobEventTrigger);
         return true;
      } else if (mobEventTrigger instanceof TickMobEventTrigger && !this.tickMobEventTriggers.contains(mobEventTrigger)) {
         this.tickMobEventTriggers.add((TickMobEventTrigger)mobEventTrigger);
         return true;
      } else {
         return false;
      }
   }

   public void removeTrigger(MobEventTrigger mobEventTrigger) {
      if (this.randomMobEventTriggers.contains(mobEventTrigger)) {
         this.randomMobEventTriggers.remove(mobEventTrigger);
      }

      if (this.tickMobEventTriggers.contains(mobEventTrigger)) {
         this.tickMobEventTriggers.remove(mobEventTrigger);
      }

      if (mobEventTrigger instanceof AltarMobEventTrigger) {
         AltarMobEventTrigger altarMobEventTrigger = (AltarMobEventTrigger)mobEventTrigger;
         altarMobEventTrigger.onRemove();
      }
   }

   @SubscribeEvent
   public void onWorldUpdate(WorldTickEvent event) {
      Level world = event.world;
      if (!world.isClientSide) {
         ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
         if (worldExt != null) {
            if (MobEventManager.getInstance().mobEventsEnabled && world.getDifficulty() != Difficulty.PEACEFUL) {
               if (worldExt.lastEventScheduleTime != world.getGameTime()) {
                  worldExt.lastEventScheduleTime = world.getGameTime();

                  for (MobEventSchedule mobEventSchedule : MobEventManager.getInstance().mobEventSchedules) {
                     if (mobEventSchedule.canStart(world)) {
                        mobEventSchedule.start(worldExt);
                     }
                  }

                  for (TickMobEventTrigger mobEventTrigger : this.tickMobEventTriggers) {
                     mobEventTrigger.onTick(world, worldExt.lastEventScheduleTime);
                  }

                  if (MobEventManager.getInstance().mobEventsRandom) {
                     if (MobEventManager.getInstance().minEventsRandomDay > 0
                        && Math.floor((double)(worldExt.useTotalWorldTime ? world.getGameTime() : world.getDayTime()) / 24000.0)
                           < (double)MobEventManager.getInstance().minEventsRandomDay) {
                        return;
                     }

                     if (worldExt.getWorldEventStartTargetTime() <= 0L
                        || worldExt.getWorldEventStartTargetTime() > world.getGameTime() + (long)MobEventManager.getInstance().maxTicksUntilEvent) {
                        worldExt.setWorldEventStartTargetTime(world.getGameTime() + (long)worldExt.getRandomEventDelay(world.random));
                     }

                     if (world.getGameTime() == worldExt.getWorldEventStartTargetTime()) {
                        this.triggerRandomMobEvent(world, worldExt);
                     } else if (world.getGameTime() > worldExt.getWorldEventStartTargetTime()) {
                        worldExt.setWorldEventStartTargetTime(0L);
                     }
                  }
               }
            } else {
               if (worldExt.serverWorldEventPlayer != null) {
                  worldExt.stopWorldEvent();
               }
            }
         }
      }
   }

   public void triggerRandomMobEvent(Level world, ExtendedWorld worldExt) {
      List<RandomMobEventTrigger> validTriggers = new ArrayList<>();
      int totalWeights = 0;
      int highestPriority = 0;

      for (RandomMobEventTrigger mobEventTrigger : this.randomMobEventTriggers) {
         if (mobEventTrigger.priority >= highestPriority && mobEventTrigger.canTrigger(world, null)) {
            if (mobEventTrigger.priority > highestPriority) {
               totalWeights = 0;
               validTriggers.clear();
            }

            totalWeights += mobEventTrigger.weight;
            highestPriority = mobEventTrigger.priority;
            validTriggers.add(mobEventTrigger);
         }
      }

      if (totalWeights > 0) {
         int randomWeight = 1;
         if (totalWeights > 1) {
            randomWeight = world.random.nextInt(totalWeights - 1) + 1;
         }

         int searchWeight = 0;

         for (RandomMobEventTrigger mobEventTriggerx : validTriggers) {
            if (mobEventTriggerx.weight + searchWeight > randomWeight) {
               mobEventTriggerx.trigger(world, null, LycPos.of(0, 0, 0), 1, -1);
               return;
            }

            searchWeight += mobEventTriggerx.weight;
         }
      }
   }
}
