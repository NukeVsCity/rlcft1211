package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 1.12 RenderRegister.registerRenderFactories. 1.12 bound renderers to entity classes; 1.21 binds them to the entity types
 * (LycEntities): creatures get RenderCreature (shadow = width / 2; 1.12 first bound dummies to RenderNone and then
 * overwrote it with RenderCreature, so every creature uses RenderCreature), JSON projectiles RenderProjectileModel when
 * they have a model class (1.12 CustomProjectileModelEntity) else RenderProjectileSprite, the old sprite projectiles
 * RenderProjectileSprite, the old model projectiles RenderProjectileModel (which in 1.12 only draws CustomProjectileEntity
 * instances, so they are invisible), special entities RenderNone.
 */
@EventBusSubscriber(modid = LycanitesMobs.modid, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RenderRegister {
   private RenderRegister() {
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void bind(EntityRenderersEvent.RegisterRenderers event, EntityType<?> type, EntityRendererProvider<?> provider) {
      if (type != null) {
         event.registerEntityRenderer((EntityType)type, (EntityRendererProvider)provider);
      }
   }

   @SubscribeEvent
   public static void registerRenderFactories(EntityRenderersEvent.RegisterRenderers event) {
      for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
         if (creatureInfo.entityClass == null) {
            continue;
         }
         String name = creatureInfo.getName();
         float shadow = (float)creatureInfo.width / 2.0F;
         EntityRendererProvider<BaseCreatureEntity> provider = context -> {
            try {
               return new RenderCreature(name, context, shadow);
            } catch (Exception e) {
               LycanitesMobs.logWarning("", "An exception occurred rendering a creature model: " + e);
               return new NoopRenderer<>(context);
            }
         };
         bind(event, LycEntities.type(name), provider);
      }

      ProjectileManager projectileManager = ProjectileManager.getInstance();
      for (ProjectileInfo projectileInfo : projectileManager.projectiles.values()) {
         boolean hasModel = projectileInfo.modelClassName != null;
         bind(event, LycEntities.projectileType(projectileInfo),
            context -> hasModel ? new RenderProjectileModel(context) : new RenderProjectileSprite(context, com.lycanitesmobs.core.entity.CustomProjectileEntity.class));
      }

      for (String projectileName : projectileManager.oldSpriteProjectiles.keySet()) {
         Class<?> projectileClass = projectileManager.oldSpriteProjectiles.get(projectileName);
         bind(event, LycEntities.type(projectileName), context -> new RenderProjectileSprite(context, projectileClass));
      }

      for (String projectileName : projectileManager.oldModelProjectiles.keySet()) {
         bind(event, LycEntities.type(projectileName), context -> new RenderProjectileModel(context, projectileName));
      }

      for (String specialName : ObjectManager.specialEntities.keySet()) {
         bind(event, LycEntities.type(specialName), context -> new NoopRenderer<Entity>(context));
      }
   }
}
