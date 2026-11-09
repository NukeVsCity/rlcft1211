package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemHippogryphEgg: metadata = EnumHippogryphTypes ordinal (0..9; 7 raptor, 8 alex, 9 dodo are developer types, not in the tab). */
public class ItemHippogryphEgg extends ItemIafThrowable {
   /** EnumHippogryphTypes names in order (the enum belongs to iaf/mobs-air-water). */
   public static final String[] TYPES = new String[]{"black", "brown", "gray", "chestnut", "creamy", "dark_brown", "white", "raptor", "alex", "dodo"};
   public static final int WILD_TYPES = 7;

   public ItemHippogryphEgg() {
      super(new Properties(), () -> ModEntityHooks.throwHippogryphEgg);
      IafNames.item(this, "iceandfire.hippogryph_egg");
   }

   /** 1.12 createEggStack(parent1, parent2): one of the two parent types at random. */
   public static ItemStack createEggStack(int parent1, int parent2) {
      int eggType = new Random().nextBoolean() ? parent1 : parent2;
      return IafMeta.stack(ModItems.hippogryph_egg, 1, eggType);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      String type = TYPES[Mth.clamp(IafMeta.getMetadata(stack), 0, TYPES.length - 1)];
      tooltip.add(Component.translatable("entity.hippogryph." + type));
   }
}
