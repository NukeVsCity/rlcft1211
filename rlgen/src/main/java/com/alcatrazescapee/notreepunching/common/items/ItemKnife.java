package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.NoTreePunching;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IShearable;

/**
 * 1.12 ItemKnife (ItemToolCore, damage max(1, material - 1.5) + material, speed -2.4): right click with the item to cut in the other
 * hand (ModRecipes.KNIFE, first match, 3 durability), non-flint knives shear sheep, knives get plant fiber from tall grass.
 */
public class ItemKnife extends TieredItem {
   private final boolean weak;

   public ItemKnife(Tier material, boolean weak) {
      super(material, new Item.Properties().attributes(DiggerItem.createAttributes(material, Math.max(1.0F, material.getAttackDamageBonus() - 1.5F), -2.4F)));
      this.weak = weak;
   }

   public boolean isWeak() {
      return this.weak;
   }

   public record KnifeRecipe(Predicate<ItemStack> input, int inputAmount, List<ItemStack> outputs) {
   }

   private static List<KnifeRecipe> recipes;

   private static KnifeRecipe r(Predicate<ItemStack> in, int amount, ItemStack... out) {
      return new KnifeRecipe(in, amount, List.of(out));
   }

   /** ModRecipes.init: the KNIFE recipes in 1.12 order */
   public static synchronized List<KnifeRecipe> recipes() {
      if (recipes == null) {
         List<KnifeRecipe> l = new ArrayList<>();
         Item fiber = NoTreePunching.GRASS_FIBER.get();
         l.add(r(s -> NoTreePunching.ROCKS.values().stream().anyMatch(i -> s.is(i.get())), 1, new ItemStack(NoTreePunching.FLINT_SHARD.get())));
         l.add(r(s -> s.is(Items.FLINT), 1, new ItemStack(NoTreePunching.FLINT_SHARD.get(), 2)));
         l.add(r(s -> s.is(ItemTags.WOOL), 1, new ItemStack(Items.STRING, 4)));
         l.add(r(s -> s.is(Items.COBWEB), 1, new ItemStack(Items.STRING, 8)));
         l.add(r(s -> s.is(Items.SUGAR_CANE), 1, new ItemStack(fiber, 2)));
         l.add(r(s -> s.is(Items.WHEAT), 1, new ItemStack(fiber, 1), new ItemStack(Items.WHEAT_SEEDS)));
         l.add(r(s -> s.is(Items.LEATHER_BOOTS), 1, new ItemStack(Items.LEATHER, 2)));
         l.add(r(s -> s.is(Items.LEATHER_CHESTPLATE), 1, new ItemStack(Items.LEATHER, 5)));
         l.add(r(s -> s.is(Items.LEATHER_LEGGINGS), 1, new ItemStack(Items.LEATHER, 4)));
         l.add(r(s -> s.is(Items.LEATHER_HELMET), 1, new ItemStack(Items.LEATHER, 3)));
         l.add(r(s -> s.is(Items.MELON), 1, new ItemStack(Items.MELON_SLICE, 9)));
         l.add(r(s -> s.is(Items.MELON_SLICE), 1, new ItemStack(Items.MELON_SEEDS, 1), new ItemStack(fiber)));
         l.add(r(s -> s.is(Items.PUMPKIN), 1, new ItemStack(Items.PUMPKIN_SEEDS, 4), new ItemStack(fiber, 2)));
         l.add(r(s -> s.is(Items.VINE), 1, new ItemStack(fiber, 3)));
         l.add(r(s -> s.is(Items.CACTUS), 1, new ItemStack(fiber, 2)));
         l.add(r(s -> s.is(ItemTags.TALL_FLOWERS) || s.is(Items.TALL_GRASS) || s.is(Items.LARGE_FERN), 1, new ItemStack(fiber, 2)));
         l.add(r(s -> s.is(ItemTags.SMALL_FLOWERS) && !s.is(Items.DANDELION), 1, new ItemStack(fiber)));
         l.add(r(s -> s.is(Items.DANDELION), 1, new ItemStack(fiber)));
         Item[][] leaves = {{Items.OAK_LEAVES, Items.OAK_SAPLING}, {Items.SPRUCE_LEAVES, Items.SPRUCE_SAPLING}, {Items.BIRCH_LEAVES, Items.BIRCH_SAPLING},
            {Items.JUNGLE_LEAVES, Items.JUNGLE_SAPLING}, {Items.ACACIA_LEAVES, Items.ACACIA_SAPLING}, {Items.DARK_OAK_LEAVES, Items.DARK_OAK_SAPLING}};
         for (Item[] p : leaves) {
            l.add(r(s -> s.is(p[0]), 6, new ItemStack(p[1]), new ItemStack(fiber, 2)));
         }
         l.add(r(s -> s.is(ItemTags.SAPLINGS), 1, new ItemStack(fiber, 2), new ItemStack(Items.STICK)));
         recipes = l;
      }
      return recipes;
   }

   public static KnifeRecipe get(ItemStack stack) {
      for (KnifeRecipe r : recipes()) {
         if (!stack.isEmpty() && r.input.test(stack) && stack.getCount() >= r.inputAmount) {
            return r;
         }
      }
      return null;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      InteractionHand handOther = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      ItemStack knifeStack = player.getItemInHand(hand);
      ItemStack cutStack = player.getItemInHand(handOther);
      KnifeRecipe recipe = get(cutStack);
      if (recipe == null) {
         return super.use(world, player, hand);
      }
      if (!world.isClientSide) {
         for (ItemStack out : recipe.outputs) {
            ItemStack stack = out.copy();
            if (!player.getInventory().add(stack)) {
               ItemEntity e = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack);
               e.setDeltaMovement(0, 0, 0);
               world.addFreshEntity(e);
            }
         }
         if (!player.isCreative()) {
            knifeStack.hurtAndBreak(3, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            cutStack.shrink(recipe.inputAmount);
         }
      }
      world.playSound(player, player.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   /** itemInteractionForEntity: non-flint knives shear */
   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (!entity.level().isClientSide && entity instanceof IShearable target && !this.weak) {
         BlockPos pos = entity.blockPosition();
         if (target.isShearable(player, stack, entity.level(), pos)) {
            for (ItemStack drop : target.onSheared(player, stack, entity.level(), pos)) {
               ItemEntity e = entity.spawnAtLocation(drop, 1.0F);
               if (e != null) {
                  var r = entity.getRandom();
                  e.setDeltaMovement(e.getDeltaMovement().add((r.nextFloat() - r.nextFloat()) * 0.1F, r.nextFloat() * 0.05F, (r.nextFloat() - r.nextFloat()) * 0.1F));
               }
            }
            stack.hurtAndBreak(1, entity, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
         }
         return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
   }
}
