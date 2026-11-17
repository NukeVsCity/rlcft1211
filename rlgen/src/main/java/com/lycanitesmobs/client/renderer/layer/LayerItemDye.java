package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.IItemModelRenderer;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import org.joml.Vector4f;
import com.lycanitesmobs.client.renderer.LycGL;
import net.minecraft.world.item.ItemStack;

public class LayerItemDye extends LayerItem {
   public LayerItemDye(IItemModelRenderer renderer, String name) {
      super(renderer, name);
   }

   @Override
   public Vector4f getPartColor(String partName, ItemStack itemStack, float loop) {
      if (!(itemStack.getItem() instanceof ItemEquipmentPart)) {
         return super.getPartColor(partName, itemStack, loop);
      } else {
         ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart)itemStack.getItem();
         return itemEquipmentPart.getColor(itemStack);
      }
   }

   @Override
   public void onRenderStart(ItemStack itemStack) {
      LycGL.blendFunc(com.lycanitesmobs.client.renderer.LycRenderTypes.BLEND_NORMAL);
   }

   @Override
   public void onRenderFinish(ItemStack itemStack) {
   }
}
