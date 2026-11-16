package com.lycanitesmobs.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.client.model.animation.ModelPartAnimation;
import com.lycanitesmobs.client.model.animation.TextureLayerAnimation;
import com.lycanitesmobs.client.renderer.RenderCreature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ModelAnimation {
   public Map<String, TextureLayerAnimation> textureLayers = new HashMap<>();
   public List<ModelPartAnimation> partAnimations = new ArrayList<>();

   public void loadFromJson(JsonObject json) {
      if (json.has("textureLayers")) {
         Iterator<JsonElement> textureLayers = json.get("textureLayers").getAsJsonArray().iterator();

         while (textureLayers.hasNext()) {
            JsonObject jsonObject = textureLayers.next().getAsJsonObject();
            TextureLayerAnimation animationLayer = new TextureLayerAnimation();
            animationLayer.loadFromJson(jsonObject);
            this.textureLayers.put(animationLayer.name, animationLayer);
         }
      }

      if (json.has("partAnimations")) {
         Iterator<JsonElement> partAnimations = json.get("partAnimations").getAsJsonArray().iterator();

         while (partAnimations.hasNext()) {
            JsonObject jsonObject = partAnimations.next().getAsJsonObject();
            ModelPartAnimation partAnimation = new ModelPartAnimation();
            partAnimation.loadFromJson(jsonObject);
            this.partAnimations.add(partAnimation);
         }
      }
   }

   public void addCreatureLayers(RenderCreature renderer) {
      for (TextureLayerAnimation textureLayer : this.textureLayers.values()) {
         renderer.addLayer(textureLayer.createCreatureLayer(renderer));
      }
   }
}
