package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.enums.EnumBestiaryPages;
import com.github.alexthe666.iceandfire.inventory.ContainerLectern;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 1.12 GuiLectern: furnace-like screen, "lectern.nopages" when the bestiary has every page. */
public class GuiLectern extends AbstractContainerScreen<ContainerLectern> {
   private static final ResourceLocation furnaceGuiTextures = ResourceLocation.parse("iceandfire:textures/gui/lectern.png");

   public GuiLectern(ContainerLectern menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title);
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      g.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
      g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
      if (this.hasAllPages()) {
         Component s1 = Component.translatable("lectern.nopages");
         g.drawString(this.font, s1, this.imageWidth / 2 - this.font.width("No new information can be added.") / 2, 20, 4210752, false);
      }
   }

   public boolean hasAllPages() {
      ItemStack book = this.menu.getSlot(0).getItem();
      if (!book.isEmpty() && book.getItem() == ModItems.bestiary) {
         List<EnumBestiaryPages> list = EnumBestiaryPages.possiblePages(book);
         return list == null || list.isEmpty();
      }
      return false;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      g.blit(furnaceGuiTextures, k, l, 0, 0, this.imageWidth, this.imageHeight);
      int i1 = this.getCookProgressScaled(25);
      g.blit(furnaceGuiTextures, k + 76, l + 36, 176, 0, i1, 16);
   }

   private int getCookProgressScaled(int pixels) {
      int j = this.menu.fields.get(2);
      int k = this.menu.fields.get(3);
      return k != 0 && j != 0 ? j * pixels / k : 0;
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      super.render(g, mouseX, mouseY, partialTicks);
      this.renderTooltip(g, mouseX, mouseY);
   }
}
