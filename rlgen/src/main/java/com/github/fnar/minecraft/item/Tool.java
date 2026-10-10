package com.github.fnar.minecraft.item;

import greymerk.roguelike.treasure.loot.Quality;
import java.util.Objects;

public class Tool extends RldBaseItem {
   private final ToolType toolType;
   private Quality quality = Quality.WOOD;

   public Tool(ToolType toolType) {
      this.toolType = toolType;
   }

   public ToolType getToolType() {
      return this.toolType;
   }

   public Quality getQuality() {
      return this.quality;
   }

   public Tool withQuality(Quality quality) {
      this.quality = quality;
      return this;
   }

   public Tool wooden() {
      this.withQuality(Quality.WOOD);
      return this;
   }

   public Tool stone() {
      this.withQuality(Quality.STONE);
      return this;
   }

   public Tool iron() {
      this.withQuality(Quality.IRON);
      return this;
   }

   public Tool golden() {
      this.withQuality(Quality.GOLD);
      return this;
   }

   public Tool diamond() {
      this.withQuality(Quality.DIAMOND);
      return this;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.TOOL;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Tool tool = (Tool)o;
         return this.toolType == tool.toolType && this.quality == tool.quality;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.toolType, this.quality);
   }

   @Override
   public String toString() {
      return "Tool(super=" + super.toString() + ", toolType=" + this.getToolType() + ", quality=" + this.getQuality() + ")";
   }
}
