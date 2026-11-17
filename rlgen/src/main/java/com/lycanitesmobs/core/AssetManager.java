package com.lycanitesmobs.core;

import com.lycanitesmobs.core.info.ModInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * The common half of 1.12 com.lycanitesmobs.client.AssetManager (textures, texture groups, sounds), which the 1.12 server code
 * also called. Sounds added before registration are registered in the SOUND_EVENT RegisterEvent (1.12 registered them directly
 * while the Forge registry was unlocked). Models live in the client AssetManager.
 */
public class AssetManager {
   public static Map<String, ResourceLocation> textures = new HashMap<>();
   public static Map<String, ResourceLocation[]> textureGroups = new HashMap<>();
   public static Map<String, SoundEvent> sounds = new LinkedHashMap<>();
   public static boolean soundsLocked = false;

   public static void addTexture(String name, ModInfo modInfo, String path) {
      name = name.toLowerCase();
      textures.put(name, ResourceLocation.fromNamespaceAndPath(modInfo.modid, path.toLowerCase()));
   }

   public static void addTextureGroup(String name, ModInfo modInfo, String[] paths) {
      name = name.toLowerCase();
      ResourceLocation[] textureGroup = new ResourceLocation[paths.length];
      for (int i = 0; i < paths.length; i++) {
         textureGroup[i] = ResourceLocation.fromNamespaceAndPath(modInfo.modid, paths[i].toLowerCase());
      }
      textureGroups.put(name, textureGroup);
   }

   public static void addSound(String name, ModInfo modInfo, String path) {
      if (!soundsLocked) {
         name = name.toLowerCase();
         ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(modInfo.modid, path.toLowerCase());
         if (!sounds.containsKey(name)) {
            sounds.put(name, SoundEvent.createVariableRangeEvent(resourceLocation));
         }
      }
   }

   public static ResourceLocation getTexture(String name) {
      name = name.toLowerCase();
      return textures.get(name);
   }

   public static ResourceLocation[] getTextureGroup(String name) {
      name = name.toLowerCase();
      return textureGroups.get(name);
   }

   public static SoundEvent getSound(String name) {
      name = name.toLowerCase();
      return sounds.get(name);
   }
}
