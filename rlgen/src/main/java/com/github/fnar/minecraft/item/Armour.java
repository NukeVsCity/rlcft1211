package com.github.fnar.minecraft.item;

import com.github.fnar.util.Color;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Objects;

public class Armour extends RldBaseItem {
   private final ArmourType armourType;
   private Quality quality = Quality.WOOD;
   private Color color = null;

   public Armour(ArmourType armourType) {
      this.armourType = armourType;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.ARMOUR;
   }

   public ArmourType getArmourType() {
      return this.armourType;
   }

   public Quality getQuality() {
      return this.quality;
   }

   public Armour withQuality(Quality quality) {
      this.quality = quality;
      return this;
   }

   public Color getColor() {
      return this.color;
   }

   public Armour withColor(Color color) {
      this.color = color;
      return this;
   }

   public Armour leather() {
      this.withQuality(Quality.WOOD);
      return this;
   }

   public Armour chainmail() {
      this.withQuality(Quality.STONE);
      return this;
   }

   public Armour iron() {
      this.withQuality(Quality.IRON);
      return this;
   }

   public Armour golden() {
      this.withQuality(Quality.GOLD);
      return this;
   }

   public Armour diamond() {
      this.withQuality(Quality.DIAMOND);
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         Armour armour = (Armour)o;
         return this.armourType == armour.armourType && this.color == armour.color && this.quality == armour.quality;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.armourType, this.color, this.quality);
   }

   @Override
   public String toString() {
      return "Armour(super="
         + super.toString()
         + ", armourType="
         + this.getArmourType()
         + ", quality="
         + this.getQuality()
         + ", color="
         + this.getColor()
         + ")";
   }
}
