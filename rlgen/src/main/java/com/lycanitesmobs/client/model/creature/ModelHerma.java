package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateArachnid;
import net.minecraft.world.entity.Mob;

public class ModelHerma extends ModelTemplateArachnid {
   public ModelHerma() {
      this(1.0F);
   }

   public ModelHerma(float shadowSize) {
      this.initModel("herma", LycanitesMobs.modInfo, "entity/herma");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
      this.bodyIsTrophy = true;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("head") && this.currentModelState != null) {
         if (entity != null && distance >= 0.25F) {
            if (this.currentModelState.getFloat("walkingAngleChangeTime") <= 0.0F) {
               float random = entity.getRandom().nextFloat();
               if (random <= 0.3F) {
                  this.currentModelState.setFloat("walkingAngleTarget", 90.0F);
               } else if (random <= 0.6F) {
                  this.currentModelState.setFloat("walkingAngleTarget", -90.0F);
               } else {
                  this.currentModelState.setFloat("walkingAngleTarget", 0.0F);
               }

               this.currentModelState.setFloat("walkingAngleChangeTime", 200.0F);
            }
         } else {
            this.currentModelState.setFloat("walkingAngleTarget", 0.0F);
         }

         float walkingAngleCurrent = this.currentModelState.getFloat("walkingAngleCurrent");
         if (walkingAngleCurrent < this.currentModelState.getFloat("walkingAngleTarget")) {
            this.currentModelState.setFloat("walkingAngleCurrent", walkingAngleCurrent + 1.0F);
         } else if (walkingAngleCurrent > this.currentModelState.getFloat("walkingAngleTarget")) {
            this.currentModelState.setFloat("walkingAngleCurrent", walkingAngleCurrent - 1.0F);
         }

         this.rotate(0.0F, this.currentModelState.getFloat("walkingAngleCurrent"), 0.0F);
         this.currentModelState.setFloat("walkingAngleChangeTime", this.currentModelState.getFloat("walkingAngleChangeTime") - 1.0F);
      }
   }
}
