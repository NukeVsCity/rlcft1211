package com.lycanitesmobs.core.dispenser;

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
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import java.util.Random;


public class DispenserBehaviorBase extends BehaviorProjectileDispense {
   ProjectileInfo projectileInfo;

   public DispenserBehaviorBase(ProjectileInfo projectileInfo) {
      this.projectileInfo = projectileInfo;
   }

   public DispenserBehaviorBase() {
   }

   public ItemStack dispenseStack(IBlockSource blockSource, ItemStack stack) {
      Level world = blockSource.getWorld();
      IPosition position = BlockDispenser.getDispensePosition(blockSource);
      Direction facing = (Direction)blockSource.getBlockState().getValue(BlockDispenser.FACING);
      Projectile iprojectile = this.getProjectileEntity(world, position, stack);
      if (iprojectile == null) {
         return stack;
      } else {
         iprojectile.shoot(
            (double)facing.getStepX(), (double)facing.getStepY(), (double)facing.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy()
         );
         world.addFreshEntity((Entity)iprojectile);
         stack.splitStack(1);
         return stack;
      }
   }

   protected Projectile getProjectileEntity(Level world, IPosition pos, ItemStack stack) {
      return this.projectileInfo == null ? null : this.projectileInfo.createProjectile(world, pos.getX(), pos.getY(), pos.getZ());
   }

   protected float getProjectileInaccuracy() {
      return 0.0F;
   }

   protected float getProjectileVelocity() {
      return this.projectileInfo != null ? (float)this.projectileInfo.velocity : 1.1F;
   }

   protected void playDispenseSound(IBlockSource blockSource) {
      SoundEvent soundEvent = this.getDispenseSound();
      if (soundEvent != null && blockSource != null) {
         blockSource.getWorld()
            .playSound(null, blockSource.getBlockPos(), soundEvent, SoundSource.AMBIENT, 1.0F, 1.0F / (RandomSource.create().nextFloat() * 0.4F + 0.8F));
      }
   }

   protected SoundEvent getDispenseSound() {
      return this.projectileInfo == null ? null : this.projectileInfo.getLaunchSound();
   }
}
