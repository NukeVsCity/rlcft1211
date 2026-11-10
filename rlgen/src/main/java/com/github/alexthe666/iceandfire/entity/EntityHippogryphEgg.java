package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Ice and Fire 1.7.1 EntityHippogryphEgg: always hatches a baby hippogryph of the egg's type. */
public class EntityHippogryphEgg extends ThrowableItemProjectile {
   private ItemStack itemstack;

   public EntityHippogryphEgg(EntityType<? extends EntityHippogryphEgg> type, Level level) {
      super(type, level);
   }

   public EntityHippogryphEgg(Level level, LivingEntity thrower, ItemStack itemstack) {
      super(AirWaterEntities.HIPPOGRYPH_EGG.get(), thrower, level);
      this.itemstack = itemstack.copy();
      this.setItem(itemstack.copyWithCount(1));
   }

   @Override
   protected Item getDefaultItem() {
      return AirWaterRefs.item("hippogryph_egg");
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 3) {
         for (int i = 0; i < 8; i++) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(),
               ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08,
               ((double)this.random.nextFloat() - 0.5) * 0.08);
         }
      }
   }

   @Override
   protected void onHit(HitResult result) {
      super.onHit(result);
      if (result instanceof EntityHitResult hit) {
         hit.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
      }
      if (!this.level().isClientSide) {
         EntityHippogryph hippogryph = AirWaterEntities.HIPPOGRYPH.get().create(this.level());
         hippogryph.setAge(-24000);
         hippogryph.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
         if (this.itemstack != null) {
            hippogryph.setVariant(AirWaterEntities.hippogryphEggVariant(this.itemstack));
         }
         this.level().addFreshEntity(hippogryph);
      }
      this.level().broadcastEntityEvent(this, (byte)3);
      this.discard();
   }
}
