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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.EquipmentForgeContainer;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;


public class TileEntityEquipmentForge extends TileEntityBase {
   protected NonNullList<ItemStack> itemStacks = NonNullList.withSize(ItemEquipment.PART_LIMIT + 1, ItemStack.EMPTY);
   protected int level = 1;

   @Override
   public void onRemove() {
      for (ItemStack itemStack : this.itemStacks) {
         if (!(itemStack.getItem() instanceof ItemEquipment)) {
            EntityItemCustom entityItem = new EntityItemCustom(
               this.getWorld(),
               (double)this.getPos().getX(),
               (double)this.getPos().getY() + 0.5,
               (double)this.getPos().getZ(),
               itemStack
            );
            this.getWorld().addFreshEntity(entityItem);
         }
      }

      this.clear();
      super.onRemove();
   }

   @Override
   public void update() {
      super.update();
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
      if (!(itemStack.getItem() instanceof ItemEquipment) && !(itemStack.getItem() instanceof ItemEquipmentPart)) {
         return false;
      } else {
         ItemStack existingStack = this.getStackInSlot(index);
         return existingStack.isEmpty();
      }
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

   @Override
   public boolean receiveClientEvent(int eventID, int eventArg) {
      return false;
   }

   public SPacketUpdateTileEntity getUpdatePacket() {
      CompoundTag syncData = new CompoundTag();
      if (!this.getWorld().isClientSide) {
         syncData.putInt("ForgeLevel", this.level);
      }

      return new SPacketUpdateTileEntity(this.getPos(), 1, syncData);
   }

   @Override
   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
      if (this.getWorld().isClientSide) {
         CompoundTag syncData = packet.getNbtCompound();
         if (syncData.contains("ForgeLevel")) {
            this.level = syncData.getInt("ForgeLevel");
         }
      }
   }

   @Override
   public void onGuiButton(byte buttonId) {
      LycanitesMobs.logDebug("", "Received button packet id: " + buttonId);
   }

   @Override
   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("ForgeLevel")) {
         this.level = nbtTagCompound.getInt("ForgeLevel");
      }

      if (nbtTagCompound.contains("Items")) {
         ContainerHelper.loadAllItems(nbtTagCompound, this.itemStacks);
      }

      super.readFromNBT(nbtTagCompound);
   }

   @Override
   public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
      nbtTagCompound.putInt("ForgeLevel", this.level);
      ContainerHelper.saveAllItems(nbtTagCompound, this.itemStacks);
      return super.writeToNBT(nbtTagCompound);
   }

   @Override
   public Object getGUI(Player player) {
      if (player.level().isClientSide) {
         return new EquipmentForgeScreen(this, player.getInventory());
      } else {
         if (player instanceof ServerPlayer) {
            ((ServerPlayer)player).connection.sendPacket(this.getUpdatePacket());
         }

         return new EquipmentForgeContainer(this, player.getInventory());
      }
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public String getName() {
      String levelName = "lesser";
      if (this.level == 2) {
         levelName = "greater";
      } else if (this.level >= 3) {
         levelName = "master";
      }

      return LanguageManager.translate("tile.equipmentforge_" + levelName + ".name");
   }

   public boolean hasCustomName() {
      return false;
   }
}
