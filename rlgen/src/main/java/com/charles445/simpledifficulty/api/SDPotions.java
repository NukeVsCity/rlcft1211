package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.potion.PotionBase;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 1.12 SDPotions + RegisterPotions (effects and potion types). */
public class SDPotions {
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, SimpleDifficulty.MODID);
   public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, SimpleDifficulty.MODID);
   public static final Map<String, Holder<Potion>> potionTypes = new HashMap<>();

   public static final DeferredHolder<MobEffect, MobEffect> hyperthermia = EFFECTS.register("hyperthermia", PotionBase.Hyperthermia::new);
   public static final DeferredHolder<MobEffect, MobEffect> hypothermia = EFFECTS.register("hypothermia", PotionBase.Hypothermia::new);
   public static final DeferredHolder<MobEffect, MobEffect> thirsty = EFFECTS.register("thirsty", PotionBase.Thirsty::new);
   public static final DeferredHolder<MobEffect, MobEffect> parasites = EFFECTS.register("parasites", PotionBase.Parasites::new);
   public static final DeferredHolder<MobEffect, MobEffect> cold_resist = EFFECTS.register("cold_resist", PotionBase.ResistCold::new);
   public static final DeferredHolder<MobEffect, MobEffect> heat_resist = EFFECTS.register("heat_resist", PotionBase.ResistHeat::new);

   public static final DeferredHolder<Potion, Potion> cold_resist_type = type("cold_resist_type", cold_resist, ModConfig.server.miscellaneous.resistancePotionDurationShort);
   public static final DeferredHolder<Potion, Potion> long_cold_resist_type = type("long_cold_resist_type", cold_resist, ModConfig.server.miscellaneous.resistancePotionDurationLong);
   public static final DeferredHolder<Potion, Potion> heat_resist_type = type("heat_resist_type", heat_resist, ModConfig.server.miscellaneous.resistancePotionDurationShort);
   public static final DeferredHolder<Potion, Potion> long_heat_resist_type = type("long_heat_resist_type", heat_resist, ModConfig.server.miscellaneous.resistancePotionDurationLong);

   private static DeferredHolder<Potion, Potion> type(String name, Holder<MobEffect> effect, int duration) {
      DeferredHolder<Potion, Potion> h = POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect, duration)));
      potionTypes.put(name, h);
      return h;
   }

   public static void register(IEventBus bus) {
      EFFECTS.register(bus);
      POTIONS.register(bus);
   }
}
