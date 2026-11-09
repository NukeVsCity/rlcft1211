package com.github.alexthe666.iceandfire.item.block;

import com.github.alexthe666.iceandfire.core.IafMeta;
import java.util.function.IntFunction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 1.12 ItemBlocks with subtypes (ItemBlockPodium, ItemBlockMyrmexResin, BlockJar.ItemBlockJar, BlockPixieHouse.ItemBlockPixieHouse):
 * the metadata is the iceandfire:meta component, the name key comes from the metadata like 1.12 getTranslationKey(stack).
 */
public class ItemBlockMeta extends BlockItem {
   private final IntFunction<String> key;
   public final int subtypes;

   public ItemBlockMeta(Block block, Properties props, int subtypes, IntFunction<String> key) {
      super(block, props);
      this.subtypes = subtypes;
      this.key = key;
   }

   @Override
   public String getDescriptionId(ItemStack stack) {
      return this.key.apply(IafMeta.getMetadata(stack)) + ".name";
   }
}
