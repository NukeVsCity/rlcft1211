package com.alcatrazescapee.notreepunching.util.types;

import com.nukevscity.rlgen.RLGen;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 Stone. Enabled like in RLCraft: the defaults plus marble/limestone/basalt (Quark compat on, Quark present), slate off (Rustic
 * compat off). The Quark stones are rlgen's ports (rlgen:marble, rlgen:limestone); Quark 1.12 basalt is minecraft:smooth_basalt here.
 */
public enum Stone {
   STONE(true, false),
   ANDESITE(true, true),
   GRANITE(true, true),
   DIORITE(true, true),
   SANDSTONE(true, false),
   RED_SANDSTONE(true, false),
   MARBLE(false, true),
   LIMESTONE(false, true),
   BASALT(false, true),
   SLATE(false, true);

   private final boolean isDefault;
   private final boolean hasCobblestone;

   Stone(boolean isDefault, boolean hasCobblestone) {
      this.isDefault = isDefault;
      this.hasCobblestone = hasCobblestone;
   }

   /** getFromBlock(state, random) with enableRandomStoneWorldGenVariants off: unknown stone = STONE */
   public static Stone getFromBlockOrStone(BlockState state) {
      Stone stone = getFromBlock(state);
      return stone == null ? STONE : stone;
   }

   @Nullable
   public static Stone getFromBlock(BlockState state) {
      if (state.is(Blocks.STONE)) {
         return STONE;
      } else if (state.is(Blocks.DIORITE)) {
         return DIORITE;
      } else if (state.is(Blocks.GRANITE)) {
         return GRANITE;
      } else if (state.is(Blocks.ANDESITE)) {
         return ANDESITE;
      } else if (state.is(Blocks.SANDSTONE) || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)) {
         return SANDSTONE; // 1.12 Blocks.SANDSTONE had all three variants
      } else if (state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.CHISELED_RED_SANDSTONE) || state.is(Blocks.CUT_RED_SANDSTONE)) {
         return RED_SANDSTONE;
      } else if (state.is(RLGen.LIMESTONE.get())) {
         return LIMESTONE;
      } else if (state.is(RLGen.MARBLE.get())) {
         return MARBLE;
      } else if (state.is(Blocks.SMOOTH_BASALT)) {
         return BASALT;
      }
      return null;
   }

   public ItemStack getStoneStack() {
      return switch (this) {
         case STONE -> new ItemStack(Items.STONE);
         case SANDSTONE -> new ItemStack(Items.SANDSTONE);
         case DIORITE -> new ItemStack(Items.DIORITE);
         case GRANITE -> new ItemStack(Items.GRANITE);
         case ANDESITE -> new ItemStack(Items.ANDESITE);
         case MARBLE -> new ItemStack(RLGen.MARBLE.get());
         case LIMESTONE -> new ItemStack(RLGen.LIMESTONE.get());
         case BASALT -> new ItemStack(Items.SMOOTH_BASALT);
         default -> ItemStack.EMPTY;
      };
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public boolean hasCobblestone() {
      return this.hasCobblestone;
   }

   public boolean isEnabled() {
      return this.isDefault || this == MARBLE || this == LIMESTONE || this == BASALT;
   }
}
