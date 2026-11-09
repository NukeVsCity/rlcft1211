package net.ilexiconn.llibrary.client.model.legacy;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** 1.12 net.minecraft.client.model.ModelBox (same vertex order, UV layout and mirror flip). */
@OnlyIn(Dist.CLIENT)
public class ModelBox {
   private final PositionTextureVertex[] vertexPositions;
   private final TexturedQuad[] quadList;
   public final float posX1;
   public final float posY1;
   public final float posZ1;
   public final float posX2;
   public final float posY2;
   public final float posZ2;
   public String boxName;

   public ModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta) {
      this(renderer, texU, texV, x, y, z, dx, dy, dz, delta, renderer.mirror);
   }

   public ModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
      this.posX1 = x;
      this.posY1 = y;
      this.posZ1 = z;
      this.posX2 = x + (float)dx;
      this.posY2 = y + (float)dy;
      this.posZ2 = z + (float)dz;
      this.vertexPositions = new PositionTextureVertex[8];
      this.quadList = new TexturedQuad[6];
      float f = x + (float)dx;
      float f1 = y + (float)dy;
      float f2 = z + (float)dz;
      x = x - delta;
      y = y - delta;
      z = z - delta;
      f = f + delta;
      f1 = f1 + delta;
      f2 = f2 + delta;
      if (mirror) {
         float f3 = f;
         f = x;
         x = f3;
      }

      PositionTextureVertex v7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
      PositionTextureVertex v0 = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
      PositionTextureVertex v1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
      PositionTextureVertex v2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
      PositionTextureVertex v3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
      PositionTextureVertex v4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
      PositionTextureVertex v5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
      PositionTextureVertex v6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
      this.vertexPositions[0] = v7;
      this.vertexPositions[1] = v0;
      this.vertexPositions[2] = v1;
      this.vertexPositions[3] = v2;
      this.vertexPositions[4] = v3;
      this.vertexPositions[5] = v4;
      this.vertexPositions[6] = v5;
      this.vertexPositions[7] = v6;
      float tw = renderer.textureWidth;
      float th = renderer.textureHeight;
      this.quadList[0] = new TexturedQuad(new PositionTextureVertex[]{v4, v0, v1, v5}, texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tw, th);
      this.quadList[1] = new TexturedQuad(new PositionTextureVertex[]{v7, v3, v6, v2}, texU, texV + dz, texU + dz, texV + dz + dy, tw, th);
      this.quadList[2] = new TexturedQuad(new PositionTextureVertex[]{v4, v3, v7, v0}, texU + dz, texV, texU + dz + dx, texV + dz, tw, th);
      this.quadList[3] = new TexturedQuad(new PositionTextureVertex[]{v1, v2, v6, v5}, texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, tw, th);
      this.quadList[4] = new TexturedQuad(new PositionTextureVertex[]{v0, v7, v2, v1}, texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tw, th);
      this.quadList[5] = new TexturedQuad(new PositionTextureVertex[]{v3, v4, v5, v6}, texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, tw, th);
      if (mirror) {
         for (TexturedQuad quad : this.quadList) {
            quad.flipFace();
         }
      }
   }

   public void render(LegacyRender ctx, float scale) {
      for (TexturedQuad quad : this.quadList) {
         quad.draw(ctx, scale);
      }
   }

   public ModelBox setBoxName(String name) {
      this.boxName = name;
      return this;
   }
}
