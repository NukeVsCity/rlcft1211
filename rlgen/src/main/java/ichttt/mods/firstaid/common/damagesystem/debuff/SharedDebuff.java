package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** 1.12 SharedDebuff: one debuff for several parts (arms, legs+feet), fed the averaged damage/healing once per tick. */
public class SharedDebuff implements IDebuff {
   private final IDebuff debuff;
   private final EnumPlayerPart[] parts;
   private int damage;
   private int healingDone;
   private int damageCount;
   private int healingCount;

   public SharedDebuff(IDebuff debuff, EnumDebuffSlot slot) {
      if (slot.playerParts.length <= 1) {
         throw new IllegalArgumentException("Only slots with more then more parts can be wrapped by SharedDebuff!");
      }
      this.debuff = debuff;
      this.parts = slot.playerParts;
   }

   @Override
   public void handleDamageTaken(float damage, float healthPerMax, @Nullable ServerPlayer player) {
      if (this.debuff.isEnabled()) {
         this.damage = (int)((float)this.damage + damage);
         this.damageCount++;
      }
   }

   @Override
   public void handleHealing(float healingDone, float healthPerMax, @Nullable ServerPlayer player) {
      if (this.debuff.isEnabled()) {
         this.healingDone = (int)((float)this.healingDone + healingDone);
         this.healingCount++;
      }
   }

   public void tick(Player player) {
      if (this.debuff.isEnabled() && !player.level().isClientSide && player instanceof ServerPlayer sp) {
         PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
         float healthPerMax = 0.0F;
         for (EnumPlayerPart part : this.parts) {
            DamageablePart damageablePart = damageModel.getFromEnum(part);
            healthPerMax += damageablePart.currentHealth / (float)damageablePart.getMaxHealth();
         }
         healthPerMax /= (float)this.parts.length;
         if (this.healingCount > 0) {
            this.healingDone = this.healingDone / this.healingCount;
            this.debuff.handleHealing((float)this.healingDone, healthPerMax, sp);
         }
         if (this.damageCount > 0) {
            this.damage = this.damage / this.damageCount;
            this.debuff.handleDamageTaken((float)this.damage, healthPerMax, sp);
         }
         this.healingDone = 0;
         this.damage = 0;
         this.damageCount = 0;
         this.healingCount = 0;
         this.debuff.update(player, healthPerMax);
      }
   }

   @Override
   public boolean isEnabled() {
      return true;
   }
}
