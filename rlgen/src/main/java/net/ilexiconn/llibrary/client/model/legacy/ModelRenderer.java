package net.ilexiconn.llibrary.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 1.12 net.minecraft.client.model.ModelRenderer drawing into the current {@link LegacyRender} context instead of GL
 * display lists. Transforms are the 1.12 ones: translate(offset) (block units), translate(rotationPoint * scale), rotate Z,
 * then Y, then X; the cubes are "compiled" with the scale of the first render call like the 1.12 display list.
 */
@OnlyIn(Dist.CLIENT)
public class ModelRenderer {
   public float textureWidth = 64.0F;
   public float textureHeight = 32.0F;
   private int textureOffsetX;
   private int textureOffsetY;
   public float rotationPointX;
   public float rotationPointY;
   public float rotationPointZ;
   public float rotateAngleX;
   public float rotateAngleY;
   public float rotateAngleZ;
   protected boolean compiled;
   /** The scale the 1.12 display list was compiled with (first render call). */
   protected float compiledScale;
   public boolean mirror;
   public boolean showModel = true;
   public boolean isHidden;
   public List<ModelBox> cubeList = new ArrayList<>();
   public List<ModelRenderer> childModels;
   public final String boxName;
   private final ModelBase baseModel;
   public float offsetX;
   public float offsetY;
   public float offsetZ;

   public ModelRenderer(ModelBase model, String boxName) {
      this.baseModel = model;
      model.boxList.add(this);
      this.boxName = boxName;
      this.setTextureSize(model.textureWidth, model.textureHeight);
   }

   public ModelRenderer(ModelBase model) {
      this(model, null);
   }

   public ModelRenderer(ModelBase model, int texOffX, int texOffY) {
      this(model);
      this.setTextureOffset(texOffX, texOffY);
   }

   public void addChild(ModelRenderer renderer) {
      if (this.childModels == null) {
         this.childModels = new ArrayList<>();
      }
      this.childModels.add(renderer);
   }

   public ModelRenderer setTextureOffset(int x, int y) {
      this.textureOffsetX = x;
      this.textureOffsetY = y;
      return this;
   }

   public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
      partName = this.boxName + "." + partName;
      TextureOffset textureoffset = this.baseModel.getTextureOffset(partName);
      this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F).setBoxName(partName));
      return this;
   }

   public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
      return this;
   }

   public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F, mirrored));
      return this;
   }

   public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, scaleFactor));
   }

   public void setRotationPoint(float x, float y, float z) {
      this.rotationPointX = x;
      this.rotationPointY = y;
      this.rotationPointZ = z;
   }

   public void render(float scale) {
      if (!this.isHidden && this.showModel) {
         LegacyRender ctx = LegacyRender.current();
         PoseStack pose = ctx.pose;
         this.compile(scale);
         pose.translate(this.offsetX, this.offsetY, this.offsetZ);
         if (this.rotateAngleX != 0.0F || this.rotateAngleY != 0.0F || this.rotateAngleZ != 0.0F) {
            pose.pushPose();
            pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            rotateZYX(pose, this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
            this.callList(ctx);
            this.renderChildren(scale);
            pose.popPose();
         } else if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
            this.callList(ctx);
            this.renderChildren(scale);
         } else {
            pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            this.callList(ctx);
            this.renderChildren(scale);
            pose.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
         }
         pose.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
      }
   }

   private void renderChildren(float scale) {
      if (this.childModels != null) {
         for (int i = 0; i < this.childModels.size(); i++) {
            this.childModels.get(i).render(scale);
         }
      }
   }

   public void renderWithRotation(float scale) {
      if (!this.isHidden && this.showModel) {
         LegacyRender ctx = LegacyRender.current();
         PoseStack pose = ctx.pose;
         this.compile(scale);
         pose.pushPose();
         pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         if (this.rotateAngleY != 0.0F) {
            pose.mulPose(Axis.YP.rotation(this.rotateAngleY));
         }
         if (this.rotateAngleX != 0.0F) {
            pose.mulPose(Axis.XP.rotation(this.rotateAngleX));
         }
         if (this.rotateAngleZ != 0.0F) {
            pose.mulPose(Axis.ZP.rotation(this.rotateAngleZ));
         }
         this.callList(ctx);
         pose.popPose();
      }
   }

   /** Applies this part's transform to the current pose (used to attach things to a part, e.g. held items). */
   public void postRender(float scale) {
      if (!this.isHidden && this.showModel) {
         PoseStack pose = LegacyRender.pose();
         this.compile(scale);
         if (this.rotateAngleX != 0.0F || this.rotateAngleY != 0.0F || this.rotateAngleZ != 0.0F) {
            pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            rotateZYX(pose, this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
         } else if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
            pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         }
      }
   }

   /** GlStateManager.rotate(z), rotate(y), rotate(x), each only when non-zero, like 1.12. */
   protected static void rotateZYX(PoseStack pose, float x, float y, float z) {
      if (z != 0.0F) {
         pose.mulPose(Axis.ZP.rotation(z));
      }
      if (y != 0.0F) {
         pose.mulPose(Axis.YP.rotation(y));
      }
      if (x != 0.0F) {
         pose.mulPose(Axis.XP.rotation(x));
      }
   }

   protected void compile(float scale) {
      if (!this.compiled) {
         this.compiledScale = scale;
         this.compiled = true;
      }
   }

   /** GlStateManager.callList(displayList): draws the cubes with the compile-time scale. */
   protected void callList(LegacyRender ctx) {
      for (int i = 0; i < this.cubeList.size(); i++) {
         this.cubeList.get(i).render(ctx, this.compiledScale);
      }
   }

   public ModelRenderer setTextureSize(int width, int height) {
      this.textureWidth = (float)width;
      this.textureHeight = (float)height;
      return this;
   }
}
