package com.lycanitesmobs.client.model.animation;

import com.google.gson.JsonObject;
import com.lycanitesmobs.client.renderer.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureLayerAnimation {
   public String name = "base";
   public String textureSuffix = "";
   public boolean subspeciesTexture = true;
   public boolean glow = false;
   public String blending = "normal";
   public Vector2f scrollSpeed = new Vector2f(0.0F, 0.0F);
   public Vector4f colorFadeSpeed;

   public void loadFromJson(JsonObject json) {
      this.name = json.get("name").getAsString().toLowerCase();
      if (json.has("textureSuffix")) {
         this.textureSuffix = json.get("textureSuffix").getAsString();
      }

      if (json.has("subspeciesTexture")) {
         this.subspeciesTexture = json.get("subspeciesTexture").getAsBoolean();
      }

      if (json.has("glow")) {
         this.glow = json.get("glow").getAsBoolean();
      }

      if (json.has("blending")) {
         this.blending = json.get("blending").getAsString().toLowerCase();
      }

      float scrollSpeedX = 0.0F;
      if (json.has("scrollSpeedX")) {
         scrollSpeedX = json.get("scrollSpeedX").getAsFloat();
      }

      float scrollSpeedY = 0.0F;
      if (json.has("scrollSpeedY")) {
         scrollSpeedY = json.get("scrollSpeedY").getAsFloat();
      }

      this.scrollSpeed = new Vector2f(scrollSpeedX, scrollSpeedY);
      float colorFadeRed = 0.0F;
      if (json.has("colorFadeRed")) {
         colorFadeRed = json.get("colorFadeRed").getAsFloat();
      }

      float colorFadeGreen = 0.0F;
      if (json.has("colorFadeGreen")) {
         colorFadeGreen = json.get("colorFadeGreen").getAsFloat();
      }

      float colorFadeBlue = 0.0F;
      if (json.has("colorFadeBlue")) {
         colorFadeBlue = json.get("colorFadeBlue").getAsFloat();
      }

      float colorFadeAlpha = 0.0F;
      if (json.has("colorFadeAlpha")) {
         colorFadeAlpha = json.get("colorFadeAlpha").getAsFloat();
      }

      if (colorFadeRed != 0.0F || colorFadeGreen != 0.0F || colorFadeBlue != 0.0F || colorFadeAlpha != 0.0F) {
         this.colorFadeSpeed = new Vector4f(colorFadeRed, colorFadeGreen, colorFadeBlue, colorFadeAlpha);
      }
   }

   public LayerCreatureEffect createCreatureLayer(RenderCreature renderer) {
      int blendingId = LayerCreatureEffect.BLEND.NORMAL.id;
      if ("add".equals(this.blending)) {
         blendingId = LayerCreatureEffect.BLEND.ADD.id;
      } else if ("sub".equals(this.blending)) {
         blendingId = LayerCreatureEffect.BLEND.SUB.id;
      }

      LayerCreatureEffect renderLayer = new LayerCreatureEffect(renderer, this.textureSuffix, this.glow, blendingId, this.subspeciesTexture);
      renderLayer.name = this.name;
      renderLayer.scrollSpeed = this.scrollSpeed;
      return renderLayer;
   }

   public LayerItem createItemLayer(IItemModelRenderer renderer) {
      int blendingId = LayerCreatureEffect.BLEND.NORMAL.id;
      if ("additive".equals(this.blending)) {
         blendingId = LayerCreatureEffect.BLEND.ADD.id;
      } else if ("subtractive".equals(this.blending)) {
         blendingId = LayerCreatureEffect.BLEND.SUB.id;
      }

      LayerItem renderLayer = new LayerItem(renderer, this.name);
      renderLayer.textureSuffix = this.textureSuffix;
      renderLayer.glow = this.glow;
      renderLayer.blending = blendingId;
      renderLayer.scrollSpeed = this.scrollSpeed;
      renderLayer.colorFadeSpeed = this.colorFadeSpeed;
      return renderLayer;
   }
}
