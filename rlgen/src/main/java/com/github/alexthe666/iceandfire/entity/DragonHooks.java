package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.github.alexthe666.iceandfire.item.ItemDragonEgg;
import com.github.alexthe666.iceandfire.item.ItemDragonFlesh;
import com.github.alexthe666.iceandfire.item.ItemDragonScales;
import com.github.alexthe666.iceandfire.item.ItemDragonSkull;
import com.github.alexthe666.iceandfire.loot.CustomizeToDragon;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** The iaf/core item and block hooks into the dragon entities (ModEntityHooks), and the dragon half of CustomizeToDragon. */
public final class DragonHooks {
   private DragonHooks() {
   }

   public static void install() {
      // 1.12 ItemDragonEgg.onItemUse
      ModEntityHooks.spawnDragonEgg = (level, type, pos, player) -> {
         EntityDragonEgg egg = new EntityDragonEgg(IafDragonEntities.DRAGON_EGG.get(), level);
         egg.setEggType(type);
         egg.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
         egg.onPlayerPlace(player);
         if (!level.isClientSide) {
            level.addFreshEntity(egg);
         }
      };
      // 1.12 TileEntityEggInIce.spawnEgg
      ModEntityHooks.spawnEggFromIce = (level, type, pos, owner) -> {
         EntityDragonEgg egg = new EntityDragonEgg(IafDragonEntities.DRAGON_EGG.get(), level);
         egg.setEggType(type);
         egg.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
         egg.setOwnerId(owner);
         if (!level.isClientSide) {
            level.addFreshEntity(egg);
         }
      };
      // 1.12 TileEntityEggInIce.update hatch
      ModEntityHooks.hatchIceDragon = (level, type, pos, owner) -> {
         EntityIceDragon dragon = new EntityIceDragon(IafDragonEntities.ICE_DRAGON.get(), level);
         dragon.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
         dragon.setVariant(type.ordinal() - 4);
         dragon.setGender(new Random().nextBoolean());
         dragon.setTame(true, true);
         dragon.setHunger(50);
         dragon.setOwnerUUID(owner);
         if (!level.isClientSide) {
            level.addFreshEntity(dragon);
         }
      };
      // 1.12 ItemDragonSkull.onItemUse
      ModEntityHooks.spawnDragonSkull = (level, type, stage, dragonAge, x, y, z, yaw) -> {
         EntityDragonSkull skull = new EntityDragonSkull(IafDragonEntities.DRAGON_SKULL.get(), level);
         skull.setDragonType(type);
         skull.setStage(stage);
         skull.setDragonAge(dragonAge);
         skull.moveTo(x, y, z, 0.0F, 0.0F);
         skull.setDragonYaw(yaw);
         if (!level.isClientSide) {
            level.addFreshEntity(skull);
         }
      };
      // 1.12 ItemDragonHornActive.onPlayerStoppedUsing
      ModEntityHooks.releaseDragonFromHorn = (level, fire, tag, pos) -> {
         EntityDragonBase dragon = fire
            ? new EntityFireDragon(IafDragonEntities.FIRE_DRAGON.get(), level)
            : new EntityIceDragon(IafDragonEntities.ICE_DRAGON.get(), level);
         dragon.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
         if (tag != null) {
            dragon.readAdditionalSaveData(tag);
         }

         dragon.setFlying(false);
         dragon.setHovering(false);
         dragon.getNavigation().stop();
         if (!level.isClientSide) {
            level.addFreshEntity(dragon);
         }
      };
      CustomizeToDragon.DRAGON = DragonHooks::customizeToDragon;
   }

   /** 1.12 CustomizeToDragon.apply for a looted dragon. */
   static ItemStack customizeToDragon(ItemStack stack, Entity looted) {
      if (!stack.isEmpty() && looted instanceof EntityDragonBase dragon) {
         Random random = new Random();
         if (stack.is(ModItems.dragonbone)) {
            stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 25));
            return stack;
         } else if (stack.getItem() instanceof ItemDragonScales) {
            stack.setCount(dragon.getAgeInDays() / 25 + random.nextInt(1 + dragon.getAgeInDays() / 5));
            return new ItemStack(dragon.getVariantScale(dragon.getVariant()), stack.getCount());
         } else if (stack.getItem() instanceof ItemDragonEgg) {
            if (dragon.isAdult()) {
               return new ItemStack(dragon.getVariantEgg(dragon.getVariant()), stack.getCount());
            } else {
               stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 5));
               return new ItemStack(dragon.getVariantScale(dragon.getVariant()), stack.getCount());
            }
         } else if (stack.getItem() instanceof ItemDragonFlesh) {
            stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 25));
            return new ItemStack(dragon.isFire ? ModItems.fire_dragon_flesh : ModItems.ice_dragon_flesh, stack.getCount());
         } else if (stack.getItem() instanceof ItemDragonSkull) {
            return stack.copy();
         } else if (stack.is(ModItems.fire_dragon_blood) || stack.is(ModItems.ice_dragon_blood)) {
            return new ItemStack(dragon.isFire ? ModItems.fire_dragon_blood : ModItems.ice_dragon_blood, stack.getCount());
         } else {
            return !stack.is(ModItems.fire_dragon_heart) && !stack.is(ModItems.ice_dragon_heart)
               ? stack
               : new ItemStack(dragon.isFire ? ModItems.fire_dragon_heart : ModItems.ice_dragon_heart, stack.getCount());
         }
      }
      return stack;
   }
}
