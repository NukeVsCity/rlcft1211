package com.charles445.simpledifficulty.register;

import com.charles445.simpledifficulty.SimpleDifficulty;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The 1.12 client particles ParticleHeater / ParticleChiller (proxy.spawnClientParticle "HEATER"/"CHILLER") as particle types. */
public class SDParticles {
   public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, SimpleDifficulty.MODID);
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HEATER = PARTICLES.register("heater", () -> new SimpleParticleType(false));
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHILLER = PARTICLES.register("chiller", () -> new SimpleParticleType(false));

   public static void register(IEventBus bus) {
      PARTICLES.register(bus);
   }
}
