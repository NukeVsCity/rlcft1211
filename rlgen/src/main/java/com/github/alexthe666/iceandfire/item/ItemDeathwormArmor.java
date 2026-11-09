package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

/** 1.12 ItemDeathwormArmor (yellow/white/red death worm chitin armor, ModelDeathWormArmor). */
public class ItemDeathwormArmor extends ItemCustomModelArmor {
   public ItemDeathwormArmor(Holder<ArmorMaterial> material, int renderIndex, String name) {
      super(material, renderIndex, typeOf(renderIndex), 15);
      IafNames.item(this, name);
   }

   @Override
   public int armorModelIndex() {
      return this.renderIndex == 2 ? 5 : 4;
   }

   @Override
   public String armorTexture() {
      String color = this.getMaterial() == ModItems.red_deathworm ? "red" : (this.getMaterial() == ModItems.white_deathworm ? "white" : "yellow");
      return "iceandfire:textures/models/armor/armor_deathworm_" + color + (this.renderIndex == 2 ? "_legs.png" : ".png");
   }
}
