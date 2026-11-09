package com.github.alexthe666.iceandfire.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 ModelIceDragonArmor (ModelBiped(modelSize, 0, 64, 64) plus its parts), generated from the 1.12 constructor by scripts. */
public class ModelIceDragonArmor extends HumanoidModel<LivingEntity> {
   public ModelIceDragonArmor(ModelPart root) {
      super(root);
   }

   @SuppressWarnings("unused")
   public static LayerDefinition createLayer(float modelSize, boolean legs) {
      MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(modelSize), 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition p_HornR = !legs ? root.getChild("head").addOrReplaceChild("hornr", CubeListBuilder.create().texOffs(48, 44).addBox(-1.0F, -0.5F, 0.0F, 2F, 3F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6F, -8.0F, 1.0F, (float) (Math.PI / 10), -0.33161256F, -0.19198622F)) : null;
      PartDefinition p_HornR4 = !legs ? root.getChild("head").addOrReplaceChild("hornr4", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.5F, 1.9F, 0.4537856F, (float) (-Math.PI / 10), -0.034906585F)) : null;
      PartDefinition p_Jaw = !legs ? root.getChild("head").addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(6, 51).addBox(-3.5F, 4.0F, -7.4F, 7F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.4F, 0.0F, -0.091106184F, -0.0F, 0.0F)) : null;
      PartDefinition p_Teeth1 = !legs ? p_Jaw.addOrReplaceChild("teeth1", CubeListBuilder.create().texOffs(6, 34).addBox(-3.6F, 0.1F, -8.9F, 4F, 1F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.4F, 0.0F, 0.0F, 0.0F)) : null;
      PartDefinition p_HornR3 = !legs ? root.getChild("head").addOrReplaceChild("hornr3", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, 0.7F, -0.06981317F, -0.4886922F, -0.08726646F)) : null;
      PartDefinition p_LeftLegSpike = legs ? root.getChild("left_leg").addOrReplaceChild("leftlegspike", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.4F, -1.4114478F, 0.0F, 0.0F)) : null;
      PartDefinition p_BackSpike2 = !legs ? root.getChild("body").addOrReplaceChild("backspike2", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.6F, 1.1838568F, 0.0F, 0.0F)) : null;
      PartDefinition p_BackSpike3 = !legs ? root.getChild("body").addOrReplaceChild("backspike3", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.4F, 0.0F, 1.1838568F, 0.0F, 0.0F)) : null;
      PartDefinition p_LeftShoulderSpike1 = !legs ? root.getChild("left_arm").addOrReplaceChild("leftshoulderspike1", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.2F, 0.0F, (float) -Math.PI, 0.0F, (float) (Math.PI / 18))) : null;
      PartDefinition p_HornL = !legs ? root.getChild("head").addOrReplaceChild("hornl", CubeListBuilder.create().texOffs(48, 44).mirror(true).addBox(-1.0F, -0.5F, 0.0F, 2F, 3F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6F, -8.0F, 1.0F, (float) (Math.PI / 10), 0.33161256F, 0.19198622F)) : null;
      PartDefinition p_HornL2 = !legs ? p_HornL.addOrReplaceChild("hornl2", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.3F, 4.5F, -0.075049154F, 0.0F, 0.0F)) : null;
      PartDefinition p_RightLegSpike3 = legs ? root.getChild("right_leg").addOrReplaceChild("rightlegspike3", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, -0.8F, -1.2217305F, 1.2217305F, (float) (-Math.PI / 18))) : null;
      PartDefinition p_LeftLegSpike3 = legs ? root.getChild("left_leg").addOrReplaceChild("leftlegspike3", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8F, 0.0F, -0.8F, -1.2217305F, -1.2217305F, (float) (Math.PI / 18))) : null;
      PartDefinition p_RightLegSpike2 = legs ? root.getChild("right_leg").addOrReplaceChild("rightlegspike2", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, 3.6F, -0.4F, -1.4114478F, 0.0F, 0.0F)) : null;
      PartDefinition p_BackSpike1 = !legs ? root.getChild("body").addOrReplaceChild("backspike1", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.9F, 0.2F, 1.1838568F, 0.0F, 0.0F)) : null;
      PartDefinition p_HornL3 = !legs ? root.getChild("head").addOrReplaceChild("hornl3", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, 0.7F, -0.06981317F, 0.4886922F, 0.08726646F)) : null;
      PartDefinition p_RightShoulderSpike1 = !legs ? root.getChild("right_arm").addOrReplaceChild("rightshoulderspike1", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.2F, 0.0F, (float) -Math.PI, 0.0F, (float) (-Math.PI / 18))) : null;
      PartDefinition p_LeftLegSpike2 = legs ? root.getChild("left_leg").addOrReplaceChild("leftlegspike2", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 3.6F, -0.4F, -1.4114478F, 0.0F, 0.0F)) : null;
      PartDefinition p_Teeth2 = !legs ? p_Jaw.addOrReplaceChild("teeth2", CubeListBuilder.create().texOffs(6, 34).mirror(true).addBox(-0.4F, 0.1F, -8.9F, 4F, 1F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.4F, 0.0F, 0.0F, 0.0F)) : null;
      PartDefinition p_RightShoulderSpike2 = !legs ? root.getChild("right_arm").addOrReplaceChild("rightshoulderspike2", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, -0.1F, 0.0F, (float) -Math.PI, 0.0F, (float) (-Math.PI / 12))) : null;
      PartDefinition p_RightLegSpike = legs ? root.getChild("right_leg").addOrReplaceChild("rightlegspike", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, 0.0F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.4F, -1.4114478F, 0.0F, 0.0F)) : null;
      PartDefinition p_HornR2 = !legs ? p_HornR.addOrReplaceChild("hornr2", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.3F, 4.5F, -0.075049154F, 0.0F, 0.0F)) : null;
      PartDefinition p_HeadFront = !legs ? root.getChild("head").addOrReplaceChild("headfront", CubeListBuilder.create().texOffs(6, 44).addBox(-3.5F, -2.8F, -8.8F, 7F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, 0.045553092F, -0.0F, 0.0F)) : null;
      PartDefinition p_LeftShoulderSpike2 = !legs ? root.getChild("left_arm").addOrReplaceChild("leftshoulderspike2", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, 0.0F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8F, -0.1F, 0.0F, (float) -Math.PI, 0.0F, (float) (Math.PI / 12))) : null;
      PartDefinition p_HornL4 = !legs ? root.getChild("head").addOrReplaceChild("hornl4", CubeListBuilder.create().texOffs(46, 36).mirror(true).addBox(-0.5F, -0.8F, -0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -7.5F, 1.9F, 0.4537856F, (float) (Math.PI / 10), 0.034906585F)) : null;
      return LayerDefinition.create(mesh, 64, 64);
   }
}
