package com.github.alexthe666.iceandfire.client.model.animator;

import com.github.alexthe666.iceandfire.client.model.util.EnumSeaSerpentAnimations;
import com.github.alexthe666.iceandfire.client.model.util.IIceAndFireTabulaModelAnimator;
import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;

public class SeaSerpentTabulaModelAnimator extends IceAndFireTabulaModelAnimator implements IIceAndFireTabulaModelAnimator<EntitySeaSerpent> {
   public SeaSerpentTabulaModelAnimator() {
      super(EnumSeaSerpentAnimations.T_POSE.seaserpent_model);
   }

   public void setRotationAngles(
      IceAndFireTabulaModel model,
      EntitySeaSerpent entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float rotationYaw,
      float rotationPitch,
      float scale
   ) {
      model.resetToDefaultPose();
      StoneEntityProperties properties = (StoneEntityProperties)EntityPropertiesHandler.INSTANCE.getProperties(entity, StoneEntityProperties.class);
      if (properties == null || !properties.isStone) {
         model.llibAnimator.update(entity);
         this.animate(model, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);
         IceAndFireTabulaModel[] swimPose = new IceAndFireTabulaModel[]{
            EnumSeaSerpentAnimations.SWIM1.seaserpent_model,
            EnumSeaSerpentAnimations.SWIM3.seaserpent_model,
            EnumSeaSerpentAnimations.SWIM4.seaserpent_model,
            EnumSeaSerpentAnimations.SWIM6.seaserpent_model
         };
         int currentIndex = entity.swimCycle / 10;
         int prevIndex = currentIndex - 1;
         if (prevIndex < 0) {
            prevIndex = 3;
         }

         IceAndFireTabulaModel prevPosition = swimPose[prevIndex];
         IceAndFireTabulaModel currentPosition = swimPose[currentIndex];
         float delta = (float)entity.swimCycle / 10.0F % 1.0F + LLibrary.PROXY.getPartialTicks() / 10.0F;
         AdvancedModelRenderer[] tailParts = new AdvancedModelRenderer[]{
            model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4"), model.getCube("Tail5"), model.getCube("Tail6")
         };
         AdvancedModelRenderer[] neckParts = new AdvancedModelRenderer[]{
            model.getCube("Neck1"), model.getCube("Neck2"), model.getCube("Neck3"), model.getCube("Head")
         };

         for (AdvancedModelRenderer cube : model.getCubes().values()) {
            if (entity.jumpProgress > 0.0F && !this.isPartEqual(cube, EnumSeaSerpentAnimations.JUMPING2.seaserpent_model.getCube(cube.boxName))) {
               this.transitionTo(cube, EnumSeaSerpentAnimations.JUMPING2.seaserpent_model.getCube(cube.boxName), entity.jumpProgress, 5.0F, false);
            }

            if (entity.wantJumpProgress > 0.0F && !this.isPartEqual(cube, EnumSeaSerpentAnimations.JUMPING1.seaserpent_model.getCube(cube.boxName))) {
               this.transitionTo(cube, EnumSeaSerpentAnimations.JUMPING1.seaserpent_model.getCube(cube.boxName), entity.wantJumpProgress, 10.0F, false);
            }

            float prevX = prevPosition.getCube(cube.boxName).rotateAngleX;
            float prevY = prevPosition.getCube(cube.boxName).rotateAngleY;
            float prevZ = prevPosition.getCube(cube.boxName).rotateAngleZ;
            float x = currentPosition.getCube(cube.boxName).rotateAngleX;
            float y = currentPosition.getCube(cube.boxName).rotateAngleY;
            float z = currentPosition.getCube(cube.boxName).rotateAngleZ;
            this.addToRotateAngle(
               cube, limbSwingAmount, prevX + delta * this.distance(prevX, x), prevY + delta * this.distance(prevY, y), prevZ + delta * this.distance(prevZ, z)
            );
         }

         if (entity.breathProgress > 0.0F) {
            this.progressRotation(model.getCube("Head"), entity.breathProgress, (float)Math.toRadians(-15.0), 0.0F, 0.0F);
            this.progressRotation(model.getCube("HeadFront"), entity.breathProgress, (float)Math.toRadians(-28.0), 0.0F, 0.0F);
            this.progressRotation(model.getCube("Jaw"), entity.breathProgress, (float)Math.toRadians(80.0), 0.0F, 0.0F);
         }

         if (entity.jumpRot > 0.0F) {
            float turn = (float)entity.getDeltaMovement().y * -4.0F;
            AdvancedModelRenderer var10000 = model.getCube("BodyUpper");
            var10000.rotateAngleX = var10000.rotateAngleX + (float)Math.toRadians((double)(22.5F * turn)) * entity.jumpRot;
            var10000 = model.getCube("Tail1");
            var10000.rotateAngleX = var10000.rotateAngleX - (float)Math.toRadians((double)turn) * entity.jumpRot;
            var10000 = model.getCube("Tail2");
            var10000.rotateAngleX = var10000.rotateAngleX - (float)Math.toRadians((double)turn) * entity.jumpRot;
            var10000 = model.getCube("Tail3");
            var10000.rotateAngleX = var10000.rotateAngleX - (float)Math.toRadians((double)turn) * entity.jumpRot;
            var10000 = model.getCube("Tail4");
            var10000.rotateAngleX = var10000.rotateAngleX - (float)Math.toRadians((double)turn) * entity.jumpRot;
         }

         entity.tail_buffer.applyChainSwingBuffer(tailParts);
         entity.roll_buffer.applyChainFlapBuffer(model.getCube("BodyUpper"));
         entity.pitch_buffer.applyChainWaveBuffer(model.getCube("BodyUpper"));
         entity.head_buffer.applyChainSwingBufferReverse(neckParts);
      }
   }

   public void progressRotation(AdvancedModelRenderer model, float progress, float rotX, float rotY, float rotZ) {
      model.rotateAngleX = model.rotateAngleX + progress * (rotX - model.defaultRotationX) / 20.0F;
      model.rotateAngleY = model.rotateAngleY + progress * (rotY - model.defaultRotationY) / 20.0F;
      model.rotateAngleZ = model.rotateAngleZ + progress * (rotZ - model.defaultRotationZ) / 20.0F;
   }

   private void animate(
      IceAndFireTabulaModel model,
      EntitySeaSerpent entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float rotationYaw,
      float rotationPitch,
      float scale
   ) {
      model.llibAnimator.setAnimation(EntitySeaSerpent.ANIMATION_SPEAK);
      model.llibAnimator.startKeyframe(5);
      this.rotate(model.llibAnimator, model.getCube("Jaw"), 25.0F, 0.0F, 0.0F);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.setStaticKeyframe(5);
      model.llibAnimator.resetKeyframe(5);
      model.llibAnimator.setAnimation(EntitySeaSerpent.ANIMATION_BITE);
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumSeaSerpentAnimations.BITE1.seaserpent_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumSeaSerpentAnimations.BITE2.seaserpent_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.setStaticKeyframe(2);
      model.llibAnimator.resetKeyframe(3);
      model.llibAnimator.setAnimation(EntitySeaSerpent.ANIMATION_ROAR);
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumSeaSerpentAnimations.ROAR1.seaserpent_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumSeaSerpentAnimations.ROAR2.seaserpent_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumSeaSerpentAnimations.ROAR3.seaserpent_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
   }
}
