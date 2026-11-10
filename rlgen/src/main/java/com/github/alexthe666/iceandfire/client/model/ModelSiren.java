// Ice and Fire 1.7.1 model, ported to the llibrary 1.12 model classes.
package com.github.alexthe666.iceandfire.client.model;

import com.github.alexthe666.iceandfire.entity.EntitySiren;
import net.ilexiconn.llibrary.client.model.ModelAnimator;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.world.entity.Entity;

public class ModelSiren extends ModelDragonBase {
   public AdvancedModelRenderer Tail_1;
   public AdvancedModelRenderer Tail_2;
   public AdvancedModelRenderer Body;
   public AdvancedModelRenderer Fin1;
   public AdvancedModelRenderer Tail_3;
   public AdvancedModelRenderer Fin2;
   public AdvancedModelRenderer FlukeL;
   public AdvancedModelRenderer FlukeR;
   public AdvancedModelRenderer Fin3;
   public AdvancedModelRenderer Left_Arm;
   public AdvancedModelRenderer Head;
   public AdvancedModelRenderer Right_Arm;
   public AdvancedModelRenderer Neck;
   public AdvancedModelRenderer Hair1;
   public AdvancedModelRenderer HairR;
   public AdvancedModelRenderer HairL;
   public AdvancedModelRenderer Mouth;
   public AdvancedModelRenderer Jaw;
   public AdvancedModelRenderer Hair2;
   private ModelAnimator animator;

   public ModelSiren() {
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.Left_Arm = new AdvancedModelRenderer(this, 40, 16);
      this.Left_Arm.mirror = true;
      this.Left_Arm.setRotationPoint(5.0F, -10.0F, 0.0F);
      this.Left_Arm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
      this.setRotateAngle(this.Left_Arm, (float) (-Math.PI * 2.0 / 9.0), 0.0F, 0.0F);
      this.HairR = new AdvancedModelRenderer(this, 80, 16);
      this.HairR.setRotationPoint(-1.8F, -7.8F, 3.2F);
      this.HairR.addBox(-1.9F, -10.7F, -0.3F, 2, 11, 4, 0.0F);
      this.setRotateAngle(this.HairR, -2.5830872F, 0.0F, 0.08726646F);
      this.Mouth = new AdvancedModelRenderer(this, 38, 0);
      this.Mouth.setRotationPoint(0.5F, -1.3F, 0.0F);
      this.Mouth.addBox(-2.5F, -0.6F, -4.6F, 4, 3, 2, 0.0F);
      this.setRotateAngle(this.Mouth, -0.3642502F, 0.0F, 0.0F);
      this.Fin2 = new AdvancedModelRenderer(this, 72, 34);
      this.Fin2.setRotationPoint(0.0F, 5.8F, 1.9F);
      this.Fin2.addBox(-1.0F, -5.5F, 0.8F, 1, 11, 4, 0.0F);
      this.Tail_3 = new AdvancedModelRenderer(this, 52, 34);
      this.Tail_3.setRotationPoint(0.0F, 10.4F, 0.1F);
      this.Tail_3.addBox(-3.0F, 0.0F, -1.9F, 6, 13, 4, 0.0F);
      this.Neck = new AdvancedModelRenderer(this, 40, 8);
      this.Neck.setRotationPoint(0.0F, -12.0F, 0.0F);
      this.Neck.addBox(-3.0F, -3.7F, -1.0F, 6, 4, 1, 0.0F);
      this.Hair2 = new AdvancedModelRenderer(this, 81, 16);
      this.Hair2.setRotationPoint(0.0F, -1.5F, 2.9F);
      this.Hair2.addBox(-3.5F, -11.9F, 0.2F, 7, 11, 3, 0.0F);
      this.setRotateAngle(this.Hair2, -0.22759093F, 0.0F, 0.0F);
      this.Fin3 = new AdvancedModelRenderer(this, 72, 15);
      this.Fin3.setRotationPoint(0.0F, 6.1F, 1.9F);
      this.Fin3.addBox(-0.9F, -5.5F, 0.3F, 1, 13, 3, 0.0F);
      this.Fin1 = new AdvancedModelRenderer(this, 84, 34);
      this.Fin1.setRotationPoint(0.0F, 6.1F, 1.9F);
      this.Fin1.addBox(-1.0F, -5.4F, 0.8F, 1, 11, 3, 0.0F);
      this.Tail_1 = new AdvancedModelRenderer(this, 0, 35);
      this.Tail_1.setRotationPoint(0.0F, 22.2F, -0.2F);
      this.Tail_1.addBox(-4.0F, -0.1F, -1.8F, 8, 11, 5, 0.1F);
      this.setRotateAngle(this.Tail_1, (float) (Math.PI / 2), 0.0F, 0.0F);
      this.Head = new AdvancedModelRenderer(this, 0, 0);
      this.Head.setRotationPoint(0.0F, -12.0F, 0.0F);
      this.Head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
      this.setRotateAngle(this.Head, -0.4553564F, 0.0F, 0.0F);
      this.FlukeL = new AdvancedModelRenderer(this, 106, 34);
      this.FlukeL.setRotationPoint(0.0F, 12.3F, 0.1F);
      this.FlukeL.addBox(-3.5F, -0.1F, -0.5F, 7, 11, 1, 0.0F);
      this.setRotateAngle(this.FlukeL, -0.034906585F, -0.08726646F, (float) (-Math.PI / 6));
      this.Tail_2 = new AdvancedModelRenderer(this, 27, 34);
      this.Tail_2.setRotationPoint(0.0F, 10.4F, 0.1F);
      this.Tail_2.addBox(-3.5F, 0.0F, -1.9F, 7, 11, 5, 0.0F);
      this.FlukeR = new AdvancedModelRenderer(this, 106, 34);
      this.FlukeR.mirror = true;
      this.FlukeR.setRotationPoint(0.0F, 12.3F, 0.1F);
      this.FlukeR.addBox(-3.5F, -0.1F, -0.5F, 7, 11, 1, 0.0F);
      this.setRotateAngle(this.FlukeR, -0.034906585F, 0.08726646F, (float) (Math.PI / 6));
      this.Right_Arm = new AdvancedModelRenderer(this, 40, 16);
      this.Right_Arm.setRotationPoint(-5.0F, -10.0F, 0.0F);
      this.Right_Arm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
      this.setRotateAngle(this.Right_Arm, (float) (-Math.PI * 2.0 / 9.0), 0.045553092F, 0.0F);
      this.Hair1 = new AdvancedModelRenderer(this, 80, 16);
      this.Hair1.setRotationPoint(0.0F, -7.8F, 3.2F);
      this.Hair1.addBox(-3.5F, -10.7F, -0.3F, 7, 11, 4, 0.0F);
      this.setRotateAngle(this.Hair1, -2.1855013F, 0.0F, 0.0F);
      this.Jaw = new AdvancedModelRenderer(this, 24, 0);
      this.Jaw.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.Jaw.addBox(-2.0F, -0.6F, -4.6F, 4, 1, 3, 0.0F);
      this.setRotateAngle(this.Jaw, 0.045553092F, 0.0F, 0.0F);
      this.Body = new AdvancedModelRenderer(this, 16, 16);
      this.Body.setRotationPoint(0.0F, 0.9F, 1.0F);
      this.Body.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, 0.0F);
      this.setRotateAngle(this.Body, -0.8196066F, 0.0F, 0.0F);
      this.HairL = new AdvancedModelRenderer(this, 80, 16);
      this.HairL.mirror = true;
      this.HairL.setRotationPoint(1.8F, -7.3F, 3.2F);
      this.HairL.addBox(0.1F, -10.7F, -0.3F, 2, 11, 4, 0.0F);
      this.setRotateAngle(this.HairL, -2.5830872F, 0.0F, -0.08726646F);
      this.Body.addChild(this.Left_Arm);
      this.Head.addChild(this.HairR);
      this.Head.addChild(this.Mouth);
      this.Tail_2.addChild(this.Fin2);
      this.Tail_2.addChild(this.Tail_3);
      this.Body.addChild(this.Neck);
      this.Hair1.addChild(this.Hair2);
      this.Tail_3.addChild(this.Fin3);
      this.Tail_1.addChild(this.Fin1);
      this.Body.addChild(this.Head);
      this.Tail_3.addChild(this.FlukeL);
      this.Tail_1.addChild(this.Tail_2);
      this.Tail_3.addChild(this.FlukeR);
      this.Body.addChild(this.Right_Arm);
      this.Head.addChild(this.Hair1);
      this.Head.addChild(this.Jaw);
      this.Tail_1.addChild(this.Body);
      this.Head.addChild(this.HairL);
      this.animator = ModelAnimator.create();
      this.updateDefaultPose();
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.animate((IAnimatedEntity)entity, f, f1, f2, f3, f4, f5);
      this.Tail_1.render(f5);
   }

   public void animate(IAnimatedEntity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.resetToDefaultPose();
      this.setRotationAngles(f, f1, f2, f3, f4, f5, (EntitySiren)entity);
      this.animator.update(entity);
      this.animator.setAnimation(EntitySiren.ANIMATION_BITE);
      this.animator.startKeyframe(5);
      this.rotate(this.animator, this.Mouth, -28.0F, 0.0F, 0.0F);
      this.rotate(this.animator, this.Jaw, 7.0F, 0.0F, 0.0F);
      this.animator.endKeyframe();
      this.animator.resetKeyframe(5);
      this.animator.endKeyframe();
      this.animator.setAnimation(EntitySiren.ANIMATION_PULL);
      this.animator.startKeyframe(5);
      this.rotate(this.animator, this.Left_Arm, -103.0F, 5.0F, 0.0F);
      this.rotate(this.animator, this.Right_Arm, -103.0F, -5.0F, 0.0F);
      this.animator.endKeyframe();
      this.animator.startKeyframe(5);
      this.rotate(this.animator, this.Left_Arm, 103.0F, 5.0F, 0.0F);
      this.rotate(this.animator, this.Right_Arm, 103.0F, -5.0F, 0.0F);
      this.animator.endKeyframe();
      this.animator.resetKeyframe(5);
      this.animator.endKeyframe();
   }

   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, EntitySiren entity) {
      float speed_walk = 0.6F;
      float speed_idle = 0.05F;
      float degree_walk = 1.0F;
      float degree_idle = 0.5F;
      this.faceTarget(f3, f4, 2.0F, new AdvancedModelRenderer[]{this.Neck, this.Head});
      AdvancedModelRenderer[] TAIL_NO_BASE = new AdvancedModelRenderer[]{this.Tail_2, this.Tail_3};
      this.walk(this.Hair1, speed_idle * 1.0F, degree_idle * 0.3F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
      this.walk(this.Hair2, speed_idle * 1.0F, degree_idle * 0.2F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
      this.swing(this.HairL, speed_idle, degree_idle * 0.4F, true, 0.0F, -0.4F, (float)entity.tickCount, 1.0F);
      this.swing(this.HairR, speed_idle, degree_idle * 0.4F, false, 0.0F, -0.4F, (float)entity.tickCount, 1.0F);
      this.walk(this.Body, speed_idle * 1.0F, degree_idle * 0.3F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
      this.walk(this.Right_Arm, speed_idle * 1.0F, degree_idle * 0.2F, true, 0.0F, 0.1F, (float)entity.tickCount, 1.0F);
      this.walk(this.Left_Arm, speed_idle * 1.0F, degree_idle * 0.2F, true, 0.0F, 0.1F, (float)entity.tickCount, 1.0F);
      this.walk(this.Body, speed_idle * 1.0F, degree_idle * 0.2F, false, 0.0F, -0.1F, (float)entity.tickCount, 1.0F);
      this.progressRotation(this.Body, entity.swimProgress, (float)Math.toRadians(-2.0), 0.0F, 0.0F);
      this.progressRotation(this.Head, entity.swimProgress, (float)Math.toRadians(-70.0), 0.0F, 0.0F);
      this.progressRotation(this.Left_Arm, entity.swimProgress, (float)Math.toRadians(-15.0), 0.0F, 0.0F);
      this.progressRotation(this.Right_Arm, entity.swimProgress, (float)Math.toRadians(-15.0), 0.0F, 0.0F);
      if (entity.isSirenSwimming()) {
         this.flap(this.Right_Arm, speed_walk * 1.0F, degree_walk * 1.2F, false, 0.0F, 1.2F, f, f1);
         this.flap(this.Left_Arm, speed_walk * 1.0F, degree_walk * 1.2F, true, 0.0F, 1.2F, f, f1);
         this.chainWave(TAIL_NO_BASE, speed_walk, degree_walk * 0.4F, 0.0, f, f1);
         this.walk(this.Tail_1, speed_walk * 1.0F, degree_walk * 0.2F, true, 0.0F, 0.0F, f, f1);
      } else {
         this.walk(this.Right_Arm, speed_walk * 1.0F, degree_walk * 0.4F, false, 0.0F, 0.0F, f, f1);
         this.walk(this.Left_Arm, speed_walk * 1.0F, degree_walk * 0.4F, true, 0.0F, 0.0F, f, f1);
         this.chainFlap(TAIL_NO_BASE, speed_walk, degree_walk * 0.6F, 1.0, f, f1);
         this.swing(this.Tail_1, speed_walk * 1.0F, degree_walk * 0.2F, true, 0.0F, 0.0F, f, f1);
      }

      if (entity.isSinging()) {
         switch (entity.getSingingPose()) {
            case 1:
               this.progressRotation(this.Body, entity.singProgress, (float)Math.toRadians(-57.0), 0.0F, 0.0F);
               this.progressRotation(this.Head, entity.singProgress, (float)Math.toRadians(-13.0), 0.0F, 0.0F);
               this.progressRotation(
                  this.Left_Arm, entity.singProgress, (float)Math.toRadians(-200.0), (float)Math.toRadians(-60.0), (float)Math.toRadians(70.0)
               );
               this.progressRotation(
                  this.Right_Arm, entity.singProgress, (float)Math.toRadians(-200.0), (float)Math.toRadians(60.0), (float)Math.toRadians(-70.0)
               );
               this.progressRotation(this.Tail_1, entity.singProgress, (float)Math.toRadians(70.0), 0.0F, 0.0F);
               this.progressRotation(this.Tail_2, entity.singProgress, (float)Math.toRadians(20.0), 0.0F, (float)Math.toRadians(25.0));
               this.progressRotation(this.Tail_3, entity.singProgress, 0.0F, 0.0F, (float)Math.toRadians(18.0));
               this.progressPosition(this.Tail_1, entity.singProgress, 0.0F, 18.9F, -0.2F);
               this.walk(this.Right_Arm, speed_idle * 1.5F, degree_idle * 0.6F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               this.walk(this.Left_Arm, speed_idle * 1.5F, degree_idle * 0.6F, true, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               if (entity.onGround()) {
                  this.chainFlap(TAIL_NO_BASE, speed_idle, degree_idle, 0.0, (float)entity.tickCount, 1.0F);
               }
               break;
            case 2:
               this.progressRotation(this.Body, entity.singProgress, (float)Math.toRadians(-46.0), 0.0F, 0.0F);
               this.progressRotation(this.Tail_1, entity.singProgress, (float)Math.toRadians(90.0), 0.0F, (float)Math.toRadians(20.0));
               this.progressRotation(this.Tail_2, entity.singProgress, 0.0F, (float)Math.toRadians(-13.0), 0.0F);
               this.progressRotation(this.Tail_3, entity.singProgress, 0.0F, (float)Math.toRadians(-7.0), 0.0F);
               this.progressRotation(this.Head, entity.singProgress, (float)Math.toRadians(-52.0), (float)Math.toRadians(2.0), (float)Math.toRadians(-26.0));
               this.progressRotation(
                  this.Left_Arm, entity.singProgress, (float)Math.toRadians(-40.0), (float)Math.toRadians(-28.0), (float)Math.toRadians(-26.0)
               );
               this.progressRotation(
                  this.Right_Arm, entity.singProgress, (float)Math.toRadians(13.0), (float)Math.toRadians(73.0), (float)Math.toRadians(130.0)
               );
               this.progressPosition(this.Head, entity.singProgress, 0.0F, -12.0F, -0.5F);
               this.walk(this.Right_Arm, speed_idle * 1.5F, degree_idle * 0.6F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               this.flap(this.Right_Arm, speed_idle * 1.5F, degree_idle * 0.6F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               if (entity.onGround()) {
                  this.chainFlap(TAIL_NO_BASE, speed_idle, degree_idle, 0.0, (float)entity.tickCount, 1.0F);
                  this.swing(this.Tail_2, speed_idle, degree_idle * 0.4F, false, 0.0F, -0.4F, (float)entity.tickCount, 1.0F);
                  this.swing(this.Tail_3, speed_idle, degree_idle * 0.4F, false, 0.0F, 0.6F, (float)entity.tickCount, 1.0F);
               }
               break;
            default:
               this.progressRotation(this.Body, entity.singProgress, (float)Math.toRadians(-46.0), 0.0F, (float)Math.toRadians(20.87F));
               this.progressPosition(this.Head, entity.singProgress, 0.0F, -12.0F, -0.5F);
               this.progressRotation(this.Head, entity.singProgress, (float)Math.toRadians(-54.0), 0.0F, (float)Math.toRadians(20.87F));
               this.progressRotation(this.Tail_1, entity.singProgress, (float)Math.toRadians(90.0), (float)Math.toRadians(20.87F), 0.0F);
               this.progressRotation(this.Tail_2, entity.singProgress, 0.0F, 0.0F, (float)Math.toRadians(-33.0));
               this.progressRotation(this.Tail_2, entity.singProgress, 0.0F, 0.0F, (float)Math.toRadians(-15.0));
               this.progressRotation(this.Right_Arm, entity.singProgress, (float)Math.toRadians(-40.0), (float)Math.toRadians(2.0), (float)Math.toRadians(53.0));
               this.progressRotation(this.Left_Arm, entity.singProgress, (float)Math.toRadians(-80.0), (float)Math.toRadians(-70.0), 0.0F);
               this.walk(this.Right_Arm, speed_idle * 1.5F, degree_idle * 0.6F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               this.walk(this.Left_Arm, speed_idle * 1.5F, degree_idle * 0.6F, true, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               this.flap(this.Right_Arm, speed_idle * 1.5F, degree_idle * 0.6F, false, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               this.flap(this.Left_Arm, speed_idle * 1.5F, degree_idle * 0.6F, true, 2.0F, 0.0F, (float)entity.tickCount, 1.0F);
               if (entity.onGround()) {
                  this.chainFlap(TAIL_NO_BASE, speed_idle, degree_idle * 0.5F, -1.0, (float)entity.tickCount, 1.0F);
               }
         }
      }

      entity.tail_buffer.applyChainSwingBuffer(TAIL_NO_BASE);
   }

   @Override
   public void renderStatue() {
      this.resetToDefaultPose();
      this.Tail_1.render(0.0625F);
   }
}
