package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelZoataur extends ModelTemplateDragon {
   public ModelZoataur() {
      this(1.0F);
   }

   public ModelZoataur(float shadowSize) {
      this.initModel("zoataur", LycanitesMobs.modInfo, "entity/zoataur");
      this.foldWings = false;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.2F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float walkIdle = Mth.sin(loop * 0.1F);
      float walkIdleRev = Mth.sin(loop * 0.1F + (float)Math.PI);
      if (partName.equals("wingleft01")) {
         this.rotate(0.0F, (float)Math.toDegrees((double)(walkIdle * 0.1F)), 0.0F);
      }

      if (partName.equals("wingright01")) {
         this.rotate(0.0F, (float)Math.toDegrees((double)(walkIdle * 0.1F)), 0.0F);
      }

      if (partName.equals("wingleft02")) {
         this.rotate(0.0F, -((float)Math.toDegrees((double)(walkIdle * 0.1F))), 0.0F);
      }

      if (partName.equals("wingright02")) {
         this.rotate(0.0F, -((float)Math.toDegrees((double)(walkIdle * 0.1F))), 0.0F);
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isBlocking()) {
         if (partName.equals("wingleft01")) {
            this.rotate(0.0F, -45.0F, 0.0F);
         } else if (partName.equals("wingleft02")) {
            this.rotate(0.0F, -75.0F, 0.0F);
         } else if (partName.equals("wingright01")) {
            this.rotate(0.0F, 45.0F, 0.0F);
         } else if (partName.equals("wingright02")) {
            this.rotate(0.0F, 75.0F, 0.0F);
         }
      } else if (partName.equals("wingleft01")) {
         this.rotate(35.0F, 50.0F, 0.0F);
      } else if (partName.equals("wingleft02")) {
         this.rotate(0.0F, 60.0F, 0.0F);
      } else if (partName.equals("wingright01")) {
         this.rotate(35.0F, -50.0F, 0.0F);
      } else if (partName.equals("wingright02")) {
         this.rotate(0.0F, -60.0F, 0.0F);
      }
   }
}
