package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.ClientEventListener;
import com.lycanitesmobs.client.model.ModelEquipment;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * 1.12 EquipmentRenderer (the item renderer of Lycanites equipment: the parts' models assembled by ModelEquipment), as a
 * 1.21 BlockEntityWithoutLevelRenderer. ITEM_RENDER_MODE 1 (hand/world) draws the mana bar like 1.12.
 */
public class EquipmentRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {
   public EquipmentRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   @Override
   public void renderByItem(ItemStack itemStack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (itemStack.getItem() instanceof ItemEquipment itemEquipment) {
         InteractionHand hand = null;
         pose.pushPose();
         LycGL.begin(buffers, null, light, null);
         LegacyRender.push(pose, null, light, overlay, -1);
         try {
            LegacyRender.translate(0.5F, 0.2F, 0.6F);
            LegacyRender.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            LegacyRender.rotate(-100.0F, 0.0F, 0.0F, 1.0F);
            LegacyRender.translate(0.0F, -1.5F, 0.0F);
            ModelEquipment modelEquipment = new ModelEquipment();
            float loop = 0.0F;
            if (Minecraft.getInstance().player != null) {
               loop = (float)Minecraft.getInstance().player.tickCount;
            }

            modelEquipment.render(itemStack, hand, this, loop);
         } finally {
            LegacyRender.pop();
            LycGL.end();
            pose.popPose();
         }

         if (ClientEventListener.ITEM_RENDER_MODE == 1) {
            double manaNormal = (double)itemEquipment.getMana(itemStack) / (double)ItemEquipment.MANA_MAX;
            if (manaNormal < 1.0) {
               pose.pushPose();
               pose.translate(0.5F, 0.45F, 0.0F);
               // 1.12: depth test, texture, alpha, blend and lighting off; POSITION_COLOR quads
               VertexConsumer buffer = buffers.getBuffer(RenderType.guiOverlay());
               Matrix4f matrix = pose.last().pose();
               double barX = -0.375;
               double barWidth = 0.825;
               double barFill = barWidth * manaNormal;
               quad(buffer, matrix, barX, -0.225, barX + barWidth, -0.075, 0, 0, 0);
               quad(buffer, matrix, barX, -0.2, barX + barFill, -0.15, 0, 128, 255);
               pose.popPose();
            }
         }
      }
   }

   private static void quad(VertexConsumer buffer, Matrix4f matrix, double x0, double y0, double x1, double y1, int r, int g, int b) {
      buffer.addVertex(matrix, (float)x1, (float)y0, 1.0F).setColor(r, g, b, 255);
      buffer.addVertex(matrix, (float)x1, (float)y1, 1.0F).setColor(r, g, b, 255);
      buffer.addVertex(matrix, (float)x0, (float)y1, 1.0F).setColor(r, g, b, 255);
      buffer.addVertex(matrix, (float)x0, (float)y0, 1.0F).setColor(r, g, b, 255);
   }

   @Override
   public void bindItemTexture(ResourceLocation location) {
      if (location != null) {
         LycGL.bindTexture(location);
      }
   }

   @Override
   public List<LayerItem> addLayer(LayerItem renderLayer) {
      return new ArrayList<>();
   }
}
