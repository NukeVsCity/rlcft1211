package net.ilexiconn.llibrary.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * Stand-in for the 1.12 immediate mode state (GlStateManager matrix + colour, the Tessellator buffer and the lightmap
 * coordinates) that 1.12 {@link ModelRenderer#render(float)} drew into. A 1.21 renderer opens a context with
 * {@link #push} (or calls {@link ModelBase#renderToBuffer}, which does it), then 1.12 model code runs unchanged: its
 * {@code GlStateManager.pushMatrix/translate/rotate/scale/color} calls become the same-named static methods here and act
 * on the context's PoseStack and colour.
 */
@OnlyIn(Dist.CLIENT)
public final class LegacyRender {
   private static final Deque<LegacyRender> STACK = new ArrayDeque<>();

   public final PoseStack pose;
   public VertexConsumer buffer;
   public int light;
   public int overlay;
   public float red;
   public float green;
   public float blue;
   public float alpha;

   private LegacyRender(PoseStack pose, VertexConsumer buffer, int light, int overlay, int argb) {
      this.pose = pose;
      this.buffer = buffer;
      this.light = light;
      this.overlay = overlay;
      this.alpha = FastColor.ARGB32.alpha(argb) / 255.0F;
      this.red = FastColor.ARGB32.red(argb) / 255.0F;
      this.green = FastColor.ARGB32.green(argb) / 255.0F;
      this.blue = FastColor.ARGB32.blue(argb) / 255.0F;
   }

   /** Opens a context; every push needs a {@link #pop()} (use try/finally). argb -1 = white, like GlStateManager.color(1,1,1,1). */
   public static LegacyRender push(PoseStack pose, VertexConsumer buffer, int light, int overlay, int argb) {
      LegacyRender ctx = new LegacyRender(pose, buffer, light, overlay, argb);
      STACK.push(ctx);
      return ctx;
   }

   public static void pop() {
      STACK.pop();
   }

   public static boolean active() {
      return !STACK.isEmpty();
   }

   public static LegacyRender current() {
      LegacyRender ctx = STACK.peek();
      if (ctx == null) {
         throw new IllegalStateException("1.12 model rendered outside a LegacyRender context (use ModelBase.renderToBuffer or LegacyRender.push)");
      }
      return ctx;
   }

   /** Switches the buffer of the current context, e.g. to draw the next parts with another RenderType. */
   public static void setBuffer(VertexConsumer buffer) {
      current().buffer = buffer;
   }

   public static void setLight(int light) {
      current().light = light;
   }

   public static PoseStack pose() {
      return current().pose;
   }

   public int color() {
      return FastColor.ARGB32.colorFromFloat(this.alpha, this.red, this.green, this.blue);
   }

   // ---- GlStateManager equivalents (1.12 names) ----

   public static void pushMatrix() {
      pose().pushPose();
   }

   public static void popMatrix() {
      pose().popPose();
   }

   public static void translate(float x, float y, float z) {
      pose().translate(x, y, z);
   }

   public static void translate(double x, double y, double z) {
      pose().translate(x, y, z);
   }

   public static void scale(float x, float y, float z) {
      pose().scale(x, y, z);
   }

   public static void scale(double x, double y, double z) {
      pose().scale((float)x, (float)y, (float)z);
   }

   /** GlStateManager.rotate(angle in degrees, axis): glRotatef normalises the axis. */
   public static void rotate(float angle, float x, float y, float z) {
      if (x == 0.0F && y == 0.0F && z == 0.0F) {
         return;
      }
      float len = (float)Math.sqrt(x * x + y * y + z * z);
      pose().mulPose(new Quaternionf().rotationAxis((float)Math.toRadians(angle), x / len, y / len, z / len));
   }

   public static void color(float r, float g, float b, float a) {
      LegacyRender ctx = current();
      ctx.red = r;
      ctx.green = g;
      ctx.blue = b;
      ctx.alpha = a;
   }

   public static void color(float r, float g, float b) {
      color(r, g, b, 1.0F);
   }
}
