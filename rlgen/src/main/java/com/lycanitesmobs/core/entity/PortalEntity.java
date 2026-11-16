package com.lycanitesmobs.core.entity;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.item.temp.ItemStaffSummoning;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;


public class PortalEntity extends BaseProjectileEntity {
   private double targetX;
   private double targetY;
   private double targetZ;
   public int summonAmount = 0;
   public int summonTick = 0;
   public int summonTime = 100;
   public double portalRange = 32.0;
   public int summonDuration = 1200;
   public Player shootingEntity;
   public Class summonClass;
   public ItemStaffSummoning portalItem;
   public String ownerName;
   public TileEntitySummoningPedestal summoningPedestal;
   protected static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);

   public PortalEntity(Level world) {
      super(world);
      this.setStats();
      this.isImmuneToFire = true;
   }

   public PortalEntity(Level world, Player shooter, Class summonClass, ItemStaffSummoning portalItem) {
      super(world, shooter);
      this.shootingEntity = shooter;
      this.summonClass = summonClass;
      this.portalItem = portalItem;
      this.setStats();
   }

   public PortalEntity(Level world, TileEntitySummoningPedestal summoningPedestal) {
      super(world);
      this.summoningPedestal = summoningPedestal;
      this.setStats();
      LycMotion.setPosX(this, (double)summoningPedestal.getPos().getX() + 0.5);
      LycMotion.setPosY(this, (double)summoningPedestal.getPos().getY() + 1.5);
      LycMotion.setPosZ(this, (double)summoningPedestal.getPos().getZ() + 0.5);
   }

   public void setStats() {
      this.entityName = "summoningportal";
      this.setProjectileScale(6.0F);
      this.moveToTarget();
      this.textureOffsetY = -0.5F;
      this.animationFrameMax = 7;
      this.movement = false;
      this.waterProof = true;
      this.lavaProof = true;
      builder.define(OWNER_NAME, "");
   }

   @Override
   public void tick() {
      if (this.shootingEntity != null || this.summoningPedestal != null) {
         this.projectileLife = 5;
      }

      super.tick();
      if (!this.level().isClientSide && this.summoningPedestal != null) {
         this.shootingEntity = this.summoningPedestal.getPlayer();
         this.summonClass = this.summoningPedestal.getSummonClass();
      }

      if (!this.level().isClientSide) {
         if (this.shootingEntity != null) {
            this.entityData.set(OWNER_NAME, this.shootingEntity.getName());
         } else if (this.summoningPedestal != null) {
            this.entityData.set(OWNER_NAME, this.summoningPedestal.getOwnerName());
         } else {
            this.entityData.set(OWNER_NAME, "");
         }
      } else {
         this.ownerName = (String)this.entityData.get(OWNER_NAME);
      }

      if (!this.level().isClientSide && !this.isRemoved()) {
         if (this.summoningPedestal != null) {
            if (this.summonClass == null) {
               this.discard();
               return;
            }
         } else {
            if (this.shootingEntity == null || !this.shootingEntity.isAlive() || this.portalItem == null) {
               this.discard();
               return;
            }

            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
            if (playerExt != null && playerExt.staffPortal != this) {
               this.discard();
               return;
            }
         }
      }

      this.moveToTarget();
      if (this.shootingEntity != null && this.summoningPedestal == null) {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
         if (playerExt != null && this.portalItem != null && ++this.summonTick >= this.portalItem.getRapidTime(null)) {
            this.summonDuration = this.portalItem.getSummonDuration();
            if (this.shootingEntity.getAbilities().instabuild) {
               this.summonAmount = this.summonAmount + this.portalItem.getSummonAmount();
            } else {
               float summonMultiplier = (float)(CreatureManager.getInstance().getCreature(this.summonClass).summonCost + this.portalItem.getSummonCostBoost())
                  * this.portalItem.getSummonCostMod();
               int summonCost = Math.round((float)playerExt.summonFocusCharge * summonMultiplier);
               if (playerExt.summonFocus >= summonCost && this.portalItem.getAdditionalCosts(this.shootingEntity)) {
                  playerExt.summonFocus -= summonCost;
                  this.summonAmount = this.summonAmount + this.portalItem.getSummonAmount();
               }
            }

            this.summonTick = 0;
         }
      } else if (this.summonClass != null && ++this.summonTick >= this.summonTime) {
         this.summonAmount = this.summoningPedestal.summonAmount;
         this.summonTick = 0;
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 32; i++) {
            double angle = Math.toRadians((double)(this.random.nextFloat() * 360.0F));
            float distance = this.random.nextFloat() * 0.5F;
            double x = (double)distance * Math.cos(angle) + Math.sin(angle);
            double z = (double)distance * Math.sin(angle) - Math.cos(angle);
            this.level()
               .addParticle(ParticleTypes.PORTAL, this.getX() + x, this.getY() + (double)(6.0F * this.random.nextFloat()) - 2.0, this.getZ() + z, ((double)this.random.nextFloat() - 0.5) * 4.0, 0.0, ((double)this.random.nextFloat() - 0.5) * 4.0);
         }
      }
   }

   public int summonCreatures() {
      if (this.level().isClientSide) {
         return 1;
      } else if (this.summonClass == null) {
         return 0;
      } else {
         for (int i = 0; i < this.summonAmount; i++) {
            Entity entity = null;

            try {
               entity = (Entity)this.summonClass.getConstructor(Level.class).newInstance(this.level());
            } catch (Exception var4) {
               LycanitesMobs.logWarning("", "A none Entity class type was passed to an EntityPortal, only entities can be summoned from portals!");
               var4.printStackTrace();
            }

            if (entity == null) {
               return 0;
            }

            entity.moveTo(this.getX(), this.getY(), this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof BaseCreatureEntity) {
               BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
               if (this.shootingEntity != null && this.summoningPedestal == null) {
                  entityCreature.setMinion(true);
                  if (entityCreature instanceof TameableCreatureEntity) {
                     ((TameableCreatureEntity)entityCreature).setPlayerOwner(this.shootingEntity);
                     if (this.portalItem != null) {
                        this.portalItem.applyMinionBehaviour((TameableCreatureEntity)entityCreature, this.shootingEntity);
                        this.portalItem.applyMinionEffects(entityCreature);
                     }
                  }
               } else if (this.summoningPedestal != null && this.summoningPedestal.getOwnerUUID() != null) {
                  entityCreature.setMinion(true);
                  entityCreature.summoningPedestal = this.summoningPedestal;
                  if (entityCreature instanceof TameableCreatureEntity) {
                     ((TameableCreatureEntity)entityCreature).setOwnerId(this.summoningPedestal.getOwnerUUID());
                     this.summoningPedestal.applyMinionBehaviour((TameableCreatureEntity)entityCreature);
                  }
               }

               if (this.summonDuration > 0) {
                  entityCreature.setTemporary(this.summonDuration);
               }

               if (this.shootingEntity != null) {
                  this.shootingEntity.addStat(ObjectManager.getStat(entityCreature.creatureInfo.getName() + ".summon"), 1);
               }
            }

            this.level().addFreshEntity(entity);
         }

         int amount = this.summonAmount;
         this.summonAmount = 0;
         return amount;
      }
   }

   @Override
   protected float getGravityVelocity() {
      return 0.0F;
   }

   public void moveToTarget() {
      if (this.shootingEntity != null && this.summoningPedestal == null) {
         Vec3 lookDirection = this.shootingEntity.getLookAngle();
         this.targetX = this.shootingEntity.getX() + lookDirection.x * this.portalRange;
         this.targetY = this.shootingEntity.getY() + (double)this.shootingEntity.getEyeHeight() + lookDirection.y * this.portalRange;
         this.targetZ = this.shootingEntity.getZ() + lookDirection.z * this.portalRange;
         HitResult target = Utilities.raytraceBlocks(
            this.level(), this.shootingEntity.getPositionEyes(1.0F), new Vec3(this.targetX, this.targetY, this.targetZ), false, true
         );
         if (target != null && target.getLocation() != null) {
            this.targetX = target.getLocation().x;
            this.targetY = target.getLocation().y;
            this.targetZ = target.getLocation().z;
         }

         LycMotion.setPosX(this, this.targetX);
         LycMotion.setPosY(this, this.targetY);
         LycMotion.setPosZ(this, this.targetZ);
      }
   }

   public double[] getFacingPosition(Entity entity, double distance) {
      double angle = Math.toRadians((double)this.getYRot());
      double xAmount = -Math.sin(angle);
      double zAmount = Math.cos(angle);
      return new double[]{entity.getX() + distance * xAmount, entity.getY(), entity.getZ() + distance * zAmount};
   }

   @Override
   protected void onImpact(HitResult movingObjectPos) {
   }

   @Override
   public ResourceLocation getTexture() {
      if (AssetManager.getTexture(this.entityName) == null) {
         AssetManager.addTexture(this.entityName, LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + ".png");
      }

      if (AssetManager.getTexture(this.entityName + "_client") == null) {
         AssetManager.addTexture(this.entityName + "_client", LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + "_client.png");
      }

      if (AssetManager.getTexture(this.entityName + "_player") == null) {
         AssetManager.addTexture(this.entityName + "_player", LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + "_player.png");
      }

      if (this.ownerName != null) {
         if (this.ownerName.equalsIgnoreCase(LycanitesMobs.proxy.getClientPlayer().getName())) {
            return AssetManager.getTexture(this.entityName + "_client");
         }

         if (!this.ownerName.equalsIgnoreCase("")) {
            return AssetManager.getTexture(this.entityName + "_player");
         }
      }

      return AssetManager.getTexture(this.entityName);
   }

   @Override
   public float getTextureOffsetY() {
      return this.getBbHeight() / 2.0F;
   }
}
