package com.lycanitesmobs.core.item.consumable;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.ItemBase;


public class ItemHalloweenTreat extends ItemBase {
   public ItemHalloweenTreat() {
      this.modInfo = LycanitesMobs.modInfo;
      this.itemName = "halloweentreat";
      this.setup();
      AssetManager.addSound(this.itemName + "_good", this.modInfo, "item." + this.itemName + ".good");
      AssetManager.addSound(this.itemName + "_bad", this.modInfo, "item." + this.itemName + ".bad");
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!player.getAbilities().instabuild) {
         itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
      }

      if (!world.isClientSide) {
         if (player.getRandom().nextBoolean()) {
            this.openGood(itemStack, world, player);
         } else {
            this.openBad(itemStack, world, player);
         }
      }

      return new InteractionResultHolder(InteractionResult.SUCCESS, itemStack);
   }

   public void openGood(ItemStack itemStack, Level world, Player player) {
      String message = LanguageManager.translate("item." + this.itemName + ".good");
      player.sendSystemMessage(Component.literal(message));
      this.playLocalSound(
         world,
         player.getX(),
         player.getY(),
         player.getZ(),
         AssetManager.getSound(this.itemName + "_good"),
         SoundSource.AMBIENT,
         5.0F,
         1.0F
      );
      ItemStack[] dropStacks = ObjectLists.getItems("halloween_treats");
      if (dropStacks.length > 0) {
         ItemStack dropStack = dropStacks[player.getRandom().nextInt(dropStacks.length)];
         if (dropStack != null) {
            dropStack.setCount(1 + player.getRandom().nextInt(4));
            EntityItemCustom entityItem = new EntityItemCustom(world, player.getX(), player.getY(), player.getZ(), dropStack);
            entityItem.setPickUpDelay(10);
            world.addFreshEntity(entityItem);
         }
      }
   }

   public void openBad(ItemStack itemStack, Level world, Player player) {
      String message = LanguageManager.translate("item." + this.itemName + ".bad");
      player.sendSystemMessage(Component.literal(message));
      this.playLocalSound(
         world,
         player.getX(),
         player.getY(),
         player.getZ(),
         AssetManager.getSound(this.itemName + "_bad"),
         SoundSource.AMBIENT,
         5.0F,
         1.0F
      );
      Class[] entityClasses = ObjectLists.getEntites("halloween_tricks");
      if (entityClasses != null) {
         if (entityClasses.length > 0) {
            Class entityClass = entityClasses[player.getRandom().nextInt(entityClasses.length)];
            if (entityClass != null) {
               Entity entity = null;

               try {
                  entity = (Entity)entityClass.getConstructor(Level.class).newInstance(world);
               } catch (Exception var9) {
                  var9.printStackTrace();
               }

               if (entity != null) {
                  entity.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                  if (entity instanceof BaseCreatureEntity) {
                     BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
                     entityCreature.addLevel(world.random.nextInt(10));
                     if (entityCreature.creatureInfo.getName().equals("ent")) {
                        entityCreature.setCustomName(Component.literal("Twisted Ent"));
                     } else if (entityCreature.creatureInfo.getName().equals("treant")) {
                        entityCreature.setCustomName(Component.literal("Wicked Treant"));
                     } else if (entityCreature.creatureInfo.getName().equals("epion")) {
                        entityCreature.setCustomName(Component.literal("Vampire Bat"));
                     } else if (entityCreature.creatureInfo.getName().equals("grue")) {
                        entityCreature.setCustomName(Component.literal("Shadow Clown"));
                     }
                  }

                  world.addFreshEntity(entity);
               }
            }
         }
      }
   }

   public static void createObjectLists() {
      ObjectLists.addItem("halloween_treats", Items.DIAMOND);
      ObjectLists.addItem("halloween_treats", Items.GOLD_INGOT);
      ObjectLists.addItem("halloween_treats", Items.EMERALD);
      ObjectLists.addItem("halloween_treats", Blocks.IRON_BLOCK);
      ObjectLists.addItem("halloween_treats", Items.ENDER_PEARL);
      ObjectLists.addItem("halloween_treats", Items.BLAZE_ROD);
      ObjectLists.addItem("halloween_treats", Items.GLOWSTONE_DUST);
      ObjectLists.addItem("halloween_treats", ObjectManager.getItem("mosspie"));
      ObjectLists.addItem("halloween_treats", ObjectManager.getItem("bulwarkburger"));
      ObjectLists.addItem("halloween_treats", ObjectManager.getItem("paleosalad"));
      ObjectLists.addItem("halloween_treats", ObjectManager.getItem("searingtaco"));
      ObjectLists.addItem("halloween_treats", ObjectManager.getItem("devillasagna"));
      ObjectLists.addFromConfig("halloween_treats");
      ObjectLists.addEntity("halloween_tricks", "behemoth");
      ObjectLists.addEntity("halloween_tricks", "ent");
      ObjectLists.addEntity("halloween_tricks", "treant");
      ObjectLists.addEntity("halloween_tricks", "wraith");
      ObjectLists.addEntity("halloween_tricks", "grue");
      ObjectLists.addEntity("halloween_tricks", "reaper");
      ObjectLists.addEntity("halloween_tricks", "epion");
      ObjectLists.addEntity("halloween_tricks", "tpumpkyn");
   }
}
