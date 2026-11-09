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

/** 1.12 ModelSeaSerpentArmor (ModelBiped(modelSize, 0, 64, 64) plus its parts), generated from the 1.12 constructor by scripts. */
public class ModelSeaSerpentArmor extends HumanoidModel<LivingEntity> {
   public ModelSeaSerpentArmor(ModelPart root) {
      super(root);
   }

   @SuppressWarnings("unused")
   public static LayerDefinition createLayer(float modelSize) {
      MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(modelSize), 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition p_headFin2 = root.getChild("head").addOrReplaceChild("headfin2", CubeListBuilder.create().texOffs(0, 32).mirror(true).addBox(-0.5F, -8.4F, -7.9F, 1F, 16F, 14F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -8.8F, 3.5F, (float) Math.PI, (float) (Math.PI / 6), 0.0F));
      PartDefinition p_armFinR = root.getChild("right_arm").addOrReplaceChild("armfinr", CubeListBuilder.create().texOffs(30, 32).addBox(-0.5F, -5.4F, -6.0F, 1F, 7F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 4.0F, -0.4F, (float) Math.PI, (float) (-Math.PI * 5.0 / 12.0), -0.0034906585F));
      PartDefinition p_headFin = root.getChild("head").addOrReplaceChild("headfin", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, -8.4F, -7.9F, 1F, 16F, 14F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -8.8F, 3.5F, (float) Math.PI, (float) (-Math.PI / 6), 0.0F));
      PartDefinition p_legFinR = root.getChild("left_leg").addOrReplaceChild("legfinr", CubeListBuilder.create().texOffs(45, 31).mirror(true).addBox(-0.5F, -5.4F, -6.0F, 1F, 7F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 5.2F, 1.6F, (float) Math.PI, (float) (Math.PI * 5.0 / 12.0), 0.0F));
      PartDefinition p_shoulderL = root.getChild("left_arm").addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(38, 46).mirror(true).addBox(-1.5F, -2.0F, -2.5F, 5F, 12F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F));
      PartDefinition p_legFinR_1 = root.getChild("right_leg").addOrReplaceChild("legfinr_1", CubeListBuilder.create().texOffs(45, 31).addBox(-0.5F, -5.4F, -6.0F, 1F, 7F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 5.2F, 1.6F, (float) Math.PI, (float) (-Math.PI * 5.0 / 12.0), 0.0F));
      PartDefinition p_shoulderR = root.getChild("right_arm").addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(38, 46).addBox(-3.5F, -2.0F, -2.5F, 5F, 12F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F));
      PartDefinition p_armFinL = root.getChild("left_arm").addOrReplaceChild("armfinl", CubeListBuilder.create().texOffs(30, 32).mirror(true).addBox(-0.5F, -5.4F, -6.0F, 1F, 7F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 4.0F, -0.4F, (float) Math.PI, (float) (Math.PI * 5.0 / 12.0), 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }
}
