package ichttt.mods.firstaid.mixin;

import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidHooks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * First Aid: 1.12 cancelled LivingHurtEvent (fired in EntityPlayer.damageEntity after the invulnerability check, before armor) and
 * did the part damage itself; here at the head of Player.actuallyHurt. The absorption hooks are DataManagerWrapper's ABSORPTION handling.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
   @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
   private void firstaid$actuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
      Player self = (Player)(Object)this;
      if (!self.isInvulnerableTo(source) && EventHandler.onLivingHurt(self, source, amount)) {
         ci.cancel();
      }
   }

   @Inject(method = "internalSetAbsorptionAmount", at = @At("HEAD"))
   private void firstaid$setAbsorption(float amount, CallbackInfo ci) {
      FirstAidHooks.onSetAbsorption((Player)(Object)this, amount);
   }

   @Inject(method = "getAbsorptionAmount", at = @At("HEAD"), cancellable = true)
   private void firstaid$getAbsorption(CallbackInfoReturnable<Float> cir) {
      Float v = FirstAidHooks.getAbsorption((Player)(Object)this);
      if (v != null) {
         cir.setReturnValue(v);
      }
   }
}
