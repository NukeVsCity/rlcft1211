package com.lycanitesmobs.core.block;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;
import java.util.Random;


public class BlockBase extends Block {
   public ModInfo group;
   public String blockName = "BlockBase";
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   public int tickRate = 0;
   public boolean removeOnTick = false;
   public boolean loopTicks = true;
   public boolean canBeCrushed = false;
   public boolean noEntityCollision = false;
   public boolean noBreakCollision = false;
   public boolean isOpaque = true;

   public BlockBase(Material material, ModInfo group, String name) {
      super(material);
      this.group = group;
      this.blockName = name;
      this.fullBlock = this.isOpaque;
      this.setup();
   }

   public BlockBase(Material material) {
      super(material);
   }

   public void setup() {
      LycReg.setName(this, this.group.modid, this.blockName.toLowerCase());
      this.setTranslationKey(this.blockName);
   }

   public void copyAttributesTo(Block block) {
      block.setHardness(this.blockHardness);
      block.setResistance(this.blockResistance);
   }

   public String getLocalizedName() {
      return LanguageManager.translate(this.getDescriptionId() + ".name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.blockName + ".description");
   }

   public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
      if (this.tickRate > 0) {
         world.scheduleUpdate(pos, this, this.tickRate(world));
      }
   }

   public Item getItemDropped(BlockState state, RandomSource random, int zero) {
      return super.getItemDropped(state, random, zero);
   }

   public int damageDropped(BlockState state) {
      return 0;
   }

   public int quantityDropped(RandomSource random) {
      return 1;
   }

   public BlockStateContainer createBlockState() {
      return super.createBlockState();
   }

   public BlockState getStateFromMeta(int meta) {
      return this.defaultBlockState();
   }

   public int getMetaFromState(BlockState state) {
      return 0;
   }

   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos triggerPos) {
      if (this.canBeCrushed && (block == Blocks.SAND || block == Blocks.GRAVEL)) {
         world.removeBlock(pos, false);
      }

      super.neighborChanged(state, world, pos, block, triggerPos);
   }

   public int tickRate(Level world) {
      return this.tickRate;
   }

   public void updateTick(Level world, BlockPos pos, BlockState state, RandomSource rand) {
      if (!world.isClientSide) {
         if (this.removeOnTick && this.canRemove(world, pos, state, rand)) {
            world.removeBlock(pos, false);
         } else if (this.tickRate > 0 && this.loopTicks) {
            world.scheduleBlockUpdate(pos, this, this.tickRate(world), 1);
         }
      }
   }

   public boolean getTickRandomly() {
      return this.tickRate > 0;
   }

   public boolean canRemove(Level world, BlockPos pos, BlockState state, RandomSource rand) {
      return true;
   }

   public boolean isPassable(BlockGetter world, BlockPos pos) {
      return this.noEntityCollision ? true : super.isPassable(world, pos);
   }

   public boolean isCollidable() {
      return this.noBreakCollision ? false : super.isCollidable();
   }

   public boolean isSideSolid(BlockState base_state, BlockGetter world, BlockPos pos, Direction side) {
      return this.noEntityCollision ? false : super.isSideSolid(base_state, world, pos, side);
   }

   public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
      return super.getBoundingBox(state, world, pos);
   }

   public AABB getCollisionBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
      return this.noEntityCollision ? Block.NULL_AABB : super.getCollisionBoundingBox(state, world, pos);
   }

   public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
      return super.getSelectedBoundingBox(state, world, pos);
   }

   public boolean isOpaqueCube(BlockState state) {
      return this.isOpaque;
   }

   public int getLightOpacity(BlockState state, BlockGetter world, BlockPos pos) {
      return this.isOpaqueCube(state) ? 255 : super.getLightOpacity(state, world, pos);
   }

   public void onEntityCollision(Level world, BlockPos pos, BlockState blockState, Entity entity) {
      super.onEntityCollision(world, pos, blockState, entity);
   }

   public Boolean isEntityInsideMaterial(
      BlockGetter world, BlockPos blockpos, BlockState blockState, Entity entity, double yToTest, Material materialIn, boolean testingHead
   ) {
      return super.isEntityInsideMaterial(world, blockpos, blockState, entity, yToTest, materialIn, testingHead);
   }

   public BlockBase setBlockStepSound(SoundType soundType) {
      return (BlockBase)this.setSoundType(soundType);
   }

   public static enum RENDER_TYPE {
      NONE(-1),
      NORMAL(0),
      CROSS(1),
      TORCH(2),
      FIRE(3),
      FLUID(4);

      public final int id;

      private RENDER_TYPE(int value) {
         this.id = value;
      }

      public int getValue() {
         return this.id;
      }
   }
}
