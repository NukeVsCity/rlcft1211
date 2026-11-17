package ichttt.mods.firstaid.common.damagesystem;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.12 AbstractDamageablePart + DamageablePart (+ AbstractPartHealer/PartHealer as {@link PartHealer}). */
public class DamageablePart {
   public final int initialMaxHealth;
   public final boolean canCauseDeath;
   public final EnumPlayerPart part;
   @Nullable
   public PartHealer activeHealer;
   public float currentHealth;
   private int maxHealth;
   private final IDebuff[] debuffs;
   private float absorption;

   public DamageablePart(int maxHealth, boolean canCauseDeath, EnumPlayerPart playerPart, IDebuff... debuffs) {
      this.initialMaxHealth = maxHealth;
      this.canCauseDeath = canCauseDeath;
      this.part = playerPart;
      this.maxHealth = maxHealth;
      this.currentHealth = (float)maxHealth;
      this.debuffs = debuffs;
   }

   public float heal(float amount, @Nullable Player player, boolean applyDebuff) {
      if (amount <= 0.0F) {
         return 0.0F;
      }
      float notFitting = Math.abs(Math.min(0.0F, (float)this.maxHealth - (this.currentHealth + amount)));
      this.currentHealth = Math.min((float)this.maxHealth, this.currentHealth + amount);
      if (notFitting > 0.0F) {
         float oldHealth = this.currentHealth;
         this.currentHealth = Math.min(this.currentHealth + notFitting, this.currentHealth);
         notFitting -= this.currentHealth - oldHealth;
      }
      float finalNotFitting = notFitting;
      if (applyDebuff) {
         Objects.requireNonNull(player, "Got null player with applyDebuff = true");
         Arrays.stream(this.debuffs).forEach(d -> d.handleHealing(amount - finalNotFitting, this.currentHealth / (float)this.maxHealth, (ServerPlayer)player));
      }
      return notFitting;
   }

   public float damage(float amount, @Nullable Player player, boolean applyDebuff) {
      return this.damage(amount, player, applyDebuff, 0.0F);
   }

   public float damage(float amount, @Nullable Player player, boolean applyDebuff, float minHealth) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else if (minHealth > (float)this.maxHealth) {
         throw new IllegalArgumentException("Cannot damage part with minHealth " + minHealth + " while he has more max health (" + this.maxHealth + ")");
      }
      float origAmount = amount;
      if (this.absorption > 0.0F) {
         amount = Math.abs(Math.min(0.0F, this.absorption - amount));
         this.absorption = Math.max(0.0F, this.absorption - origAmount);
      }
      float notFitting = Math.abs(Math.min(minHealth, this.currentHealth - amount) - minHealth);
      this.currentHealth = Math.max(minHealth, this.currentHealth - amount);
      if (applyDebuff) {
         Objects.requireNonNull(player, "Got null player with applyDebuff = true");
         Arrays.stream(this.debuffs).forEach(d -> d.handleDamageTaken(origAmount - notFitting, this.currentHealth / (float)this.maxHealth, (ServerPlayer)player));
      }
      return notFitting;
   }

   public void tick(Level world, Player player, boolean tickDebuffs) {
      if (this.activeHealer != null) {
         if (this.activeHealer.tick()) {
            this.heal(1.0F, player, !world.isClientSide);
         }
         if (this.activeHealer.hasFinished()) {
            this.activeHealer = null;
         }
      }
      if (!world.isClientSide && tickDebuffs) {
         Arrays.stream(this.debuffs).forEach(d -> d.update(player, this.currentHealth / (float)this.maxHealth));
      }
   }

   public CompoundTag serializeNBT(HolderLookup.Provider provider) {
      CompoundTag compound = new CompoundTag();
      compound.putFloat("health", this.currentHealth);
      if (FirstAidConfig.scaleMaxHealth) {
         compound.putInt("maxHealth", this.maxHealth);
      }
      if (this.absorption > 0.0F) {
         compound.putFloat("absorption", this.absorption);
      }
      if (this.activeHealer != null) {
         compound.put("healer", this.activeHealer.stack.save(provider));
         compound.putInt("itemTicks", this.activeHealer.getTicksPassed());
         compound.putInt("itemHeals", this.activeHealer.getHealsDone());
      }
      return compound;
   }

   public void deserializeNBT(HolderLookup.Provider provider, @Nullable CompoundTag nbt) {
      if (nbt == null) {
         return;
      }
      if (nbt.contains("maxHealth") && FirstAidConfig.scaleMaxHealth) {
         this.maxHealth = nbt.getInt("maxHealth");
      }
      this.currentHealth = Math.min((float)this.maxHealth, nbt.getFloat("health"));
      ItemStack stack = null;
      if (nbt.contains("healer")) {
         stack = ItemStack.parseOptional(provider, nbt.getCompound("healer"));
      }
      if (stack != null && !stack.isEmpty()) {
         PartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
         if (healer == null) {
            FirstAid.LOGGER.warn("Failed to lookup healer for item {}", stack.getItem());
         } else {
            this.activeHealer = healer.loadNBT(nbt.getInt("itemTicks"), nbt.getInt("itemHeals"));
         }
      }
      if (nbt.contains("absorption")) {
         this.absorption = nbt.getFloat("absorption");
      }
      Arrays.stream(this.debuffs).forEach(d -> d.handleHealing(0.0F, this.currentHealth / (float)this.maxHealth, null));
   }

   public void setAbsorption(float absorption) {
      if (absorption > 4.0F && FirstAidConfig.capMaxHealth) {
         absorption = 4.0F;
      }
      if (absorption > 32.0F) {
         absorption = 32.0F;
      }
      this.absorption = absorption;
      this.currentHealth = Math.min((float)this.maxHealth + absorption, this.currentHealth);
   }

   public float getAbsorption() {
      return this.absorption;
   }

   public void setMaxHealth(int maxHealth) {
      if (maxHealth > 12 && FirstAidConfig.capMaxHealth) {
         maxHealth = 12;
      }
      if (maxHealth > 128) {
         maxHealth = 128;
      }
      this.maxHealth = Math.max(2, maxHealth);
      this.currentHealth = Math.min(this.currentHealth, (float)this.maxHealth);
   }

   public int getMaxHealth() {
      return this.maxHealth;
   }

   /** 1.12 AbstractPartHealer + PartHealer: one heal (half a heart) every ticksPerHeal ticks, maxHeal heals. */
   public static class PartHealer {
      public final int maxHeal;
      public final ItemStack stack;
      public final int ticksPerHeal;
      private int ticksPassed = 0;
      private int heals = 0;

      public PartHealer(int ticksPerHeal, int maxHeal, ItemStack stack) {
         this.maxHeal = maxHeal;
         this.stack = stack;
         this.ticksPerHeal = ticksPerHeal;
      }

      public PartHealer loadNBT(int ticksPassed, int heals) {
         this.ticksPassed = ticksPassed;
         this.heals = heals;
         return this;
      }

      public boolean hasFinished() {
         return this.heals >= this.maxHeal;
      }

      public boolean tick() {
         if (this.hasFinished()) {
            return false;
         }
         this.ticksPassed++;
         boolean nextHeal = this.ticksPassed >= this.ticksPerHeal;
         if (nextHeal) {
            this.ticksPassed = 0;
            this.heals++;
         }
         return nextHeal;
      }

      public int getTicksPassed() {
         return this.ticksPassed;
      }

      public int getHealsDone() {
         return this.heals;
      }
   }
}
