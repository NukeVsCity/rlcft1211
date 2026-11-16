package com.lycanitesmobs.client.obj;

import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class IndexedModel {
   private ArrayList<Vector3f> vertices = new ArrayList<>();
   private ArrayList<Vector2f> texCoords = new ArrayList<>();
   private ArrayList<Vector3f> normals = new ArrayList<>();
   private ArrayList<Vector3f> tangents = new ArrayList<>();
   private ArrayList<Integer> indices = new ArrayList<>();
   private ArrayList<OBJLoader.OBJIndex> objindices = new ArrayList<>();

   public ArrayList<Vector3f> getPositions() {
      return this.vertices;
   }

   public ArrayList<Vector2f> getTexCoords() {
      return this.texCoords;
   }

   public ArrayList<Vector3f> getNormals() {
      return this.normals;
   }

   public ArrayList<Integer> getIndices() {
      return this.indices;
   }

   public ArrayList<Vector3f> getTangents() {
      return this.tangents;
   }

   public void toMesh(Mesh mesh) {
      ArrayList<Vertex> verticesList = new ArrayList<>();
      int n = Math.min(this.vertices.size(), Math.min(this.texCoords.size(), this.normals.size()));

      for (int i = 0; i < n; i++) {
         Vertex vertex = new Vertex(this.vertices.get(i), this.texCoords.get(i), this.normals.get(i), new Vector3f());
         verticesList.add(vertex);
      }

      Integer[] indicesArray = this.indices.toArray(new Integer[0]);
      Vertex[] verticesArray = verticesList.toArray(new Vertex[0]);
      int[] indicesArrayInt = new int[indicesArray.length];

      for (int i = 0; i < indicesArray.length; i++) {
         indicesArrayInt[i] = indicesArray[i];
      }

      mesh.vertices = verticesArray;
      mesh.indices = indicesArrayInt;
   }

   public void computeNormals() {
      for (int i = 0; i < this.indices.size(); i += 3) {
         int x = this.indices.get(i);
         int y = this.indices.get(i + 1);
         int z = this.indices.get(i + 2);
         Vector3f v = new Vector3f(this.vertices.get(y));
         v.sub(this.vertices.get(x));
         Vector3f var10 = new Vector3f(this.vertices.get(z));
         var10.sub(this.vertices.get(x));
         Vector3f var11 = new Vector3f(v);
         v.cross(var10, var11);
         v = new Vector3f(this.normals.get(x));
         v.add(var11);
         this.normals.set(x, v);
         v = new Vector3f(this.normals.get(y));
         v.add(var11);
         this.normals.set(y, v);
         v = new Vector3f(this.normals.get(z));
         v.add(var11);
         this.normals.set(z, v);
      }

      for (int i = 0; i < this.normals.size(); i++) {
         this.normals.get(i).normalize();
      }
   }

   public Vector3f getFaceNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
      Vector3f output = new Vector3f();
      Vector3f calU = new Vector3f(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
      Vector3f calV = new Vector3f(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);
      output.x = calU.y * calV.z - calU.z * calV.y;
      output.y = calU.z * calV.x - calU.x * calV.z;
      output.z = calU.x * calV.y - calU.y * calV.x;
      output.normalize();
      return output;
   }

   public void computeTangents() {
      this.tangents.clear();

      for (int i = 0; i < this.vertices.size(); i++) {
         this.tangents.add(new Vector3f());
      }

      for (int i = 0; i < this.indices.size(); i += 3) {
         int i0 = this.indices.get(i);
         int i1 = this.indices.get(i + 1);
         int i2 = this.indices.get(i + 2);
         Vector3f v = new Vector3f(this.vertices.get(i1));
         v.sub(this.vertices.get(i0));
         Vector3f var23 = new Vector3f(this.vertices.get(i2));
         var23.sub(this.vertices.get(i0));
         double deltaU1 = (double)(this.texCoords.get(i1).x - this.texCoords.get(i0).x);
         double deltaU2 = (double)(this.texCoords.get(i2).x - this.texCoords.get(i0).x);
         double deltaV1 = (double)(this.texCoords.get(i1).y - this.texCoords.get(i0).y);
         double deltaV2 = (double)(this.texCoords.get(i2).y - this.texCoords.get(i0).y);
         double dividend = deltaU1 * deltaV2 - deltaU2 * deltaV1;
         double f = dividend == 0.0 ? 0.0 : 1.0 / dividend;
         Vector3f tangent = new Vector3f(
            (float)(f * (deltaV2 * (double)v.x - deltaV1 * (double)var23.x)),
            (float)(f * (deltaV2 * (double)v.y - deltaV1 * (double)var23.y)),
            (float)(f * (deltaV2 * (double)v.z - deltaV1 * (double)var23.z))
         );
         v = new Vector3f(this.tangents.get(i0));
         v.add(tangent);
         this.tangents.set(i0, v);
         v = new Vector3f(this.tangents.get(i1));
         v.add(tangent);
         this.tangents.set(i1, v);
         v = new Vector3f(this.tangents.get(i2));
         v.add(tangent);
         this.tangents.set(i2, v);
      }

      for (int i = 0; i < this.tangents.size(); i++) {
         this.tangents.get(i).normalize();
      }
   }

   public ArrayList<OBJLoader.OBJIndex> getObjIndices() {
      return this.objindices;
   }

   public org.joml.Vector3f computeCenter() {
      float x = 0.0F;
      float y = 0.0F;
      float z = 0.0F;

      for (Vector3f position : this.vertices) {
         x += position.x;
         y += position.y;
         z += position.z;
      }

      x /= (float)this.vertices.size();
      y /= (float)this.vertices.size();
      z /= (float)this.vertices.size();
      return new org.joml.Vector3f(x, y, z);
   }
}
