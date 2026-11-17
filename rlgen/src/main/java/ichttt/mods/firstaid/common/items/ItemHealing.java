package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart.PartHealer;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** 1.12 ItemHealing / DefaultItemHealing (bandage, plaster): right click opens the health screen to pick the part (client). */
public class ItemHealing extends Item {
   /** set by the client setup: FirstAid.proxy.showGuiApplyHealth */
   public static java.util.function.Consumer<InteractionHand> openGui = h -> {
   };

   public ItemHealing(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (world.isClientSide) {
         openGui.accept(hand);
      }
      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   /** ClientEventHandler.tooltipItems: "Restores %s hearts total, requires %s per half heart" */
   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      PartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
      if (healer != null) {
         tooltip.add(Component.translatable("firstaid.tooltip.healer", healer.maxHeal / 2, StringUtil.formatTickDuration(healer.ticksPerHeal, 20.0F)));
      }
   }
}
