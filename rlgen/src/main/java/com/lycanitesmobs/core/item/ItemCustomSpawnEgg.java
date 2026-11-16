package com.lycanitesmobs.core.item;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.dispenser.DispenserBehaviorMobEggCustom;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.CreatureType;
import java.util.List;
import java.util.UUID;


public class ItemCustomSpawnEgg extends ItemBase {
   public CreatureType creatureType;

   public ItemCustomSpawnEgg(String name, CreatureType creatureType) {
      this.setTranslationKey(name);
      this.setHasSubtypes(true);
      CreativeTabs.add(LycanitesMobs.creaturesTab, this);
      this.itemName = name;
      this.creatureType = creatureType;
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new DispenserBehaviorMobEggCustom());
      LycanitesMobs.logDebug("Creature Type", "Created Creature Type Spawn Egg: " + this.itemName);
   }

   @Override
   public String getItemStackDisplayName(ItemStack itemStack) {
      String displayName = LanguageManager.translate("creaturetype.spawn") + " " + this.creatureType.getTitle() + ": ";
      String creatureName = this.getCreatureName(itemStack);
      if (creatureName != null) {
         CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName);
         String creatureTitle;
         if (creatureInfo != null) {
            creatureTitle = creatureInfo.getTitle();
         } else {
            creatureTitle = LanguageManager.translate("entity." + creatureName + ".name");
         }

         displayName = displayName + creatureTitle;
      }

      return displayName;
   }

   @Override
   public void addInformation(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      String description = this.getDescription(stack, worldIn, tooltip, flagIn);
      if (!"".equalsIgnoreCase(description) && !("item." + this.itemName + ".description").equals(description)) {
         FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

         for (Object formattedDescription : fontRenderer.listFormattedStringToWidth(description, ItemBase.DESCRIPTION_WIDTH)) {
            if (formattedDescription instanceof String) {
               tooltip.add("§a" + formattedDescription);
            }
         }
      }
   }

   @Override
   public String getDescription(ItemStack itemStack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
      if (creatureInfo == null) {
         String creatureName = this.getCreatureName(itemStack);
         LycanitesMobs.logWarning("Mob Spawn Egg", "Unable to get Creature Info for id: " + creatureName);
         return "Unable to get Creature Info for id: '" + creatureName + "' this spawn egg may have been created by a give command without NBT data.";
      } else {
         return creatureInfo.getDescription();
      }
   }

   @Override
   public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      ItemStack itemStack = player.getItemInHand(hand);
      BlockState blockState = world.getBlockState(pos);
      Block block = blockState.getBlock();
      if (world.isClientSide) {
         return InteractionResult.SUCCESS;
      } else if (!player.canPlayerEdit(pos.relative(facing), facing, itemStack)) {
         return InteractionResult.FAIL;
      } else if (block == Blocks.SPAWNER) {
         BlockEntity tileEntity = world.getBlockEntity(pos);
         MobSpawnerBaseLogic mobspawnerbaselogic = ((TileEntityMobSpawner)tileEntity).getSpawnerBaseLogic();
         CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
         if (creatureInfo == null) {
            return InteractionResult.FAIL;
         } else {
            mobspawnerbaselogic.setEntityId(creatureInfo.getResourceLocation());
            tileEntity.markDirty();
            world.notifyBlockUpdate(pos, blockState, blockState, 3);
            if (!player.getAbilities().instabuild) {
               itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         pos = pos.relative(facing);
         double d0 = 0.0;
         if (facing == Direction.UP && blockState.getBlock() instanceof BlockFence) {
            d0 = 0.5;
         }

         LivingEntity entity = this.spawnCreature(
            world, itemStack, (double)pos.getX() + 0.5, (double)pos.getY() + d0, (double)pos.getZ() + 0.5
         );
         if (entity != null) {
            if (itemStack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
               entity.setCustomName(Component.literal(itemStack.getHoverName()));
            }

            this.applyItemEntityDataToEntity(world, player, itemStack, entity);
            if (!player.getAbilities().instabuild) {
               itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (world.isClientSide) {
         return new InteractionResultHolder(InteractionResult.PASS, itemStack);
      } else {
         HitResult rayTraceResult = this.rayTrace(world, player, true);
         if (rayTraceResult == null) {
            return new InteractionResultHolder(InteractionResult.PASS, itemStack);
         } else {
            if (rayTraceResult.getType() == Type.BLOCK) {
               BlockPos pos = rayTraceResult.getBlockPos();
               if (!world.canMineBlockBody(player, pos)) {
                  return new InteractionResultHolder(InteractionResult.FAIL, itemStack);
               }

               if (!player.canPlayerEdit(pos, rayTraceResult.sideHit, itemStack)) {
                  return new InteractionResultHolder(InteractionResult.PASS, itemStack);
               }

               if (Material.of(world.getBlockState(pos)) == Material.WATER) {
                  LivingEntity entity = this.spawnCreature(
                     world, itemStack, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ()
                  );
                  if (entity != null && itemStack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                     entity.setCustomName(Component.literal(itemStack.getHoverName()));
                  }

                  if (!player.getAbilities().instabuild) {
                     itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                  }
               }
            }

            return new InteractionResultHolder(InteractionResult.SUCCESS, itemStack);
         }
      }
   }

   @Override
   public boolean useItemColors() {
      return true;
   }

   @Override
   public int getColorFromItemstack(ItemStack itemStack, int tintIndex) {
      CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
      return creatureInfo != null ? (tintIndex == 0 ? creatureInfo.eggBackColor : creatureInfo.eggForeColor) : 16777215;
   }

   public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
      if (this.modInfo != null && this.creatureType != null && this.isInCreativeTab(tab)) {
         for (CreatureInfo creatureInfo : this.creatureType.creatures.values()) {
            ItemStack itemstack = new ItemStack(this, 1);
            this.applyCreatureInfoToItemStack(itemstack, creatureInfo);
            items.add(itemstack);
         }
      }
   }

   public CreatureInfo getCreatureInfo(ItemStack itemStack) {
      String creatureName = this.getCreatureName(itemStack);
      return CreatureManager.getInstance().getCreature(creatureName);
   }

   public String getCreatureName(ItemStack itemStack) {
      CompoundTag itemStackNBT = LycNBT.get(itemStack);
      if (itemStackNBT != null && itemStackNBT.contains("CreatureInfoSpawnEgg", 10)) {
         CompoundTag spawnEggNBT = itemStackNBT.getCompound("CreatureInfoSpawnEgg");
         return !spawnEggNBT.contains("creaturename", 8) ? null : spawnEggNBT.getString("creaturename");
      } else {
         return null;
      }
   }

   public LivingEntity spawnCreature(Level world, ItemStack itemStack, double x, double y, double z) {
      CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
      if (creatureInfo == null) {
         return null;
      } else {
         LivingEntity entity = creatureInfo.createEntity(world);
         if (entity != null) {
            entity.moveTo(x, y, z, Mth.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
            entity.yHeadRot = entity.getYRot();
            entity.yBodyRot = entity.getYRot();
            if (entity instanceof Mob) {
               Mob entityliving = (Mob)entity;
               entityliving.onInitialSpawn(world.getCurrentDifficultyAt(LycPos.of(entityliving)), null);
               entityliving.playLivingSound();
            }

            world.addFreshEntity(entity);
         }

         return entity;
      }
   }

   public void applyCreatureInfoToItemStack(ItemStack itemStack, CreatureInfo creatureInfo) {
      CompoundTag itemStackNBT = LycNBT.hasTag(itemStack) ? LycNBT.get(itemStack) : new CompoundTag();
      CompoundTag spawnEggNBT = new CompoundTag();
      spawnEggNBT.putString("creaturename", creatureInfo.getName());
      itemStackNBT.put("CreatureInfoSpawnEgg", spawnEggNBT);
      LycNBT.put(itemStack, itemStackNBT);
   }

   public void applyItemEntityDataToEntity(Level entityWorld, Player player, ItemStack stack, Entity targetEntity) {
      MinecraftServer minecraftserver = entityWorld.getServer();
      if (minecraftserver != null && targetEntity != null) {
         CompoundTag nbttagcompound = LycNBT.get(stack);
         if (nbttagcompound != null && nbttagcompound.contains("CreatureInfoSpawnEgg", 10)) {
            if (!entityWorld.isClientSide
               && targetEntity.ignoreItemEntityData()
               && (player == null || !minecraftserver.getPlayerList().canSendCommands(player.getGameProfile()))) {
               return;
            }

            CompoundTag entityNBT = new CompoundTag();
            targetEntity.writeToNBT(entityNBT);
            UUID uuid = targetEntity.getUUID();
            entityNBT.merge(nbttagcompound.getCompound("CreatureInfoSpawnEgg"));
            targetEntity.putUUID(uuid);
            targetEntity.readFromNBT(entityNBT);
         }
      }
   }
}
