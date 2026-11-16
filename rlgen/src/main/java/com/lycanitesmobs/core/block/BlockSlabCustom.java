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
import java.util.List;
import java.util.Random;


public class BlockSlabCustom extends BlockSlab {
   public static final PropertyEnum<BlockSlabCustom.Variant> VARIANT = PropertyEnum.create("variant", BlockSlabCustom.Variant.class);
   protected Block doubleBlock;

   public BlockSlabCustom(BlockBase block, Block doubleBlock) {
      super(Material.of(block.defaultBlockState()));
      this.doubleBlock = doubleBlock;
      this.setDefaultState(
         this.blockState.getBaseState().setValue(HALF, EnumBlockHalf.BOTTOM).setValue(VARIANT, BlockSlabCustom.Variant.DEFAULT)
      );
      String slabName = "_slab";
      LycReg.setName(this, ResourceLocation.fromNamespaceAndPath(block.group.modid, block.blockName + slabName));
      this.setTranslationKey(block.blockName + slabName);
      block.copyAttributesTo(this);
   }

   public String getTranslationKey(int meta) {
      return super.getDescriptionId();
   }

   public boolean isDouble() {
      return false;
   }

   public Block getDoubleBlock() {
      return this.doubleBlock;
   }

   public String getLocalizedName() {
      return LanguageManager.translate(this.getDescriptionId() + ".name");
   }

   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate(this.getDescriptionId() + ".description");
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public BlockState getStateFromMeta(int meta) {
      return this.defaultBlockState().setValue(HALF, meta == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
   }

   public int getMetaFromState(BlockState state) {
      return state.getValue(HALF) == EnumBlockHalf.BOTTOM ? 0 : 1;
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockSlabCustom.Variant.DEFAULT;
   }

   public Item getItemDropped(BlockState state, RandomSource rand, int fortune) {
      return Item.byBlock(this);
   }

   public ItemStack getItem(Level worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(this);
   }

   public static enum Variant implements StringRepresentable {
      DEFAULT;

      public String getName() {
         return "default";
      }
   }
}
