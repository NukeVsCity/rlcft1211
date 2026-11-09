package com.github.alexthe666.iceandfire.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Shared part of the 1.12 armors with their own ModelBiped (dragon scale, death worm, troll, tide): getArmorTexture returns the 1.12
 * texture (renderIndex 2 = legs uses the "_legs" texture) and {@link #armorModelIndex()} is the 1.12 CommonProxy.getArmorModel index
 * (0/1 fire dragon, 2/3 ice dragon, 4/5 death worm, 6/7 troll, 8/9 tide; odd = legs model) used by the client renderer.
 */
public abstract class ItemCustomModelArmor extends ArmorItem {
   /** 1.12 ItemArmor.renderIndex (0 head .. 3 feet). */
   public final int renderIndex;

   protected ItemCustomModelArmor(Holder<ArmorMaterial> material, int renderIndex, Type type, int durabilityFactor) {
      super(material, type, new Properties().durability(type.getDurability(durabilityFactor)));
      this.renderIndex = renderIndex;
   }

   public abstract int armorModelIndex();

   /** The 1.12 getArmorTexture path. */
   public abstract String armorTexture();

   @Override
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
      return ResourceLocation.parse(this.armorTexture());
   }

   public static Type typeOf(int renderIndex) {
      return switch (renderIndex) {
         case 0 -> Type.HELMET;
         case 1 -> Type.CHESTPLATE;
         case 2 -> Type.LEGGINGS;
         default -> Type.BOOTS;
      };
   }
}
