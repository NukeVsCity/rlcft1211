package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.client.model.ICustomStatueModel;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 1.12 LayerStoneEntity (added to every living renderer by EventClient.initializeStoneLayer) and LayerStoneEntityCrack: a stone
 * mob is invisible and its model is drawn again with the stone texture sized to the model's texture, then the block break stage.
 * Horses (not llamas) and guardians are drawn with the 1.12 ModelHorseStatue / ModelGuardianStatue. The stone texture size
 * comes from the 1.12 main model of the mob (vanilla models: their 1.12 ModelBase texture size).
 */
public class LayerStoneEntity<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[10];

   static {
      for (int i = 0; i < 10; i++) {
         DESTROY_STAGES[i] = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + i + ".png");
      }
   }

   private static final com.github.alexthe666.iceandfire.client.model.ModelHorseStatue HORSE_MODEL = new com.github.alexthe666.iceandfire.client.model.ModelHorseStatue();
   private static final com.github.alexthe666.iceandfire.client.model.ModelGuardianStatue GUARDIAN_MODEL = new com.github.alexthe666.iceandfire.client.model.ModelGuardianStatue();
   private static final java.util.Map<net.minecraft.world.entity.EntityType<?>, int[]> TEXTURE_112 = new java.util.HashMap<>();

   static {
      net.minecraft.world.entity.EntityType<?>[][] groups = {
         {net.minecraft.world.entity.EntityType.HORSE, net.minecraft.world.entity.EntityType.DONKEY, net.minecraft.world.entity.EntityType.MULE,
            net.minecraft.world.entity.EntityType.SKELETON_HORSE, net.minecraft.world.entity.EntityType.ZOMBIE_HORSE},
         {net.minecraft.world.entity.EntityType.LLAMA, net.minecraft.world.entity.EntityType.TRADER_LLAMA, net.minecraft.world.entity.EntityType.POLAR_BEAR},
         {net.minecraft.world.entity.EntityType.PARROT},
         {net.minecraft.world.entity.EntityType.GUARDIAN, net.minecraft.world.entity.EntityType.ELDER_GUARDIAN, net.minecraft.world.entity.EntityType.BAT,
            net.minecraft.world.entity.EntityType.SHULKER, net.minecraft.world.entity.EntityType.WITHER, net.minecraft.world.entity.EntityType.VEX,
            net.minecraft.world.entity.EntityType.VILLAGER, net.minecraft.world.entity.EntityType.EVOKER, net.minecraft.world.entity.EntityType.VINDICATOR,
            net.minecraft.world.entity.EntityType.ILLUSIONER, net.minecraft.world.entity.EntityType.ZOMBIE, net.minecraft.world.entity.EntityType.HUSK,
            net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN, net.minecraft.world.entity.EntityType.GIANT,
            net.minecraft.world.entity.EntityType.DROWNED, net.minecraft.world.entity.EntityType.ARMOR_STAND},
         {net.minecraft.world.entity.EntityType.WITCH},
         {net.minecraft.world.entity.EntityType.ENDER_DRAGON}
      };
      int[][] sizes = {{128, 128}, {128, 64}, {32, 32}, {64, 64}, {64, 128}, {256, 256}};
      for (int g = 0; g < groups.length; g++) {
         for (net.minecraft.world.entity.EntityType<?> type : groups[g]) {
            TEXTURE_112.put(type, sizes[g]);
         }
      }
   }

   /** 1.12 renderer.getMainModel().textureWidth / textureHeight (ModelBase default 64x32). */
   public static int[] textureSize112(LivingEntity entity, Object model) {
      if (model instanceof ModelBase legacy) {
         return new int[]{legacy.textureWidth, legacy.textureHeight};
      }
      int[] size = TEXTURE_112.get(entity.getType());
      return size != null ? size : new int[]{64, 32};
   }

   private final boolean crackOnly;

   public LayerStoneEntity(RenderLayerParent<T, M> renderer) {
      this(renderer, false);
   }

   public LayerStoneEntity(RenderLayerParent<T, M> renderer, boolean crackOnly) {
      super(renderer);
      this.crackOnly = crackOnly;
   }

   public static ResourceLocation getStoneType(int texWidth, int texHeight) {
      int sizeX = clampToMultipleOfFour(Math.min(128, texWidth));
      int sizeY = clampToMultipleOfFour(Math.min(128, texHeight));
      return sizeX <= 16 && sizeY <= 16
         ? ResourceLocation.withDefaultNamespace("textures/block/stone.png")
         : ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/gorgon/stone" + sizeX + "x" + sizeY + ".png");
   }

   public static int clampToMultipleOfFour(int i) {
      if (i % 4 != 0) {
         if (i > 128) {
            return 128;
         } else if (i > 64) {
            return 64;
         } else {
            return i > 32 ? 32 : 16;
         }
      } else {
         return i;
      }
   }

   @Override
   public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks,
      float ageInTicks, float netHeadYaw, float headPitch) {
      if (!(entity instanceof Mob)) {
         return;
      }
      StoneEntityProperties properties = StoneEntityProperties.get(entity);
      if (properties == null || !properties.isStone) {
         return;
      }
      M model = this.getParentModel();
      if (!this.crackOnly) {
         int[] size = textureSize112(entity, model);
         ResourceLocation tex = getStoneType(size[0], size[1]);
         draw(model, entity, pose, buffers.getBuffer(RenderType.entityCutout(tex)), light, limbSwing, netHeadYaw, headPitch);
      }
      if (properties.breakLvl > 0) {
         VertexConsumer crack = buffers.getBuffer(RenderType.crumbling(DESTROY_STAGES[Math.min(9, properties.breakLvl - 1)]));
         if (this.crackOnly && (Object)model instanceof ModelBase legacy) {
            // 1.12 LayerStoneEntityCrack: a ModelTroll is drawn with its normal render, not renderStatue
            legacy.renderLegacy(pose, crack, light, OverlayTexture.NO_OVERLAY, -1, entity, limbSwing, 0.0F, 0.0F, netHeadYaw, headPitch, 0.0625F);
         } else {
            draw(model, entity, pose, crack, light, limbSwing, netHeadYaw, headPitch);
         }
      }
   }

   static <T extends LivingEntity> void draw(EntityModel<T> model, T entity, PoseStack pose, VertexConsumer vc, int light, float limbSwing, float yaw, float pitch) {
      if ((Object)model instanceof ICustomStatueModel statue && (Object)model instanceof ModelBase) {
         LegacyRender.push(pose, vc, light, OverlayTexture.NO_OVERLAY, -1);
         try {
            statue.renderStatue();
         } finally {
            LegacyRender.pop();
         }
      } else if (entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse && !(entity instanceof net.minecraft.world.entity.animal.horse.Llama)) {
         HORSE_MODEL.renderLegacy(pose, vc, light, OverlayTexture.NO_OVERLAY, -1, entity, limbSwing, 0.0F, 0.0F, yaw, pitch, 0.0625F);
      } else if (entity instanceof net.minecraft.world.entity.monster.Guardian) {
         GUARDIAN_MODEL.renderLegacy(pose, vc, light, OverlayTexture.NO_OVERLAY, -1, entity, limbSwing, 0.0F, 0.0F, yaw, pitch, 0.0625F);
      } else if ((Object)model instanceof ModelBase legacy) {
         legacy.renderLegacy(pose, vc, light, OverlayTexture.NO_OVERLAY, -1, entity, limbSwing, 0.0F, 0.0F, yaw, pitch, 0.0625F);
      } else {
         model.setupAnim(entity, limbSwing, 0.0F, 0.0F, yaw, pitch);
         model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, -1);
      }
   }
}
