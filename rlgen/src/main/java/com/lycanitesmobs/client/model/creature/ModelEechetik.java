package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelEechetik extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelEechetik() {
      this(1.0F);
   }

   public ModelEechetik(float shadowSize) {
      this.initModel("eechetik", LycanitesMobs.modInfo, "entity/eechetik");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float loopOffset = 0.0F;
      if (partName.contains("01")) {
         loopOffset += 10.0F;
      } else if (partName.contains("02")) {
         loopOffset += 20.0F;
      } else if (partName.contains("03")) {
         loopOffset += 30.0F;
      } else if (partName.contains("04")) {
         loopOffset += 40.0F;
      }

      if (partName.equals("wingleftbottom") || partName.equals("winglefttop")) {
         this.rotate(0.0F, 10.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 2.0F) * 0.4F)), 0.0F);
      } else if (partName.equals("wingrightbottom") || partName.equals("wingrighttop")) {
         this.rotate(0.0F, -10.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 2.0F + (float)Math.PI) * 0.4F)), 0.0F);
      } else if (partName.contains("armleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            (float)Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F)) - 10.0F,
            (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
         );
      } else if (partName.contains("armright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            (float)(-Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F))) + 10.0F,
            (float)Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F))
         );
      } else if (partName.contains("mouthleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.1F)),
            0.0F,
            (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset) * 0.2F) * 0.1F)))
         );
      } else if (partName.contains("mouthright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.1F)),
            0.0F,
            (float)Math.toDegrees((double)(Mth.cos((loop + loopOffset) * 0.2F) * 0.1F))
         );
      } else if (partName.contains("antennaleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.1F)),
            0.0F,
            (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset) * 0.2F) * 0.1F)))
         );
      } else if (partName.contains("antennaright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.1F)),
            0.0F,
            (float)Math.toDegrees((double)(Mth.cos((loop + loopOffset) * 0.2F) * 0.1F))
         );
      }
   }
}
