package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.IDragonFlute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** 1.12 ItemDragonFlute: 60 tick cooldown; every IDragonFlute within dragonFluteDistance chunks (256 up/down) hears it, nearest first. */
public class ItemDragonFlute extends Item {
   public ItemDragonFlute() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.dragon_flute");
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
      ItemStack itemStackIn = player.getItemInHand(hand);
      player.getCooldowns().addCooldown(this, 60);
      float chunksize = (float)(16 * IceAndFire.CONFIG.dragonFluteDistance);
      List<Entity> list = worldIn.getEntities(player,
         new AABB(player.getX(), player.getY(), player.getZ(), player.getX() + 1.0, player.getY() + 1.0, player.getZ() + 1.0)
            .inflate((double)chunksize, 256.0, (double)chunksize));
      Collections.sort(list, new Sorter(player));
      List<IDragonFlute> dragons = new ArrayList<>();
      for (Entity entity : list) {
         if (entity instanceof IDragonFlute flute) {
            dragons.add(flute);
         }
      }
      for (IDragonFlute dragon : dragons) {
         dragon.onHearFlute(player);
      }
      worldIn.playSound(player, player.blockPosition(), ModSounds.DRAGONFLUTE, SoundSource.NEUTRAL, 1.0F, 1.75F);
      return InteractionResultHolder.success(itemStackIn);
   }

   public static class Sorter implements Comparator<Entity> {
      private final Entity theEntity;

      public Sorter(Entity theEntityIn) {
         this.theEntity = theEntityIn;
      }

      @Override
      public int compare(Entity a, Entity b) {
         double d0 = this.theEntity.distanceToSqr(a);
         double d1 = this.theEntity.distanceToSqr(b);
         return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
      }
   }
}
