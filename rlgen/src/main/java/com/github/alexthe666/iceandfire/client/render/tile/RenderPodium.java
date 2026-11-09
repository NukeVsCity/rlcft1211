package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPodium;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.github.alexthe666.iceandfire.item.ItemDragonEgg;
import com.github.alexthe666.iceandfire.item.ItemMyrmexEgg;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** 1.12 RenderPodium: a dragon or myrmex egg (ModelDragonEgg.renderPodium) standing on the podium; other items are not drawn (1.12). */
public class RenderPodium implements BlockEntityRenderer<TileEntityPodium> {
   public static final ResourceLocation EGG_JUNGLE = ResourceLocation.parse("iceandfire:textures/models/myrmex/myrmex_jungle_egg.png");
   public static final ResourceLocation EGG_DESERT = ResourceLocation.parse("iceandfire:textures/models/myrmex/myrmex_desert_egg.png");

   public RenderPodium(BlockEntityRendererProvider.Context ctx) {
   }

   public static ResourceLocation getEggTexture(EnumDragonEgg type) {
      return switch (type) {
         case GREEN -> IafRenderHooks.EGG_GREEN;
         case BRONZE -> IafRenderHooks.EGG_BRONZE;
         case GRAY -> IafRenderHooks.EGG_GREY;
         case BLUE -> IafRenderHooks.EGG_BLUE;
         case WHITE -> IafRenderHooks.EGG_WHITE;
         case SAPPHIRE -> IafRenderHooks.EGG_SAPPHIRE;
         case SILVER -> IafRenderHooks.EGG_SILVER;
         default -> IafRenderHooks.EGG_RED;
      };
   }

   @Override
   public void render(TileEntityPodium podium, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      ItemStack stack = podium.getStackInSlot(0);
      if (stack.isEmpty() || IafRenderHooks.egg == null) {
         return;
      }
      ResourceLocation tex = null;
      if (stack.getItem() instanceof ItemDragonEgg egg) {
         tex = getEggTexture(egg.type);
      } else if (stack.getItem() instanceof ItemMyrmexEgg) {
         tex = stack.getItem() == ModItems.myrmex_jungle_egg ? EGG_JUNGLE : EGG_DESERT;
      }
      if (tex != null) {
         pose.pushPose();
         pose.translate(0.5F, 0.475F, 0.5F);
         IafRenderHooks.egg.render(pose, buffers, light, overlay, tex, -1);
         pose.popPose();
      }
   }
}
