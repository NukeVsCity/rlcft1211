package com.lycanitesmobs.client.model;

import java.util.HashMap;
import java.util.Map;

public class ModelObjState {
   public float attackAnimationProgress = 0.0F;
   public boolean attackAnimationIncreasing = false;
   public boolean attackAnimationPlaying = false;
   public float attackAnimationSpeed = 0.1F;
   protected Map<String, Boolean> additionalBooleans = new HashMap<>();
   protected Map<String, Float> additionalFloats = new HashMap<>();

   public void setBoolean(String key, boolean value) {
      this.additionalBooleans.put(key, value);
   }

   public boolean getBoolean(String key) {
      return this.additionalBooleans.containsKey(key) ? this.additionalBooleans.get(key) : false;
   }

   public void setFloat(String key, float value) {
      this.additionalFloats.put(key, value);
   }

   public float getFloat(String key) {
      return this.additionalFloats.containsKey(key) ? this.additionalFloats.get(key) : 0.0F;
   }
}
