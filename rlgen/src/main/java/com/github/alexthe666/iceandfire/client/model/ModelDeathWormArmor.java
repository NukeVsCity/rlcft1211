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

/** 1.12 ModelDeathWormArmor (ModelBiped(modelSize, 0, 64, 64) plus its parts), generated from the 1.12 constructor by scripts. */
public class ModelDeathWormArmor extends HumanoidModel<LivingEntity> {
   public ModelDeathWormArmor(ModelPart root) {
      super(root);
   }

   @SuppressWarnings("unused")
   public static LayerDefinition createLayer(float modelSize) {
      MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(modelSize), 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition p_spineH1 = root.getChild("hat").addOrReplaceChild("spineh1", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, -3.0F, -0.49148473F, 0.0F, 0.0F));
      PartDefinition p_spineL2 = root.getChild("left_arm").addOrReplaceChild("spinel2", CubeListBuilder.create().texOffs(32, 40).addBox(-0.4F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -1.6F, 0.0F, -0.49148473F, 0.0F, 0.0F));
      PartDefinition p_spineL1 = root.getChild("left_arm").addOrReplaceChild("spinel1", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -2.7F, 0.0F, -0.49148473F, 0.0F, 0.0F));
      PartDefinition p_spineH6 = root.getChild("hat").addOrReplaceChild("spineh6", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -2.7F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 5.0F, -2.003289F, 0.0F, 0.0F));
      PartDefinition p_spineH5 = root.getChild("hat").addOrReplaceChild("spineh5", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 5.0F, -1.821251F, 0.0F, 0.0F));
      PartDefinition p_spineH3 = root.getChild("hat").addOrReplaceChild("spineh3", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 3.0F, -0.8651597F, 0.0F, 0.0F));
      PartDefinition p_spineR2 = root.getChild("right_arm").addOrReplaceChild("spiner2", CubeListBuilder.create().texOffs(32, 40).addBox(-0.6F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -1.6F, 0.0F, -0.49148473F, 0.0F, 0.0F));
      PartDefinition p_spineH2 = root.getChild("hat").addOrReplaceChild("spineh2", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -2.7F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.49148473F, 0.0F, 0.0F));
      PartDefinition p_spineH4 = root.getChild("hat").addOrReplaceChild("spineh4", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -2.7F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 5.0F, -1.548107F, 0.0F, 0.0F));
      PartDefinition p_spineH7 = root.getChild("hat").addOrReplaceChild("spineh7", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.3F, 4.5F, -2.003289F, 0.0F, 0.0F));
      PartDefinition p_spineR1 = root.getChild("right_arm").addOrReplaceChild("spiner1", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -1.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.7F, 0.0F, -0.49148473F, 0.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }
}
