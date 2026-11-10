package com.github.alexthe666.iceandfire.client.model;

import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.util.Mth;

public class ModelHorseStatue extends ModelBase {
   private final ModelRenderer head;
   private final ModelRenderer upperMouth;
   private final ModelRenderer lowerMouth;
   private final ModelRenderer horseLeftEar;
   private final ModelRenderer horseRightEar;
   private final ModelRenderer muleLeftEar;
   private final ModelRenderer muleRightEar;
   private final ModelRenderer neck;
   private final ModelRenderer horseFaceRopes;
   private final ModelRenderer mane;
   private final ModelRenderer body;
   private final ModelRenderer tailBase;
   private final ModelRenderer tailMiddle;
   private final ModelRenderer tailTip;
   private final ModelRenderer backLeftLeg;
   private final ModelRenderer backLeftShin;
   private final ModelRenderer backLeftHoof;
   private final ModelRenderer backRightLeg;
   private final ModelRenderer backRightShin;
   private final ModelRenderer backRightHoof;
   private final ModelRenderer frontLeftLeg;
   private final ModelRenderer frontLeftShin;
   private final ModelRenderer frontLeftHoof;
   private final ModelRenderer frontRightLeg;
   private final ModelRenderer frontRightShin;
   private final ModelRenderer frontRightHoof;
   private final ModelRenderer muleLeftChest;
   private final ModelRenderer muleRightChest;
   private final ModelRenderer horseSaddleBottom;
   private final ModelRenderer horseSaddleFront;
   private final ModelRenderer horseSaddleBack;
   private final ModelRenderer horseLeftSaddleRope;
   private final ModelRenderer horseLeftSaddleMetal;
   private final ModelRenderer horseRightSaddleRope;
   private final ModelRenderer horseRightSaddleMetal;
   private final ModelRenderer horseLeftFaceMetal;
   private final ModelRenderer horseRightFaceMetal;
   private final ModelRenderer horseLeftRein;
   private final ModelRenderer horseRightRein;

   public ModelHorseStatue() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.body = new ModelRenderer(this, 0, 34);
      this.body.addBox(-5.0F, -8.0F, -19.0F, 10, 10, 24);
      this.body.setRotationPoint(0.0F, 11.0F, 9.0F);
      this.tailBase = new ModelRenderer(this, 44, 0);
      this.tailBase.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 3);
      this.tailBase.setRotationPoint(0.0F, 3.0F, 14.0F);
      this.tailBase.rotateAngleX = -1.134464F;
      this.tailMiddle = new ModelRenderer(this, 38, 7);
      this.tailMiddle.addBox(-1.5F, -2.0F, 3.0F, 3, 4, 7);
      this.tailMiddle.setRotationPoint(0.0F, 3.0F, 14.0F);
      this.tailMiddle.rotateAngleX = -1.134464F;
      this.tailTip = new ModelRenderer(this, 24, 3);
      this.tailTip.addBox(-1.5F, -4.5F, 9.0F, 3, 4, 7);
      this.tailTip.setRotationPoint(0.0F, 3.0F, 14.0F);
      this.tailTip.rotateAngleX = (float) (-Math.PI * 4.0 / 9.0);
      this.backLeftLeg = new ModelRenderer(this, 78, 29);
      this.backLeftLeg.addBox(-2.5F, -2.0F, -2.5F, 4, 9, 5);
      this.backLeftLeg.setRotationPoint(4.0F, 9.0F, 11.0F);
      this.backLeftShin = new ModelRenderer(this, 78, 43);
      this.backLeftShin.addBox(-2.0F, 0.0F, -1.5F, 3, 5, 3);
      this.backLeftShin.setRotationPoint(4.0F, 16.0F, 11.0F);
      this.backLeftHoof = new ModelRenderer(this, 78, 51);
      this.backLeftHoof.addBox(-2.5F, 5.1F, -2.0F, 4, 3, 4);
      this.backLeftHoof.setRotationPoint(4.0F, 16.0F, 11.0F);
      this.backRightLeg = new ModelRenderer(this, 96, 29);
      this.backRightLeg.addBox(-1.5F, -2.0F, -2.5F, 4, 9, 5);
      this.backRightLeg.setRotationPoint(-4.0F, 9.0F, 11.0F);
      this.backRightShin = new ModelRenderer(this, 96, 43);
      this.backRightShin.addBox(-1.0F, 0.0F, -1.5F, 3, 5, 3);
      this.backRightShin.setRotationPoint(-4.0F, 16.0F, 11.0F);
      this.backRightHoof = new ModelRenderer(this, 96, 51);
      this.backRightHoof.addBox(-1.5F, 5.1F, -2.0F, 4, 3, 4);
      this.backRightHoof.setRotationPoint(-4.0F, 16.0F, 11.0F);
      this.frontLeftLeg = new ModelRenderer(this, 44, 29);
      this.frontLeftLeg.addBox(-1.9F, -1.0F, -2.1F, 3, 8, 4);
      this.frontLeftLeg.setRotationPoint(4.0F, 9.0F, -8.0F);
      this.frontLeftShin = new ModelRenderer(this, 44, 41);
      this.frontLeftShin.addBox(-1.9F, 0.0F, -1.6F, 3, 5, 3);
      this.frontLeftShin.setRotationPoint(4.0F, 16.0F, -8.0F);
      this.frontLeftHoof = new ModelRenderer(this, 44, 51);
      this.frontLeftHoof.addBox(-2.4F, 5.1F, -2.1F, 4, 3, 4);
      this.frontLeftHoof.setRotationPoint(4.0F, 16.0F, -8.0F);
      this.frontRightLeg = new ModelRenderer(this, 60, 29);
      this.frontRightLeg.addBox(-1.1F, -1.0F, -2.1F, 3, 8, 4);
      this.frontRightLeg.setRotationPoint(-4.0F, 9.0F, -8.0F);
      this.frontRightShin = new ModelRenderer(this, 60, 41);
      this.frontRightShin.addBox(-1.1F, 0.0F, -1.6F, 3, 5, 3);
      this.frontRightShin.setRotationPoint(-4.0F, 16.0F, -8.0F);
      this.frontRightHoof = new ModelRenderer(this, 60, 51);
      this.frontRightHoof.addBox(-1.6F, 5.1F, -2.1F, 4, 3, 4);
      this.frontRightHoof.setRotationPoint(-4.0F, 16.0F, -8.0F);
      this.head = new ModelRenderer(this, 0, 0);
      this.head.addBox(-2.5F, -10.0F, -1.5F, 5, 5, 7);
      this.head.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.head.rotateAngleX = (float) (Math.PI / 6);
      this.upperMouth = new ModelRenderer(this, 24, 18);
      this.upperMouth.addBox(-2.0F, -10.0F, -7.0F, 4, 3, 6);
      this.upperMouth.setRotationPoint(0.0F, 0.02F, 0.02F);
      this.lowerMouth = new ModelRenderer(this, 24, 27);
      this.lowerMouth.addBox(-2.0F, -7.0F, -6.5F, 4, 2, 5);
      this.lowerMouth.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.head.addChild(this.upperMouth);
      this.head.addChild(this.lowerMouth);
      this.horseLeftEar = new ModelRenderer(this, 0, 0);
      this.horseLeftEar.addBox(0.45F, -12.0F, 4.0F, 2, 3, 1);
      this.horseLeftEar.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseLeftEar.rotateAngleX = (float) (Math.PI / 6);
      this.horseRightEar = new ModelRenderer(this, 0, 0);
      this.horseRightEar.addBox(-2.45F, -12.0F, 4.0F, 2, 3, 1);
      this.horseRightEar.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseRightEar.rotateAngleX = (float) (Math.PI / 6);
      this.muleLeftEar = new ModelRenderer(this, 0, 12);
      this.muleLeftEar.addBox(-2.0F, -16.0F, 4.0F, 2, 7, 1);
      this.muleLeftEar.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.muleLeftEar.rotateAngleX = (float) (Math.PI / 6);
      this.muleLeftEar.rotateAngleZ = (float) (Math.PI / 12);
      this.muleRightEar = new ModelRenderer(this, 0, 12);
      this.muleRightEar.addBox(0.0F, -16.0F, 4.0F, 2, 7, 1);
      this.muleRightEar.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.muleRightEar.rotateAngleX = (float) (Math.PI / 6);
      this.muleRightEar.rotateAngleZ = (float) (-Math.PI / 12);
      this.neck = new ModelRenderer(this, 0, 12);
      this.neck.addBox(-2.05F, -9.8F, -2.0F, 4, 14, 8);
      this.neck.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.neck.rotateAngleX = (float) (Math.PI / 6);
      this.muleLeftChest = new ModelRenderer(this, 0, 34);
      this.muleLeftChest.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3);
      this.muleLeftChest.setRotationPoint(-7.5F, 3.0F, 10.0F);
      this.muleLeftChest.rotateAngleY = (float) (Math.PI / 2);
      this.muleRightChest = new ModelRenderer(this, 0, 47);
      this.muleRightChest.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3);
      this.muleRightChest.setRotationPoint(4.5F, 3.0F, 10.0F);
      this.muleRightChest.rotateAngleY = (float) (Math.PI / 2);
      this.horseSaddleBottom = new ModelRenderer(this, 80, 0);
      this.horseSaddleBottom.addBox(-5.0F, 0.0F, -3.0F, 10, 1, 8);
      this.horseSaddleBottom.setRotationPoint(0.0F, 2.0F, 2.0F);
      this.horseSaddleFront = new ModelRenderer(this, 106, 9);
      this.horseSaddleFront.addBox(-1.5F, -1.0F, -3.0F, 3, 1, 2);
      this.horseSaddleFront.setRotationPoint(0.0F, 2.0F, 2.0F);
      this.horseSaddleBack = new ModelRenderer(this, 80, 9);
      this.horseSaddleBack.addBox(-4.0F, -1.0F, 3.0F, 8, 1, 2);
      this.horseSaddleBack.setRotationPoint(0.0F, 2.0F, 2.0F);
      this.horseLeftSaddleMetal = new ModelRenderer(this, 74, 0);
      this.horseLeftSaddleMetal.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
      this.horseLeftSaddleMetal.setRotationPoint(5.0F, 3.0F, 2.0F);
      this.horseLeftSaddleRope = new ModelRenderer(this, 70, 0);
      this.horseLeftSaddleRope.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
      this.horseLeftSaddleRope.setRotationPoint(5.0F, 3.0F, 2.0F);
      this.horseRightSaddleMetal = new ModelRenderer(this, 74, 4);
      this.horseRightSaddleMetal.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
      this.horseRightSaddleMetal.setRotationPoint(-5.0F, 3.0F, 2.0F);
      this.horseRightSaddleRope = new ModelRenderer(this, 80, 0);
      this.horseRightSaddleRope.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
      this.horseRightSaddleRope.setRotationPoint(-5.0F, 3.0F, 2.0F);
      this.horseLeftFaceMetal = new ModelRenderer(this, 74, 13);
      this.horseLeftFaceMetal.addBox(1.5F, -8.0F, -4.0F, 1, 2, 2);
      this.horseLeftFaceMetal.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseLeftFaceMetal.rotateAngleX = (float) (Math.PI / 6);
      this.horseRightFaceMetal = new ModelRenderer(this, 74, 13);
      this.horseRightFaceMetal.addBox(-2.5F, -8.0F, -4.0F, 1, 2, 2);
      this.horseRightFaceMetal.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseRightFaceMetal.rotateAngleX = (float) (Math.PI / 6);
      this.horseLeftRein = new ModelRenderer(this, 44, 10);
      this.horseLeftRein.addBox(2.6F, -6.0F, -6.0F, 0, 3, 16);
      this.horseLeftRein.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseRightRein = new ModelRenderer(this, 44, 5);
      this.horseRightRein.addBox(-2.6F, -6.0F, -6.0F, 0, 3, 16);
      this.horseRightRein.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.mane = new ModelRenderer(this, 58, 0);
      this.mane.addBox(-1.0F, -11.5F, 5.0F, 2, 16, 4);
      this.mane.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.mane.rotateAngleX = (float) (Math.PI / 6);
      this.horseFaceRopes = new ModelRenderer(this, 80, 12);
      this.horseFaceRopes.addBox(-2.5F, -10.1F, -7.0F, 5, 5, 12, 0.2F);
      this.horseFaceRopes.setRotationPoint(0.0F, 4.0F, -10.0F);
      this.horseFaceRopes.rotateAngleX = (float) (Math.PI / 6);
   }

   public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      AbstractHorse abstracthorse = (AbstractHorse)entityIn;
      float f = abstracthorse.getEatAnim(0.0F);
      boolean flag = abstracthorse.isBaby();
      boolean flag1 = !flag && abstracthorse.isSaddled();
      boolean flag2 = abstracthorse instanceof AbstractChestedHorse;
      boolean flag3 = !flag && flag2 && ((AbstractChestedHorse)abstracthorse).hasChest();
      float f1 = 0.5F /* 1.12 AbstractHorse.getHorseSize */;
      boolean flag4 = abstracthorse.isVehicle();
      if (flag1) {
         this.horseFaceRopes.render(scale);
         this.horseSaddleBottom.render(scale);
         this.horseSaddleFront.render(scale);
         this.horseSaddleBack.render(scale);
         this.horseLeftSaddleRope.render(scale);
         this.horseLeftSaddleMetal.render(scale);
         this.horseRightSaddleRope.render(scale);
         this.horseRightSaddleMetal.render(scale);
         this.horseLeftFaceMetal.render(scale);
         this.horseRightFaceMetal.render(scale);
         if (flag4) {
            this.horseLeftRein.render(scale);
            this.horseRightRein.render(scale);
         }
      }

      if (flag) {
         LegacyRender.pushMatrix();
         LegacyRender.scale(f1, 0.5F + f1 * 0.5F, f1);
         LegacyRender.translate(0.0F, 0.95F * (1.0F - f1), 0.0F);
      }

      this.backLeftLeg.render(scale);
      this.backLeftShin.render(scale);
      this.backLeftHoof.render(scale);
      this.backRightLeg.render(scale);
      this.backRightShin.render(scale);
      this.backRightHoof.render(scale);
      this.frontLeftLeg.render(scale);
      this.frontLeftShin.render(scale);
      this.frontLeftHoof.render(scale);
      this.frontRightLeg.render(scale);
      this.frontRightShin.render(scale);
      this.frontRightHoof.render(scale);
      if (flag) {
         LegacyRender.popMatrix();
         LegacyRender.pushMatrix();
         LegacyRender.scale(f1, f1, f1);
         LegacyRender.translate(0.0F, 1.35F * (1.0F - f1), 0.0F);
      }

      this.body.render(scale);
      this.tailBase.render(scale);
      this.tailMiddle.render(scale);
      this.tailTip.render(scale);
      this.neck.render(scale);
      this.mane.render(scale);
      if (flag) {
         LegacyRender.popMatrix();
         LegacyRender.pushMatrix();
         float f2 = 0.5F + f1 * f1 * 0.5F;
         LegacyRender.scale(f2, f2, f2);
         if (f <= 0.0F) {
            LegacyRender.translate(0.0F, 1.35F * (1.0F - f1), 0.0F);
         } else {
            LegacyRender.translate(0.0F, 0.9F * (1.0F - f1) * f + 1.35F * (1.0F - f1) * (1.0F - f), 0.15F * (1.0F - f1) * f);
         }
      }

      if (flag2) {
         this.muleLeftEar.render(scale);
         this.muleRightEar.render(scale);
      } else {
         this.horseLeftEar.render(scale);
         this.horseRightEar.render(scale);
      }

      this.head.render(scale);
      if (flag) {
         LegacyRender.popMatrix();
      }

      if (flag3) {
         this.muleLeftChest.render(scale);
         this.muleRightChest.render(scale);
      }
   }

   private float updateHorseRotation(float f1, float f2, float f3) {
      float f = f2 - f1;

      while (f < -180.0F) {
         f += 360.0F;
      }

      while (f >= 180.0F) {
         f -= 360.0F;
      }

      return f1 + f3 * f;
   }

   // 1.12 setLivingAnimations is only called on a renderer's main model, never on this statue model, so its pose
   // stays at the constructor values (a standing horse).

}
