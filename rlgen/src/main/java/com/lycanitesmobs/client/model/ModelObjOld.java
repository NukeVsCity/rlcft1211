package com.lycanitesmobs.client.model;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.obj.ObjObject;
import com.lycanitesmobs.client.obj.TessellatorModel;
import com.lycanitesmobs.client.obj.VBOModel;
import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.client.renderer.LycRenderTypes;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;

public class ModelObjOld extends ModelCustom {
   public static float modelXRotOffset = 180.0F;
   public static float modelYPosOffset = -1.5F;
   public TessellatorModel wavefrontObject;
   public List<ObjObject> wavefrontParts;
   public Map<String, float[]> partCenters = new HashMap<>();
   public Map<String, float[]> partSubCenters = new HashMap<>();
   public Map<String, float[]> offsets = new HashMap<>();
   public boolean lockHeadX = false;
   public boolean lockHeadY = false;
   public boolean bodyIsTrophy = true;
   public float trophyScale = 1.0F;
   public float[] trophyOffset = new float[0];
   public float[] trophyMouthOffset = new float[0];
   public boolean dontColor = false;

   public ModelObjOld() {
      this(1.0F);
   }

   public ModelObjOld(float shadowSize) {
   }

   public ModelObjOld initModel(String name, ModInfo groupInfo, String path) {
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(name);
      if (creatureInfo != null && !creatureInfo.enabled) {
         return this;
      } else {
         this.wavefrontObject = new VBOModel(TessellatorModel.location(groupInfo.modid, path + ".obj"));
         this.wavefrontParts = this.wavefrontObject.objObjects;
         if (this.wavefrontParts.isEmpty()) {
            LycanitesMobs.logWarning("", "Unable to load any parts for the " + name + " model!");
         }

         return this;
      }
   }

   @Override
   public void render(Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale, LayerCreatureBase layer, boolean animate) {
      boolean trophyModel = false;
      if (scale < 0.0F) {
         trophyModel = true;
         scale = -scale;
      } else {
         scale *= 16.0F;
         if (entity instanceof BaseCreatureEntity) {
            scale = (float)((double)scale * ((BaseCreatureEntity)entity).getRenderScale());
         }
      }

      if (entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity creature = (BaseCreatureEntity)entity;
         if (creature.onlyRenderTicks >= 0.0F) {
            loop = creature.onlyRenderTicks;
         }
      }

      for (ObjObject part : this.wavefrontParts) {
         if (part.getName() != null) {
            String partName = part.getName().toLowerCase();
            boolean isTrophyPart = this.isTrophyPart(partName);
            if (this.bodyIsTrophy && partName.contains("body")) {
               isTrophyPart = true;
            }

            if (this.canRenderPart(partName, entity, layer, trophyModel) && (!trophyModel || isTrophyPart)) {
               LegacyRender.pushMatrix();
               
               this.rotate(modelXRotOffset, 1.0F, 0.0F, 0.0F);
               this.translate(0.0F, modelYPosOffset, 0.0F);
               if (this.isChild && !trophyModel) {
                  this.childScale(partName);
               }

               this.scale(scale, scale, scale);
               if (trophyModel) {
                  this.scale(this.trophyScale, this.trophyScale, this.trophyScale);
               }

               this.centerPart(partName);
               if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPerchTarget()) {
                  distance = 0.0F;
               }

               this.animatePart(partName, (Mob)entity, time, distance, loop, -lookY, lookX, scale);
               if (trophyModel) {
                  if (!partName.contains("head") && !partName.contains("body")) {
                     float[] mouthOffset = this.comparePartCenters(this.bodyIsTrophy ? "body" : "head", partName);
                     this.translate(mouthOffset[0], mouthOffset[1], mouthOffset[2]);
                     if (this.trophyMouthOffset.length >= 3) {
                        this.translate(this.trophyMouthOffset[0], this.trophyMouthOffset[1], this.trophyMouthOffset[2]);
                     }
                  }

                  if (partName.contains("head")) {
                     if (!partName.contains("left")) {
                        this.translate(-0.3F, 0.0F, 0.0F);
                        this.rotate(5.0F, 0.0F, 1.0F, 0.0F);
                     }

                     if (!partName.contains("right")) {
                        this.translate(0.3F, 0.0F, 0.0F);
                        this.rotate(-5.0F, 0.0F, 1.0F, 0.0F);
                     }
                  }

                  this.uncenterPart(partName);
                  if (this.trophyOffset.length >= 3) {
                     this.translate(this.trophyOffset[0], this.trophyOffset[1], this.trophyOffset[2]);
                  }
               }

               this.uncenterPart(partName);
               this.onRenderStart(layer, entity, trophyModel);
               this.wavefrontObject
                  .renderGroup(
                     part,
                     this.getPartColor(partName, entity, layer, trophyModel, loop),
                     this.getPartTextureOffset(partName, entity, layer, trophyModel, loop),
                     null
                  );
               this.onRenderFinish(layer, entity, trophyModel);
               LegacyRender.popMatrix();
            }
         }
      }
   }

   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      if (!CreatureManager.getInstance().config.disableModelAlpha) {
         LycGL.enableBlend();
      }

      LycGL.blendFunc(LycRenderTypes.BLEND_NORMAL);
      if (layer != null) {
         layer.onRenderStart(entity, renderAsTrophy);
      }
   }

   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      if (!CreatureManager.getInstance().config.disableModelAlpha) {
         LycGL.disableBlend();
      }

      if (layer != null) {
         layer.onRenderFinish(entity, renderAsTrophy);
      }
   }

   public boolean isTrophyPart(String partName) {
      if (partName == null) {
         return false;
      } else {
         partName = partName.toLowerCase();
         return partName.contains("head") || partName.contains("mouth") || partName.contains("eye");
      }
   }

   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
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
      if (partName.toLowerCase().contains("head")) {
         if (!this.lockHeadX) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))));
         }

         if (!this.lockHeadY) {
            rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))));
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }

   public void childScale(String partName) {
      this.scale(0.5F, 0.5F, 0.5F);
   }

   public void rotate(float rotX, float rotY, float rotZ) {
      LegacyRender.rotate(rotX, 1.0F, 0.0F, 0.0F);
      LegacyRender.rotate(rotY, 0.0F, 1.0F, 0.0F);
      LegacyRender.rotate(rotZ, 0.0F, 0.0F, 1.0F);
   }

   public void rotate(float rotation, float angleX, float angleY, float angleZ) {
      LegacyRender.rotate(rotation, angleX, angleY, angleZ);
   }

   public void translate(float posX, float posY, float posZ) {
      LegacyRender.translate(posX, posY, posZ);
   }

   public void scale(float scaleX, float scaleY, float scaleZ) {
      LegacyRender.scale(scaleX, scaleY, scaleZ);
   }

   public void setPartCenter(String partName, float centerX, float centerY, float centerZ) {
      if (this.isTrophyPart(partName)) {
         this.bodyIsTrophy = false;
      }

      this.partCenters.put(partName, new float[]{centerX, centerY, centerZ});
   }

   public void setPartCenters(float centerX, float centerY, float centerZ, String... partNames) {
      for (String partName : partNames) {
         this.setPartCenter(partName, centerX, centerY, centerZ);
      }
   }

   public float[] getPartCenter(String partName) {
      return !this.partCenters.containsKey(partName) ? new float[]{0.0F, 0.0F, 0.0F} : this.partCenters.get(partName);
   }

   public void centerPart(String partName) {
      if (this.partCenters.containsKey(partName)) {
         float[] partCenter = this.partCenters.get(partName);
         this.translate(partCenter[0], partCenter[1], partCenter[2]);
      }
   }

   public void uncenterPart(String partName) {
      if (this.partCenters.containsKey(partName)) {
         float[] partCenter = this.partCenters.get(partName);
         this.translate(-partCenter[0], -partCenter[1], -partCenter[2]);
      }
   }

   public void centerPartToPart(String part, String targetPart) {
      this.uncenterPart(part);
      float[] partCenter = this.partCenters.get(targetPart);
      if (partCenter != null) {
         this.translate(partCenter[0], partCenter[1], partCenter[2]);
      }
   }

   public void uncenterPartToPart(String part, String targetPart) {
      float[] partCenter = this.partCenters.get(targetPart);
      if (partCenter != null) {
         this.translate(-partCenter[0], -partCenter[1], -partCenter[2]);
      }

      this.centerPart(part);
   }

   public float[] comparePartCenters(String centerPartName, String targetPartName) {
      float[] centerPart = this.getPartCenter(centerPartName);
      float[] targetPart = this.getPartCenter(targetPartName);
      float[] partDifference = new float[3];
      if (targetPart == null) {
         return partDifference;
      } else {
         for (int i = 0; i < 3; i++) {
            partDifference[i] = targetPart[i] - centerPart[i];
         }

         return partDifference;
      }
   }

   public void setPartSubCenter(String partName, float centerX, float centerY, float centerZ) {
      this.partSubCenters.put(partName, new float[]{centerX, centerY, centerZ});
   }

   public void setPartSubCenters(float centerX, float centerY, float centerZ, String... partNames) {
      for (String partName : partNames) {
         this.setPartSubCenter(partName, centerX, centerY, centerZ);
      }
   }

   public void subCenterPart(String partName) {
      float[] offset = this.getSubCenterOffset(partName);
      if (offset != null) {
         this.translate(offset[0], offset[1], offset[2]);
      }
   }

   public void unsubCenterPart(String partName) {
      float[] offset = this.getSubCenterOffset(partName);
      if (offset != null) {
         this.translate(-offset[0], -offset[1], -offset[2]);
      }
   }

   public float[] getSubCenterOffset(String partName) {
      if (!this.partCenters.containsKey(partName)) {
         return null;
      } else if (!this.partSubCenters.containsKey(partName)) {
         return null;
      } else {
         float[] partCenter = this.partCenters.get(partName);
         float[] partSubCenter = this.partSubCenters.get(partName);
         float[] offset = new float[3];

         for (int coord = 0; coord < 3; coord++) {
            offset[coord] = partSubCenter[coord] - partCenter[coord];
         }

         return offset;
      }
   }

   public void setOffset(String offsetName, float[] offset) {
      this.offsets.put(offsetName, offset);
   }

   public float[] getOffset(String offsetName) {
      return !this.offsets.containsKey(offsetName) ? new float[]{0.0F, 0.0F, 0.0F} : this.offsets.get(offsetName);
   }

   public double rotateToPoint(double aTarget, double bTarget) {
      return this.rotateToPoint(0.0, 0.0, aTarget, bTarget);
   }

   public double rotateToPoint(double aCenter, double bCenter, double aTarget, double bTarget) {
      if (aTarget - aCenter == 0.0) {
         if (aTarget > aCenter) {
            return 0.0;
         }

         if (aTarget < aCenter) {
            return 180.0;
         }
      }

      if (bTarget - bCenter == 0.0) {
         if (bTarget > bCenter) {
            return 90.0;
         }

         if (bTarget < bCenter) {
            return -90.0;
         }
      }

      return aTarget - aCenter == 0.0 && bTarget - bCenter == 0.0 ? 0.0 : Math.toDegrees(Math.atan2(aCenter - aTarget, bCenter - bTarget) - (Math.PI / 2));
   }

   public double[] rotateToPoint(double xCenter, double yCenter, double zCenter, double xTarget, double yTarget, double zTarget) {
      return new double[]{
         this.rotateToPoint(yCenter, -zCenter, yTarget, -zTarget),
         this.rotateToPoint(-zCenter, xCenter, -zTarget, xTarget),
         this.rotateToPoint(yCenter, xCenter, yTarget, xTarget)
      };
   }
}
