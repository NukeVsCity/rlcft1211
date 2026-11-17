package com.lycanitesmobs.client;

import com.lycanitesmobs.CommonProxy;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.Subspecies;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 1.12 ClientProxy (created by LycanitesMobs on the client). */
public class ClientProxy extends CommonProxy {
   @Override
   public void initLanguageManager() {
   }

   @Override
   public Object getFontRenderer() {
      return Minecraft.getInstance().font;
   }

   @Override
   public File getMinecraftDir() {
      return Minecraft.getInstance().gameDirectory;
   }

   @Override
   public void registerEvents() {
      ClientEvents.register();
   }

   @Override
   public void registerTextures() {
      ModInfo group = LycanitesMobs.modInfo;
      AssetManager.addTexture("GUIBeastiaryBackground", group, "textures/guis/beastiary/background.png");
      AssetManager.addTexture("GUIPetLevel", group, "textures/guis/beastiary/level.png");
      AssetManager.addTexture("GUIPetSpirit", group, "textures/guis/beastiary/spirit.png");
      AssetManager.addTexture("GUIPetSpiritEmpty", group, "textures/guis/beastiary/spirit_empty.png");
      AssetManager.addTexture("GUIPetSpiritUsed", group, "textures/guis/beastiary/spirit_used.png");
      AssetManager.addTexture("GUIPetSpiritFilling", group, "textures/guis/beastiary/spirit_filling.png");
      AssetManager.addTexture("GUIPetBarHealth", group, "textures/guis/beastiary/bar_health.png");
      AssetManager.addTexture("GUIPetBarRespawn", group, "textures/guis/beastiary/bar_respawn.png");
      AssetManager.addTexture("GUIBarExperience", group, "textures/guis/beastiary/bar_experience.png");
      AssetManager.addTexture("GUIPetBarEmpty", group, "textures/guis/beastiary/bar_empty.png");
      AssetManager.addTexture("GUIInventoryCreature", group, "textures/guis/inventory_creature.png");
      AssetManager.addTexture("GUIMinionLg", group, "textures/guis/minion_lg.png");
      AssetManager.addTexture("GUIEquipmentForge", group, "textures/guis/equipmentforge.png");
      ClientEvents.registerTabs();
   }

   /** 1.12 registerRenders: the entity renderers are bound in RenderRegister (EntityRenderersEvent). */
   @Override
   public void registerRenders(ModInfo modInfo) {
   }

   @Override
   public void registerModels(ModInfo groupInfo) {
   }

   @Override
   public void loadCreatureModel(CreatureInfo creature, String modelClassName) throws ClassNotFoundException {
      creature.modelClass = Class.forName(modelClassName);
   }

   @Override
   public void loadSubspeciesModel(Subspecies subspecies, String modelClassName) throws ClassNotFoundException {
      subspecies.modelClass = Class.forName(modelClassName);
   }

   @Override
   public Level getClientWorld() {
      return Minecraft.getInstance().level;
   }

   @Override
   public Player getClientPlayer() {
      return Minecraft.getInstance().player;
   }

   @Override
   public java.util.List<String> wrapText(String text, int width) {
      java.util.List<String> lines = new java.util.ArrayList<>();
      Minecraft.getInstance().font.getSplitter().splitLines(text, width, net.minecraft.network.chat.Style.EMPTY)
         .forEach(line -> lines.add(line.getString()));
      return lines;
   }

   /** 1.12 MobEventPlayerClient.playSound: PositionedSoundRecord(sound, RECORDS, 1, 1, no repeat, 0, NONE, 0, 0, 0). */
   @Override
   public Object playMobEventSound(net.minecraft.sounds.SoundEvent sound) {
      net.minecraft.client.resources.sounds.SimpleSoundInstance instance = new net.minecraft.client.resources.sounds.SimpleSoundInstance(
         sound.getLocation(), net.minecraft.sounds.SoundSource.RECORDS, 1.0F, 1.0F, net.minecraft.client.resources.sounds.SoundInstance.createUnseededRandom(),
         false, 0, net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, false);
      Minecraft.getInstance().getSoundManager().play(instance);
      return instance;
   }

   @Override
   public void drawMobEventIntro(Object gui, net.minecraft.resources.ResourceLocation texture, int x, int y, int width, int height, float alpha) {
      if (gui instanceof com.lycanitesmobs.client.gui.overlays.BaseOverlay overlay) {
         overlay.drawMobEventIntro(texture, x, y, width, height, alpha);
      }
   }

   /** 1.12 GuiHandler.getClientGuiElement for the client-only GUIs (beastiary pages, minion selection). */
   @Override
   public void openClientGui(Player player, int id, Level world, int x, int y, int z) {
      Minecraft.getInstance().setScreen(com.lycanitesmobs.client.gui.ClientScreens.clientGuiElement(id, player, x));
   }

   @Override
   public void openClientScreen(String screen, Player player) {
      com.lycanitesmobs.client.gui.ClientScreens.open(screen, player);
   }
}
