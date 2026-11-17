package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 1.12 BlockWallCustom (BlockWall of a Lycanites block: its hardness and resistance). */
public class BlockWallCustom extends WallBlock {
   public final BlockBase modelBlock;

   public BlockWallCustom(BlockBase block) {
      super(BlockBehaviour.Properties.ofFullCopy(block).strength(block.getBlockHardness(), block.getExplosionResistance()).noOcclusion());
      this.modelBlock = block;
      LycReg.setName(this, block.group.modid, block.blockName + "_wall");
   }

   public BlockWallCustom setCreativeTab(Object tab) {
      if (tab instanceof com.lycanitesmobs.compat.CreativeTabs tabs) {
         com.lycanitesmobs.compat.CreativeTabs.add(tabs, this);
      }
      return this;
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.modelBlock.blockName + "_wall.name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.modelBlock.blockName + "_wall.description");
   }
}
