package com.lycanitesmobs.core.inventory;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.info.ObjectLists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InventoryCreature implements Container {
   public static final Map<String, Integer> armorValues = new HashMap<>();
   public static final List<String> equipmentTypes = new ArrayList<>();
   public BaseCreatureEntity creature;
   public String inventoryName = "Creature Inventory";
   protected NonNullList<ItemStack> inventoryContents;
   protected boolean basicArmor = true;
   protected int nextEquipmentSlot = 0;
   public Map<String, Integer> equipmentTypeToSlot = new HashMap<>();
   public Map<Integer, String> equipmentSlotToType = new HashMap<>();

   public static EntityDataAccessor<ItemStack> getEquipmentDataParameter(String type) {
      if (type.equals("head")) {
         return BaseCreatureEntity.EQUIPMENT_HEAD;
      } else if (type.equals("chest")) {
         return BaseCreatureEntity.EQUIPMENT_CHEST;
      } else if (type.equals("legs")) {
         return BaseCreatureEntity.EQUIPMENT_LEGS;
      } else if (type.equals("feet")) {
         return BaseCreatureEntity.EQUIPMENT_FEET;
      } else if (type.equals("saddle")) {
         return BaseCreatureEntity.EQUIPMENT_SADDLE;
      } else {
         return type.equals("bag") ? BaseCreatureEntity.EQUIPMENT_BAG : null;
      }
   }

   public static void registerDataParameters(SynchedEntityData dataManager) {
      for (String equipmentType : equipmentTypes) {
         builder.define(getEquipmentDataParameter(equipmentType), ItemStack.EMPTY);
      }
   }

   public InventoryCreature(String inventoryName, BaseCreatureEntity creature) {
      this.inventoryName = inventoryName;
      this.creature = creature;
      this.addEquipmentSlot("chest");
      this.addEquipmentSlot("saddle");
      this.addEquipmentSlot("bag");
      this.inventoryContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
   }

   protected void addEquipmentSlot(String type) {
      this.equipmentTypeToSlot.put(type, this.nextEquipmentSlot);
      this.equipmentSlotToType.put(this.nextEquipmentSlot, type);
      this.nextEquipmentSlot++;
   }

   public String getName() {
      return this.inventoryName;
   }

   public Component getDisplayName() {
      return Component.literal(this.getName());
   }

   public boolean hasCustomName() {
      return true;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public int getSizeInventory() {
      return this.getItemSlotsSize() + this.getSpecialSlotsSize();
   }

   public int getActiveItemSlotsSize() {
      return !this.getEquipmentStack("bag").isEmpty() ? this.creature.getBagSize() : this.creature.getNoBagSize();
   }

   public int getItemSlotsSize() {
      return this.creature.getInventorySizeMax();
   }

   public int getSpecialSlotsSize() {
      return this.equipmentTypeToSlot.size();
   }

   public boolean hasBagItems() {
      for (ItemStack itemStack : this.inventoryContents) {
         if (itemStack != null) {
            return true;
         }
      }

      return false;
   }

   public boolean isEmpty() {
      for (ItemStack itemstack : this.inventoryContents) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void onInventoryChanged(boolean refresh) {
      if (!this.creature.level().isClientSide) {
         if (this.getEquipmentStack("bag") == null) {
            int bagSizeDiff = this.creature.getBagSize() - this.creature.getNoBagSize();
            if (bagSizeDiff > 0) {
               for (int i = this.creature.getNoBagSize(); i <= this.getItemSlotsSize(); i++) {
                  if (this.getStackInSlot(i - 1) != null) {
                     this.creature.spawnAtLocation(this.getStackInSlot(i - 1));
                     this.setInventorySlotContentsNoUpdate(i - 1, null);
                  }
               }
            }
         }

         for (String type : this.equipmentSlotToType.values()) {
            ItemStack itemStack = this.getEquipmentStack(type);
            EntityDataAccessor<ItemStack> dataParameter = getEquipmentDataParameter(type);
            if (dataParameter != null) {
               if (itemStack == null) {
                  this.creature.getEntityData().set(dataParameter, ItemStack.EMPTY);
               } else {
                  this.creature.getEntityData().set(dataParameter, itemStack);
               }
            }
         }

         if (refresh) {
            this.creature.scheduleGUIRefresh();
         }
      }
   }

   public boolean isUsableByPlayer(Player entityplayer) {
      return true;
   }

   public void openInventory(Player player) {
   }

   public void closeInventory(Player player) {
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
   }

   public ItemStack getStackInSlot(int slotID) {
      return slotID < this.getSizeInventory() && slotID >= 0 ? (ItemStack)this.inventoryContents.get(slotID) : ItemStack.EMPTY;
   }

   public void setInventorySlotContents(int slotID, ItemStack itemStack) {
      this.setInventorySlotContentsNoUpdate(slotID, itemStack);
      this.onInventoryChanged("bag".equals(this.getTypeFromSlot(slotID)));
   }

   public void setInventorySlotContentsNoUpdate(int slotID, ItemStack itemStack) {
      if (slotID < this.getSizeInventory() && slotID >= 0) {
         if (itemStack != null) {
            if (itemStack.getCount() > this.getInventoryStackLimit()) {
               itemStack.setCount(this.getInventoryStackLimit());
            }

            if (itemStack.getCount() < 1) {
               itemStack = null;
            }
         }

         if (itemStack == null) {
            itemStack = ItemStack.EMPTY;
         }

         this.inventoryContents.set(slotID, itemStack);
      }
   }

   public ItemStack decrStackSize(int slot, int amount) {
      if (!this.creature.isAlive()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack[] splitStacks = this.decrStackSize(this.getStackInSlot(slot), amount);
         this.setInventorySlotContents(slot, splitStacks[0]);
         this.onInventoryChanged(false);
         return splitStacks[1];
      }
   }

   public ItemStack removeStackFromSlot(int index) {
      return ItemStack.EMPTY;
   }

   public ItemStack[] decrStackSize(ItemStack itemStack, int amount) {
      ItemStack[] splitStacks = new ItemStack[]{null, null};
      if (itemStack == null) {
         return splitStacks;
      } else {
         if (itemStack.getCount() <= amount) {
            splitStacks[0] = null;
            splitStacks[1] = itemStack;
         } else {
            splitStacks[1] = itemStack.splitStack(amount);
            if (itemStack.getCount() == 0) {
               itemStack = null;
            }

            splitStacks[0] = itemStack;
         }

         return splitStacks;
      }
   }

   public boolean isItemValidForSlot(int slotID, ItemStack itemStack) {
      if (!this.creature.isAlive()) {
         return false;
      } else {
         String type = this.getTypeFromSlot(slotID);
         if (type != null) {
            if (!this.isEquipmentValidForSlot(type, itemStack)) {
               return false;
            }

            ItemStack equipedStack = this.getStackInSlot(slotID);
            if (equipedStack != null && !equipedStack.isEmpty()) {
               return false;
            }
         }

         return true;
      }
   }

   public int getSpaceForStack(ItemStack itemStack) {
      if (itemStack == null) {
         return 0;
      } else if (itemStack.getItem() != null && itemStack.getCount() >= 1) {
         int space = 0;

         for (int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
            if (this.isItemValidForSlot(slotID, itemStack)) {
               ItemStack slotStack = this.getStackInSlot(slotID);
               if (slotStack != null && slotStack != ItemStack.EMPTY && !slotStack.isEmpty()) {
                  if (slotStack.getCount() < slotStack.getMaxStackSize()
                     && slotStack.getItem() == itemStack.getItem()
                     && slotStack.getDamageValue() == itemStack.getDamageValue()) {
                     space += slotStack.getMaxStackSize() - slotStack.getCount();
                  }
               } else {
                  space += itemStack.getMaxStackSize();
               }
            }

            if (space >= itemStack.getCount()) {
               break;
            }
         }

         return Math.min(space, itemStack.getCount());
      } else {
         return 0;
      }
   }

   public ItemStack autoInsertStack(ItemStack itemStack) {
      if (itemStack != null && itemStack.getCount() >= 1 && this.creature.isAlive()) {
         for (int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
            ItemStack slotStack = (ItemStack)this.inventoryContents.get(slotID);
            if (slotStack != null && slotStack != ItemStack.EMPTY && !slotStack.isEmpty()) {
               if (slotStack.getCount() < slotStack.getMaxStackSize()
                  && slotStack.getItem() == itemStack.getItem()
                  && slotStack.getDamageValue() == itemStack.getDamageValue()) {
                  int space = Math.max(slotStack.getMaxStackSize() - slotStack.getCount(), 0);
                  if (space >= itemStack.getCount()) {
                     this.getStackInSlot(slotID).setCount(this.getStackInSlot(slotID).getCount() + itemStack.getCount());
                     itemStack = null;
                  } else {
                     this.getStackInSlot(slotID).setCount(this.getStackInSlot(slotID).getCount() + space);
                     itemStack.setCount(itemStack.getCount() - space);
                  }
               }
            } else {
               this.setInventorySlotContents(slotID, itemStack);
               itemStack = null;
            }

            if (itemStack == null) {
               break;
            }
         }

         return itemStack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setAdvancedArmor(boolean advanced) {
      this.basicArmor = !advanced;
      if (advanced) {
         this.addEquipmentSlot("head");
         this.addEquipmentSlot("legs");
         this.addEquipmentSlot("feet");
      }
   }

   public boolean useAdvancedArmor() {
      return !this.basicArmor;
   }

   public int getSlotFromType(String type) {
      return this.equipmentTypeToSlot.containsKey(type) ? this.equipmentTypeToSlot.get(type) + this.getItemSlotsSize() : -1;
   }

   public String getTypeFromSlot(int slotID) {
      return this.equipmentSlotToType.containsKey(slotID - this.getItemSlotsSize()) ? this.equipmentSlotToType.get(slotID - this.getItemSlotsSize()) : null;
   }

   public ItemStack getEquipmentStack(String type) {
      if (getEquipmentDataParameter(type) == null) {
         return ItemStack.EMPTY;
      } else if (this.creature.level().isClientSide) {
         try {
            return (ItemStack)this.creature.getEntityData().get(getEquipmentDataParameter(type));
         } catch (Exception var3) {
            return ItemStack.EMPTY;
         }
      } else {
         return this.getStackInSlot(this.getSlotFromType(type));
      }
   }

   public void setEquipmentStack(String type, ItemStack itemStack) {
      if (!this.creature.level().isClientSide && this.equipmentTypeToSlot.containsKey(type) && this.isEquipmentValidForSlot(type, itemStack)) {
         this.setInventorySlotContents(this.getSlotFromType(type), itemStack);
      }
   }

   public void setEquipmentStack(ItemStack itemStack) {
      String type = this.getSlotForEquipment(itemStack);
      if (type != null) {
         this.setEquipmentStack(type, itemStack);
      }
   }

   public String getSlotForEquipment(ItemStack itemStack) {
      if (itemStack == null) {
         return null;
      } else {
         if (this.basicArmor) {
            if (itemStack.getItem() == Items.IRON_HORSE_ARMOR) {
               return "chest";
            }

            if (itemStack.getItem() == Items.GOLDEN_HORSE_ARMOR) {
               return "chest";
            }

            if (itemStack.getItem() == Items.DIAMOND_HORSE_ARMOR) {
               return "chest";
            }
         }

         if (!this.basicArmor && itemStack.getItem() instanceof ArmorItem) {
            ArmorItem armorstack = (ArmorItem)itemStack.getItem();
            if (armorstack.armorType == EquipmentSlot.HEAD) {
               return "head";
            }

            if (armorstack.armorType == EquipmentSlot.CHEST) {
               return "chest";
            }

            if (armorstack.armorType == EquipmentSlot.LEGS) {
               return "legs";
            }

            if (armorstack.armorType == EquipmentSlot.FEET) {
               return "feet";
            }
         }

         if (this.creature instanceof RideableCreatureEntity && itemStack.getItem() == this.creature.creatureInfo.creatureType.saddle) {
            return "saddle";
         } else if (itemStack.getItem() == Item.byBlock(Blocks.CHEST)) {
            return "bag";
         } else {
            return ObjectLists.isInOreDictionary("chestWood", itemStack) ? "bag" : null;
         }
      }
   }

   public boolean isEquipmentValidForSlot(String type, ItemStack itemStack) {
      return itemStack == null ? true : type.equals(this.getSlotForEquipment(itemStack));
   }

   public String getEquipmentGrade(String type) {
      ItemStack equipmentStack = this.getEquipmentStack(type);
      if (equipmentStack == null) {
         return null;
      } else {
         if (equipmentStack.getItem() instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem)equipmentStack.getItem();
            if (armor.getArmorMaterial() == ArmorMaterial.LEATHER) {
               return "Leather";
            }

            if (armor.getArmorMaterial() == ArmorMaterial.IRON) {
               return "Iron";
            }

            if (armor.getArmorMaterial() == ArmorMaterial.CHAIN) {
               return "Chain";
            }

            if (armor.getArmorMaterial() == ArmorMaterial.GOLD) {
               return "Gold";
            }

            if (armor.getArmorMaterial() == ArmorMaterial.DIAMOND) {
               return "Diamond";
            }
         }

         if (equipmentStack.getItem() == Items.IRON_HORSE_ARMOR) {
            return "Iron";
         } else if (equipmentStack.getItem() == Items.GOLDEN_HORSE_ARMOR) {
            return "Gold";
         } else {
            return equipmentStack.getItem() == Items.DIAMOND_HORSE_ARMOR ? "Diamond" : null;
         }
      }
   }

   public int getArmorValue() {
      String[] armorSlots = new String[]{"head", "chest", "legs", "feet"};
      int totalArmor = 0;

      for (String armorSlot : armorSlots) {
         ItemStack armorStack = this.getEquipmentStack(armorSlot);
         if (armorStack != null) {
            if (armorStack.getItem() instanceof ArmorItem) {
               totalArmor += ((ArmorItem)armorStack.getItem()).damageReduceAmount;
            } else if (this.getEquipmentGrade(armorSlot) != null && armorValues.containsKey(this.getEquipmentGrade(armorSlot))) {
               totalArmor += armorValues.get(this.getEquipmentGrade(armorSlot));
            }
         }
      }

      return totalArmor;
   }

   public void dropInventory() {
      for (int slotID = 0; slotID < this.inventoryContents.size(); slotID++) {
         ItemStack itemStack = (ItemStack)this.inventoryContents.get(slotID);
         if (itemStack != null) {
            this.creature.spawnAtLocation(itemStack);
            this.setInventorySlotContentsNoUpdate(slotID, ItemStack.EMPTY);
         }
      }
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(nbtTagCompound, itemStacks);

      for (int i = 0; i < itemStacks.size(); i++) {
         if (i < this.getSizeInventory()) {
            ItemStack itemStack = (ItemStack)itemStacks.get(i);
            if (itemStack.isEmpty()) {
               this.setInventorySlotContentsNoUpdate(i, ItemStack.EMPTY);
            } else {
               this.setInventorySlotContentsNoUpdate(i, itemStack);
            }
         }
      }

      this.onInventoryChanged(false);
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

      for (int i = 0; i < this.getSizeInventory(); i++) {
         if (this.getStackInSlot(i) != null) {
            itemStacks.set(i, this.getStackInSlot(i));
         }
      }

      ContainerHelper.saveAllItems(nbtTagCompound, itemStacks);
   }

   public void markDirty() {
   }

   static {
      armorValues.put("Leather", 5);
      armorValues.put("Iron", 10);
      armorValues.put("Gold", 15);
      armorValues.put("Chain", 17);
      armorValues.put("Diamond", 20);
      equipmentTypes.add("head");
      equipmentTypes.add("chest");
      equipmentTypes.add("legs");
      equipmentTypes.add("feet");
      equipmentTypes.add("saddle");
      equipmentTypes.add("bag");
   }
}
