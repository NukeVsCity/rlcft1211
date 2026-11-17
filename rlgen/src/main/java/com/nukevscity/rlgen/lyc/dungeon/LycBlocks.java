package com.nukevscity.rlgen.lyc.dungeon;

import com.nukevscity.rlgen.lyc.LycFluids;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Theme block ids as 1.21 blocks: the real Lycanites blocks (stone sets, crystals, fluids, fires, clouds) of the ported mod;
 * ids it does not have fall back to the rlgen stand-ins (stone sets with Lycanites' textures, rlgen fluids, vanilla fire, air).
 */
public final class LycBlocks {
   private static final Map<String, String> SPECIAL = Map.ofEntries(
      Map.entry("minecraft:magma", "minecraft:magma_block"),
      Map.entry("lycanitesmobs:scorchfire", "minecraft:fire"),
      Map.entry("lycanitesmobs:hellfire", "minecraft:fire"),
      Map.entry("lycanitesmobs:primefire", "minecraft:fire"),
      Map.entry("lycanitesmobs:doomfire", "minecraft:fire"),
      Map.entry("lycanitesmobs:shadowfire", "minecraft:soul_fire"),
      Map.entry("lycanitesmobs:poisoncloud", "minecraft:air"),
      Map.entry("lycanitesmobs:frostcloud", "minecraft:air"),
      Map.entry("lycanitesmobs:veswax", "minecraft:honeycomb_block"),
      Map.entry("lycanitesmobs:propolis", "minecraft:honey_block"),
      Map.entry("rustic:honey", "minecraft:honey_block"));

   private LycBlocks() {
   }

   public static Block block(String id) {
      ResourceLocation real = ResourceLocation.tryParse(id);
      if (real != null && real.getNamespace().equals("lycanitesmobs") && BuiltInRegistries.BLOCK.containsKey(real)) {
         return BuiltInRegistries.BLOCK.get(real);
      }
      switch (id) {
         case "lycanitesmobs:ooze":
            return LycFluids.OOZE.block.get();
         case "lycanitesmobs:poison":
            return LycFluids.POISON.block.get();
         case "lycanitesmobs:acid":
            return LycFluids.ACID.block.get();
         case "lycanitesmobs:moglava":
            return LycFluids.MOGLAVA.block.get();
         default:
      }
      String mapped = SPECIAL.get(id);
      if (mapped == null && id.startsWith("lycanitesmobs:")) {
         mapped = "rlgen:" + id.substring("lycanitesmobs:".length());
      }
      ResourceLocation rl = ResourceLocation.tryParse(mapped != null ? mapped : id);
      if (rl == null || !BuiltInRegistries.BLOCK.containsKey(rl)) {
         Lyc.warn("Dungeon", "unknown theme block " + id);
         return Blocks.AIR;
      }
      return BuiltInRegistries.BLOCK.get(rl);
   }
}
