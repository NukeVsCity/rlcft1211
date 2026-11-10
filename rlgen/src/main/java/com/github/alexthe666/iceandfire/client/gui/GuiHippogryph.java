package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.inventory.ContainerHippogryph;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

/** Ice and Fire 1.7.1 GuiHippogryph (also GuiHippocampus, which uses the same texture). */
public class GuiHippogryph extends AbstractContainerScreen<ContainerHippogryph> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/gui/hippogryph.png");
   private float mousePosx;
   private float mousePosY;

   public GuiHippogryph(ContainerHippogryph menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      drawBackground(g, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, this.menu.hippogryph.isChested(), this.menu.hippogryph,
         this.mousePosx, this.mousePosY);
   }

   static void drawBackground(GuiGraphics g, int i, int j, int xSize, int ySize, boolean chested, LivingEntity entity, float mx, float my) {
      g.blit(TEXTURE, i, j, 0, 0, xSize, ySize);
      if (chested) {
         g.blit(TEXTURE, i + 79, j + 17, 0, ySize, 90, 54);
      }
      InventoryScreen.renderEntityInInventoryFollowsMouse(g, i + 26, j + 18, i + 78, j + 70, 17, 0.0625F, mx, my, entity);
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      this.mousePosx = mouseX;
      this.mousePosY = mouseY;
      super.render(g, mouseX, mouseY, partialTicks);
      this.renderTooltip(g, mouseX, mouseY);
   }
}
