package net.ilexiconn.llibrary.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 1.12 net.minecraft.client.model.TexturedQuad; draw() writes into the current {@link LegacyRender} buffer. */
@OnlyIn(Dist.CLIENT)
public class TexturedQuad {
   public PositionTextureVertex[] vertexPositions;
   public int nVertices;
   private boolean invertNormal;

   public TexturedQuad(PositionTextureVertex[] vertices) {
      this.vertexPositions = vertices;
      this.nVertices = vertices.length;
   }

   public TexturedQuad(PositionTextureVertex[] vertices, int texcoordU1, int texcoordV1, int texcoordU2, int texcoordV2, float textureWidth, float textureHeight) {
      this(vertices);
      float f = 0.0F / textureWidth;
      float f1 = 0.0F / textureHeight;
      vertices[0] = vertices[0].setTexturePosition((float)texcoordU2 / textureWidth - f, (float)texcoordV1 / textureHeight + f1);
      vertices[1] = vertices[1].setTexturePosition((float)texcoordU1 / textureWidth + f, (float)texcoordV1 / textureHeight + f1);
      vertices[2] = vertices[2].setTexturePosition((float)texcoordU1 / textureWidth + f, (float)texcoordV2 / textureHeight - f1);
      vertices[3] = vertices[3].setTexturePosition((float)texcoordU2 / textureWidth - f, (float)texcoordV2 / textureHeight - f1);
   }

   public void flipFace() {
      PositionTextureVertex[] flipped = new PositionTextureVertex[this.vertexPositions.length];
      for (int i = 0; i < this.vertexPositions.length; i++) {
         flipped[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
      }
      this.vertexPositions = flipped;
   }

   /** 1.12 draw(BufferBuilder, scale): positions * scale, normal from the first three vertices (model space, then the normal matrix). */
   public void draw(LegacyRender ctx, float scale) {
      Vec3 a = this.vertexPositions[0].vector3D.subtract(this.vertexPositions[1].vector3D);
      Vec3 b = this.vertexPositions[2].vector3D.subtract(this.vertexPositions[1].vector3D);
      Vec3 n = b.cross(a).normalize();
      float nx = (float)n.x;
      float ny = (float)n.y;
      float nz = (float)n.z;
      if (this.invertNormal) {
         nx = -nx;
         ny = -ny;
         nz = -nz;
      }
      PoseStack.Pose pose = ctx.pose.last();
      Matrix4f matrix = pose.pose();
      Vector3f normal = pose.transformNormal(nx, ny, nz, new Vector3f());
      int color = ctx.color();
      Vector3f pos = new Vector3f();
      for (int i = 0; i < 4; i++) {
         PositionTextureVertex v = this.vertexPositions[i];
         matrix.transformPosition((float)(v.vector3D.x * scale), (float)(v.vector3D.y * scale), (float)(v.vector3D.z * scale), pos);
         ctx.buffer.addVertex(pos.x(), pos.y(), pos.z(), color, v.texturePositionX, v.texturePositionY, ctx.overlay, ctx.light,
            normal.x(), normal.y(), normal.z());
      }
   }
}
