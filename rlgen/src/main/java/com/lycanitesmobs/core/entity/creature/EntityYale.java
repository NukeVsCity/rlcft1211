package com.lycanitesmobs.core.entity.creature;

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
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.EatBlockGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.info.ItemDrop;
import java.util.ArrayList;
import java.util.Random;


public class EntityYale extends AgeableCreatureEntity implements IShearable {
   protected static final EntityDataAccessor<Byte> FUR = SynchedEntityData.defineId(EntityYale.class, EntityDataSerializers.BYTE);
   public ItemDrop woolDrop;
   private final InventoryCrafting colorMixer = new InventoryCrafting(new Container() {
      public boolean canInteractWith(Player par1EntityPlayer) {
         return false;
      }
   }, 2, 1);

   public EntityYale(Level world) {
      super(LycEntities.type(EntityYale.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = false;
      this.canGrow = true;
      this.babySpawnChance = 0.1;
      this.fleeHealthPercent = 1.0F;
      this.isAggressiveByDefault = false;
      this.setupMob();
      this.woolDrop = new ItemDrop(LycReg.key(Blocks.WHITE_WOOL).toString(), 0, 1.0F).setMinAmount(1).setMaxAmount(3);
   }

   @Override
   protected void initEntityAI() {
      this.tasks
         .addTask(this.nextIdleGoalIndex++, new EatBlockGoal(this).setBlocks(Blocks.GRASS_BLOCK).setReplaceBlock(Blocks.DIRT));
      super.initEntityAI();
      this.tasks.addTask(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FUR, (byte)1);
   }

   @Override
   public void onFirstSpawn() {
      if (!this.isBaby()) {
         this.setColor(this.getRandomFurColor(this.getRandom()));
      }

      super.onFirstSpawn();
   }

   public boolean isShearable(ItemStack item, BlockGetter world, BlockPos pos) {
      return this.hasFur() && !this.isBaby();
   }

   public ArrayList<ItemStack> onSheared(ItemStack item, BlockGetter world, BlockPos pos, int fortune) {
      ArrayList<ItemStack> dropStacks = new ArrayList<>();
      if (this.woolDrop == null) {
         return dropStacks;
      } else {
         this.setFur(false);
         this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
         int quantity = this.woolDrop.getQuantity(this.getRandom(), fortune, 1);
         ItemStack dropStack = this.woolDrop.getEntityDropItemStack(this, quantity);
         this.spawnAtLocation(dropStack);
         dropStacks.add(dropStack);
         return dropStacks;
      }
   }

   public boolean hasFur() {
      return this.entityData == null ? true : (Byte)this.entityData.get(FUR) > 0;
   }

   public void setFur(boolean fur) {
      if (!this.level().isClientSide) {
         this.entityData.set(FUR, (byte)(fur ? 1 : 0));
      }
   }

   @Override
   public void onEat() {
      if (!this.level().isClientSide) {
         this.setFur(true);
      }
   }

   @Override
   public boolean canBeColored(Player player) {
      return true;
   }

   @Override
   public void setColor(int color) {
      if (this.woolDrop == null) {
         this.woolDrop = new ItemDrop(LycReg.key(Blocks.WHITE_WOOL).toString(), 0, 1.0F).setMinAmount(1).setMaxAmount(3);
      } else if (this.woolDrop.getItemStack().getItem() == Item.byBlock(Blocks.WHITE_WOOL)) {
         this.woolDrop.setDrop(new ItemStack(Blocks.WHITE_WOOL, 1, color));
      }

      super.setColor(color);
   }

   public int getRandomFurColor(RandomSource random) {
      int i = random.nextInt(100);
      return i < 5 ? 15 : (i < 10 ? 7 : (i < 15 ? 8 : (i < 18 ? 12 : (random.nextInt(500) == 0 ? 6 : 0))));
   }

   public int getMixedFurColor(BaseCreatureEntity entityA, BaseCreatureEntity entityB) {
      int i = 15 - entityA.getColor();
      int j = 15 - entityB.getColor();
      if (i == j) {
         return 15 - i;
      } else {
         this.colorMixer.getStackInSlot(0).setDamageValue(i);
         this.colorMixer.getStackInSlot(1).setDamageValue(j);
         ItemStack itemstack = CraftingManager.findMatchingResult(this.colorMixer, entityA.world);
         int k;
         if (!itemstack.isEmpty() && itemstack.getItem() == Items.INK_SAC) {
            k = itemstack.getDamageValue();
         } else {
            k = this.level().random.nextBoolean() ? i : j;
         }

         return 15 - k;
      }
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      BlockState blockState = this.level().getBlockState(LycPos.of(x, y - 1, z));
      Block block = blockState.getBlock();
      if (block != Blocks.AIR) {
         if (Material.of(blockState) == Material.GRASS) {
            return 10.0F;
         }

         if (Material.of(blockState) == Material.GROUND) {
            return 7.0F;
         }
      }

      return super.getBlockPathWeight(x, y, z);
   }

   @Override
   public boolean canBeLeashedTo(Player player) {
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
   public float getFallResistance() {
      return 50.0F;
   }

   @Override
   public boolean canDropItem(ItemDrop itemDrop) {
      if (!super.canDropItem(itemDrop)) {
         return false;
      } else {
         return itemDrop.getItemStack().getItem() instanceof BlockItem
               && ((BlockItem)itemDrop.getItemStack().getItem()).getBlock() == Blocks.WHITE_WOOL
            ? this.hasFur()
            : true;
      }
   }

   @Override
   public void dropItem(ItemStack itemStack) {
      if (this.woolDrop != null
         && itemStack.getItem() instanceof BlockItem
         && ((BlockItem)itemStack.getItem()).getBlock() == Blocks.WHITE_WOOL) {
         itemStack = new ItemStack(itemStack.getItem(), itemStack.getCount(), this.woolDrop.getItemStack().getMetadata());
      }

      super.spawnAtLocation(itemStack);
   }

   @Override
   public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
      AgeableCreatureEntity baby = new EntityYale(this.level());
      int color = this.getMixedFurColor(this, partner);
      baby.setColor(color);
      return baby;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.readAdditionalSaveData(nbtTagCompound);
      if (nbtTagCompound.contains("HasFur")) {
         this.setFur(nbtTagCompound.getBoolean("HasFur"));
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putBoolean("HasFur", this.hasFur());
   }
}
