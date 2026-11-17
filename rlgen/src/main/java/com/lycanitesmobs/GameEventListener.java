package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.lycanitesmobs.core.LanguageManager;
import net.neoforged.bus.api.EventPriority;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.info.ItemConfig;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.ItemBase;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.network.MessagePlayerLeftClick;


/**
 * 1.12 GameEventListener on NeoForge 1.21 events. Registry events moved to LycEntities/ObjectManager; capabilities are
 * attachments (LycAttachments, player copy on death included); World.addEventListener -> EntityJoinLevelEvent.
 * LivingHurtEvent -> LivingIncomingDamageEvent (same point: before armor), LivingUpdateEvent -> EntityTickEvent.Pre.
 */
public class GameEventListener {
   WorldEventListener worldEventListener = new WorldEventListener(this);

   @SubscribeEvent
   public void onWorldLoading(net.neoforged.neoforge.event.level.LevelEvent.Load event) {
      if (event.getLevel() instanceof Level level) {
         ExtendedWorld.getForWorld(level);
      }
   }

   @SubscribeEvent
   public void onWorldUnloading(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
      if (event.getLevel() instanceof Level level) {
         ExtendedWorld.remove(level);
      }
   }

   /** 1.12 IWorldEventListener.onEntityAdded (GameEventListener added it to every world). */
   @SubscribeEvent
   public void onEntityJoin(EntityJoinLevelEvent event) {
      this.worldEventListener.onEntityAdded(event.getEntity());
   }

   @SubscribeEvent
   public void onEntityLeave(EntityLeaveLevelEvent event) {
      this.worldEventListener.onEntityRemoved(event.getEntity());
   }

   @SubscribeEvent
   public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
      ExtendedPlayer.getForPlayer(event.getEntity()).needsFullSync = true;
   }

   @SubscribeEvent
   public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
      ExtendedPlayer.getForPlayer(event.getEntity()).needsFullSync = true;
   }

   /** 1.12 EntityConstructing: force removed entity ids (non living entities). */
   @SubscribeEvent
   public void onEntityConstructing(EntityJoinLevelEvent event) {
      if (event.getEntity() != null && event.getEntity().level() != null && !event.getEntity().level().isClientSide) {
         if (!(event.getEntity() instanceof LivingEntity)
            && ExtendedEntity.FORCE_REMOVE_ENTITY_IDS != null
            && ExtendedEntity.FORCE_REMOVE_ENTITY_IDS.length > 0) {
            LycanitesMobs.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + event.getEntity().getName().getString());

            for (String forceRemoveID : ExtendedEntity.FORCE_REMOVE_ENTITY_IDS) {
               if (forceRemoveID.equalsIgnoreCase(event.getEntity().getName().getString())) {
                  event.setCanceled(true);
                  break;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingDeathEvent(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      if (entity != null) {
         ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
         if (extendedEntity != null) {
            extendedEntity.onDeath();
         }

         if (entity instanceof Player) {
            Player player = (Player)entity;
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer != null) {
               extendedPlayer.onDeath();
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityUpdate(EntityTickEvent.Pre event) {
      if (event.getEntity() instanceof LivingEntity entity) {
         ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
         if (extendedEntity != null) {
            extendedEntity.onUpdate();
         }

         if (entity instanceof Player) {
            Player player = (Player)entity;
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null) {
               playerExt.onUpdate();
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
      Player player = event.getEntity();
      if (player != null) {
         ItemStack itemStack = player.getItemInHand(event.getHand());
         Item item = itemStack.getItem();
         if (item instanceof ItemEquipment) {
            MessagePlayerLeftClick message = new MessagePlayerLeftClick();
            LycanitesMobs.packetHandler.sendToServer(message);
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
      Player player = event.getEntity();
      if (player != null && !event.getSide().isClient()) {
         ItemStack itemStack = player.getItemInHand(event.getHand());
         Item item = itemStack.getItem();
         if (item instanceof ItemEquipment) {
            ((ItemEquipment)item).onItemLeftClick(event.getLevel(), player, event.getHand());
         }
      }
   }

   @SubscribeEvent
   public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
      Player player = event.getEntity();
      Entity entity = event.getTarget();
      if (player != null && entity != null) {
         if (player.getItemInHand(event.getHand()) != null) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            Item item = itemStack.getItem();
            if (item instanceof ItemBase && ((ItemBase)item).onItemRightClickOnEntity(player, entity, itemStack)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onAttackTarget(LivingChangeTargetEvent event) {
      Entity targetEntity = event.getNewAboutToBeSetTarget();
      if (event.getEntity() != null && targetEntity != null) {
         if (!event.getEntity().hasEffect(MobEffects.NIGHT_VISION) && targetEntity.isInvisible()) {
            event.setCanceled(true);
         } else {
            if (event.getEntity() instanceof Mob
               && targetEntity instanceof BaseCreatureEntity
               && !((BaseCreatureEntity)targetEntity).canBeTargetedBy(event.getEntity())) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onLivingHurt(LivingIncomingDamageEvent event) {
      if (!event.isCanceled()) {
         if (event.getSource() != null && event.getEntity() != null) {
            LivingEntity damagedEntity = event.getEntity();
            ExtendedEntity damagedEntityExt = ExtendedEntity.getForEntity(damagedEntity);
            if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity) {
               ExtendedEntity attackerExtendedEntity = ExtendedEntity.getForEntity((LivingEntity)event.getSource().getEntity());
               if (attackerExtendedEntity != null) {
                  attackerExtendedEntity.setLastAttackedEntity(damagedEntity);
               }
            }

            if (damagedEntity.getVehicle() != null && damagedEntity.getVehicle() instanceof RideableCreatureEntity) {
               RideableCreatureEntity creatureRideable = (RideableCreatureEntity)event.getEntity().getVehicle();
               if (creatureRideable.isBlocking()) {
                  event.setAmount(0.0F);
                  event.setCanceled(true);
                  return;
               }

               if ("inWall".equals(event.getSource().getMsgId())) {
                  event.setAmount(0.0F);
                  event.setCanceled(true);
                  return;
               }

               if (!creatureRideable.isDamageTypeApplicable(event.getSource().getMsgId(), event.getSource(), event.getAmount())) {
                  event.setAmount(0.0F);
                  event.setCanceled(true);
                  return;
               }
            }

            if (damagedEntityExt != null && damagedEntityExt.isPickedUp() && "inWall".equals(event.getSource().getMsgId())) {
               event.setAmount(0.0F);
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingDrops(LivingDropsEvent event) {
      Level world = event.getEntity().level();
      if (ItemConfig.seasonalItemDropChance > 0.0 && (Utilities.isHalloween() || Utilities.isYuletide() || Utilities.isNewYear())) {
         boolean noSeaonalDrop = false;
         if (event.getEntity() instanceof BaseCreatureEntity && ((BaseCreatureEntity)event.getEntity()).isMinion()) {
            noSeaonalDrop = true;
         }

         Item seasonalItem = null;
         if (Utilities.isHalloween()) {
            seasonalItem = ObjectManager.getItem("halloweentreat");
         }

         if (Utilities.isYuletide()) {
            seasonalItem = ObjectManager.getItem("wintergift");
            if (Utilities.isYuletidePeak() && world.random.nextBoolean()) {
               seasonalItem = ObjectManager.getItem("wintergiftlarge");
            }
         }

         if (seasonalItem != null && !noSeaonalDrop && (double)event.getEntity().getRandom().nextFloat() < ItemConfig.seasonalItemDropChance) {
            ItemStack dropStack = new ItemStack(seasonalItem, 1);
            EntityItemCustom entityItem = new EntityItemCustom(world, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), dropStack);
            entityItem.setPickUpDelay(10);
            world.addFreshEntity(entityItem);
         }
      }
   }

   @SubscribeEvent
   public void onMountEntity(EntityMountEvent event) {
      // 1.12 condition (boat AND minecart) never matches; kept as is.
      if (event.isMounting()
         && event.getEntityBeingMounted() instanceof Boat
         && event.getEntityBeingMounted() instanceof AbstractMinecart
         && event.getEntityMounting() instanceof BaseCreatureEntity) {
         BaseCreatureEntity creatureEntity = (BaseCreatureEntity)event.getEntityMounting();
         if (creatureEntity.isBoss() || creatureEntity.getBbWidth() >= 2.0F || creatureEntity.getBbHeight() >= 2.0F) {
            event.setCanceled(true);
         }
      }
   }

   // 1.12 FillBucketEvent (fill a bucket from a Lycanites fluid source): 1.21 LiquidBlock.pickupBlock does it with the fluid's bucket.

   @SubscribeEvent
   public void onBlockBreak(BlockEvent.BreakEvent event) {
      if (event.getState() != null && event.getLevel() instanceof Level world && !event.isCanceled() && !world.isClientSide) {
         if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
            if (!(event.getState().getBlock() instanceof BlockFireBase) && extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
               event.setCanceled(true);
               event.getPlayer().displayClientMessage(Component.literal(LanguageManager.translate("boss.block.protection.break")), true);
               return;
            }
         }

         if (event.getPlayer() != null) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getPlayer());
            if (extendedPlayer == null) {
               return;
            }

            extendedPlayer.setJustBrokenBlock(event.getState());
         }
      }
   }

   @SubscribeEvent
   public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
      if (event.getState() != null && event.getLevel() instanceof Level world && !event.isCanceled()) {
         if (event.getEntity() instanceof Player player && !player.isCreative()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
            if (extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
               event.setCanceled(true);
               player.displayClientMessage(Component.literal(LanguageManager.translate("boss.block.protection.place")), true);
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityMount(EntityMountEvent event) {
      if (LycanitesMobs.config.getBool("Extras", "Disable Sneak Dismount", true)) {
         if (event.isDismounting() && event.getEntityMounting() instanceof Player && event.getEntityBeingMounted() instanceof RideableCreatureEntity) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((Player)event.getEntityMounting());
            if (extendedPlayer != null) {
               event.setCanceled(event.getEntityMounting().isShiftKeyDown() && !extendedPlayer.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT));
            }
         }
      }
   }

   @SubscribeEvent
   public void onProjectileImpact(ProjectileImpactEvent event) {
      Entity shooter = null;
      Entity target = event.getRayTraceResult() instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
      if (target instanceof BaseCreatureEntity) {
         BaseCreatureEntity targetCreature = (BaseCreatureEntity)target;
         if (event.getEntity() instanceof Projectile projectile) {
            shooter = projectile.getOwner();
         }

         if (shooter != null && !targetCreature.isDamageEntityApplicable(shooter)) {
            event.setCanceled(true);
         }
      }
   }
}
