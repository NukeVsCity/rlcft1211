package com.lycanitesmobs.core.info;

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
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeGoal;


public class ExtraMobBehaviour {
   public BaseCreatureEntity host;
   public double multiplierHealth = 1.0;
   public double multiplierDefense = 1.0;
   public double multiplierArmor = 1.0;
   public double multiplierSpeed = 1.0;
   public double multiplierDamage = 1.0;
   public double multiplierHaste = 1.0;
   public double multiplierEffect = 1.0;
   public double multiplierPierce = 1.0;
   public int boostHealth = 0;
   public int boostDefense = 0;
   public int boostArmor = 0;
   public int boostSpeed = 0;
   public int boostDamage = 0;
   public int boostHaste = 0;
   public int boostEffect = 0;
   public int boostPierce = 0;
   public boolean aggressiveOverride = false;
   public boolean flightOverride = false;
   public boolean swimmingOverride = false;
   public boolean waterBreathingOverride = false;
   public boolean fireImmunityOverride = false;
   public boolean stealthOverride = false;
   public boolean itemPickupOverride = false;
   public int inventorySizeOverride = 0;
   public double itemDropMultiplierOverride = 1.0;
   public boolean aiAttackPlayers = false;
   public boolean aiDefendAnimals = false;

   public ExtraMobBehaviour(BaseCreatureEntity host) {
      this.host = host;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("MultiplierHealth")) {
         this.multiplierHealth = nbtTagCompound.getDouble("MultiplierHealth");
      }

      if (nbtTagCompound.contains("MultiplierDefense")) {
         this.multiplierDefense = nbtTagCompound.getDouble("MultiplierDefense");
      }

      if (nbtTagCompound.contains("MultiplierArmor")) {
         this.multiplierArmor = nbtTagCompound.getDouble("MultiplierArmor");
      }

      if (nbtTagCompound.contains("MultiplierSpeed")) {
         this.multiplierSpeed = nbtTagCompound.getDouble("MultiplierSpeed");
      }

      if (nbtTagCompound.contains("MultiplierDamage")) {
         this.multiplierDamage = nbtTagCompound.getDouble("MultiplierDamage");
      }

      if (nbtTagCompound.contains("MultiplierHaste")) {
         this.multiplierHaste = nbtTagCompound.getDouble("MultiplierHaste");
      }

      if (nbtTagCompound.contains("MultiplierEffect")) {
         this.multiplierEffect = nbtTagCompound.getDouble("MultiplierEffect");
      }

      if (nbtTagCompound.contains("MultiplierPierce")) {
         this.multiplierEffect = nbtTagCompound.getDouble("MultiplierPierce");
      }

      if (nbtTagCompound.contains("BoostHealth")) {
         this.boostHealth = nbtTagCompound.getInt("BoostHealth");
      }

      if (nbtTagCompound.contains("BoostDefense")) {
         this.boostDefense = nbtTagCompound.getInt("BoostDefense");
      }

      if (nbtTagCompound.contains("BoostArmor")) {
         this.boostArmor = nbtTagCompound.getInt("BoostArmor");
      }

      if (nbtTagCompound.contains("BoostSpeed")) {
         this.boostSpeed = nbtTagCompound.getInt("BoostSpeed");
      }

      if (nbtTagCompound.contains("BoostDamage")) {
         this.boostDamage = nbtTagCompound.getInt("BoostDamage");
      }

      if (nbtTagCompound.contains("BoostHaste")) {
         this.boostHaste = nbtTagCompound.getInt("BoostHaste");
      }

      if (nbtTagCompound.contains("BoostEffect")) {
         this.boostEffect = nbtTagCompound.getInt("BoostEffect");
      }

      if (nbtTagCompound.contains("BoostPierce")) {
         this.boostEffect = nbtTagCompound.getInt("BoostPierce");
      }

      if (nbtTagCompound.contains("AggressiveOverride")) {
         this.aggressiveOverride = nbtTagCompound.getBoolean("AggressiveOverride");
      }

      if (nbtTagCompound.contains("FlightOverride")) {
         this.flightOverride = nbtTagCompound.getBoolean("FlightOverride");
      }

      if (nbtTagCompound.contains("SwimmingOverride")) {
         this.swimmingOverride = nbtTagCompound.getBoolean("SwimmingOverride");
      }

      if (nbtTagCompound.contains("WaterBreathingOverride")) {
         this.waterBreathingOverride = nbtTagCompound.getBoolean("WaterBreathingOverride");
      }

      if (nbtTagCompound.contains("FireImmunityOverride")) {
         this.fireImmunityOverride = nbtTagCompound.getBoolean("FireImmunityOverride");
      }

      if (nbtTagCompound.contains("StealthOverride")) {
         this.stealthOverride = nbtTagCompound.getBoolean("StealthOverride");
      }

      if (nbtTagCompound.contains("ItemPickupOverride")) {
         this.itemPickupOverride = nbtTagCompound.getBoolean("ItemPickupOverride");
      }

      if (nbtTagCompound.contains("InventorySizeOverride")) {
         this.inventorySizeOverride = nbtTagCompound.getInt("InventorySizeOverride");
      }

      if (nbtTagCompound.contains("ItemDropMultiplierOverride")) {
         this.itemDropMultiplierOverride = nbtTagCompound.getDouble("ItemDropMultiplierOverride");
      }

      if (nbtTagCompound.contains("AIAttackPlayers")) {
         this.aiAttackPlayers = nbtTagCompound.getBoolean("AIAttackPlayers");
         this.host.targetTasks.removeTask(this.host.aiTargetPlayer);
         if (this.aiAttackPlayers) {
            if (this.host.aiTargetPlayer == null) {
               this.host.aiTargetPlayer = new FindAttackTargetGoal(this.host).addTargets(Player.class);
            }

            this.host.targetTasks.addTask(9, this.host.aiTargetPlayer);
         }
      }

      if (nbtTagCompound.contains("AIDefendAnimals")) {
         this.aiDefendAnimals = nbtTagCompound.getBoolean("AIDefendAnimals");
         this.host.targetTasks.removeTask(this.host.aiDefendAnimals);
         if (this.aiDefendAnimals) {
            if (this.host.aiDefendAnimals == null) {
               this.host.aiDefendAnimals = new RevengeGoal(this.host).setHelpClasses(Animal.class);
            }

            this.host.targetTasks.addTask(10, this.host.aiDefendAnimals);
         }
      }
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      nbtTagCompound.putDouble("MultiplierHealth", this.multiplierHealth);
      nbtTagCompound.putDouble("MultiplierDefense", this.multiplierDefense);
      nbtTagCompound.putDouble("MultiplierArmor", this.multiplierArmor);
      nbtTagCompound.putDouble("MultiplierSpeed", this.multiplierSpeed);
      nbtTagCompound.putDouble("MultiplierDamage", this.multiplierDamage);
      nbtTagCompound.putDouble("MultiplierHaste", this.multiplierHaste);
      nbtTagCompound.putDouble("MultiplierEffect", this.multiplierEffect);
      nbtTagCompound.putDouble("MultiplierPierce", this.multiplierPierce);
      nbtTagCompound.putInt("BoostHealth", this.boostHealth);
      nbtTagCompound.putInt("BoostDefense", this.boostDefense);
      nbtTagCompound.putInt("BoostArmor", this.boostArmor);
      nbtTagCompound.putInt("BoostSpeed", this.boostSpeed);
      nbtTagCompound.putInt("BoostDamage", this.boostDamage);
      nbtTagCompound.putInt("BoostHaste", this.boostHaste);
      nbtTagCompound.putInt("BoostEffect", this.boostEffect);
      nbtTagCompound.putInt("BoostPierce", this.boostPierce);
      nbtTagCompound.putBoolean("AggressiveOverride", this.aggressiveOverride);
      nbtTagCompound.putBoolean("FlightOverride", this.flightOverride);
      nbtTagCompound.putBoolean("SwimmingOverride", this.swimmingOverride);
      nbtTagCompound.putBoolean("WaterBreathingOverride", this.waterBreathingOverride);
      nbtTagCompound.putBoolean("FireImmunityOverride", this.fireImmunityOverride);
      nbtTagCompound.putBoolean("StealthOverride", this.stealthOverride);
      nbtTagCompound.putBoolean("ItemPickupOverride", this.itemPickupOverride);
      nbtTagCompound.putInt("InventorySizeOverride", this.inventorySizeOverride);
      nbtTagCompound.putDouble("ItemDropMultiplierOverride", this.itemDropMultiplierOverride);
      nbtTagCompound.putBoolean("AIAttackPlayers", this.aiAttackPlayers);
      nbtTagCompound.putBoolean("AIDefendAnimals", this.aiDefendAnimals);
   }
}
