package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;

/** 1.12 ChickenEntityProperties (time until a chicken may lay a rotten egg), as a data attachment saved as "TimeUntilNextEgg". */
public class ChickenEntityProperties {
   public int timeUntilNextEgg = 1;
   private static Random rand = new Random();

   public static final Supplier<AttachmentType<ChickenEntityProperties>> TYPE = IafRegistries.ATTACHMENT_TYPES
      .register("chicken", () -> AttachmentType.builder(() -> {
         ChickenEntityProperties p = new ChickenEntityProperties();
         p.timeUntilNextEgg = p.generateTime();
         return p;
      }).serialize(Codec.INT.xmap(i -> {
         ChickenEntityProperties p = new ChickenEntityProperties();
         p.timeUntilNextEgg = i;
         return p;
      }, p -> p.timeUntilNextEgg)).build());

   public static void init() {
   }

   public int generateTime() {
      return rand.nextInt(6000) + 6000;
   }
}
