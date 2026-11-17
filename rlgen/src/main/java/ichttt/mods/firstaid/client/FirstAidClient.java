package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.items.ItemHealing;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

/** 1.12 ClientProxy + ClientEventHandler: H opens the health screen, the HUD overlay, the locational armor tooltips. */
@EventBusSubscriber(modid = FirstAid.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FirstAidClient {
   public static final KeyMapping SHOW_WOUNDS = new KeyMapping("keybinds.show_wounds", GLFW.GLFW_KEY_H, "First Aid");
   private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

   private FirstAidClient() {
   }

   @SubscribeEvent
   public static void keys(RegisterKeyMappingsEvent event) {
      event.register(SHOW_WOUNDS);
   }

   @SubscribeEvent
   public static void layers(RegisterGuiLayersEvent event) {
      event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "overlay"), HUDHandler.INSTANCE);
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      ItemHealing.openGui = hand -> {
         Minecraft mc = Minecraft.getInstance();
         mc.setScreen(new GuiHealthScreen(FirstAid.getDamageModel(mc.player), hand));
      };
      NeoForge.EVENT_BUS.addListener(FirstAidClient::clientTick);
      NeoForge.EVENT_BUS.addListener(FirstAidClient::tooltip);
      NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
         FirstAid.isSynced = false;
         HUDHandler.INSTANCE.ticker = -1;
      });
   }

   /** ClientEventHandler.clientTick (END) + onKeyPress */
   private static void clientTick(ClientTickEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null && !mc.isPaused()) {
         HUDHandler.INSTANCE.clientTick();
      }
      while (mc.player != null && SHOW_WOUNDS.consumeClick()) {
         PlayerDamageModel damageModel = FirstAid.getDamageModel(mc.player);
         if (!damageModel.hasTutorial) {
            // 1.12 showed GuiTutorial once here (not ported); the tutorial counts as done
            damageModel.hasTutorial = true;
            FirstAidNetwork.sendClientRequest(0);
         }
         mc.setScreen(new GuiHealthScreen(damageModel));
      }
   }

   /** ClientEventHandler.tooltipItems: armor values with the locational multiplier and offset (REPLACE the vanilla line) */
   private static void tooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (FirstAidConfig.overlay.armorTooltipMode == FirstAidConfig.Overlay.TooltipMode.NONE || !(stack.getItem() instanceof ArmorItem armor)) {
         return;
      }
      EquipmentSlot slot = armor.getEquipmentSlot();
      List<Component> tooltip = event.getToolTip();
      double normalArmor = ArmorUtils.getArmor(stack, slot);
      double totalArmor = ArmorUtils.applyArmorModifier(slot, normalArmor);
      if (totalArmor > 0.0) {
         replaceOrAppend(tooltip, original(normalArmor, "attribute.name.generic.armor"), msg(totalArmor, "firstaid.specificarmor"));
      }
      double normalToughness = ArmorUtils.getArmorToughness(stack, slot);
      double totalToughness = ArmorUtils.applyToughnessModifier(slot, normalToughness);
      if (totalToughness > 0.0) {
         replaceOrAppend(tooltip, original(normalToughness, "attribute.name.generic.armor_toughness"), msg(totalToughness, "firstaid.specifictoughness"));
      }
   }

   private static Component msg(double value, String key) {
      return value >= 0.0 ? Component.translatable(key, FORMAT.format(value)).withStyle(ChatFormatting.BLUE)
         : Component.translatable(key + ".take", FORMAT.format(-value)).withStyle(ChatFormatting.RED);
   }

   private static String original(double val, String attributeKey) {
      String plusOrTake = val < 0.0 ? "take" : "plus";
      return Component.translatable("attribute.modifier." + plusOrTake + ".0", FORMAT.format(Math.abs(val)), Component.translatable(attributeKey)).getString();
   }

   private static void replaceOrAppend(List<Component> list, String search, Component replace) {
      int index = -1;
      for (int i = 0; i < list.size(); i++) {
         if (list.get(i).getString().trim().equals(search.trim())) {
            index = i;
            break;
         }
      }
      if (FirstAidConfig.overlay.armorTooltipMode == FirstAidConfig.Overlay.TooltipMode.REPLACE && index >= 0) {
         list.set(index, replace);
      } else {
         list.add(replace);
      }
   }
}
