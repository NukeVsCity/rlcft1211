package com.nukevscity.rlgen;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/** Client-only: Quark TreeVariants' vine colour check, worldIn.getBiome(pos) == Biomes.SWAMPLAND (rlgen:swampland here). */
final class QuarkVineColor {
   static final ResourceKey<Biome> SWAMPLAND = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(RLGen.MODID, "swampland"));

   private QuarkVineColor() {
   }

   static boolean inSwampland(BlockAndTintGetter level, BlockPos pos) {
      LevelReader reader = level instanceof LevelReader r ? r : Minecraft.getInstance().level;
      return reader != null && reader.getBiome(pos).is(SWAMPLAND);
   }
}
