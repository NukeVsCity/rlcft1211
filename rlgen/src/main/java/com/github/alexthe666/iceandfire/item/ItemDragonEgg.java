package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/** 1.12 ItemDragonEgg: placed on top of a block it becomes an EntityDragonEgg (ModEntityHooks.spawnDragonEgg, iaf/dragons). */
public class ItemDragonEgg extends Item {
   public EnumDragonEgg type;

   public ItemDragonEgg(EnumDragonEgg type) {
      super(new Properties().stacksTo(1));
      this.type = type;
      IafNames.item(this, "iceandfire.dragonegg");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("dragon." + this.type.toString().toLowerCase()).withStyle(this.type.color));
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      if (ctx.getClickedFace() != Direction.UP) {
         return InteractionResult.PASS;
      }
      Player player = ctx.getPlayer();
      if (ModEntityHooks.spawnDragonEgg == null || player == null) {
         return InteractionResult.PASS;
      }
      if (!ctx.getLevel().isClientSide) {
         ModEntityHooks.spawnDragonEgg.spawn(ctx.getLevel(), this.type, ctx.getClickedPos(), player);
      }
      ItemStack itemstack = ctx.getItemInHand();
      if (!player.getAbilities().instabuild) {
         itemstack.shrink(1);
      }
      return InteractionResult.SUCCESS;
   }
}
