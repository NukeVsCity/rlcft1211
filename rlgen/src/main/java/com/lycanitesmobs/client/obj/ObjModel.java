package com.lycanitesmobs.client.obj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector4f;

/** 1.12 ObjModel (without the unused render events). */
public abstract class ObjModel {
   public List<ObjObject> objObjects = new ArrayList<>();
   protected String filename;
   protected String id;

   public ObjModel(String classpathElem) {
      this.filename = classpathElem;
      if (this.filename.contains("/")) {
         this.id = this.filename.substring(this.filename.lastIndexOf("/") + 1);
      } else {
         this.id = this.filename;
      }
   }

   public String getID() {
      return this.id;
   }

   protected byte[] read(InputStream resource) throws IOException {
      byte[] buffer = new byte[65565];
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      int i;
      while ((i = resource.read(buffer, 0, buffer.length)) != -1) {
         out.write(buffer, 0, i);
      }

      out.flush();
      out.close();
      return out.toByteArray();
   }

   public void renderGroup(ObjObject group) {
      this.renderGroup(group, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector2f(0.0F, 0.0F), null);
   }

   public void renderGroup(ObjObject group, Vector4f color, Vector2f textureOffset, Object vertexFormat) {
      this.renderGroupImpl(group, color, textureOffset);
   }

   public void renderGroups(String group) {
      for (ObjObject object : this.objObjects) {
         if (object.getName().equals(group)) {
            this.renderGroup(object);
         }
      }
   }

   protected abstract void renderGroupImpl(ObjObject obj, Vector4f color, Vector2f textureOffset);
}
