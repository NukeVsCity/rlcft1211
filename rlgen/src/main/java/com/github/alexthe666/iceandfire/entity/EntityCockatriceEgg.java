package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Ice and Fire 1.7.1 EntityCockatriceEgg: the thrown rotten egg (1 in 4 hatches a cockatrice chick, 1 in 32 of those four). */
public class EntityCockatriceEgg extends ThrowableItemProjectile {
   public EntityCockatriceEgg(EntityType<? extends EntityCockatriceEgg> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityCockatriceEgg(Level worldIn, LivingEntity throwerIn) {
      super(IafLandMobs.COCKATRICE_EGG.get(), throwerIn, worldIn);
   }

   public EntityCockatriceEgg(Level worldIn, double x, double y, double z) {
      super(IafLandMobs.COCKATRICE_EGG.get(), x, y, z, worldIn);
   }

   @Override
   protected Item getDefaultItem() {
      return IafLand.item("rotten_egg");
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 3) {
         for (int i = 0; i < 8; i++) {
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(IafLand.item("rotten_egg"))),
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  ((double)this.random.nextFloat() - 0.5) * 0.08,
                  ((double)this.random.nextFloat() - 0.5) * 0.08,
                  ((double)this.random.nextFloat() - 0.5) * 0.08
               );
         }

         if (this.getOwner() != null && this.getOwner() instanceof Player) {
            for (int i = 0; i < 8; i++) {
               this.level()
                  .addParticle(
                     ParticleTypes.HEART,
                     this.getX(),
                     this.getY(),
                     this.getZ(),
                     ((double)this.random.nextFloat() - 0.5) * 0.08,
                     ((double)this.random.nextFloat() - 0.5) * 0.08,
                     ((double)this.random.nextFloat() - 0.5) * 0.08
                  );
            }
         }
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
   }

   @Override
   protected void onHit(HitResult result) {
      super.onHit(result);
      if (!this.level().isClientSide) {
         if (this.random.nextInt(4) == 0) {
            int i = 1;
            if (this.random.nextInt(32) == 0) {
               i = 4;
            }

            for (int j = 0; j < i; j++) {
               EntityCockatrice cockatrice = IafLandMobs.COCKATRICE.get().create(this.level());
               cockatrice.setAge(-24000);
               cockatrice.setHen(this.random.nextBoolean());
               cockatrice.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
               if (this.getOwner() != null && this.getOwner() instanceof Player) {
                  cockatrice.tame((Player)this.getOwner());
               }

               this.level().addFreshEntity(cockatrice);
            }
         }

         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }
   }
}
