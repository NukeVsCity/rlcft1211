package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.BaseScreen;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import java.net.URI;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * 1.12 BeastiaryScreen. 1.12 forced GUI scale 2 while a beastiary screen is open and put the old scale back when it
 * closed (GUI_ACTIVE / OPENED_GUI_SCALE), kept here.
 */
public abstract class BeastiaryScreen extends BaseScreen {
   static int OPENED_GUI_SCALE;
   static boolean GUI_ACTIVE;
   public Player player;
   public ExtendedPlayer playerExt;
   public LivingEntity creaturePreviewEntity;
   public float creaturePreviewTicks = 0.0F;
   public int centerX;
   public int centerY;
   public int windowWidth;
   public int windowHeight;
   public int halfX;
   public int halfY;
   public int windowX;
   public int windowY;
   public int colLeftX;
   public int colLeftY;
   public int colLeftWidth;
   public int colLeftHeight;
   public int colLeftCenterX;
   public int colLeftCenterY;
   public int colRightX;
   public int colRightY;
   public int colRightWidth;
   public int colRightHeight;
   public int colRightCenterX;
   public int colRightCenterY;

   public BeastiaryScreen(Player player) {
      this.player = player;
      this.playerExt = ExtendedPlayer.getForPlayer(player);
      int guiScale = this.mc.options.guiScale().get();
      if (guiScale == 2 && !GUI_ACTIVE) {
         GUI_ACTIVE = true;
      } else {
         OPENED_GUI_SCALE = guiScale;
         this.mc.options.guiScale().set(2);
         this.mc.resizeDisplay();
      }
   }

   @Override
   public void removed() {
      if (this.mc.options.guiScale().get() == 2 && !GUI_ACTIVE) {
         this.mc.options.guiScale().set(OPENED_GUI_SCALE);
         this.mc.resizeDisplay();
      }

      GUI_ACTIVE = false;
      super.removed();
   }

   public String getTitleText() {
      return "Beastiary";
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   public int getScaledX(float x) {
      float targetAspect = 0.5625F;
      float scaledHeight = (float)this.mc.getWindow().getGuiScaledHeight();
      float scaledWidth = (float)this.mc.getWindow().getGuiScaledWidth();
      float currentAspect = scaledHeight * x / (scaledWidth * x);
      if (currentAspect < targetAspect) {
         scaledWidth = scaledHeight + scaledHeight * targetAspect;
      } else if (currentAspect > targetAspect) {
         scaledHeight = scaledWidth + scaledWidth * targetAspect;
      }

      float guiWidth = scaledWidth * x;
      return Math.round(Math.max(x, guiWidth));
   }

   public int getScaledY(float y) {
      float baseHeight = (float)Math.round((float)this.getScaledX(y) * 0.5625F);
      return Math.round(baseHeight * y);
   }

   /** 1.12 initGui. */
   @Override
   protected void init() {
      super.init();
      this.zLevel = -1000.0F;
      this.windowWidth = this.getScaledX(0.95F);
      this.windowHeight = this.getScaledY(0.95F);
      this.halfX = this.windowWidth / 2;
      this.halfY = this.windowHeight / 2;
      this.windowX = this.width / 2 - this.windowWidth / 2;
      this.windowY = this.height / 2 - this.windowHeight / 2;
      this.centerX = this.windowX + this.windowWidth / 2;
      this.centerY = this.windowY + this.windowHeight / 2;
      this.colLeftX = this.windowX + this.getScaledX(0.041666668F);
      this.colLeftY = this.windowY + this.getScaledY(0.42592594F);
      this.colLeftWidth = this.getScaledX(0.16666667F);
      this.colLeftHeight = this.getScaledX(0.35185185F);
      this.colLeftCenterX = this.colLeftX + Math.round((float)(this.colLeftWidth / 2));
      this.colLeftCenterY = this.colLeftY + Math.round((float)(this.colLeftHeight / 2));
      this.colRightX = this.windowX + this.getScaledX(0.25F);
      this.colRightY = this.windowY + this.getScaledY(0.3888889F);
      this.colRightWidth = this.getScaledX(0.65625F);
      this.colRightHeight = this.getScaledX(0.37037036F);
      this.colRightCenterX = this.colRightX + Math.round((float)(this.colRightWidth / 2));
      this.colRightCenterY = this.colRightY + Math.round((float)(this.colRightHeight / 2));
      this.clearWidgets();
      this.buttonCount = 0;
      this.initControls();
   }

   protected int buttonCount;

   protected ButtonBase addMenuButton(int id, int x, int y, int w, int h, String text) {
      this.buttonCount++;
      return this.addButton(new ButtonBase(id, x, y, w, h, text));
   }

   protected void initControls() {
      int menuPadding = 6;
      int menuX = this.centerX - Math.round((float)this.windowWidth / 2.0F) + menuPadding;
      int menuY = this.windowY + menuPadding;
      int menuWidth = this.windowWidth - menuPadding * 2;
      int buttonCount = 5;
      int buttonPadding = 2;
      int buttonX = menuX + buttonPadding;
      int buttonWidth = Math.round((float)(menuWidth / buttonCount)) - buttonPadding * 2;
      int buttonWidthPadded = buttonWidth + buttonPadding * 2;
      int buttonHeight = 20;
      this.addMenuButton(GuiHandler.Beastiary.INDEX.id, buttonX + buttonWidthPadded * this.buttonCount, menuY, buttonWidth, buttonHeight,
         LanguageManager.translate("gui.beastiary.index.title"));
      this.addMenuButton(GuiHandler.Beastiary.CREATURES.id, buttonX + buttonWidthPadded * this.buttonCount, menuY, buttonWidth, buttonHeight,
         LanguageManager.translate("gui.beastiary.creatures"));
      this.addMenuButton(GuiHandler.Beastiary.PETS.id, buttonX + buttonWidthPadded * this.buttonCount, menuY, buttonWidth, buttonHeight,
         LanguageManager.translate("gui.beastiary.pets"));
      this.addMenuButton(GuiHandler.Beastiary.SUMMONING.id, buttonX + buttonWidthPadded * this.buttonCount, menuY, buttonWidth, buttonHeight,
         LanguageManager.translate("gui.beastiary.summoning"));
      this.addMenuButton(GuiHandler.Beastiary.ELEMENTS.id, buttonX + buttonWidthPadded * this.buttonCount, menuY, buttonWidth, buttonHeight,
         LanguageManager.translate("gui.beastiary.elements"));
   }

   @Override
   public void drawScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.drawBackground(mouseX, mouseY, partialTicks);
      this.drawForeground(mouseX, mouseY, partialTicks);
      this.updateControls(mouseX, mouseY, partialTicks);
      super.drawScreen(graphics, mouseX, mouseY, partialTicks);
   }

   public void drawBackground(int mouseX, int mouseY, float partialTicks) {
      this.drawTexture(AssetManager.getTexture("GUIBeastiaryBackground"), (float)this.windowX, (float)this.windowY, this.zLevel, 1.0F, 1.0F,
         (float)this.windowWidth, (float)this.windowHeight);
   }

   /** 1.12 drew the buttons here; the 1.21 widgets are drawn by Screen.render (drawScreen's super call). */
   protected void updateControls(int mouseX, int mouseY, float partialTicks) {
   }

   public void drawForeground(int mouseX, int mouseY, float partialTicks) {
      String title = "§l§n" + this.getTitleText();
      float width = (float)this.getFontRenderer().width(title);
      this.drawString(title, (float)(this.colRightCenterX - Math.round(width / 2.0F)), (float)this.colRightY, 16777215, true);
   }

   @Override
   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         if (guiButton.id == GuiHandler.Beastiary.INDEX.id) {
            IndexBeastiaryScreen.openToPlayer(this.player);
         }

         if (guiButton.id == GuiHandler.Beastiary.CREATURES.id) {
            CreaturesBeastiaryScreen.openToPlayer(this.player);
         }

         if (guiButton.id == GuiHandler.Beastiary.PETS.id) {
            PetsBeastiaryScreen.openToPlayer(this.player);
         }

         if (guiButton.id == GuiHandler.Beastiary.SUMMONING.id) {
            SummoningBeastiaryScreen.openToPlayer(this.player);
         }

         if (guiButton.id == GuiHandler.Beastiary.ELEMENTS.id) {
            ElementsBeastiaryScreen.openToPlayer(this.player);
         }
      }
   }

   /** 1.12 keyTyped: escape or the inventory key closes. */
   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE || this.mc.options.keyInventory.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   protected void openURI(URI uri) {
      try {
         Util.getPlatform().openUri(uri);
      } catch (Throwable e) {
         LycanitesMobs.logWarning("", "Unable to open link: " + uri.toString());
      }
   }

   public void drawLevel(CreatureInfo creatureInfo, ResourceLocation texture, int x, int y) {
      int level = creatureInfo.summonCost;
      if (level <= 10) {
         this.drawBar(texture, x, y, 0.0F, 9.0F, 9.0F, level, 10);
      }
   }

   /** 1.12 renderCreature: GuiInventory.drawEntityOnScreen style preview at size 70, looking at the mouse. */
   public void renderCreature(CreatureInfo creatureInfo, int x, int y, int mouseX, int mouseY, float partialTicks) {
      if (creatureInfo == null) {
         this.creaturePreviewEntity = null;
      } else {
         try {
            boolean subspeciesMatch = true;
            if (this.creaturePreviewEntity instanceof BaseCreatureEntity previewCreature) {
               subspeciesMatch = previewCreature.getSubspeciesIndex() == this.getDisplaySubspecies(creatureInfo);
               if (subspeciesMatch) {
                  subspeciesMatch = previewCreature.getVariantIndex() == this.getDisplayVariant(creatureInfo);
               }
            }

            if (this.creaturePreviewEntity == null || this.creaturePreviewEntity.getClass() != creatureInfo.entityClass || !subspeciesMatch) {
               this.creaturePreviewEntity = creatureInfo.createEntity(this.player.level());
               this.creaturePreviewEntity.setOnGround(true);
               if (this.creaturePreviewEntity instanceof BaseCreatureEntity previewCreature) {
                  previewCreature.setSubspecies(this.getDisplaySubspecies(creatureInfo));
                  previewCreature.setVariant(this.getDisplayVariant(creatureInfo));
                  previewCreature.updateSize();
               }

               if (this.creaturePreviewEntity instanceof AgeableCreatureEntity ageable) {
                  ageable.setGrowingAge(0);
               }

               this.onCreateDisplayEntity(creatureInfo, this.creaturePreviewEntity);
               this.playCreatureSelectSound(creatureInfo);
            }

            if (this.creaturePreviewEntity != null) {
               int creatureSize = 70;
               float creatureWidth = this.creaturePreviewEntity.getBbWidth();
               float creatureHeight = this.creaturePreviewEntity.getBbHeight();
               int scale = Math.round(1.8F / Math.max(creatureWidth, creatureHeight) * (float)creatureSize);
               int posY = y + 32 + creatureSize;
               float lookX = (float)x - (float)mouseX;
               float lookY = (float)posY - (float)mouseY;
               this.creaturePreviewTicks += partialTicks;
               if (this.creaturePreviewEntity instanceof BaseCreatureEntity previewCreatureBase) {
                  previewCreatureBase.onlyRenderTicks = this.creaturePreviewTicks;
               }

               LivingEntity e = this.creaturePreviewEntity;
               float f = e.yBodyRot;
               float f1 = e.getYRot();
               float f2 = e.getXRot();
               float f3 = e.yHeadRotO;
               float f4 = e.yHeadRot;
               float angleX = (float)Math.atan((double)(lookX / 40.0F));
               float angleY = (float)Math.atan((double)(lookY / 40.0F));
               Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
               Quaternionf pitch = new Quaternionf().rotateX(angleY * 20.0F * (float)(Math.PI / 180.0));
               rotation.mul(pitch);
               e.yBodyRot = angleX * 20.0F;
               e.setYRot(angleX * 40.0F);
               e.setXRot(-angleY * 20.0F);
               e.yHeadRot = e.getYRot();
               e.yHeadRotO = e.getYRot();
               InventoryScreen.renderEntityInInventory(com.lycanitesmobs.client.gui.DrawHelper.graphics, (float)x, (float)posY, (float)scale,
                  new Vector3f(), rotation, pitch, e);
               e.yBodyRot = f;
               e.setYRot(f1);
               e.setXRot(f2);
               e.yHeadRotO = f3;
               e.yHeadRot = f4;
            }
         } catch (Exception e) {
            LycanitesMobs.logWarning("", "An exception occurred when trying to preview a creature in the Beastiary. " + e);
         }
      }
   }

   public int getDisplaySubspecies(CreatureInfo creatureInfo) {
      return this.playerExt.selectedSubspecies;
   }

   public int getDisplayVariant(CreatureInfo creatureInfo) {
      return this.playerExt.selectedVariant;
   }

   public void playCreatureSelectSound(CreatureInfo creatureInfo) {
      String soundSuffix = "";
      if (creatureInfo.getSubspecies(this.playerExt.selectedSubspecies).name != null) {
         soundSuffix = soundSuffix + "." + creatureInfo.getSubspecies(this.playerExt.selectedSubspecies).name;
      }

      SoundEvent soundEvent = AssetManager.getSound(creatureInfo.getName() + soundSuffix + "_say");
      if (soundEvent != null) {
         this.player.level().playSound(this.player, this.player.getX(), this.player.getY(), this.player.getZ(), soundEvent, SoundSource.NEUTRAL, 1.0F, 1.0F);
      }
   }

   public void onCreateDisplayEntity(CreatureInfo creatureInfo, LivingEntity entity) {
   }
}
