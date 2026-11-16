package com.lycanitesmobs.core.mobevent;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;


public class MobEventPlayerClient {
   public MobEvent mobEvent;
   public int ticks = 0;
   public Level world;
   public Object sound; // 1.12 PositionedSoundRecord, made by the client proxy
   public boolean extended = false;

   public MobEventPlayerClient(MobEvent mobEvent, Level world) {
      this.mobEvent = mobEvent;
      this.world = world;
      if (!world.isClientSide) {
         LycanitesMobs.logWarning("", "Created a MobEventClient with a server side world, this shouldn't happen, things are going to get weird!");
      }
   }

   public void onStart(Player player) {
      if (!this.extended) {
         this.ticks = 0;
      }

      String eventMessage = LanguageManager.translate("event." + (this.extended ? "extended" : "started"));
      eventMessage = eventMessage.replace("%event%", this.mobEvent.getTitle());
      player.sendSystemMessage(Component.literal(eventMessage));
      if (!player.getAbilities().instabuild || MobEventPlayerServer.testOnCreative || !"world".equalsIgnoreCase(this.mobEvent.channel)) {
         this.playSound();
      }
   }

   public void playSound() {
      if (AssetManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()) == null) {
         LycanitesMobs.logWarning("MobEvent", "Sound missing for: " + this.mobEvent.getTitle());
      } else {
         // 1.12 PositionedSoundRecord(sound, RECORDS, 1, 1, no repeat, 0, AttenuationType.NONE, 0, 0, 0) on the sound handler
         this.sound = LycanitesMobs.proxy.playMobEventSound(AssetManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()));
      }
   }

   public void onFinish(Player player) {
      String eventMessage = LanguageManager.translate("event.finished");
      eventMessage = eventMessage.replace("%event%", this.mobEvent.getTitle());
      player.sendSystemMessage(Component.literal(eventMessage));
   }

   public void onUpdate() {
      this.ticks++;
   }

   public void onGUIUpdate(Object gui, int sWidth, int sHeight) {
      Player player = LycanitesMobs.proxy.getClientPlayer();
      if (!player.getAbilities().instabuild || MobEventPlayerServer.testOnCreative || !"world".equalsIgnoreCase(this.mobEvent.channel)) {
         if (this.world != null && this.world == player.level()) {
            if (this.world.isClientSide) {
               int introTime = 240;
               if (this.ticks <= introTime) {
                  int startTime = 40;
                  int stopTime = 80;
                  float animation = 1.0F;
                  if (this.ticks < startTime) {
                     animation = (float)this.ticks / (float)startTime;
                  } else if (this.ticks > introTime - stopTime) {
                     animation = (float)(introTime - this.ticks) / (float)stopTime;
                  }

                  int width = 256;
                  int height = 256;
                  int x = sWidth / 2 - width / 2;
                  int y = sHeight / 2 - height / 2;
                  x += 3 - this.ticks % 6;
                  y += 2 - this.ticks % 4;
                  // 1.12 bindTexture + glColor4f(1, 1, 1, animation) + drawTexturedModalRect: drawn by the client overlay
                  if (animation > 0.0F) {
                     LycanitesMobs.proxy.drawMobEventIntro(gui, this.getTexture(), x, y, width, height, animation);
                  }
               }
            }
         }
      }
   }

   public ResourceLocation getTexture() {
      if (AssetManager.getTexture("guimobevent" + this.mobEvent.title) == null) {
         AssetManager.addTexture("guimobevent" + this.mobEvent.title, LycanitesMobs.modInfo, "textures/mobevents/" + this.mobEvent.title.toLowerCase() + ".png");
      }

      return AssetManager.getTexture("guimobevent" + this.mobEvent.title);
   }
}
