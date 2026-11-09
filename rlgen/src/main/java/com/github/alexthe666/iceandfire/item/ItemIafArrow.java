package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Ice and Fire arrows: 1.12 ItemStymphalianArrow / ItemAmphithereArrow / ItemSeaSerpentArrow (ItemArrow, so any bow shoots them;
 * they are in #minecraft:arrows) and the dragonbone_arrow (an ItemGeneric in 1.12 that only the dragonbone bow and dispensers fire; not
 * in #minecraft:arrows). The arrow entity comes from the mob branch through a ModEntityHooks.ArrowFactory; until it is set a vanilla
 * arrow is fired.
 */
public class ItemIafArrow extends ArrowItem {
   private final Supplier<ModEntityHooks.ArrowFactory> factory;
   @Nullable
   private final String desc;

   public ItemIafArrow(String name, Supplier<ModEntityHooks.ArrowFactory> factory, @Nullable String desc) {
      super(new Properties());
      this.factory = factory;
      this.desc = desc;
      IafNames.item(this, name);
   }

   @Override
   public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
      ModEntityHooks.ArrowFactory f = this.factory.get();
      return f != null ? f.create(level, shooter, shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(), ammo) : super.createArrow(level, ammo, shooter, weapon);
   }

   @Override
   public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
      ModEntityHooks.ArrowFactory f = this.factory.get();
      if (f == null) {
         return super.asProjectile(level, pos, stack, direction);
      }
      AbstractArrow arrow = f.create(level, null, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
      arrow.pickup = AbstractArrow.Pickup.ALLOWED;
      return arrow;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      if (this.desc != null) {
         tooltip.add(Component.translatable(this.desc));
      }
   }
}
