package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.model.ModelCyclops;
import com.github.alexthe666.iceandfire.client.model.ModelDeathWorm;
import com.github.alexthe666.iceandfire.client.model.ModelDragonEgg;
import com.github.alexthe666.iceandfire.client.model.ModelGorgon;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexQueen;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexRoyal;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexSentinel;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexSoldier;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexWorker;
import com.github.alexthe666.iceandfire.client.model.ModelTroll;
import com.github.alexthe666.iceandfire.client.render.entity.RenderCockatrice;
import com.github.alexthe666.iceandfire.client.render.entity.RenderLandMob;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LandLayers;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerStoneEntity;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexEgg;
import com.github.alexthe666.iceandfire.entity.EntityStoneStatue;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.entity.IafLandMobs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 1.12 ClientProxy.render for the land mobs, and EventClient.initializeStoneLayer. */
@EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LandClient {
   private LandClient() {
   }

   private static ResourceLocation tex(String path) {
      return IafLand.rl("textures/models/" + path + ".png");
   }

   private static final ResourceLocation[][] CYCLOPS = new ResourceLocation[4][3];

   static {
      for (int i = 0; i < 4; i++) {
         CYCLOPS[i][0] = tex("cyclops/cyclops_" + i);
         CYCLOPS[i][1] = tex("cyclops/cyclops_" + i + "_blink");
         CYCLOPS[i][2] = tex("cyclops/cyclops_" + i + "_injured");
      }
   }

   private static ResourceLocation cyclopsTexture(EntityCyclops c) {
      ResourceLocation[] t = CYCLOPS[Mth.clamp(c.getVariant(), 0, 3)];
      return c.isBlinded() ? t[2] : (c.isBlinking() ? t[1] : t[0]);
   }

   private static final ResourceLocation GORGON_PASSIVE = tex("gorgon/gorgon_passive");
   private static final ResourceLocation GORGON_ACTIVE = tex("gorgon/gorgon_active");
   private static final ResourceLocation GORGON_DEAD = tex("gorgon/gorgon_decapitated");
   private static final ResourceLocation WORM_RED = tex("deathworm/deathworm_red");
   private static final ResourceLocation WORM_WHITE = tex("deathworm/deathworm_white");
   private static final ResourceLocation WORM_YELLOW = tex("deathworm/deathworm_yellow");

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(IafLandMobs.GORGON.get(), ctx -> {
         RenderLandMob<EntityGorgon> r = new RenderLandMob<>(ctx, new ModelGorgon(), 0.6F,
            g -> g.getAnimation() == EntityGorgon.ANIMATION_SCARE ? GORGON_ACTIVE : (g.deathTime > 0 ? GORGON_DEAD : GORGON_PASSIVE),
            (g, pose, pt) -> pose.scale(0.85F, 0.85F, 0.85F));
         r.addLayer(new LandLayers.GorgonEyes(r));
         return r;
      });
      event.registerEntityRenderer(IafLandMobs.CYCLOPS.get(), ctx -> new RenderLandMob<>(ctx, new ModelCyclops(), 1.6F, LandClient::cyclopsTexture,
         (c, pose, pt) -> pose.scale(2.25F, 2.25F, 2.25F)));
      event.registerEntityRenderer(IafLandMobs.TROLL.get(), ctx -> {
         RenderLandMob<EntityTroll> r = new RenderLandMob<>(ctx, new ModelTroll(), 0.9F, t -> t.getTrollType().TEXTURE, null);
         r.addLayer(new LandLayers.TrollWeapon(r));
         r.addLayer(new LandLayers.TrollEyes(r));
         r.addLayer(new LandLayers.TrollStone(r));
         r.addLayer(new LayerStoneEntity<>(r, true));
         return r;
      });
      event.registerEntityRenderer(IafLandMobs.COCKATRICE.get(), RenderCockatrice::new);
      event.registerEntityRenderer(IafLandMobs.DEATHWORM.get(), DeathWormRenderer::new);
      myrmex(event, IafLandMobs.MYRMEX_WORKER, ModelMyrmexWorker::new, 0.5F);
      myrmex(event, IafLandMobs.MYRMEX_SOLDIER, ModelMyrmexSoldier::new, 0.75F);
      myrmex(event, IafLandMobs.MYRMEX_SENTINEL, ModelMyrmexSentinel::new, 0.85F);
      myrmex(event, IafLandMobs.MYRMEX_ROYAL, ModelMyrmexRoyal::new, 0.75F);
      myrmex(event, IafLandMobs.MYRMEX_QUEEN, ModelMyrmexQueen::new, 1.25F);
      event.registerEntityRenderer(IafLandMobs.MYRMEX_EGG.get(), ctx -> new RenderLandMob<EntityMyrmexEgg>(ctx, new ModelDragonEgg(), 0.3F,
         e -> e.isJungle() ? tex("myrmex/myrmex_jungle_egg") : tex("myrmex/myrmex_desert_egg"), null));
      event.registerEntityRenderer(IafLandMobs.STONE_STATUE.get(), StoneStatueRenderer::new);
      event.registerEntityRenderer(IafLandMobs.COCKATRICE_EGG.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer(IafLandMobs.DEATHWORM_EGG.get(), ThrownItemRenderer::new);
   }

   private static <T extends EntityMyrmexBase> void myrmex(EntityRenderersEvent.RegisterRenderers event, Supplier<EntityType<T>> type, Supplier<ModelBase> model, float shadow) {
      event.registerEntityRenderer(type.get(), ctx -> {
         RenderLandMob<T> r = new RenderLandMob<T>(ctx, model.get(), shadow, EntityMyrmexBase::getTexture, (m, pose, pt) -> {
            float scale = m.getModelScale();
            if (m.getGrowthStage() == 0) {
               scale /= 2.0F;
            }
            if (m.getGrowthStage() == 1) {
               scale /= 1.5F;
            }
            pose.scale(scale, scale, scale);
            if (m.isPassenger() && m.getGrowthStage() < 2) {
               pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
         });
         @SuppressWarnings("unchecked")
         var parent = (net.minecraft.client.renderer.entity.RenderLayerParent<EntityMyrmexBase, EntityModel<EntityMyrmexBase>>)(Object)r;
         r.addLayer((net.minecraft.client.renderer.entity.layers.RenderLayer<T, EntityModel<T>>)(Object)new LandLayers.MyrmexItem(parent));
         return r;
      });
   }

   /** RenderDeathWorm: scaled by getScaleForAge, shadow a third of it, lit from the surface while under sand */
   static class DeathWormRenderer extends RenderLandMob<EntityDeathWorm> {
      DeathWormRenderer(EntityRendererProvider.Context ctx) {
         super(ctx, new ModelDeathWorm(), 0.0F, e -> e.getVariant() == 2 ? WORM_RED : (e.getVariant() == 1 ? WORM_WHITE : WORM_YELLOW), null);
      }

      @Override
      protected void scale(EntityDeathWorm entity, PoseStack pose, float pt) {
         this.shadowRadius = entity.getScaleForAge() / 3.0F;
         pose.scale(entity.getScaleForAge(), entity.getScaleForAge(), entity.getScaleForAge());
      }

      /** getBrightnessForRender */
      @Override
      protected int getBlockLightLevel(EntityDeathWorm worm, BlockPos pos) {
         return worm.level().getBrightness(LightLayer.BLOCK, this.lightPos(worm));
      }

      @Override
      protected int getSkyLightLevel(EntityDeathWorm worm, BlockPos pos) {
         return worm.level().getBrightness(LightLayer.SKY, this.lightPos(worm));
      }

      private BlockPos lightPos(EntityDeathWorm worm) {
         BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(Mth.floor(worm.getX()), Mth.floor(worm.getY() + (double)worm.getEyeHeight()), Mth.floor(worm.getZ()));
         if (IafLand.isSandMaterial(worm.level().getBlockState(p)) || worm.isInWall()) {
            p.setY(worm.level().getHeight(Heightmap.Types.MOTION_BLOCKING, p.getX(), p.getZ()));
         }
         return p;
      }
   }

   /** RenderStoneStatue: the player model (slim arms when smallArms) with the stone texture, plus the crack stage. */
   static class StoneStatueRenderer extends LivingEntityRenderer<EntityStoneStatue, PlayerModel<EntityStoneStatue>> {
      private final PlayerModel<EntityStoneStatue> wide;
      private final PlayerModel<EntityStoneStatue> slim;

      StoneStatueRenderer(EntityRendererProvider.Context ctx) {
         super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
         this.wide = this.model;
         this.slim = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
         this.addLayer(new StatueCrack(this));
      }

      @Override
      public void render(EntityStoneStatue entity, float yaw, float pt, PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
         // 1.12 rendered MODEL_SLIM when smallArms (always set by the gorgon)
         this.model = entity.smallArms ? this.slim : this.wide;
         super.render(entity, yaw, pt, pose, buffers, light);
      }

      @Override
      protected boolean shouldShowName(EntityStoneStatue entity) {
         return entity.hasCustomName() && super.shouldShowName(entity);
      }

      @Override
      public ResourceLocation getTextureLocation(EntityStoneStatue entity) {
         // getStoneType(smallArms ? MODEL : MODEL_SLIM, 1): the 64x64 player texture size
         return LayerStoneEntity.getStoneType(64, 64);
      }
   }

   static class StatueCrack extends net.minecraft.client.renderer.entity.layers.RenderLayer<EntityStoneStatue, PlayerModel<EntityStoneStatue>> {
      private static final ResourceLocation[] STAGES = new ResourceLocation[10];

      static {
         for (int i = 0; i < 10; i++) {
            STAGES[i] = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + i + ".png");
         }
      }

      StatueCrack(net.minecraft.client.renderer.entity.RenderLayerParent<EntityStoneStatue, PlayerModel<EntityStoneStatue>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light, EntityStoneStatue statue, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (statue.getCrackAmount() > 0) {
            this.getParentModel().renderToBuffer(pose, buffers.getBuffer(net.minecraft.client.renderer.RenderType.crumbling(STAGES[Math.min(9, statue.getCrackAmount() - 1)])), light,
               net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1);
         }
      }
   }

   /** EventClient.initializeStoneLayer: the stone layer on every living renderer (the troll has its own). */
   @SubscribeEvent
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static void addLayers(EntityRenderersEvent.AddLayers event) {
      for (EntityType<?> type : event.getEntityTypes()) {
         if (type == IafLandMobs.TROLL.get() || type == IafLandMobs.STONE_STATUE.get()) {
            continue;
         }
         EntityRenderer<?> r = event.getRenderer(type);
         if (r instanceof LivingEntityRenderer living) {
            living.addLayer(new LayerStoneEntity(living));
         }
      }
   }
}
