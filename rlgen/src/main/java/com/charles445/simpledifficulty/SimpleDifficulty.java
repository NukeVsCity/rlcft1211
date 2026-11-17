package com.charles445.simpledifficulty;

import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.command.CommandSimpleDifficulty;
import com.charles445.simpledifficulty.config.JsonConfigInternal;
import com.charles445.simpledifficulty.handler.BlockHandler;
import com.charles445.simpledifficulty.handler.CapabilityHandler;
import com.charles445.simpledifficulty.handler.TemperatureHandler;
import com.charles445.simpledifficulty.handler.ThirstHandler;
import com.charles445.simpledifficulty.network.PacketHandler;
import com.charles445.simpledifficulty.register.RegisterRecipes;
import com.charles445.simpledifficulty.temperature.Modifiers;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple Difficulty 0.3.9 (thirst + temperature) as in RLCraft 2.9.3, ported from the 1.12 code. The registers live in the api.SD*
 * holder classes like the 1.12 api fields; capabilities are attachments (SDCapabilities), messages are payloads (PacketHandler).
 */
@Mod(SimpleDifficulty.MODID)
public class SimpleDifficulty {
   public static final String MODID = "simpledifficulty";
   public static final String NAME = "SimpleDifficulty";
   public static final String VERSION = "0.3.9";
   public static final Logger logger = LogManager.getLogger("SimpleDifficulty");

   public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(MODID);
   /** The 1.12 item metadata of items that had subtypes (juice). */
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> METADATA = COMPONENTS.registerComponentType("metadata",
      b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tab", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup.tabSimpleDifficulty"))
      .icon(() -> new ItemStack(SDItems.purifiedWaterBottle.get()))
      .displayItems((params, out) -> SDItems.fillTab(out))
      .build());

   public SimpleDifficulty(IEventBus modBus) {
      COMPONENTS.register(modBus);
      TABS.register(modBus);
      SDBlocks.register(modBus);
      SDItems.register(modBus);
      SDFluids.register(modBus);
      SDPotions.register(modBus);
      SDCapabilities.register(modBus);
      RegisterRecipes.register(modBus);
      modBus.addListener(PacketHandler::register);
      modBus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.register(new BlockHandler());
      NeoForge.EVENT_BUS.register(new CapabilityHandler());
      NeoForge.EVENT_BUS.register(new TemperatureHandler());
      NeoForge.EVENT_BUS.register(new ThirstHandler());
      NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> CommandSimpleDifficulty.register(e.getDispatcher()));
      NeoForge.EVENT_BUS.addListener(RegisterRecipes::registerBrewing);
      Modifiers.registerAll();
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(JsonConfigInternal::postInit);
   }

   public static int getMetadata(ItemStack stack) {
      Integer m = stack.get(METADATA.get());
      return m == null ? 0 : m;
   }
}
