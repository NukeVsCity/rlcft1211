package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.StubTestEntities;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 1.12 RenderRegister.registerRenderFactories (RenderFactoryCreature: shadow size = creature width / 2). */
@EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RenderRegister {
   private RenderRegister() {
   }

   @SubscribeEvent
   public static void registerRenderFactories(EntityRenderersEvent.RegisterRenderers event) {
      // STUB(lyc/server): the test entity types until the real creature entity types exist
      for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
         Supplier<? extends EntityType<? extends com.lycanitesmobs.core.entity.BaseCreatureEntity>> type = StubTestEntities.TYPES.get(creatureInfo.getName());
         if (type == null) {
            continue;
         }
         String name = creatureInfo.getName();
         float shadow = (float)creatureInfo.width / 2.0F;
         event.registerEntityRenderer(type.get(), context -> {
            try {
               return new RenderCreature(name, context, shadow);
            } catch (Exception e) {
               LycanitesMobs.logWarning("", "An exception occurred rendering a creature model: " + e);
               return new net.minecraft.client.renderer.entity.NoopRenderer<>(context);
            }
         });
      }
   }
}
