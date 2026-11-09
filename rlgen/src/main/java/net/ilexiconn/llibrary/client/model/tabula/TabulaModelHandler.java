package net.ilexiconn.llibrary.client.model.tabula;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaCubeContainer;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaCubeGroupContainer;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaModelContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * LLibrary 1.7.20 TabulaModelHandler: reads a Tabula .tbl (zip with model.json) into a {@link TabulaModelContainer} with Gson,
 * exactly like 1.12 (the classes have no no-arg constructor, so Gson leaves fields that are absent from the json at their
 * Java defaults, not at the field initialisers). The 1.12 baked item/block model loader (ICustomModelLoader) is not ported:
 * Ice and Fire 1.7.1 does not use it.
 */
@OnlyIn(Dist.CLIENT)
public enum TabulaModelHandler {
   INSTANCE;

   private final Gson gson = new GsonBuilder().create();

   /** @param path classpath path like "/assets/iceandfire/models/tabula/firedragon/dragonFireGround" (".tbl" is added). */
   public TabulaModelContainer loadTabulaModel(String path) throws IOException {
      if (!path.startsWith("/")) {
         path = "/" + path;
      }

      if (!path.endsWith(".tbl")) {
         path = path + ".tbl";
      }

      InputStream stream = TabulaModelHandler.class.getResourceAsStream(path);
      if (stream == null) {
         stream = openResource(path);
      }
      if (stream == null) {
         throw new IOException("Tabula model not found: " + path);
      }
      try (InputStream in = this.getModelJsonStream(path, stream)) {
         return INSTANCE.loadTabulaModel(in);
      }
   }

   public TabulaModelContainer loadTabulaModel(InputStream stream) {
      return this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), TabulaModelContainer.class);
   }

   /** "/assets/<ns>/<path>" through the client resource manager (resource packs), when the class loader cannot see it. */
   private static InputStream openResource(String path) throws IOException {
      if (!path.startsWith("/assets/")) {
         return null;
      }
      String rest = path.substring("/assets/".length());
      int slash = rest.indexOf('/');
      if (slash < 0) {
         return null;
      }
      ResourceLocation location = ResourceLocation.fromNamespaceAndPath(rest.substring(0, slash), rest.substring(slash + 1));
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getResourceManager() == null) {
         return null;
      }
      Optional<Resource> resource = mc.getResourceManager().getResource(location);
      return resource.isPresent() ? resource.get().open() : null;
   }

   public TabulaCubeContainer getCubeByName(String name, TabulaModelContainer model) {
      for (TabulaCubeContainer cube : this.getAllCubes(model)) {
         if (cube.getName().equals(name)) {
            return cube;
         }
      }
      return null;
   }

   public TabulaCubeContainer getCubeByIdentifier(String identifier, TabulaModelContainer model) {
      for (TabulaCubeContainer cube : this.getAllCubes(model)) {
         if (cube.getIdentifier().equals(identifier)) {
            return cube;
         }
      }
      return null;
   }

   public List<TabulaCubeContainer> getAllCubes(TabulaModelContainer model) {
      List<TabulaCubeContainer> cubes = new ArrayList<>();
      for (TabulaCubeGroupContainer cubeGroup : model.getCubeGroups()) {
         cubes.addAll(this.traverse(cubeGroup));
      }
      for (TabulaCubeContainer cube : model.getCubes()) {
         cubes.addAll(this.traverse(cube));
      }
      return cubes;
   }

   private List<TabulaCubeContainer> traverse(TabulaCubeGroupContainer group) {
      List<TabulaCubeContainer> retCubes = new ArrayList<>();
      for (TabulaCubeContainer child : group.getCubes()) {
         retCubes.addAll(this.traverse(child));
      }
      for (TabulaCubeGroupContainer child : group.getCubeGroups()) {
         retCubes.addAll(this.traverse(child));
      }
      return retCubes;
   }

   private List<TabulaCubeContainer> traverse(TabulaCubeContainer cube) {
      List<TabulaCubeContainer> retCubes = new ArrayList<>();
      retCubes.add(cube);
      for (TabulaCubeContainer child : cube.getChildren()) {
         retCubes.addAll(this.traverse(child));
      }
      return retCubes;
   }

   private InputStream getModelJsonStream(String name, InputStream file) throws IOException {
      ZipInputStream zip = new ZipInputStream(file);
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
         if (entry.getName().equals("model.json")) {
            return zip;
         }
      }
      zip.close();
      throw new RuntimeException("No model.json present in " + name);
   }
}
