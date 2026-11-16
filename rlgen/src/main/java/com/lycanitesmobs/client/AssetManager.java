package com.lycanitesmobs.client;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import java.util.HashMap;
import java.util.Map;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.resources.ResourceLocation;

/** 1.12 AssetManager (client side): texture and model registries. */
public class AssetManager {
   public static Map<String, ResourceLocation> textures = new HashMap<>();
   public static Map<String, ResourceLocation[]> textureGroups = new HashMap<>();
   public static Map<String, ModelBase> models = new HashMap<>();
   public static Map<ProjectileInfo, ModelBase> projectileModels = new HashMap<>();

   public static void addTexture(String name, ModInfo modInfo, String path) {
      name = name.toLowerCase();
      textures.put(name, ResourceLocation.fromNamespaceAndPath(modInfo.modid, path));
   }

   public static void addTextureGroup(String name, ModInfo modInfo, String[] paths) {
      name = name.toLowerCase();
      ResourceLocation[] textureGroup = new ResourceLocation[paths.length];

      for (int i = 0; i < paths.length; i++) {
         textureGroup[i] = ResourceLocation.fromNamespaceAndPath(modInfo.modid, paths[i]);
      }

      textureGroups.put(name, textureGroup);
   }

   public static void addModel(String name, ModelBase model) {
      name = name.toLowerCase();
      models.put(name, model);
   }

   public static ResourceLocation getTexture(String name) {
      name = name.toLowerCase();
      return !textures.containsKey(name) ? null : textures.get(name);
   }

   public static ResourceLocation[] getTextureGroup(String name) {
      name = name.toLowerCase();
      return !textureGroups.containsKey(name) ? null : textureGroups.get(name);
   }

   public static ModelBase getModel(String name) {
      name = name.toLowerCase();
      return !models.containsKey(name) ? null : models.get(name);
   }

   public static ModelBase getCreatureModel(BaseCreatureEntity entityCreature) {
      if (entityCreature.creatureInfo == null) {
         return null;
      } else if (entityCreature.getSubspecies() != null && entityCreature.getSubspecies().modelClass != null) {
         return getModelInstance(entityCreature.getSubspecies().modelClass.getName());
      } else {
         return getCreatureModel(entityCreature.creatureInfo);
      }
   }

   public static ModelBase getCreatureModel(CreatureInfo creatureInfo) {
      if (creatureInfo != null && creatureInfo.modelClassName != null) {
         return getModelInstance(creatureInfo.modelClassName);
      } else {
         return null;
      }
   }

   /** 1.12 kept one model instance per model class (models.get(modelClass.toString())). */
   private static ModelBase getModelInstance(String className) {
      String key = "class " + className;
      if (models.containsKey(key)) {
         return models.get(key);
      }
      ModelBase creatureModel = null;
      try {
         creatureModel = (ModelBase)Class.forName(className).getConstructor().newInstance();
      } catch (Exception e) {
         LycanitesMobs.logWarning("", "[Creature] Unable to create the model " + className + ": " + e);
      }
      models.put(key, creatureModel);
      return creatureModel;
   }

   /** 1.12 registerModels created every projectile's model (ProjectileInfo.modelClassName) at start; here on first use. */
   public static ModelBase getProjectileModel(ProjectileInfo projectileInfo) {
      if (projectileModels.containsKey(projectileInfo)) {
         return projectileModels.get(projectileInfo);
      }
      ModelBase model = null;
      if (projectileInfo.modelClassName != null) {
         try {
            model = (ModelBase)Class.forName(projectileInfo.modelClassName).getConstructor().newInstance();
         } catch (Exception e) {
            LycanitesMobs.logWarning("", "Unable to load a Projectile model, check that the model class name is correct in the associated projectile json. " + e);
         }
      }
      projectileModels.put(projectileInfo, model);
      return model;
   }
}
