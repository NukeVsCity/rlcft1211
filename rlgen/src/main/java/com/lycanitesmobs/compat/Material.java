package com.lycanitesmobs.compat;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 net.minecraft.block.material.Material. 1.21 has no materials, so {@link #of} sorts a block state into the 1.12 material
 * its 1.12 block had (same flags: isSolid, isLiquid, blocksMovement, isReplaceable). Identity compare (==) works like 1.12.
 */
public final class Material {
   public static final Material AIR = new Material("air", false, false, false, true);
   public static final Material GRASS = solid("grass");
   public static final Material GROUND = solid("ground");
   public static final Material WOOD = solid("wood");
   public static final Material ROCK = solid("rock");
   public static final Material IRON = solid("iron");
   public static final Material ANVIL = solid("anvil");
   public static final Material WATER = new Material("water", false, true, false, true);
   public static final Material LAVA = new Material("lava", false, true, false, true);
   public static final Material LEAVES = solid("leaves");
   public static final Material PLANTS = new Material("plants", false, false, false, false);
   public static final Material VINE = new Material("vine", false, false, false, true);
   public static final Material SPONGE = solid("sponge");
   public static final Material CLOTH = solid("cloth");
   public static final Material FIRE = new Material("fire", false, false, false, true);
   public static final Material SAND = solid("sand");
   public static final Material CIRCUITS = new Material("circuits", false, false, false, false);
   public static final Material CARPET = new Material("carpet", false, false, false, false);
   public static final Material GLASS = solid("glass");
   public static final Material REDSTONE_LIGHT = solid("redstone_light");
   public static final Material TNT = solid("tnt");
   public static final Material CORAL = solid("coral");
   public static final Material ICE = solid("ice");
   public static final Material PACKED_ICE = solid("packed_ice");
   public static final Material SNOW = new Material("snow", false, false, false, true);
   public static final Material CRAFTED_SNOW = solid("crafted_snow");
   public static final Material CACTUS = solid("cactus");
   public static final Material CLAY = solid("clay");
   public static final Material GOURD = solid("gourd");
   public static final Material DRAGON_EGG = solid("dragon_egg");
   public static final Material PORTAL = new Material("portal", false, false, false, false);
   public static final Material CAKE = solid("cake");
   public static final Material WEB = new Material("web", true, false, false, false);
   public static final Material PISTON = solid("piston");
   public static final Material BARRIER = solid("barrier");
   public static final Material STRUCTURE_VOID = new Material("structure_void", false, false, false, false);
   // armor materials named like 1.12 ItemArmor.ArmorMaterial (only used as names)
   public static final Material LEATHER = solid("leather");
   public static final Material CHAIN = solid("chain");
   public static final Material GOLD = solid("gold");
   public static final Material DIAMOND = solid("diamond");

   public final String name;
   private final boolean solid;
   private final boolean liquid;
   private final boolean blocksMovement;
   private final boolean replaceable;

   private Material(String name, boolean solid, boolean liquid, boolean blocksMovement, boolean replaceable) {
      this.name = name;
      this.solid = solid;
      this.liquid = liquid;
      this.blocksMovement = blocksMovement;
      this.replaceable = replaceable;
   }

   private static Material solid(String name) {
      return new Material(name, true, false, true, false);
   }

   public boolean isSolid() {
      return this.solid;
   }

   public boolean isLiquid() {
      return this.liquid;
   }

   public boolean blocksMovement() {
      return this.blocksMovement;
   }

   public boolean isReplaceable() {
      return this.replaceable;
   }

   public boolean isOpaque() {
      return this.solid && this != GLASS && this != LEAVES && this != ICE;
   }

   public static Material of(BlockState state) {
      Block b = state.getBlock();
      if (state.isAir()) return AIR;
      if (b == Blocks.WATER || b == Blocks.BUBBLE_COLUMN) return WATER;
      if (b == Blocks.LAVA) return LAVA;
      if (b instanceof LiquidBlock) return state.getFluidState().is(FluidTags.LAVA) ? LAVA : WATER;
      if (b instanceof BaseFireBlock) return FIRE;
      if (b == Blocks.GRASS_BLOCK || b == Blocks.MYCELIUM || b == Blocks.HAY_BLOCK || b == Blocks.SLIME_BLOCK) return GRASS;
      if (b == Blocks.DIRT || b == Blocks.COARSE_DIRT || b == Blocks.PODZOL || b == Blocks.FARMLAND || b == Blocks.DIRT_PATH || b == Blocks.ROOTED_DIRT || b == Blocks.MUD) return GROUND;
      if (b == Blocks.SAND || b == Blocks.RED_SAND || b == Blocks.GRAVEL || b == Blocks.SOUL_SAND || b == Blocks.SOUL_SOIL || b instanceof ConcretePowderBlock) return SAND;
      if (b == Blocks.CLAY) return CLAY;
      if (state.is(BlockTags.LEAVES)) return LEAVES;
      if (b == Blocks.COBWEB) return WEB;
      if (b == Blocks.SNOW) return SNOW;
      if (b == Blocks.SNOW_BLOCK || b == Blocks.POWDER_SNOW) return CRAFTED_SNOW;
      if (b == Blocks.PACKED_ICE || b == Blocks.BLUE_ICE) return PACKED_ICE;
      if (b instanceof IceBlock || b == Blocks.ICE || b == Blocks.FROSTED_ICE) return ICE;
      if (b == Blocks.CACTUS) return CACTUS;
      if (b == Blocks.PUMPKIN || b == Blocks.CARVED_PUMPKIN || b == Blocks.JACK_O_LANTERN || b == Blocks.MELON) return GOURD;
      if (b == Blocks.TNT) return TNT;
      if (b instanceof CakeBlock) return CAKE;
      if (b instanceof NetherPortalBlock || b instanceof EndPortalBlock) return PORTAL;
      if (b instanceof AnvilBlock) return ANVIL;
      if (b == Blocks.BARRIER) return BARRIER;
      if (b == Blocks.DRAGON_EGG) return DRAGON_EGG;
      if (b == Blocks.SPONGE || b == Blocks.WET_SPONGE) return SPONGE;
      if (state.is(BlockTags.WOOL)) return CLOTH;
      if (state.is(BlockTags.WOOL_CARPETS)) return CARPET;
      if (b instanceof TransparentBlock || b instanceof StainedGlassPaneBlock || b == Blocks.GLASS_PANE || b == Blocks.GLOWSTONE || b == Blocks.SEA_LANTERN) return GLASS;
      if (b instanceof VineBlock || b instanceof TallGrassBlock || b instanceof DoublePlantBlock || b instanceof DeadBushBlock || b instanceof SeagrassBlock) return VINE;
      if (b instanceof BushBlock || b instanceof SugarCaneBlock || b instanceof CocoaBlock || b instanceof ChorusPlantBlock || b instanceof ChorusFlowerBlock
         || b instanceof WaterlilyBlock || b instanceof KelpBlock || b instanceof KelpPlantBlock || b instanceof BambooStalkBlock) return PLANTS;
      if (b instanceof BaseCoralPlantTypeBlock || b instanceof CoralBlock) return CORAL;
      if (b instanceof TorchBlock || b instanceof RedStoneWireBlock || b instanceof DiodeBlock || b instanceof LeverBlock || b instanceof ButtonBlock
         || b instanceof BaseRailBlock || b instanceof LadderBlock || b instanceof FlowerPotBlock || b instanceof SkullBlock || b instanceof TripWireBlock
         || b instanceof TripWireHookBlock) return CIRCUITS;
      if (b instanceof net.minecraft.world.level.block.piston.PistonBaseBlock || b instanceof net.minecraft.world.level.block.piston.PistonHeadBlock) return PISTON;
      if (b == Blocks.REDSTONE_LAMP) return REDSTONE_LIGHT;
      if (state.is(BlockTags.MINEABLE_WITH_AXE)) return WOOD;
      if (state.getSoundType() == net.minecraft.world.level.block.SoundType.METAL || state.getSoundType() == net.minecraft.world.level.block.SoundType.NETHERITE_BLOCK) return IRON;
      if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return GROUND;
      if (!state.blocksMotion()) return state.canBeReplaced() ? VINE : CIRCUITS;
      return ROCK;
   }

   @Override
   public String toString() {
      return this.name;
   }
}
