package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import org.joml.Vector2f;

public class LayerCreatureScrolling extends LayerCreatureEffect {
   public LayerCreatureScrolling(RenderCreature renderer, String textureSuffix, boolean glow, int blending, boolean subspecies, Vector2f scrollSpeed) {
      super(renderer, textureSuffix, glow, blending, subspecies);
      this.scrollSpeed = scrollSpeed;
   }
}
