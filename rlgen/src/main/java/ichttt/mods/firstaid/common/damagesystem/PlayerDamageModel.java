package ichttt.mods.firstaid.common.damagesystem;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.util.CommonUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** 1.12 AbstractPlayerDamageModel + PlayerDamageModel (the firstaid player capability, here an attachment). */
public class PlayerDamageModel implements Iterable<DamageablePart>, INBTSerializable<CompoundTag> {
   public final DamageablePart HEAD;
   public final DamageablePart LEFT_ARM;
   public final DamageablePart LEFT_LEG;
   public final DamageablePart LEFT_FOOT;
   public final DamageablePart BODY;
   public final DamageablePart RIGHT_ARM;
   public final DamageablePart RIGHT_LEG;
   public final DamageablePart RIGHT_FOOT;
   public boolean hasTutorial;
   private final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
   private int morphineTicksLeft = 0;
   private int sleepBlockTicks = 0;
   private float prevHealthCurrent = -1.0F;
   private float prevScaleFactor;
   private final boolean noCritical;
   private boolean needsMorphineUpdate = false;
   private int resyncTimer = -1;

   public static PlayerDamageModel create() {
      FirstAidRegistryImpl registry = FirstAidRegistryImpl.INSTANCE;
      return new PlayerDamageModel(registry.getDebuffs(EnumDebuffSlot.HEAD), registry.getDebuffs(EnumDebuffSlot.BODY), registry.getDebuffs(EnumDebuffSlot.ARMS),
         registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET));
   }

   protected PlayerDamageModel(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
      FirstAidConfig.DamageSystem c = FirstAidConfig.damageSystem;
      this.HEAD = new DamageablePart(c.maxHealthHead, c.causeDeathHead, EnumPlayerPart.HEAD, headDebuffs);
      this.LEFT_ARM = new DamageablePart(c.maxHealthLeftArm, false, EnumPlayerPart.LEFT_ARM, armDebuffs);
      this.LEFT_LEG = new DamageablePart(c.maxHealthLeftLeg, false, EnumPlayerPart.LEFT_LEG, legFootDebuffs);
      this.LEFT_FOOT = new DamageablePart(c.maxHealthLeftFoot, false, EnumPlayerPart.LEFT_FOOT, legFootDebuffs);
      this.BODY = new DamageablePart(c.maxHealthBody, c.causeDeathBody, EnumPlayerPart.BODY, bodyDebuffs);
      this.RIGHT_ARM = new DamageablePart(c.maxHealthRightArm, false, EnumPlayerPart.RIGHT_ARM, armDebuffs);
      this.RIGHT_LEG = new DamageablePart(c.maxHealthRightLeg, false, EnumPlayerPart.RIGHT_LEG, legFootDebuffs);
      this.RIGHT_FOOT = new DamageablePart(c.maxHealthRightFoot, false, EnumPlayerPart.RIGHT_FOOT, legFootDebuffs);
      for (IDebuff debuff : armDebuffs) {
         this.sharedDebuffs.add((SharedDebuff)debuff);
      }
      for (IDebuff debuff : legFootDebuffs) {
         this.sharedDebuffs.add((SharedDebuff)debuff);
      }
      this.noCritical = !c.causeDeathBody && !c.causeDeathHead;
   }

   public DamageablePart getFromEnum(EnumPlayerPart part) {
      return switch (part) {
         case HEAD -> this.HEAD;
         case LEFT_ARM -> this.LEFT_ARM;
         case LEFT_LEG -> this.LEFT_LEG;
         case BODY -> this.BODY;
         case RIGHT_ARM -> this.RIGHT_ARM;
         case RIGHT_LEG -> this.RIGHT_LEG;
         case LEFT_FOOT -> this.LEFT_FOOT;
         case RIGHT_FOOT -> this.RIGHT_FOOT;
      };
   }

   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider p) {
      CompoundTag tag = new CompoundTag();
      tag.put("head", this.HEAD.serializeNBT(p));
      tag.put("leftArm", this.LEFT_ARM.serializeNBT(p));
      tag.put("leftLeg", this.LEFT_LEG.serializeNBT(p));
      tag.put("leftFoot", this.LEFT_FOOT.serializeNBT(p));
      tag.put("body", this.BODY.serializeNBT(p));
      tag.put("rightArm", this.RIGHT_ARM.serializeNBT(p));
      tag.put("rightLeg", this.RIGHT_LEG.serializeNBT(p));
      tag.put("rightFoot", this.RIGHT_FOOT.serializeNBT(p));
      tag.putBoolean("hasTutorial", this.hasTutorial);
      return tag;
   }

   @Override
   public void deserializeNBT(HolderLookup.Provider p, CompoundTag nbt) {
      this.HEAD.deserializeNBT(p, nbt.getCompound("head"));
      this.LEFT_ARM.deserializeNBT(p, nbt.getCompound("leftArm"));
      this.LEFT_LEG.deserializeNBT(p, nbt.getCompound("leftLeg"));
      this.LEFT_FOOT.deserializeNBT(p, nbt.getCompound("leftFoot"));
      this.BODY.deserializeNBT(p, nbt.getCompound("body"));
      this.RIGHT_ARM.deserializeNBT(p, nbt.getCompound("rightArm"));
      this.RIGHT_LEG.deserializeNBT(p, nbt.getCompound("rightLeg"));
      this.RIGHT_FOOT.deserializeNBT(p, nbt.getCompound("rightFoot"));
      if (nbt.contains("morphineTicks")) {
         this.morphineTicksLeft = nbt.getInt("morphineTicks");
         this.needsMorphineUpdate = true;
      }
      if (nbt.contains("hasTutorial")) {
         this.hasTutorial = nbt.getBoolean("hasTutorial");
      }
   }

   public void tick(Level world, Player player) {
      if (this.isDead(player)) {
         return;
      }
      if (this.sleepBlockTicks > 0) {
         this.sleepBlockTicks--;
      } else if (this.sleepBlockTicks < 0) {
         throw new RuntimeException("Negative sleepBlockTicks " + this.sleepBlockTicks);
      }
      float newCurrentHealth = this.calculateNewCurrentHealth(player);
      if (Float.isNaN(newCurrentHealth)) {
         FirstAid.LOGGER.warn("New current health is not a number, setting it to 0!");
         newCurrentHealth = 0.0F;
      }
      if (newCurrentHealth <= 0.0F) {
         FirstAid.LOGGER.error("Got {} health left, but isn't marked as dead!", newCurrentHealth);
         return;
      }
      if (!world.isClientSide && this.resyncTimer != -1) {
         this.resyncTimer--;
         if (this.resyncTimer == 0) {
            this.resyncTimer = -1;
            FirstAidNetwork.syncDamageModel((ServerPlayer)player, this, true);
         }
      }
      if (Float.isInfinite(newCurrentHealth)) {
         FirstAid.LOGGER.error("Error calculating current health: Value was infinite");
      } else {
         if (newCurrentHealth != this.prevHealthCurrent) {
            FirstAid.setHealthImpl(player, newCurrentHealth);
         }
         this.prevHealthCurrent = newCurrentHealth;
      }
      this.runScaleLogic(player);
      if (this.needsMorphineUpdate) {
         player.addEffect(new MobEffectInstance(FirstAid.MORPHINE, this.morphineTicksLeft, 0, false, false));
      }
      MobEffectInstance morphine = player.getEffect(FirstAid.MORPHINE);
      if (!this.needsMorphineUpdate) {
         this.morphineTicksLeft = morphine == null ? 0 : morphine.getDuration();
      }
      this.needsMorphineUpdate = false;
      for (DamageablePart part : this) {
         part.tick(world, player, morphine == null);
      }
      if (morphine == null) {
         this.sharedDebuffs.forEach(d -> d.tick(player));
      }
   }

   public static int getRandMorphineDuration() {
      return FirstAid.RAND.nextInt(5) * 20 * 15 + 4200;
   }

   public void applyMorphine(Player player) {
      player.addEffect(new MobEffectInstance(FirstAid.MORPHINE, getRandMorphineDuration(), 0, false, false));
   }

   public int getMorphineTicks() {
      return this.morphineTicksLeft;
   }

   @Override
   public Iterator<DamageablePart> iterator() {
      return new Iterator<>() {
         private byte count = 1;

         @Override
         public boolean hasNext() {
            return this.count <= 8;
         }

         @Override
         public DamageablePart next() {
            if (this.count > 8) {
               throw new NoSuchElementException();
            }
            return PlayerDamageModel.this.getFromEnum(EnumPlayerPart.fromID(this.count++));
         }
      };
   }

   public float getCurrentHealth() {
      float currentHealth = 0.0F;
      for (DamageablePart part : this) {
         currentHealth += part.currentHealth;
      }
      return currentHealth;
   }

   private float calculateNewCurrentHealth(Player player) {
      float currentHealth = 0.0F;
      FirstAidConfig.VanillaHealthCalculationMode mode = FirstAidConfig.vanillaHealthCalculation;
      if (this.noCritical) {
         mode = FirstAidConfig.VanillaHealthCalculationMode.AVERAGE_ALL;
      }
      switch (mode) {
         case AVERAGE_CRITICAL -> {
            int maxHealth = 0;
            for (DamageablePart part : this) {
               if (part.canCauseDeath) {
                  currentHealth += part.currentHealth;
                  maxHealth += part.getMaxHealth();
               }
            }
            currentHealth /= (float)maxHealth;
         }
         case MIN_CRITICAL -> {
            DamageablePart minimal = null;
            float lowest = Float.MAX_VALUE;
            for (DamageablePart part : this) {
               if (part.canCauseDeath && part.currentHealth < lowest) {
                  minimal = part;
                  lowest = part.currentHealth;
               }
            }
            Objects.requireNonNull(minimal);
            currentHealth = minimal.currentHealth / (float)minimal.getMaxHealth();
         }
         case AVERAGE_ALL -> {
            for (DamageablePart part : this) {
               currentHealth += part.currentHealth;
            }
            currentHealth /= (float)this.getCurrentMaxHealth();
         }
         case CRITICAL_50_PERCENT_OTHER_50_PERCENT -> {
            float currentNormal = 0.0F;
            int maxNormal = 0;
            float currentCritical = 0.0F;
            int maxCritical = 0;
            for (DamageablePart part : this) {
               if (!part.canCauseDeath) {
                  currentNormal += part.currentHealth;
                  maxNormal += part.getMaxHealth();
               } else {
                  currentCritical += part.currentHealth;
                  maxCritical += part.getMaxHealth();
               }
            }
            currentHealth = (currentCritical / (float)maxCritical + currentNormal / (float)maxNormal) / 2.0F;
         }
      }
      return currentHealth * player.getMaxHealth();
   }

   public boolean isDead(@Nullable Player player) {
      if (player == null || player.isAlive() && !(player.getHealth() <= 0.0F)) {
         if (!this.noCritical) {
            for (DamageablePart part : this) {
               if (part.canCauseDeath && part.currentHealth <= 0.0F) {
                  return true;
               }
            }
            return false;
         } else {
            for (DamageablePart part : this) {
               if (part.currentHealth > 0.0F) {
                  return false;
               }
            }
            return true;
         }
      }
      return true;
   }

   public float getAbsorption() {
      float value = 0.0F;
      for (DamageablePart part : this) {
         value += part.getAbsorption();
      }
      return value;
   }

   public void setAbsorption(float absorption) {
      float newAbsorption = absorption / 8.0F;
      for (DamageablePart part : this) {
         part.setAbsorption(newAbsorption);
      }
   }

   public void sleepHeal(Player player) {
      if (this.sleepBlockTicks <= 0) {
         CommonUtils.healPlayerByPercentage(FirstAidConfig.externalHealing.sleepHealPercentage, this, player);
         this.sleepBlockTicks = 20;
      }
   }

   public int getCurrentMaxHealth() {
      int maxHealth = 0;
      for (DamageablePart part : this) {
         maxHealth += part.getMaxHealth();
      }
      return maxHealth;
   }

   public boolean isWaitingForHelp() {
      return false; // PlayerRevive is not in RLCraft
   }

   public void revivePlayer(Player player) {
      for (DamageablePart part : this) {
         if ((part.canCauseDeath || this.noCritical) && part.currentHealth <= 0.0F) {
            part.currentHealth = 1.0F;
         }
      }
      if (player instanceof ServerPlayer sp) {
         FirstAidNetwork.syncDamageModel(sp, this, true);
      }
   }

   /** 1.12 runScaleLogic: part max health follows player max health / 20, keeping even numbers. */
   public void runScaleLogic(Player player) {
      if (!FirstAidConfig.scaleMaxHealth) {
         return;
      }
      float globalFactor = player.getMaxHealth() / 20.0F;
      if (this.prevScaleFactor != globalFactor) {
         int reduced = 0;
         int added = 0;
         float expectedNewMaxHealth = 0.0F;
         int newMaxHealth = 0;
         for (DamageablePart part : this) {
            float floatResult = (float)part.initialMaxHealth * globalFactor;
            expectedNewMaxHealth += floatResult;
            int result = (int)floatResult;
            if (result % 2 == 1) {
               int partMaxHealth = part.getMaxHealth();
               if (part.currentHealth < (float)partMaxHealth && reduced < 4) {
                  result--;
                  reduced++;
               } else if (part.currentHealth > (float)partMaxHealth && added < 4) {
                  result++;
                  added++;
               } else if (reduced > added) {
                  result++;
                  added++;
               } else {
                  result--;
                  reduced++;
               }
            }
            newMaxHealth += result;
            part.setMaxHealth(result);
         }
         if (Math.abs(expectedNewMaxHealth - (float)newMaxHealth) >= 2.0F) {
            List<DamageablePart> prioList = new ArrayList<>();
            for (DamageablePart part : this) {
               prioList.add(part);
            }
            prioList.sort(Comparator.comparingInt(DamageablePart::getMaxHealth));
            for (DamageablePart part : prioList) {
               int maxHealth = part.getMaxHealth();
               if (expectedNewMaxHealth > (float)newMaxHealth) {
                  part.setMaxHealth(maxHealth + 2);
                  newMaxHealth += part.getMaxHealth() - maxHealth;
               } else if (expectedNewMaxHealth < (float)newMaxHealth) {
                  part.setMaxHealth(maxHealth - 2);
                  newMaxHealth -= maxHealth - part.getMaxHealth();
               }
               if (Math.abs(expectedNewMaxHealth - (float)newMaxHealth) < 2.0F) {
                  break;
               }
            }
         }
      }
      this.prevScaleFactor = globalFactor;
   }

   public void scheduleResync() {
      if (this.resyncTimer == -1) {
         this.resyncTimer = 3;
      } else {
         FirstAid.LOGGER.warn("resync already scheduled!");
      }
   }

   public boolean hasNoCritical() {
      return this.noCritical;
   }

   public int getMaxRenderSize() {
      int max = 0;
      for (DamageablePart part : this) {
         max = Math.max(max, (int)((float)((int)((float)part.getMaxHealth() + part.getAbsorption() + 0.9999F) + 1) / 2.0F * 9.0F));
      }
      return max;
   }
}
