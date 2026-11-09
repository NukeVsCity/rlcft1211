package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityStymphalianBird;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 StymphalianBirdAITarget */
public class StymphalianBirdAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   private final EntityStymphalianBird bird;

   public StymphalianBirdAITarget(EntityStymphalianBird entityIn, Class<T> classTarget, boolean checkSight) {
      super(entityIn, classTarget, 0, checkSight, false, entity -> !AirWaterUtils.isStone(entity)
         && (entity instanceof Player p && !p.isCreative() || entity instanceof AbstractVillager || entity instanceof AbstractGolem
            || entity instanceof Animal && com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stympahlianBirdAttackAnimals));
      this.bird = entityIn;
   }

   @Override
   public boolean shouldExecute() {
      boolean supe = super.shouldExecute();
      return this.targetEntity != null && this.bird.getVictor() != null && this.bird.getVictor().getUUID().equals(this.targetEntity.getUUID())
         ? false
         : supe && this.targetEntity != null && !this.targetEntity.getClass().equals(this.bird.getClass());
   }

   @Override
   protected AABB getTargetableArea(double targetDistance) {
      return this.bird.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
   }
}
