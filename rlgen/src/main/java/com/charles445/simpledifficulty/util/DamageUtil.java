package com.charles445.simpledifficulty.util;

import com.charles445.simpledifficulty.config.ModConfig;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 1.12 DamageUtil, SoundUtil and WorldUtil. */
public class DamageUtil {
   public static boolean isModDangerous(Level world) {
      return world.getDifficulty() != Difficulty.PEACEFUL || ModConfig.server.peacefulDanger;
   }

   public static boolean healthAboveDifficulty(Level world, Player player) {
      Difficulty difficulty = world.getDifficulty();
      return difficulty == Difficulty.HARD
         || difficulty == Difficulty.NORMAL && player.getHealth() > 1.0F
         || difficulty == Difficulty.EASY && player.getHealth() > 10.0F
         || difficulty == Difficulty.PEACEFUL && player.getHealth() > 10.0F;
   }

   /** SoundUtil.commonPlayPlayerSound */
   public static void commonPlayPlayerSound(Player player, SoundEvent sound) {
      player.playSound(sound, 0.5F, 1.0F);
   }

   /** SoundUtil.serverPlayBlockSound */
   public static void serverPlayBlockSound(Level world, BlockPos pos, SoundEvent sound) {
      if (!world.isClientSide) {
         world.playSound(null, pos, sound, SoundSource.BLOCKS, sound == SoundEvents.GENERIC_DRINK ? 0.5F : 0.75F, 1.0F);
      }
   }

   public static void serverPlayBlockSound(Level world, BlockPos pos, SoundEvent sound, float volume, float pitch) {
      if (!world.isClientSide) {
         world.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
      }
   }

   /** WorldUtil.getSidedBlockPos (entity.getPosition = BlockPos of the feet) */
   public static BlockPos getSidedBlockPos(Level world, Entity entity) {
      return WorldUtil.getSidedBlockPos(world, entity);
   }
}
