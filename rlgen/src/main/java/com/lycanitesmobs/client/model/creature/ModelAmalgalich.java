package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.creature.EntityAmalgalich;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelAmalgalich extends ModelCreatureObj {
   public ModelAmalgalich() {
      this(1.0F);
   }

   public ModelAmalgalich(float shadowSize) {
      this.initModel("amalgalich", LycanitesMobs.modInfo, "entity/amalgalich");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.75F;
      this.lookNeckScaleX = 0.25F;
      this.lookNeckScaleY = 0.25F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "fire", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      return layer != null && entity instanceof EntityAmalgalich
         ? ((EntityAmalgalich)entity).getConsumptionAnimation() > 0.0F
         : super.canRenderPart(partName, entity, layer, trophy);
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
      if (entity instanceof EntityAmalgalich && ((EntityAmalgalich)entity).getConsumptionAnimation() > 0.0F) {
         float consumptionScale = ((EntityAmalgalich)entity).getConsumptionAnimation();
         if (consumptionScale == 1.0F) {
            loop *= 8.0F * consumptionScale;
         }

         if (partName.equals("body")) {
            posY -= 8.0F * consumptionScale;
         }

         if (partName.equals("legleftfront01") || partName.equals("legleftback01")) {
            rotZ += 65.0F * consumptionScale;
         }

         if (partName.equals("legrightfront01") || partName.equals("legrightback01")) {
            rotZ -= 65.0F * consumptionScale;
         }

         if (partName.equals("legleftfront03") || partName.equals("legleftback03")) {
            rotZ -= 25.0F * consumptionScale;
         }

         if (partName.equals("legrightfront03") || partName.equals("legrightback03")) {
            rotZ += 25.0F * consumptionScale;
         }

         if (partName.equals("neck")) {
            rotX += 20.0F * consumptionScale;
         }

         if (partName.equals("head")) {
            rotX -= 20.0F * consumptionScale;
         }

         if (partName.equals("mouth")) {
            rotX += 40.0F * consumptionScale;
         }

         if (partName.equals("armleft01")) {
            rotZ += 35.0F * consumptionScale;
         }

         if (partName.equals("armright01")) {
            rotZ -= 35.0F * consumptionScale;
         }
      }

      if (partName.equals("mouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F))), 0.0F, 0.0F);
      }

      if (partName.equals("neck")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.contains("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.contains("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.contains("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      } else if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F)), 0.0F, 0.0F);
      }

      float walkSwing = 0.1F;
      if (partName.contains("armleft")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F)));
      }

      if (partName.contains("armright")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F)));
      }

      if (partName.contains("legrightfront") || partName.contains("legleftback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.contains("legleftfront") || partName.contains("legrightback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
