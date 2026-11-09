package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

/**
 * 1.12 BlockMyrmexResin (myrmex_resin, myrmex_resin_sticky; VARIANT desert/jungle = item metadata). Slipperiness 0.75 for everything
 * except myrmex (which get the default 0.6); sticky resin slows non-myrmex entities to 40% horizontally and speeds myrmex up by 20%.
 * Mobs cannot spawn on it.
 */
public class BlockMyrmexResin extends Block {
   public static final EnumProperty<EnumType> VARIANT = EnumProperty.create("variant", EnumType.class);
   private final boolean sticky;

   public BlockMyrmexResin(Properties props, boolean sticky) {
      super(props.isValidSpawn((s, l, p, t) -> false));
      this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, EnumType.DESERT));
      this.sticky = sticky;
      IafNames.block(this, sticky ? "iceandfire.myrmex_resin_sticky" : "iceandfire.myrmex_resin");
   }

   @Override
   public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
      return entity != null && ModEntityHooks.isMyrmex(entity) ? this.getFriction() : 0.75F;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      int meta = IafMeta.getMetadata(ctx.getItemInHand());
      return this.defaultBlockState().setValue(VARIANT, EnumType.values()[Math.max(0, Math.min(1, meta))]);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(VARIANT);
   }

   @Override
   protected void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
      if (this.sticky) {
         Vec3 m = entityIn.getDeltaMovement();
         if (ModEntityHooks.isMyrmex(entityIn)) {
            entityIn.setDeltaMovement(m.x * 1.2, m.y * 1.2, m.z * 1.2);
         } else {
            entityIn.setDeltaMovement(m.x * 0.4, m.y, m.z * 0.4);
         }
      }
   }

   public enum EnumType implements StringRepresentable {
      DESERT("desert"),
      JUNGLE("jungle");

      private final String name;

      EnumType(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public String getSerializedName() {
         return this.name;
      }
   }
}
