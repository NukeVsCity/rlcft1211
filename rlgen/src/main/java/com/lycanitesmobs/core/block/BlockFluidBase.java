package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.ForgeFluid;
import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * 1.12 BlockFluidBase (a Forge BlockFluidClassic) as a 1.21 LiquidBlock of the Lycanites fluid (ForgeFluid holds the 1.21
 * source/flowing fluids). The Forge hooks the subclasses override are kept: canDisplace(world, pos) is asked for the blocks the
 * fluid flows next to (ooze freezing water, acid melting...), see {@link ForgeFluid}, and onEntityCollision for what touches it.
 */
public class BlockFluidBase extends LiquidBlock {
   public String blockName;
   public ModInfo group;
   public final ForgeFluid fluid;
   public final Material material;
   protected boolean destroyItems = true;
   protected int lightValue = -1;
   protected int lightOpacity = -1;

   public BlockFluidBase(ForgeFluid fluid, Material material, ModInfo group, String blockName) {
      super(fluid.getSource(), BlockBehaviour.Properties.ofFullCopy(material == Material.LAVA ? Blocks.LAVA : Blocks.WATER).pushReaction(PushReaction.DESTROY));
      this.fluid = fluid;
      this.material = material;
      this.blockName = blockName;
      this.group = group;
      fluid.block = this;
      LycReg.setName(this, this.group.modid, this.blockName);
   }

   public BlockFluidBase setLightLevel(float value) {
      this.lightValue = (int)(15.0F * value);
      return this;
   }

   public BlockFluidBase setLightOpacity(int opacity) {
      this.lightOpacity = opacity;
      return this;
   }

   @Override
   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return this.lightValue >= 0 ? this.lightValue : super.getLightEmission(state, level, pos);
   }

   @Override
   protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return this.lightOpacity >= 0 ? Math.min(this.lightOpacity, 15) : super.getLightBlock(state, level, pos);
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.blockName + ".name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.blockName + ".description");
   }

   /** 1.12 BlockFluidClassic level (metadata): 0 is a source block. */
   public int getMetaFromState(BlockState state) {
      return state.hasProperty(LEVEL) ? state.getValue(LEVEL) : 0;
   }

   /** Forge BlockFluidBase.canDisplace: air, or a block that does not block movement and is not a liquid. */
   public boolean canDisplace(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (state.isAir()) {
         return true;
      } else if (state.getBlock() == this) {
         return false;
      } else {
         Material material = Material.of(state);
         return !material.blocksMovement() && material != Material.PORTAL && !material.isLiquid();
      }
   }

   /** Forge BlockFluidBase.displaceIfPossible */
   public boolean displaceIfPossible(Level world, BlockPos pos) {
      if (world.isEmptyBlock(pos)) {
         return true;
      }
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() == this) {
         return false;
      }
      boolean canDisplace = this.canDisplace(world, pos);
      if (canDisplace) {
         world.destroyBlock(pos, true);
      }
      return canDisplace;
   }

   /** Called by the Lycanites fluid when it ticks: lets 1.12 canDisplace see the liquids around (Forge flowed into them). */
   public void checkNeighbours(Level world, BlockPos pos) {
      for (Direction direction : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
         BlockPos next = pos.relative(direction);
         if (Material.of(world.getBlockState(next)).isLiquid() && world.getBlockState(next).getBlock() != this) {
            this.displaceIfPossible(world, next);
         }
      }
   }

   @Override
   protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      this.onEntityCollision(world, pos, state, entity);
   }

   /** 1.12 onEntityCollision */
   public void onEntityCollision(Level world, BlockPos pos, BlockState state, Entity entity) {
      if (this.destroyItems && (entity instanceof ItemEntity || entity instanceof ExperienceOrb)) {
         entity.discard();
      }
   }

   @Override
   public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
      super.animateTick(state, world, pos, random);
   }

   @Override
   protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      super.tick(state, world, pos, random);
   }
}
