package com.lycanitesmobs.core.item.special;

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
import com.lycanitesmobs.core.info.AltarInfo;
import com.lycanitesmobs.core.item.ItemBase;
import java.util.ArrayList;
import java.util.List;


public class ItemSoulkey extends ItemBase {
   public int variant = -1;

   public ItemSoulkey(String itemName, int variant) {
      this.itemName = itemName;
      this.variant = variant;
      this.setup();
   }

   @Override
   public void onUpdate(ItemStack itemStack, Level world, Entity entity, int par4, boolean par5) {
      super.onUpdate(itemStack, world, entity, par4, par5);
   }

   @Override
   public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!AltarInfo.checkAltarsEnabled() && !player.level().isClientSide) {
         String message = LanguageManager.translate("message.soulkey.disabled");
         player.sendSystemMessage(Component.literal(message));
         return InteractionResult.FAIL;
      } else {
         List<AltarInfo> possibleAltars = new ArrayList<>();
         if (AltarInfo.altars.isEmpty()) {
            LycanitesMobs.logWarning("", "No altars have been registered, Soulkeys will not work at all.");
         }

         for (AltarInfo altarInfo : AltarInfo.altars.values()) {
            if (altarInfo.checkBlockEvent(player, world, pos) && altarInfo.quickCheck(player, world, pos)) {
               possibleAltars.add(altarInfo);
            }
         }

         if (possibleAltars.isEmpty()) {
            String message = LanguageManager.translate("message.soulkey.none");
            player.sendSystemMessage(Component.literal(message));
            return InteractionResult.FAIL;
         } else {
            for (AltarInfo altarInfox : possibleAltars) {
               if (altarInfox.fullCheck(player, world, pos)) {
                  if (!player.level().isClientSide) {
                     if (!altarInfox.activate(player, world, pos, this.variant)) {
                        String message = LanguageManager.translate("message.soulkey.badlocation");
                        player.sendSystemMessage(Component.literal(message));
                        return InteractionResult.FAIL;
                     }

                     if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                     }

                     String message = LanguageManager.translate("message.soulkey.active");
                     player.sendSystemMessage(Component.literal(message));
                  }

                  return InteractionResult.SUCCESS;
               }
            }

            if (!player.getAbilities().instabuild) {
               String message = LanguageManager.translate("message.soulkey.invalid");
               player.sendSystemMessage(Component.literal(message));
            }

            return InteractionResult.FAIL;
         }
      }
   }
}
