package com.github.alexthe666.iceandfire.event;

import com.github.alexthe666.iceandfire.entity.FrozenEntityProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * The iaf/core part of 1.12 EventClient: the frosted ice box drawn around frozen entities (texture by ticksUntilUnfrozen, 0.25 wider
 * on the sides and top, UVs scaled by 0.625 and repeating, like 1.12 renderAABB). The dragon camera views and the siren shader are in
 * the dragon/siren client code.
 */
public class EventClient {
   private static final ResourceLocation TEXTURE_0 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_0.png");
   private static final ResourceLocation TEXTURE_1 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_1.png");
   private static final ResourceLocation TEXTURE_2 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_2.png");
   private static final ResourceLocation TEXTURE_3 = ResourceLocation.withDefaultNamespace("textures/block/frosted_ice_3.png");

   @SubscribeEvent
   public void onPostRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      LivingEntity entity = event.getEntity();
      FrozenEntityProperties frozenProps = FrozenEntityProperties.get(entity);
      if (frozenProps != null && frozenProps.isFrozen) {
         float sideExpand = 0.25F;
         AABB bb = entity.getBoundingBoxForCulling();
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
         }
         return TEXTURE_1;
      }
      return TEXTURE_0;
   }

   private static void v(PoseStack pose, VertexConsumer vc, double x, double y, double z, double u, double v, float nx, float ny, float nz, int light) {
      PoseStack.Pose p = pose.last();
      vc.addVertex(p, (float)x, (float)y, (float)z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv((float)u, (float)v)
         .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
   }

   public static void renderAABB(PoseStack pose, VertexConsumer vc, AABB b, int light) {
      double maxX = b.maxX * 0.625, minX = b.minX * 0.625, maxY = b.maxY * 0.625, minY = b.minY * 0.625, maxZ = b.maxZ * 0.625, minZ = b.minZ * 0.625;
      v(pose, vc, b.minX, b.maxY, b.minZ, minX - maxX, maxY - minY, 0, 0, -1, light);
      v(pose, vc, b.maxX, b.maxY, b.minZ, maxX - minX, maxY - minY, 0, 0, -1, light);
      v(pose, vc, b.maxX, b.minY, b.minZ, maxX - minX, minY - maxY, 0, 0, -1, light);
      v(pose, vc, b.minX, b.minY, b.minZ, minX - maxX, minY - maxY, 0, 0, -1, light);
      v(pose, vc, b.minX, b.minY, b.maxZ, minX - maxX, minY - maxY, 0, 0, 1, light);
      v(pose, vc, b.maxX, b.minY, b.maxZ, maxX - minX, minY - maxY, 0, 0, 1, light);
      v(pose, vc, b.maxX, b.maxY, b.maxZ, maxX - minX, maxY - minY, 0, 0, 1, light);
      v(pose, vc, b.minX, b.maxY, b.maxZ, minX - maxX, maxY - minY, 0, 0, 1, light);
      v(pose, vc, b.minX, b.minY, b.minZ, minX - maxX, minY - maxY, 0, -1, 0, light);
      v(pose, vc, b.maxX, b.minY, b.minZ, maxX - minX, minY - maxY, 0, -1, 0, light);
      v(pose, vc, b.maxX, b.minY, b.maxZ, maxX - minX, maxZ - minZ, 0, -1, 0, light);
      v(pose, vc, b.minX, b.minY, b.maxZ, minX - maxX, maxZ - minZ, 0, -1, 0, light);
      v(pose, vc, b.minX, b.maxY, b.maxZ, minX - maxX, minY - maxY, 0, 1, 0, light);
      v(pose, vc, b.maxX, b.maxY, b.maxZ, maxX - minX, minY - maxY, 0, 1, 0, light);
      v(pose, vc, b.maxX, b.maxY, b.minZ, maxX - minX, maxZ - minZ, 0, 1, 0, light);
      v(pose, vc, b.minX, b.maxY, b.minZ, minX - maxX, maxZ - minZ, 0, 1, 0, light);
      v(pose, vc, b.minX, b.minY, b.maxZ, minX - maxX, minY - maxY, -1, 0, 0, light);
      v(pose, vc, b.minX, b.maxY, b.maxZ, minX - maxX, maxY - minY, -1, 0, 0, light);
      v(pose, vc, b.minX, b.maxY, b.minZ, maxX - minX, maxY - minY, -1, 0, 0, light);
      v(pose, vc, b.minX, b.minY, b.minZ, maxX - minX, minY - maxY, -1, 0, 0, light);
      v(pose, vc, b.maxX, b.minY, b.minZ, minX - maxX, minY - maxY, 1, 0, 0, light);
      v(pose, vc, b.maxX, b.maxY, b.minZ, minX - maxX, maxY - minY, 1, 0, 0, light);
      v(pose, vc, b.maxX, b.maxY, b.maxZ, maxX - minX, maxY - minY, 1, 0, 0, light);
      v(pose, vc, b.maxX, b.minY, b.maxZ, maxX - minX, minY - maxY, 1, 0, 0, light);
   }
}
