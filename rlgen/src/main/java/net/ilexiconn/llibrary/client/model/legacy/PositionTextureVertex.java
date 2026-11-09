package net.ilexiconn.llibrary.client.model.legacy;

import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** 1.12 net.minecraft.client.model.PositionTextureVertex. */
@OnlyIn(Dist.CLIENT)
public class PositionTextureVertex {
   public Vec3 vector3D;
   public float texturePositionX;
   public float texturePositionY;

   public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
      this(new Vec3(x, y, z), texU, texV);
   }

   public PositionTextureVertex setTexturePosition(float texU, float texV) {
      return new PositionTextureVertex(this, texU, texV);
   }

   public PositionTextureVertex(PositionTextureVertex vertex, float texU, float texV) {
      this.vector3D = vertex.vector3D;
      this.texturePositionX = texU;
      this.texturePositionY = texV;
   }

   public PositionTextureVertex(Vec3 vector, float texU, float texV) {
      this.vector3D = vector;
      this.texturePositionX = texU;
      this.texturePositionY = texV;
   }
}
