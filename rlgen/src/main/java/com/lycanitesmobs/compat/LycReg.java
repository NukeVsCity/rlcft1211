package com.lycanitesmobs.compat;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** 1.12 IForgeRegistryEntry.getRegistryName/setRegistryName: names set before registration are kept here. */
public final class LycReg {
   private static final Map<Object, ResourceLocation> NAMES = new IdentityHashMap<>();

   private LycReg() {
   }

   public static <T> T setName(T obj, String name) {
      return setName(obj, name.contains(":") ? ResourceLocation.parse(name) : ResourceLocation.fromNamespaceAndPath("lycanitesmobs", name));
   }

   public static <T> T setName(T obj, ResourceLocation name) {
      NAMES.put(obj, name);
      return obj;
   }

   public static <T> T setName(T obj, String modid, String name) {
      return setName(obj, ResourceLocation.fromNamespaceAndPath(modid, name));
   }

   public static ResourceLocation key(Object obj) {
      ResourceLocation set = NAMES.get(obj);
      if (set != null) return set;
      ResourceLocation key = null;
      if (obj instanceof Item i) key = BuiltInRegistries.ITEM.getKey(i);
      else if (obj instanceof Block b) key = BuiltInRegistries.BLOCK.getKey(b);
      else if (obj instanceof EntityType<?> t) key = BuiltInRegistries.ENTITY_TYPE.getKey(t);
      else if (obj instanceof MobEffect m) key = BuiltInRegistries.MOB_EFFECT.getKey(m);
      else if (obj instanceof Fluid f) key = BuiltInRegistries.FLUID.getKey(f);
      return key;
   }

   /** Registries for item NBT (server registries once a server runs, else the built-in ones). */
   public static net.minecraft.core.HolderLookup.Provider access() {
      net.minecraft.core.RegistryAccess access = LycDamage.registries();
      return access != null ? access : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
   }

   public static Object tab(Object obj) {
      return null;
   }
}
