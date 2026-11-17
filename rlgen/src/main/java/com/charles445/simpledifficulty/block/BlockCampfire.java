package com.charles445.simpledifficulty.block;

import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.LegacyClimate;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockCampfire: logs refuel (age -3, -4 when burnt out), sticks ignite 1 in 5, flint and steel always; rain puts it out. */
public class BlockCampfire extends Block {
   public static final MapCodec<BlockCampfire> CODEC = simpleCodec(BlockCampfire::new);
   public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
   public static final BooleanProperty BURNING = BooleanProperty.create("burning");
   private static final VoxelShape HITBOX = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.4, 1.0);

   public BlockCampfire(Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BURNING, false));
   }

   @Override
   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(AGE, BURNING);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      return InteractionResult.sidedSuccess(world.isClientSide);
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack heldItemStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                             BlockHitResult hit) {
      if (heldItemStack.isEmpty()) {
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      }
      if (heldItemStack.getItem() == SDItems.spit.get()) {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // 1.12 returned false so the spit gets placed on top
      }
      int age = state.getValue(AGE);
      boolean burning = state.getValue(BURNING);
      if (world.isClientSide) {
         if (heldItemStack.getItem() == Items.FLINT_AND_STEEL && !burning && age < 7 && !LegacyClimate.isRainingAt(world, pos.above())) {
            world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.4F + 0.8F);
         }
         return ItemInteractionResult.SUCCESS;
      }
      if (heldItemStack.is(ItemTags.LOGS)) {
         if (age > 0) {
            if (!player.getAbilities().instabuild) {
               heldItemStack.shrink(1);
            }
            int refuelAmount = 3 + (age == 7 ? 1 : 0);
            world.setBlock(pos, state.setValue(AGE, Math.max(0, age - refuelAmount)), 2);
         }
         return ItemInteractionResult.CONSUME;
      }
      if (!burning && age < 7 && !LegacyClimate.isRainingAt(world, pos.above())) {
         if (heldItemStack.getItem() == Items.STICK || heldItemStack.is(net.neoforged.neoforge.common.Tags.Items.RODS_WOODEN)) {
            if (!player.getAbilities().instabuild) {
               heldItemStack.shrink(1);
            }
            if (world.random.nextInt(ModConfig.server.miscellaneous.campfireStickIgniteChance) == 0) {
               world.setBlock(pos, state.setValue(BURNING, true), 2);
            }
            return ItemInteractionResult.CONSUME;
         }
         if (heldItemStack.getItem() == Items.FLINT_AND_STEEL) {
            world.setBlock(pos, state.setValue(BURNING, true), 2);
            heldItemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return ItemInteractionResult.CONSUME;
         }
      }
      return ItemInteractionResult.CONSUME;
   }

   /** 1.12 updateTick, run by random ticks */
   @Override
   protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
      int age = state.getValue(AGE);
      if (state.getValue(BURNING)) {
         if (LegacyClimate.isRainingAt(world, pos.above())) {
            world.setBlock(pos, state.setValue(BURNING, false), 2);
            this.effectExtinguish(world, pos);
         } else if (rand.nextInt(ModConfig.server.miscellaneous.campfireDecayChance) == 0) {
            if (++age >= 7) {
               world.setBlock(pos, state.setValue(AGE, 7).setValue(BURNING, false), 2);
               this.effectExtinguish(world, pos);
            } else {
               world.setBlock(pos, state.setValue(AGE, age), 2);
            }
         }
      }
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return HITBOX;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (!world.isClientSide && state.getValue(BURNING) && entity instanceof LivingEntity) {
         entity.igniteForSeconds(1.0F);
      }
   }

   @Override
   public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
      if (state.getValue(BURNING)) {
         int age = state.getValue(AGE);
         float strength = 1.0F - (float)age / 7.0F;
         if (rand.nextFloat() < strength) {
            int loop = rand.nextInt(6) + 1;
            for (int i = 0; i < loop; i++) {
               double yOffset = rand.nextDouble() * 0.35 + 0.35;
               double offAdj = (0.7 - yOffset) * 2.28571428;
               double xOffset = (rand.nextDouble() - 0.5) * offAdj + 0.5;
               double zOffset = (rand.nextDouble() - 0.5) * offAdj + 0.5;
               world.addParticle(ParticleTypes.FLAME, xOffset + pos.getX(), yOffset + pos.getY(), zOffset + pos.getZ(), 0.0,
                  rand.nextDouble() * 0.015 + 0.005, 0.0);
            }
         }
         if (rand.nextInt(30) == 0) {
            world.playLocalSound(0.5 + pos.getX(), 0.5 + pos.getY(), 0.5 + pos.getZ(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.5F, 1.0F, false);
         }
      }
   }

   private void effectExtinguish(ServerLevel world, BlockPos pos) {
      DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.FIRE_EXTINGUISH);
      // 1.12 World.spawnParticle on the server is a no-op (only client worlds have listeners spawning particles)
   }
}
