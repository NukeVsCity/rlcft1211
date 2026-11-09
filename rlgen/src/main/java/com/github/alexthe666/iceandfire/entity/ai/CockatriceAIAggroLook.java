package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyAi;
import net.minecraft.world.entity.player.Player;

/** 1.12 CockatriceAIAggroLook: an untamed cockatrice attacks the player who looks at it. */
public class CockatriceAIAggroLook extends EntityAINearestAttackableTarget<Player> {
   private final EntityCockatrice cockatrice;
   private Player player;
   private int aggroTime;
   private int teleportTime;

   public CockatriceAIAggroLook(EntityCockatrice cockatrice) {
      super(cockatrice, Player.class, false);
      this.cockatrice = cockatrice;
   }

   @Override
   public boolean canUse() {
      if (this.cockatrice.isTame()) {
         return false;
      } else {
         double d0 = this.getTargetDistance();
         this.player = LegacyAi.getNearestAttackablePlayer(
            this.cockatrice.level(),
            this.cockatrice.getX(),
            this.cockatrice.getY(),
            this.cockatrice.getZ(),
            d0,
            d0,
            p -> p != null && EntityGorgon.isEntityLookingAt(p, this.cockatrice, 0.6F)
         );
         return this.player != null;
      }
   }

   @Override
   public void start() {
      this.aggroTime = 5;
      this.teleportTime = 0;
   }

   @Override
   public void stop() {
      this.player = null;
      super.stop();
   }

   @Override
   public boolean canContinueToUse() {
      if (this.player != null) {
         if (!EntityGorgon.isEntityLookingAt(this.player, this.cockatrice, 0.4F)) {
            return false;
         } else {
            this.cockatrice.lookAt(this.player, 10.0F, 10.0F);
            if (!this.cockatrice.isTame()) {
               this.cockatrice.setTargetedEntity(this.player.getId());
               this.cockatrice.setTarget(this.player);
            }

            return true;
         }
      } else {
         return this.targetEntity != null && this.targetEntity.isAlive() ? true : super.canContinueToUse();
      }
   }
}
