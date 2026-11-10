package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.entity.AirWaterBiomes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** Ice and Fire 1.7.1 EnumHippogryphTypes (BiomeDictionary types as strings). */
public enum EnumHippogryphTypes {
   BLACK(false, "DRY", "HOT", "SANDY"),
   BROWN(false, "MOUNTAIN"),
   GRAY(false, "SPOOKY", "DENSE", "FOREST"),
   CHESTNUT(false, "FOREST"),
   CREAMY(false, "SAVANNA"),
   DARK_BROWN(false, "CONIFEROUS"),
   WHITE(false, "SNOWY"),
   RAPTOR(true),
   ALEX(true),
   DODO(true);

   public final boolean developer;
   public final String[] spawnBiomes;
   public final ResourceLocation TEXTURE;
   public final ResourceLocation TEXTURE_BLINK;

   EnumHippogryphTypes(boolean developer, String... biomes) {
      this.spawnBiomes = biomes;
      this.developer = developer;
      String name = this.name().toLowerCase(Locale.ROOT);
      this.TEXTURE = ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/hippogryph/" + name + ".png");
      this.TEXTURE_BLINK = ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/hippogryph/" + name + "_blink.png");
   }

   public static EnumHippogryphTypes[] getWildTypes() {
      return new EnumHippogryphTypes[]{BLACK, BROWN, GRAY, CHESTNUT, CREAMY, DARK_BROWN, WHITE};
   }

   /** 1.7.1 quirk kept: nextInt(length - 1) never picks WHITE. */
   public static EnumHippogryphTypes getRandomType() {
      return getWildTypes()[new Random().nextInt(getWildTypes().length - 1)];
   }

   public static EnumHippogryphTypes getBiomeType(Holder<Biome> biome) {
      List<EnumHippogryphTypes> types = new ArrayList<>();
      for (EnumHippogryphTypes type : getWildTypes()) {
         for (String biomeType : type.spawnBiomes) {
            if (AirWaterBiomes.hasType(biome, biomeType)) {
               types.add(type);
            }
         }
      }
      if (types.isEmpty()) {
         return getRandomType();
      } else {
         return types.contains(GRAY) && types.contains(CHESTNUT) ? GRAY : types.get(new Random().nextInt(types.size()));
      }
   }
}
