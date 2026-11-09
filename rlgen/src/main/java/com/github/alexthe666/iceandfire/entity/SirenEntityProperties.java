package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * Ice and Fire 1.7.1 SirenEntityProperties (an llibrary EntityProperties of every living entity), as an attachment. CharmedBySiren and
 * SirenID are saved, singTime is not (as in 1.12). The server sends it to the charmed player every 20 ticks (getTrackingTime).
 */
public class SirenEntityProperties {
   public static final Codec<SirenEntityProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.BOOL.fieldOf("CharmedBySiren").forGetter(p -> p.isCharmed),
      Codec.INT.fieldOf("SirenID").forGetter(p -> p.sirenID)).apply(i, SirenEntityProperties::new));
   public static final Supplier<AttachmentType<SirenEntityProperties>> TYPE = IafRegistries.ATTACHMENT_TYPES.register("siren_properties",
      () -> AttachmentType.builder(SirenEntityProperties::new).serialize(CODEC).build());

   public boolean isCharmed;
   public int sirenID;
   public int singTime;

   public SirenEntityProperties() {
   }

   private SirenEntityProperties(boolean charmed, int sirenID) {
      this.isCharmed = charmed;
      this.sirenID = sirenID;
   }

   public static SirenEntityProperties get(LivingEntity entity) {
      return entity.getData(TYPE);
   }

   public int getTrackingTime() {
      return 20;
   }

   public EntitySiren getSiren(Level world) {
      Entity entity = world.getEntity(this.sirenID);
      return entity instanceof EntitySiren siren ? siren : null;
   }
}
