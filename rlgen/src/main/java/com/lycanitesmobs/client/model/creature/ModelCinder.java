package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import org.joml.Vector2f;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;

public class ModelCinder extends ModelTemplateElemental {
   public ModelCinder() {
      this(1.0F);
   }

   public ModelCinder(float shadowSize) {
      this.initModel("cinder", LycanitesMobs.modInfo, "entity/cinder");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "eyes"));
   }

   @Override
   public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
      return partName.contains("effect") ? super.getBaseTextureOffset(partName, entity, trophy, loop) : new Vector2f(loop, 0.0F);
   }

   @Override
   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderStart(layer, entity, renderAsTrophy);
      LycGL.disableLighting();
   }

   @Override
   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderFinish(layer, entity, renderAsTrophy);
      LycGL.enableLighting();
   }
}
