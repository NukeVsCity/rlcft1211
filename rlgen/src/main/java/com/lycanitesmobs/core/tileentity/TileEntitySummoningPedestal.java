package com.lycanitesmobs.core.tileentity;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockSummoningPedestal;
import com.lycanitesmobs.core.container.SummoningPedestalContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.PortalEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.network.MessageSummoningPedestalStats;
import com.lycanitesmobs.core.network.MessageSummoningPedestalSummonSet;
import com.lycanitesmobs.core.pets.SummonSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TileEntitySummoningPedestal extends TileEntityBase {
   public long updateTick = 0L;
   public PortalEntity summoningPortal;
   public UUID ownerUUID;
   public String ownerName = "";
   public SummonSet summonSet;
   public int summonAmount = 1;
   public int capacityCharge = 100;
   public int capacity = 0;
   public int capacityMax = this.capacityCharge * 10;
   public int summonProgress = 0;
   public int summonProgressMax = 180;
   public String inventoryName = "";
   public NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
   public int summoningFuel = 0;
   public int summoningFuelMax = 12000;
   public int summoningFuelAmount = 12000;
   public List<BaseCreatureEntity> minions = new ArrayList<>();
   protected String[] loadMinionIDs;
   protected boolean blockStateSet = false;

   public TileEntitySummoningPedestal() {
      this.summoningFuelAmount = LycanitesMobs.config
         .getInt(
            "Player",
            "Summoning Pedestal Redstone Time",
            this.summoningFuelAmount,
            "How much summoning time (in ticks) 1 redstone dust provides. 20 ticks = 1 second, default is 12000 (10 minutes)."
         );
   }

   @Override
   public void onRemove() {
      if (this.summoningPortal != null && !this.summoningPortal.isRemoved()) {
         this.summoningPortal.discard();
      }

      for (ItemStack itemStack : this.itemStacks) {
         EntityItemCustom entityItem = new EntityItemCustom(
            this.getWorld(),
            (double)this.getPos().getX(),
            (double)this.getPos().getY() + 0.5,
            (double)this.getPos().getZ(),
            itemStack
         );
         this.getWorld().addFreshEntity(entityItem);
      }

      this.clear();
      super.onRemove();
   }

   @Override
   public void update() {
      if (this.getWorld().isClientSide) {
         if (this.summoningFuel > 0) {
            if (this.summonProgress >= this.summonProgressMax) {
               this.summonProgress = 0;
            } else if (this.summonProgress > 0) {
               this.summonProgress++;
            }
         }
      } else {
         if (this.loadMinionIDs != null) {
            int range = 20;

            for (BaseCreatureEntity possibleEntity : this.getWorld()
               .getEntitiesOfClass(
                  BaseCreatureEntity.class,
                  new AABB(
                     (double)(this.getPos().getX() - range),
                     (double)(this.getPos().getY() - range),
                     (double)(this.getPos().getZ() - range),
                     (double)(this.getPos().getX() + range),
                     (double)(this.getPos().getY() + range),
                     (double)(this.getPos().getZ() + range)
                  )
               )) {
               for (String loadMinionID : this.loadMinionIDs) {
                  UUID uuid = null;

                  try {
                     uuid = UUID.fromString(loadMinionID);
                  } catch (Exception var11) {
                  }

                  if (possibleEntity.getUUID().equals(uuid)) {
                     this.minions.add(possibleEntity);
                     break;
                  }
               }
            }

            this.loadMinionIDs = null;
         }

         if (this.summonSet != null && this.summonSet.getCreatureInfo() != null) {
            if (this.summonSet.getFollowing()) {
               this.summonSet.following = false;
            }

            if (this.updateTick % 100L == 0L) {
               this.capacity = 0;

               for (BaseCreatureEntity minion : this.minions.toArray(new BaseCreatureEntity[this.minions.size()])) {
                  if (minion != null && !minion.isRemoved()) {
                     this.capacity = this.capacity + minion.creatureInfo.summonCost * this.capacityCharge;
                  } else {
                     this.minions.remove(minion);
                  }
               }
            }

            if (this.capacity + this.summonSet.getCreatureInfo().summonCost > this.capacityMax) {
               this.summonProgress = 0;
            } else {
               if (this.summoningFuel <= 0) {
                  ItemStack fuelStack = this.getStackInSlot(0);
                  if (!fuelStack.isEmpty()) {
                     int refuel = this.summoningFuelAmount;
                     if (fuelStack.getItem() == Item.byBlock(Blocks.REDSTONE_BLOCK)) {
                        refuel = this.summoningFuelAmount * 9;
                     }

                     fuelStack.shrink(1);
                     this.summoningFuel = refuel;
                     this.summoningFuelMax = refuel;
                  }
               }

               if (this.summoningFuel > 0) {
                  this.summoningFuel--;
                  if (this.summoningPortal == null || this.summoningPortal.isRemoved()) {
                     this.summoningPortal = new PortalEntity(this.getWorld(), this);
                     this.summoningPortal.setProjectileScale(4.0F);
                     this.getWorld().addFreshEntity(this.summoningPortal);
                  }

                  if (this.summonProgress++ >= this.summonProgressMax) {
                     this.summoningPortal.summonCreatures();
                     this.summonProgress = 0;
                     this.capacity = Math.min(this.capacity + this.capacityCharge * this.summonSet.getCreatureInfo().summonCost, this.capacityMax);
                  }
               }
            }
         }

         if (this.summoningFuel <= 0 && this.summoningPortal != null && !this.summoningPortal.isRemoved()) {
            this.summoningPortal.discard();
         }

         if (!this.blockStateSet) {
            if (!"".equals(this.getOwnerName())) {
               BlockSummoningPedestal.setState(BlockSummoningPedestal.EnumSummoningPedestal.PLAYER, this.getWorld(), this.getPos());
            } else {
               BlockSummoningPedestal.setState(BlockSummoningPedestal.EnumSummoningPedestal.NONE, this.getWorld(), this.getPos());
            }

            this.blockStateSet = true;
         }

         if (this.updateTick % 20L == 0L) {
            LycanitesMobs.packetHandler
               .sendToAllAround(
                  new MessageSummoningPedestalStats(
                     this.capacity,
                     this.summonProgress,
                     this.summoningFuel,
                     this.summoningFuelMax,
                     this.getPos().getX(),
                     this.getPos().getY(),
                     this.getPos().getZ()
                  ),
                  new TargetPoint(
                     LycDim.id(this.getWorld()),
                     (double)this.getPos().getX(),
                     (double)this.getPos().getY(),
                     (double)this.getPos().getZ(),
                     5.0
                  )
               );
         }

         this.updateTick++;
      }
   }

   public void setOwner(LivingEntity entity) {
      if (entity instanceof Player) {
         Player entityPlayer = (Player)entity;
         this.ownerUUID = entityPlayer.getUUID();
         this.ownerName = entity.getName();
      }
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public String getOwnerName() {
      return this.ownerName;
   }

   public Player getPlayer() {
      return this.ownerUUID == null ? null : this.getWorld().getPlayerEntityByUUID(this.ownerUUID);
   }

   public Class getSummonClass() {
      return this.summonSet == null ? null : this.summonSet.getCreatureClass();
   }

   public void setSummonSet(SummonSet summonSet) {
      if (this.getPlayer() == null || summonSet.isUseable()) {
         this.summonSet = new SummonSet(null);
         this.summonSet.setSummonType(summonSet.summonType);
         this.summonSet.sitting = summonSet.getSitting();
         this.summonSet.following = false;
         this.summonSet.passive = summonSet.getPassive();
         this.summonSet.aggressive = summonSet.getAggressive();
         this.summonSet.pvp = summonSet.getPVP();
      }
   }

   public void applyMinionBehaviour(TameableCreatureEntity minion) {
      minion.fromSummoningPedestal = true;
      if (this.summonSet != null) {
         this.summonSet.applyBehaviour(minion);
         minion.setSubspecies(this.summonSet.subspecies);
         minion.applyVariant(this.summonSet.variant);
      }

      this.minions.add(minion);
      minion.setHome(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 20.0F);
   }

   public boolean isEmpty() {
      for (ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getStackInSlot(int index) {
      return (ItemStack)this.itemStacks.get(index);
   }

   public ItemStack decrStackSize(int index, int count) {
      return ContainerHelper.getAndSplit(this.itemStacks, index, count);
   }

   public ItemStack removeStackFromSlot(int index) {
      return ContainerHelper.getAndRemove(this.itemStacks, index);
   }

   public void setInventorySlotContents(int index, ItemStack stack) {
      this.itemStacks.set(index, stack);
      if (stack.getCount() > this.getInventoryStackLimit()) {
         stack.setCount(this.getInventoryStackLimit());
      }
   }

   public int getSizeInventory() {
      return this.itemStacks.size();
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void openInventory(Player player) {
   }

   public void closeInventory(Player player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack itemStack) {
      return itemStack.getItem() == Items.REDSTONE || itemStack.getItem() == Item.byBlock(Blocks.REDSTONE_BLOCK);
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      this.itemStacks.clear();
   }

   public String getName() {
      return this.inventoryName;
   }

   public boolean hasCustomName() {
      return !"".equals(this.inventoryName);
   }

   @Override
   public boolean receiveClientEvent(int eventID, int eventArg) {
      return false;
   }

   public SPacketUpdateTileEntity getUpdatePacket() {
      CompoundTag syncData = new CompoundTag();
      if (this.summonSet != null) {
         CompoundTag summonSetNBT = new CompoundTag();
         this.summonSet.writeToNBT(summonSetNBT);
         syncData.put("SummonSet", summonSetNBT);
      }

      if (!this.getWorld().isClientSide && this.getOwnerUUID() != null && this.getOwnerName() != null) {
         syncData.putString("OwnerUUID", this.getOwnerUUID().toString());
         syncData.putString("OwnerName", this.getOwnerName());
         syncData.putString("InventoryName", this.getName());
      }

      return new SPacketUpdateTileEntity(this.getPos(), 1, syncData);
   }

   @Override
   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
      if (this.getWorld().isClientSide) {
         CompoundTag syncData = packet.getNbtCompound();
         if (syncData.contains("OwnerUUID")) {
            this.ownerUUID = UUID.fromString(syncData.getString("OwnerUUID"));
         }

         if (syncData.contains("OwnerName")) {
            this.ownerName = syncData.getString("OwnerName");
         }

         if (syncData.contains("InventoryName")) {
            this.inventoryName = syncData.getString("InventoryName");
         }

         if (syncData.contains("SummonSet")) {
            SummonSet summonSet = new SummonSet(null);
            summonSet.readFromNBT(syncData.getCompound("SummonSet"));
            this.summonSet = summonSet;
         }
      }
   }

   public void sendSummonSetToServer(SummonSet summonSet) {
      LycanitesMobs.packetHandler
         .sendToServer(
            new MessageSummoningPedestalSummonSet(
               summonSet, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()
            )
         );
   }

   @Override
   public void readFromNBT(CompoundTag nbtTagCompound) {
      super.readFromNBT(nbtTagCompound);
      if (nbtTagCompound.contains("OwnerUUID")) {
         String uuidString = nbtTagCompound.getString("OwnerUUID");
         if (!"".equals(uuidString)) {
            this.ownerUUID = UUID.fromString(uuidString);
         } else {
            this.ownerUUID = null;
         }
      } else {
         this.ownerUUID = null;
      }

      if (nbtTagCompound.contains("OwnerName")) {
         this.ownerName = nbtTagCompound.getString("OwnerName");
      } else {
         this.ownerName = "";
      }

      if (nbtTagCompound.contains("SummonSet")) {
         CompoundTag summonSetNBT = nbtTagCompound.getCompound("SummonSet");
         SummonSet summonSet = new SummonSet(null);
         summonSet.readFromNBT(summonSetNBT);
         this.summonSet = summonSet;
      } else {
         this.summonSet = null;
      }

      if (nbtTagCompound.contains("MinionIDs")) {
         ListTag minionIDs = nbtTagCompound.getList("MinionIDs", 10);
         this.loadMinionIDs = new String[minionIDs.size()];

         for (int i = 0; i < minionIDs.size(); i++) {
            CompoundTag minionID = minionIDs.getCompound(i);
            if (minionID.contains("ID")) {
               this.loadMinionIDs[i] = minionID.getString("ID");
            }
         }
      }

      if (nbtTagCompound.contains("Fuel")) {
         this.summoningFuel = nbtTagCompound.getInt("Fuel");
      }

      if (nbtTagCompound.contains("FuelMax")) {
         this.summoningFuelMax = nbtTagCompound.getInt("FuelMax");
      }

      if (nbtTagCompound.contains("Items")) {
         NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
         ContainerHelper.loadAllItems(nbtTagCompound, itemStacks);

         for (int ix = 0; ix < itemStacks.size(); ix++) {
            if (ix < this.getSizeInventory()) {
               ItemStack itemStack = (ItemStack)itemStacks.get(ix);
               if (itemStack.isEmpty()) {
                  this.setInventorySlotContents(ix, ItemStack.EMPTY);
               } else {
                  this.setInventorySlotContents(ix, itemStack);
               }
            }
         }
      }

      super.readFromNBT(nbtTagCompound);
   }

   @Override
   public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
      super.writeToNBT(nbtTagCompound);
      if (this.ownerUUID == null) {
         nbtTagCompound.putString("OwnerUUID", "");
      } else {
         nbtTagCompound.putString("OwnerUUID", this.ownerUUID.toString());
      }

      if (this.summonSet != null) {
         CompoundTag summonSetNBT = new CompoundTag();
         this.summonSet.writeToNBT(summonSetNBT);
         nbtTagCompound.put("SummonSet", summonSetNBT);
      }

      nbtTagCompound.putString("OwnerName", this.ownerName);
      if (this.minions.size() > 0) {
         ListTag minionIDs = new ListTag();

         for (LivingEntity minion : this.minions) {
            CompoundTag minionID = new CompoundTag();
            minionID.putString("ID", minion.getUUID().toString());
            minionIDs.add(minionID);
         }

         nbtTagCompound.put("MinionIDs", minionIDs);
      }

      nbtTagCompound.putInt("Fuel", this.summoningFuel);
      nbtTagCompound.putInt("FuelMax", this.summoningFuelMax);
      ContainerHelper.saveAllItems(nbtTagCompound, this.itemStacks);
      return nbtTagCompound;
   }

   @Override
   public Object getGUI(Player player) {
      return this.getWorld().isClientSide ? new SummoningPedestalScreen(player, this) : new SummoningPedestalContainer(this, player.getInventory());
   }
}
