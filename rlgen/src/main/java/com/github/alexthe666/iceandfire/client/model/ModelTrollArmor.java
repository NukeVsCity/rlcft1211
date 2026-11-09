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

/** 1.12 ModelTrollArmor (ModelBiped(modelSize, 0, 64, 64) plus its parts), generated from the 1.12 constructor by scripts. */
public class ModelTrollArmor extends HumanoidModel<LivingEntity> {
   public ModelTrollArmor(ModelPart root) {
      super(root);
   }

   @SuppressWarnings("unused")
   public static LayerDefinition createLayer(float modelSize) {
      MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(modelSize), 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition p_hornL = root.getChild("head").addOrReplaceChild("hornl", CubeListBuilder.create().texOffs(3, 41).mirror(true).addBox(-1.0F, -0.5F, 0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.2F, -3.0F, -0.7740535F, 2.959555F, -0.27314404F));
      PartDefinition p_hornL2 = p_hornL.addOrReplaceChild("hornl2", CubeListBuilder.create().texOffs(15, 50).mirror(true).addBox(-0.51F, -0.8F, -0.0F, 1F, 2F, 7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, 1.3F, 4.5F, 1.2747885F, 0.0F, 0.0F));
      PartDefinition p_hornR = root.getChild("head").addOrReplaceChild("hornr", CubeListBuilder.create().texOffs(4, 41).mirror(true).addBox(-0.5F, -0.5F, 0.0F, 1F, 2F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3F, -2.2F, -3.0F, -0.7740535F, -2.959555F, 0.27314404F));
      PartDefinition p_hornR2 = p_hornR.addOrReplaceChild("hornr2", CubeListBuilder.create().texOffs(15, 50).mirror(true).addBox(-0.01F, -0.8F, -0.0F, 1F, 2F, 7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, 1.3F, 4.5F, 1.2747885F, 0.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }
}
