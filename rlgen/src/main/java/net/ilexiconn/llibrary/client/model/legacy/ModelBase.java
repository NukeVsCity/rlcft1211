package net.ilexiconn.llibrary.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 1.12 net.minecraft.client.model.ModelBase, plugged into the 1.21 renderer pipeline as an {@link EntityModel}:
 * <ul>
 * <li>{@link #prepareMobModel} calls the 1.12 {@link #setLivingAnimations};</li>
 * <li>{@link #setupAnim} only remembers its arguments (1.12 setRotationAngles is called by render() itself);</li>
 * <li>{@link #renderToBuffer} copies attackTime/riding/young into swingProgress/isRiding/isChild, opens a
 * {@link LegacyRender} context and calls the 1.12 {@link #render(Entity, float, float, float, float, float, float)} with
 * scale 0.0625F, like 1.12 RenderLivingBase.renderModel.</li>
 * </ul>
 * Use it in a 1.21 renderer with an unchecked cast, e.g. {@code (EntityModel<EntityFireDragon>)(EntityModel<?>)model}.
 */
@OnlyIn(Dist.CLIENT)
public abstract class ModelBase extends EntityModel<Entity> {
   public float swingProgress;
   public boolean isRiding;
   public boolean isChild = true;
   public List<ModelRenderer> boxList = new ArrayList<>();
   private final Map<String, TextureOffset> modelTextureMap = new HashMap<>();
   public int textureWidth = 64;
   public int textureHeight = 32;

   private Entity animEntity;
   private float animLimbSwing;
   private float animLimbSwingAmount;
   private float animAgeInTicks;
   private float animNetHeadYaw;
   private float animHeadPitch;

   protected ModelBase() {
      super();
   }

   protected ModelBase(Function<ResourceLocation, RenderType> renderType) {
      super(renderType);
   }

   public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
   }

   public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
   }

   public void setLivingAnimations(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
   }

   public ModelRenderer getRandomModelBox(Random rand) {
      return this.boxList.get(rand.nextInt(this.boxList.size()));
   }

   protected void setTextureOffset(String partName, int x, int y) {
      this.modelTextureMap.put(partName, new TextureOffset(x, y));
   }

   public TextureOffset getTextureOffset(String partName) {
      return this.modelTextureMap.get(partName);
   }

   public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
      dest.rotateAngleX = source.rotateAngleX;
      dest.rotateAngleY = source.rotateAngleY;
      dest.rotateAngleZ = source.rotateAngleZ;
      dest.rotationPointX = source.rotationPointX;
      dest.rotationPointY = source.rotationPointY;
      dest.rotationPointZ = source.rotationPointZ;
   }

   public void setModelAttributes(ModelBase model) {
      this.swingProgress = model.swingProgress;
      this.isRiding = model.isRiding;
      this.isChild = model.isChild;
   }

   // ---- 1.21 EntityModel bridge ----

   @Override
   public void prepareMobModel(Entity entity, float limbSwing, float limbSwingAmount, float partialTick) {
      if (entity instanceof LivingEntity living) {
         this.setLivingAnimations(living, limbSwing, limbSwingAmount, partialTick);
      }
   }

   @Override
   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animEntity = entity;
      this.animLimbSwing = limbSwing;
      this.animLimbSwingAmount = limbSwingAmount;
      this.animAgeInTicks = ageInTicks;
      this.animNetHeadYaw = netHeadYaw;
      this.animHeadPitch = headPitch;
   }

   @Override
   public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.swingProgress = this.attackTime;
      this.isRiding = this.riding;
      this.isChild = this.young;
      LegacyRender.push(pose, buffer, packedLight, packedOverlay, color);
      try {
         this.render(this.animEntity, this.animLimbSwing, this.animLimbSwingAmount, this.animAgeInTicks, this.animNetHeadYaw, this.animHeadPitch, 0.0625F);
      } finally {
         LegacyRender.pop();
      }
   }

   /** Draws the 1.12 model with explicit arguments (for layers / block entity renderers that called model.render in 1.12). */
   public void renderLegacy(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color, Entity entity, float limbSwing,
      float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      LegacyRender.push(pose, buffer, packedLight, packedOverlay, color);
      try {
         this.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      } finally {
         LegacyRender.pop();
      }
   }
}
