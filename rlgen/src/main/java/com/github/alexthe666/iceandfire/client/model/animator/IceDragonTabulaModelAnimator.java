package com.github.alexthe666.iceandfire.client.model.animator;

import com.github.alexthe666.iceandfire.client.model.util.EnumDragonAnimations;
import com.github.alexthe666.iceandfire.client.model.util.IIceAndFireTabulaModelAnimator;
import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.ReversedBuffer;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.model.tools.ChainBuffer;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;

public class IceDragonTabulaModelAnimator extends IceAndFireTabulaModelAnimator implements IIceAndFireTabulaModelAnimator<EntityIceDragon> {
   public IceDragonTabulaModelAnimator() {
      super(EnumDragonAnimations.GROUND_POSE.icedragon_model);
   }

   public void setRotationAngles(
      IceAndFireTabulaModel model,
      EntityIceDragon entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float rotationYaw,
      float rotationPitch,
      float scale
   ) {
      model.resetToDefaultPose();
      model.llibAnimator.update(entity);
      this.animate(model, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);
      IceAndFireTabulaModel[] walkPoses = new IceAndFireTabulaModel[]{
         EnumDragonAnimations.WALK1.icedragon_model,
         EnumDragonAnimations.WALK2.icedragon_model,
         EnumDragonAnimations.WALK3.icedragon_model,
         EnumDragonAnimations.WALK4.icedragon_model
      };
      IceAndFireTabulaModel[] flyPoses = new IceAndFireTabulaModel[]{
         EnumDragonAnimations.FLIGHT1.icedragon_model,
         EnumDragonAnimations.FLIGHT2.icedragon_model,
         EnumDragonAnimations.FLIGHT3.icedragon_model,
         EnumDragonAnimations.FLIGHT4.icedragon_model,
         EnumDragonAnimations.FLIGHT5.icedragon_model,
         EnumDragonAnimations.FLIGHT6.icedragon_model
      };
      boolean walking = !entity.isFlying() && !entity.isHovering() && (entity.hoverProgress <= 0.0F || entity.flyProgress <= 0.0F);
      int currentIndex = walking ? entity.walkCycle / 10 : entity.flightCycle / 10;
      int prevIndex = currentIndex - 1;
      if (prevIndex < 0) {
         prevIndex = walking ? 3 : 5;
      }

      IceAndFireTabulaModel prevPosition = walking ? walkPoses[prevIndex] : flyPoses[prevIndex];
      IceAndFireTabulaModel currentPosition = walking ? walkPoses[currentIndex] : flyPoses[currentIndex];
      float delta = (float)(walking ? entity.walkCycle : entity.flightCycle) / 10.0F % 1.0F + LLibrary.PROXY.getPartialTicks() / 10.0F;
      AdvancedModelRenderer[] neckParts = new AdvancedModelRenderer[]{
         model.getCube("Neck1"), model.getCube("Neck2"), model.getCube("Neck3"), model.getCube("Neck3"), model.getCube("Head")
      };
      AdvancedModelRenderer[] tailParts = new AdvancedModelRenderer[]{
         model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")
      };
      AdvancedModelRenderer[] tailPartsWBody = new AdvancedModelRenderer[]{
         model.getCube("BodyLower"), model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")
      };
      AdvancedModelRenderer[] toesPartsL = new AdvancedModelRenderer[]{model.getCube("ToeL1"), model.getCube("ToeL2"), model.getCube("ToeL3")};
      AdvancedModelRenderer[] toesPartsR = new AdvancedModelRenderer[]{model.getCube("ToeR1"), model.getCube("ToeR2"), model.getCube("ToeR3")};

      for (AdvancedModelRenderer cube : model.getCubes().values()) {
         if (entity.swimProgress <= 0.0F
            && prevPosition.getCube(cube.boxName) != null
            && walking
            && entity.flyProgress <= 0.0F
            && entity.hoverProgress <= 0.0F
            && entity.hoverProgress <= 0.0F
            && entity.modelDeadProgress <= 0.0F) {
            float prevX = prevPosition.getCube(cube.boxName).rotateAngleX;
            float prevY = prevPosition.getCube(cube.boxName).rotateAngleY;
            float prevZ = prevPosition.getCube(cube.boxName).rotateAngleZ;
            float x = currentPosition.getCube(cube.boxName).rotateAngleX;
            float y = currentPosition.getCube(cube.boxName).rotateAngleY;
            float z = currentPosition.getCube(cube.boxName).rotateAngleZ;
            if (cube.boxName.equals("Neck3")) {
               prevX = (float)Math.toRadians(6.04);
               x = (float)Math.toRadians(6.04);
            }

            this.addToRotateAngle(
               cube, limbSwingAmount, prevX + delta * this.distance(prevX, x), prevY + delta * this.distance(prevY, y), prevZ + delta * this.distance(prevZ, z)
            );
         }

         if (entity.modelDeadProgress > 0.0F) {
            if (!this.isPartEqual(cube, EnumDragonAnimations.DEAD.icedragon_model.getCube(cube.boxName))) {
               this.transitionTo(
                  cube,
                  EnumDragonAnimations.DEAD.icedragon_model.getCube(cube.boxName),
                  entity.modelDeadProgress,
                  20.0F,
                  cube.boxName.equals("ThighR") || cube.boxName.equals("ThighL")
               );
            }

            if (cube.boxName.equals("BodyUpper")) {
               cube.rotationPointY = cube.rotationPointY + (18.0F - cube.rotationPointY) / 20.0F * entity.modelDeadProgress;
            }
         }

         if (entity.sleepProgress > 0.0F) {
            if (!this.isPartEqual(cube, EnumDragonAnimations.SLEEPING_POSE.icedragon_model.getCube(cube.boxName))) {
               this.transitionTo(cube, EnumDragonAnimations.SLEEPING_POSE.icedragon_model.getCube(cube.boxName), entity.sleepProgress, 20.0F, false);
            }

            if (cube.boxName.equals("BodyUpper")) {
               cube.rotationPointY = cube.rotationPointY + (18.0F - cube.rotationPointY) / 20.0F * entity.sleepProgress;
            }
         }

         if (entity.hoverProgress > 0.0F
            && !this.isPartEqual(cube, EnumDragonAnimations.HOVERING_POSE.icedragon_model.getCube(cube.boxName))
            && !this.isWing(model, cube)) {
            this.transitionTo(cube, EnumDragonAnimations.HOVERING_POSE.icedragon_model.getCube(cube.boxName), entity.hoverProgress, 20.0F, false);
         }

         if (entity.flyProgress > 0.0F && !this.isPartEqual(cube, EnumDragonAnimations.FLYING_POSE.icedragon_model.getCube(cube.boxName))) {
            this.transitionTo(cube, EnumDragonAnimations.FLYING_POSE.icedragon_model.getCube(cube.boxName), entity.flyProgress, 20.0F, false);
         }

         if (entity.swimProgress > 0.0F) {
            if (!this.isPartEqual(cube, EnumDragonAnimations.SWIM_POSE.icedragon_model.getCube(cube.boxName))) {
               this.transitionTo(cube, EnumDragonAnimations.SWIM_POSE.icedragon_model.getCube(cube.boxName), entity.swimProgress, 20.0F, false);
            }

            if (cube.boxName.equals("BodyUpper")) {
               cube.rotationPointY = cube.rotationPointY + (18.0F - cube.rotationPointY) / 20.0F * entity.swimProgress;
            }
         }

         if (entity.sitProgress > 0.0F && !entity.isPassenger()) {
            if (!this.isPartEqual(cube, EnumDragonAnimations.SITTING_POSE.icedragon_model.getCube(cube.boxName))) {
               this.transitionTo(cube, EnumDragonAnimations.SITTING_POSE.icedragon_model.getCube(cube.boxName), entity.sitProgress, 20.0F, false);
            }

            if (cube.boxName.equals("BodyUpper")) {
               cube.rotationPointY = cube.rotationPointY + (15.0F - cube.rotationPointY) / 20.0F * entity.sitProgress;
            }
         }

         if (entity.ridingProgress > 0.0F
            && EnumDragonAnimations.SIT_ON_PLAYER_POSE.icedragon_model.getCube(cube.boxName) != null
            && !this.isPartEqual(cube, EnumDragonAnimations.SIT_ON_PLAYER_POSE.icedragon_model.getCube(cube.boxName))) {
            this.transitionTo(cube, EnumDragonAnimations.SIT_ON_PLAYER_POSE.icedragon_model.getCube(cube.boxName), entity.ridingProgress, 20.0F, false);
            if (cube.boxName.equals("BodyUpper")) {
               cube.rotationPointZ = (float)(
                  (double)cube.rotationPointZ - (Math.toRadians(-15.0) - (double)cube.rotationPointZ) / 20.0 * (double)entity.ridingProgress
               );
               cube.rotationPointY = cube.rotationPointY + (15.0F - cube.rotationPointY) / 20.0F * entity.ridingProgress;
            }
         }

         if (entity.tackleProgress > 0.0F
            && EnumDragonAnimations.TACKLE.icedragon_model.getCube(cube.boxName) != null
            && !this.isPartEqual(
               EnumDragonAnimations.TACKLE.icedragon_model.getCube(cube.boxName),
               EnumDragonAnimations.FLYING_POSE.icedragon_model.getCube(cube.boxName)
            )
            && !this.isWing(model, cube)) {
            this.transitionTo(cube, EnumDragonAnimations.TACKLE.icedragon_model.getCube(cube.boxName), entity.tackleProgress, 5.0F, false);
         }

         if (entity.fireBreathProgress > 0.0F
            && !this.isPartEqual(cube, EnumDragonAnimations.STREAM_BREATH.icedragon_model.getCube(cube.boxName))
            && !this.isWing(model, cube)
            && !cube.boxName.contains("Finger")) {
            this.transitionTo(cube, EnumDragonAnimations.STREAM_BREATH.icedragon_model.getCube(cube.boxName), entity.fireBreathProgress, 20.0F, false);
         }

         if (!walking && prevPosition.getCube(cube.boxName) != null) {
            AdvancedModelRenderer flightPart = EnumDragonAnimations.FLYING_POSE.icedragon_model.getCube(cube.boxName);
            float prevX = prevPosition.getCube(cube.boxName).rotateAngleX;
            float prevY = prevPosition.getCube(cube.boxName).rotateAngleY;
            float prevZ = prevPosition.getCube(cube.boxName).rotateAngleZ;
            float x = currentPosition.getCube(cube.boxName).rotateAngleX;
            float y = currentPosition.getCube(cube.boxName).rotateAngleY;
            float z = currentPosition.getCube(cube.boxName).rotateAngleZ;
            if (cube.boxName.equals("Head")) {
               prevX = (float)Math.toRadians(6.04);
               x = (float)Math.toRadians(6.04);
            }

            if (x != flightPart.rotateAngleX || y != flightPart.rotateAngleY || z != flightPart.rotateAngleZ) {
               this.setRotateAngle(
                  cube, prevX + delta * this.distance(prevX, x), prevY + delta * this.distance(prevY, y), prevZ + delta * this.distance(prevZ, z)
               );
            }
         }
      }

      float speed_walk = 0.2F;
      float speed_idle = 0.05F;
      float speed_fly = 0.2F;
      float degree_walk = 0.5F;
      float degree_idle = 0.5F;
      float degree_fly = 0.5F;
      if (!entity.isNoAi()) {
         if (entity.isSwimming()) {
            model.bob(model.getCube("BodyUpper"), -speed_fly, degree_fly * 5.0F, false, limbSwing, limbSwingAmount);
            model.walk(model.getCube("ThighR"), -speed_fly, degree_fly * 0.1F, false, 0.0F, 0.0F, limbSwing, limbSwingAmount);
            model.walk(model.getCube("ThighL"), -speed_fly, degree_fly * 0.1F, true, 0.0F, 0.0F, limbSwing, limbSwingAmount);
            model.flap(model.getCube("ArmR1"), -speed_fly, degree_fly * 0.6F, false, 0.0F, 0.0F, limbSwing, limbSwingAmount);
            model.flap(model.getCube("ArmL1"), -speed_fly, degree_fly * 0.6F, true, 0.0F, 0.0F, limbSwing, limbSwingAmount);
            model.chainWave(neckParts, speed_fly, degree_fly * 0.15F, -2.0, limbSwing, limbSwingAmount);
            model.chainWave(tailPartsWBody, speed_fly, degree_fly * 0.3F, -2.0, limbSwing, limbSwingAmount);
         } else if (!walking) {
            model.bob(model.getCube("BodyUpper"), -speed_fly, degree_fly * 5.0F, false, (float)entity.tickCount, 1.0F);
            model.walk(model.getCube("BodyUpper"), -speed_fly, degree_fly * 0.1F, false, 0.0F, 0.0F, (float)entity.tickCount, 1.0F);
            model.chainWave(tailPartsWBody, speed_fly, degree_fly * -0.1F, 0.0, (float)entity.tickCount, 1.0F);
            model.chainWave(neckParts, speed_fly, degree_fly * 0.2F, -4.0, (float)entity.tickCount, 1.0F);
            model.chainWave(toesPartsL, speed_fly, degree_fly * 0.2F, -2.0, (float)entity.tickCount, 1.0F);
            model.chainWave(toesPartsR, speed_fly, degree_fly * 0.2F, -2.0, (float)entity.tickCount, 1.0F);
            model.walk(model.getCube("ThighR"), -speed_fly, degree_fly * 0.1F, false, 0.0F, 0.0F, (float)entity.tickCount, 1.0F);
            model.walk(model.getCube("ThighL"), -speed_fly, degree_fly * 0.1F, true, 0.0F, 0.0F, (float)entity.tickCount, 1.0F);
         } else {
            model.bob(model.getCube("BodyUpper"), speed_walk * 2.0F, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(model.getCube("ThighR"), speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(model.getCube("ThighL"), speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.chainSwing(tailParts, speed_walk, degree_walk * 0.25F, -2.0, limbSwing, limbSwingAmount);
            model.chainWave(tailParts, speed_walk, degree_walk * 0.15F, 2.0, limbSwing, limbSwingAmount);
            model.chainSwing(neckParts, speed_walk, degree_walk * 0.15F, 2.0, limbSwing, limbSwingAmount);
            model.chainWave(neckParts, speed_walk, degree_walk * 0.05F, -2.0, limbSwing, limbSwingAmount);
            model.chainSwing(tailParts, speed_idle, degree_idle * 0.25F, -2.0, (float)entity.tickCount, 1.0F);
            model.chainWave(tailParts, speed_idle, degree_idle * 0.15F, -2.0, (float)entity.tickCount, 1.0F);
            model.chainWave(neckParts, speed_idle, degree_idle * -0.15F, -3.0, (float)entity.tickCount, 1.0F);
            model.walk(model.getCube("Neck1"), speed_idle, degree_idle * 0.05F, false, 0.0F, 0.0F, (float)entity.tickCount, 1.0F);
         }

         model.bob(model.getCube("BodyUpper"), speed_idle, degree_idle * 1.3F, false, (float)entity.tickCount, 1.0F);
         model.bob(model.getCube("ThighR"), speed_idle, -degree_idle * 1.3F, false, (float)entity.tickCount, 1.0F);
         model.bob(model.getCube("ThighL"), speed_idle, -degree_idle * 1.3F, false, (float)entity.tickCount, 1.0F);
         model.bob(model.getCube("ArmR1"), speed_idle, -degree_idle * 1.3F, false, (float)entity.tickCount, 1.0F);
         model.bob(model.getCube("ArmL1"), speed_idle, -degree_idle * 1.3F, false, (float)entity.tickCount, 1.0F);
         if (entity.getAnimation() != EntityDragonBase.ANIMATION_SHAKEPREY || entity.getAnimation() != EntityDragonBase.ANIMATION_ROAR) {
            model.faceTarget(rotationYaw, rotationPitch, 4.0F, neckParts);
         }
      }

      // the buffers live on the entity (created and ticked client side by DragonClientHooks)
      if (!entity.isModelDead() && entity.turn_buffer instanceof ReversedBuffer turn && entity.tail_buffer instanceof ChainBuffer tail) {
         turn.applyChainSwingBuffer(neckParts);
         tail.applyChainSwingBuffer(tailPartsWBody);
      }
   }

   private boolean isWing(IceAndFireTabulaModel model, AdvancedModelRenderer modelRenderer) {
      return model.getCube("ArmL1").childModels.contains(modelRenderer) || model.getCube("ArmR1").childModels.contains(modelRenderer);
   }

   public void animate(
      IceAndFireTabulaModel model,
      EntityIceDragon entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float rotationYaw,
      float rotationPitch,
      float scale
   ) {
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_SPEAK);
      model.llibAnimator.startKeyframe(5);
      this.rotate(model.llibAnimator, model.getCube("JawBottom"), 25.0F, 0.0F, 0.0F);
      model.llibAnimator.move(model.getCube("JawBottom"), 0.0F, 0.0F, 0.2F);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.setStaticKeyframe(5);
      model.llibAnimator.startKeyframe(5);
      this.rotate(model.llibAnimator, model.getCube("JawBottom"), 25.0F, 0.0F, 0.0F);
      model.llibAnimator.move(model.getCube("JawBottom"), 0.0F, 0.0F, 0.2F);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(5);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_BITE);
      model.llibAnimator.startKeyframe(15);
      this.rotate(model.llibAnimator, model.getCube("Neck1"), -12.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Neck2"), -5.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Neck3"), 5.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Head"), 36.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("JawBottom"), 30.0F, 0.0F, 0.0F);
      model.llibAnimator.move(model.getCube("JawBottom"), 0.0F, 0.0F, 0.2F);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.rotate(model.llibAnimator, model.getCube("Neck1"), -2.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Neck2"), 10.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Neck3"), 10.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Head"), 20.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("JawBottom"), 30.0F, 0.0F, 0.0F);
      model.llibAnimator.move(model.getCube("JawBottom"), 0.0F, 0.0F, 0.2F);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_SHAKEPREY);
      model.llibAnimator.startKeyframe(15);
      this.moveToPose(model, EnumDragonAnimations.GRAB1.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.GRAB2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.GRAB3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.GRAB4.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_TAILWHACK);
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.TAIL_WHIP1.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.TAIL_WHIP2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.TAIL_WHIP3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_FIRECHARGE);
      model.llibAnimator.startKeyframe(15);
      this.moveToPose(model, EnumDragonAnimations.STREAM_CHARGE.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_WINGBLAST);
      model.llibAnimator.startKeyframe(10);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST1.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(5);
      this.moveToPose(model, EnumDragonAnimations.WING_BLAST3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
      model.llibAnimator.setAnimation(EntityIceDragon.ANIMATION_ROAR);
      model.llibAnimator.startKeyframe(10);
      this.rotate(model.llibAnimator, model.getCube("Neck1"), -5.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Head"), -5.0F, 0.0F, 0.0F);
      this.moveToPose(model, EnumDragonAnimations.ROAR1.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.rotate(model.llibAnimator, model.getCube("Neck1"), -5.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Head"), -5.0F, 0.0F, 0.0F);
      this.moveToPose(model, EnumDragonAnimations.ROAR2.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.startKeyframe(10);
      this.rotate(model.llibAnimator, model.getCube("Neck1"), -5.0F, 0.0F, 0.0F);
      this.rotate(model.llibAnimator, model.getCube("Head"), -5.0F, 0.0F, 0.0F);
      this.moveToPose(model, EnumDragonAnimations.ROAR3.icedragon_model);
      model.llibAnimator.endKeyframe();
      model.llibAnimator.resetKeyframe(10);
   }
}
