package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 1.12 BlockFenceCustom (attributes of its block, 1.12 copyAttributesTo). */
public class BlockFenceCustom extends FenceBlock {
   public final BlockBase modelBlock;

   public BlockFenceCustom(BlockBase block) {
      super(BlockBehaviour.Properties.ofFullCopy(block).strength(block.getBlockHardness(), block.getExplosionResistance()).noOcclusion());
      this.modelBlock = block;
      LycReg.setName(this, block.group.modid, block.blockName + "_fence");
   }

   public BlockFenceCustom setCreativeTab(Object tab) {
      if (tab instanceof com.lycanitesmobs.compat.CreativeTabs tabs) {
         com.lycanitesmobs.compat.CreativeTabs.add(tabs, this);
      }
      return this;
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.modelBlock.blockName + "_fence.name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.modelBlock.blockName + "_fence.description");
   }

   /** 1.12: drops itself (no loot tables) */
   @Override
   protected java.util.List<ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
      return java.util.List.of(new ItemStack(this, 1));
   }
}
