package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateAquatic;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureRoa;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelRoa extends ModelTemplateAquatic {
   public ModelRoa() {
      this(1.0F);
   }

   public ModelRoa(float shadowSize) {
      this.initModel("roa", LycanitesMobs.modInfo, "entity/roa");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureRoa(renderer));
   }

   @Override
   public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
      return !"effect".equals(partName);
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      return "effect".equals(partName) ? layer instanceof LayerCreatureRoa : super.canRenderPart(partName, entity, layer, trophy);
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float posX = 0.0F;
      float posY = 0.0F;
      float posZ = 0.0F;
      float angleX = 0.0F;
      float angleY = 0.0F;
      float angleZ = 0.0F;
      float rotation = 0.0F;
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      if (partName.contains("effect")) {
         rotY += loop * 30.0F;
         float effectScale = 1.05F + (0.5F + Mth.sin(loop / 4.0F) / 2.0F) / 8.0F;
         this.scale(effectScale, effectScale, effectScale);
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
