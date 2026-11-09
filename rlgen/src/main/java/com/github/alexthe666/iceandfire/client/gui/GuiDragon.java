package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.inventory.ContainerDragon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 1.12 GuiDragon: dragon inventory (armor slots) with the dragon drawn in the middle and its stats in white below it. */
public class GuiDragon extends AbstractContainerScreen<ContainerDragon> {
   private static final ResourceLocation texture = ResourceLocation.parse("iceandfire:textures/gui/dragon.png");
   private final EntityDragonBase dragon;
   private float mousePosx;
   private float mousePosY;

   public GuiDragon(ContainerDragon container, Inventory playerInv, Component title) {
      super(container, playerInv, title);
      this.dragon = container.getDragon();
      this.imageHeight = 214;
   }

   /** 1.12 drawEntityOnScreen, scaled by 1 / (getRenderSize() / 3) so every age fills the same box. */
   public static void drawEntityOnScreen(GuiGraphics graphics, int posX, int posY, int scale, float mouseX, float mouseY, EntityDragonBase entity) {
      float dragonscale = entity.getRenderSize() / 3.0F;
      float f2 = (float)Math.atan((double)(mouseX / 40.0F));
      float f3 = (float)Math.atan((double)(mouseY / 40.0F));
      // 1.12 used scale(-s, s, s) * rotZ(180) with renderYawOffset = atan * 20; the 1.21 GUI basis is scale(s, s, -s) * rotZ(180),
      // which is the same picture with the yaws offset by 180 (as InventoryScreen does)
      Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI);
      Quaternionf camera = new Quaternionf().rotateX(f3 * 20.0F * (float)(Math.PI / 180.0));
      pose.mul(camera);
      float yawOffset = entity.yBodyRot;
      float yaw = entity.getYRot();
      float pitch = entity.getXRot();
      float prevYawHead = entity.yHeadRotO;
      float yawHead = entity.yHeadRot;
      entity.yBodyRot = 180.0F + f2 * 20.0F;
      entity.setYRot(180.0F + f2 * 40.0F);
      entity.setXRot(-f3 * 20.0F);
      entity.yHeadRot = entity.getYRot();
      entity.yHeadRotO = entity.getYRot();
      InventoryScreen.renderEntityInInventory(graphics, (float)posX, (float)posY, (float)scale / dragonscale, new Vector3f(), pose, camera, entity);
      entity.yBodyRot = yawOffset;
      entity.setYRot(yaw);
      entity.setXRot(pitch);
      entity.yHeadRotO = prevYawHead;
      entity.yHeadRot = yawHead;
   }

   private void centered(GuiGraphics graphics, String s, int y, int color) {
      graphics.drawString(this.font, s, this.imageWidth / 2 - this.font.width(s) / 2, y, color, false);
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      // drawGuiContainerForegroundLayer (no "Inventory" label in 1.12)
      String s1 = I18n.get(this.dragon.isFire ? "entity.firedragon.name" : "entity.icedragon.name");
      this.centered(graphics, s1, 6, 4210752);
      String name = this.dragon.hasCustomName() ? this.dragon.getCustomName().getString() : "";
      String s3 = name.length() == 0 ? I18n.get("dragon.unnamed") : I18n.get("dragon.name") + name;
      this.centered(graphics, s3, 75, 16777215);
      String s2 = I18n.get("dragon.health") + Math.min(this.dragon.getHealth(), this.dragon.getMaxHealth()) + "/" + this.dragon.getMaxHealth();
      this.centered(graphics, s2, 84, 16777215);
      String s5 = I18n.get("dragon.gender") + I18n.get(this.dragon.isMale() ? "dragon.gender.male" : "dragon.gender.female");
      this.centered(graphics, s5, 93, 16777215);
      String s6 = I18n.get("dragon.hunger") + this.dragon.getHunger() + "/100";
      this.centered(graphics, s6, 102, 16777215);
      String s4 = I18n.get("dragon.stage") + this.dragon.getDragonStage() + " " + I18n.get("dragon.days.front") + this.dragon.getAgeInDays() + " "
         + I18n.get("dragon.days.back");
      this.centered(graphics, s4, 111, 16777215);
      String s7 = this.dragon.getOwner() != null ? I18n.get("dragon.owner") + this.dragon.getOwner().getName().getString() : I18n.get("dragon.untamed");
      this.centered(graphics, s7, 120, 16777215);
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      graphics.blit(texture, k, l, 0, 0, this.imageWidth, this.imageHeight);
      drawEntityOnScreen(graphics, k + 88, l + (int)(0.5F * this.dragon.flyProgress) + 55, 23, (float)(k + 51) - this.mousePosx,
         (float)(l + 75 - 50) - this.mousePosY, this.dragon);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.mousePosx = (float)mouseX;
      this.mousePosY = (float)mouseY;
      super.render(graphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(graphics, mouseX, mouseY);
   }
}
