package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.client.model.ModelItemBase;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12 EquipmentPartRenderer (the TileEntitySpecialRenderer used as the item renderer of equipment parts), as the 1.21
 * item renderer (BlockEntityWithoutLevelRenderer). The item model is "builtin/entity" without display transforms like the
 * 1.12 EquipmentPartModel, so the 1.12 transforms below are the only ones.
 */
public class EquipmentPartRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {
   protected List<LayerItem> renderLayers = new ArrayList<>();

   public EquipmentPartRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   @Override
   public void renderByItem(ItemStack itemStack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (itemStack.getItem() instanceof ItemEquipmentPart itemEquipmentPart) {
         InteractionHand hand = null;
         ModelItemBase modelItemBase = AssetManager.getItemModel(itemEquipmentPart.itemName);
         this.renderLayers.clear();
         modelItemBase.addCustomLayers(this);
         float loop = 0.0F;
         if (Minecraft.getInstance().player != null) {
            loop = (float)Minecraft.getInstance().player.tickCount;
         }

         pose.pushPose();
         LycGL.begin(buffers, null, light, null);
         LegacyRender.push(pose, null, light, overlay, -1);
         try {
            LegacyRender.translate(0.5F, 0.35F, 0.5F);
            LegacyRender.rotate(190.0F, 1.0F, 0.0F, 0.0F);
            LegacyRender.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
            LegacyRender.rotate(10.0F, 0.0F, 0.0F, 1.0F);
            LegacyRender.translate(0.0F, -1.7F, 0.0F);
            if ("head".equalsIgnoreCase(itemEquipmentPart.slotType)) {
               LegacyRender.translate(0.0F, 0.0F, 0.5F);
            } else if ("blade".equalsIgnoreCase(itemEquipmentPart.slotType)
               || "pike".equalsIgnoreCase(itemEquipmentPart.slotType)
               || "axe".equalsIgnoreCase(itemEquipmentPart.slotType)
               || "jewel".equalsIgnoreCase(itemEquipmentPart.slotType)) {
               LegacyRender.translate(0.0F, 0.0F, 1.0F);
            }

            LegacyRender.pushMatrix();
            modelItemBase.generateAnimationFrames(itemStack, null, loop, null);
            modelItemBase.render(itemStack, hand, this, null, null, loop, false);

            for (LayerItem renderLayer : this.renderLayers) {
               modelItemBase.render(itemStack, hand, this, null, renderLayer, loop, false);
            }

            modelItemBase.clearAnimationFrames();
            LegacyRender.popMatrix();
         } finally {
            LegacyRender.pop();
            LycGL.end();
            pose.popPose();
         }
      }
   }

   @Override
   public void bindItemTexture(ResourceLocation location) {
      if (location != null) {
         LycGL.bindTexture(location);
      }
   }

   @Override
   public List<LayerItem> addLayer(LayerItem renderLayer) {
      if (!this.renderLayers.contains(renderLayer)) {
         this.renderLayers.add(renderLayer);
      }

      return this.renderLayers;
   }
}
