package com.lycanitesmobs.core.entity.goals.actions;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import java.util.Random;


public class RandomPositionGenerator {
   private static Vec3 staticVector = new Vec3(0.0, 0.0, 0.0);

   public static Vec3 findRandomTarget(BaseCreatureEntity entity, int range, int height) {
      return findRandomTarget(entity, range, height, 0);
   }

   public static Vec3 findRandomTarget(BaseCreatureEntity entity, int range, int height, int heightLevel) {
      return getTargetBlock(entity, range, height, (Vec3)null, heightLevel);
   }

   public static Vec3 findRandomTargetTowards(BaseCreatureEntity entity, int range, int height, Vec3 chaseTarget) {
      staticVector = new Vec3(
         chaseTarget.x - entity.getX(), chaseTarget.y - entity.getY(), chaseTarget.z - entity.getZ()
      );
      return findRandomTargetTowards(entity, range, height, staticVector, 0);
   }

   public static Vec3 findRandomTargetTowards(BaseCreatureEntity entity, int range, int height, Vec3 chaseTarget, int heightLevel) {
      staticVector = new Vec3(
         chaseTarget.x - entity.getX(), chaseTarget.y - entity.getY(), chaseTarget.z - entity.getZ()
      );
      return getTargetBlock(entity, range, height, staticVector, heightLevel);
   }

   public static Vec3 findRandomTargetAwayFrom(BaseCreatureEntity entity, int range, int height, Vec3 avoidTarget) {
      staticVector = new Vec3(
         entity.getX() - avoidTarget.x, entity.getY() - avoidTarget.y, entity.getZ() - avoidTarget.z
      );
      return findRandomTargetAwayFrom(entity, range, height, avoidTarget, 0);
   }

   public static Vec3 findRandomTargetAwayFrom(BaseCreatureEntity entity, int range, int height, Vec3 avoidTarget, int heightLevel) {
      staticVector = new Vec3(entity.getX(), entity.getY(), entity.getZ()).subtract(avoidTarget);
      return getTargetBlock(entity, range, height, staticVector, heightLevel);
   }

   private static Vec3 getTargetBlock(BaseCreatureEntity entity, int range, int height, Vec3 target, int heightLevel) {
      PathNavigation pathNavigate = entity.getNavigation();
      RandomSource random = entity.getRandom();
      boolean validTarget = false;
      int targetX = 0;
      int targetY = 0;
      int targetZ = 0;
      float pathMin = -99999.0F;
      boolean pastHome;
      if (entity.hasHome()) {
         double homeDist = entity.getHomePosition()
               .distanceTo(
                  Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ())
               )
            + 4.0;
         double homeDistMax = (double)(entity.getHomeDistanceMax() + (float)range);
         pastHome = homeDist < homeDistMax * homeDistMax;
      } else {
         pastHome = false;
      }

      for (int attempt = 0; attempt < 10; attempt++) {
         int possibleX = random.nextInt(2 * range) - range;
         int possibleY = random.nextInt(2 * height) - height;
         int possibleZ = random.nextInt(2 * range) - range;
         if (entity.isFlying() || entity.isStrongSwimmer() && entity.isInWater()) {
            if (entity.getY() > (double)entity.level().getPrecipitationHeight(entity.blockPosition()).getY() + (double)heightLevel * 1.25) {
               possibleY = random.nextInt(2 * height) - height * 3 / 2;
            } else if (entity.getY() < (double)(entity.level().getPrecipitationHeight(entity.blockPosition()).getY() + heightLevel)) {
               possibleY = random.nextInt(2 * height) - height / 2;
            }
         }

         if (target == null || (double)possibleX * target.x + (double)possibleZ * target.z >= 0.0) {
            possibleX += Mth.floor(entity.getX());
            possibleY += Mth.floor(entity.getY());
            possibleZ += Mth.floor(entity.getZ());
            BlockPos possiblePos = LycPos.of(possibleX, possibleY, possibleZ);
            if ((!pastHome || entity.positionNearHome(possibleX, possibleY, possibleZ))
               && (entity.useDirectNavigator() || pathNavigate.canEntityStandOnPos(possiblePos))) {
               float pathWeight = entity.getBlockPathWeight(possibleX, possibleY, possibleZ);
               if (pathWeight > pathMin) {
                  pathMin = pathWeight;
                  targetX = possibleX;
                  targetY = possibleY;
                  targetZ = possibleZ;
                  validTarget = true;
               }
            }
         }
      }

      return validTarget ? new Vec3((double)targetX, (double)targetY, (double)targetZ) : null;
   }
}
