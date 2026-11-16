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
import com.lycanitesmobs.core.info.ItemConfig;
import com.lycanitesmobs.core.item.ChargeItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;


public class EquipmentInfuserContainer extends BaseContainer {
   public EquipmentInfuserTileEntity equipmentInfuser;
   EquipmentInfuserPartSlot partSlot;
   EquipmentInfuserChargeSlot chargeSlot;

   public EquipmentInfuserContainer(EquipmentInfuserTileEntity equipmentInfuser, Inventory playerInventory) {
      this.equipmentInfuser = equipmentInfuser;
      this.addPlayerSlots(playerInventory, 0, 0);
      this.inventoryStart = this.inventorySlots.size();
      int slots = 0;
      if (equipmentInfuser.getSizeInventory() > 0) {
         int y = 28;
         this.chargeSlot = new EquipmentInfuserChargeSlot(this, slots++, 50, y);
         this.addSlotToContainer(this.chargeSlot);
         this.partSlot = new EquipmentInfuserPartSlot(this, slots++, 110, y);
         this.addSlotToContainer(this.partSlot);
      }

      this.inventoryFinish = this.inventoryStart + slots;
   }

   @Override
   public boolean canInteractWith(Player player) {
      return this.equipmentInfuser != null && this.equipmentInfuser.isUsableByPlayer(player);
   }

   public void attemptInfusion() {
      if (this.partSlot.getStack().getItem() instanceof ItemEquipmentPart) {
         ItemEquipmentPart equipmentPart = (ItemEquipmentPart)this.partSlot.getStack().getItem();
         if (this.chargeSlot.getStack().getItem() instanceof ChargeItem) {
            if (equipmentPart.getLevel(this.partSlot.getStack()) >= equipmentPart.levelMax) {
               return;
            }

            ChargeItem chargeItem = (ChargeItem)this.chargeSlot.getStack().getItem();
            if (equipmentPart.isLevelingChargeItem(this.chargeSlot.getStack())) {
               int experienceGained = equipmentPart.getExperienceFromChargeItem(this.chargeSlot.getStack());
               equipmentPart.addExperience(this.partSlot.getStack(), experienceGained);
               this.chargeSlot.decrStackSize(1);
               this.attemptInfusion();
            }

            return;
         }

         if (this.chargeSlot.getStack().getItem() instanceof ItemDye) {
            int dyeColor = ItemDye.DYE_COLORS[this.chargeSlot.getStack().getMetadata()];
            int i = (dyeColor & 0xFF0000) >> 16;
            int j = (dyeColor & 0xFF00) >> 8;
            int k = (dyeColor & 0xFF) >> 0;
            equipmentPart.setColor(this.partSlot.getStack(), (float)i / 255.0F, (float)j / 255.0F, (float)k / 255.0F);
            this.chargeSlot.decrStackSize(1);
            return;
         }

         if (this.chargeSlot.getStack().getItem() == Items.WATER_BUCKET) {
            equipmentPart.setColor(this.partSlot.getStack(), 1.0F, 1.0F, 1.0F);
            this.chargeSlot.putStack(new ItemStack(Items.BUCKET));
            return;
         }
      }

      if (ItemConfig.chargesToXPBottles
         && this.partSlot.getStack().getItem() == Items.GLASS_BOTTLE
         && this.chargeSlot.getStack().getItem() instanceof ChargeItem) {
         this.partSlot.putStack(new ItemStack(Items.EXPERIENCE_BOTTLE));
         this.chargeSlot.decrStackSize(1);
      }
   }

   @Override
   public ItemStack transferStackInSlot(Player player, int slotID) {
      return ItemStack.EMPTY;
   }
}
