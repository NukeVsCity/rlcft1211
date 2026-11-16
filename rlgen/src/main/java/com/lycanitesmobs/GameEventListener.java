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


public class GameEventListener {
   WorldEventListener worldEventListener = new WorldEventListener(this);

   @SubscribeEvent
   public void registerBlocks(Register<Block> event) {
      ObjectManager.registerBlocks(event);
      ItemManager.getInstance().registerBlockOres();
   }

   @SubscribeEvent
   public void registerItems(Register<Item> event) {
      ObjectManager.registerItems(event);
      ItemManager.getInstance().registerItemOres();
   }

   @SubscribeEvent
   public void registerPotions(Register<Potion> event) {
      ObjectManager.registerPotions(event);
   }

   @SubscribeEvent
   public void registerEntities(Register<EntityEntry> event) {
      ObjectManager.registerSpecialEntities(event);
   }

   @SubscribeEvent
   public void onWorldLoading(Load event) {
      if (event.getWorld() != null) {
         ExtendedWorld.getForWorld(event.getWorld());
         event.getWorld().addEventListener(this.worldEventListener);
      }
   }

   @SubscribeEvent
   public void onWorldUnloading(Unload event) {
      ExtendedWorld.remove(event.getWorld());
   }

   @SubscribeEvent
   public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof LivingEntity) {
         event.addCapability(ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "IExtendedEntity"), new ICapabilitySerializable<CompoundTag>() {
            private final ExtendedEntity instance = (ExtendedEntity)LycanitesMobs.EXTENDED_ENTITY.getDefaultInstance();

            public boolean hasCapability(Capability<?> capability, Direction facing) {
               return capability == LycanitesMobs.EXTENDED_ENTITY;
            }

            public <T> T getCapability(Capability<T> capability, Direction facing) {
               return (T)(capability == LycanitesMobs.EXTENDED_ENTITY ? LycanitesMobs.EXTENDED_ENTITY.cast(this.instance) : null);
            }

            public CompoundTag serializeNBT() {
               return (CompoundTag)LycanitesMobs.EXTENDED_ENTITY.getStorage().writeNBT(LycanitesMobs.EXTENDED_ENTITY, this.instance, null);
            }

            public void deserializeNBT(CompoundTag nbt) {
               LycanitesMobs.EXTENDED_ENTITY.getStorage().readNBT(LycanitesMobs.EXTENDED_ENTITY, this.instance, null, nbt);
            }
         });
      }

      if (event.getObject() instanceof Player) {
         event.addCapability(ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "IExtendedPlayer"), new ICapabilitySerializable<CompoundTag>() {
            private final ExtendedPlayer instance = (ExtendedPlayer)LycanitesMobs.EXTENDED_PLAYER.getDefaultInstance();

            public boolean hasCapability(Capability<?> capability, Direction facing) {
               return capability == LycanitesMobs.EXTENDED_PLAYER;
            }

            public <T> T getCapability(Capability<T> capability, Direction facing) {
               return (T)(capability == LycanitesMobs.EXTENDED_PLAYER ? LycanitesMobs.EXTENDED_PLAYER.cast(this.instance) : null);
            }

            public CompoundTag serializeNBT() {
               return (CompoundTag)LycanitesMobs.EXTENDED_PLAYER.getStorage().writeNBT(LycanitesMobs.EXTENDED_PLAYER, this.instance, null);
            }

            public void deserializeNBT(CompoundTag nbt) {
               LycanitesMobs.EXTENDED_PLAYER.getStorage().readNBT(LycanitesMobs.EXTENDED_PLAYER, this.instance, null, nbt);
            }
         });
      }
   }

   @SubscribeEvent
   public void onPlayerClone(Clone event) {
      ExtendedPlayer extendedPlayerOld = ExtendedPlayer.getForPlayer(event.getOriginal());
      ExtendedPlayer extendedPlayerNew = ExtendedPlayer.getForPlayer(event.getEntityPlayer());
      CompoundTag nbt = new CompoundTag();
      extendedPlayerOld.writeNBT(nbt);
      extendedPlayerNew.readNBT(nbt);
   }

   @SubscribeEvent
   public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event) {
      ExtendedPlayer.getForPlayer(event.player).needsFullSync = true;
   }

   @SubscribeEvent
   public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
      ExtendedPlayer.getForPlayer(event.player).needsFullSync = true;
   }

   @SubscribeEvent
   public void onEntityConstructing(EntityConstructing event) {
      if (event.getEntity() != null && event.getEntity().level() != null && !event.getEntity().level().isClientSide) {
         if (!(event.getEntity() instanceof LivingEntity)
            && ExtendedEntity.FORCE_REMOVE_ENTITY_IDS != null
            && ExtendedEntity.FORCE_REMOVE_ENTITY_IDS.length > 0) {
            LycanitesMobs.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + event.getEntity().getName());

            for (String forceRemoveID : ExtendedEntity.FORCE_REMOVE_ENTITY_IDS) {
               if (forceRemoveID.equalsIgnoreCase(event.getEntity().getName())) {
                  event.getEntity().discard();
                  break;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingDeathEvent(LivingDeathEvent event) {
      LivingEntity entity = event.getEntityLiving();
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
   public void onEntityUpdate(LivingUpdateEvent event) {
      LivingEntity entity = event.getEntityLiving();
      if (entity != null) {
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
   public void onPlayerLeftClickEmpty(LeftClickEmpty event) {
      Player player = event.getEntityPlayer();
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
   public void onPlayerLeftClickBlock(LeftClickBlock event) {
      Player player = event.getEntityPlayer();
      if (player != null && !event.getSide().isClient()) {
         ItemStack itemStack = player.getItemInHand(event.getHand());
         Item item = itemStack.getItem();
         if (item instanceof ItemEquipment) {
            ((ItemEquipment)item).onItemLeftClick(event.getWorld(), player, event.getHand());
         }
      }
   }

   @SubscribeEvent
   public void onEntityInteract(EntityInteract event) {
      Player player = event.getEntityPlayer();
      Entity entity = event.getTarget();
      if (player != null && entity != null) {
         if (player.getItemInHand(event.getHand()) != null) {
            ItemStack itemStack = player.getItemInHand(event.getHand());
            Item item = itemStack.getItem();
            if (item instanceof ItemBase && ((ItemBase)item).onItemRightClickOnEntity(player, entity, itemStack) && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onAttackTarget(LivingSetAttackTargetEvent event) {
      Entity targetEntity = event.getTarget();
      if (event.getEntityLiving() != null && targetEntity != null) {
         if (!event.getEntityLiving().hasEffect(MobEffects.NIGHT_VISION) && targetEntity.isInvisible()) {
            if (event.isCancelable()) {
               event.setCanceled(true);
            }
         } else {
            if (event.getEntityLiving() instanceof Mob
               && targetEntity instanceof BaseCreatureEntity
               && !((BaseCreatureEntity)targetEntity).canBeTargetedBy(event.getEntityLiving())
               && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         if (event.getSource() != null && event.getEntityLiving() != null) {
            LivingEntity damagedEntity = event.getEntityLiving();
            ExtendedEntity damagedEntityExt = ExtendedEntity.getForEntity(damagedEntity);
            if (event.getSource() instanceof EntityDamageSource) {
               EntityDamageSource entityDamageSource = (EntityDamageSource)event.getSource();
               if (entityDamageSource.getEntity() != null && entityDamageSource.getEntity() instanceof LivingEntity) {
                  ExtendedEntity attackerExtendedEntity = ExtendedEntity.getForEntity((LivingEntity)entityDamageSource.getEntity());
                  if (attackerExtendedEntity != null) {
                     attackerExtendedEntity.setLastAttackedEntity(damagedEntity);
                  }
               }
            }

            if (damagedEntity.getVehicle() != null && damagedEntity.getVehicle() instanceof RideableCreatureEntity) {
               RideableCreatureEntity creatureRideable = (RideableCreatureEntity)event.getEntityLiving().getVehicle();
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
      Level world = event.getEntityLiving().level();
      if (ItemConfig.seasonalItemDropChance > 0.0 && (Utilities.isHalloween() || Utilities.isYuletide() || Utilities.isNewYear())) {
         boolean noSeaonalDrop = false;
         if (event.getEntityLiving() instanceof BaseCreatureEntity && ((BaseCreatureEntity)event.getEntityLiving()).isMinion()) {
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

         if (seasonalItem != null && !noSeaonalDrop && (double)event.getEntityLiving().getRandom().nextFloat() < ItemConfig.seasonalItemDropChance) {
            ItemStack dropStack = new ItemStack(seasonalItem, 1);
            EntityItemCustom entityItem = new EntityItemCustom(
               world, event.getEntityLiving().getX(), event.getEntityLiving().getY(), event.getEntityLiving().getZ(), dropStack
            );
            entityItem.setPickUpDelay(10);
            world.addFreshEntity(entityItem);
         }
      }
   }

   @SubscribeEvent
   public void onMountEntity(EntityMountEvent event) {
      if (event.isMounting()
         && event.getEntityBeingMounted() instanceof Boat
         && event.getEntityBeingMounted() instanceof AbstractMinecart
         && event.getEntityMounting() instanceof BaseCreatureEntity) {
         BaseCreatureEntity creatureEntity = (BaseCreatureEntity)event.getEntityMounting();
         if (creatureEntity.isBoss() || creatureEntity.width >= 2.0F || creatureEntity.height >= 2.0F) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onBucketFill(FillBucketEvent event) {
      Level world = event.getWorld();
      HitResult target = event.getTarget();
      if (target != null) {
         BlockPos pos = target.getBlockPos();
         Block block = world.getBlockState(pos).getBlock();
         Item bucket = ObjectManager.buckets.get(block);
         if (bucket != null && (Integer)world.getBlockState(pos).getValue(BlockLiquid.LEVEL) == 0) {
            world.removeBlock(pos, false);
         }

         if (bucket != null) {
            event.setFilledBucket(new ItemStack(bucket));
            event.setResult(Result.ALLOW);
         }
      }
   }

   @SubscribeEvent
   public void onBlockBreak(BreakEvent event) {
      if (event.getState() != null && event.getWorld() != null && !event.isCanceled() && !event.getWorld().isClientSide) {
         if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(event.getWorld());
            if (!(event.getState().getBlock() instanceof BlockFireBase) && extendedWorld.isBossNearby(new Vec3(event.getPos()))) {
               event.setCanceled(true);
               event.setResult(Result.DENY);
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
   public void onBlockPlace(PlaceEvent event) {
      if (event.getState() != null && event.getWorld() != null && !event.isCanceled()) {
         if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(event.getWorld());
            if (extendedWorld.isBossNearby(new Vec3(event.getPos()))) {
               event.setCanceled(true);
               event.setResult(Result.DENY);
               event.getPlayer().displayClientMessage(Component.literal(LanguageManager.translate("boss.block.protection.place")), true);
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
      Entity target = event.getRayTraceResult().entityHit;
      if (target instanceof BaseCreatureEntity) {
         BaseCreatureEntity targetCreature = (BaseCreatureEntity)target;
         if (event.getEntity() instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)event.getEntity();
            shooter = arrow.shootingEntity;
         }

         if (event.getEntity() instanceof ThrowableProjectile) {
            ThrowableProjectile throwable = (ThrowableProjectile)event.getEntity();
            shooter = throwable.getOwner();
         }

         if (shooter != null && !targetCreature.isDamageEntityApplicable(shooter)) {
            event.setCanceled(true);
         }
      }
   }
}
