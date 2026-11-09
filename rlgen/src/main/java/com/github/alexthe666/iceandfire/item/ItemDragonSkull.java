package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;

/**
 * 1.12 ItemDragonSkull (meta 0 fire, 1 ice; NBT Stage, DragonAge; an un-tagged skull becomes stage 4 / age 75 in an inventory).
 * Using it places an EntityDragonSkull (ModEntityHooks.spawnDragonSkull, iaf/dragons).
 * (1.12 also made it the crafting item of the "firedragon"/"icedragon" banner patterns; not ported: 1.21 banner patterns need a
 * loom pattern item.)
 */
public class ItemDragonSkull extends Item {
   public ItemDragonSkull() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.dragon_skull");
   }


   @Override
   public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
      if (IafMeta.getTag(stack) == null) {
         CompoundTag tag = new CompoundTag();
         tag.putInt("Stage", 4);
         tag.putInt("DragonAge", 75);
         IafMeta.setTag(stack, tag);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      String iceorfire = IafMeta.getMetadata(stack) == 0 ? "dragon.fire" : "dragon.ice";
      tooltip.add(Component.translatable(iceorfire));
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag != null) {
         tooltip.add(Component.translatable("dragon.stage").append(String.valueOf(tag.getInt("Stage"))));
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Player player = ctx.getPlayer();
      ItemStack stack = ctx.getItemInHand();
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag != null && player != null && ModEntityHooks.spawnDragonSkull != null) {
         Direction side = ctx.getClickedFace();
         BlockPos offset = ctx.getClickedPos().relative(side, 1);
         float yaw = player.getYRot();
         if (side != Direction.UP) {
            yaw = player.getDirection().toYRot();
         }
         if (!ctx.getLevel().isClientSide) {
            ModEntityHooks.spawnDragonSkull.spawn(ctx.getLevel(), IafMeta.getMetadata(stack), tag.getInt("Stage"), tag.getInt("DragonAge"),
               (double)offset.getX() + 0.5, (double)offset.getY(), (double)offset.getZ() + 0.5, yaw);
         }
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }
      return InteractionResult.SUCCESS;
   }
}
