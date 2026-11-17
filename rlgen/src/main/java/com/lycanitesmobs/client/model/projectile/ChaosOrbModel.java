package com.lycanitesmobs.client.model.projectile;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class ChaosOrbModel extends ModelCreatureObj {
   public ChaosOrbModel() {
      this(1.0F);
   }

   public ChaosOrbModel(float shadowSize) {
      this.initModel("chaosorb", LycanitesMobs.modInfo, "projectile/chaosorb");
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      this.rotate(0.0F, 0.0F, loop * 8.0F);
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      float glowSpeed = 40.0F;
      float glow = loop * glowSpeed % 360.0F;
      float color = (float)Math.cos(Math.toRadians((double)glow)) * 0.1F + 0.9F;
      return new Vector4f(color, color, color, 1.0F);
   }

   @Override
   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderStart(layer, entity, renderAsTrophy);
      int i = 15728880;
      int j = i % 65536;
      int k = i / 65536;
      LycGL.setLightmap(i);
   }

   @Override
   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderFinish(layer, entity, renderAsTrophy);
      int i = LycGL.entityLight();
      int j = i % 65536;
      int k = i / 65536;
      LycGL.setLightmap(i);
   }
}
