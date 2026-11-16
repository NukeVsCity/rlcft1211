package com.lycanitesmobs.core.container;

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
import java.util.HashMap;
import java.util.Map;


public class BaseContainer extends Container {
   public Map<Integer, String> specialSlots = new HashMap<>();
   public int playerInventoryStart = -1;
   public int playerInventoryFinish = -1;
   public int inventoryStart = -1;
   public int inventoryFinish = -1;
   public int specialStart = -1;
   public int specialFinish = -1;

   public Slot addSlot(Container inventory, int slotIndex, int x, int y) {
      return this.addSlotToContainer(new BaseSlot(inventory, slotIndex, x, y));
   }

   public void addPlayerSlots(Inventory playerInventory, int offsetX, int offsetY) {
      this.playerInventoryStart = this.inventorySlots.size();
      this.addSlotGrid(playerInventory, 8, 142, 1, 0, 8);
      this.addSlotGrid(playerInventory, 8, 84, 3, 9, 35);
      this.playerInventoryFinish = this.inventorySlots.size() - 1;
   }

   public void addSlotGrid(Container inventory, int x, int y, int totalRows, int start, int finish) {
      int totalSlots = finish - start + 1;
      if (totalSlots >= 1) {
         int excessSlots = totalSlots % totalRows;
         int totalColumns = (totalSlots - excessSlots) / totalRows;
         int slot = 0;

         int row;
         for (row = 0; row < totalRows; row++) {
            for (int column = 0; column < totalColumns; column++) {
               this.addSlot(inventory, start + slot, x + column * 18, y + row * 18);
               slot++;
            }
         }

         row++;

         for (int column = 0; column < excessSlots; column++) {
            this.addSlot(inventory, start + slot, x + column * 18, y + row * 18);
            slot++;
         }
      }
   }

   public void addSlotGrid(Container inventory, int x, int y, int totalRows) {
      this.addSlotGrid(inventory, x, y, totalRows, 0);
   }

   public void addSlotGrid(Container inventory, int x, int y, int totalRows, int start) {
      this.addSlotGrid(inventory, x, y, totalRows, start, inventory.getSizeInventory() - 1);
   }

   public void addSlotsByColumn(Container inventory, int x, int y, int columnMax, int start, int finish) {
      int row = 0;
      int column = 0;

      for (int slot = 0; slot <= finish; slot++) {
         if (column >= columnMax) {
            row++;
            column = 0;
         }

         this.addSlot(inventory, start + slot, x + column * 18, y + row * 18);
         column++;
      }
   }

   public ItemStack transferStackInSlot(Player player, int slotID) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(slotID);
      if (slot != null && slot.getHasStack()) {
         ItemStack slotStack = slot.getStack();
         itemStack = slotStack.copy();
         if (this.isPlayerSlot(slotID)) {
            if (this.specialStart >= 0 && this.specialFinish >= 0) {
               for (int i = this.specialStart; i <= this.specialFinish; i++) {
                  if (i < this.inventorySlots.size()) {
                     Slot targetSlot = (Slot)this.inventorySlots.get(i);
                     if (targetSlot.isItemValid(itemStack) && !this.mergeItemStack(slotStack, i, i + 1, false)) {
                        return ItemStack.EMPTY;
                     }
                  }
               }
            }

            if (this.inventoryStart >= 0
               && this.inventoryFinish >= 0
               && this.inventoryStart < this.inventoryFinish
               && !this.mergeItemStack(slotStack, this.inventoryStart, this.inventoryFinish + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.playerInventoryFinish >= 0 && !this.mergeItemStack(slotStack, this.playerInventoryStart, this.playerInventoryFinish, true)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.getCount() == 0) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }

         return ItemStack.EMPTY;
      } else {
         return itemStack;
      }
   }

   public boolean isPlayerSlot(int slotID) {
      return slotID >= this.playerInventoryStart && slotID <= this.playerInventoryFinish;
   }

   public boolean sortToSpecialSlots(ItemStack slotStack) {
      return true;
   }

   public String getSpecialSlot(int slotID) {
      return !this.specialSlots.containsKey(slotID) ? null : this.specialSlots.get(slotID);
   }

   public boolean canInteractWith(Player entityplayer) {
      return true;
   }
}
