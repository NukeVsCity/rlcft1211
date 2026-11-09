package net.ilexiconn.llibrary.client.model.legacy;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** 1.12 net.minecraft.client.model.TextureOffset. */
@OnlyIn(Dist.CLIENT)
public class TextureOffset {
   public final int textureOffsetX;
   public final int textureOffsetY;

   public TextureOffset(int textureOffsetX, int textureOffsetY) {
      this.textureOffsetX = textureOffsetX;
      this.textureOffsetY = textureOffsetY;
   }
}
