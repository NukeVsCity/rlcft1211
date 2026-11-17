package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.common.container.ContainerSmallVessel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

/** 1.12 ItemSmallVessel: stack size 1, 4 slots (ItemHandlerItem), right click (not sneaking) opens the GUI, tooltip lists the contents. */
public class ItemSmallVessel extends Item {
   public static final int SLOTS = 4;

   public ItemSmallVessel(Properties props) {
      super(props.stacksTo(1));
   }

   /** the 4 slots of the stack, saved back into its container component whenever they change */
   public static ItemStackHandler getHandler(ItemStack stack) {
      NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
      stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
      return new ItemStackHandler(items) {
         @Override
         protected void onContentsChanged(int slot) {
            List<ItemStack> copy = new ArrayList<>();
            for (int i = 0; i < this.getSlots(); i++) {
               copy.add(this.getStackInSlot(i).copy());
            }
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(copy));
         }
      };
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (!worldIn.isClientSide && !playerIn.isShiftKeyDown()) {
         ItemStack held = ContainerSmallVessel.heldVessel(playerIn);
         playerIn.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerSmallVessel(id, inv, held), Component.translatable(held.getDescriptionId())));
      }
      return InteractionResultHolder.success(stack);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
      ItemStackHandler cap = getHandler(stack);
      boolean flag = true;
      for (int i = 0; i < cap.getSlots(); i++) {
         ItemStack stack1 = cap.getStackInSlot(i);
         if (!stack1.isEmpty()) {
            if (flag) {
               tooltip.add(Component.literal("Contents:").withStyle(ChatFormatting.GREEN));
               flag = false;
            }
            tooltip.add(Component.literal(stack1.getCount() + "x ").append(stack1.getHoverName()).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
