package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 1.12 BlockStairsCustom: stairs of a Lycanites block (hardness and blast resistance of that block, like 1.12 BlockStairs). */
public class BlockStairsCustom extends StairBlock {
   public final BlockBase modelBlock;

   public BlockStairsCustom(BlockBase block) {
      super(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block)
         .strength(block.getBlockHardness(), block.getExplosionResistance()).noOcclusion());
      this.modelBlock = block;
      LycReg.setName(this, block.group.modid, block.blockName + "_stairs");
   }

   public BlockStairsCustom setCreativeTab(Object tab) {
      if (tab instanceof com.lycanitesmobs.compat.CreativeTabs tabs) {
         com.lycanitesmobs.compat.CreativeTabs.add(tabs, this);
      }
      return this;
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.modelBlock.blockName + "_stairs.name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.modelBlock.blockName + "_stairs.description");
   }
}
