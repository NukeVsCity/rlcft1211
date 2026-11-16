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
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import java.util.ArrayList;
import java.util.List;


public class ChargeItem extends ItemBase {
   public static int CHARGE_EXPERIENCE = 50;
   public ProjectileInfo projectileInfo;

   public ChargeItem(ProjectileInfo projectileInfo) {
      this.projectileInfo = projectileInfo;
      this.modInfo = LycanitesMobs.modInfo;
      if (this.projectileInfo != null) {
         this.itemName = projectileInfo.chargeItemName;
         LycanitesMobs.logDebug("Projectile", "Created Charge Item: " + projectileInfo.chargeItemName);
         this.setup();
      }
   }

   @Override
   public void setup() {
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      this.setTranslationKey(this.itemName);
      CreativeTabs.add(LycanitesMobs.chargesTab, this);
   }

   @Override
   public String getItemStackDisplayName(ItemStack stack) {
      return this.getProjectileName() + " " + LanguageManager.translate("item.lycanitesmobs.charge");
   }

      public void addInformation(ItemStack itemStack, Level world, List<String> tooltip, TooltipFlag tooltipFlag) {
      super.addInformation(itemStack, world, tooltip, tooltipFlag);
      FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

      for (String description : this.getAdditionalDescriptions(itemStack, world, tooltipFlag)) {
         for (Object formattedDescription : fontRenderer.listFormattedStringToWidth("-------------------\n" + description, DESCRIPTION_WIDTH)) {
            if (formattedDescription instanceof String) {
               tooltip.add("§3" + formattedDescription);
            }
         }
      }
   }

   @Override
   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate("item.lycanitesmobs.charge.description");
   }

   public List<String> getAdditionalDescriptions(ItemStack itemStack, Level world, TooltipFlag tooltipFlag) {
      List<String> descriptions = new ArrayList<>();
      if (!this.getElements().isEmpty()) {
         String elements = LanguageManager.translate("item.lycanitesmobs.charge.elements") + " " + this.getElementNames();
         descriptions.add(elements);
      }

      String projectile = LanguageManager.translate("item.lycanitesmobs.charge.projectile") + " " + this.getProjectileName();
      descriptions.add(projectile);
      return descriptions;
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!world.isClientSide && player.isShiftKeyDown()) {
         BaseProjectileEntity projectile = this.createProjectile(itemStack, world, player);
         if (projectile == null) {
            LycanitesMobs.logWarning("", "Failed to create projectile from Charge Item: " + this.itemName);
            return new InteractionResultHolder(InteractionResult.FAIL, itemStack);
         }

         world.addFreshEntity(projectile);
         if (!player.getAbilities().instabuild) {
            itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
         }

         this.playSound(
            world, player.blockPosition(), projectile.getLaunchSound(), SoundSource.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)
         );
      }

      return new InteractionResultHolder(InteractionResult.SUCCESS, itemStack);
   }

   @Override
   public boolean onItemRightClickOnEntity(Player player, Entity entity, ItemStack itemStack) {
      return !(entity instanceof TameableCreatureEntity) || ((TameableCreatureEntity)entity).getPlayerOwner() != player;
   }

   public BaseProjectileEntity createProjectile(ItemStack itemStack, Level world, Player entityPlayer) {
      return this.projectileInfo == null ? null : this.projectileInfo.createProjectile(world, entityPlayer);
   }

   public List<ElementInfo> getElements() {
      return (List<ElementInfo>)(this.projectileInfo == null ? new ArrayList<>() : this.projectileInfo.elements);
   }

   public String getElementNames() {
      String elementNames = "";
      boolean firstElement = true;

      for (ElementInfo element : this.getElements()) {
         if (!firstElement) {
            elementNames = elementNames + ", ";
         }

         firstElement = false;
         elementNames = elementNames + element.getTitle();
      }

      return elementNames;
   }

   public String getProjectileName() {
      return this.projectileInfo != null ? this.projectileInfo.getTitle() : LanguageManager.translate("item.lycanitesmobs.charge");
   }
}
