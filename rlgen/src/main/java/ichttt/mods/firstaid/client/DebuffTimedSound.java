package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAidConfig;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** 1.12 DebuffTimedSound: the heartbeat, at the player, fading from 1.25 volume over the debuff (max 300 ticks). */
public class DebuffTimedSound extends AbstractTickableSoundInstance {
   private static final float volumeMultiplier = 1.25F;
   private static final Map<SoundEvent, DebuffTimedSound> activeSounds = new HashMap<>();
   private final float minusPerTick;
   private final int debuffDuration;
   private final SoundEvent event;
   private final WeakReference<LocalPlayer> player;
   private int ticks;

   public static void playHurtSound(SoundEvent event, int duration) {
      if (!FirstAidConfig.enableSoundSystem) {
         return;
      }
      var soundManager = Minecraft.getInstance().getSoundManager();
      DebuffTimedSound matchingSound = activeSounds.get(event);
      if (matchingSound != null) {
         if (!matchingSound.isStopped()) {
            soundManager.stop(matchingSound);
         }
         activeSounds.remove(event);
      }
      DebuffTimedSound newSound = new DebuffTimedSound(event, duration);
      soundManager.play(newSound);
      activeSounds.put(event, newSound);
   }

   public DebuffTimedSound(SoundEvent event, int debuffDuration) {
      super(event, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
      this.event = event;
      this.player = new WeakReference<>(Minecraft.getInstance().player);
      this.debuffDuration = Integer.min(300, debuffDuration);
      this.minusPerTick = 1.0F / (float)this.debuffDuration * volumeMultiplier;
      this.volume = volumeMultiplier;
      this.pitch = 1.0F;
      this.looping = false;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.relative = false;
   }

   @Override
   public void tick() {
      LocalPlayer p = this.player.get();
      if (p == null || this.ticks >= this.debuffDuration || p.getHealth() <= 0.0F) {
         activeSounds.remove(this.event);
         this.stop();
         return;
      }
      this.x = p.getX();
      this.y = p.getY();
      this.z = p.getZ();
      this.ticks++;
      this.volume = Math.max(0.15F, this.volume - this.minusPerTick);
   }
}
