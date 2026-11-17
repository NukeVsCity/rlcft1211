package com.lycanitesmobs.client.model;

import com.lycanitesmobs.client.renderer.LycGL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.animation.ModelPartAnimation;
import com.lycanitesmobs.client.obj.ObjObject;
import com.lycanitesmobs.client.obj.TessellatorModel;
import com.lycanitesmobs.client.obj.VBOModel;
import com.lycanitesmobs.client.renderer.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.minecraft.client.Minecraft;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public abstract class ModelItemBase implements IAnimationModel {
   public static float modelXRotOffset = 180.0F;
   public static float modelYPosOffset = -1.5F;
   public TessellatorModel wavefrontObject;
   public List<ObjObject> wavefrontParts;
   public Map<String, ModelObjPart> animationParts = new HashMap<>();
   protected Animator animator;
   protected ModelAnimation animation;
   protected ModelObjPart currentAnimationPart;
   protected Map<ItemStack, ModelObjState> modelStates = new HashMap<>();
   protected ModelObjState currentModelState;

   public ModelItemBase initModel(String name, ModInfo groupInfo, String path) {
      this.wavefrontObject = new VBOModel(TessellatorModel.location(groupInfo.modid, path + ".obj"));
      this.wavefrontParts = this.wavefrontObject.objObjects;
      if (this.wavefrontParts.isEmpty()) {
         LycanitesMobs.logWarning("", "Unable to load any parts for the " + name + " model!");
      }

      this.animator = new Animator();
      ResourceLocation animPartsLoc = TessellatorModel.location(groupInfo.modid, path + "_parts.json");

      try {
         Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
         InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(animPartsLoc).open();
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
      } catch (Exception var25) {
         LycanitesMobs.logWarning("", "There was a problem loading animation parts for " + name + ":");
         var25.printStackTrace();
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
      } catch (Exception var23) {
         LycanitesMobs.logWarning("Model", "Unable to load animation json for " + name + ".");
      }

      return this;
   }

   public void addAnimationPart(ModelObjPart animationPart) {
      if (this.animationParts.containsKey(animationPart.name)) {
         LycanitesMobs.logWarning("", "Tried to add an animation part that already exists: " + animationPart.name + ".");
      } else {
         if (animationPart.parentName != null && animationPart.parentName.equals(animationPart.name)) {
            animationPart.parentName = null;
         }

         this.animationParts.put(animationPart.name, animationPart);
      }
   }

   public void addCustomLayers(IItemModelRenderer renderer) {
      if (this.animation != null) {
         this.animation.addItemLayers(renderer);
      }
   }

   public void render(ItemStack itemStack, InteractionHand hand, IItemModelRenderer renderer, ModelObjPart offsetObjPart, LayerItem layer, float loop, boolean animate) {
      if (itemStack != null) {
         if (layer == null && this.animation != null) {
            layer = this.animation.getBaseLayer(renderer);
         }

         renderer.bindItemTexture(this.getTexture(itemStack, layer));
         if (animate) {
            this.generateAnimationFrames(itemStack, layer, loop, offsetObjPart);
         }

         for (ObjObject part : this.wavefrontParts) {
            String partName = part.getName().toLowerCase();
            if (this.canRenderPart(partName, itemStack, layer)) {
               this.currentAnimationPart = this.animationParts.get(partName);
               LegacyRender.pushMatrix();
               this.doAngle(modelXRotOffset, 1.0F, 0.0F, 0.0F);
               this.doTranslate(0.0F, modelYPosOffset, 0.0F);
               this.currentAnimationPart.applyAnimationFrames(this.animator);
               this.onRenderStart(layer, itemStack);
               this.wavefrontObject
                  .renderGroup(part, this.getPartColor(partName, itemStack, layer, loop), this.getPartTextureOffset(partName, itemStack, layer, loop), null);
               this.onRenderFinish(layer, itemStack);
               LegacyRender.popMatrix();
            }
         }

         if (animate) {
            this.clearAnimationFrames();
         }
      }
   }

   public void onRenderStart(LayerItem layer, ItemStack itemStack) {
      if (!CreatureManager.getInstance().config.disableModelAlpha) {
         LycGL.enableBlend();
      }

      LycGL.blendFunc(com.lycanitesmobs.client.renderer.LycRenderTypes.BLEND_NORMAL);
      if (layer != null) {
         layer.onRenderStart(itemStack);
      }
   }

   public void onRenderFinish(LayerItem layer, ItemStack itemStack) {
      if (!CreatureManager.getInstance().config.disableModelAlpha) {
         LycGL.disableBlend();
      }

      if (layer != null) {
         layer.onRenderFinish(itemStack);
      }
   }

   public void generateAnimationFrames(ItemStack itemStack, LayerItem layer, float loop, ModelObjPart offsetObjPart) {
      for (ObjObject part : this.wavefrontParts) {
         String partName = part.getName().toLowerCase();
         if (this.canRenderPart(partName, itemStack, layer)) {
            this.currentAnimationPart = this.animationParts.get(partName);
            this.animatePart(partName, itemStack, loop);
         }
      }
   }

   public void clearAnimationFrames() {
      for (ModelObjPart animationPart : this.animationParts.values()) {
         animationPart.animationFrames.clear();
      }
   }

   public boolean canRenderPart(String partName, ItemStack itemStack, LayerItem layer) {
      if (partName == null) {
         return false;
      } else {
         partName = partName.toLowerCase();
         return this.animationParts.containsKey(partName);
      }
   }

   public void animatePart(String partName, ItemStack itemStack, float loop) {
      if (this.animation != null) {
         for (ModelPartAnimation partAnimation : this.animation.partAnimations) {
            partAnimation.animatePart(this, partName, loop);
         }
      }
   }

   public ResourceLocation getTexture(ItemStack itemStack, LayerItem layer) {
      return null;
   }

   public Vector4f getPartColor(String partName, ItemStack itemStack, LayerItem layer, float loop) {
      return layer != null ? layer.getPartColor(partName, itemStack, loop) : new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public Vector2f getPartTextureOffset(String partName, ItemStack itemStack, LayerItem layer, float loop) {
      return layer != null ? layer.getTextureOffset(partName, itemStack, loop) : new Vector2f(0.0F, 0.0F);
   }

   public void doAngle(float rotation, float angleX, float angleY, float angleZ) {
      LegacyRender.rotate(rotation, angleX, angleY, angleZ);
   }

   public void doRotate(float rotX, float rotY, float rotZ) {
      LegacyRender.rotate(rotX, 1.0F, 0.0F, 0.0F);
      LegacyRender.rotate(rotY, 0.0F, 1.0F, 0.0F);
      LegacyRender.rotate(rotZ, 0.0F, 0.0F, 1.0F);
   }

   public void doTranslate(float posX, float posY, float posZ) {
      LegacyRender.translate(posX, posY, posZ);
   }

   public void doScale(float scaleX, float scaleY, float scaleZ) {
      LegacyRender.scale(scaleX, scaleY, scaleZ);
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
}
