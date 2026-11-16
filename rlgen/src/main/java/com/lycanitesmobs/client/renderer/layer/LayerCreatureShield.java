package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import org.joml.Vector4f;
import com.lycanitesmobs.client.renderer.LycRenderTypes;
import com.lycanitesmobs.client.renderer.LycGL;
import net.minecraft.world.entity.Entity;

public class LayerCreatureShield extends LayerCreatureBase {
   public LayerCreatureShield(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      return !super.canRenderLayer(entity, scale) ? false : entity.isBlocking();
   }

   @Override
   public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
      return "shield".equals(partName);
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public void onRenderStart(Entity entity, boolean trophy) {
      LycGL.setLightmap(15728880);
      LycGL.blendFunc(LycRenderTypes.BLEND_ADD);
      LycGL.enableCull();
   }

   @Override
   public void onRenderFinish(Entity entity, boolean trophy) {
      LycGL.disableCull();
   }

}
