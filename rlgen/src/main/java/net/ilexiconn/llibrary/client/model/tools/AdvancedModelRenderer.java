package net.ilexiconn.llibrary.client.model.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBox;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.ilexiconn.llibrary.client.model.legacy.TextureOffset;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** LLibrary 1.7.20 AdvancedModelRenderer (render() uses the 1.12 transform order through {@link LegacyRender}). */
@OnlyIn(Dist.CLIENT)
public class AdvancedModelRenderer extends ModelRenderer {
   public float defaultRotationX;
   public float defaultRotationY;
   public float defaultRotationZ;
   public float defaultOffsetX;
   public float defaultOffsetY;
   public float defaultOffsetZ;
   public float defaultPositionX;
   public float defaultPositionY;
   public float defaultPositionZ;
   public float scaleX = 1.0F;
   public float scaleY = 1.0F;
   public float scaleZ = 1.0F;
   public float opacity = 1.0F;
   public int textureOffsetX;
   public int textureOffsetY;
   public boolean scaleChildren;
   private AdvancedModelBase model;
   private AdvancedModelRenderer parent;

   public AdvancedModelRenderer(AdvancedModelBase model, String name) {
      super(model, name);
      this.model = model;
   }

   public AdvancedModelRenderer(AdvancedModelBase model) {
      this(model, null);
   }

   public AdvancedModelRenderer(AdvancedModelBase model, int textureOffsetX, int textureOffsetY) {
      this(model);
      this.setTextureOffset(textureOffsetX, textureOffsetY);
   }

   @Override
   public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {
      partName = this.boxName + "." + partName;
      TextureOffset textureoffset = this.model.getTextureOffset(partName);
      this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F).setBoxName(partName));
      return this;
   }

   @Override
   public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
      return this;
   }

   @Override
   public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F, mirrored));
      return this;
   }

   @Override
   public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
      this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, scaleFactor));
   }

   public void setShouldScaleChildren(boolean scaleChildren) {
      this.scaleChildren = scaleChildren;
   }

   public void setScale(float scaleX, float scaleY, float scaleZ) {
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
   }

   public void setScaleX(float scaleX) {
      this.scaleX = scaleX;
   }

   public void setScaleY(float scaleY) {
      this.scaleY = scaleY;
   }

   public void setScaleZ(float scaleZ) {
      this.scaleZ = scaleZ;
   }

   public void setOpacity(float opacity) {
      this.opacity = opacity;
   }

   public void updateDefaultPose() {
      this.defaultRotationX = this.rotateAngleX;
      this.defaultRotationY = this.rotateAngleY;
      this.defaultRotationZ = this.rotateAngleZ;
      this.defaultOffsetX = this.offsetX;
      this.defaultOffsetY = this.offsetY;
      this.defaultOffsetZ = this.offsetZ;
      this.defaultPositionX = this.rotationPointX;
      this.defaultPositionY = this.rotationPointY;
      this.defaultPositionZ = this.rotationPointZ;
   }

   public void resetToDefaultPose() {
      this.rotateAngleX = this.defaultRotationX;
      this.rotateAngleY = this.defaultRotationY;
      this.rotateAngleZ = this.defaultRotationZ;
      this.offsetX = this.defaultOffsetX;
      this.offsetY = this.defaultOffsetY;
      this.offsetZ = this.defaultOffsetZ;
      this.rotationPointX = this.defaultPositionX;
      this.rotationPointY = this.defaultPositionY;
      this.rotationPointZ = this.defaultPositionZ;
   }

   @Override
   public void addChild(ModelRenderer child) {
      super.addChild(child);
      if (child instanceof AdvancedModelRenderer advancedChild) {
         advancedChild.setParent(this);
      }
   }

   public AdvancedModelRenderer getParent() {
      return this.parent;
   }

   public void setParent(AdvancedModelRenderer parent) {
      this.parent = parent;
   }

   public void parentedPostRender(float scale) {
      if (this.parent != null) {
         this.parent.parentedPostRender(scale);
      }
      this.postRender(scale);
   }

   public void renderWithParents(float scale) {
      if (this.parent != null) {
         this.parent.renderWithParents(scale);
      }
      this.render(scale);
   }

   @Override
   public void render(float scale) {
      if (!this.isHidden && this.showModel) {
         LegacyRender ctx = LegacyRender.current();
         PoseStack pose = ctx.pose;
         pose.pushPose();
         this.compile(scale);
         pose.translate(this.offsetX, this.offsetY, this.offsetZ);
         pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         rotateZYX(pose, this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
         if (this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F) {
            pose.scale(this.scaleX, this.scaleY, this.scaleZ);
         }

         if (this.opacity != 1.0F) {
            // GlStateManager.enableBlend + color(1, 1, 1, opacity): needs a translucent RenderType to show
            LegacyRender.color(1.0F, 1.0F, 1.0F, this.opacity);
         }

         this.callList(ctx);
         if (this.opacity != 1.0F) {
            LegacyRender.color(1.0F, 1.0F, 1.0F, 1.0F);
         }

         if (!this.scaleChildren && (this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F)) {
            pose.popPose();
            pose.pushPose();
            pose.translate(this.offsetX, this.offsetY, this.offsetZ);
            pose.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            rotateZYX(pose, this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
         }

         if (this.childModels != null) {
            for (ModelRenderer childModel : this.childModels) {
               childModel.render(scale);
            }
         }

         pose.popPose();
      }
   }

   public AdvancedModelBase getModel() {
      return this.model;
   }

   private float calculateRotation(float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
      float movementScale = this.model.getMovementScale();
      float rotation = Mth.cos(f * speed * movementScale + offset) * degree * movementScale * f1 + weight * f1;
      return invert ? -rotation : rotation;
   }

   public void walk(float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
      this.rotateAngleX = this.rotateAngleX + this.calculateRotation(speed, degree, invert, offset, weight, walk, walkAmount);
   }

   public void flap(float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
      this.rotateAngleZ = this.rotateAngleZ + this.calculateRotation(speed, degree, invert, offset, weight, flap, flapAmount);
   }

   public void swing(float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
      this.rotateAngleY = this.rotateAngleY + this.calculateRotation(speed, degree, invert, offset, weight, swing, swingAmount);
   }

   public void bob(float speed, float degree, boolean bounce, float f, float f1) {
      float movementScale = this.model.getMovementScale();
      degree *= movementScale;
      speed *= movementScale;
      float bob = (float)(Math.sin((double)(f * speed)) * (double)f1 * (double)degree - (double)(f1 * degree));
      if (bounce) {
         bob = (float)(-Math.abs(Math.sin((double)(f * speed)) * (double)f1 * (double)degree));
      }
      this.rotationPointY += bob;
   }

   @Override
   public AdvancedModelRenderer setTextureOffset(int textureOffsetX, int textureOffsetY) {
      this.textureOffsetX = textureOffsetX;
      this.textureOffsetY = textureOffsetY;
      return this;
   }

   public void transitionTo(AdvancedModelRenderer to, float timer, float maxTime) {
      this.rotateAngleX = this.rotateAngleX + (to.rotateAngleX - this.rotateAngleX) / maxTime * timer;
      this.rotateAngleY = this.rotateAngleY + (to.rotateAngleY - this.rotateAngleY) / maxTime * timer;
      this.rotateAngleZ = this.rotateAngleZ + (to.rotateAngleZ - this.rotateAngleZ) / maxTime * timer;
      this.rotationPointX = this.rotationPointX + (to.rotationPointX - this.rotationPointX) / maxTime * timer;
      this.rotationPointY = this.rotationPointY + (to.rotationPointY - this.rotationPointY) / maxTime * timer;
      this.rotationPointZ = this.rotationPointZ + (to.rotationPointZ - this.rotationPointZ) / maxTime * timer;
      this.offsetX = this.offsetX + (to.offsetX - this.offsetX) / maxTime * timer;
      this.offsetY = this.offsetY + (to.offsetY - this.offsetY) / maxTime * timer;
      this.offsetZ = this.offsetZ + (to.offsetZ - this.offsetZ) / maxTime * timer;
   }

   /** 1.12 getWorldPos (javax.vecmath Matrix4d replaced by the same rotations written out). */
   public Vec3 getWorldPos(Entity entity) {
      Vec3 modelPos = this.getModelPos(this, new Vec3(this.rotationPointX / 16.0F, this.rotationPointY / 16.0F, this.rotationPointZ / 16.0F));
      double x = modelPos.x;
      double y = modelPos.y + 1.5;
      double z = modelPos.z;
      double[] p = rotY(new double[]{x, y, z}, -Math.toRadians(entity.getYRot()));
      return new Vec3(p[0] + entity.getX(), p[1] + entity.getY(), p[2] + entity.getZ());
   }

   public Vec3 getModelPos(AdvancedModelRenderer modelRenderer, Vec3 recurseValue) {
      double[] p = new double[]{recurseValue.x, recurseValue.y, recurseValue.z};
      AdvancedModelRenderer parent = modelRenderer.getParent();
      if (parent != null) {
         p = rotX(p, parent.rotateAngleX);
         p = rotY(p, -parent.rotateAngleY);
         p = rotZ(p, -parent.rotateAngleZ);
         p[0] += parent.rotationPointX / 16.0F;
         p[1] += -parent.rotationPointY / 16.0F;
         p[2] += -parent.rotationPointZ / 16.0F;
         return this.getModelPos(parent, new Vec3(p[0], p[1], p[2]));
      } else {
         return new Vec3(p[0], p[1], p[2]);
      }
   }

   // javax.vecmath Matrix4d.rotX/rotY/rotZ(angle).transform(Point3d)
   private static double[] rotX(double[] p, double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[]{p[0], c * p[1] - s * p[2], s * p[1] + c * p[2]};
   }

   private static double[] rotY(double[] p, double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[]{c * p[0] + s * p[2], p[1], -s * p[0] + c * p[2]};
   }

   private static double[] rotZ(double[] p, double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[]{c * p[0] - s * p[1], s * p[0] + c * p[1], p[2]};
   }
}
