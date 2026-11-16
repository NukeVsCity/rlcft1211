package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.Variant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in for the 1.12 BaseCreatureEntity until lyc/server pushes the real one. Only the methods the
// client code calls; the test entities (core.StubTestEntities) cycle the attack cooldown so the attack animations show.
public class BaseCreatureEntity extends PathfinderMob {
   public CreatureInfo creatureInfo;
   public float onlyRenderTicks = -1.0F;
   public int attackPhaseMax = 0;
   public double sizeScale = 1.0;
   private final Subspecies subspecies = new Subspecies();

   public BaseCreatureEntity(EntityType<? extends BaseCreatureEntity> type, Level level) {
      super(type, level);
      this.creatureInfo = CreatureManager.getInstance().getCreature(EntityType.getKey(type).getPath());
      this.setNoAi(true);
      this.setPersistenceRequired();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
   }

   public double getRenderScale() {
      return this.sizeScale * this.creatureInfo.sizeScale;
   }

   public boolean isAttackOnCooldown() {
      return this.tickCount % 60 < 20;
   }

   public boolean hasAttackTarget() {
      return true;
   }

   public boolean hasPerchTarget() {
      return false;
   }

   public boolean isFlying() {
      return false;
   }

   public boolean hasPickupEntity() {
      return false;
   }

   public boolean isBlocking() {
      return false;
   }

   public boolean isTamed() {
      return false;
   }

   public boolean isSitting() {
      return false;
   }

   public float getStealth() {
      return 0.0F;
   }

   public int getAttackPhase() {
      return 0;
   }

   public int getColor() {
      return 0;
   }

   public boolean canEquip() {
      return false;
   }

   public String getEquipmentName(String type) {
      return null;
   }

   public Subspecies getSubspecies() {
      return this.subspecies;
   }

   public Variant getVariant() {
      return null;
   }

   public String getTextureName() {
      return this.creatureInfo.getName();
   }

   public ResourceLocation getTexture() {
      return this.getTexture("");
   }

   public ResourceLocation getTexture(String suffix) {
      String textureName = this.getTextureName();
      if (this.getSubspecies().name != null) {
         textureName = textureName + "_" + this.getSubspecies().name;
      }
      if (this.getVariant() != null) {
         textureName = textureName + "_" + this.getVariant().color;
      }
      if (!"".equals(suffix)) {
         textureName = textureName + "_" + suffix;
      }
      return ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/entity/" + textureName.toLowerCase() + ".png");
   }

   public ResourceLocation getEquipmentTexture(String equipmentName) {
      if (!this.canEquip()) {
         return this.getTexture();
      }
      if (this.getSubspecies() != null && this.getSubspecies().name != null) {
         equipmentName = this.getSubspecies().name + "_" + equipmentName;
      }
      return this.getSubTexture(equipmentName);
   }

   public ResourceLocation getSubTexture(String subName) {
      subName = subName.toLowerCase();
      String textureName = this.getTextureName() + "_" + subName;
      return ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/entity/" + textureName.toLowerCase() + ".png");
   }
}
