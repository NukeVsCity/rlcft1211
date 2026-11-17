package com.alcatrazescapee.notreepunching.common.blocks;

import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.container.ContainerFirePit;
import com.alcatrazescapee.notreepunching.common.tile.TileFirePit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * 1.12 BlockFirePit (Material.CIRCUITS, hardness 0, wood sound, no collision, light 15 when lit). Lit: sand puts it out, a stick
 * gives 2 torches (allowTorchesFromFirePits). Unlit: a fire starter (FireRegistry: flint and steel, NTP fire starter) lights it.
 * Otherwise right click (not sneaking) opens the GUI. Rain on a pit that sees the sky puts it out on a random tick. Drops 2 sticks,
 * pops off when the block below is no longer a normal cube.
 */
public class BlockFirePit extends BaseEntityBlock {
   public static final MapCodec<BlockFirePit> CODEC = simpleCodec(BlockFirePit::new);
   public static final BooleanProperty LIT = BooleanProperty.create("lit");
   private static final VoxelShape AABB = Shapes.box(0.375, 0.0, 0.375, 0.615, 0.0625, 0.625);
   private static final TagKey<Item> SAND = ItemTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "sands"));
   private static final TagKey<Item> STICKS = ItemTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));

   public BlockFirePit(Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LIT);
   }

   /** FireRegistry.isFireStarter */
   public static boolean isFireStarter(ItemStack stack) {
      return stack.is(Items.FLINT_AND_STEEL) || stack.is(NoTreePunching.FIRE_STARTER.get());
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFirePit(pos, state);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide ? null : createTickerHelper(type, NoTreePunching.FIRE_PIT_TILE.get(), TileFirePit::serverTick);
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }

   /** World.getTopSolidOrLiquidBlock(pos).getY() < pos.getY() + 2 with canBlockSeeSky and isRaining */
   private static boolean isRainedOn(Level worldIn, BlockPos pos) {
      return worldIn.canSeeSky(pos) && worldIn.isRaining()
         && worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() < pos.getY() + 2;
   }

   @Override
   protected boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   @Override
   protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      if (isRainedOn(worldIn, pos)) {
         worldIn.setBlockAndUpdate(pos, state.setValue(LIT, false));
      }
   }

   /** randomDisplayTick + ParticleManager.generateFirePitFlame / generateFirePitSmoke */
   @Override
   public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
      if (stateIn.getValue(LIT)) {
         if (rand.nextFloat() < 0.3) {
            smoke(worldIn, pos, rand);
         }
         worldIn.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5 + 0.15 * fastGaussian(rand), pos.getY() + 0.1,
            pos.getZ() + 0.5 + 0.15 * fastGaussian(rand), 0.0, 0.008, 0.0);
         worldIn.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         if (isRainedOn(worldIn, pos) && rand.nextFloat() < 0.5F) {
            worldIn.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            smoke(worldIn, pos, rand);
         }
      }
   }

   private static void smoke(Level worldIn, BlockPos pos, RandomSource rand) {
      worldIn.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + 0.1 * fastGaussian(rand), pos.getY() + 0.1, pos.getZ() + 0.5 + 0.1 * fastGaussian(rand),
         0.01 * fastGaussian(rand), 0.05, 0.01 * fastGaussian(rand));
   }

   private static double fastGaussian(RandomSource rand) {
      return (double)(rand.nextFloat() - rand.nextFloat());
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                             BlockHitResult hit) {
      if (!(world.getBlockEntity(pos) instanceof TileFirePit te)) {
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      }
      if (state.getValue(LIT)) {
         if (stack.is(SAND)) {
            if (!world.isClientSide) {
               te.extinguish();
               if (!player.getAbilities().instabuild) {
                  stack.shrink(1);
               }
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
         }
         if (stack.is(STICKS)) {
            if (!world.isClientSide && ModConfig.GENERAL.allowTorchesFromFirePits) {
               ItemStack torches = new ItemStack(Blocks.TORCH, 2);
               if (!player.getInventory().add(torches)) {
                  world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(world, player.blockPosition().getX() + 0.5,
                     player.blockPosition().getY() + 0.5, player.blockPosition().getZ() + 0.5, torches));
               }
               if (!player.getAbilities().instabuild) {
                  stack.shrink(1);
               }
               world.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
         }
      } else if (isFireStarter(stack)) {
         if (!world.isClientSide) {
            te.light(false);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
         }
         return ItemInteractionResult.sidedSuccess(world.isClientSide);
      }
      this.openGui(world, pos, player, te);
      return ItemInteractionResult.sidedSuccess(world.isClientSide);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      if (world.getBlockEntity(pos) instanceof TileFirePit te) {
         this.openGui(world, pos, player, te);
      }
      return InteractionResult.sidedSuccess(world.isClientSide);
   }

   private void openGui(Level world, BlockPos pos, Player player, TileFirePit te) {
      if (!player.isShiftKeyDown() && !world.isClientSide) {
         player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerFirePit(id, inv, te), Component.translatable(this.getDescriptionId())), pos);
      }
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   /** neighborChanged: !stateUnder.isNormalCube() -> drop and remove */
   @Override
   protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving) {
      super.neighborChanged(state, worldIn, pos, blockIn, fromPos, moving);
      if (!worldIn.isClientSide && !worldIn.getBlockState(pos.below()).isRedstoneConductor(worldIn, pos.below())) {
         dropResources(state, worldIn, pos);
         worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }
   }

   /** BlockTileCore.breakBlock -> TileInventory.onBreakBlock (the three slots drop) */
   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileInventory te) {
         te.onBreakBlock();
      }
      super.onRemove(state, level, pos, newState, moved);
   }
}
