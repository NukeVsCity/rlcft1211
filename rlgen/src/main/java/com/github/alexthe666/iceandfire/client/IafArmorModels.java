package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.model.ModelDeathWormArmor;
import com.github.alexthe666.iceandfire.client.model.ModelFireDragonArmor;
import com.github.alexthe666.iceandfire.client.model.ModelIceDragonArmor;
import com.github.alexthe666.iceandfire.client.model.ModelSeaSerpentArmor;
import com.github.alexthe666.iceandfire.client.model.ModelTrollArmor;
import com.github.alexthe666.iceandfire.item.ItemCustomModelArmor;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 1.12 ClientProxy armor models (getArmorModel index 0..9): FIRE_DRAGON_SCALE_ARMOR_MODEL(0.5) / _LEGS(0.2, legs), ICE_...(0.5) /
 * _LEGS(0.2, legs), DEATHWORM(0.5)/(0.2), TROLL(0.75)/(0.35), TIDE(0.4)/(0.2).
 */
public final class IafArmorModels {
   private IafArmorModels() {
   }

   private static final ModelLayerLocation[] LAYERS = new ModelLayerLocation[10];
   private static final HumanoidModel<?>[] MODELS = new HumanoidModel<?>[10];

   private static ModelLayerLocation loc(int i) {
      return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("iceandfire", "armor_" + i), "main");
   }

   public static void init(IEventBus modBus) {
      for (int i = 0; i < 10; i++) {
         LAYERS[i] = loc(i);
      }
      modBus.addListener(IafArmorModels::layers);
      modBus.addListener(IafArmorModels::extensions);
   }

   private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
      Supplier<LayerDefinition>[] defs = new Supplier[]{() -> ModelFireDragonArmor.createLayer(0.5F, false), () -> ModelFireDragonArmor.createLayer(0.2F, true),
         () -> ModelIceDragonArmor.createLayer(0.5F, false), () -> ModelIceDragonArmor.createLayer(0.2F, true), () -> ModelDeathWormArmor.createLayer(0.5F),
         () -> ModelDeathWormArmor.createLayer(0.2F), () -> ModelTrollArmor.createLayer(0.75F), () -> ModelTrollArmor.createLayer(0.35F),
         () -> ModelSeaSerpentArmor.createLayer(0.4F), () -> ModelSeaSerpentArmor.createLayer(0.2F)};
      for (int i = 0; i < 10; i++) {
         event.registerLayerDefinition(LAYERS[i], defs[i]);
      }
   }

   private static HumanoidModel<?> model(int i) {
      if (MODELS[i] == null) {
         ModelPart part = Minecraft.getInstance().getEntityModels().bakeLayer(LAYERS[i]);
         Function<ModelPart, HumanoidModel<LivingEntity>> f = switch (i / 2) {
            case 0 -> ModelFireDragonArmor::new;
            case 1 -> ModelIceDragonArmor::new;
            case 2 -> ModelDeathWormArmor::new;
            case 3 -> ModelTrollArmor::new;
            default -> ModelSeaSerpentArmor::new;
         };
         MODELS[i] = f.apply(part);
      }
      return MODELS[i];
   }

   private static void extensions(RegisterClientExtensionsEvent event) {
      IClientItemExtensions ext = new IClientItemExtensions() {
         @Override
         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (stack.getItem() instanceof ItemCustomModelArmor armor) {
               return model(armor.armorModelIndex());
            }
            return original;
         }
      };
      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof ItemCustomModelArmor) {
            event.registerItem(ext, item);
         }
      }
   }
}
