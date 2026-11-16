package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.FearEntity;
import com.lycanitesmobs.core.info.CreatureGroup;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.network.MessageEntityVelocity;
import java.util.List;
import java.util.UUID;


public class PotionEffects {
   private static final UUID swiftswimmingMoveBoostUUID = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5e");
   private static final AttributeModifier swiftswimmingMoveBoost = new AttributeModifier(swiftswimmingMoveBoostUUID, "Swiftswimming Speed Boost", 1.0, 2)
      .setSaved(false);
   private static final UUID swiftswimmingMoveBoostUUID2 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5d");
   private static final AttributeModifier swiftswimmingMoveBoost2 = new AttributeModifier(swiftswimmingMoveBoostUUID2, "Swiftswimming Speed Boost 2", 2.0, 2)
      .setSaved(false);
   private static final UUID swiftswimmingMoveBoostUUID3 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5c");
   private static final AttributeModifier swiftswimmingMoveBoost3 = new AttributeModifier(swiftswimmingMoveBoostUUID3, "Swiftswimming Speed Boost 3", 3.0, 2)
      .setSaved(false);
   private static final UUID swiftswimmingMoveBoostUUID4 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5b");
   private static final AttributeModifier swiftswimmingMoveBoost4 = new AttributeModifier(swiftswimmingMoveBoostUUID4, "Swiftswimming Speed Boost 4", 4.0, 2)
      .setSaved(false);
   public boolean disableNausea = false;

   public void init(ConfigBase config) {
      config.setCategoryComment(
         "Potion Effects",
         "Here you can override each potion effect ID from the automatic ID, use 0 if you want it to stay automatic. Overrides should only be needed if you are running a lot of mods that add custom effects."
      );
      if (config.getBool("Potion Effects", "Enable Custom Effects", true, "Set to false to disable the custom potion effects.")) {
         ObjectManager.addEffect("paralysis", config, true, 16776960, false);
         ObjectManager.addEffect("penetration", config, true, 2236962, false);
         ObjectManager.addEffect("recklessness", config, true, 16711748, false);
         ObjectManager.addEffect("rage", config, true, 16729088, false);
         ObjectManager.addEffect("weight", config, true, 34, false);
         ObjectManager.addEffect("fear", config, true, 2228258, false);
         ObjectManager.addEffect("decay", config, true, 1114163, false);
         ObjectManager.addEffect("insomnia", config, true, 8738, false);
         ObjectManager.addEffect("instability", config, true, 17442, false);
         ObjectManager.addEffect("lifeleak", config, true, 22015, false);
         ObjectManager.addEffect("bleed", config, true, 16720418, false);
         ObjectManager.addEffect("plague", config, true, 2228326, false);
         ObjectManager.addEffect("aphagia", config, true, 16768477, false);
         ObjectManager.addEffect("smited", config, true, 14540287, false);
         ObjectManager.addEffect("smouldering", config, true, 14483456, false);
         ObjectManager.addEffect("leech", config, false, 65433, true);
         ObjectManager.addEffect("swiftswimming", config, false, 255, true);
         ObjectManager.addEffect("fallresist", config, false, 14548991, true);
         ObjectManager.addEffect("rejuvenation", config, false, 10092475, true);
         ObjectManager.addEffect("immunization", config, false, 6750139, true);
         ObjectManager.addEffect("cleansed", config, false, 6732799, true);
         ObjectManager.addEffect("repulsion", config, false, 12342062, true);
         ObjectManager.addEffect("heataura", config, false, 10053120, true);
         ObjectManager.addEffect("staticaura", config, false, 268154193, true);
         ObjectManager.addEffect("freezeaura", config, false, 5618687, true);
         ObjectManager.addEffect("envenom", config, false, 4513126, true);
         NeoForge.EVENT_BUS.register(this);
         AssetManager.addSound("effect_fear", LycanitesMobs.modInfo, "effect.fear");
      }

      this.disableNausea = config.getBool(
         "Potion Effects", "Disable Nausea Debuff", this.disableNausea, "Set to true to disable the vanilla nausea debuff on players."
      );
   }

   @SubscribeEvent
   public void onEntityUpdate(LivingUpdateEvent event) {
      LivingEntity entity = event.getEntityLiving();
      if (entity != null) {
         for (Object potionEffectObj : entity.getActiveEffects()) {
            if (potionEffectObj == null) {
               entity.removeAllEffects();
               LycanitesMobs.logWarning("EffectsSetup", "Found a null potion effect on entity: " + entity + " all effects have been removed from this entity.");
            }
         }

         if (entity.hasEffect(MobEffects.BLINDNESS) && entity.hasEffect(MobEffects.NIGHT_VISION)) {
            entity.removeEffect(MobEffects.BLINDNESS);
         }

         if (this.disableNausea && event.getEntityLiving() instanceof Player && entity.hasEffect(MobEffects.CONFUSION)) {
            entity.removeEffect(MobEffects.CONFUSION);
         }

         boolean invulnerable = false;
         if (entity instanceof Player) {
            Player player = (Player)entity;
            invulnerable = player.isCreative() || player.isSpectator();
         }

         PotionBase paralysis = ObjectManager.getEffect("paralysis");
         if (paralysis != null && !invulnerable && entity.hasEffect(paralysis)) {
            LycMotion.setX(entity, 0.0);
            if (entity.getDeltaMovement().y > 0.0) {
               LycMotion.setY(entity, 0.0);
            }

            LycMotion.setZ(entity, 0.0);
            entity.onGround = false;
         }

         PotionBase weight = ObjectManager.getEffect("weight");
         if (weight != null && !invulnerable && entity.hasEffect(weight) && !entity.hasEffect(MobEffects.DAMAGE_BOOST) && entity.getDeltaMovement().y > -0.2) {
            LycMotion.setY(entity, -0.2);
         }

         PotionBase fear = ObjectManager.getEffect("fear");
         if (fear != null && !entity.level().isClientSide && !invulnerable && !this.isBoss(entity) && entity.hasEffect(fear)) {
            ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
            if (extendedEntity != null && extendedEntity.fearEntity == null) {
               FearEntity fearEntity = new FearEntity(entity.level(), entity);
               entity.level().addFreshEntity(fearEntity);
               extendedEntity.fearEntity = fearEntity;
            }
         }

         PotionBase instability = ObjectManager.getEffect("instability");
         if (instability != null
            && !entity.level().isClientSide
            && !(entity instanceof IGroupBoss)
            && !invulnerable
            && entity.hasEffect(instability)
            && entity.level().random.nextDouble() <= 0.1) {
            double strength = (double)(1 + entity.getEffect(instability).getAmplifier());
            LycMotion.setX(entity, entity.getDeltaMovement().x + strength * (entity.level().random.nextDouble() - 0.5));
            LycMotion.setY(entity, entity.getDeltaMovement().y + strength * (entity.level().random.nextDouble() - 0.5));
            LycMotion.setZ(entity, entity.getDeltaMovement().z + strength * (entity.level().random.nextDouble() - 0.5));

            try {
               if (entity instanceof ServerPlayer) {
                  ServerPlayer player = (ServerPlayer)entity;
                  player.connection.sendPacket(new SPacketEntityVelocity(entity));
                  MessageEntityVelocity messageEntityVelocity = new MessageEntityVelocity(
                     player,
                     strength * (entity.level().random.nextDouble() - 0.5),
                     strength * (entity.level().random.nextDouble() - 0.5),
                     strength * (entity.level().random.nextDouble() - 0.5)
                  );
                  LycanitesMobs.packetHandler.sendToPlayer(messageEntityVelocity, player);
               }
            } catch (Exception var17) {
               LycanitesMobs.logWarning("", "Failed to create and send a network packet for instability velocity!");
               var17.printStackTrace();
            }
         }

         PotionBase plague = ObjectManager.getEffect("plague");
         if (plague != null && !entity.level().isClientSide && !invulnerable && entity.hasEffect(plague)) {
            int poisonAmplifier = entity.getEffect(plague).getAmplifier();
            int poisonDuration = entity.getEffect(plague).getDuration();
            if (entity.hasEffect(MobEffects.POISON)) {
               poisonAmplifier = Math.max(poisonAmplifier, entity.getEffect(MobEffects.POISON).getAmplifier());
               poisonDuration = Math.max(poisonDuration, entity.getEffect(MobEffects.POISON).getDuration());
            }

            entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, poisonAmplifier));
            if (entity.level().getGameTime() % 20L == 0L) {
               for (Object entityObj : this.getNearbyEntities(entity, LivingEntity.class, null, 2.0)) {
                  LivingEntity target = (LivingEntity)entityObj;
                  if (target != entity && !entity.isAlliedTo(target)) {
                     int amplifier = entity.getEffect(plague).getAmplifier();
                     int duration = entity.getEffect(plague).getDuration();
                     if (amplifier > 0) {
                        target.addEffect(new MobEffectInstance(plague, duration, amplifier - 1));
                     } else {
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
                     }
                  }
               }
            }
         }

         PotionBase smited = ObjectManager.getEffect("smited");
         if (smited != null
            && !entity.level().isClientSide
            && !invulnerable
            && entity.hasEffect(smited)
            && entity.level().getGameTime() % 20L == 0L) {
            float brightness = entity.getLightLevelDependentMagicValue();
            if (brightness > 0.5F && entity.level().canSeeSky(entity.blockPosition())) {
               entity.igniteForSeconds(4);
            }
         }

         PotionBase bleed = ObjectManager.getEffect("bleed");
         if (bleed != null
            && !entity.level().isClientSide
            && !invulnerable
            && entity.hasEffect(bleed)
            && entity.level().getGameTime() % 20L == 0L
            && !entity.isPassenger()
            && entity.prevDistanceWalkedModified != entity.distanceWalkedModified) {
            entity.hurt(LycDamage.src(DamageTypes.MAGIC), (float)(entity.getEffect(bleed).getAmplifier() + 1));
         }

         PotionBase smouldering = ObjectManager.getEffect("smouldering");
         if (smouldering != null
            && !entity.level().isClientSide
            && !invulnerable
            && entity.hasEffect(smouldering)
            && entity.level().getGameTime() % 20L == 0L) {
            entity.igniteForSeconds(4 + 4 * entity.getEffect(smouldering).getAmplifier());
         }

         PotionBase swiftswimming = ObjectManager.getEffect("swiftswimming");
         if (swiftswimming != null && entity instanceof Player) {
            Player player = (Player)entity;
            AttributeInstance movement = entity.getAttribute(LivingEntity.SWIM_SPEED);
            int amplifier = -1;
            if (entity.hasEffect(swiftswimming)) {
               amplifier = entity.getEffect(swiftswimming).getAmplifier();
            }

            if (amplifier == 0 && movement.getModifier(swiftswimmingMoveBoostUUID) == null) {
               movement.applyModifier(swiftswimmingMoveBoost);
            } else if (amplifier != 0 && movement.getModifier(swiftswimmingMoveBoostUUID) != null) {
               movement.removeModifier(swiftswimmingMoveBoost);
            }

            if (amplifier == 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) == null) {
               movement.applyModifier(swiftswimmingMoveBoost2);
            } else if (amplifier != 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) != null) {
               movement.removeModifier(swiftswimmingMoveBoost2);
            }

            if (amplifier == 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) == null) {
               movement.applyModifier(swiftswimmingMoveBoost3);
            } else if (amplifier != 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) != null) {
               movement.removeModifier(swiftswimmingMoveBoost3);
            }

            if (amplifier >= 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) == null) {
               movement.applyModifier(swiftswimmingMoveBoost4);
            } else if (amplifier < 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) != null) {
               movement.removeModifier(swiftswimmingMoveBoost4);
            }
         }

         PotionBase immunization = ObjectManager.getEffect("immunization");
         if (immunization != null && !entity.level().isClientSide && entity.hasEffect(ObjectManager.getEffect("immunization"))) {
            if (entity.hasEffect(MobEffects.POISON)) {
               entity.removeEffect(MobEffects.POISON);
            }

            if (entity.hasEffect(MobEffects.HUNGER)) {
               entity.removeEffect(MobEffects.HUNGER);
            }

            if (entity.hasEffect(MobEffects.WEAKNESS)) {
               entity.removeEffect(MobEffects.WEAKNESS);
            }

            if (entity.hasEffect(MobEffects.CONFUSION)) {
               entity.removeEffect(MobEffects.CONFUSION);
            }

            if (ObjectManager.getEffect("paralysis") != null && entity.hasEffect(ObjectManager.getEffect("paralysis"))) {
               entity.removeEffect(ObjectManager.getEffect("paralysis"));
            }
         }

         PotionBase cleansed = ObjectManager.getEffect("cleansed");
         if (ObjectManager.getEffect("cleansed") != null && !entity.level().isClientSide && entity.hasEffect(ObjectManager.getEffect("cleansed"))) {
            if (entity.hasEffect(MobEffects.WITHER)) {
               entity.removeEffect(MobEffects.WITHER);
            }

            if (entity.hasEffect(MobEffects.UNLUCK)) {
               entity.removeEffect(MobEffects.UNLUCK);
            }

            if (ObjectManager.getEffect("fear") != null && entity.hasEffect(ObjectManager.getEffect("fear"))) {
               entity.removeEffect(ObjectManager.getEffect("fear"));
            }

            if (ObjectManager.getEffect("insomnia") != null && entity.hasEffect(ObjectManager.getEffect("insomnia"))) {
               entity.removeEffect(ObjectManager.getEffect("insomnia"));
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityJump(LivingJumpEvent event) {
      LivingEntity entity = event.getEntityLiving();
      if (entity != null) {
         boolean invulnerable = false;
         if (entity instanceof Player) {
            Player player = (Player)entity;
            invulnerable = player.getAbilities().instabuild;
         }

         if (!invulnerable) {
            PotionBase paralysis = ObjectManager.getEffect("paralysis");
            if (paralysis != null && entity.hasEffect(paralysis) && event.isCancelable()) {
               event.setCanceled(true);
            }

            PotionBase weight = ObjectManager.getEffect("weight");
            if (weight != null && entity.hasEffect(weight) && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingAttack(LivingAttackEvent event) {
      if (!event.isCancelable() || !event.isCanceled()) {
         if (event.getEntityLiving() != null) {
            LivingEntity target = event.getEntityLiving();
            LivingEntity attacker = null;
            if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity) {
               attacker = (LivingEntity)event.getSource().getEntity();
            }

            if (attacker != null) {
               PotionBase lifeleak = ObjectManager.getEffect("lifeleak");
               if (lifeleak != null && !event.getEntityLiving().level().isClientSide && attacker.hasEffect(lifeleak)) {
                  if (event.isCancelable()) {
                     event.setCanceled(true);
                  }

                  target.heal(event.getAmount());
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.isCancelable() || !event.isCanceled()) {
         if (event.getEntityLiving() != null) {
            LivingEntity target = event.getEntityLiving();
            Entity attacker = event.getSource().getEntity();
            PotionBase fallresist = ObjectManager.getEffect("fallresist");
            if (fallresist != null && event.getEntityLiving().hasEffect(fallresist) && "fall".equals(event.getSource().getMsgId())) {
               event.setAmount(0.0F);
               event.setCanceled(true);
            }

            PotionBase penetration = ObjectManager.getEffect("penetration");
            if (penetration != null && event.getEntityLiving().hasEffect(penetration)) {
               float damage = event.getAmount();
               float multiplier = 0.25F * (float)(event.getEntityLiving().getEffect(penetration).getAmplifier() + 1);
               event.setAmount(damage + damage * multiplier);
            }

            PotionBase fear = ObjectManager.getEffect("fear");
            if (fear != null
               && !this.isBoss(event.getEntityLiving())
               && event.getEntityLiving().hasEffect(fear)
               && "inWall".equals(event.getSource().getMsgId())) {
               event.setAmount(0.0F);
               event.setCanceled(true);
            }

            PotionBase leech = ObjectManager.getEffect("leech");
            if (leech != null && event.getSource().getEntity() != null) {
               LivingEntity leechingEntity = null;
               if (event.getSource().getDirectEntity() instanceof LivingEntity) {
                  leechingEntity = (LivingEntity)event.getSource().getDirectEntity();
               } else if (event.getSource().getEntity() instanceof LivingEntity) {
                  leechingEntity = (LivingEntity)event.getSource().getEntity();
               }

               if (leechingEntity != null && leechingEntity.hasEffect(leech)) {
                  int leeching = leechingEntity.getEffect(leech).getAmplifier() + 1;
                  leechingEntity.heal((float)Math.max(leeching, 1));
               }
            }

            PotionBase repulsion = ObjectManager.getEffect("repulsion");
            if (repulsion != null) {
               boolean attackerIsBoss = attacker instanceof IGroupBoss;
               if (!attackerIsBoss && CreatureManager.getInstance().getCreatureGroup("boss") != null) {
                  attackerIsBoss = CreatureManager.getInstance().getCreatureGroup("boss").hasEntity(attacker);
               }

               if (attacker != null && !attackerIsBoss && target.hasEffect(repulsion)) {
                  float knockback = (float)(target.getEffect(repulsion).getAmplifier() + 2);
                  double xDist = attacker.position().x - target.position().x;
                  double zDist = attacker.position().z - target.position().z;
                  double xzDist = Math.max((double)(float)Math.sqrt(xDist * xDist + zDist * zDist), 0.01);
                  double motionCap = 10.0;
                  double xVel = xDist / xzDist * (double)knockback;
                  double zVel = zDist / xzDist * (double)knockback;
                  if (attacker.getDeltaMovement().x < motionCap
                     && attacker.getDeltaMovement().x > -motionCap
                     && attacker.getDeltaMovement().z < motionCap
                     && attacker.getDeltaMovement().z > -motionCap) {
                     attacker.push(xVel, 0.0, zVel);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityHeal(LivingHealEvent event) {
      LivingEntity entity = event.getEntityLiving();
      if (entity != null) {
         PotionBase rejuvenation = ObjectManager.getEffect("rejuvenation");
         if (rejuvenation != null && entity.hasEffect(rejuvenation)) {
            event.setAmount((float)Math.ceil((double)(event.getAmount() * (float)(2 * (1 + entity.getEffect(rejuvenation).getAmplifier())))));
         }

         PotionBase decay = ObjectManager.getEffect("decay");
         if (decay != null && entity.hasEffect(decay)) {
            event.setAmount((float)Math.floor((double)(event.getAmount() / (float)(2 * (1 + entity.getEffect(decay).getAmplifier())))));
         }
      }
   }

   @SubscribeEvent
   public void onSleep(PlayerSleepInBedEvent event) {
      Player player = event.getEntityPlayer();
      if (player != null && !player.level().isClientSide && !event.isCanceled()) {
         PotionBase insomnia = ObjectManager.getEffect("insomnia");
         if (insomnia != null && player.hasEffect(insomnia)) {
            event.setResult(SleepResult.NOT_SAFE);
         }
      }
   }

   @SubscribeEvent
   public void onLivingUseItem(LivingEntityUseItemEvent event) {
      if (!event.isCancelable() || !event.isCanceled()) {
         if (event.getEntityLiving() != null) {
            PotionBase aphagia = ObjectManager.getEffect("aphagia");
            if (aphagia != null
               && !event.getEntityLiving().level().isClientSide
               && event.getEntityLiving().hasEffect(aphagia)
               && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }

   public <T extends Entity> List<T> getNearbyEntities(Entity searchEntity, Class<? extends T> clazz, Class filterClass, double range) {
      return searchEntity.level()
         .getEntitiesOfClass(
            clazz,
            searchEntity.getBoundingBox().inflate(range, range, range),
            entity -> filterClass == null ? true : filterClass.isAssignableFrom(entity.getClass())
         );
   }

   public boolean isBoss(Entity entity) {
      if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
         return true;
      } else if (entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity creature = (BaseCreatureEntity)entity;
         return creature.isBoss();
      } else {
         CreatureGroup bossGroup = CreatureManager.getInstance().getCreatureGroup("boss");
         return bossGroup != null ? bossGroup.hasEntity(entity) : false;
      }
   }
}
