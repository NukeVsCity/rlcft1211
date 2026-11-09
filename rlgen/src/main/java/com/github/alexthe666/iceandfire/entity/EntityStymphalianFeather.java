package com.github.alexthe666.iceandfire.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Ice and Fire 1.7.1 EntityStymphalianFeather: the bird's feather darts; 1 in 25 turn into a feather item when they vanish. */
public class EntityStymphalianFeather extends AbstractArrow {
   public EntityStymphalianFeather(EntityType<? extends EntityStymphalianFeather> type, Level level) {
      super(type, level);
   }

   public EntityStymphalianFeather(Level level, LivingEntity shooter) {
      super(AirWaterEntities.STYMPHALIAN_FEATHER.get(), shooter, level, new ItemStack(AirWaterRefs.item("stymphalian_bird_feather")), null);
      this.setBaseDamage(AirWaterConfig.stymphalianBirdFeatherAttackStength);
   }

   @Override
   public void remove(RemovalReason reason) {
      boolean dead = !this.isRemoved() && (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED);
      super.remove(reason);
      if (dead && AirWaterConfig.stymphalianBirdFeatherDropChance > 0 && !this.level().isClientSide
         && this.random.nextInt(AirWaterConfig.stymphalianBirdFeatherDropChance) == 0) {
         this.spawnAtLocation(this.getDefaultPickupItem(), 0.1F);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.tickCount > 100) {
         this.discard();
      }
   }

   /** Feathers of a bird pass through other stymphalian birds. */
   @Override
   protected boolean canHitEntity(Entity target) {
      if (this.getOwner() instanceof EntityStymphalianBird && target instanceof EntityStymphalianBird) {
         return false;
      }
      return super.canHitEntity(target);
   }

   @Override
   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      if (result.getEntity() instanceof LivingEntity living) {
         living.setArrowCount(living.getArrowCount() - 1);
      }
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(AirWaterRefs.item("stymphalian_bird_feather"));
   }
}
