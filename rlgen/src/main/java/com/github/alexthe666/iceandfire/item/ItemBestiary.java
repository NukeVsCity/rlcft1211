package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.enums.EnumBestiaryPages;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 1.12 ItemBestiary: NBT int array "Pages" (EnumBestiaryPages ordinals), INTRODUCTION when it first ticks in an inventory. Right click
 * opens GuiBestiary (client). New pages come from the lectern (bestiary + manuscript).
 */
public class ItemBestiary extends Item {
   /** Set by the client setup: opens GuiBestiary for the stack (1.12 IceAndFire.PROXY.openBestiaryGui). */
   public static volatile java.util.function.Consumer<ItemStack> openGui;

   public ItemBestiary() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.bestiary");
   }

   /** The creative tab's second bestiary, with every page. */
   public static ItemStack allPages(Item item) {
      ItemStack stack = new ItemStack(item);
      int[] pages = new int[EnumBestiaryPages.values().length];
      for (int i = 0; i < pages.length; i++) {
         pages[i] = i;
      }
      CompoundTag tag = new CompoundTag();
      tag.putIntArray("Pages", pages);
      IafMeta.setTag(stack, tag);
      return stack;
   }

   @Override
   public void onCraftedBy(ItemStack itemStack, Level world, Player player) {
      CompoundTag tag = new CompoundTag();
      tag.putIntArray("Pages", new int[]{0});
      IafMeta.setTag(itemStack, tag);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
      ItemStack itemStackIn = playerIn.getItemInHand(hand);
      if (worldIn.isClientSide && openGui != null) {
         openGui.accept(itemStackIn);
      }
      return InteractionResultHolder.pass(itemStackIn);
   }

   @Override
   public void inventoryTick(ItemStack stack, Level world, Entity entity, int f, boolean f1) {
      if (IafMeta.getTag(stack) == null) {
         CompoundTag tag = new CompoundTag();
         tag.putIntArray("Pages", new int[]{EnumBestiaryPages.INTRODUCTION.ordinal()});
         IafMeta.setTag(stack, tag);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag != null) {
         tooltip.add(Component.translatable("bestiary.contains"));
         for (EnumBestiaryPages page : EnumBestiaryPages.containedPages(EnumBestiaryPages.toList(tag.getIntArray("Pages")))) {
            tooltip.add(Component.literal("-").append(Component.translatable("bestiary." + page.toString().toLowerCase())).withStyle(ChatFormatting.WHITE));
         }
      }
   }
}
