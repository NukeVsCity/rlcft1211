package com.lycanitesmobs.client.obj;

import org.joml.Vector3f;

/** 1.12 Mesh. The VBO is gone: {@link VBOModel} writes the triangles into the 1.21 entity buffer; the face normals are the same. */
public class Mesh {
   public int[] indices;
   public Vertex[] vertices;
   public Vector3f[] normals;

   /** The normals 1.12 Mesh.getVbo() built: one face normal per triangle corner, stored as signed bytes (n * 127). */
   public Vector3f[] getNormals() {
      if (this.normals == null) {
         this.normals = new Vector3f[this.indices.length];

         for (int i = 0; i < this.normals.length / 3; i++) {
            Vector3f v1 = this.vertices[this.indices[i * 3]].getPos();
            Vector3f v2 = this.vertices[this.indices[i * 3 + 1]].getPos();
            Vector3f v3 = this.vertices[this.indices[i * 3 + 2]].getPos();
            Vector3f normal = calcNormal(v1, v2, v3);
            normal.set((byte)((int)(normal.x * 127.0F)) / 127.0F, (byte)((int)(normal.y * 127.0F)) / 127.0F, (byte)((int)(normal.z * 127.0F)) / 127.0F);
            this.normals[i * 3] = normal;
            this.normals[i * 3 + 1] = normal;
            this.normals[i * 3 + 2] = normal;
         }
      }
      return this.normals;
   }

   private static Vector3f calcNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
      Vector3f output = new Vector3f();
      Vector3f calU = new Vector3f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
      Vector3f calV = new Vector3f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
      output.x = calU.y * calV.z - calU.z * calV.y;
      output.y = calU.z * calV.x - calU.x * calV.z;
      output.z = calU.x * calV.y - calU.y * calV.x;
      output.normalize();
      return output;
   }
}
