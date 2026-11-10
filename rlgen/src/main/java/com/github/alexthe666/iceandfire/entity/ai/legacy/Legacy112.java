package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** 1.12.2 World / EntitySelectors / EntityAITarget helpers the 1.12 AI code calls, written against the 1.21 API. */
public final class Legacy112 {
   private Legacy112() {
   }

   /** EntitySelectors.NOT_SPECTATING */
   public static boolean notSpectating(Entity e) {
      return !(e instanceof Player p) || !p.isSpectator();
   }

   /** EntitySelectors.CAN_AI_TARGET */
   public static boolean canAiTarget(Entity e) {
      return !(e instanceof Player p) || !p.isSpectator() && !p.isCreative();
   }

   /** 1.12 World.getClosestPlayer(x, y, z, distance, predicate) */
   @Nullable
   public static Player getClosestPlayer(Level level, double x, double y, double z, double distance, @Nullable Predicate<Entity> predicate) {
      double best = -1.0;
      Player found = null;
      for (Player player : level.players()) {
         if (predicate == null || predicate.test(player)) {
            double d = player.distanceToSqr(x, y, z);
            if ((distance < 0.0 || d < distance * distance) && (best == -1.0 || d < best)) {
               best = d;
               found = player;
            }
         }
      }
      return found;
   }

   /** 1.12 World.getClosestPlayerToEntity(entity, distance) (NOT_SPECTATING) */
   @Nullable
   public static Player getClosestPlayerToEntity(Entity entity, double distance) {
      return getClosestPlayer(entity.level(), entity.getX(), entity.getY(), entity.getZ(), distance, Legacy112::notSpectating);
   }

   /** 1.12 World.getNearestAttackablePlayer(x, y, z, maxXZ, maxY, multiplier, predicate) */
   @Nullable
   public static Player getNearestAttackablePlayer(Level level, double x, double y, double z, double maxXZ, double maxY,
         @Nullable Function<Player, Double> multiplier, @Nullable Predicate<Player> predicate) {
      double best = -1.0;
      Player found = null;
      for (Player player : level.players()) {
         if (!player.getAbilities().invulnerable && player.isAlive() && !player.isSpectator() && (predicate == null || predicate.test(player))) {
            double d = player.distanceToSqr(x, player.getY(), z);
            double range = maxXZ;
            if (player.isShiftKeyDown()) {
               range = maxXZ * 0.8F;
            }
            if (player.isInvisible()) {
               float f = player.getArmorCoverPercentage();
               if (f < 0.1F) {
                  f = 0.1F;
               }
               range *= 0.7F * f;
            }
            if (multiplier != null) {
               Double m = multiplier.apply(player);
               range *= m == null ? 1.0 : m;
            }
            if ((maxY < 0.0 || Math.abs(player.getY() - y) < maxY * maxY) && (maxXZ < 0.0 || d < range * range) && (best == -1.0 || d < best)) {
               best = d;
               found = player;
            }
         }
      }
      return found;
   }

   /** 1.12 World.findNearestEntityWithinAABB */
   @Nullable
   public static <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> clazz, AABB box, T closestTo) {
      List<? extends T> list = closestTo.level().getEntitiesOfClass(clazz, box, e -> true);
      T found = null;
      double best = Double.MAX_VALUE;
      for (T e : list) {
         if (e != closestTo && Legacy112.notSpectating(e)) {
            double d = closestTo.distanceToSqr(e);
            if (d <= best) {
               found = e;
               best = d;
            }
         }
      }
      return found;
   }

   /** 1.12 EntityAITarget.isSuitableTarget(attacker, target, includeInvincibles, checkSight) */
   public static boolean isSuitableTarget(Mob attacker, @Nullable LivingEntity target, boolean includeInvincibles, boolean checkSight) {
      if (target == null) {
         return false;
      } else if (target == attacker) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else if (!attacker.canAttackType(target.getType())) {
         return false;
      } else if (attacker.isAlliedTo(target)) {
         return false;
      } else {
         if (attacker instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            if (target instanceof OwnableEntity other && ownable.getOwnerUUID().equals(other.getOwnerUUID())) {
               return false;
            }
            if (target == ownable.getOwner()) {
               return false;
            }
         } else if (target instanceof Player player && !includeInvincibles && player.getAbilities().invulnerable) {
            return false;
         }
         return !checkSight || attacker.getSensing().hasLineOfSight(target);
      }
   }

   /** 1.12 EntityCreature.isWithinHomeDistanceFromPosition */
   public static boolean isWithinHomeDistance(Mob mob, BlockPos pos) {
      return !(mob instanceof PathfinderMob p) || p.isWithinRestriction(pos);
   }

   /** 1.12 EntityLiving.getVerticalFaceSpeed (40) / getHorizontalFaceSpeed (10) */
   public static float verticalFaceSpeed(Mob mob) {
      return mob.getMaxHeadXRot();
   }

   public static float horizontalFaceSpeed(Mob mob) {
      return mob.getMaxHeadYRot();
   }

   /** MathHelper.sqrt(double) as float */
   public static float sqrt(double d) {
      return (float)Math.sqrt(d);
   }

   /** Material.WATER of a 1.12 block: water and the non-solid blocks 1.21 puts in water (seagrass, kelp, bubble columns). */
   public static boolean isWaterMaterial(net.minecraft.world.level.block.state.BlockState state) {
      return state.getFluidState().is(net.minecraft.tags.FluidTags.WATER) && !state.blocksMotion();
   }

   public static float wrapDegrees(float f) {
      return Mth.wrapDegrees(f);
   }

   /**
    * Runs a mob's 1.12 task lists through {@link EntityAITasks#install} (targetTasks, then tasks, every tick) and, before them,
    * 1.12 EntityLiving.updateMovementGoalFlags every 5 ticks.
    */
   public static void install(net.minecraft.world.entity.Mob mob, EntityAITasks tasks, EntityAITasks targetTasks) {
      mob.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.Goal() {
         {
            this.setFlags(java.util.EnumSet.noneOf(net.minecraft.world.entity.ai.goal.Goal.Flag.class));
         }

         @Override
         public boolean canUse() {
            return true;
         }

         @Override
         public boolean isInterruptable() {
            return false;
         }

         @Override
         public boolean requiresUpdateEveryTick() {
            return true;
         }

         @Override
         public void tick() {
            if (mob.tickCount % 5 == 0) {
               boolean notMobRidden = !(mob.getControllingPassenger() instanceof net.minecraft.world.entity.Mob);
               boolean notInBoat = !(mob.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat);
               tasks.setControlFlag(1, notMobRidden);
               tasks.setControlFlag(4, notMobRidden && notInBoat);
               tasks.setControlFlag(2, notMobRidden);
            }
         }
      });
      EntityAITasks.install(mob, mob.goalSelector, tasks, mob.targetSelector, targetTasks);
   }
}
