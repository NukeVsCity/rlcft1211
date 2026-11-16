package com.lycanitesmobs.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.obj.ObjObject;
import com.lycanitesmobs.client.obj.TessellatorModel;
import com.lycanitesmobs.client.obj.VBOModel;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.client.renderer.LycRenderTypes;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class ModelCreatureObj extends ModelCustom implements IAnimationModel {
   public static float modelXRotOffset = 180.0F;
   public static float modelYPosOffset = -1.5F;
   public TessellatorModel wavefrontObject;
   public List<ObjObject> wavefrontParts;
   public Map<String, ModelObjPart> animationParts = new HashMap<>();
   public float lookHeadScaleX = 1.0F;
   public float lookHeadScaleY = 1.0F;
   public float lookNeckScaleX = 0.0F;
   public float lookNeckScaleY = 0.0F;
   public float lookBodyScaleX = 0.0F;
   public float lookBodyScaleY = 0.0F;
   public boolean bigChildHead = false;
   public boolean bodyIsTrophy = true;
   public float trophyScale = 1.0F;
   public float[] trophyOffset = new float[0];
   public float[] trophyMouthOffset = new float[0];
   public boolean dontColor = false;
   protected Animator animator;
   protected ModelObjPart currentAnimationPart;
   protected ModelAnimation animation;
   private final Map<Entity, ModelObjState> modelStates = new WeakHashMap<>();
   protected ModelObjState currentModelState;

   public ModelCreatureObj() {
      this(1.0F);
   }

   public ModelCreatureObj(float shadowSize) {
   }

   public ModelCreatureObj initModel(String name, ModInfo groupInfo, String path) {
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(name);
      if (creatureInfo != null && !creatureInfo.enabled) {
         return this;
      } else {
         this.wavefrontObject = new VBOModel(TessellatorModel.location(groupInfo.modid, path + ".obj"));
         this.wavefrontParts = this.wavefrontObject.objObjects;
         if (this.wavefrontParts.isEmpty()) {
            LycanitesMobs.logWarning("", "Unable to load any parts for the " + name + " model!");
         }

         this.animator = new Animator();
         ResourceLocation modelPartsLocation = TessellatorModel.location(groupInfo.modid, path + "_parts.json");

         try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(modelPartsLocation).open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
               JsonArray jsonArray = GsonHelper.fromJson(gson, reader, JsonArray.class);
               Iterator<JsonElement> jsonIterator = jsonArray.iterator();

               while (jsonIterator.hasNext()) {
                  JsonObject partJson = jsonIterator.next().getAsJsonObject();
                  ModelObjPart animationPart = new ModelObjPart();
                  animationPart.loadFromJson(partJson);
                  this.addAnimationPart(animationPart);
               }
            } finally {
               try { reader.close(); } catch (java.io.IOException ignored) { }
            }
         } catch (Exception var26) {
            LycanitesMobs.logWarning("", "There was a problem loading animation parts for " + name + ": " + var26);
         }

         for (ModelObjPart part : this.animationParts.values()) {
            part.addChildren(this.animationParts.values().toArray(new ModelObjPart[this.animationParts.size()]));
         }

         ResourceLocation animationLocation = TessellatorModel.location(groupInfo.modid, path + "_animation.json");

         try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(animationLocation).open();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            try {
               JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class);
               this.animation = new ModelAnimation();
               this.animation.loadFromJson(json);
            } finally {
               try { reader.close(); } catch (java.io.IOException ignored) { }
            }
         } catch (Exception var24) {
            LycanitesMobs.logWarning("Models", "Unable to load animation json for " + name + ".");
         }

         return this;
      }
   }

   public void addAnimationPart(ModelObjPart animationPart) {
      if (!this.animationParts.containsKey(animationPart.name)) {
         if (animationPart.parentName != null && animationPart.parentName.equals(animationPart.name)) {
            animationPart.parentName = null;
         }

         this.animationParts.put(animationPart.name, animationPart);
      }
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      if (this.animation != null) {
         this.animation.addCreatureLayers(renderer);
      }
   }

   @Override
   public void render(Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale, LayerCreatureBase layer, boolean animate) {
      boolean renderAsTrophy = false;
      if (scale < 0.0F) {
         renderAsTrophy = true;
         scale = -scale;
      } else if (entity instanceof BaseCreatureEntity) {
         scale *= 16.0F;
         scale = (float)((double)scale * ((BaseCreatureEntity)entity).getRenderScale());
      } else if (entity instanceof BaseProjectileEntity) {
         scale *= 4.0F;
         scale *= ((BaseProjectileEntity)entity).getProjectileScale();
      }

      if (entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity creature = (BaseCreatureEntity)entity;
         if (creature.onlyRenderTicks >= 0.0F) {
            loop = creature.onlyRenderTicks;
         }
      }

      this.currentModelState = this.getModelState(entity);
      if (layer == null) {
         this.updateAttackProgress(entity);
      }

      if (animate) {
         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPerchTarget()) {
            distance = 0.0F;
         }

         this.generateAnimationFrames(entity, time, distance, loop, lookY, lookX, scale, layer, renderAsTrophy);
      }

      for (ObjObject part : this.wavefrontParts) {
         String partName = part.getName().toLowerCase();
         if (this.canRenderPart(partName, entity, layer, renderAsTrophy)) {
            this.currentAnimationPart = this.animationParts.get(partName);
            if (this.currentAnimationPart != null) {
               LegacyRender.pushMatrix();
               this.animator.doAngle(modelXRotOffset, 1.0F, 0.0F, 0.0F);
               this.animator.doTranslate(0.0F, modelYPosOffset, 0.0F);
               if (this.isChild && !renderAsTrophy) {
                  this.childScale(partName);
                  if (this.bigChildHead && (partName.contains("head") || partName.contains("mouth"))) {
                     this.animator
                        .doTranslate(
                           -(this.currentAnimationPart.centerX / 2.0F),
                           -(this.currentAnimationPart.centerY / 2.0F),
                           -(this.currentAnimationPart.centerZ / 2.0F)
                        );
                  }
               }

               if (renderAsTrophy) {
                  this.animator.doScale(this.trophyScale, this.trophyScale, this.trophyScale);
               }

               this.animator.doScale(scale, scale, scale);
               this.currentAnimationPart.applyAnimationFrames(this.animator);
               this.onRenderStart(layer, entity, renderAsTrophy);
               this.wavefrontObject
                  .renderGroup(
                     part,
                     this.getPartColor(partName, entity, layer, renderAsTrophy, loop),
                     this.getPartTextureOffset(partName, entity, layer, renderAsTrophy, loop),
                     null
                  );
               this.onRenderFinish(layer, entity, renderAsTrophy);
               LegacyRender.popMatrix();
            }
         }
      }

      if (animate) {
         this.clearAnimationFrames();
      }

      this.currentModelState = null;
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

   public void generateAnimationFrames(
      Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale, LayerCreatureBase layer, boolean renderAsTrophy
   ) {
      for (ObjObject part : this.wavefrontParts) {
         String partName = part.getName().toLowerCase();
         this.currentAnimationPart = this.animationParts.get(partName);
         if (this.currentAnimationPart != null) {
            if (entity instanceof Mob) {
               this.animatePart(partName, (Mob)entity, time, distance, loop, -lookY, lookX, scale);
            }

            if (renderAsTrophy) {
               if (partName.contains("head")) {
                  if (!partName.contains("left")) {
                     this.translate(-0.3F, 0.0F, 0.0F);
                     this.angle(5.0F, 0.0F, 1.0F, 0.0F);
                  }

                  if (!partName.contains("right")) {
                     this.translate(0.3F, 0.0F, 0.0F);
                     this.angle(-5.0F, 0.0F, 1.0F, 0.0F);
                  }
               }

               if (this.trophyOffset.length >= 3) {
                  this.translate(this.trophyOffset[0], this.trophyOffset[1], this.trophyOffset[2]);
               }
            }
         }
      }
   }

   public void clearAnimationFrames() {
      for (ModelObjPart animationPart : this.animationParts.values()) {
         animationPart.animationFrames.clear();
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if (partName == null) {
         return false;
      } else {
         partName = partName.toLowerCase();
         if (!this.animationParts.containsKey(partName)) {
            return false;
         } else {
            return trophy && !this.isTrophyPart(partName) ? false : super.canRenderPart(partName, entity, layer, trophy);
         }
      }
   }

   public boolean isTrophyPart(String partName) {
      if (partName == null) {
         return false;
      } else {
         partName = partName.toLowerCase();
         return partName.contains("head") || partName.contains("mouth") || partName.contains("eye") ? true : this.bodyIsTrophy && partName.contains("body");
      }
   }

   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      if (partName.equals("head")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * (double)this.lookHeadScaleX);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * (double)this.lookHeadScaleY);
      }

      if (partName.contains("neck")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * (double)this.lookNeckScaleX);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * (double)this.lookNeckScaleY);
      }

      this.rotate(rotX, rotY, rotZ);
   }

   public ModelObjState getModelState(Entity entity) {
      return entity == null ? null : this.modelStates.computeIfAbsent(entity, k -> new ModelObjState());
   }

   public void childScale(String partName) {
      if (!this.bigChildHead || !partName.contains("head") && !partName.contains("mouth")) {
         this.animator.doScale(0.5F, 0.5F, 0.5F);
      }
   }

   public void updateAttackProgress(Entity entity) {
      if (this.currentModelState != null && entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
         if (this.currentModelState.attackAnimationPlaying) {
            if (this.currentModelState.attackAnimationIncreasing) {
               this.currentModelState.attackAnimationProgress = Math.min(
                  this.currentModelState.attackAnimationProgress + this.currentModelState.attackAnimationSpeed, 1.0F
               );
               if (this.currentModelState.attackAnimationProgress >= 1.0F) {
                  this.currentModelState.attackAnimationIncreasing = false;
               }
            } else {
               this.currentModelState.attackAnimationProgress = Math.max(
                  this.currentModelState.attackAnimationProgress - this.currentModelState.attackAnimationSpeed, 0.0F
               );
               if (this.currentModelState.attackAnimationProgress <= 0.0F) {
                  this.currentModelState.attackAnimationPlaying = false;
               }
            }
         } else if (entityCreature.isAttackOnCooldown()) {
            this.currentModelState.attackAnimationPlaying = true;
            this.currentModelState.attackAnimationIncreasing = true;
            this.currentModelState.attackAnimationProgress = 0.0F;
         }
      }
   }

   public float getAttackProgress() {
      return this.currentModelState == null ? 0.0F : this.currentModelState.attackAnimationProgress;
   }

   @Override
   public void angle(float rotation, float angleX, float angleY, float angleZ) {
      this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("angle", rotation, angleX, angleY, angleZ));
   }

   @Override
   public void rotate(float rotX, float rotY, float rotZ) {
      this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("rotate", 1.0F, rotX, rotY, rotZ));
   }

   @Override
   public void translate(float posX, float posY, float posZ) {
      this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("translate", 1.0F, posX, posY, posZ));
   }

   @Override
   public void scale(float scaleX, float scaleY, float scaleZ) {
      this.currentAnimationPart.addAnimationFrame(new ModelObjAnimationFrame("scale", 1.0F, scaleX, scaleY, scaleZ));
   }

   @Override
   public double rotateToPoint(double aTarget, double bTarget) {
      return this.rotateToPoint(0.0, 0.0, aTarget, bTarget);
   }

   @Override
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

   @Override
   public double[] rotateToPoint(double xCenter, double yCenter, double zCenter, double xTarget, double yTarget, double zTarget) {
      return new double[]{
         this.rotateToPoint(yCenter, -zCenter, yTarget, -zTarget),
         this.rotateToPoint(-zCenter, xCenter, -zTarget, xTarget),
         this.rotateToPoint(yCenter, xCenter, yTarget, xTarget)
      };
   }

   public void shiftOrigin(String fromPartName, String toPartName) {
      ModelObjPart fromPart = this.animationParts.get(fromPartName);
      ModelObjPart toPart = this.animationParts.get(toPartName);
      float offsetX = toPart.centerX - fromPart.centerX;
      float offsetY = toPart.centerY - fromPart.centerY;
      float offsetZ = toPart.centerZ - fromPart.centerZ;
      this.translate(offsetX, offsetY, offsetZ);
   }

   public void shiftOriginBack(String fromPartName, String toPartName) {
      ModelObjPart fromPart = this.animationParts.get(fromPartName);
      ModelObjPart toPart = this.animationParts.get(toPartName);
      float offsetX = toPart.centerX - fromPart.centerX;
      float offsetY = toPart.centerY - fromPart.centerY;
      float offsetZ = toPart.centerZ - fromPart.centerZ;
      this.translate(-offsetX, -offsetY, -offsetZ);
   }
}
