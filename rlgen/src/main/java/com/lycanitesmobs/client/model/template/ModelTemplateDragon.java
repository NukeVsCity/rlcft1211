package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateDragon extends ModelCreatureObj {
   public boolean foldWings = true;

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
      if (partName.equals("mouth") || partName.equals("mouthbottom")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F))), 0.0F, 0.0F);
      }

      if (partName.equals("mouthtop")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F)), 0.0F, 0.0F);
      }

      if (partName.equals("neck")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("tail") || partName.equals("tail01") || partName.equals("tail02")) {
         rotX += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.25F)));
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.05F) * 0.05F)));
      }

      if (entity != null && entity.onGround()) {
         float walkLoop = Mth.cos(time * 0.3F);
         float walkLoopRev = Mth.cos(time * 0.3F + (float)Math.PI);
         float walkIdle = Mth.sin(loop * 0.1F);
         float walkIdleRev = Mth.sin(loop * 0.1F + (float)Math.PI);
         float walkSwing = 0.6F;
         if (partName.equals("legleft") || partName.equals("legrightfront") || partName.equals("armrightfront01") || partName.equals("legleftback")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(walkLoopRev * walkSwing * distance)));
         }

         if (partName.equals("legright") || partName.equals("legleftfront") || partName.equals("armleftfront01") || partName.equals("legrightback")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(walkLoop * walkSwing * distance)));
         }

         if (partName.equals("armleft01")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(walkLoop * (walkSwing / 2.0F) * distance)));
            rotY -= 10.0F;
            rotZ -= 25.0F;
         }

         if (partName.equals("armright01")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(walkLoopRev * (walkSwing / 2.0F) * distance)));
            rotY += 10.0F;
            rotZ += 25.0F;
         }

         if (this.foldWings) {
            if (partName.equals("wingleft01")) {
               rotX = (float)((double)rotX + Math.toDegrees((double)(walkIdle * 0.1F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(walkIdle * 0.1F)));
               rotZ += 80.0F;
            }

            if (partName.equals("wingright01")) {
               rotX = (float)((double)rotX + Math.toDegrees((double)(walkIdle * 0.1F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(walkIdleRev * 0.1F)));
               rotZ -= 80.0F;
            }

            if (partName.equals("wingleft02")) {
               rotZ = (float)((double)rotZ + Math.toDegrees((double)(walkIdle * 0.1F)));
               rotZ -= 170.0F;
               rotX -= 10.0F;
            }

            if (partName.equals("wingright02")) {
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(walkIdle * 0.1F)));
               rotZ += 170.0F;
               rotX -= 10.0F;
            }

            if (partName.equals("wingleftupper")) {
               rotX = (float)((double)rotX + 20.0 + Math.toDegrees((double)(walkIdle * 0.1F)));
               rotY += 60.0F;
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(walkIdle * 0.1F)));
            }

            if (partName.equals("wingrightupper")) {
               rotX = (float)((double)rotX + 20.0 + Math.toDegrees((double)(walkIdle * 0.1F)));
               rotY -= 60.0F;
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(walkIdleRev * 0.1F)));
            }

            if (partName.equals("wingleftlower")) {
               rotZ = (float)((double)rotZ + (Math.toDegrees((double)(walkIdle * 0.1F - 0.1F)) - 35.0));
            }

            if (partName.equals("wingrightlower")) {
               rotZ = (float)((double)rotZ - (Math.toDegrees((double)(walkIdle * 0.1F - 0.1F)) - 35.0));
            }
         }
      }

      if (entity != null && !entity.onGround()) {
         float flightLoop = Mth.sin(loop * 0.4F);
         float flightLoopRev = Mth.sin(loop * 0.4F + (float)Math.PI);
         if (partName.equals("body") && entity.getPassengers().isEmpty()) {
            posY += flightLoop / 2.0F;
         }

         if (partName.equals("head")) {
            rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.05F)));
         }

         if (partName.equals("legleftfront") || partName.equals("legrightfront")) {
            rotX += 25.0F;
            rotX = (float)((double)rotX + Math.toDegrees((double)(flightLoop * 0.1F)));
         }

         if (partName.equals("legleftback") || partName.equals("legrightback")) {
            rotX += 25.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.1F)));
         }

         if (this.foldWings) {
            if (partName.equals("wingleft")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.6F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoop * 0.6F)));
            }

            if (partName.equals("wingright")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.6F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoopRev * 0.6F)));
            }

            if (partName.equals("wingleft01") || partName.equals("armleft01") || partName.equals("wingleftupper")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.3F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoop * 0.3F)));
            }

            if (partName.equals("wingright01") || partName.equals("armright01") || partName.equals("wingrightupper")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(flightLoop * 0.3F)));
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoopRev * 0.3F)));
            }

            if (partName.equals("wingleft02") || partName.equals("armleft02")) {
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoop * 0.3F)));
            }

            if (partName.equals("wingright02") || partName.equals("armright02")) {
               rotZ = (float)((double)rotZ - Math.toDegrees((double)(flightLoopRev * 0.3F)));
            }
         }
      }

      if (partName.equals("mouth") || partName.equals("mouthbottom")) {
         rotX -= 15.0F * this.getAttackProgress();
      } else if (partName.equals("mouthtop")) {
         rotX += 15.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
