package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelWraamon extends ModelTemplateBiped {
   public ModelWraamon() {
      this(1.0F);
   }

   public ModelWraamon(float shadowSize) {
      this.initModel("wraamon", LycanitesMobs.modInfo, "entity/wraamon");
      this.lookHeadScaleX = 0.8F;
      this.lookHeadScaleY = 0.8F;
      this.lookNeckScaleX = 0.2F;
      this.lookNeckScaleY = 0.2F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      if (this.currentModelState != null) {
         if (this.currentModelState.getBoolean("standing")) {
            if (partName.equals("body")) {
               this.rotate(-45.0F, 0.0F, 0.0F);
            } else if (partName.equals("head")) {
               this.rotate(30.0F, 0.0F, 0.0F);
            } else if (partName.contains("leg")) {
               this.rotate(45.0F, 0.0F, 0.0F);
            } else if (partName.contains("tail")) {
               this.rotate(50.0F, 0.0F, 0.0F);
            }
         }

         if (this.currentModelState.getFloat("standingChangeTime") <= 0.0F) {
            this.currentModelState.setBoolean("standing", !this.currentModelState.getBoolean("standing"));
            this.currentModelState.setFloat("standingChangeTime", 40.0F + 160.0F * entity.getRandom().nextFloat());
         }

         if (partName.equals("body")) {
            this.currentModelState.setFloat("standingChangeTime", this.currentModelState.getFloat("standingChangeTime") - 1.0F);
         }
      }

      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("nose")) {
         this.rotate(
            (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.1F))),
            (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.4F) * 0.1F))),
            0.0F
         );
      } else if (partName.equals("tailleft")) {
         float rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         float rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
         this.rotate(rotX, rotY, 0.0F);
      } else if (partName.equals("tailright")) {
         float rotX = (float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F));
         float rotY = (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F));
         this.rotate(rotX, rotY, 0.0F);
      }
   }
}
