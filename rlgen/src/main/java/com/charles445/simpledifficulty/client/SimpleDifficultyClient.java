package com.charles445.simpledifficulty.client;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.client.gui.TemperatureGui;
import com.charles445.simpledifficulty.client.gui.ThirstGui;
import com.charles445.simpledifficulty.client.particle.ParticleTemperature;
import com.charles445.simpledifficulty.client.render.RenderSpit;
import com.charles445.simpledifficulty.item.ItemArmorTemperature;
import com.charles445.simpledifficulty.item.ItemCanteen;
import com.charles445.simpledifficulty.item.ItemThermometer;
import com.charles445.simpledifficulty.register.SDParticles;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 1.12 ClientProxy + RegisterClientModels: HUD, item model properties, the spit renderer, particles, the purified water fluid. */
@EventBusSubscriber(modid = SimpleDifficulty.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SimpleDifficultyClient {
   private static final ThirstGui THIRST = new ThirstGui();
   private static final TemperatureGui TEMPERATURE = new TemperatureGui();

   private SimpleDifficultyClient() {
   }

   @SubscribeEvent
   public static void guiLayers(RegisterGuiLayersEvent event) {
      event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "temperature"), TEMPERATURE);
      event.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "thirst"), THIRST);
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
         THIRST.onClientTick();
         TEMPERATURE.onClientTick();
      });
      NeoForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> ItemArmorTemperature.armorTempTooltip(e.getItemStack(), e.getToolTip()));
      event.enqueueWork(() -> {
         ItemProperties.register(SDItems.thermometer.get(), ResourceLocation.withDefaultNamespace("temperature"),
            (stack, level, entity, seed) -> ItemThermometer.property(stack, level, entity));
         ItemProperties.register(SDItems.juice.get(), ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "type"),
            (stack, level, entity, seed) -> (float)SimpleDifficulty.getMetadata(stack));
         for (Item canteen : new Item[]{SDItems.ironCanteen.get(), SDItems.dragon_canteen.get()}) {
            ItemProperties.register(canteen, ResourceLocation.withDefaultNamespace("contain"),
               (stack, level, entity, seed) -> stack.getItem() instanceof ItemCanteen c && !c.isCanteenEmpty(stack) ? 1.0F : 0.0F);
         }
      });
   }

   @SubscribeEvent
   public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(SDBlocks.SPIT_TILE.get(), RenderSpit::new);
   }

   @SubscribeEvent
   public static void particles(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(SDParticles.HEATER.get(), ParticleTemperature.Provider::new);
      event.registerSpriteSet(SDParticles.CHILLER.get(), ParticleTemperature.Provider::new);
   }

   @SubscribeEvent
   public static void extensions(RegisterClientExtensionsEvent event) {
      event.registerFluidType(new IClientFluidTypeExtensions() {
         private final ResourceLocation still = ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "block/purifiedwater_still");
         private final ResourceLocation flow = ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "block/purifiedwater_flow");

         @Override
         public ResourceLocation getStillTexture() {
            return this.still;
         }

         @Override
         public ResourceLocation getFlowingTexture() {
            return this.flow;
         }
      }, SDFluids.purifiedWaterType.get());
   }
}
