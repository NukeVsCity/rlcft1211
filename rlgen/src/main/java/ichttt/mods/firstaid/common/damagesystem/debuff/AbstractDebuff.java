package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/** 1.12 IDebuff, AbstractDebuff, OnHitDebuff and ConstantDebuff. */
public abstract class AbstractDebuff implements IDebuff {
   public final Holder<MobEffect> effect;
   public final BooleanSupplier isEnabled;
   protected final Float2IntLinkedOpenHashMap map;

   protected AbstractDebuff(String potionName, Float2IntLinkedOpenHashMap map, BooleanSupplier isEnabled) {
      this.effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.withDefaultNamespace(potionName)).orElseThrow();
      this.isEnabled = isEnabled;
      this.map = map;
   }

   @Override
   public boolean isEnabled() {
      return this.isEnabled.getAsBoolean();
   }

   /** OnHitDebuff: every bound the damage reaches adds the effect for its length; the longest one plays the sound. */
   public static class OnHitDebuff extends AbstractDebuff {
      @Nullable
      private final SoundEvent sound;

      public OnHitDebuff(String potionName, Float2IntLinkedOpenHashMap map, BooleanSupplier isEnabled, @Nullable SoundEvent sound) {
         super(potionName, map, isEnabled);
         this.sound = sound;
      }

      @Override
      public void handleDamageTaken(float damage, float healthPerMax, @Nullable ServerPlayer player) {
         if (this.isEnabled.getAsBoolean()) {
            int value = -1;
            for (Float2IntMap.Entry entry : this.map.float2IntEntrySet()) {
               if (damage >= entry.getFloatKey()) {
                  value = Math.max(value, entry.getIntValue());
                  Objects.requireNonNull(player).addEffect(new MobEffectInstance(this.effect, entry.getIntValue(), 0, false, false));
               }
            }
            if (value != -1 && this.sound != null) {
               FirstAidNetwork.playHurtSound(Objects.requireNonNull(player), this.sound, value);
            }
         }
      }

      @Override
      public void handleHealing(float healingDone, float healthPerMax, @Nullable ServerPlayer player) {
      }
   }

   /** ConstantDebuff: while the part is below a health fraction, the effect is renewed (169 ticks) every 79 ticks. */
   public static class ConstantDebuff extends AbstractDebuff {
      private int ticks = 0;
      private int activeMultiplier = 0;

      public ConstantDebuff(String potionName, Float2IntLinkedOpenHashMap map, BooleanSupplier isEnabled) {
         super(potionName, map, isEnabled);
      }

      private void syncMultiplier(float healthPerMax) {
         if (this.isEnabled.getAsBoolean()) {
            boolean found = false;
            for (Float2IntMap.Entry entry : this.map.float2IntEntrySet()) {
               if (healthPerMax < entry.getFloatKey()) {
                  this.ticks = 0;
                  this.activeMultiplier = entry.getIntValue();
                  found = true;
                  break;
               }
            }
            if (!found) {
               this.activeMultiplier = 0;
            }
         }
      }

      @Override
      public void handleDamageTaken(float damage, float healthPerMax, @Nullable ServerPlayer player) {
         this.syncMultiplier(healthPerMax);
      }

      @Override
      public void handleHealing(float healingDone, float healthPerMax, @Nullable ServerPlayer player) {
         this.syncMultiplier(healthPerMax);
      }

      @Override
      public void update(Player player) {
         this.update(player, -1.0F);
      }

      @Override
      public void update(Player player, float healthPerMax) {
         if (this.isEnabled.getAsBoolean()) {
            if (this.activeMultiplier == 0) {
               this.ticks = 0;
            } else {
               if (this.ticks == 0) {
                  if (healthPerMax != -1.0F) {
                     this.syncMultiplier(healthPerMax);
                  }
                  if (this.activeMultiplier != 0) {
                     player.addEffect(new MobEffectInstance(this.effect, 169, this.activeMultiplier - 1, false, false));
                  }
               }
               this.ticks++;
               if (this.ticks >= 79) {
                  this.ticks = 0;
               }
            }
         }
      }
   }
}
