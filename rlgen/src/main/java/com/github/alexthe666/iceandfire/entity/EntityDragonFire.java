package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 EntityDragonFire: one puff of the fire breath stream. Its impact is a {@link FireExplosion}. */
public class EntityDragonFire extends LegacyFireball implements IDragonProjectile {
   public EntityDragonFire(EntityType<? extends EntityDragonFire> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityDragonFire(Level worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_FIRE.get(), worldIn, posX, posY, posZ, accelX, accelY, accelZ);
   }

   public EntityDragonFire(Level worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_FIRE.get(), worldIn, shooter, accelX, accelY, accelZ);
      this.setSize(0.5F, 0.5F);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
      this.accelerationY = accelY / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
      this.accelerationZ = accelZ / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
   }

   public void setSizes(float width, float height) {
      this.setSize(width, height);
   }

   @Override
   protected boolean isFireballFiery() {
      return false;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public void onUpdate() {
      super.onUpdate();

      for (int i = 0; i < 6; i++) {
         DragonParticles.spawnParticle("dragonfire", this.level(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }

      if (this.tickCount > 160) {
         this.discard();
      }

      if (this.isInWater()) {
         this.discard();
      }
   }

   static boolean mobGriefing(Level level) {
      return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
   }

   /** 1.12 applyEnchantments(shootingEntity, entityHit). */
   static void applyEnchantments(LivingEntity shooter, Entity hit) {
      if (shooter != null && shooter.level() instanceof ServerLevel serverLevel) {
         EnchantmentHelper.doPostAttackEffects(serverLevel, hit, shooter.damageSources().mobAttack(shooter));
      }
   }

   @Override
   protected void onImpact(RayTraceResult movingObject) {
      boolean flag = mobGriefing(this.level());
      if (!this.level().isClientSide) {
         Entity hit = movingObject.entityHit;
         if (hit != null && hit instanceof IDragonProjectile) {
            return;
         }

         if (hit != null
            && this.shootingEntity != null
            && this.shootingEntity instanceof EntityDragonBase
            && ((EntityDragonBase)this.shootingEntity).isTame()
            && hit instanceof Player
            && ((EntityDragonBase)this.shootingEntity).isOwner((Player)hit)) {
            return;
         }

         if (hit != null && !(hit instanceof IDragonProjectile) && this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && hit != this.shootingEntity
            || hit == null) {
            if (hit instanceof TamableAnimal && ((EntityDragonBase)this.shootingEntity).getOwner() == ((TamableAnimal)hit).getOwner()) {
               return;
            }

            if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && IceAndFire.CONFIG.dragonGriefing != 2) {
               FireExplosion explosion = new FireExplosion(
                  this.level(), this.shootingEntity, this.getX(), this.getY(), this.getZ(), (float)((EntityDragonBase)this.shootingEntity).getDragonStage() * 2.5F, flag
               );
               explosion.doExplosionA();
               explosion.doExplosionB(true);
            } else if (hit != null) {
               hit.igniteForSeconds(5.0F);
            }

            this.discard();
         }

         if (hit != null && !(hit instanceof IDragonProjectile) && !hit.is(this.shootingEntity)) {
            if (this.shootingEntity != null
               && (
                  hit.is(this.shootingEntity)
                     || this.shootingEntity instanceof EntityDragonBase & hit instanceof TamableAnimal
                        && ((EntityDragonBase)this.shootingEntity).getOwner() == ((TamableAnimal)hit).getOwner()
               )) {
               return;
            }

            if (this.shootingEntity != null
               && this.shootingEntity instanceof EntityDragonBase
               && hit instanceof LivingEntity
               && ((LivingEntity)hit).getHealth() == 0.0F) {
               ((EntityDragonBase)this.shootingEntity).attackDecision = true;
            }

            applyEnchantments(this.shootingEntity, hit);
            this.discard();
         }

         if (movingObject.typeOfHit != RayTraceResult.Type.ENTITY || hit != null && !(hit instanceof IDragonProjectile)) {
            this.discard();
         }
      }

      this.discard();
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   @Override
   public float getPickRadius() {
      return 1.0F;
   }
}
