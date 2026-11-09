package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/** 1.12 ItemMyrmexEgg: metadata = caste (0 worker, 1 soldier, 2 royal, 3 sentinel, 4 queen, glowing); placed it is an EntityMyrmexEgg. */
public class ItemMyrmexEgg extends Item {
   boolean isJungle;

   public ItemMyrmexEgg(boolean isJungle) {
      super(new Properties().stacksTo(1));
      this.isJungle = isJungle;
      IafNames.item(this, isJungle ? "iceandfire.myrmex_jungle_egg" : "iceandfire.myrmex_desert_egg");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      int meta = IafMeta.getMetadata(stack);
      String caste = switch (meta) {
         case 1 -> "soldier";
         case 2 -> "royal";
         case 3 -> "sentinel";
         case 4 -> "queen";
         default -> "worker";
      };
      Component c = Component.translatable("myrmex.caste_" + caste + ".name");
      tooltip.add(meta == 4 ? c.copy().withStyle(ChatFormatting.LIGHT_PURPLE) : c);
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      if (ctx.getClickedFace() != Direction.UP) {
         return InteractionResult.PASS;
      }
      Player player = ctx.getPlayer();
      if (player == null || ModEntityHooks.placeMyrmexEgg == null) {
         return InteractionResult.PASS;
      }
      ItemStack itemstack = ctx.getItemInHand();
      if (!ctx.getLevel().isClientSide) {
         ModEntityHooks.placeMyrmexEgg.place(ctx.getLevel(), this.isJungle, IafMeta.getMetadata(itemstack), ctx.getClickedPos(), player);
      }
      if (!player.getAbilities().instabuild) {
         itemstack.shrink(1);
      }
      return InteractionResult.SUCCESS;
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return super.isFoil(stack) || IafMeta.getMetadata(stack) == 4;
   }
}
