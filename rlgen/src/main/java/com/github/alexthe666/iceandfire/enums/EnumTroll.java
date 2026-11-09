package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

/**
 * 1.12 EnumTroll. spawnBiome is the 1.12 BiomeDictionary type as the matching c: biome tag (FOREST c:is_forest, SNOWY c:is_snowy,
 * MOUNTAIN c:is_mountain). material/leather/armor are set by ModItems at registration; Weapon.item too.
 */
public enum EnumTroll {
   FOREST("is_forest", () -> ModItems.troll_forest, Weapon.TRUNK, Weapon.COLUMN_FOREST, Weapon.AXE, Weapon.HAMMER),
   FROST("is_snowy", () -> ModItems.troll_frost, Weapon.COLUMN_FROST, Weapon.TRUNK_FROST, Weapon.AXE, Weapon.HAMMER),
   MOUNTAIN("is_mountain", () -> ModItems.troll_mountain, Weapon.COLUMN, Weapon.AXE, Weapon.HAMMER);

   public ResourceLocation TEXTURE;
   public ResourceLocation TEXTURE_STONE;
   public ResourceLocation TEXTURE_EYES;
   public TagKey<Biome> spawnBiome;
   private final Supplier<Holder<ArmorMaterial>> materialSupplier;
   private Weapon[] weapons;
   public Item leather;
   public Item helmet;
   public Item chestplate;
   public Item leggings;
   public Item boots;

   private EnumTroll(String biomeTag, Supplier<Holder<ArmorMaterial>> material, Weapon... weapons) {
      this.spawnBiome = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", biomeTag));
      this.weapons = weapons;
      this.materialSupplier = material;
      this.TEXTURE = ResourceLocation.parse("iceandfire:textures/models/troll/troll_" + this.name().toLowerCase() + ".png");
      this.TEXTURE_STONE = ResourceLocation.parse("iceandfire:textures/models/troll/troll_" + this.name().toLowerCase() + "_stone.png");
      this.TEXTURE_EYES = ResourceLocation.parse("iceandfire:textures/models/troll/troll_" + this.name().toLowerCase() + "_eyes.png");
   }

   public Holder<ArmorMaterial> material() {
      return this.materialSupplier.get();
   }

   /** 1.12 getBiomeType: a random troll type whose biome type the biome has, else any type. */
   public static EnumTroll getBiomeType(Holder<Biome> biome) {
      List<EnumTroll> types = new ArrayList<>();
      for (EnumTroll type : values()) {
         if (biome.is(type.spawnBiome)) {
            types.add(type);
         }
      }
      return types.isEmpty() ? values()[new Random().nextInt(values().length)] : types.get(new Random().nextInt(types.size()));
   }

   public static Weapon getWeaponForType(EnumTroll troll) {
      return troll.weapons[new Random().nextInt(troll.weapons.length)];
   }

   public enum Weapon {
      AXE,
      COLUMN,
      COLUMN_FOREST,
      COLUMN_FROST,
      HAMMER,
      TRUNK,
      TRUNK_FROST;

      public ResourceLocation TEXTURE = ResourceLocation.parse("iceandfire:textures/models/troll/weapon/weapon_" + this.name().toLowerCase() + ".png");
      public Item item;
   }
}
