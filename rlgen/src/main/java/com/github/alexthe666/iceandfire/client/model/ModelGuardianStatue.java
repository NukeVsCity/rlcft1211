package com.github.alexthe666.iceandfire.client.model;

import net.minecraft.client.Minecraft;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.util.Mth;

public class ModelGuardianStatue extends ModelBase {
   private final ModelRenderer guardianBody;
   private final ModelRenderer guardianEye;
   private final ModelRenderer[] guardianSpines;
   private final ModelRenderer[] guardianTail;

   public ModelGuardianStatue() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.guardianSpines = new ModelRenderer[12];
      this.guardianBody = new ModelRenderer(this);
      this.guardianBody.setTextureOffset(0, 0).addBox(-6.0F, 10.0F, -8.0F, 12, 12, 16);
      this.guardianBody.setTextureOffset(0, 28).addBox(-8.0F, 10.0F, -6.0F, 2, 12, 12);
      this.guardianBody.setTextureOffset(0, 28).addBox(6.0F, 10.0F, -6.0F, 2, 12, 12, true);
      this.guardianBody.setTextureOffset(16, 40).addBox(-6.0F, 8.0F, -6.0F, 12, 2, 12);
      this.guardianBody.setTextureOffset(16, 40).addBox(-6.0F, 22.0F, -6.0F, 12, 2, 12);

      for (int i = 0; i < this.guardianSpines.length; i++) {
         this.guardianSpines[i] = new ModelRenderer(this, 0, 0);
         this.guardianSpines[i].addBox(-1.0F, -4.5F, -1.0F, 2, 9, 2);
         this.guardianBody.addChild(this.guardianSpines[i]);
      }

      this.guardianEye = new ModelRenderer(this, 8, 0);
      this.guardianEye.addBox(-1.0F, 15.0F, 0.0F, 2, 2, 1);
      this.guardianBody.addChild(this.guardianEye);
      this.guardianTail = new ModelRenderer[3];
      this.guardianTail[0] = new ModelRenderer(this, 40, 0);
      this.guardianTail[0].addBox(-2.0F, 14.0F, 7.0F, 4, 4, 8);
      this.guardianTail[1] = new ModelRenderer(this, 0, 54);
      this.guardianTail[1].addBox(0.0F, 14.0F, 0.0F, 3, 3, 7);
      this.guardianTail[2] = new ModelRenderer(this);
      this.guardianTail[2].setTextureOffset(41, 32).addBox(0.0F, 14.0F, 0.0F, 2, 2, 6);
      this.guardianTail[2].setTextureOffset(25, 19).addBox(1.0F, 10.5F, 3.0F, 1, 9, 9);
      this.guardianBody.addChild(this.guardianTail[0]);
      this.guardianTail[0].addChild(this.guardianTail[1]);
      this.guardianTail[1].addChild(this.guardianTail[2]);
   }

   public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
      this.guardianBody.render(scale);
   }

   public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
      Guardian entityguardian = (Guardian)entityIn;
      float f = ageInTicks - (float)entityguardian.tickCount;
      this.guardianBody.rotateAngleY = netHeadYaw * (float) (Math.PI / 180.0);
      this.guardianBody.rotateAngleX = headPitch * (float) (Math.PI / 180.0);
      float[] afloat = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
      float[] afloat1 = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
      float[] afloat2 = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
      float[] afloat3 = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
      float[] afloat4 = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
      float[] afloat5 = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
      float f1 = 0.55F;

      for (int i = 0; i < 12; i++) {
         this.guardianSpines[i].rotateAngleX = (float) Math.PI * afloat[i];
         this.guardianSpines[i].rotateAngleY = (float) Math.PI * afloat1[i];
         this.guardianSpines[i].rotateAngleZ = (float) Math.PI * afloat2[i];
         this.guardianSpines[i].rotationPointX = afloat3[i] * (1.0F + Mth.cos(ageInTicks * 1.5F + (float)i) * 0.01F - f1);
         this.guardianSpines[i].rotationPointY = 16.0F + afloat4[i] * (1.0F + Mth.cos(ageInTicks * 1.5F + (float)i) * 0.01F - f1);
         this.guardianSpines[i].rotationPointZ = afloat5[i] * (1.0F + Mth.cos(ageInTicks * 1.5F + (float)i) * 0.01F - f1);
      }

      this.guardianEye.rotationPointZ = -8.25F;
      Entity entity = Minecraft.getInstance().getCameraEntity();
      if (entityguardian.hasActiveAttackTarget()) {
         Entity var20 = entityguardian.getActiveAttackTarget();
      }

      this.guardianEye.showModel = true;
      float f2 = entityguardian.getTailAnimation(f);
      this.guardianTail[0].rotateAngleY = Mth.sin(f2) * (float) Math.PI * 0.05F;
      this.guardianTail[1].rotateAngleY = Mth.sin(f2) * (float) Math.PI * 0.1F;
      this.guardianTail[1].rotationPointX = -1.5F;
      this.guardianTail[1].rotationPointY = 0.5F;
      this.guardianTail[1].rotationPointZ = 14.0F;
      this.guardianTail[2].rotateAngleY = Mth.sin(f2) * (float) Math.PI * 0.15F;
      this.guardianTail[2].rotationPointX = 0.5F;
      this.guardianTail[2].rotationPointY = 0.5F;
      this.guardianTail[2].rotationPointZ = 6.0F;
   }
}
