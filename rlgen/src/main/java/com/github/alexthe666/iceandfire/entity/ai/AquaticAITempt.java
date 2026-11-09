package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Ice and Fire 1.7.1 AquaticAITempt (used by the dragons with fire stew / frost stew). */
public class AquaticAITempt extends EntityAIBase {
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

   public AquaticAITempt(PathfinderMob temptedEntityIn, double speedIn, ItemLike temptItemIn, boolean scaredByPlayerMovementIn) {
      this(temptedEntityIn, speedIn, scaredByPlayerMovementIn, Sets.newHashSet(temptItemIn.asItem()));
   }

   public AquaticAITempt(PathfinderMob temptedEntityIn, double speedIn, boolean scaredByPlayerMovementIn, Set<Item> temptItemIn) {
      this.temptedEntity = temptedEntityIn;
      this.speed = speedIn;
      this.temptItem = temptItemIn;
      this.scaredByPlayerMovement = scaredByPlayerMovementIn;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      if (this.delayTemptCounter > 0) {
         this.delayTemptCounter--;
         return false;
      } else {
         this.temptingPlayer = this.temptedEntity.level().getNearestPlayer(this.temptedEntity, 10.0);
         return this.temptingPlayer == null ? false : this.isTempting(this.temptingPlayer.getMainHandItem()) || this.isTempting(this.temptingPlayer.getOffhandItem());
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

         this.pitch = (double)this.temptingPlayer.getXRot();
         this.yaw = (double)this.temptingPlayer.getYRot();
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
      // 1.12 getHorizontalFaceSpeed() + 20 (dragons: 10 * stage / 5), getVerticalFaceSpeed() = 40
      this.temptedEntity.getLookControl().setLookAt(this.temptingPlayer, (float)(this.horizontalFaceSpeed() + 20), (float)this.temptedEntity.getMaxHeadXRot());
      if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 6.25) {
         this.temptedEntity.getNavigation().stop();
      } else {
         this.temptedEntity.getNavigation().moveTo(this.temptingPlayer, this.speed);
      }
   }

   private int horizontalFaceSpeed() {
      return this.temptedEntity instanceof com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest.FaceSpeeds faceSpeeds
         ? faceSpeeds.getHorizontalFaceSpeed()
         : 10;
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
