package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * 1.12 DataManagerWrapper (wrapped every player's EntityDataManager to watch HEALTH and ABSORPTION) and PotionPoisonPatched,
 * called from the mixins.
 */
public final class FirstAidHooks {
   private FirstAidHooks() {
   }

   /** DataManagerWrapper.set(HEALTH, value): true when the vanilla write must be skipped. */
   public static boolean onSetHealth(Player player, float rawValue) {
      if (player instanceof FakePlayer || !player.hasData(FirstAid.DAMAGE_MODEL) && player.tickCount == 0) {
         return false;
      }
      float aFloat = Mth.clamp(rawValue, 0.0F, player.getMaxHealth()); // 1.12 setHealth clamped before the data manager
      if (!FirstAid.isTracking(player)) {
         return true; // untracked writes of HEALTH are dropped
      }
      if (player.level().isClientSide) {
         return false;
      }
      PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
      if (aFloat > player.getMaxHealth()) {
         for (DamageablePart part : damageModel) {
            part.currentHealth = (float)part.getMaxHealth();
         }
         return false;
      }
      if (damageModel.isWaitingForHelp()) {
         return false;
      }
      if (FirstAidConfig.watchSetHealth && !Float.isInfinite(aFloat) && !Float.isNaN(aFloat) && aFloat > 0.0F && player instanceof ServerPlayer sp
         && sp.connection != null) {
         float orig = player.getHealth();
         if (orig > 0.0F && !Float.isNaN(orig) && !Float.isInfinite(orig)) {
            if (FirstAidConfig.scaleMaxHealth) {
               orig = Math.min(orig, (float)player.getAttributeValue(Attributes.MAX_HEALTH));
            }
            float healed = aFloat - orig;
            if ((double)Math.abs(healed) > 0.001) {
               if (healed < 0.0F) {
                  DamageDistribution.handleDamageTaken(RandomDamageDistribution.getDefault(), damageModel, -healed, player, player.damageSources().magic(), true, true);
               } else {
                  HealthDistribution.addRandomHealth(healed, player, true);
               }
            }
            return true;
         }
      }
      return false;
   }

   /** DataManagerWrapper.set(ABSORPTION) */
   public static void onSetAbsorption(Player player, float value) {
      if (player instanceof FakePlayer || !FirstAid.isTracking(player)) {
         return;
      }
      if (player instanceof ServerPlayer sp && sp.connection != null) {
         FirstAidNetwork.applyAbsorption(sp, value);
      }
      FirstAid.getDamageModel(player).setAbsorption(value);
   }

   /** DataManagerWrapper.get(ABSORPTION): the vanilla value always follows the parts */
   @Nullable
   public static Float getAbsorption(Player player) {
      if (player instanceof FakePlayer) {
         return null;
      }
      return FirstAid.getDamageModel(player).getAbsorption();
   }

   /** PotionPoisonPatched.performEffect: true when handled (player with a critical part) */
   public static boolean poison(LivingEntity entity) {
      if (entity instanceof Player player && !(player instanceof FakePlayer)
         && (FirstAidConfig.damageSystem.causeDeathBody || FirstAidConfig.damageSystem.causeDeathHead)) {
         DamageSource magic = player.damageSources().magic();
         if (player.level().isClientSide || !player.isAlive() || player.isInvulnerableTo(magic)) {
            return true;
         }
         PlayerDamageModel model = FirstAid.getDamageModel(player);
         if (DamageDistribution.handleDamageTaken(RandomDamageDistribution.ANY_NOKILL, model, 1.0F, player, magic, true, false) != 1.0F) {
            if (player.isSleeping()) {
               player.stopSleepInBed(true, false);
            }
            var acc = (ichttt.mods.firstaid.mixin.LivingEntityAccessor)player;
            net.minecraft.sounds.SoundEvent sound = acc.firstaid$getHurtSound(magic);
            if (sound != null) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), acc.firstaid$getSoundVolume(),
                  player.getVoicePitch());
            }
         }
         return true;
      }
      return false;
   }
}
