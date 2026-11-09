package com.github.alexthe666.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.IPhasesThroughBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ice and Fire 1.12 EventLiving.onGatherCollisionBoxes: an IPhasesThroughBlock entity (death worm: sand, amphithere: leaves)
 * gets no collision box from the blocks it can phase through.
 */
@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {
   @Shadow @Final private CollisionContext context;
   @Shadow @Final private CollisionGetter collisionGetter;

   @Redirect(
      method = "computeNext",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
      )
   )
   private VoxelShape iceandfire$phase(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
      if (ctx instanceof EntityCollisionContext ec && ec.getEntity() instanceof IPhasesThroughBlock phaser && this.collisionGetter instanceof Level level
         && phaser.canPhaseThroughBlock(level, pos)) {
         return Shapes.empty();
      }
      return state.getCollisionShape(getter, pos, ctx);
   }
}
