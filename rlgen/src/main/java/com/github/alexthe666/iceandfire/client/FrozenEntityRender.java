package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.FrozenEntityProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * 1.12 EventClient.onPostRenderLiving: an entity frozen by ice dragon breath is drawn inside a block of frosted ice (its render
 * box grown 0.25 sideways and on top), whose crack stage follows ticksUntilUnfrozen. UVs are the 1.12 ones (box * 0.625, repeating).
 */
@EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT)
public final class FrozenEntityRender {
   private static final ResourceLocation TEXTURE_0 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_0.png");
   private static final ResourceLocation TEXTURE_1 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_1.png");
   private static final ResourceLocation TEXTURE_2 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_2.png");
   private static final ResourceLocation TEXTURE_3 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_3.png");

   private FrozenEntityRender() {
   }

   @SubscribeEvent
   public static void onPostRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      LivingEntity entity = event.getEntity();
      FrozenEntityProperties frozenProps = FrozenEntityProperties.get(entity);
      if (frozenProps != null && frozenProps.isFrozen) {
         float sideExpand = 0.25F;
         AABB bb = entity.getBoundingBoxForCulling();
         // the pose stack is at the entity's position here (1.12 added event.getX() - posX)
         AABB box = new AABB(bb.minX - entity.getX() - sideExpand, bb.minY - entity.getY(), bb.minZ - entity.getZ() - sideExpand,
            bb.maxX - entity.getX() + sideExpand, bb.maxY - entity.getY() + sideExpand, bb.maxZ - entity.getZ() + sideExpand);
         VertexConsumer vc = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(getIceTexture(frozenProps.ticksUntilUnfrozen)));
         renderAABB(event.getPoseStack(), vc, box, event.getPackedLight());
      }
   }

   private static ResourceLocation getIceTexture(int ticksFrozen) {
      if (ticksFrozen < 100) {
         if (ticksFrozen < 50) {
            return ticksFrozen < 20 ? TEXTURE_3 : TEXTURE_2;
         } else {
            return TEXTURE_1;
         }
      } else {
         return TEXTURE_0;
      }
   }

   private static void v(VertexConsumer vc, Matrix4f m, PoseStack.Pose pose, double x, double y, double z, double u, double v, float nx, float ny, float nz,
                         int light) {
      vc.addVertex(m, (float)x, (float)y, (float)z).setColor(-1).setUv((float)u, (float)v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
         .setNormal(pose, nx, ny, nz);
   }

   /** 1.12 EventClient.renderAABB, vertex for vertex. */
   public static void renderAABB(PoseStack ps, VertexConsumer vc, AABB b, int light) {
      PoseStack.Pose pose = ps.last();
      Matrix4f m = pose.pose();
      double maxX = b.maxX * 0.625;
      double minX = b.minX * 0.625;
      double maxY = b.maxY * 0.625;
      double minY = b.minY * 0.625;
      double maxZ = b.maxZ * 0.625;
      double minZ = b.minZ * 0.625;
      v(vc, m, pose, b.minX, b.maxY, b.minZ, minX - maxX, maxY - minY, 0, 0, -1, light);
      v(vc, m, pose, b.maxX, b.maxY, b.minZ, maxX - minX, maxY - minY, 0, 0, -1, light);
      v(vc, m, pose, b.maxX, b.minY, b.minZ, maxX - minX, minY - maxY, 0, 0, -1, light);
      v(vc, m, pose, b.minX, b.minY, b.minZ, minX - maxX, minY - maxY, 0, 0, -1, light);
      v(vc, m, pose, b.minX, b.minY, b.maxZ, minX - maxX, minY - maxY, 0, 0, 1, light);
      v(vc, m, pose, b.maxX, b.minY, b.maxZ, maxX - minX, minY - maxY, 0, 0, 1, light);
      v(vc, m, pose, b.maxX, b.maxY, b.maxZ, maxX - minX, maxY - minY, 0, 0, 1, light);
      v(vc, m, pose, b.minX, b.maxY, b.maxZ, minX - maxX, maxY - minY, 0, 0, 1, light);
      v(vc, m, pose, b.minX, b.minY, b.minZ, minX - maxX, minY - maxY, 0, -1, 0, light);
      v(vc, m, pose, b.maxX, b.minY, b.minZ, maxX - minX, minY - maxY, 0, -1, 0, light);
      v(vc, m, pose, b.maxX, b.minY, b.maxZ, maxX - minX, maxZ - minZ, 0, -1, 0, light);
      v(vc, m, pose, b.minX, b.minY, b.maxZ, minX - maxX, maxZ - minZ, 0, -1, 0, light);
      v(vc, m, pose, b.minX, b.maxY, b.maxZ, minX - maxX, minY - maxY, 0, 1, 0, light);
      v(vc, m, pose, b.maxX, b.maxY, b.maxZ, maxX - minX, minY - maxY, 0, 1, 0, light);
      v(vc, m, pose, b.maxX, b.maxY, b.minZ, maxX - minX, maxZ - minZ, 0, 1, 0, light);
      v(vc, m, pose, b.minX, b.maxY, b.minZ, minX - maxX, maxZ - minZ, 0, 1, 0, light);
      v(vc, m, pose, b.minX, b.minY, b.maxZ, minX - maxX, minY - maxY, -1, 0, 0, light);
      v(vc, m, pose, b.minX, b.maxY, b.maxZ, minX - maxX, maxY - minY, -1, 0, 0, light);
      v(vc, m, pose, b.minX, b.maxY, b.minZ, maxX - minX, maxY - minY, -1, 0, 0, light);
      v(vc, m, pose, b.minX, b.minY, b.minZ, maxX - minX, minY - maxY, -1, 0, 0, light);
      v(vc, m, pose, b.maxX, b.minY, b.minZ, minX - maxX, minY - maxY, 1, 0, 0, light);
      v(vc, m, pose, b.maxX, b.maxY, b.minZ, minX - maxX, maxY - minY, 1, 0, 0, light);
      v(vc, m, pose, b.maxX, b.maxY, b.maxZ, maxX - minX, maxY - minY, 1, 0, 0, light);
      v(vc, m, pose, b.maxX, b.minY, b.maxZ, maxX - minX, minY - maxY, 1, 0, 0, light);
   }
}
