package com.charles445.simpledifficulty.block;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.item.ItemCanteen;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.LegacyClimate;
import com.charles445.simpledifficulty.util.ThirstUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** 1.12 BlockRainCollector: fills one level per successful random tick in rain (1 in RainCollectorFillChance), purified water. */
public class BlockRainCollector extends Block {
   public static final MapCodec<BlockRainCollector> CODEC = simpleCodec(BlockRainCollector::new);
   public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

   public BlockRainCollector(Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
   }

   @Override
   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LEVEL);
   }

   @Override
   protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (world.random.nextInt(Math.max(1, ModConfig.server.miscellaneous.rainCollectorFillChance)) == 0 && LegacyClimate.isRainingAt(world, pos.above())) {
         float f = LegacyClimate.getTemperature(LegacyClimate.getBiome(world, pos), pos);
         if (f >= 0.15F) {
            BlockState iblockstate = world.getBlockState(pos);
            if (iblockstate.getValue(LEVEL) < 3) {
               world.setBlock(pos, iblockstate.cycle(LEVEL), 2);
            }
         }
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      if (player.isShiftKeyDown()) {
         int amount = state.getValue(LEVEL);
         if (amount > 0 && SDCapabilities.getThirstData(player).isThirsty()) {
            DamageUtil.commonPlayPlayerSound(player, SoundEvents.GENERIC_DRINK);
            if (!world.isClientSide) {
               this.setWaterLevel(world, pos, state, amount - 1);
               ThirstUtil.takeDrink(player, ThirstEnum.PURIFIED);
            }
         }
      }
      return InteractionResult.sidedSuccess(world.isClientSide);
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack itemstack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                             BlockHitResult hit) {
      if (itemstack.isEmpty()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
      int amount = state.getValue(LEVEL);
      Item item = itemstack.getItem();
      if (item == Items.BUCKET) {
         if (amount > 0 && !world.isClientSide) {
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
               if (itemstack.isEmpty()) {
                  player.setItemInHand(hand, ThirstUtil.createPurifiedWaterBucket());
               } else if (!player.getInventory().add(ThirstUtil.createPurifiedWaterBucket())) {
                  player.drop(ThirstUtil.createPurifiedWaterBucket(), false);
               }
            }
            this.setWaterLevel(world, pos, state, amount - 1);
            DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.BUCKET_FILL);
         }
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      } else if (item == Items.GLASS_BOTTLE) {
         if (amount > 0 && !world.isClientSide && !player.getAbilities().instabuild) {
            itemstack.shrink(1);
            ItemStack bottle = new ItemStack(SDItems.purifiedWaterBottle.get());
            if (itemstack.isEmpty()) {
               player.setItemInHand(hand, bottle);
            } else if (!player.getInventory().add(bottle)) {
               player.drop(bottle, false);
            }
            this.setWaterLevel(world, pos, state, amount - 1);
            DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.BOTTLE_FILL);
         }
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      } else if (item == SDItems.canteen.get()) {
         if (amount > 0 && !world.isClientSide && !player.getAbilities().instabuild) {
            if (((ItemCanteen)item).tryAddDose(itemstack, ThirstEnum.PURIFIED)) {
               this.setWaterLevel(world, pos, state, amount - 1);
               DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.BUCKET_FILL);
            }
         }
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      } else {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      }
   }

   public void setWaterLevel(Level world, BlockPos pos, BlockState state, int level) {
      world.setBlock(pos, state.setValue(LEVEL, Mth.clamp(level, 0, 3)), 2);
      world.updateNeighbourForOutputSignal(pos, this);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
      return state.getValue(LEVEL);
   }
}
