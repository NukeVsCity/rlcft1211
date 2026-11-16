package com.lycanitesmobs.core.container;

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
import com.lycanitesmobs.core.inventory.InventoryCreature;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.equipment.features.EquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.SlotEquipmentFeature;
import java.util.ArrayList;
import java.util.List;


public class EquipmentForgeSlot extends BaseSlot {
   public String type;
   public EquipmentForgeContainer containerForge;
   public List<EquipmentForgeSlot> childSlots = new ArrayList<>();

   public EquipmentForgeSlot(EquipmentForgeContainer containerForge, int slotIndex, int x, int y, String type) {
      super(containerForge.equipmentForge, slotIndex, x, y);
      this.containerForge = containerForge;
      this.type = type;
   }

   @Override
   public boolean isItemValid(ItemStack itemStack) {
      Item item = itemStack.getItem();
      if (item instanceof ItemEquipmentPart) {
         ItemEquipmentPart equipmentPart = (ItemEquipmentPart)item;
         return this.type.equals(equipmentPart.slotType) && equipmentPart.getLevel(itemStack) <= this.containerForge.equipmentForge.getLevel();
      } else if (item instanceof ItemEquipment) {
         if (this.containerForge.equipmentForge.getLevel() < 3) {
            ItemEquipment equipment = (ItemEquipment)item;
            int equipmentLevel = equipment.getHighestLevel(itemStack);
            if (equipmentLevel > this.containerForge.equipmentForge.getLevel()) {
               return false;
            }
         }

         return this.type.equals("piece") && !this.getHasStack();
      } else {
         return false;
      }
   }

   public boolean getHasStack() {
      if (!super.getHasStack()) {
         return false;
      } else {
         if (this.getStack().getItem() instanceof ItemEquipment) {
            ItemEquipment itemEquipment = (ItemEquipment)this.getStack().getItem();
            if (itemEquipment.getEquipmentPartCount(this.getStack()) == 0) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public int getSlotStackLimit() {
      return this.inventory instanceof InventoryCreature && ((InventoryCreature)this.inventory).getTypeFromSlot(this.getSlotIndex()) != null
         ? 1
         : this.inventory.getInventoryStackLimit();
   }

   public void setType(String type) {
      this.type = type;
   }

   public void addChildSlot(EquipmentForgeSlot slot) {
      if (!this.childSlots.contains(slot)) {
         this.childSlots.add(slot);
         this.updateChildSlots();
      }
   }

   public boolean updateChildSlot(int index, String type) {
      if (index >= this.childSlots.size()) {
         return false;
      } else {
         this.childSlots.get(index).setType(type);
         return true;
      }
   }

   public void putStack(ItemStack itemStack) {
      this.putStackWithoutUpdate(itemStack);
      if ("piece".equals(this.type)) {
         this.containerForge.onEquipmentPieceSlotChanged(this);
      } else {
         this.containerForge.onEquipmentPartSlotChanged(this);
      }
   }

   public void putStackWithoutUpdate(ItemStack itemStack) {
      super.putStack(itemStack);
      if (!this.childSlots.isEmpty()) {
         this.updateChildSlots();
      }
   }

   public ItemStack onTake(Player player, ItemStack itemStack) {
      Item item = itemStack.getItem();
      if (item instanceof ItemEquipmentPart) {
         this.updateChildSlots();
      }

      if (item instanceof ItemEquipment) {
         int forgeLevel = this.containerForge.equipmentForge.getLevel();
         if (forgeLevel < 3) {
            ((ItemEquipment)item).applyLevelCap(itemStack, this.containerForge.equipmentForge.getLevel());
         }
      }

      if ("piece".equals(this.type)) {
         this.containerForge.onEquipmentPieceSlotChanged(this);
      } else {
         this.containerForge.onEquipmentPartSlotChanged(this);
      }

      return super.onTake(player, itemStack);
   }

   public boolean canTakeStack(Player player) {
      if (!this.childSlots.isEmpty()) {
         for (EquipmentForgeSlot childSlot : this.childSlots) {
            if (!childSlot.getStack().isEmpty()) {
               return false;
            }
         }
      } else if ("piece".equals(this.type) && this.getHasStack() && !this.containerForge.isEquipmentValid()) {
         return false;
      }

      return true;
   }

   public void updateChildSlots() {
      Item item = this.getStack().getItem();
      List<Integer> updatedChildSlots = new ArrayList<>();
      if (item instanceof ItemEquipmentPart) {
         ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart)item;
         int axeSlots = 0;

         for (EquipmentFeature feature : itemEquipmentPart.features) {
            if (feature instanceof SlotEquipmentFeature) {
               SlotEquipmentFeature slotFeature = (SlotEquipmentFeature)feature;
               int level = itemEquipmentPart.getLevel(this.getStack());
               if (slotFeature.isActive(this.getStack(), level)) {
                  int index = 0;
                  if (slotFeature.slotType.equals("axe")) {
                     index = ++axeSlots;
                  } else if (slotFeature.slotType.equals("pommel")) {
                     index = 1;
                  }

                  if (!updatedChildSlots.contains(index) && this.updateChildSlot(index, slotFeature.slotType)) {
                     updatedChildSlots.add(index);
                  }
               }
            }
         }
      }

      for (int indexx = 0; indexx < this.childSlots.size(); indexx++) {
         if (!updatedChildSlots.contains(indexx)) {
            this.updateChildSlot(indexx, "none");
         }
      }
   }
}
