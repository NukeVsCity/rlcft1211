package net.ilexiconn.llibrary.client.model.tabula;

import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** LLibrary 1.7.20 ITabulaModelAnimator. */
@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface ITabulaModelAnimator<T extends Entity> {
   void setRotationAngles(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale);
}
