package com.lycanitesmobs.core;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

// STUB(lyc/server): temporary test entities (one per creature, no AI) so the client models can be checked before
// lyc/server pushes the real creature classes. Delete when they arrive.
public final class StubTestEntities {
   private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, LycanitesMobs.MODID);
   public static final Map<String, Supplier<EntityType<BaseCreatureEntity>>> TYPES = new LinkedHashMap<>();

   static {
      for (CreatureInfo info : CreatureManager.getInstance().creatures.values()) {
         TYPES.put(info.name, ENTITIES.register(info.name, () -> EntityType.Builder.<BaseCreatureEntity>of(factory(info.name), MobCategory.MONSTER)
            .sized((float)info.width, (float)info.height).clientTrackingRange(10).build(LycanitesMobs.MODID + ":" + info.name)));
      }
   }

   private StubTestEntities() {
   }

   private static EntityType.EntityFactory<BaseCreatureEntity> factory(String name) {
      return switch (name) {
         case "amalgalich" -> com.lycanitesmobs.core.entity.creature.EntityAmalgalich::new;
         case "asmodeus" -> com.lycanitesmobs.core.entity.creature.EntityAsmodeus::new;
         case "darkling" -> com.lycanitesmobs.core.entity.creature.EntityDarkling::new;
         case "spectre" -> com.lycanitesmobs.core.entity.creature.EntitySpectre::new;
         case "yale" -> com.lycanitesmobs.core.entity.creature.EntityYale::new;
         case "roa" -> com.lycanitesmobs.core.entity.creature.EntityRoa::new;
         case "thresher" -> com.lycanitesmobs.core.entity.creature.EntityThresher::new;
         default -> StubCreature::new;
      };
   }

   /** Rideable so the model casts of the real creatures (e.g. ModelIgnibus) work on the test entities. */
   public static class StubCreature extends com.lycanitesmobs.core.entity.RideableCreatureEntity {
      public StubCreature(EntityType<? extends BaseCreatureEntity> type, net.minecraft.world.level.Level level) {
         super(type, level);
      }
   }

   public static void register(IEventBus modBus) {
      ENTITIES.register(modBus);
      modBus.addListener(EntityAttributeCreationEvent.class, event -> {
         for (Supplier<EntityType<BaseCreatureEntity>> type : TYPES.values()) {
            event.put(type.get(), BaseCreatureEntity.createAttributes().build());
         }
      });
   }
}
