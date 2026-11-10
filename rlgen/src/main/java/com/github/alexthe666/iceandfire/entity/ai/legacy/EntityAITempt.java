package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.Set;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** 1.12.2 EntityAITempt */
public class EntityAITempt extends EntityAIBase {
   private final PathfinderMob temptedEntity;
   private final double speed;
   private double targetX;
   private double targetY;
   private double targetZ;
   private double pitch;
   private double yaw;
   private Player temptingPlayer;
   private int delayTemptCounter;
   private boolean isRunning;
   private final Set<Item> temptItem;
   private final boolean scaredByPlayerMovement;

   public EntityAITempt(PathfinderMob entity, double speed, Item temptItem, boolean scaredByPlayerMovement) {
      this(entity, speed, scaredByPlayerMovement, Set.of(temptItem));
   }

   public EntityAITempt(PathfinderMob entity, double speed, boolean scaredByPlayerMovement, Set<Item> temptItem) {
      this.temptedEntity = entity;
      this.speed = speed;
      this.temptItem = temptItem;
      this.scaredByPlayerMovement = scaredByPlayerMovement;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      if (this.delayTemptCounter > 0) {
         this.delayTemptCounter--;
         return false;
      } else {
         this.temptingPlayer = Legacy112.getClosestPlayerToEntity(this.temptedEntity, 10.0);
         return this.temptingPlayer == null
            ? false
            : this.isTempting(this.temptingPlayer.getMainHandItem()) || this.isTempting(this.temptingPlayer.getOffhandItem());
      }
   }

   protected boolean isTempting(ItemStack stack) {
      return this.temptItem.contains(stack.getItem());
   }

   @Override
   public boolean shouldContinueExecuting() {
      if (this.scaredByPlayerMovement) {
         if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 36.0) {
            if (this.temptingPlayer.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002) {
               return false;
            }
            if (Math.abs((double)this.temptingPlayer.getXRot() - this.pitch) > 5.0 || Math.abs((double)this.temptingPlayer.getYRot() - this.yaw) > 5.0) {
               return false;
            }
         } else {
            this.targetX = this.temptingPlayer.getX();
            this.targetY = this.temptingPlayer.getY();
            this.targetZ = this.temptingPlayer.getZ();
         }
         this.pitch = this.temptingPlayer.getXRot();
         this.yaw = this.temptingPlayer.getYRot();
      }
      return this.shouldExecute();
   }

   @Override
   public void startExecuting() {
      this.targetX = this.temptingPlayer.getX();
      this.targetY = this.temptingPlayer.getY();
      this.targetZ = this.temptingPlayer.getZ();
      this.isRunning = true;
   }

   @Override
   public void resetTask() {
      this.temptingPlayer = null;
      this.temptedEntity.getNavigation().stop();
      this.delayTemptCounter = 100;
      this.isRunning = false;
   }

   @Override
   public void updateTask() {
      this.temptedEntity.getLookControl().setLookAt(this.temptingPlayer, Legacy112.horizontalFaceSpeed(this.temptedEntity) + 20,
         Legacy112.verticalFaceSpeed(this.temptedEntity));
      if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 6.25) {
         this.temptedEntity.getNavigation().stop();
      } else {
         this.temptedEntity.getNavigation().moveTo(this.temptingPlayer, this.speed);
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
