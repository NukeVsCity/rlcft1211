package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.common.blocks.BlockPottery;
import com.alcatrazescapee.notreepunching.util.types.Pottery;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 ItemClayTool (ItemToolCore WOOD, damage 0, speed 2.2, 60 uses, no repair): right click a clay block -> worked clay; right
 * click pottery -> 40% chance to become the next shape (a flower pot becomes air). In crafting it stays in the grid with 2 damage
 * (breaks at the max).
 */
public class ItemClayTool extends Item {
   public ItemClayTool(Properties props) {
      super(props.durability(60).setNoRepair().attributes(ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 0.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, 2.2F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .build()));
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Level worldIn = ctx.getLevel();
      Player player = ctx.getPlayer();
      if (!worldIn.isClientSide && player != null) {
         BlockPos pos = ctx.getClickedPos();
         ItemStack stack = ctx.getItemInHand();
         BlockState state = worldIn.getBlockState(pos);
         if (state.getBlock() == Blocks.CLAY) {
            worldIn.setBlockAndUpdate(pos, BlockPottery.get(Pottery.WORKED).defaultBlockState());
            worldIn.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(ctx.getHand()));
         } else if (state.getBlock() instanceof BlockPottery pottery) {
            if (worldIn.random.nextDouble() < 0.4) {
               Pottery next = pottery.getType().next();
               if (next == null) {
                  worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
               } else {
                  worldIn.setBlockAndUpdate(pos, BlockPottery.get(next).defaultBlockState());
               }
            }
            worldIn.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(ctx.getHand()));
         }
      }
      return InteractionResult.SUCCESS;
   }

   @Override
   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      ItemStack copy = stack.copy();
      copy.setDamageValue(copy.getDamageValue() + 2);
      return copy.getDamageValue() >= copy.getMaxDamage() ? ItemStack.EMPTY : copy;
   }

   @Override
   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   /** ToolMaterial.WOOD enchantability 15; only BREAKABLE enchantments (enchantable/durability tag) */
   @Override
   public int getEnchantmentValue() {
      return 15;
   }
}
