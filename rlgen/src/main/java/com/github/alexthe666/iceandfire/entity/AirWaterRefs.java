package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Looks up the Ice and Fire 1.7.1 items, blocks and sounds (ModItems / ModBlocks / ModSounds) the air and water mobs use, by their 1.12
 * registry names, so these mobs do not depend at compile time on how the items/blocks part registers them. Missing ones are air.
 */
public final class AirWaterRefs {
   private AirWaterRefs() {
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("iceandfire", path);
   }

   public static Item item(String name) {
      return BuiltInRegistries.ITEM.get(id(name));
   }

   public static boolean is(ItemStack stack, String name) {
      Item item = item(name);
      return item != Items.AIR && stack.is(item);
   }

   public static Block block(String name) {
      return BuiltInRegistries.BLOCK.get(id(name));
   }

   /** ModSounds.X (sound names are the lower case field names) */
   public static SoundEvent sound(String name) {
      SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(id(name));
      return sound != null ? sound : SoundEvent.createVariableRangeEvent(id(name));
   }
}
