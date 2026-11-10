package com.github.alexthe666.iceandfire.entity;

import java.util.Random;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Ice and Fire 1.7.1 EntityDeathWormEgg: a thrown death worm egg hatches a tamed worm of the thrower. */
public class EntityDeathWormEgg extends ThrowableItemProjectile {
   private boolean giant;

   public EntityDeathWormEgg(EntityType<? extends EntityDeathWormEgg> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityDeathWormEgg(Level worldIn, LivingEntity throwerIn, boolean giant) {
      super(IafLandMobs.DEATHWORM_EGG.get(), throwerIn, worldIn);
      this.giant = giant;
   }

   public EntityDeathWormEgg(Level worldIn, double x, double y, double z) {
      super(IafLandMobs.DEATHWORM_EGG.get(), x, y, z, worldIn);
   }

   @Override
   protected Item getDefaultItem() {
      return IafLand.item("iceandfire.deathworm_egg");
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 3) {
         for (int i = 0; i < 8; i++) {
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(IafLand.item("iceandfire.deathworm_egg"))),
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  ((double)this.random.nextFloat() - 0.5) * 0.08,
                  ((double)this.random.nextFloat() - 0.5) * 0.08,
                  ((double)this.random.nextFloat() - 0.5) * 0.08
               );
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
      if (!this.level().isClientSide && this.getOwner() != null) {
         EntityDeathWorm deathworm = IafLandMobs.DEATHWORM.get().create(this.level());
         deathworm.setVariant(new Random().nextInt(3));
         deathworm.setTame(true, false);
         deathworm.setWormHome(this.blockPosition());
         deathworm.setWormAge(1);
         deathworm.setDeathWormScale(this.giant ? (0.25F + (float)(Math.random() * 0.35F)) * 4.0F : 0.25F + (float)(Math.random() * 0.35F));
         deathworm.setOwnerUUID(this.getOwner().getUUID());
         deathworm.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
         this.level().addFreshEntity(deathworm);
         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }
   }
}
