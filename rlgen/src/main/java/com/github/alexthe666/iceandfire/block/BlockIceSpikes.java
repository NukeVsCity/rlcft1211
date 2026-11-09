package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockIceSpikes (dragon_ice_spikes): walking on it (except ice dragons) deals 1 cactus damage and knocks back. */
public class BlockIceSpikes extends Block {
   protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);

   public BlockIceSpikes(Properties props) {
      super(props.noOcclusion());
      IafNames.block(this, "iceandfire.dragon_ice_spikes");
   }

   @Override
   public void stepOn(Level level, BlockPos pos, BlockState state, Entity entityIn) {
      if (!ModEntityHooks.is(entityIn, "icedragon")) {
         entityIn.hurt(level.damageSources().cactus(), 1.0F);
         Vec3 m = entityIn.getDeltaMovement();
         if (entityIn instanceof LivingEntity living && m.x != 0.0 && m.z != 0.0) {
            living.knockback(0.5, m.x, m.z);
         }
      }
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }
}
