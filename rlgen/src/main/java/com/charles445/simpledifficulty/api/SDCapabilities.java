package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.capability.TemperatureCapability;
import com.charles445.simpledifficulty.capability.ThirstCapability;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12 SDCapabilities: the simpledifficulty:temperature and simpledifficulty:thirst player capabilities as attachments (same NBT as
 * the 1.12 TemperatureStorage/ThirstStorage). Not copied on death: 1.12 had no PlayerEvent.Clone handler, so a respawn starts fresh.
 */
public class SDCapabilities {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SimpleDifficulty.MODID);
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<TemperatureCapability>> TEMPERATURE = ATTACHMENTS.register("temperature",
      () -> AttachmentType.serializable(TemperatureCapability::new).build());
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<ThirstCapability>> THIRST = ATTACHMENTS.register("thirst",
      () -> AttachmentType.serializable(ThirstCapability::new).build());

   public static TemperatureCapability getTemperatureData(Player player) {
      return player.getData(TEMPERATURE);
   }

   public static ThirstCapability getThirstData(Player player) {
      return player.getData(THIRST);
   }

   public static void register(IEventBus bus) {
      ATTACHMENTS.register(bus);
   }
}
