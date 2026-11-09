package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.client.model.ModelGorgonHead;
import com.github.alexthe666.iceandfire.client.model.ModelGorgonHeadActive;
import com.github.alexthe666.iceandfire.client.model.ModelTrollWeapon;
import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.item.ItemTrollWeapon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12 IceAndFireTEISR + the dummy tile entity renderers of the "builtin/entity" items: troll weapons (RenderTrollWeapon), the gorgon
 * head (RenderGorgonHead, active model while drawn = metadata 1) and the pixie house item (RenderPixieHouse with the item's house type).
 */
public class IceAndFireTEISR extends BlockEntityWithoutLevelRenderer {
   private static final ModelBase TROLL_WEAPON = new ModelTrollWeapon();
   private static final ModelBase GORGON_ACTIVE = new ModelGorgonHeadActive();
   private static final ModelBase GORGON_INACTIVE = new ModelGorgonHead();
   private static final ResourceLocation ACTIVE_TEXTURE = ResourceLocation.parse("iceandfire:textures/models/gorgon/head_active.png");
   private static final ResourceLocation INACTIVE_TEXTURE = ResourceLocation.parse("iceandfire:textures/models/gorgon/head_inactive.png");

   public IceAndFireTEISR() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   @Override
   public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (stack.getItem() instanceof ItemTrollWeapon weapon) {
         legacy(pose, buffers, light, overlay, TROLL_WEAPON, weapon.weapon.TEXTURE);
      } else if (stack.getItem() == ModItems.gorgon_head) {
         boolean active = IafMeta.getMetadata(stack) == 1;
         legacy(pose, buffers, light, overlay, active ? GORGON_ACTIVE : GORGON_INACTIVE, active ? ACTIVE_TEXTURE : INACTIVE_TEXTURE);
      } else if (stack.getItem() == ModBlocks.pixieHouse.asItem()) {
         RenderPixieHouse.renderHouse(pose, buffers, light, overlay, IafMeta.getMetadata(stack), 0, null);
      }
   }

   /** 1.12: translate(x + 0.5, y - 0.75, z + 0.5), model.render(null, 0, 0, 0, 0, 0, 1/16). */
   private static void legacy(PoseStack pose, MultiBufferSource buffers, int light, int overlay, ModelBase model, ResourceLocation tex) {
      pose.pushPose();
      pose.translate(0.5F, -0.75F, 0.5F);
      LegacyRender.push(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)), light, overlay, -1);
      try {
         model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
      } finally {
         LegacyRender.pop();
      }
      pose.popPose();
   }
}
