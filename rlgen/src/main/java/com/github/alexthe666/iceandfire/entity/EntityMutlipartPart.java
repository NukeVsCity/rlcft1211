package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.multipart.PartEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Ice and Fire 1.7.1 EntityMutlipartPart (sic). Interactions and hits go to the parent. In 1.12 the client also sent
 * MessageMultipartInteract to the server; with NeoForge part entities the server resolves the parent itself.
 */
public class EntityMutlipartPart extends PartEntity {
   public EntityMutlipartPart(Mob parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      super(parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
   }

   @Override
   public InteractionResult interact(Player player, InteractionHand hand) {
      return this.parent.interact(player, hand);
   }

   @Override
   public boolean hurt(DamageSource source, float damage) {
      return this.parent.hurt(source, damage * this.damageMultiplier);
   }

   public void resize(float width, float height) {
      this.setSize(width, height);
   }
}
