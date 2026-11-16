package com.lycanitesmobs.core.item;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
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



public class ItemSlabCustom extends BlockItem {
   private final BlockSlab singleSlab;
   private final Block doubleSlab;

   public ItemSlabCustom(Block block, BlockSlab singleSlab, Block doubleSlab) {
      super(block);
      this.singleSlab = singleSlab;
      this.doubleSlab = doubleSlab;
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
      LycReg.setName(this, LycReg.key(block));
   }

   public int getMetadata(int damage) {
      return damage;
   }

   public String getTranslationKey(ItemStack stack) {
      return this.singleSlab.getTranslationKey(stack.getMetadata());
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return this.block.getLocalizedName();
   }

   public InteractionResult onItemUse(Player player, Level worldIn, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (itemStack.getCount() != 0 && player.canPlayerEdit(pos.relative(facing), facing, itemStack)) {
         Comparable<?> comparable = this.singleSlab.getTypeForItem(itemStack);
         BlockState iblockstate = worldIn.getBlockState(pos);
         if (iblockstate.getBlock() == this.singleSlab) {
            IProperty<?> iproperty = this.singleSlab.getVariantProperty();
            Comparable<?> comparable1 = iblockstate.getValue(iproperty);
            EnumBlockHalf blockslab$enumblockhalf = (EnumBlockHalf)iblockstate.getValue(BlockSlab.HALF);
            if ((
                  facing == Direction.UP && blockslab$enumblockhalf == EnumBlockHalf.BOTTOM
                     || facing == Direction.DOWN && blockslab$enumblockhalf == EnumBlockHalf.TOP
               )
               && comparable1 == comparable) {
               BlockState iblockstate1 = this.makeState(iproperty, comparable1);
               AABB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
               if (axisalignedbb != Block.NULL_AABB
                  && worldIn.checkNoEntityCollision(axisalignedbb.relative(pos))
                  && worldIn.setBlock(pos, iblockstate1, 11)) {
                  SoundType soundtype = this.doubleSlab.getSoundType();
                  worldIn.playSound(
                     player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F
                  );
                  itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
               }

               return InteractionResult.SUCCESS;
            }
         }

         return this.tryPlace(player, itemStack, worldIn, pos.relative(facing), comparable)
            ? InteractionResult.SUCCESS
            : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         return InteractionResult.FAIL;
      }
   }

   public boolean canPlaceBlockOnSide(Level worldIn, BlockPos pos, Direction side, Player player, ItemStack stack) {
      IProperty<?> iproperty = this.singleSlab.getVariantProperty();
      Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
      BlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this.singleSlab) {
         boolean top = iblockstate.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
         if ((side == Direction.UP && !top || side == Direction.DOWN && top) && comparable == iblockstate.getValue(iproperty)) {
            return true;
         }
      }

      BlockPos var11 = pos.relative(side);
      BlockState iblockstate1 = worldIn.getBlockState(var11);
      return iblockstate1.getBlock() == this.singleSlab && comparable == iblockstate1.getValue(iproperty)
         ? true
         : super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
   }

   private boolean tryPlace(Player player, ItemStack itemStack, Level worldIn, BlockPos pos, Object itemSlabType) {
      BlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this.singleSlab) {
         Comparable<?> comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());
         if (comparable == itemSlabType) {
            BlockState iblockstate1 = this.makeState(this.singleSlab.getVariantProperty(), comparable);
            AABB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
            if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.relative(pos)) && worldIn.setBlock(pos, iblockstate1, 11)
               )
             {
               SoundType soundtype = this.doubleSlab.getSoundType();
               worldIn.playSound(
                  player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F
               );
               itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }

            return true;
         }
      }

      return false;
   }

   protected <T extends Comparable<T>> BlockState makeState(IProperty<T> p_185055_1_, Comparable<?> p_185055_2_) {
      return this.doubleSlab.defaultBlockState();
   }
}
