package com.charles445.simpledifficulty.util;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.capability.ThirstCapability;
import com.charles445.simpledifficulty.config.ModConfig;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** 1.12 api ThirstUtil + util.internal.ThirstUtilInternal. */
public class ThirstUtil {
   @Nullable
   public static ThirstEnum.BlockPosResult traceWaterToDrink(Player player) {
      if (player.getMainHandItem().isEmpty()) {
         ThirstCapability capability = SDCapabilities.getThirstData(player);
         if (capability.isThirsty()) {
            ThirstEnum.BlockPosResult traceResult = traceWater(player);
            if (traceResult == null) {
               return null;
            }
            if (traceResult.thirstEnum == ThirstEnum.PURIFIED) {
               if (!ModConfig.server.thirstDrinkBlocks) {
                  return null;
               }
               if (!ModConfig.server.infinitePurifiedWater) {
                  player.level().removeBlock(traceResult.pos, false);
               }
            } else {
               if (traceResult.thirstEnum == ThirstEnum.RAIN && !ModConfig.server.thirstDrinkRain) {
                  return null;
               }
               if (traceResult.thirstEnum == ThirstEnum.NORMAL && !ModConfig.server.thirstDrinkBlocks) {
                  return null;
               }
            }
            return traceResult;
         }
      }
      return null;
   }

   /** Rain when looking up (pitch < -75) under open rain, else the source water block within half the 1.12 reach (5.0 / 2). */
   @Nullable
   public static ThirstEnum.BlockPosResult traceWater(Player player) {
      BlockPos ppos = WorldUtil.getPosition(player);
      if (player.getXRot() < -75.0F && LegacyClimate.isRainingAt(player.level(), ppos) && player.level().canSeeSky(ppos) && ModConfig.server.thirstDrinkRain) {
         return new ThirstEnum.BlockPosResult(ThirstEnum.RAIN, ppos);
      } else {
         double reach = (player.blockInteractionRange() + 0.5) * 0.5;
         Vec3 eyevec = player.getEyePosition(1.0F);
         Vec3 lookvec = player.getViewVector(1.0F);
         Vec3 targetvec = eyevec.add(lookvec.x * reach, lookvec.y * reach, lookvec.z * reach);
         BlockHitResult trace = player.level().clip(new ClipContext(eyevec, targetvec, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
         if (trace.getType() == HitResult.Type.BLOCK) {
            Block traceBlock = player.level().getBlockState(trace.getBlockPos()).getBlock();
            if (traceBlock == Blocks.WATER) {
               return new ThirstEnum.BlockPosResult(ThirstEnum.NORMAL, trace.getBlockPos());
            } else {
               return traceBlock == SDFluids.blockPurifiedWater.get() ? new ThirstEnum.BlockPosResult(ThirstEnum.PURIFIED, trace.getBlockPos()) : null;
            }
         } else {
            return null;
         }
      }
   }

   public static void takeDrink(Player player, int thirst, float saturation, float dirtyChance) {
      if (ModConfig.isThirstEnabled()) {
         ThirstCapability capability = SDCapabilities.getThirstData(player);
         if (capability.isThirsty()) {
            capability.addThirstLevel(thirst);
            capability.addThirstSaturation(saturation);
            if (dirtyChance != 0.0F && player.level().random.nextFloat() < dirtyChance) {
               player.addEffect(new MobEffectInstance(SDPotions.thirsty, 600));
               if (ModConfig.server.thirst.thirstParasites && player.level().random.nextDouble() < ModConfig.server.thirst.thirstParasitesChance) {
                  player.addEffect(new MobEffectInstance(SDPotions.parasites, ModConfig.server.thirst.thirstParasitesDuration));
               }
            }
         } else if (capability.getThirstSaturation() < saturation) {
            capability.setThirstSaturation(saturation);
         }
      }
   }

   public static void takeDrink(Player player, int thirst, float saturation) {
      takeDrink(player, thirst, saturation, 0.0F);
   }

   public static void takeDrink(Player player, ThirstEnum type) {
      takeDrink(player, type.getThirst(), type.getSaturation(), type.getThirstyChance());
   }

   public static ItemStack createPurifiedWaterBucket() {
      return new ItemStack(SDItems.purifiedWaterBucket.get());
   }
}
