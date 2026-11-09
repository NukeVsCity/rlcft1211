package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.enums.EnumDragonTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** 1.12 LayerDragonArmor: the main model again with the armor texture of one slot (0 head, 1 neck, 2 body, 3 tail). */
public class LayerDragonArmor extends RenderLayer<EntityDragonBase, EntityModel<EntityDragonBase>> {
   private final int slot;

   public LayerDragonArmor(RenderLayerParent<EntityDragonBase, EntityModel<EntityDragonBase>> renderer, int slot) {
      super(renderer);
      this.slot = slot;
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource buffers, int light, EntityDragonBase entity, float limbSwing, float limbSwingAmount,
                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.getArmorInSlot(this.slot) != 0) {
         EnumDragonTextures.Armor armor = EnumDragonTextures.Armor.getArmorForDragon(entity, this.slot);
         ResourceLocation texture = entity instanceof EntityIceDragon ? armor.ICETEXTURE : armor.FIRETEXTURE;
         if (texture != null) {
            this.getParentModel().renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light,
               LivingEntityRenderer.getOverlayCoords(entity, 0.0F), -1);
         }
      }
   }
}
