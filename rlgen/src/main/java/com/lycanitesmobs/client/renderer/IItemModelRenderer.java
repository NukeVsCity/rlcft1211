package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.renderer.layer.LayerItem;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public interface IItemModelRenderer {
   void bindItemTexture(ResourceLocation var1);

   List<LayerItem> addLayer(LayerItem var1);
}
