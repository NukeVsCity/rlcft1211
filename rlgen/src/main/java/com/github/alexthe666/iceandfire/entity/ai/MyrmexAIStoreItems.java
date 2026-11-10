package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIStoreItems: put the carried item into a myrmex cocoon of a food room. */
public class MyrmexAIStoreItems extends Goal {
   private final EntityMyrmexBase myrmex;
   private final double movementSpeed;
   private Path path;
   private BlockPos nextRoom = BlockPos.ZERO;
   private BlockPos nextCocoon = null;

   public MyrmexAIStoreItems(EntityMyrmexBase entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /** BlockMyrmexCocoon with a TileEntityMyrmexCocoon (any "myrmex...cocoon" block whose block entity is a container) */
   @Nullable
   public static Container cocoon(Level level, BlockPos pos) {
      String id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
      if (id.contains("myrmex") && id.contains("cocoon")) {
         BlockEntity be = level.getBlockEntity(pos);
         return be instanceof Container c ? c : null;
      }
      return null;
   }

   /** TileEntityMyrmexCocoon.isFull */
   public static boolean isFull(Container cocoon, ItemStack heldStack) {
      for (int i = 0; i < cocoon.getContainerSize(); i++) {
         ItemStack itemstack = cocoon.getItem(i);
         if (itemstack.isEmpty()
            || heldStack != null && !heldStack.isEmpty() && ItemStack.isSameItem(itemstack, heldStack) && itemstack.getCount() + heldStack.getCount() < itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove()
         && (!(this.myrmex instanceof EntityMyrmexWorker) || !((EntityMyrmexWorker)this.myrmex).holdingBaby())
         && (this.myrmex.shouldEnterHive() || this.myrmex.getNavigation().isDone())
         && !this.myrmex.canSeeSky()
         && !this.myrmex.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
         MyrmexHive village = this.myrmex.getHive();
         if (village == null) {
            return false;
         } else {
            this.nextRoom = MyrmexHive.getGroundedPos(this.myrmex.level(), village.getRandomRoom(WorldGenMyrmexHive.RoomType.FOOD, this.myrmex.getRandom(), this.myrmex.blockPosition()));
            this.nextCocoon = this.getNearbyCocoon(this.nextRoom);
            if (this.nextCocoon != null) {
               this.path = this.myrmex.getNavigation().createPath(this.nextCocoon, 0);
               return this.path != null;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.myrmex.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
         && !this.myrmex.getNavigation().isDone()
         && this.nextCocoon != null
         && this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextCocoon)) > 3.0
         && this.myrmex.shouldEnterHive()
         && this.isUseableCocoon(this.nextCocoon);
   }

   @Override
   public void start() {
      this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.nextCocoon != null
         && this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextCocoon)) < 4.0
         && !this.myrmex.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
         && this.isUseableCocoon(this.nextCocoon)) {
         Container cocoon = cocoon(this.myrmex.level(), this.nextCocoon);
         ItemStack itemstack = this.myrmex.getItemInHand(InteractionHand.MAIN_HAND);
         if (!itemstack.isEmpty()) {
            for (int i = 0; i < cocoon.getContainerSize(); i++) {
               if (!itemstack.isEmpty()) {
                  ItemStack itemstack1 = cocoon.getItem(i);
                  if (itemstack1.isEmpty()) {
                     cocoon.setItem(i, itemstack);
                     cocoon.setChanged();
                     this.myrmex.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                     this.myrmex.isEnteringHive = false;
                     return;
                  }

                  if (ItemStack.isSameItem(itemstack1, itemstack)) {
                     int j = Math.min(cocoon.getMaxStackSize(), itemstack1.getMaxStackSize());
                     int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());
                     if (k > 0) {
                        itemstack1.grow(k);
                        itemstack.shrink(k);
                        if (itemstack.isEmpty()) {
                           cocoon.setChanged();
                        }

                        this.myrmex.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        this.myrmex.isEnteringHive = false;
                        return;
                     }
                  }
               }
            }
         } else {
            this.stop();
         }
      }
   }

   @Override
   public void stop() {
      this.nextRoom = BlockPos.ZERO;
      this.nextCocoon = null;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
   }

   @Nullable
   public BlockPos getNearbyCocoon(BlockPos roomCenter) {
      int RADIUS_XZ = 15;
      int RADIUS_Y = 7;
      List<BlockPos> closeCocoons = new ArrayList<>();

      for (BlockPos blockpos : BlockPos.betweenClosed(roomCenter.offset(-RADIUS_XZ, -RADIUS_Y, -RADIUS_XZ), roomCenter.offset(RADIUS_XZ, RADIUS_Y, RADIUS_XZ))) {
         Container c = cocoon(this.myrmex.level(), blockpos);
         if (c != null && !isFull(c, this.myrmex.getItemInHand(InteractionHand.MAIN_HAND))) {
            closeCocoons.add(blockpos.immutable());
         }
      }

      return closeCocoons.isEmpty() ? null : closeCocoons.get(this.myrmex.getRandom().nextInt(Math.max(closeCocoons.size() - 1, 1)));
   }

   public boolean isUseableCocoon(BlockPos blockpos) {
      Container c = cocoon(this.myrmex.level(), blockpos);
      return c != null && !isFull(c, this.myrmex.getItemInHand(InteractionHand.MAIN_HAND));
   }
}
