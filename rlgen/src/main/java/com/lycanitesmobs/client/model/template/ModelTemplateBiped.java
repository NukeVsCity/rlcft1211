package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateBiped extends ModelCreatureObj {
   protected float walkSwing = 0.6F;
   protected float legScaleX = 1.0F;
   protected float tailScaleX = 1.0F;
   protected float tailScaleY = 1.0F;
   protected float flightBobScale = 1.0F;
   protected float mouthScale = 1.0F;
   protected float mouthRate = 1.0F;
   protected float wingScale = 1.0F;

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
      BaseCreatureEntity creatureEntity = null;
      if (entity instanceof BaseCreatureEntity) {
         creatureEntity = (BaseCreatureEntity)entity;
      }

      if (partName.equals("mouth") || partName.contains("mouthbottom")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F))) * this.mouthScale, 0.0F, 0.0F);
      }

      if (partName.contains("mouthtop")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F)) * this.mouthScale, 0.0F, 0.0F);
      }

      if (partName.contains("mouthleft")) {
         this.rotate(0.0F, (float)(-Math.toDegrees((double)(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F))) * this.mouthScale, 0.0F);
      }

      if (partName.contains("mouthright")) {
         this.rotate(0.0F, (float)Math.toDegrees((double)(Mth.cos(loop * this.mouthRate * 0.09F) * 0.1F - 0.1F)) * this.mouthScale, 0.0F);
      }

      if (partName.equals("neck")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.contains("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.contains("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (entity == null || entity.onGround() || entity.isInWater()) {
         if (partName.contains("wingleft")) {
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
         }

         if (partName.contains("wingright")) {
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
         }
      }

      if (partName.contains("tail")) {
         float sine = 0.0F;
         if (partName.equals("tail.002") || partName.equals("tail02")) {
            sine = 1.0F;
         } else if (partName.equals("tail.003") || partName.equals("tail03")) {
            sine = 2.0F;
         } else if (partName.equals("tail.004")) {
            sine = 3.0F;
         } else if (partName.equals("tail.005")) {
            sine = 4.0F;
         } else if (partName.equals("tail.006")) {
            sine = 5.0F;
         } else if (partName.equals("tail.007")) {
            sine = 6.0F;
         }

         sine = Mth.sin(sine / 6.0F) - 0.5F;
         float tailRotX = (float)(-Math.toDegrees((double)(Mth.cos((loop + time) * 0.1F) * 0.05F - 0.05F)));
         float tailRotY = (float)(-Math.toDegrees((double)(Mth.cos((loop + time) * sine * 0.1F) * 0.4F)));
         tailRotX = (float)((double)tailRotX + Math.toDegrees((double)(Mth.cos(time * 0.5F) * 0.1F * distance * 0.5F)));
         tailRotY = (float)((double)tailRotY + Math.toDegrees((double)(Mth.cos(time * 0.25F) * distance * 0.5F)));
         this.rotate(tailRotX * this.tailScaleX, tailRotY * this.tailScaleY, 0.0F);
      }

      if (partName.contains("finger")) {
         if (partName.contains("thumb")) {
            this.rotate(-((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F))), 0.0F, 0.0F);
         } else {
            this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
         }
      }

      if (entity == null || entity.onGround() || entity.isInWater()) {
         if (partName.contains("armleft") || partName.equals("wingright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 1.0F * distance * 0.5F)));
            if (!partName.contains("lower")) {
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 0.5F * distance * 0.5F)));
            }
         }

         if (partName.contains("armright") || partName.equals("wingleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F)));
            if (!partName.contains("lower")) {
               rotZ = (float)(
                  (double)rotZ + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F))
               );
            }
         }

         if (partName.equals("legleft")) {
            rotX = (float)(
               (double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * distance * this.legScaleX))
            );
         }

         if (partName.equals("legright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 1.4F * distance * this.legScaleX)));
         }

         if (partName.equals("legleftupper")) {
            rotX = (float)(
               (double)rotX
                  + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * distance * 0.5F * this.legScaleX))
            );
         }

         if (partName.equals("legleftlower")) {
            rotX = (float)(
               (double)rotX
                  + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 1.4F * distance * 0.5F * this.legScaleX))
            );
         }

         if (partName.equals("legrightupper")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 1.4F * distance * 0.5F * this.legScaleX)));
         }

         if (partName.equals("legrightlower")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 1.4F * distance * 0.5F * this.legScaleX)));
         }

         if (partName.contains("legleft0")) {
            rotX = (float)(
               (double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing + (float)Math.PI) * 0.6F * distance * this.legScaleX))
            );
         }

         if (partName.contains("legright0")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * this.walkSwing) * 0.6F * distance * this.legScaleX)));
         }
      }

      if (entity != null && !entity.onGround() && !entity.isInWater() && (creatureEntity == null || !creatureEntity.hasPerchTarget())) {
         if (partName.contains("wingleft")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F)));
         }

         if (partName.contains("wingright")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale + (float)Math.PI) * 0.6F)));
         }

         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isFlying() && partName.equals("body")) {
            float bob = Mth.sin(loop * 0.4F) * 0.15F;
            posY += bob * this.flightBobScale;
         }

         if (partName.contains("legleft")) {
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
         }

         if (partName.contains("legright")) {
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
         }
      }

      if (creatureEntity != null && creatureEntity.hasAttackTarget()) {
         if (partName.equals("mouth") || partName.contains("mouthbottom")) {
            rotX += 20.0F * this.getAttackProgress() * this.mouthScale;
         }

         if (partName.contains("mouthtop")) {
            rotX -= 20.0F * this.getAttackProgress() * this.mouthScale;
         }

         if (partName.contains("mouthleft")) {
            rotY -= 20.0F * this.getAttackProgress() * this.mouthScale;
         }

         if (partName.contains("mouthright")) {
            rotY += 20.0F * this.getAttackProgress() * this.mouthScale;
         }

         if (partName.contains("armleft") || partName.contains("armright")) {
            rotX -= 80.0F * this.getAttackProgress();
         }
      }

      if (entity.isPassenger()) {
         if (partName.equals("legleft")) {
            rotX -= 50.0F;
         }

         if (partName.equals("legright")) {
            rotX -= 50.0F;
         }
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
