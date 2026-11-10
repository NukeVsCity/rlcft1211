package com.github.alexthe666.iceandfire.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 EntityStoneStatue: what a player turns into when a gorgon looks at him. */
public class EntityStoneStatue extends Mob implements IBlacklistedFromStatues {
   public boolean smallArms;
   private int crackAmount;

   public EntityStoneStatue(EntityType<? extends EntityStoneStatue> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes();
   }

   /** getCollisionBox / getCollisionBoundingBox: solid like a block. */
   @Override
   public boolean canBeCollidedWith() {
      return true;
   }

   @Override
   public boolean isNoAi() {
      return true;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("CrackAmount", this.crackAmount);
      tag.putBoolean("SmallArms", this.smallArms);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setCrackAmount(tag.getByte("CrackAmount"));
      this.smallArms = tag.getBoolean("SmallArms");
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return source.is(DamageTypes.FELL_OUT_OF_WORLD);
   }

   public int getCrackAmount() {
      return this.crackAmount;
   }

   public void setCrackAmount(int crackAmount) {
      this.crackAmount = crackAmount;
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }
}
