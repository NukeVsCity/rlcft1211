package com.github.alexthe666.iceandfire.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 1.12 Block construction values as 1.21 properties. 1.12 materials become a map colour plus "requires tool" (ROCK and IRON materials
 * only drop with the right tool, like 1.12 Material.isToolNotRequired); the tool/level of setHarvestLevel is in the block tags
 * (mineable/*, needs_*_tool). Explosion resistance: 1.12 setResistance(r) stored r*3 and setHardness(h) raised it to h*5, and the
 * explosion used stored/5, so 1.21 resistance = stored/5.
 */
public final class IafBlockProps {
   private IafBlockProps() {
   }

   public enum Mat {
      GROUND(MapColor.DIRT, false), GRASS(MapColor.GRASS, false), ROCK(MapColor.STONE, true), SAND(MapColor.SAND, false), WOOD(MapColor.WOOD, false),
      IRON(MapColor.METAL, true), PACKED_ICE(MapColor.ICE, false), ICE(MapColor.ICE, false), CLAY(MapColor.CLAY, false), GLASS(MapColor.NONE, false),
      PLANTS(MapColor.PLANT, false);

      final MapColor color;
      final boolean requiresTool;

      Mat(MapColor color, boolean requiresTool) {
         this.color = color;
         this.requiresTool = requiresTool;
      }
   }

   /** 1.12 SoundType names: GROUND is the gravel sound set, PLANT grass, SLIME slime. */
   public static SoundType sound(String s) {
      return switch (s) {
         case "GROUND" -> SoundType.GRAVEL;
         case "STONE" -> SoundType.STONE;
         case "GLASS" -> SoundType.GLASS;
         case "SAND" -> SoundType.SAND;
         case "METAL" -> SoundType.METAL;
         case "WOOD" -> SoundType.WOOD;
         case "PLANT" -> SoundType.GRASS;
         case "SLIME" -> SoundType.SLIME_BLOCK;
         default -> throw new IllegalArgumentException(s);
      };
   }

   /** setHardness(h) then setResistance(r) (BlockGeneric order). */
   public static BlockBehaviour.Properties hardThenRes(Mat mat, float hardness, float resistance, SoundType sound) {
      return base(mat, hardness, resistance * 3.0F / 5.0F, sound);
   }

   /** setResistance(r) then setHardness(h) (BlockDragonOre, BlockLectern order). */
   public static BlockBehaviour.Properties resThenHard(Mat mat, float hardness, float resistance, SoundType sound) {
      return base(mat, hardness, Math.max(resistance * 3.0F, hardness * 5.0F) / 5.0F, sound);
   }

   /** Only setHardness(h). */
   public static BlockBehaviour.Properties hard(Mat mat, float hardness, SoundType sound) {
      return base(mat, hardness, hardness, sound);
   }

   private static BlockBehaviour.Properties base(Mat mat, float hardness, float resistance, SoundType sound) {
      BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(mat.color).strength(hardness, resistance).sound(sound);
      if (mat.requiresTool) {
         p = p.requiresCorrectToolForDrops();
      }
      if (mat == Mat.PLANTS) {
         p = p.noCollission().pushReaction(PushReaction.DESTROY);
      }
      return p;
   }
}
