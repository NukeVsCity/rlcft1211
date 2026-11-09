package com.github.alexthe666.iceandfire.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

// STUB(iaf/core): the dragon sounds of Ice and Fire 1.7.1 ModSounds as direct (unregistered) sound events.
public final class ModSounds {
   private ModSounds() {
   }

   public static final SoundEvent DRAGON_HATCH = create("dragon_hatch");
   public static final SoundEvent FIREDRAGON_BREATH = create("firedragon_breath");
   public static final SoundEvent ICEDRAGON_BREATH = create("icedragon_breath");
   public static final SoundEvent FIREDRAGON_CHILD_IDLE = create("firedragon_child_idle");
   public static final SoundEvent FIREDRAGON_CHILD_HURT = create("firedragon_child_hurt");
   public static final SoundEvent FIREDRAGON_CHILD_DEATH = create("firedragon_child_death");
   public static final SoundEvent FIREDRAGON_CHILD_ROAR = create("firedragon_child_roar");
   public static final SoundEvent FIREDRAGON_TEEN_ROAR = create("firedragon_teen_roar");
   public static final SoundEvent FIREDRAGON_TEEN_IDLE = create("firedragon_teen_idle");
   public static final SoundEvent FIREDRAGON_TEEN_DEATH = create("firedragon_teen_death");
   public static final SoundEvent FIREDRAGON_TEEN_HURT = create("firedragon_teen_hurt");
   public static final SoundEvent FIREDRAGON_ADULT_ROAR = create("firedragon_adult_roar");
   public static final SoundEvent FIREDRAGON_ADULT_IDLE = create("firedragon_adult_idle");
   public static final SoundEvent FIREDRAGON_ADULT_DEATH = create("firedragon_adult_death");
   public static final SoundEvent FIREDRAGON_ADULT_HURT = create("firedragon_adult_hurt");
   public static final SoundEvent ICEDRAGON_CHILD_IDLE = create("icedragon_child_idle");
   public static final SoundEvent ICEDRAGON_CHILD_HURT = create("icedragon_child_hurt");
   public static final SoundEvent ICEDRAGON_CHILD_DEATH = create("icedragon_child_death");
   public static final SoundEvent ICEDRAGON_CHILD_ROAR = create("icedragon_child_roar");
   public static final SoundEvent ICEDRAGON_TEEN_ROAR = create("icedragon_teen_roar");
   public static final SoundEvent ICEDRAGON_TEEN_IDLE = create("icedragon_teen_idle");
   public static final SoundEvent ICEDRAGON_TEEN_DEATH = create("icedragon_teen_death");
   public static final SoundEvent ICEDRAGON_TEEN_HURT = create("icedragon_teen_hurt");
   public static final SoundEvent ICEDRAGON_ADULT_ROAR = create("icedragon_adult_roar");
   public static final SoundEvent ICEDRAGON_ADULT_IDLE = create("icedragon_adult_idle");
   public static final SoundEvent ICEDRAGON_ADULT_DEATH = create("icedragon_adult_death");
   public static final SoundEvent ICEDRAGON_ADULT_HURT = create("icedragon_adult_hurt");
   public static final SoundEvent DRAGON_FLIGHT = create("dragon_flight");

   private static SoundEvent create(String name) {
      return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("iceandfire", name));
   }
}
