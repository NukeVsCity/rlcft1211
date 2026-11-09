package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 1.12 ItemStoneStatue: a petrified creature picked up (NBT: its entity NBT plus IAFStoneStatueEntityPlayer, and the entity type; 1.12
 * kept the numeric entity id IAFStoneStatueEntityID, 1.21 has no numeric ids so the type id is in "IAFStoneStatueEntityType").
 * Placing it on top of a block spawns the statue again (ModEntityHooks.placeStoneStatue, iaf/mobs-land).
 */
public class ItemStoneStatue extends Item {
   public ItemStoneStatue() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.stone_statue");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag != null) {
         if (tag.getBoolean("IAFStoneStatueEntityPlayer")) {
            tooltip.add(Component.translatable("entity.player.name"));
         } else if (tag.contains("IAFStoneStatueEntityType")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(tag.getString("IAFStoneStatueEntityType")));
            tooltip.add(type.getDescription());
         }
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      if (ctx.getClickedFace() != Direction.UP) {
         return InteractionResult.FAIL;
      }
      if (ctx.getPlayer() != null && IafMeta.getTag(ctx.getItemInHand()) != null && ModEntityHooks.placeStoneStatue != null) {
         ModEntityHooks.placeStoneStatue.place(ctx.getLevel(), ctx.getPlayer(), ctx.getItemInHand(), ctx.getClickedPos(), ctx.getClickedFace());
      }
      return InteractionResult.SUCCESS;
   }
}
