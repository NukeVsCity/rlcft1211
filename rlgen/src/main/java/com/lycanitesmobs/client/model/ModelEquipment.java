package com.lycanitesmobs.client.model;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.client.renderer.EquipmentRenderer;
import com.lycanitesmobs.client.renderer.IItemModelRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

public class ModelEquipment implements IItemModelRenderer {
   protected List<LayerItem> renderLayers = new ArrayList<>();
   protected List<ModelItemBase> renderedModels = new ArrayList<>();

   public void render(ItemStack itemStack, InteractionHand hand, EquipmentRenderer renderer, float loop) {
      if (itemStack.getItem() instanceof ItemEquipment) {
         ItemEquipment itemEquipment = (ItemEquipment)itemStack.getItem();
         NonNullList<ItemStack> equipmentPartStacks = itemEquipment.getEquipmentPartStacks(itemStack);
         int slotId = -1;
         ModelItemBase modelPartBase = null;
         ModelItemBase modelPartHead = null;

         for (ItemStack partStack : equipmentPartStacks) {
            if (++slotId == 0) {
               modelPartBase = this.renderPart(partStack, hand, renderer, null, loop);
            } else if (slotId == 1) {
               if (modelPartBase == null || !modelPartBase.animationParts.containsKey("head")) {
                  continue;
               }

               modelPartHead = this.renderPart(partStack, hand, renderer, modelPartBase.animationParts.get("head"), loop);
            } else if (slotId == 2) {
               if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipa")) {
                  continue;
               }

               this.renderPart(partStack, hand, renderer, modelPartHead.animationParts.get("tipa"), loop);
            } else if (slotId == 3) {
               if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipb")) {
                  continue;
               }

               this.renderPart(partStack, hand, renderer, modelPartHead.animationParts.get("tipb"), loop);
            } else if (slotId == 4) {
               if (modelPartHead == null || !modelPartHead.animationParts.containsKey("tipc")) {
                  continue;
               }

               this.renderPart(partStack, hand, renderer, modelPartHead.animationParts.get("tipc"), loop);
            }

            if (slotId == 5 && modelPartBase != null && modelPartBase.animationParts.containsKey("pommel")) {
               this.renderPart(partStack, hand, renderer, modelPartBase.animationParts.get("pommel"), loop);
            }
         }

         for (ModelItemBase modelItemBase : this.renderedModels) {
            modelItemBase.clearAnimationFrames();
         }

         this.renderedModels.clear();
      }
   }

   public ModelItemBase renderPart(ItemStack partStack, InteractionHand hand, EquipmentRenderer renderer, ModelObjPart offsetPart, float loop) {
      if (!partStack.isEmpty() && partStack.getItem() instanceof ItemEquipmentPart) {
         ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart)partStack.getItem();
         ModelItemBase modelItemBase = AssetManager.getItemModel(itemEquipmentPart.itemName);
         if (modelItemBase.animationParts.containsKey("base")) {
            modelItemBase.animationParts.get("base").setOffset(offsetPart);
         }

         this.renderLayers.clear();
         modelItemBase.addCustomLayers(this);
         modelItemBase.generateAnimationFrames(partStack, null, loop, offsetPart);
         modelItemBase.render(partStack, hand, renderer, offsetPart, null, loop, false);

         for (LayerItem renderLayer : this.renderLayers) {
            modelItemBase.render(partStack, hand, renderer, offsetPart, renderLayer, loop, false);
         }

         this.renderedModels.add(modelItemBase);
         return modelItemBase;
      } else {
         return null;
      }
   }

   @Override
   public void bindItemTexture(ResourceLocation location) {
   }

   @Override
   public List<LayerItem> addLayer(LayerItem renderLayer) {
      if (!this.renderLayers.contains(renderLayer)) {
         this.renderLayers.add(renderLayer);
      }

      return this.renderLayers;
   }
}
