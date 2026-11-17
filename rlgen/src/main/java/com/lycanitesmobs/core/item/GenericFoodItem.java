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
import com.lycanitesmobs.core.info.EffectEntry;
import com.lycanitesmobs.core.info.ItemProperties;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;


public class GenericFoodItem extends Item {
   public String itemName;
   public ModInfo group = LycanitesMobs.modInfo;
   public String modelName;
   public ItemProperties properties;

   public GenericFoodItem(ItemProperties properties, String itemName) {
      // 1.12 ItemFood(hunger, saturation modifier, isWolfsFavoriteMeat): 1.21 food component with the same nutrition and saturation formula
      super(foodProperties(properties));
      this.itemName = itemName;
      this.properties = properties;
      this.setup();
   }

   private static Item.Properties foodProperties(ItemProperties properties) {
      net.minecraft.world.food.FoodProperties.Builder food = new net.minecraft.world.food.FoodProperties.Builder()
         .nutrition(properties.food.hunger).saturationModifier(properties.food.saturation);
      if (properties.food.alwaysEdible) {
         food.alwaysEdible();
      }
      return new Item.Properties().food(food.build()).stacksTo(Math.max(1, Math.min(99, properties.maxStackSize)));
   }

   public void setup() {
      LycReg.setName(this, this.group.modid, this.itemName);
      CreativeTabs.add(this.properties.creativeTab, this);
   }

   public void addInformation(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      String description = this.getDescription(stack, worldIn, tooltip, flagIn);
      if (!"".equalsIgnoreCase(description)) {
         // 1.12 wrapped the text to DESCRIPTION_WIDTH with the client font
         tooltip.add("§a" + description);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
      List<String> tooltip = new java.util.ArrayList<>();
      this.addInformation(stack, null, tooltip, flag);
      for (String line : tooltip) {
         tooltipComponents.add(Component.literal(line));
      }
   }

   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate("item." + this.itemName + ".description");
   }

   /** 1.12 ItemFood.onFoodEaten (players only) */
   @Override
   public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity entity) {
      ItemStack result = super.finishUsingItem(itemStack, world, entity);
      if (!world.isClientSide && entity instanceof Player player) {
         this.onFoodEaten(itemStack, world, player);
      }
      return result;
   }

   protected void onFoodEaten(ItemStack itemStack, Level world, Player player) {

      for (EffectEntry effectEntry : this.properties.food.effects) {
         MobEffectInstance potionEffect = effectEntry.createEffectInstance(world);
         if (potionEffect != null) {
            player.addEffect(potionEffect);
         }
      }
   }
}
