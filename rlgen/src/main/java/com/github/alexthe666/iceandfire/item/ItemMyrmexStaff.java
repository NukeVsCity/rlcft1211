package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** 1.12 ItemMyrmexStaff: NBT HiveUUID (0-0 until bound by a royal); the hive GUIs are in ModEntityHooks.useMyrmexStaff (iaf/mobs-land). */
public class ItemMyrmexStaff extends Item {
   public ItemMyrmexStaff(boolean jungle) {
      super(new Properties().stacksTo(1));
      IafNames.item(this, jungle ? "iceandfire.myrmex_jungle_staff" : "iceandfire.myrmex_desert_staff");
   }

   @Override
   public void inventoryTick(ItemStack stack, Level world, Entity entity, int f, boolean f1) {
      if (IafMeta.getTag(stack) == null) {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("HiveUUID", new UUID(0L, 0L));
         IafMeta.setTag(stack, tag);
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
      ItemStack itemStackIn = playerIn.getItemInHand(hand);
      if (playerIn.isShiftKeyDown()) {
         return super.use(worldIn, playerIn, hand);
      }
      if (ModEntityHooks.useMyrmexStaff != null) {
         ModEntityHooks.useMyrmexStaff.use(worldIn, playerIn, itemStackIn, null, null);
      }
      playerIn.swing(hand);
      return InteractionResultHolder.pass(itemStackIn);
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Player player = ctx.getPlayer();
      if (player == null || !player.isShiftKeyDown()) {
         return super.useOn(ctx);
      }
      if (ModEntityHooks.useMyrmexStaff != null) {
         ModEntityHooks.useMyrmexStaff.use(ctx.getLevel(), player, ctx.getItemInHand(), ctx.getClickedPos(), player.getDirection());
      }
      player.swing(ctx.getHand());
      return InteractionResult.SUCCESS;
   }
}
