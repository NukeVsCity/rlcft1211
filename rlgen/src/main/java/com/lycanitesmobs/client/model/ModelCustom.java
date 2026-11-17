package com.lycanitesmobs.client.model;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEquipment;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureSaddle;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class ModelCustom extends ModelBase {
   public Map<ModelRenderer, float[]> initRotations = new HashMap<>();

   public ModelCustom() {
      this(1.0F);
   }

   public ModelCustom(float shadowSize) {
      this.textureWidth = 128;
      this.textureHeight = 128;
   }

   public void addCustomLayers(RenderCreature renderer) {
      renderer.addLayer(new LayerCreatureEquipment(renderer, "chest"));
      renderer.addLayer(new LayerCreatureSaddle(renderer));
   }

   public void setRotation(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
      if (!this.initRotations.containsKey(model)) {
         this.initRotations.put(model, new float[]{x, y, z});
      }
   }

   public void render(Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      this.render(entity, time, distance, loop, lookY, lookX, scale, null, true);
   }

   public void render(Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale, LayerCreatureBase layer, boolean animate) {
      float sizeScale = 1.0F;
      if (entity instanceof BaseCreatureEntity) {
         sizeScale = (float)((double)sizeScale * ((BaseCreatureEntity)entity).getRenderScale());
      }

      LegacyRender.scale(sizeScale, sizeScale, sizeScale);
      LegacyRender.translate(0.0F, 0.5F - sizeScale / 2.0F, 0.0F);
      this.setAngles((Mob)entity, time, distance, loop, lookY, lookX, scale);
      this.animate((Mob)entity, time, distance, loop, lookY, lookX, scale);
   }

   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if (layer == null) {
         return this.canBaseRenderPart(partName, entity, trophy);
      } else {
         return entity instanceof BaseCreatureEntity ? layer.canRenderPart(partName, (BaseCreatureEntity)entity, trophy) : false;
      }
   }

   public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
      return true;
   }

   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return layer != null && entity instanceof BaseCreatureEntity
         ? layer.getPartColor(partName, (BaseCreatureEntity)entity, trophy)
         : this.getBasePartColor(partName, entity, trophy, loop);
   }

   public Vector4f getBasePartColor(String partName, Entity entity, boolean trophy, float loop) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public Vector2f getPartTextureOffset(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return layer != null && entity instanceof BaseCreatureEntity
         ? layer.getTextureOffset(partName, (BaseCreatureEntity)entity, trophy, loop)
         : this.getBaseTextureOffset(partName, entity, trophy, loop);
   }

   public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
      return new Vector2f(0.0F, 0.0F);
   }

   public void setAngles(Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      for (Entry<ModelRenderer, float[]> initRotation : this.initRotations.entrySet()) {
         float[] rotations = initRotation.getValue();
         this.setRotation(initRotation.getKey(), rotations[0], rotations[1], rotations[2]);
      }
   }

   public void animate(Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
   }
}
