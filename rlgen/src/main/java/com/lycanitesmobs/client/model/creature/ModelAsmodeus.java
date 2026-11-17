package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureFire;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureShield;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityAsmodeus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelAsmodeus extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelAsmodeus() {
      this(1.0F);
   }

   public ModelAsmodeus(float shadowSize) {
      this.initModel("asmodeus", LycanitesMobs.modInfo, "entity/asmodeus");
      this.setPartCenter("head", 0.0F, 10.0F, 0.0F);
      this.setPartCenter("shield", 0.0F, 10.0F, 0.0F);
      this.setPartCenter("body", 0.0F, 10.0F, 0.0F);
      this.setPartCenter("turret", 0.0F, 7.72F, -0.995F);
      this.setPartCenter("weapon", 0.0F, 4.6F, -7.025F);
      this.setPartCenter("armleft", 6.8F, 15.6F, -2.0F);
      this.setPartCenter("armright", -6.8F, 15.6F, -2.0F);
      this.setPartCenter("legleftfront", 5.14256F, 9.2F, -3.2F);
      this.setPartCenter("legleftmiddle", 6.4F, 9.2F, 0.0F);
      this.setPartCenter("legleftback", 5.14256F, 9.2F, 3.2F);
      this.setPartCenter("legrightfront", -5.14256F, 9.2F, -3.2F);
      this.setPartCenter("legrightmiddle", -6.4F, 9.2F, 0.0F);
      this.setPartCenter("legrightback", -5.14256F, 9.2F, 3.2F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 0.1F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureFire(renderer));
      renderer.addLayer(new LayerCreatureShield(renderer));
   }

   @Override
   public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
      return !"shield".equals(partName);
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float pi = (float)Math.PI;
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
      if (partName.equals("legleftfront")
         || partName.equals("legleftback")
         || partName.equals("legrightmiddle")
         || partName.equals("legleftmiddle")
         || partName.equals("legrightfront")
         || partName.equals("legrightback")) {
         angleZ = 1.0F;
      }

      if (partName.equals("legleftfront")) {
         angleY = 0.083333336F;
      }

      if (partName.equals("legleftmiddle")) {
         angleY = 0.0F;
      }

      if (partName.equals("legleftback")) {
         angleY = -0.083333336F;
      }

      if (partName.equals("legrightfront")) {
         angleY = -0.083333336F;
      }

      if (partName.equals("legrightmiddle")) {
         angleY = 0.0F;
      }

      if (partName.equals("legrightback")) {
         angleY = 0.083333336F;
      }

      float armSwing = 0.3F;
      if (partName.equals("armleft")) {
         rotZ = (float)((double)rotZ + -Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
         rotX = (float)((double)rotX + -Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (entity != null && !entity.onGround()) {
         float walkSwing = 0.3F;
         if (partName.equals("legleftfront") || partName.equals("legleftmiddle") || partName.equals("legleftback")) {
            rotation += 20.0F;
         }

         if (partName.equals("legrightfront") || partName.equals("legrightmiddle") || partName.equals("legrightback")) {
            rotation -= 20.0F;
         }
      } else {
         if (partName.equals("armleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * 2.0F * distance * armSwing)));
         }

         if (partName.equals("armright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * 2.0F * distance * armSwing)));
         }

         float walkSwingx = 0.3F;
         if (partName.equals("legleftfront") || partName.equals("legleftback") || partName.equals("legrightmiddle")) {
            rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwingx * distance)));
         }

         if (partName.equals("legleftmiddle") || partName.equals("legrightfront") || partName.equals("legrightback")) {
            rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwingx * distance)));
         }

         float bob = Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwingx * distance;
         if (bob < 0.0F) {
            bob += -bob * 2.0F;
         }

         posY += bob;
      }

      if (partName.contains("turret") || partName.contains("weapon")) {
         if (partName.contains("weapon")) {
            this.centerPartToPart("weapon", "turret");
         }

         float xRotation = 0.0F;
         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasAttackTarget()) {
            xRotation = (float)Math.toDegrees((double)lookX / (180.0 / Math.PI)) - 25.0F;
         }

         this.rotate(xRotation, (float)Math.toDegrees((double)lookY / (180.0 / Math.PI)), 0.0F);
         if (partName.contains("weapon")) {
            this.uncenterPartToPart("weapon", "turret");
         }
      }

      if (entity instanceof EntityAsmodeus && partName.contains("weapon")) {
         EntityAsmodeus entityAsmodeus = (EntityAsmodeus)entity;
         if (entityAsmodeus.isAttackOnCooldown()) {
            rotZ -= loop * 30.0F;
         } else if (entityAsmodeus.aiRangedAttack != null && !entityAsmodeus.aiRangedAttack.attackOnCooldown) {
            rotZ -= loop * 7.5F;
         }
      }

      if (partName.contains("shield")) {
         rotY += loop * 30.0F;
         float shieldScale = 1.05F + (0.5F + Mth.sin(loop / 4.0F) / 2.0F) / 8.0F;
         this.scale(shieldScale, shieldScale, shieldScale);
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
