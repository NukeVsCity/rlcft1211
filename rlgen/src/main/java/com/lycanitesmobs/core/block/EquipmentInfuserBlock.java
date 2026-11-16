package com.lycanitesmobs.core.block;

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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.tileentity.TileEntityBase;
import java.util.List;


public class EquipmentInfuserBlock extends BlockBase implements ITileEntityProvider {
   public static final DirectionProperty FACING = BlockHorizontal.FACING;

   public EquipmentInfuserBlock(ModInfo group) {
      super(Material.IRON);
      this.group = group;
      this.blockName = "equipment_infuser";
      this.setup();
      this.setHardness(5.0F);
      this.hasTileEntity = true;
      CreativeTabs.add(LycanitesMobs.blocksTab, this);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

      public String getLocalizedName() {
      return LanguageManager.translate(this.getDescriptionId());
   }

      public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   @Override
   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate(this.getDescriptionId() + ".description");
   }

   @Override
   protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState lycOldState, boolean lycMoved) {
      this.setDefaultFacing(world, pos, state);
      super.onPlace(state, world, pos, lycOldState, lycMoved);
   }

   protected void setDefaultFacing(Level world, BlockPos pos, BlockState state) {
      if (!world.isClientSide) {
         BlockState iblockstate = world.getBlockState(pos.north());
         BlockState iblockstate1 = world.getBlockState(pos.south());
         BlockState iblockstate2 = world.getBlockState(pos.west());
         BlockState iblockstate3 = world.getBlockState(pos.east());
         Direction enumfacing = (Direction)state.getValue(FACING);
         if (enumfacing == Direction.NORTH && LycBlocks.isFullBlock(iblockstate) && !LycBlocks.isFullBlock(iblockstate1)) {
            enumfacing = Direction.SOUTH;
         } else if (enumfacing == Direction.SOUTH && LycBlocks.isFullBlock(iblockstate1) && !LycBlocks.isFullBlock(iblockstate)) {
            enumfacing = Direction.NORTH;
         } else if (enumfacing == Direction.WEST && LycBlocks.isFullBlock(iblockstate2) && !LycBlocks.isFullBlock(iblockstate3)) {
            enumfacing = Direction.EAST;
         } else if (enumfacing == Direction.EAST && LycBlocks.isFullBlock(iblockstate3) && !LycBlocks.isFullBlock(iblockstate2)) {
            enumfacing = Direction.WEST;
         }

         world.setBlock(pos, state.setValue(FACING, enumfacing), 2);
      }
   }

   public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      worldIn.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
      super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
   }

   public void breakBlock(Level worldIn, BlockPos pos, BlockState state) {
      BlockEntity tileEntity = worldIn.getBlockEntity(pos);
      if (tileEntity != null && tileEntity instanceof TileEntityBase) {
         ((TileEntityBase)tileEntity).onRemove();
      }

      super.breakBlock(worldIn, pos, state);
      worldIn.removeTileEntity(pos);
   }

   public boolean eventReceived(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam) {
      BlockEntity tileEntity = worldIn.getBlockEntity(pos);
      return tileEntity != null && tileEntity.receiveClientEvent(eventID, eventParam);
   }

   public boolean onBlockActivated(
      Level world, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ
   ) {
      if (!world.isClientSide && playerIn != null) {
         playerIn.openGui(
            LycanitesMobs.instance, GuiHandler.GuiType.TILEENTITY.id, playerIn.level(), pos.getX(), pos.getY(), pos.getZ()
         );
      }

      return true;
   }

   public BlockEntity createNewTileEntity(Level world, int metadata) {
      return new EquipmentInfuserTileEntity();
   }

      public BlockState getStateFromMeta(int meta) {
      Direction enumfacing = Direction.byIndex(meta);
      if (enumfacing.getAxis() == Axis.Y) {
         enumfacing = Direction.NORTH;
      }

      return this.defaultBlockState().setValue(FACING, enumfacing);
   }

      public int getMetaFromState(BlockState state) {
      return ((Direction)state.getValue(FACING)).getIndex();
   }

   public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
      return this.defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
   }
}
