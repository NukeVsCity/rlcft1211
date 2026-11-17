package com.charles445.simpledifficulty.tileentity;

import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry.ITemperatureTileEntity;
import com.charles445.simpledifficulty.block.BlockTemperature;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 1.12 TileEntityTemperature: full power within HeaterFullPowerRange, fading to 0 at HeaterMaxRange (squared distances). With
 * StrictHeaters the diagonal walk from the player to the heater must never reach a block open to the sky.
 */
public class TileEntityTemperature extends BlockEntity implements ITemperatureTileEntity {
   public TileEntityTemperature(BlockPos pos, BlockState state) {
      super(SDBlocks.TEMPERATURE_TILE.get(), pos, state);
   }

   @Override
   public float getInfluence(BlockPos targetPos, double distance) {
      BlockState state = this.level.getBlockState(this.worldPosition);
      Block block = state.getBlock();
      if (block instanceof BlockTemperature bt && state.getValue(BlockTemperature.ENABLED)) {
         float activeTemp = bt.getActiveTemperatureMult() * (float)ModConfig.server.temperature.heaterTemperature;
         double fullPowerSq = sq(ModConfig.server.temperature.heaterFullPowerRange);
         if (distance < fullPowerSq) {
            return this.handleStrict(targetPos, activeTemp);
         } else {
            double distanceDiv = sq(ModConfig.server.temperature.heaterMaxRange) - fullPowerSq;
            return distanceDiv <= 0.0 ? 0.0F : this.handleStrict(targetPos, activeTemp * Math.max(0.0F, 1.0F - (float)((distance - fullPowerSq) / distanceDiv)));
         }
      }
      return 0.0F;
   }

   private float handleStrict(BlockPos targetPos, float distanceTemp) {
      if (!ModConfig.server.strictHeaters) {
         return distanceTemp;
      }
      BlockPos thisPos = this.getBlockPos();
      int curX = targetPos.getX();
      int curY = targetPos.getY();
      int curZ = targetPos.getZ();
      int destX = thisPos.getX();
      int destY = thisPos.getY();
      int destZ = thisPos.getZ();
      int xinc = curX < destX ? 1 : -1;
      int yinc = curY < destY ? 1 : -1;
      int zinc = curZ < destZ ? 1 : -1;
      if (!this.isUnprotected(new BlockPos(curX, curY, curZ)) && !this.isUnprotected(new BlockPos(destX, destY, destZ))) {
         while (curX != destX || curZ != destZ || curY != destY) {
            if (curX != destX) {
               curX += xinc;
            }
            if (curY != destY) {
               curY += yinc;
            }
            if (curZ != destZ) {
               curZ += zinc;
            }
            if (this.isUnprotected(new BlockPos(curX, curY, curZ))) {
               return 0.0F;
            }
         }
         return distanceTemp;
      } else {
         return 0.0F;
      }
   }

   private boolean isUnprotected(BlockPos pos) {
      if (!WorldUtil.isChunkLoaded(this.level, pos)) {
         return true;
      }
      return !this.level.canSeeSky(pos) ? false : this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY();
   }

   private static double sq(double d) {
      return d * d;
   }
}
