package com.lycanitesmobs.core.entity.creature;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import java.util.HashMap;


public class EntityXaphan extends TameableCreatureEntity implements Enemy {
   private int nextSplash = 20;

   public EntityXaphan(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsInWater = true;
      this.hasAttackSound = false;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setRange(14.0F).setMinChaseDistance(10.0F));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.updateTick % (long)this.nextSplash == 0L) {
         this.fireProjectile(
            "acidsplash",
            null,
            0.0F,
            0.0F,
            new Vec3(0.5 - this.getRandom().nextDouble(), 0.0, 0.5 - this.getRandom().nextDouble()),
            0.0F,
            (float)this.nextSplash / 20.0F,
            1.0F
         );
         this.nextSplash = 20 + this.getRandom().nextInt(20);
      }
   }

   @Override
   public boolean isDamageEntityApplicable(Entity entity) {
      return entity instanceof EntityXaphan && this.getPlayerOwner() == ((EntityXaphan)entity).getPlayerOwner()
         ? false
         : super.isDamageEntityApplicable(entity);
   }

   @Override
   public void attackRanged(Entity target, float range) {
      for (int row = -1; row <= 1; row++) {
         int projectileCount = 10;

         for (int i = 0; i < projectileCount; i++) {
            this.fireProjectile("acidsplash", target, range, (float)(90 / projectileCount * i), new Vec3(0.0, (double)(3 * row), 0.0), 0.6F, 2.0F, 1.0F);
         }
      }

      super.attackRanged(target, range);
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public boolean isStrongSwimmer() {
      return true;
   }

   @Override
   public int getNoBagSize() {
      return 0;
   }

   @Override
   public int getBagSize() {
      return this.creatureInfo.bagSize;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("acid") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      commands.putAll(super.getInteractCommands(player, hand, itemStack));
      if (itemStack != null && itemStack.getItem() == Items.BUCKET && this.isTamed()) {
         commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Water");
      }

      return commands;
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
