package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.gui.GuiHippocampus;
import com.github.alexthe666.iceandfire.client.gui.GuiHippogryph;
import com.github.alexthe666.iceandfire.client.model.ModelAmphithere;
import com.github.alexthe666.iceandfire.client.model.ModelHippocampus;
import com.github.alexthe666.iceandfire.client.model.ModelHippogryph;
import com.github.alexthe666.iceandfire.client.model.ModelPixie;
import com.github.alexthe666.iceandfire.client.model.ModelSiren;
import com.github.alexthe666.iceandfire.client.model.ModelStymphalianBird;
import com.github.alexthe666.iceandfire.client.model.animator.SeaSerpentTabulaModelAnimator;
import com.github.alexthe666.iceandfire.client.model.util.EnumSeaSerpentAnimations;
import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import com.github.alexthe666.iceandfire.client.render.entity.LegacyMobRenderer;
import com.github.alexthe666.iceandfire.client.render.entity.RenderSnowVillager;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerPixieItem;
import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.EntityStymphalianBird;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** ClientProxy.render(): the renderers (RenderHippogryph, RenderAmphithere, ...) and screens of the air and water mobs. */
public final class AirWaterRenderers {
   private AirWaterRenderers() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener(AirWaterRenderers::renderers);
      modBus.addListener(AirWaterRenderers::screens);
   }

   static ResourceLocation tex(String path) {
      return ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/" + path + ".png");
   }

   private static final String[] AMPHITHERE = {"blue", "green", "olive", "red", "yellow"};
   private static final String[] SERPENT = {"blue", "bronze", "darkblue", "green", "purple", "red", "teal"};

   private static <T extends AbstractArrow> EntityRendererProvider<T> arrow(String texture) {
      ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/" + texture + ".png");
      return ctx -> new ArrowRenderer<T>(ctx) {
         @Override
         public ResourceLocation getTextureLocation(T entity) {
            return tex;
         }
      };
   }

   private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
      // RenderHippogryph: 1.2 scale, saddle / bridle (ridden) / chest / armor layers
      event.registerEntityRenderer(AirWaterEntities.HIPPOGRYPH.get(), ctx -> new LegacyMobRenderer<EntityHippogryph>(ctx, new ModelHippogryph(), 0.8F, 1.2F,
            e -> e.isBlinking() ? e.getEnumVariant().TEXTURE_BLINK : e.getEnumVariant().TEXTURE)
         .layer(EntityHippogryph::isSaddled, e -> tex("hippogryph/saddle"))
         .layer(e -> e.isSaddled() && e.getControllingPassenger() != null, e -> tex("hippogryph/bridle"))
         .layer(EntityHippogryph::isChested, e -> tex("hippogryph/chest"))
         .layer(e -> e.getArmor() != 0, e -> tex("hippogryph/armor_" + (e.getArmor() == 1 ? "iron" : e.getArmor() == 2 ? "gold" : "diamond"))));
      event.registerEntityRenderer(AirWaterEntities.HIPPOGRYPH_EGG.get(), ThrownItemRenderer::new);
      // RenderAmphithere: 2.0 scale
      event.registerEntityRenderer(AirWaterEntities.AMPHITHERE.get(), ctx -> new LegacyMobRenderer<EntityAmphithere>(ctx, new ModelAmphithere(), 1.6F, 2.0F,
         e -> e.getVariant() >= 0 && e.getVariant() < 5 ? tex("amphithere/amphithere_" + AMPHITHERE[e.getVariant()] + (e.isBlinking() ? "_blink" : ""))
            : tex("amphithere/amphithere_green")));
      event.registerEntityRenderer(AirWaterEntities.AMPHITHERE_ARROW.get(), arrow("misc/amphithere_arrow"));
      // RenderStymphalianBird: 0.75 scale
      event.registerEntityRenderer(AirWaterEntities.STYMPHALIAN_BIRD.get(), ctx -> new LegacyMobRenderer<EntityStymphalianBird>(ctx,
         new ModelStymphalianBird(), 0.6F, 0.75F, e -> tex("stymphalianbird/stymphalian_bird")));
      event.registerEntityRenderer(AirWaterEntities.STYMPHALIAN_FEATHER.get(), arrow("stymphalianbird/feather"));
      event.registerEntityRenderer(AirWaterEntities.STYMPHALIAN_ARROW.get(), arrow("misc/stymphalian_arrow"));
      // RenderSiren: translate (0, 0, -0.5)
      event.registerEntityRenderer(AirWaterEntities.SIREN.get(), ctx -> new LegacyMobRenderer<EntitySiren>(ctx, new ModelSiren(), 0.8F, 1.0F,
         e -> tex("siren/siren_" + Math.max(0, Math.min(2, e.getHairColor())) + (e.isAgressive() ? "_aggressive" : ""))) {
         @Override
         protected void extraTransform(EntitySiren entity, com.mojang.blaze3d.vertex.PoseStack pose, float partialTick) {
            pose.translate(0.0F, 0.0F, -0.5F);
         }
      });
      // RenderPixie: 0.55 scale, raised while sitting; LayerPixieItem, LayerPixieGlow (additive, full bright)
      event.registerEntityRenderer(AirWaterEntities.PIXIE.get(), ctx -> {
         LegacyMobRenderer<EntityPixie> r = new LegacyMobRenderer<EntityPixie>(ctx, new ModelPixie(), 0.2F, 0.55F,
            e -> tex("pixie/pixie_" + e.getColor())) {
            @Override
            protected void extraTransform(EntityPixie entity, com.mojang.blaze3d.vertex.PoseStack pose, float partialTick) {
               if (entity.isSitting()) {
                  pose.translate(0.0F, 0.5F, 0.0F);
               }
            }
         };
         r.addLayer(new LayerPixieItem(r, ctx.getItemInHandRenderer()));
         r.layer(e -> !AirWaterUtils.isStone(e), e -> tex("pixie/pixie_" + e.getColor()), e -> -1, RenderType::eyes);
         return r;
      });
      // RenderHippocampus: saddle / bridle / chest / rainbow (named "rainbow") / armor layers
      event.registerEntityRenderer(AirWaterEntities.HIPPOCAMPUS.get(), ctx -> new LegacyMobRenderer<EntityHippocampus>(ctx, new ModelHippocampus(), 0.8F,
            1.0F, e -> tex("hippocampus/hippocampus_" + Math.max(0, Math.min(5, e.getVariant())) + (e.isBlinking() ? "_blinking" : "")))
         .layer(EntityHippocampus::isSaddled, e -> tex("hippocampus/saddle"))
         .layer(e -> e.isSaddled() && e.getControllingPassenger() != null, e -> tex("hippocampus/bridle"))
         .layer(EntityHippocampus::isChested, e -> tex("hippocampus/chest"))
         .layer(e -> e.hasCustomName() && e.getCustomName().getString().toLowerCase().contains("rainbow"),
            e -> tex(e.isBlinking() ? "hippocampus/rainbow_blink" : "hippocampus/rainbow"), AirWaterRenderers::rainbow, RenderType::entityCutoutNoCull)
         .layer(e -> e.getArmor() != 0, e -> tex("hippocampus/armor_" + (e.getArmor() == 1 ? "iron" : e.getArmor() == 2 ? "gold" : "diamond"))));
      // RenderSeaSerpent: the Tabula model, scaled by the serpent's size, LayerSeaSerpentAncient
      event.registerEntityRenderer(AirWaterEntities.SEA_SERPENT.get(), ctx -> {
         EnumSeaSerpentAnimations.initializeDragonModels();
         IceAndFireTabulaModel model;
         try {
            model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/seaserpent/seaserpent"),
               new SeaSerpentTabulaModelAnimator());
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
         return new LegacyMobRenderer<EntitySeaSerpent>(ctx, model, 1.6F, 1.0F,
               e -> tex("seaserpent/seaserpent_" + SERPENT[Math.max(0, Math.min(6, e.getVariant()))] + (e.isBlinking() ? "_blink" : ""))) {
            @Override
            protected void extraTransform(EntitySeaSerpent entity, com.mojang.blaze3d.vertex.PoseStack pose, float partialTick) {
               this.shadowRadius = entity.getSeaSerpentScale();
               pose.scale(this.shadowRadius, this.shadowRadius, this.shadowRadius);
            }
         }.layer(EntitySeaSerpent::isAncient, e -> tex(e.isBlinking() ? "seaserpent/ancient_overlay_blink" : "seaserpent/ancient_overlay"), e -> -1,
            RenderType::entityTranslucent);
      });
      event.registerEntityRenderer(AirWaterEntities.SEA_SERPENT_BUBBLES.get(), NoopRenderer::new);
      event.registerEntityRenderer(AirWaterEntities.SEA_SERPENT_ARROW.get(), arrow("misc/sea_serpent_arrow"));
      event.registerEntityRenderer(AirWaterEntities.SNOW_VILLAGER.get(), RenderSnowVillager::new);
   }

   /** LayerHippocampusRainbow: the sheep dye cycle (jeb_) */
   private static int rainbow(EntityHippocampus e) {
      int i = e.tickCount / 25 + e.getId();
      int j = DyeColor.values().length;
      int k = i % j;
      int l = (i + 1) % j;
      float f = ((float)(e.tickCount % 25) + net.ilexiconn.llibrary.LLibrary.PROXY.getPartialTicks()) / 25.0F;
      int c1 = Sheep.getColor(DyeColor.byId(k));
      int c2 = Sheep.getColor(DyeColor.byId(l));
      return FastColor.ARGB32.lerp(f, c1, c2);
   }

   private static void screens(RegisterMenuScreensEvent event) {
      event.register(AirWaterEntities.HIPPOGRYPH_MENU.get(), GuiHippogryph::new);
      event.register(AirWaterEntities.HIPPOCAMPUS_MENU.get(), GuiHippocampus::new);
   }
}
