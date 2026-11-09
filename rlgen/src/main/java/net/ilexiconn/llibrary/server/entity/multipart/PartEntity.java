package net.ilexiconn.llibrary.server.entity.multipart;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;

/**
 * LLibrary 1.7.20 PartEntity on top of NeoForge's part entities. Like 1.12 it is never added to the level: the parent
 * creates its parts, returns them from {@code getParts()} (and {@code isMultipartEntity() == true}, so NeoForge puts them in
 * the level's part map and they are found by ray traces / AABB queries, as 1.12 World.getEntitiesInAABBexcluding found
 * Entity.getParts()), and moves them every tick with {@link IMultipartEntity#onUpdateParts()} -> {@link #onUpdate()}.
 * Parts get ids from the global counter on each side, so a parent that needs client hits on parts to reach the server by
 * id must give its parts ids derived from its own in setId (see EnderDragon).
 */
public class PartEntity extends net.neoforged.neoforge.entity.PartEntity<Mob> {
   protected Mob parent;
   protected float radius;
   protected float angleYaw;
   protected float offsetY;
   protected float damageMultiplier;
   private EntityDimensions size;

   public PartEntity(Mob parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      super(parent);
      this.setSize(sizeX, sizeY);
      this.parent = parent;
      this.radius = radius;
      this.angleYaw = (angleYaw + 90.0F) * (float)(Math.PI / 180.0);
      this.offsetY = offsetY;
      this.damageMultiplier = damageMultiplier;
   }

   /** 1.12 Entity.setSize(width, height). */
   protected void setSize(float width, float height) {
      this.size = EntityDimensions.scalable(width, height);
      this.refreshDimensions();
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return this.size != null ? this.size : super.getDimensions(pose);
   }

   /** 1.12 onUpdate: follows the parent's renderYawOffset, pushes nearby entities, then the vanilla entity update. */
   public void onUpdate() {
      this.moveTo(
         this.parent.getX() + (double)this.radius * Math.cos((double)this.parent.yBodyRot * (Math.PI / 180.0) + (double)this.angleYaw),
         this.parent.getY() + (double)this.offsetY,
         this.parent.getZ() + (double)this.radius * Math.sin((double)this.parent.yBodyRot * (Math.PI / 180.0) + (double)this.angleYaw),
         this.getYRot(),
         this.getXRot()
      );
      if (!this.level().isClientSide) {
         this.collideWithNearbyEntities();
      }

      if (this.parent.isRemoved()) {
         this.discard();
      }

      super.tick();
   }

   @Override
   public void tick() {
      this.onUpdate();
   }

   @Override
   public boolean hurt(DamageSource source, float damage) {
      return this.parent.hurt(source, damage * this.damageMultiplier);
   }

   /** 1.12 isEntityEqual. */
   @Override
   public boolean is(Entity entity) {
      return this == entity || this.parent == entity;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag nbtTag) {
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag nbtTag) {
   }

   /** 1.12 canBeCollidedWith. */
   @Override
   public boolean isPickable() {
      return true;
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }

   /** 1.12 used getEntityBoundingBox().expand(0.2F, 0.0, 0.2F), which only grows towards +x/+z in 1.12. */
   public void collideWithNearbyEntities() {
      List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(0.2F, 0.0, 0.2F));
      entities.stream()
         .filter(entity -> entity != this.parent && !(entity instanceof PartEntity) && entity.isPushable())
         .forEach(entity -> entity.push(this.parent));
   }

   /** 1.12 setPositionAndRotationDirect is a no-op: parts are only moved by their parent. */
   @Override
   public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
   }
}
