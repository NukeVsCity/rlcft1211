package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.model.IFChainBuffer;
import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.SirenEntityProperties;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.github.alexthe666.iceandfire.message.AirWaterNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Client side of the Ice and Fire 1.7.1 air and water mobs (ClientProxy parts, updateClientControls, message handlers). */
public final class AirWaterClient {
   private AirWaterClient() {
   }

   public static void register(IEventBus modBus) {
      AirWaterRenderers.register(modBus);
      NeoForge.EVENT_BUS.addListener(AirWaterClient::onPlayerTick);
   }

   public static Object newChainBuffer() {
      return new IFChainBuffer();
   }

   public static void flapBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      ((IFChainBuffer)buffer).calculateChainFlapBuffer(maxAngle, bufferTime, angleDecrement, entity);
   }

   public static void flapBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      ((IFChainBuffer)buffer).calculateChainFlapBuffer(maxAngle, bufferTime, angleDecrement, divisor, entity);
   }

   public static void waveBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      ((IFChainBuffer)buffer).calculateChainWaveBuffer(maxAngle, bufferTime, angleDecrement, divisor, entity);
   }

   public static void swingBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      ((IFChainBuffer)buffer).calculateChainSwingBuffer(maxAngle, bufferTime, angleDecrement, entity);
   }

   public static void waveBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      ((IFChainBuffer)buffer).calculateChainWaveBuffer(maxAngle, bufferTime, angleDecrement, entity);
   }

   private static KeyMapping key(String name) {
      for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
         if (k.getName().equals(name)) {
            return k;
         }
      }
      return null;
   }

   /** ModKeys.dragon_down (X) / dragon_strike (G) of the dragon port, or those default keys if it is not installed. */
   static boolean dragonDown() {
      KeyMapping k = key("key.dragon_down");
      return k != null ? k.isDown() : InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_X);
   }

   static boolean dragonStrike() {
      KeyMapping k = key("key.dragon_strike");
      return k != null ? k.isDown() : InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_G);
   }

   private static void send(Entity e, byte state) {
      PacketDistributor.sendToServer(new AirWaterNetwork.MobControl(e.getId(), state, e.getX(), e.getY(), e.getZ()));
   }

   /** EntityHippogryph / EntityHippocampus / EntityAmphithere.updateClientControls */
   public static void updateControls(Entity entity) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }
      if (entity instanceof EntityAmphithere a) {
         amphithereControls(a, mc);
      }
      if (entity instanceof EntityHippocampus hc) {
         hippocampusControls(hc, mc);
      }
      if (entity instanceof EntityHippogryph h) {
         if (h.isRidingPlayer(mc.player)) {
            byte previousState = h.getControlState();
            h.up(mc.options.keyJump.isDown());
            h.down(dragonDown());
            h.attack(dragonStrike());
            h.dismount(mc.options.keyShift.isDown());
            byte controlState = h.getControlState();
            if (controlState != previousState) {
               send(h, controlState);
            }
         }
         if (h.getVehicle() != null && h.getVehicle() == mc.player) {
            byte previousState = h.getControlState();
            h.dismount(mc.options.keyShift.isDown());
            byte controlState = h.getControlState();
            if (controlState != previousState) {
               send(h, controlState);
            }
         }
      }
   }

   public static void spawnParticle(String name, net.minecraft.world.level.Level level, double x, double y, double z, double vx, double vy, double vz) {
   }

   /** MessageSirenSong.onClientReceived */
   private static void amphithereControls(EntityAmphithere h, Minecraft mc) {
      if (h.isRidingPlayer(mc.player)) {
         byte previousState = h.getControlState();
         h.up(mc.options.keyJump.isDown());
         h.down(dragonDown());
         h.attack(dragonStrike());
         h.dismount(mc.options.keyShift.isDown());
         if (h.getControlState() != previousState) {
            send(h, h.getControlState());
         }
      }
      if (h.getVehicle() != null && h.getVehicle() == mc.player) {
         byte previousState = h.getControlState();
         h.dismount(mc.options.keyShift.isDown());
         if (h.getControlState() != previousState) {
            send(h, h.getControlState());
         }
      }
   }

   private static void hippocampusControls(EntityHippocampus h, Minecraft mc) {
      if (h.isRidingPlayer(mc.player)) {
         byte previousState = h.getControlState();
         h.up(mc.options.keyJump.isDown());
         h.dismount(mc.options.keyShift.isDown());
         h.down(dragonDown());
         if (h.getControlState() != previousState) {
            send(h, h.getControlState());
         }
      }
      if (h.getVehicle() != null && h.getVehicle() == mc.player) {
         byte previousState = h.getControlState();
         h.dismount(mc.options.keyShift.isDown());
         h.down(dragonDown());
         if (h.getControlState() != previousState) {
            send(h, h.getControlState());
         }
      }
   }

   public static void onSirenSong(int sirenId, boolean singing) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.getEntity(sirenId) instanceof EntitySiren siren) {
         siren.setSinging(singing);
      }
   }

   private static final ResourceLocation SIREN_SHADER = ResourceLocation.fromNamespaceAndPath("iceandfire", "shaders/post/siren.json");
   private static final java.util.Random rand = new java.util.Random();

   /** EventClient.onLivingUpdate (siren part): the pink screen and siren appearance particles while a siren charms the player. */
   private static void onPlayerTick(EntityTickEvent.Pre event) {
      if (!(event.getEntity() instanceof Player player) || !player.level().isClientSide || player != Minecraft.getInstance().player) {
         return;
      }
      SirenEntityProperties sirenProps = SirenEntityProperties.get(player);
      if (sirenProps == null) {
         return;
      }
      GameRenderer renderer = Minecraft.getInstance().gameRenderer;
      EntitySiren siren = (EntitySiren)sirenProps.getSiren(player.level());
      if (siren == null) {
         sirenProps.isCharmed = false;
      }
      if (sirenProps.isCharmed) {
         if (rand.nextInt(40) == 0) {
            spawnParticle("siren_appearance", player.level(), player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
         }
         if (com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenShader && renderer.currentEffect() == null) {
            renderer.loadEffect(SIREN_SHADER);
         }
      }
      if (com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenShader && !sirenProps.isCharmed && renderer.currentEffect() != null
         && SIREN_SHADER.toString().equals(renderer.currentEffect().getName())) {
         renderer.shutdownEffect();
      }
   }
}
