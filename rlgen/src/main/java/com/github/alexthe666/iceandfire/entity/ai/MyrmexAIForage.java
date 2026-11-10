package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIForage: a worker on the surface eats a plant/leaf block and carries its first drop home. */
public class MyrmexAIForage extends Goal {
   private static final int RADIUS = 8;
   private final EntityMyrmexWorker myrmex;
   private final BlockSorter targetSorter;
   private BlockPos targetBlock = BlockPos.ZERO;
   private int wanderRadius = 16;

   public MyrmexAIForage(EntityMyrmexWorker myrmex) {
      this.myrmex = myrmex;
      this.targetSorter = new BlockSorter();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove()
         && !this.myrmex.holdingBaby()
         && this.myrmex.getNavigation().isDone()
         && this.myrmex.canSeeSky()
         && !this.myrmex.shouldEnterHive()
         && this.myrmex.keepSearching) {
         List<BlockPos> allBlocks = new ArrayList<>();

         for (BlockPos pos : BlockPos.betweenClosed(this.myrmex.blockPosition().offset(-8, -8, -8), this.myrmex.blockPosition().offset(8, 8, 8))) {
            if (EntityMyrmexBase.isEdibleBlock(this.myrmex.level().getBlockState(pos))) {
               allBlocks.add(pos.immutable());
               this.myrmex.keepSearching = false;
            }
         }

         if (allBlocks.isEmpty()) {
            this.myrmex.keepSearching = true;
            this.wanderRadius += 8;
            Vec3 vec = DefaultRandomPos.getPos(this.myrmex, this.wanderRadius, 7);
            if (vec != null) {
               this.targetBlock = BlockPos.containing(vec);
            }

            return true;
         } else {
            allBlocks.sort(this.targetSorter);
            this.targetBlock = allBlocks.get(0);
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (!this.myrmex.keepSearching && this.targetBlock == null) {
         return false;
      } else if (this.myrmex.shouldEnterHive()) {
         this.myrmex.keepSearching = false;
         return false;
      } else {
         return this.myrmex.getNavigation().isDone();
      }
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.myrmex.keepSearching) {
         this.myrmex.getNavigation().moveTo((double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY(), (double)this.targetBlock.getZ() + 0.5, 1.0);
         if (this.myrmex.distanceToSqr(Vec3.atCenterOf(this.targetBlock)) < 2.0) {
            this.stop();
         }
      } else if (this.targetBlock != null) {
         this.myrmex.getNavigation().moveTo((double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY(), (double)this.targetBlock.getZ() + 0.5, 1.0);
         BlockState block = this.myrmex.level().getBlockState(this.targetBlock);
         if (EntityMyrmexBase.isEdibleBlock(block)) {
            double distance = this.getDistance(this.targetBlock);
            if (distance <= 5.0 && this.myrmex.level() instanceof ServerLevel server) {
               List<ItemStack> drops = new ArrayList<>(Block.getDrops(block, server, this.targetBlock, server.getBlockEntity(this.targetBlock)));
               if (!drops.isEmpty()) {
                  this.myrmex.level().destroyBlock(this.targetBlock, false);
                  ItemStack heldStack = drops.get(0).copy();
                  heldStack.setCount(1);
                  drops.get(0).shrink(1);
                  this.myrmex.setItemInHand(InteractionHand.MAIN_HAND, heldStack);

                  for (ItemStack stack : drops) {
                     ItemEntity itemEntity = new ItemEntity(
                        this.myrmex.level(),
                        (double)this.targetBlock.getX() + this.myrmex.getRandom().nextDouble(),
                        (double)this.targetBlock.getY() + this.myrmex.getRandom().nextDouble(),
                        (double)this.targetBlock.getZ() + this.myrmex.getRandom().nextDouble(),
                        stack
                     );
                     itemEntity.setDefaultPickUpDelay();
                     this.myrmex.level().addFreshEntity(itemEntity);
                  }

                  this.targetBlock = null;
                  this.stop();
                  this.myrmex.keepSearching = false;
                  this.wanderRadius = 8;
                  return;
               }
            }
         }
      }
   }

   @Override
   public void stop() {
      this.targetBlock = BlockPos.ZERO;
      this.myrmex.keepSearching = true;
   }

   private double getDistance(BlockPos pos) {
      double deltaX = this.myrmex.getX() - ((double)pos.getX() + 0.5);
      double deltaY = this.myrmex.getY() + (double)this.myrmex.getEyeHeight() - ((double)pos.getY() + 0.5);
      double deltaZ = this.myrmex.getZ() - ((double)pos.getZ() + 0.5);
      return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
   }

   public class BlockSorter implements Comparator<BlockPos> {
      @Override
      public int compare(BlockPos pos1, BlockPos pos2) {
         return Double.compare(MyrmexAIForage.this.getDistance(pos1), MyrmexAIForage.this.getDistance(pos2));
      }
   }
}
