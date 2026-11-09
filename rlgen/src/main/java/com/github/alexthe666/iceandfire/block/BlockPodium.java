package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPodium;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockPodium: 6 wood variants (item metadata 0..5 = VARIANT), shows its one item, drops it when broken. */
public class BlockPodium extends BaseEntityBlock {
   public static final MapCodec<BlockPodium> CODEC = simpleCodec(BlockPodium::new);
   public static final EnumProperty<EnumType> VARIANT = EnumProperty.create("variant", EnumType.class);
   private static final VoxelShape AABB = Block.box(2.0, 0.0, 2.0, 14.0, 23.0, 14.0);

   public BlockPodium(Properties props) {
      super(props.noOcclusion());
      this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, EnumType.OAK));
      IafNames.block(this, "iceandfire.podium");
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   @Override
   protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock()) && worldIn.getBlockEntity(pos) instanceof TileEntityPodium podium && !podium.getStackInSlot(0).isEmpty()) {
         worldIn.addFreshEntity(new ItemEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, podium.getStackInSlot(0)));
      }
      super.onRemove(state, worldIn, pos, newState, moving);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit) {
      if (playerIn.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      if (!worldIn.isClientSide && worldIn.getBlockEntity(pos) instanceof TileEntityPodium podium) {
         playerIn.openMenu(podium);
      }
      return InteractionResult.sidedSuccess(worldIn.isClientSide);
   }

   /** The placed variant is the item metadata (1.12 getStateFromMeta). */
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(VARIANT, EnumType.byMetadata(IafMeta.getMetadata(ctx.getItemInHand())));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(VARIANT);
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos.below(), Direction.UP);
   }

   @Override
   protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving) {
      worldIn.scheduleTick(pos, this, 10);
   }

   @Override
   protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(worldIn, pos)) {
         worldIn.destroyBlock(pos, true);
      }
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityPodium(pos, state);
   }

   public enum EnumType implements StringRepresentable {
      OAK(0, "oak"),
      SPRUCE(1, "spruce"),
      BIRCH(2, "birch"),
      JUNGLE(3, "jungle"),
      ACACIA(4, "acacia"),
      DARK_OAK(5, "dark_oak", "big_oak");

      private static final EnumType[] META_LOOKUP = new EnumType[values().length];
      private final int meta;
      private final String name;
      private final String unlocalizedName;

      EnumType(int meta, String name) {
         this(meta, name, name);
      }

      EnumType(int meta, String name, String unlocalizedName) {
         this.meta = meta;
         this.name = name;
         this.unlocalizedName = unlocalizedName;
      }

      public static EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }
         return META_LOOKUP[meta];
      }

      public int getMetadata() {
         return this.meta;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public String getSerializedName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         for (EnumType t : values()) {
            META_LOOKUP[t.getMetadata()] = t;
         }
      }
   }
}
