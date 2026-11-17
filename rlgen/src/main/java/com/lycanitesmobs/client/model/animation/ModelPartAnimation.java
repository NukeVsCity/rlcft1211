package com.lycanitesmobs.client.model.animation;

import com.google.gson.JsonObject;
import com.lycanitesmobs.client.model.IAnimationModel;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector4f;
import net.minecraft.world.entity.Mob;

public class ModelPartAnimation {
   public List<String> targetParts = new ArrayList<>();
   public String timeSource = "static";
   public String loopType = "reset";
   public float duration = 0.0F;
   public float offset = 0.0F;
   public Vector3f translate = new Vector3f(0.0F, 0.0F, 0.0F);
   public Vector4f angle = new Vector4f(0.0F, 0.0F, 0.0F, 0.0F);
   public Vector3f rotate = new Vector3f(0.0F, 0.0F, 0.0F);
   public Vector3f scale = new Vector3f(0.0F, 0.0F, 0.0F);

   public void loadFromJson(JsonObject json) {
      if (json.has("targetParts")) {
         this.targetParts = new ArrayList<>(); // 1.12 JSONHelper.getJsonStrings
         for (com.google.gson.JsonElement element : json.getAsJsonArray("targetParts")) {
            this.targetParts.add(element.getAsString());
         }
      }

      if (json.has("timeSource")) {
         this.timeSource = json.get("timeSource").getAsString();
      }

      if (json.has("loopType")) {
         this.loopType = json.get("loopType").getAsString();
      }

      if (json.has("duration")) {
         this.duration = json.get("duration").getAsFloat();
      }

      if (json.has("offset")) {
         this.offset = json.get("offset").getAsFloat();
      }

      float translateX = 0.0F;
      if (json.has("translateX")) {
         translateX = json.get("translateX").getAsFloat();
      }

      float translateY = 0.0F;
      if (json.has("translateY")) {
         translateY = json.get("translateY").getAsFloat();
      }

      float translateZ = 0.0F;
      if (json.has("translateZ")) {
         translateZ = json.get("translateZ").getAsFloat();
      }

      this.translate = new Vector3f(translateX, translateY, translateZ);
      float angleAmount = 0.0F;
      if (json.has("angleAmount")) {
         angleAmount = json.get("angleAmount").getAsFloat();
      }

      float angleX = 0.0F;
      if (json.has("angleX")) {
         angleX = json.get("angleX").getAsFloat();
      }

      float angleY = 0.0F;
      if (json.has("angleY")) {
         angleY = json.get("angleY").getAsFloat();
      }

      float angleZ = 0.0F;
      if (json.has("angleZ")) {
         angleZ = json.get("angleZ").getAsFloat();
      }

      this.angle = new Vector4f(angleX, angleY, angleZ, angleAmount);
      float rotateX = 0.0F;
      if (json.has("rotateX")) {
         rotateX = json.get("rotateX").getAsFloat();
      }

      float rotateY = 0.0F;
      if (json.has("rotateY")) {
         rotateY = json.get("rotateY").getAsFloat();
      }

      float rotateZ = 0.0F;
      if (json.has("rotateZ")) {
         rotateZ = json.get("rotateZ").getAsFloat();
      }

      this.rotate = new Vector3f(rotateX, rotateY, rotateZ);
      float scaleX = 0.0F;
      if (json.has("scaleX")) {
         scaleX = json.get("scaleX").getAsFloat();
      }

      float scaleY = 0.0F;
      if (json.has("scaleY")) {
         scaleY = json.get("scaleY").getAsFloat();
      }

      float scaleZ = 0.0F;
      if (json.has("scaleZ")) {
         scaleZ = json.get("scaleZ").getAsFloat();
      }

      this.scale = new Vector3f(scaleX, scaleY, scaleZ);
   }

   public void animatePart(
      IAnimationModel model, String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale
   ) {
      boolean matchingPart = false;

      for (String targetPart : this.targetParts) {
         if (targetPart.equalsIgnoreCase(partName)) {
            matchingPart = true;
            break;
         }
      }

      if (matchingPart) {
         float progress = loop + this.offset * 20.0F;
         if ("distance".equalsIgnoreCase(this.timeSource)) {
            progress = distance + this.offset;
         } else if ("time".equalsIgnoreCase(this.timeSource)) {
            progress = time + this.offset;
         }

         float duration = this.duration * 20.0F;
         if (duration != 0.0F) {
            if ("reset".equalsIgnoreCase(this.loopType)) {
               progress %= duration;
            } else if ("reverse".equalsIgnoreCase(this.loopType)) {
               float cycleProgress = progress % duration;
               int cycleCount = Math.round((progress - cycleProgress) / duration);
               if (cycleCount % 2 != 0) {
                  cycleProgress = duration - cycleProgress;
               }

               progress = cycleProgress;
            }
         }

         model.translate(this.translate.x * progress, this.translate.y * progress, this.translate.z * progress);
         model.angle(this.angle.w * progress, this.angle.x, this.angle.y, this.angle.z);
         model.rotate(this.rotate.x * progress, this.rotate.y * progress, this.rotate.z * progress);
         model.scale(1.0F + this.scale.x * progress, 1.0F + this.scale.y * progress, 1.0F + this.scale.z * progress);
      }
   }

   public void animatePart(IAnimationModel model, String partName, float loop) {
      this.animatePart(model, partName, null, loop, 0.0F, loop, 0.0F, 0.0F, 1.0F);
   }
}
