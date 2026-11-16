package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 1.12 BlockSlabCustom (1.12 needed a separate double slab block; a 1.21 slab doubles itself). */
public class BlockSlabCustom extends SlabBlock {
   public final BlockBase modelBlock;
   protected Block doubleBlock;

   public BlockSlabCustom(BlockBase block, Block doubleBlock) {
      super(BlockBehaviour.Properties.ofFullCopy(block).strength(block.getBlockHardness(), block.getExplosionResistance()));
      this.modelBlock = block;
      this.doubleBlock = doubleBlock;
      LycReg.setName(this, block.group.modid, block.blockName + "_slab");
   }

   public BlockSlabCustom setCreativeTab(Object tab) {
      if (tab instanceof com.lycanitesmobs.compat.CreativeTabs tabs) {
         com.lycanitesmobs.compat.CreativeTabs.add(tabs, this);
      }
      return this;
   }

   public Block getDoubleBlock() {
      return this.doubleBlock;
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.modelBlock.blockName + "_slab.name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.modelBlock.blockName + "_slab.description");
   }
}
