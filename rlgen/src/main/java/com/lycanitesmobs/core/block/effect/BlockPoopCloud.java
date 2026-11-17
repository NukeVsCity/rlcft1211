package com.lycanitesmobs.core.block.effect;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.block.BlockBase;
import com.lycanitesmobs.core.config.ConfigBase;
import java.util.Random;


public class BlockPoopCloud extends BlockBase {
   public BlockPoopCloud() {
      super(Material.PLANTS);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
      this.group = LycanitesMobs.modInfo;
      this.blockName = "poopcloud";
      this.setup();
      this.tickRate = ConfigBase.getConfig(this.group, "general").getBool("Features", "Enable Poop Clouds", true) ? 200 : 1;
      this.removeOnTick = true;
      this.loopTicks = false;
      this.canBeCrushed = true;
      this.noEntityCollision = true;
      this.noBreakCollision = true;
      this.isOpaque = false;
      this.setBlockUnbreakable();
      this.setLightOpacity(1);
   }

      public BlockState getStateFromMeta(int meta) {
      return this.defaultBlockState().setValue(AGE, meta);
   }

      public int getMetaFromState(BlockState state) {
      return (Integer)state.getValue(AGE);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

      public Item getItemDropped(BlockState blockState, RandomSource random, int fortune) {
      return null;
   }

      public int damageDropped(BlockState blockState) {
      return 0;
   }

      public int quantityDropped(RandomSource random) {
      return 1;
   }

   @Override
   protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      super.entityInside(state, world, pos, entity);
      if (entity instanceof LivingEntity) {
         ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
         ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
      }
   }

   public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
      super.animateTick(state, world, pos, random);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      if (random.nextInt(24) == 0) {
         world.playLocalSound(
            (double)((float)x + 0.5F),
            (double)((float)y + 0.5F),
            (double)((float)z + 0.5F),
            AssetManager.getSound("poopcloud"),
            SoundSource.BLOCKS,
            0.5F + random.nextFloat(),
            random.nextFloat() * 0.7F + 0.3F,
            false
         );
      }

      for (int particleCount = 0; particleCount < 12; particleCount++) {
         float particleX = (float)x + random.nextFloat();
         float particleY = (float)y + random.nextFloat() * 0.5F;
         float particleZ = (float)z + random.nextFloat();
         world.addParticle(ParticleTypes.POOF, (double)particleX, (double)particleY, (double)particleZ, 0.0, 0.0, 0.0);
      }
   }

}
