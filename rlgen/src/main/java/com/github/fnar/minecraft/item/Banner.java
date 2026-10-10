package com.github.fnar.minecraft.item;

import com.google.common.collect.Lists;
import greymerk.roguelike.util.DyeColor;
import java.util.List;
import java.util.Random;

public class Banner extends RldBaseItem {
   private final List<Design> designs = Lists.newArrayList();

   @Override
   public ItemType getItemType() {
      return ItemType.BANNER;
   }

   public void addRandomPattern(Random random) {
      this.withDesign(DesignPattern.chooseRandom(random).withColor(DyeColor.chooseRandom(random)));
   }

   public Banner withDesign(Design design) {
      this.designs.add(design);
      return this;
   }

   public Banner withDesigns(List<Design> designs) {
      this.designs.addAll(designs);
      return this;
   }

   public List<Design> getDesigns() {
      return this.designs;
   }
}
