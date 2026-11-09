package com.github.alexthe666.iceandfire.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

/**
 * 1.12 translation keys: {@code setTranslationKey("iceandfire.silverIngot")} made the name key "item.iceandfire.silverIngot.name"
 * (blocks "tile.iceandfire.x.name"). The lang files are the 1.12 .lang files converted 1:1, so items and blocks keep their 1.12 keys.
 */
public final class IafNames {
   private IafNames() {
   }

   public static <T extends Item> T item(T item, String translationKey) {
      ObfuscationReflectionHelper.setPrivateValue(Item.class, item, "item." + translationKey + ".name", "descriptionId");
      return item;
   }

   public static <T extends Block> T block(T block, String translationKey) {
      ObfuscationReflectionHelper.setPrivateValue(Block.class, block, "tile." + translationKey + ".name", "descriptionId");
      return block;
   }
}
