package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterConfig;
import com.github.alexthe666.iceandfire.entity.AirWaterRefs;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Ice and Fire 1.7.1 PixieAISteal: a wild pixie takes a random stack from the nearest player's inventory, then flees. */
public class PixieAISteal extends EntityAIBase {
   private final EntityPixie temptedEntity;
   private final double speed;
   private double targetX;
   private double targetY;
   private double targetZ;
   private Player temptingPlayer;
   private int delayTemptCounter;
   private boolean isRunning;

   public PixieAISteal(EntityPixie temptedEntityIn, double speedIn) {
      this.temptedEntity = temptedEntityIn;
      this.speed = speedIn;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      if (!AirWaterConfig.pixiesStealItems) {
         return false;
      } else if (this.temptedEntity.getRandom().nextInt(3) == 0) {
         return false;
      } else if (this.temptedEntity.isTame()) {
         return false;
      } else if (this.delayTemptCounter > 0) {
         this.delayTemptCounter--;
         return false;
      } else {
         this.temptingPlayer = Legacy112.getClosestPlayerToEntity(this.temptedEntity, 10.0);
         return this.temptingPlayer == null
            ? false
            : this.temptedEntity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && !this.temptingPlayer.getInventory().isEmpty()
               && !this.temptingPlayer.isCreative();
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
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
      this.temptedEntity.getLegacyMoveControl().setWait();
      this.delayTemptCounter = 10;
      this.isRunning = false;
   }

   @Override
   public void updateTask() {
      this.temptedEntity.getLookControl().setLookAt(this.temptingPlayer, Legacy112.horizontalFaceSpeed(this.temptedEntity) + 20,
         Legacy112.verticalFaceSpeed(this.temptedEntity));
      ArrayList<Integer> slotlist = new ArrayList<>();
      if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 6.25 && !this.temptingPlayer.getInventory().isEmpty()) {
         // 1.12 InventoryPlayer slots: 36 main, 4 armor, 1 offhand (getStackInSlot != EMPTY, i.e. every slot that is not the EMPTY constant)
         for (int i = 0; i < this.temptingPlayer.getInventory().getContainerSize(); i++) {
            if (!this.temptingPlayer.getInventory().getItem(i).isEmpty()) {
               slotlist.add(i);
            }
         }
         int slot = slotlist.get(new Random().nextInt(slotlist.size()));
         ItemStack randomItem = this.temptingPlayer.getInventory().getItem(slot);
         this.temptedEntity.setItemInHand(InteractionHand.MAIN_HAND, randomItem);
         this.temptingPlayer.getInventory().removeItemNoUpdate(slot);
         this.temptedEntity.flipAI(true);
         this.temptedEntity.playSound(AirWaterRefs.sound("pixie_taunt"), 1.0F, 1.0F);
         this.temptedEntity.getLegacyMoveControl().setWait();
         this.temptingPlayer.addEffect(new MobEffectInstance(this.temptedEntity.negativePotions[this.temptedEntity.getColor()], 100));
      } else {
         this.temptedEntity.getMoveControl().setWantedPosition(this.temptingPlayer.getX(), this.temptingPlayer.getY() + 1.5, this.temptingPlayer.getZ(), 1.0);
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
