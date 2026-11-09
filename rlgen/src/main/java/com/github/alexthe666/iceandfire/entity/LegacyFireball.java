package com.github.alexthe666.iceandfire.entity;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/**
 * 1.12.2 net.minecraft.entity.projectile.EntityFireball with ProjectileHelper.forwardsRaycast/rotateTowardsMovement: fixed
 * acceleration vector, 0.95 drag, entity hits from ticksInAir 25 on even against the shooter. 1.21's AbstractHurtingProjectile
 * accelerates along the current motion instead, so the dragon projectiles keep the 1.12 physics through this class.
 */
public abstract class LegacyFireball extends Entity implements IEntityWithComplexSpawn {
   @Nullable
   public LivingEntity shootingEntity;
   @Nullable
   private UUID shootingEntityId;
   private int ticksAlive;
   private int ticksInAir;
   public double accelerationX;
   public double accelerationY;
   public double accelerationZ;
   private EntityDimensions size = EntityDimensions.scalable(1.0F, 1.0F);

   /** 1.12 RayTraceResult as far as onImpact uses it. */
   public static final class RayTraceResult {
      public enum Type {
         MISS, BLOCK, ENTITY
      }

      public final Type typeOfHit;
      @Nullable
      public final Entity entityHit;
      public final Vec3 hitVec;
      @Nullable
      public final BlockPos blockPos;

      RayTraceResult(Type type, @Nullable Entity entity, Vec3 hitVec, @Nullable BlockPos pos) {
         this.typeOfHit = type;
         this.entityHit = entity;
         this.hitVec = hitVec;
         this.blockPos = pos;
      }
   }

   protected LegacyFireball(EntityType<? extends LegacyFireball> type, Level world) {
      super(type, world);
      this.setSize(1.0F, 1.0F);
   }

   protected LegacyFireball(EntityType<? extends LegacyFireball> type, Level world, double x, double y, double z, double accelX, double accelY, double accelZ) {
      this(type, world);
      this.moveTo(x, y, z, this.getYRot(), this.getXRot());
      this.setPos(x, y, z);
      double d0 = (double)Mth.sqrt((float)(accelX * accelX + accelY * accelY + accelZ * accelZ));
      this.accelerationX = accelX / d0 * 0.1;
      this.accelerationY = accelY / d0 * 0.1;
      this.accelerationZ = accelZ / d0 * 0.1;
   }

   protected LegacyFireball(EntityType<? extends LegacyFireball> type, Level world, LivingEntity shooter, double accelX, double accelY, double accelZ) {
      this(type, world);
      this.setShooter(shooter);
      this.setSize(1.0F, 1.0F);
      this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
      this.setPos(this.getX(), this.getY(), this.getZ());
      this.setDeltaMovement(Vec3.ZERO);
      accelX += this.random.nextGaussian() * 0.4;
      accelY += this.random.nextGaussian() * 0.4;
      accelZ += this.random.nextGaussian() * 0.4;
      double d0 = (double)Mth.sqrt((float)(accelX * accelX + accelY * accelY + accelZ * accelZ));
      this.accelerationX = accelX / d0 * 0.1;
      this.accelerationY = accelY / d0 * 0.1;
      this.accelerationZ = accelZ / d0 * 0.1;
   }

   protected void setShooter(@Nullable LivingEntity shooter) {
      this.shootingEntity = shooter;
      this.shootingEntityId = shooter == null ? null : shooter.getUUID();
   }

   /** 1.12 Entity.setSize. */
   public void setSize(float width, float height) {
      this.size = EntityDimensions.scalable(width, height);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return this.size == null ? EntityDimensions.scalable(1.0F, 1.0F) : this.size;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double d0 = this.getBoundingBox().getSize() * 4.0;
      if (Double.isNaN(d0)) {
         d0 = 4.0;
      }

      d0 *= 64.0;
      return distance < d0 * d0;
   }

   protected boolean isFireballFiery() {
      return true;
   }

   protected ParticleOptions getParticleType() {
      return ParticleTypes.SMOKE;
   }

   protected float getMotionFactor() {
      return 0.95F;
   }

   protected abstract void onImpact(RayTraceResult result);

   /** Re-resolves the shooter after a reload (1.12 lost it). */
   protected void resolveShooter() {
      if (this.shootingEntity == null && this.shootingEntityId != null && this.level() instanceof ServerLevel serverLevel) {
         Entity entity = serverLevel.getEntity(this.shootingEntityId);
         if (entity instanceof LivingEntity living) {
            this.shootingEntity = living;
         }
      }
   }

   protected boolean shooterDead() {
      return this.shootingEntity != null && this.shootingEntity.isRemoved();
   }

   @Override
   public void tick() {
      this.onUpdate();
   }

   /** 1.12 EntityFireball.onUpdate. */
   public void onUpdate() {
      this.fireballUpdate();
   }

   protected final void fireballUpdate() {
      this.resolveShooter();
      if (this.level().isClientSide || !this.shooterDead() && this.level().hasChunkAt(this.blockPosition())) {
         super.tick();
         if (this.isFireballFiery()) {
            this.igniteForSeconds(1.0F);
         }

         this.ticksInAir++;
         RayTraceResult raytraceresult = this.forwardsRaycast(true, this.ticksInAir >= 25, this.shootingEntity);
         if (raytraceresult != null) {
            this.onImpact(raytraceresult);
         }

         this.moveAlong();
         this.level().addParticle(this.getParticleType(), this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
         this.setPos(this.getX(), this.getY(), this.getZ());
      } else {
         this.discard();
      }
   }

   /** The movement half of 1.12 EntityFireball.onUpdate (position += motion, rotate, accelerate, drag). */
   protected void moveAlong() {
      Vec3 motion = this.getDeltaMovement();
      this.setPosRaw(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
      this.rotateTowardsMovement(0.2F);
      float f = this.getMotionFactor();
      if (this.isInWater()) {
         for (int i = 0; i < 4; i++) {
            this.level()
               .addParticle(
                  ParticleTypes.BUBBLE, this.getX() - motion.x * 0.25, this.getY() - motion.y * 0.25, this.getZ() - motion.z * 0.25, motion.x, motion.y, motion.z
               );
         }

         f = 0.8F;
      }

      motion = motion.add(this.accelerationX, this.accelerationY, this.accelerationZ).scale((double)f);
      this.setDeltaMovement(motion);
   }

   /** 1.12 ProjectileHelper.rotateTowardsMovement. */
   protected void rotateTowardsMovement(float rotationSpeed) {
      Vec3 motion = this.getDeltaMovement();
      double d0 = motion.x;
      double d1 = motion.y;
      double d2 = motion.z;
      float f = Mth.sqrt((float)(d0 * d0 + d2 * d2));
      float yaw = (float)(Mth.atan2(d2, d0) * (180.0 / Math.PI)) + 90.0F;
      float pitch = (float)(Mth.atan2((double)f, d1) * (180.0 / Math.PI)) - 90.0F;
      float prevPitch = this.xRotO;
      float prevYaw = this.yRotO;
      while (pitch - prevPitch < -180.0F) {
         prevPitch -= 360.0F;
      }

      while (pitch - prevPitch >= 180.0F) {
         prevPitch += 360.0F;
      }

      while (yaw - prevYaw < -180.0F) {
         prevYaw -= 360.0F;
      }

      while (yaw - prevYaw >= 180.0F) {
         prevYaw += 360.0F;
      }

      this.xRotO = prevPitch;
      this.yRotO = prevYaw;
      this.setXRot(prevPitch + (pitch - prevPitch) * rotationSpeed);
      this.setYRot(prevYaw + (yaw - prevYaw) * rotationSpeed);
   }

   /** 1.12 ProjectileHelper.forwardsRaycast. */
   @Nullable
   protected RayTraceResult forwardsRaycast(boolean includeEntities, boolean ignoreExcludedEntity, @Nullable Entity excludedEntity) {
      Vec3 motion = this.getDeltaMovement();
      Vec3 start = this.position();
      Vec3 end = start.add(motion);
      RayTraceResult result = null;
      BlockHitResult blockHit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
      if (blockHit.getType() != HitResult.Type.MISS) {
         result = new RayTraceResult(RayTraceResult.Type.BLOCK, null, blockHit.getLocation(), blockHit.getBlockPos());
      }

      if (includeEntities) {
         if (result != null) {
            end = result.hitVec;
         }

         Entity hit = null;
         List<Entity> list = this.level().getEntities(this, this.getBoundingBox().expandTowards(motion).inflate(1.0));
         double d0 = 0.0;

         for (Entity entity : list) {
            if (entity.isPickable() && (ignoreExcludedEntity || !entity.is(excludedEntity)) && !entity.noPhysics) {
               AABB aabb = entity.getBoundingBox().inflate(0.30000001192092896);
               java.util.Optional<Vec3> intercept = aabb.clip(start, end);
               if (intercept.isPresent()) {
                  double d1 = start.distanceToSqr(intercept.get());
                  if (d1 < d0 || d0 == 0.0) {
                     hit = entity;
                     d0 = d1;
                  }
               }
            }
         }

         if (hit != null) {
            result = new RayTraceResult(RayTraceResult.Type.ENTITY, hit, hit.position(), null);
         }
      }

      return result;
   }

   public int getTicksInAir() {
      return this.ticksInAir;
   }

   protected void setTicksInAir(int ticks) {
      this.ticksInAir = ticks;
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      Vec3 motion = this.getDeltaMovement();
      tag.put("direction", this.doubleList(motion.x, motion.y, motion.z));
      tag.put("power", this.doubleList(this.accelerationX, this.accelerationY, this.accelerationZ));
      tag.putInt("life", this.ticksAlive);
      if (this.shootingEntityId != null) {
         tag.putUUID("Shooter", this.shootingEntityId);
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.contains("power", 9)) {
         ListTag list = tag.getList("power", 6);
         if (list.size() == 3) {
            this.accelerationX = list.getDouble(0);
            this.accelerationY = list.getDouble(1);
            this.accelerationZ = list.getDouble(2);
         }
      }

      this.ticksAlive = tag.getInt("life");
      if (tag.hasUUID("Shooter")) {
         this.shootingEntityId = tag.getUUID("Shooter");
      }
      if (tag.contains("direction", 9) && tag.getList("direction", 6).size() == 3) {
         ListTag list = tag.getList("direction", 6);
         this.setDeltaMovement(list.getDouble(0), list.getDouble(1), list.getDouble(2));
      } else {
         this.discard();
      }
   }

   private ListTag doubleList(double... values) {
      ListTag list = new ListTag();
      for (double d : values) {
         list.add(DoubleTag.valueOf(d));
      }
      return list;
   }

   @Override
   public boolean isPickable() {
      return true;
   }

   @Override
   public float getPickRadius() {
      return 1.0F;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.markHurt();
         if (source.getEntity() != null) {
            Vec3 look = source.getEntity().getViewVector(1.0F);
            this.setDeltaMovement(look);
            this.accelerationX = look.x * 0.1;
            this.accelerationY = look.y * 0.1;
            this.accelerationZ = look.z * 0.1;
            if (source.getEntity() instanceof LivingEntity living) {
               this.setShooter(living);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   @Override
   public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
      buffer.writeDouble(this.accelerationX);
      buffer.writeDouble(this.accelerationY);
      buffer.writeDouble(this.accelerationZ);
      Vec3 motion = this.getDeltaMovement();
      buffer.writeDouble(motion.x);
      buffer.writeDouble(motion.y);
      buffer.writeDouble(motion.z);
      buffer.writeFloat(this.size.width());
      buffer.writeFloat(this.size.height());
      buffer.writeInt(this.shootingEntity == null ? -1 : this.shootingEntity.getId());
   }

   @Override
   public void readSpawnData(RegistryFriendlyByteBuf buffer) {
      this.accelerationX = buffer.readDouble();
      this.accelerationY = buffer.readDouble();
      this.accelerationZ = buffer.readDouble();
      this.setDeltaMovement(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
      this.setSize(buffer.readFloat(), buffer.readFloat());
      int shooter = buffer.readInt();
      if (shooter >= 0 && this.level().getEntity(shooter) instanceof LivingEntity living) {
         this.shootingEntity = living;
      }
   }
}
