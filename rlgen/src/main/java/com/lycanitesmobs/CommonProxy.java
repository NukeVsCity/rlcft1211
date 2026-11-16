package com.lycanitesmobs;

import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.network.MessageContext;
import java.io.File;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 1.12 CommonProxy. The client side (lyc/client) replaces {@link LycanitesMobs#proxy} with a ClientProxy subclass
 * (com.lycanitesmobs.client.ClientProxy, created by reflection on the client so this class stays server safe).
 */
public class CommonProxy {
   public void registerEvents() {
   }

   /** 1.12 getMinecraftDir: config files are read from <game dir>/config/lycanitesmobs. */
   public File getMinecraftDir() {
      return FMLPaths.GAMEDIR.get().toFile();
   }

   public void initLanguageManager() {
   }

   public void registerRenders(ModInfo groupInfo) {
   }

   public void registerTextures() {
   }

   public void registerModels(ModInfo groupInfo) {
   }

   /** The client world (null on the server): lyc/client returns Minecraft's level. */
   public net.minecraft.world.level.Level getClientWorld() {
      return null;
   }

   public Player getClientPlayer() {
      return null;
   }

   public void addBlockRender(ModInfo group, Block block) {
   }

   public void addItemRender(ModInfo group, Item item) {
   }

   public void loadCreatureModel(CreatureInfo creature, String modelClassName) throws ClassNotFoundException {
   }

   public void loadSubspeciesModel(Subspecies subspecies, String modelClassName) throws ClassNotFoundException {
   }

   public Object getFontRenderer() {
      return null;
   }

   /** Client-only GUIs (beastiary, minion selection...) opened with GuiHandler.openGui on the client: lyc/client. */
   public void openClientGui(Player player, int id, net.minecraft.world.level.Level world, int x, int y, int z) {
   }

   public Player getPlayer(MessageContext ctx) {
      return ctx.player;
   }
}
