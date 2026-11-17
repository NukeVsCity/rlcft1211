package ichttt.mods.firstaid.mixin;

import ichttt.mods.firstaid.common.FirstAidHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** First Aid 1.12 DataManagerWrapper.set(HEALTH): setHealth on a player becomes part damage/healing (watchSetHealth). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
   private void firstaid$setHealth(float health, CallbackInfo ci) {
      if ((Object)this instanceof Player player && FirstAidHooks.onSetHealth(player, health)) {
         ci.cancel();
      }
   }
}
