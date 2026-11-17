package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;

/** 1.12 ItemMorphine: eaten in 40 ticks, morphine for 3:30-4:30 (suppresses the part debuffs). */
public class ItemMorphine extends Item {
   public ItemMorphine(Properties props) {
      super(props);
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
      if (entityLiving instanceof Player player && !(entityLiving instanceof FakePlayer)) {
         FirstAid.getDamageModel(player).applyMorphine(player);
      }
      stack.shrink(1);
      return stack;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.EAT;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 40;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("firstaid.tooltip.morphine", "3:30-4:30"));
   }
}
