package com.github.alexthe666.iceandfire.entity;

import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityStymphalianArrow: 3.5 damage, no gravity until it slows down. */
public class EntityStymphalianArrow extends AbstractArrow {
   public EntityStymphalianArrow(EntityType<? extends EntityStymphalianArrow> type, Level level) {
      super(type, level);
      this.setBaseDamage(3.5);
   }

   public EntityStymphalianArrow(Level level, double x, double y, double z, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.STYMPHALIAN_ARROW.get(), x, y, z, level, pickup, weapon);
      this.setBaseDamage(3.5);
   }

   public EntityStymphalianArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.STYMPHALIAN_ARROW.get(), shooter, level, pickup, weapon);
      this.setBaseDamage(3.5);
   }

   @Override
   public void tick() {
      super.tick();
      Vec3 m = this.getDeltaMovement();
      float sqrt = (float)Math.sqrt(m.x * m.x + m.z * m.z);
      if (sqrt < 0.1F) {
         this.setDeltaMovement(m.x, m.y - 0.01F, m.z);
      }
   }

   @Override
   public boolean isNoGravity() {
      return true;
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(AirWaterRefs.item("stymphalian_arrow"));
   }
}
