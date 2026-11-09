package net.ilexiconn.llibrary.client.util;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * LLibrary 1.7.20 ItemTESRContext. In 1.12 the LLibrary core mod filled it before TileEntityItemStackRenderer ran; in 1.21 a
 * BlockEntityWithoutLevelRenderer.renderByItem gets the stack directly, so call {@link #provideStackContext} /
 * {@link #providePerspectiveContext} there before reusing 1.12 code that reads it.
 */
@OnlyIn(Dist.CLIENT)
public enum ItemTESRContext {
   INSTANCE;

   @Nonnull
   private ItemStack currentStack = ItemStack.EMPTY;
   @Nonnull
   private ItemDisplayContext currentTransform = ItemDisplayContext.GROUND;

   public void provideStackContext(@Nonnull ItemStack stack) {
      this.currentStack = stack;
   }

   public void providePerspectiveContext(@Nonnull ItemDisplayContext transform) {
      this.currentTransform = transform;
   }

   @Nonnull
   public ItemStack getCurrentStack() {
      return this.currentStack;
   }

   @Nonnull
   public ItemDisplayContext getCurrentTransform() {
      return this.currentTransform;
   }
}
