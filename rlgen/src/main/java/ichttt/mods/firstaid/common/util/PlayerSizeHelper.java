package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import org.apache.commons.lang3.tuple.Pair;

/** 1.12 PlayerSizeHelper + AABBAlignedBoundingBox: which armor slot a projectile or a melee mob hits. */
public class PlayerSizeHelper {
   private static final Map<EquipmentSlot, double[]> NORMAL_BOXES = new LinkedHashMap<>();
   private static final Map<EquipmentSlot, double[]> SNEAKING_BOXES = new LinkedHashMap<>();

   static {
      NORMAL_BOXES.put(EquipmentSlot.FEET, new double[]{0.0, 0.15});
      NORMAL_BOXES.put(EquipmentSlot.LEGS, new double[]{0.15, 0.45});
      NORMAL_BOXES.put(EquipmentSlot.CHEST, new double[]{0.45, 0.8});
      NORMAL_BOXES.put(EquipmentSlot.HEAD, new double[]{0.8, 1.0});
      SNEAKING_BOXES.put(EquipmentSlot.FEET, new double[]{0.0, 0.15});
      SNEAKING_BOXES.put(EquipmentSlot.LEGS, new double[]{0.15, 0.4});
      SNEAKING_BOXES.put(EquipmentSlot.CHEST, new double[]{0.4, 0.75});
      SNEAKING_BOXES.put(EquipmentSlot.HEAD, new double[]{0.75, 1.0});
   }

   public static Map<EquipmentSlot, double[]> getBoxes(Entity entity) {
      return entity.isShiftKeyDown() ? SNEAKING_BOXES : NORMAL_BOXES;
   }

   /** AABBAlignedBoundingBox.createAABB: full width, the given height fraction of the box */
   private static AABB createAABB(double[] y, AABB original) {
      double sizeY = original.maxY - original.minY;
      return new AABB(original.minX, original.minY + sizeY * y[0], original.minZ, original.maxX, original.minY + sizeY * y[1], original.maxZ);
   }

   @Nullable
   public static EquipmentSlot getSlotTypeForProjectileHit(Entity hittingObject, Player toTest) {
      Map<EquipmentSlot, double[]> toUse = getBoxes(toTest);
      Vec3 oldPosition = hittingObject.position();
      Vec3 newPosition = oldPosition.add(hittingObject.getDeltaMovement());
      float[] inflationSteps = new float[]{0.01F, 0.1F, 0.2F, 0.3F};
      for (float inflation : inflationSteps) {
         EquipmentSlot bestSlot = null;
         double bestValue = Double.MAX_VALUE;
         for (Map.Entry<EquipmentSlot, double[]> entry : toUse.entrySet()) {
            AABB box = createAABB(entry.getValue(), toTest.getBoundingBox()).inflate(inflation);
            Optional<Vec3> rtr = box.clip(oldPosition, newPosition);
            if (rtr.isPresent()) {
               double d1 = oldPosition.distanceToSqr(rtr.get());
               if (d1 < bestValue) {
                  bestSlot = entry.getKey();
                  bestValue = d1;
               }
            }
         }
         if (bestSlot != null) {
            return bestSlot;
         }
      }
      return null;
   }

   /** A melee mob (EntityMob with EntityAIAttackMelee running against the player) only reaches the slots its box overlaps. */
   @Nullable
   public static IDamageDistribution getMeleeDistribution(Player player, DamageSource source) {
      Entity causingEntity = source.getEntity();
      if (causingEntity != null && causingEntity == source.getDirectEntity() && causingEntity instanceof Monster mobEntity) {
         if (mobEntity.getTarget() == player && isMeleeAttacking(mobEntity)) {
            Map<EquipmentSlot, double[]> boxes = getBoxes(player);
            List<EquipmentSlot> allowedParts = new ArrayList<>();
            double grow = mobEntity.getBbWidth() * 2.0F + player.getBbWidth();
            AABB modAABB = mobEntity.getBoundingBox().inflate(grow, 0.0, grow);
            for (Map.Entry<EquipmentSlot, double[]> entry : boxes.entrySet()) {
               if (modAABB.intersects(createAABB(entry.getValue(), player.getBoundingBox()))) {
                  allowedParts.add(entry.getKey());
               }
            }
            if (allowedParts.isEmpty() && player.getY() > mobEntity.getY() && player.getY() - mobEntity.getY() < (double)(mobEntity.getBbWidth() * 2.0F)) {
               allowedParts.add(EquipmentSlot.FEET);
            }
            if (!allowedParts.isEmpty() && !allowedParts.containsAll(Arrays.asList(CommonUtils.ARMOR_SLOTS))) {
               List<Pair<EquipmentSlot, EnumPlayerPart[]>> list = new ArrayList<>();
               for (EquipmentSlot allowedPart : allowedParts) {
                  list.add(Pair.of(allowedPart, CommonUtils.getPartArrayForSlot(allowedPart)));
               }
               return new StandardDamageDistribution(list, true, true);
            }
         }
      }
      return null;
   }

   /** 1.12 EntityAIAttackMelee running: the vanilla goal, or the ported 1.12 task in the "tasks" list of rlgen's legacy 1.12 AI. */
   private static boolean isMeleeAttacking(Monster mob) {
      for (WrappedGoal g : mob.goalSelector.getAvailableGoals()) {
         if (g.isRunning() && g.getGoal() instanceof MeleeAttackGoal) {
            return true;
         }
      }
      EntityAITasks tasks = legacyTasks(mob);
      return tasks != null && tasks.isExecutingType(EntityAIAttackMelee.class);
   }

   private static final Map<Class<?>, Optional<Field>> TASK_FIELDS = new java.util.concurrent.ConcurrentHashMap<>();

   @Nullable
   private static EntityAITasks legacyTasks(Monster mob) {
      Optional<Field> f = TASK_FIELDS.computeIfAbsent(mob.getClass(), cls -> {
         for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
               if (field.getType() == EntityAITasks.class && field.getName().equals("tasks")) {
                  field.setAccessible(true);
                  return Optional.of(field);
               }
            }
         }
         return Optional.empty();
      });
      try {
         return f.isPresent() ? (EntityAITasks)f.get().get(mob) : null;
      } catch (IllegalAccessException e) {
         return null;
      }
   }
}
