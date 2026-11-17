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
import net.minecraft.world.inventory.Slot;
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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;


public class EquipmentForgeContainer extends BaseContainer {
   public TileEntityEquipmentForge equipmentForge;

   public EquipmentForgeContainer(TileEntityEquipmentForge equipmentForge, Inventory playerInventory) {
      this.equipmentForge = equipmentForge;
      this.addPlayerSlots(playerInventory, 0, 0);
      this.inventoryStart = this.inventorySlots.size();
      int slots = 0;
      if (equipmentForge.getSizeInventory() > 0) {
         int slotSize = 18;
         int x = 8 + slotSize;
         int y = 38;
         EquipmentForgeSlot slotEquipmentPiece = new EquipmentForgeSlot(this, slots++, x + slotSize * 6, y, "piece");
         this.addSlotToContainer(slotEquipmentPiece);
         EquipmentForgeSlot slotEquipmentBase = new EquipmentForgeSlot(this, slots++, x + slotSize, y, "base");
         this.addSlotToContainer(slotEquipmentBase);
         EquipmentForgeSlot slotEquipmentHead = new EquipmentForgeSlot(this, slots++, x + slotSize * 2, y, "none");
         this.addSlotToContainer(slotEquipmentHead);
         slotEquipmentBase.addChildSlot(slotEquipmentHead);
         EquipmentForgeSlot slotEquipmentTipA = new EquipmentForgeSlot(this, slots++, x + slotSize * 3, y, "none");
         this.addSlotToContainer(slotEquipmentTipA);
         slotEquipmentHead.addChildSlot(slotEquipmentTipA);
         EquipmentForgeSlot slotEquipmentTipB = new EquipmentForgeSlot(this, slots++, x + slotSize * 2, y - slotSize, "none");
         this.addSlotToContainer(slotEquipmentTipB);
         slotEquipmentHead.addChildSlot(slotEquipmentTipB);
         EquipmentForgeSlot slotEquipmentTipC = new EquipmentForgeSlot(this, slots++, x + slotSize * 2, y + slotSize, "none");
         this.addSlotToContainer(slotEquipmentTipC);
         slotEquipmentHead.addChildSlot(slotEquipmentTipC);
         EquipmentForgeSlot slotEquipmentPommel = new EquipmentForgeSlot(this, slots++, x, y, "none");
         this.addSlotToContainer(slotEquipmentPommel);
         slotEquipmentBase.addChildSlot(slotEquipmentPommel);
      }

      this.inventoryFinish = this.inventoryStart + slots;
   }

   @Override
   public boolean canInteractWith(Player player) {
      return this.equipmentForge != null && this.equipmentForge.isUsableByPlayer(player);
   }

   public void onEquipmentPieceSlotChanged(EquipmentForgeSlot slotEquipment) {
      if (this.equipmentForge != null) {
         this.clearPartSlots();
         EquipmentForgeSlot slotBase = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 1);
         if (slotEquipment.hasItem() && !slotBase.hasItem() && slotEquipment.getItem().getItem() instanceof ItemEquipment) {
            EquipmentForgeSlot slotHead = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 2);
            EquipmentForgeSlot slotTipA = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 3);
            EquipmentForgeSlot slotTipB = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 4);
            EquipmentForgeSlot slotTipC = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 5);
            EquipmentForgeSlot slotPommel = (EquipmentForgeSlot)this.getSlot(this.inventoryStart + 6);
            ItemEquipment itemEquipment = (ItemEquipment)slotEquipment.getItem().getItem();
            int axeIndex = 0;

            for (ItemStack partStack : itemEquipment.getEquipmentPartStacks(slotEquipment.getItem())) {
               ItemEquipmentPart itemEquipmentPart = itemEquipment.getEquipmentPart(partStack);
               if (itemEquipmentPart != null) {
                  if ("base".equals(itemEquipmentPart.slotType)) {
                     slotBase.putStackWithoutUpdate(partStack);
                  } else if ("head".equals(itemEquipmentPart.slotType)) {
                     slotHead.putStackWithoutUpdate(partStack);
                  } else if ("blade".equals(itemEquipmentPart.slotType)
                     || "pike".equals(itemEquipmentPart.slotType)
                     || "jewel".equals(itemEquipmentPart.slotType)) {
                     slotTipA.putStackWithoutUpdate(partStack);
                  } else if ("axe".equals(itemEquipmentPart.slotType)) {
                     if (axeIndex++ == 0) {
                        slotTipB.putStackWithoutUpdate(partStack);
                     } else {
                        slotTipC.putStackWithoutUpdate(partStack);
                     }
                  } else if ("pommel".equals(itemEquipmentPart.slotType)) {
                     slotPommel.putStackWithoutUpdate(partStack);
                  }
               }
            }
         }
      }
   }

   public void onEquipmentPartSlotChanged(EquipmentForgeSlot slotEquipment) {
      if (this.equipmentForge != null) {
         Slot slotPiece = this.getSlot(this.inventoryStart);
         Slot slotBase = this.getSlot(this.inventoryStart + 1);
         Slot slotHead = this.getSlot(this.inventoryStart + 2);
         Slot slotTipA = this.getSlot(this.inventoryStart + 3);
         Slot slotTipB = this.getSlot(this.inventoryStart + 4);
         Slot slotTipC = this.getSlot(this.inventoryStart + 5);
         Slot slotPommel = this.getSlot(this.inventoryStart + 6);
         ItemEquipment itemEquipment = (ItemEquipment)ObjectManager.getItem("equipment");
         ItemStack pieceStack = new ItemStack(itemEquipment);
         if (slotBase.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotBase.getItem(), 0);
         }

         if (slotHead.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotHead.getItem(), 1);
         }

         if (slotTipA.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipA.getItem(), 2);
         }

         if (slotTipB.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipB.getItem(), 3);
         }

         if (slotTipC.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotTipC.getItem(), 4);
         }

         if (slotPommel.hasItem()) {
            itemEquipment.addEquipmentPart(pieceStack, slotPommel.getItem(), 5);
         }

         if (itemEquipment.getEquipmentPartCount(pieceStack) > 0) {
            slotPiece.set(pieceStack);
         } else {
            slotPiece.set(ItemStack.EMPTY);
         }
      }
   }

   public void clearPartSlots() {
      for (int i = 1; i <= 6; i++) {
         Slot slot = this.getSlot(this.inventoryStart + i);
         if (slot instanceof EquipmentForgeSlot) {
            EquipmentForgeSlot slotEquipment = (EquipmentForgeSlot)slot;
            slotEquipment.putStackWithoutUpdate(ItemStack.EMPTY);
         }
      }
   }

   public boolean isEquipmentValid() {
      Slot slotBase = this.getSlot(this.inventoryStart + 1);
      Slot slotHead = this.getSlot(this.inventoryStart + 2);
      Slot slotTipA = this.getSlot(this.inventoryStart + 3);
      Slot slotTipB = this.getSlot(this.inventoryStart + 4);
      Slot slotTipC = this.getSlot(this.inventoryStart + 5);
      return slotBase.hasItem() && slotHead.hasItem() ? slotTipA.hasItem() || slotTipB.hasItem() || slotTipC.hasItem() : false;
   }

   @Override
   public ItemStack transferStackInSlot(Player player, int slotID) {
      return ItemStack.EMPTY;
   }
}
