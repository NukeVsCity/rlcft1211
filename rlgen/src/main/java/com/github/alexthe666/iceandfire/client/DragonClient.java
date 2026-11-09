package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.gui.GuiDragon;
import com.github.alexthe666.iceandfire.client.model.IFChainBuffer;
import com.github.alexthe666.iceandfire.client.model.animator.FireDragonTabulaModelAnimator;
import com.github.alexthe666.iceandfire.client.model.animator.IceDragonTabulaModelAnimator;
import com.github.alexthe666.iceandfire.client.model.util.EnumDragonAnimations;
import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import com.github.alexthe666.iceandfire.client.particle.DragonParticles;
import com.github.alexthe666.iceandfire.client.particle.ParticleDragonFire;
import com.github.alexthe666.iceandfire.client.particle.ParticleDragonIce;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDragonBase;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDragonEgg;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDragonFireCharge;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDragonSkull;
import com.github.alexthe666.iceandfire.client.render.tile.RenderEggInIce;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonEntities;
import com.github.alexthe666.iceandfire.entity.ReversedBuffer;
import java.io.IOException;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;
import net.ilexiconn.llibrary.client.model.tools.ChainBuffer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** The dragon part of 1.12 ClientProxy (renderEntities, spawnParticle, GuiHandler) and the client half of the entity's buffers. */
@EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class DragonClient {
   private static IceAndFireTabulaModel firedragon_model;
   private static IceAndFireTabulaModel icedragon_model;

   private DragonClient() {
   }

   /** ClientProxy.renderEntities: EnumDragonAnimations.initializeDragonModels(), then the two ground-pose models with their animators. */
   private static synchronized void loadModels() {
      if (firedragon_model == null) {
         EnumDragonAnimations.initializeDragonModels();
         try {
            firedragon_model = new IceAndFireTabulaModel(
               TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/firedragon/dragonFireGround"), new FireDragonTabulaModelAnimator());
            icedragon_model = new IceAndFireTabulaModel(
               TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/icedragon/dragonIceGround"), new IceDragonTabulaModelAnimator());
         } catch (IOException e) {
            throw new IllegalStateException("Ice and Fire dragon models", e);
         }
      }
   }

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      loadModels();
      event.registerEntityRenderer(IafDragonEntities.FIRE_DRAGON.get(), ctx -> new RenderDragonBase(ctx, firedragon_model, true));
      event.registerEntityRenderer(IafDragonEntities.ICE_DRAGON.get(), ctx -> new RenderDragonBase(ctx, icedragon_model, false));
      event.registerEntityRenderer(IafDragonEntities.DRAGON_EGG.get(), RenderDragonEgg::new);
      event.registerEntityRenderer(IafDragonEntities.DRAGON_SKULL.get(), RenderDragonSkull::new);
      event.registerEntityRenderer(IafDragonEntities.DRAGON_FIRE.get(), NoopRenderer::new);
      event.registerEntityRenderer(IafDragonEntities.DRAGON_FIRE_CHARGE.get(), ctx -> new RenderDragonFireCharge<>(ctx, true));
      event.registerEntityRenderer(IafDragonEntities.DRAGON_ICE.get(), NoopRenderer::new);
      event.registerEntityRenderer(IafDragonEntities.DRAGON_ICE_CHARGE.get(), ctx -> new RenderDragonFireCharge<>(ctx, false));
      event.registerBlockEntityRenderer(ModBlocks.EGG_IN_ICE_TILE.get(), RenderEggInIce::new);
   }

   @SubscribeEvent
   public static void registerParticles(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(DragonParticles.DRAGON_FIRE.get(), ParticleDragonFire.Provider::new);
      event.registerSpriteSet(DragonParticles.DRAGON_ICE.get(), ParticleDragonIce.Provider::new);
   }

   @SubscribeEvent
   public static void registerScreens(RegisterMenuScreensEvent event) {
      event.register(IafDragonEntities.DRAGON_MENU.get(), GuiDragon::new);
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      com.github.alexthe666.iceandfire.entity.DragonParticles.spawner = DragonParticles::spawn;
      EntityDragonBase.clientBuffers = DragonClient::updateBuffers;
   }

   /**
    * 1.12 EntityDragonBase: the buffers are made in the constructor on the client, and every client tick of a living model
    * roll_buffer.calculateChainFlapBuffer(50, 10, 4), turn_buffer.calculateChainSwingBuffer(50, 0, 4),
    * tail_buffer.calculateChainSwingBuffer(90, 10, 2.5).
    */
   private static void updateBuffers(EntityDragonBase dragon) {
      if (dragon.roll_buffer == null) {
         dragon.roll_buffer = new IFChainBuffer();
         dragon.turn_buffer = new ReversedBuffer();
         dragon.tail_buffer = new ChainBuffer();
      }
      ((IFChainBuffer)dragon.roll_buffer).calculateChainFlapBuffer(50.0F, 10, 4.0F, dragon);
      ((ReversedBuffer)dragon.turn_buffer).calculateChainSwingBuffer(50.0F, 0, 4.0F, dragon);
      ((ChainBuffer)dragon.tail_buffer).calculateChainSwingBuffer(90.0F, 10, 2.5F, dragon);
   }
}
