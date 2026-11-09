package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.world.LegacyBiomeTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

/**
 * 1.12 EnumTroll, entity side: the biome type each troll spawns in, its weapons and textures. The armour material and the
 * leather/armour/weapon items belong to the items part and are looked up by their 1.12 names.
 */
public enum EnumTroll {
   FOREST("FOREST", Weapon.TRUNK, Weapon.COLUMN_FOREST, Weapon.AXE, Weapon.HAMMER),
   FROST("SNOWY", Weapon.COLUMN_FROST, Weapon.TRUNK_FROST, Weapon.AXE, Weapon.HAMMER),
   MOUNTAIN("MOUNTAIN", Weapon.COLUMN, Weapon.AXE, Weapon.HAMMER);

   public final ResourceLocation TEXTURE;
   public final ResourceLocation TEXTURE_STONE;
   public final ResourceLocation TEXTURE_EYES;
   /** BiomeDictionary.Type name */
   public final String spawnBiome;
   private final Weapon[] weapons;

   EnumTroll(String biome, Weapon... weapons) {
      this.spawnBiome = biome;
      this.weapons = weapons;
      this.TEXTURE = IafLand.rl("textures/models/troll/troll_" + this.name().toLowerCase() + ".png");
      this.TEXTURE_STONE = IafLand.rl("textures/models/troll/troll_" + this.name().toLowerCase() + "_stone.png");
      this.TEXTURE_EYES = IafLand.rl("textures/models/troll/troll_" + this.name().toLowerCase() + "_eyes.png");
   }

   public static EnumTroll getBiomeType(Holder<Biome> biome) {
      List<EnumTroll> types = new ArrayList<>();

      for (EnumTroll type : values()) {
         if (LegacyBiomeTypes.hasType(biome, type.spawnBiome)) {
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

      public final ResourceLocation TEXTURE = IafLand.rl("textures/models/troll/weapon/weapon_" + this.name().toLowerCase() + ".png");

      /** ItemTrollWeapon, 1.12 registry name "troll_weapon.<weapon>" */
      public Item item() {
         Item i = IafLand.item("troll_weapon." + this.name().toLowerCase());
         return i != Items.AIR ? i : IafLand.item("troll_weapon_" + this.name().toLowerCase());
      }
   }
}
