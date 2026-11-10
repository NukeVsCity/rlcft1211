package com.github.alexthe666.iceandfire.entity.ai.legacy;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 net.minecraft.entity.ai.EntityAIWanderAvoidWater. */
public class EntityAIWanderAvoidWater extends EntityAIWander {
   protected final float probability;

   public EntityAIWanderAvoidWater(PathfinderMob creature, double speed) {
      this(creature, speed, 0.001F);
   }

   public EntityAIWanderAvoidWater(PathfinderMob creature, double speed, float probability) {
      super(creature, speed);
      this.probability = probability;
   }

   @Nullable
   @Override
   protected Vec3 getPosition() {
      if (this.entity.isInWater()) {
         Vec3 vec3d = RandomPositionGenerator.getLandPos(this.entity, 15, 7);
         return vec3d == null ? super.getPosition() : vec3d;
      } else {
         return this.entity.getRandom().nextFloat() >= this.probability ? RandomPositionGenerator.getLandPos(this.entity, 10, 7) : super.getPosition();
      }
   }
}
