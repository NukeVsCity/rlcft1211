package com.lycanitesmobs.client.obj;

import com.lycanitesmobs.LycanitesMobs;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector2f;
import org.joml.Vector4f;

/** 1.12 TessellatorModel: loads a Wavefront .obj from the client resources into one {@link Mesh} per object/group. */
public class TessellatorModel extends ObjModel {
   /**
    * 1.12 kept the .obj and *_parts.json / *_animation.json files in assets/&lt;modid&gt;/models/. 1.21 parses every json under
    * models/ as a block model (and logs an error for each of these), so they live in objmodels/ instead.
    */
   public static final String MODEL_DIR = "objmodels/";

   public static ResourceLocation location(String modid, String path) {
      return ResourceLocation.fromNamespaceAndPath(modid, MODEL_DIR + path);
   }

   public TessellatorModel(ResourceLocation resourceLocation) {
      super(resourceLocation.getPath());
      String path = resourceLocation.toString();

      try {
         Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
         if (resource.isEmpty()) {
            throw new java.io.FileNotFoundException(path);
         }
         String content;
         try (InputStream inputStream = resource.get().open()) {
            content = new String(this.read(inputStream), "UTF-8");
         }
         String startPath = path.substring(0, path.lastIndexOf(47) + 1);
         HashMap<ObjObject, IndexedModel> map = new OBJLoader().loadModel(startPath, content);
         this.objObjects.clear();

         for (ObjObject object : map.keySet()) {
            Mesh mesh = new Mesh();
            object.mesh = mesh;
            this.objObjects.add(object);
            map.get(object).toMesh(mesh);
         }
      } catch (Exception e) {
         LycanitesMobs.logWarning("", "Unable to load the obj model " + path + ": " + e);
      }
   }

   @Override
   protected void renderGroupImpl(ObjObject obj, Vector4f color, Vector2f textureOffset) {
   }
}
