package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

/** The client side of the 1.12 First Aid messages. */
public final class ClientHandlers {
   private ClientHandlers() {
   }

   private static LocalPlayer player() {
      return Minecraft.getInstance().player;
   }

   /** MessageUpdatePart */
   public static void updatePart(FirstAidNetwork.UpdatePart m) {
      if (player() == null) {
         return;
      }
      DamageablePart part = FirstAid.getDamageModel(player()).getFromEnum(EnumPlayerPart.fromID(m.id()));
      part.setMaxHealth(m.maxHealth());
      part.setAbsorption(m.absorption());
      part.currentHealth = m.currentHealth();
   }

   /** MessageSyncDamageModel */
   public static void sync(FirstAidNetwork.SyncDamageModel m) {
      if (player() == null) {
         return;
      }
      PlayerDamageModel model = FirstAid.getDamageModel(player());
      if (m.scaleMaxHealth()) {
         model.runScaleLogic(player());
      }
      model.deserializeNBT(player().registryAccess(), m.model());
   }

   /** MessageConfiguration */
   public static void configuration(FirstAidNetwork.Configuration m) {
      if (player() == null) {
         return;
      }
      PlayerDamageModel model = FirstAid.getDamageModel(player());
      model.deserializeNBT(player().registryAccess(), m.model());
      if (!model.hasTutorial) {
         player().sendSystemMessage(Component.literal("[First Aid] ").append(Component.translatable("firstaid.tutorial.hint",
            FirstAidClient.SHOW_WOUNDS.getTranslatedKeyMessage())));
      }
      HUDHandler.INSTANCE.ticker = 200;
      FirstAid.isSynced = true;
   }

   /** MessageApplyAbsorption */
   public static void absorption(FirstAidNetwork.ApplyAbsorption m) {
      if (player() != null) {
         FirstAid.getDamageModel(player()).setAbsorption(m.amount());
      }
   }

   /** MessageAddHealth */
   public static void addHealth(FirstAidNetwork.AddHealth m) {
      if (player() == null) {
         return;
      }
      PlayerDamageModel model = FirstAid.getDamageModel(player());
      for (int i = 0; i < m.table().length; i++) {
         model.getFromEnum(EnumPlayerPart.fromID(i + 1)).heal(m.table()[i], player(), false);
      }
   }

   /** MessagePlayHurtSound */
   public static void hurtSound(FirstAidNetwork.PlayHurtSound m) {
      SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(m.sound());
      if (sound != null) {
         DebuffTimedSound.playHurtSound(sound, m.duration());
      }
   }
}
