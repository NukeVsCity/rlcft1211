package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockJar (jar_empty, jar_pixie with item metadata = pixie colour 0..4). A pixie jar gives pixie dust on right click once it has
 * produced (every 24000 ticks), releases the pixie when broken or when its support goes, drops an empty jar (loot table).
 */
public class BlockJar extends BaseEntityBlock {
   public static final MapCodec<BlockJar> CODEC = simpleCodec(p -> new BlockJar(p, true));
   private static final VoxelShape AABB = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
   private final boolean empty;

   public BlockJar(Properties props, boolean empty) {
      super(props.noOcclusion());
      this.empty = empty;
      IafNames.block(this, "iceandfire.jar" + (empty ? "_empty" : "_pixie"));
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   /** 1.12 canPlaceBlockAt: a solid top or something a torch can stand on. */
   @Override
   protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      BlockState below = worldIn.getBlockState(pos.below());
      return below.isFaceSturdy(worldIn, pos.below(), Direction.UP) || Block.canSupportCenter(worldIn, pos.below(), Direction.UP);
   }

   @Override
   protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         this.dropPixie(worldIn, pos);
      }
      super.onRemove(state, worldIn, pos, newState, moving);
   }

   @Override
   protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving) {
      worldIn.scheduleTick(pos, this, 10);
   }

   @Override
   protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(world, pos)) {
         this.dropPixie(world, pos);
         world.destroyBlock(pos, true);
      }
   }

   public void dropPixie(Level world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof TileEntityJar jar && jar.hasPixie) {
         jar.releasePixie();
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerIn, BlockHitResult hit) {
      if (!this.empty && world.getBlockEntity(pos) instanceof TileEntityJar jar && jar.hasPixie && jar.hasProduced) {
         jar.hasProduced = false;
         ItemEntity item = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, new ItemStack(ModItems.pixie_dust));
         if (!world.isClientSide) {
            world.addFreshEntity(item);
            jar.sync();
         }
         world.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, ModSounds.PIXIE_HURT, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
      }
      return InteractionResult.PASS;
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      if (world.getBlockEntity(pos) instanceof TileEntityJar jar) {
         if (!this.empty) {
            jar.hasPixie = true;
            jar.pixieType = IafMeta.getMetadata(stack);
         } else {
            jar.hasPixie = false;
         }
         jar.sync();
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityJar(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, ModBlocks.JAR_TILE.get(), TileEntityJar::tick);
   }
}
