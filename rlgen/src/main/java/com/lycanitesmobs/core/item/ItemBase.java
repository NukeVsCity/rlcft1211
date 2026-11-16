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
import com.google.common.collect.Multimap;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;


@Interface(
   iface = "thaumcraft.api.research.IScanThing",
   modid = "thaumcraft",
   striprefs = true
)
public class ItemBase extends Item {
   public static int DESCRIPTION_WIDTH = 200;
   public String itemName = "unamed_item";
   public ModInfo modInfo;

   public ItemBase() {
      this.modInfo = LycanitesMobs.modInfo;
      this.setMaxStackSize(64);
   }

   public void setup() {
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      this.setTranslationKey(this.itemName);
      CreativeTabs.add(LycanitesMobs.itemsTab, this);
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return LanguageManager.translate(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
   }

   public void addInformation(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      String description = this.getDescription(stack, worldIn, tooltip, flagIn);
      if (!"".equalsIgnoreCase(description)) {
         FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

         for (Object formattedDescription : fontRenderer.listFormattedStringToWidth(description, DESCRIPTION_WIDTH)) {
            if (formattedDescription instanceof String) {
               tooltip.add("§a" + formattedDescription);
            }
         }
      }

      super.addInformation(stack, worldIn, tooltip, flagIn);
   }

   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate(this.getDescriptionId() + ".description");
   }

   public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      return super.getAttributeModifiers(slot, stack);
   }

   public CompoundTag getTagCompound(ItemStack itemStack) {
      return LycNBT.hasTag(itemStack) ? LycNBT.get(itemStack) : new CompoundTag();
   }

   public void onUpdate(ItemStack itemStack, Level world, Entity entity, int par4, boolean par5) {
      super.onUpdate(itemStack, world, entity, par4, par5);
   }

   public InteractionResult onItemUse(
      Player playerIn, Level worldIn, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ
   ) {
      return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
   }

   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }

   public void onItemLeftClick(ItemStack itemStackIn, Level worldIn, Player playerIn, InteractionHand hand) {
   }

   public void onUsingTick(ItemStack itemStack, LivingEntity entity, int useRemaining) {
      super.onUsingTick(itemStack, entity, useRemaining);
   }

   public void onPlayerStoppedUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
      super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
   }

   public UseAnim getItemUseAction(ItemStack itemStack) {
      return super.getItemUseAction(itemStack);
   }

   public boolean onItemRightClickOnEntity(Player player, Entity entity, ItemStack itemStack) {
      return false;
   }

   public int getItemEnchantability() {
      return 0;
   }

   public boolean getIsRepairable(ItemStack itemStack, ItemStack repairStack) {
      return super.getIsRepairable(itemStack, repairStack);
   }

   public void playSound(Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
      world.playLocalSound(null, x, y, z, sound, category, volume, pitch);
   }

   public void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
      world.playSound(null, pos, sound, category, volume, pitch);
   }

   public boolean isFull3D() {
      return super.isFull3D();
   }

   public ModelResourceLocation getModelResourceLocation() {
      return new ModelResourceLocation(LycReg.key(this), "inventory");
   }

   public boolean useItemColors() {
      return false;
   }

   public int getColorFromItemstack(ItemStack itemStack, int tintIndex) {
      return 16777215;
   }

   @Method(
      modid = "thaumcraft"
   )
   public boolean checkThing(Player player, Object obj) {
      return false;
   }

   @Method(
      modid = "thaumcraft"
   )
   public String getResearchKey(Player player, Object object) {
      return null;
   }
}
