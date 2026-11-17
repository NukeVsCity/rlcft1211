package ichttt.mods.firstaid.mixin;

import ichttt.mods.firstaid.common.FirstAidHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** First Aid 1.12 PotionPoisonPatched (registered over minecraft:poison): poison on players never kills a critical part. */
@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public abstract class PoisonMobEffectMixin {
   @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
   private void firstaid$poison(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
      if (FirstAidHooks.poison(entity)) {
         cir.setReturnValue(true);
      }
   }
}
