package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The item hooks of iaf/core that need the land mobs: rotten egg, death worm egg, myrmex egg, stone statue, gorgon head. */
public final class LandHooks {
   private LandHooks() {
   }

   public static void install() {
      ModEntityHooks.throwRottenEgg = (level, player, stack) -> {
         if (!level.isClientSide) {
            EntityCockatriceEgg egg = new EntityCockatriceEgg(level, player);
            egg.setItem(stack);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(egg);
         }
      };
      ModEntityHooks.throwDeathwormEgg = (level, player, stack) -> {
         if (!level.isClientSide) {
            boolean giant = com.github.alexthe666.iceandfire.core.IafMeta.getMetadata(stack) == 1;
            EntityDeathWormEgg egg = new EntityDeathWormEgg(level, player, giant);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(egg);
         }
      };
      ModEntityHooks.placeMyrmexEgg = (level, jungle, caste, pos, player) -> {
         if (!level.isClientSide) {
            EntityMyrmexEgg egg = IafLandMobs.MYRMEX_EGG.get().create(level);
            egg.setJungle(jungle);
            egg.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            egg.onPlayerPlace(player);
            egg.setMyrmexCaste(caste);
            level.addFreshEntity(egg);
         }
      };
      ModEntityHooks.placeStoneStatue = LandHooks::placeStoneStatue;
      ModEntityHooks.useGorgonHead = LandHooks::useGorgonHead;
   }

   /** ItemStoneStatue.onItemUse */
   private static boolean placeStoneStatue(Level worldIn, Player player, ItemStack stack, BlockPos pos, Direction side) {
      if (side != Direction.UP) {
         return false;
      }
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
         CompoundTag tag = data.copyTag();
         float yaw = Mth.wrapDegrees(player.getYRot() + 180.0F);
         if (tag.getBoolean("IAFStoneStatueEntityPlayer")) {
            EntityStoneStatue statue = IafLandMobs.STONE_STATUE.get().create(worldIn);
            statue.moveTo((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5, player.getYRot(), 0.0F);
            statue.smallArms = true;
            statue.readAdditionalSaveData(tag);
            statue.setCrackAmount(0);
            statue.yRotO = yaw;
            statue.setYRot(yaw);
            statue.yHeadRot = yaw;
            statue.yBodyRot = yaw;
            statue.yBodyRotO = yaw;
            if (!worldIn.isClientSide) {
               worldIn.addFreshEntity(statue);
            }
         } else {
            Optional<EntityType<?>> type = EntityType.byString(tag.getString("IAFStoneStatueEntityType"));
            if (type.isEmpty()) {
               return true;
            }
            Entity entity = type.get().create(worldIn);
            if (entity instanceof Mob living) {
               living.moveTo((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5, player.getYRot(), 0.0F);
               living.readAdditionalSaveData(tag);
               living.setNoAi(true);
               living.yRotO = yaw;
               living.setYRot(yaw);
               living.yHeadRot = yaw;
               living.yBodyRot = yaw;
               living.yBodyRotO = yaw;
               if (!worldIn.isClientSide) {
                  worldIn.addFreshEntity(living);
                  LandUtils.setStone(living, true);
               }
            } else {
               return true;
            }
         }
         if (!player.isCreative()) {
            stack.shrink(1);
         }
      }
      return true;
   }

   /** ItemGorgonHead.onPlayerStoppedUsing: petrify the entity the player looks at (32 blocks). */
   private static void useGorgonHead(Level worldIn, Player entity, ItemStack stack, int timeLeft) {
      double dist = 32.0;
      Vec3 vec3d = entity.getEyePosition(1.0F);
      Vec3 vec3d1 = entity.getViewVector(1.0F);
      Vec3 vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
      Entity pointedEntity = null;
      List<Entity> list = worldIn.getEntities(entity, entity.getBoundingBox().expandTowards(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).inflate(1.0, 1.0, 1.0), e -> {
         boolean blindness = e instanceof LivingEntity && ((LivingEntity)e).hasEffect(MobEffects.BLINDNESS)
            || e instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues)e).canBeTurnedToStone();
         return e != null && !e.isSpectator() && e.isPickable() && !blindness
            && (e instanceof Player || e instanceof Mob && StoneEntityProperties.get(e) != null && !StoneEntityProperties.get(e).isStone);
      });
      double d2 = dist;

      for (Entity entity1 : list) {
         AABB axisalignedbb = entity1.getBoundingBox().inflate((double)entity1.getPickRadius());
         Optional<Vec3> raytraceresult = axisalignedbb.clip(vec3d, vec3d2);
         if (axisalignedbb.contains(vec3d)) {
            if (d2 >= 0.0) {
               pointedEntity = entity1;
               d2 = 0.0;
            }
         } else if (raytraceresult.isPresent()) {
            double d3 = vec3d.distanceTo(raytraceresult.get());
            if (d3 < d2 || d2 == 0.0) {
               if (entity1.getRootVehicle() != entity.getRootVehicle() || entity.canRiderInteract()) {
                  pointedEntity = entity1;
                  d2 = d3;
               } else if (d2 == 0.0) {
                  pointedEntity = entity1;
               }
            }
         }
      }

      if (pointedEntity != null && (pointedEntity instanceof Mob || pointedEntity instanceof Player)) {
         if (pointedEntity instanceof Player) {
            pointedEntity.playSound(IafLand.sound("gorgon_turn_stone"), 1.0F, 1.0F);
            if (!worldIn.isClientSide) {
               pointedEntity.hurt(IafLand.gorgon(worldIn), 2.1474836E9F);
               EntityStoneStatue statue = IafLandMobs.STONE_STATUE.get().create(worldIn);
               statue.moveTo(pointedEntity.getX(), pointedEntity.getY(), pointedEntity.getZ(), pointedEntity.getYRot(), pointedEntity.getXRot());
               statue.smallArms = true;
               worldIn.addFreshEntity(statue);
            }
         } else {
            if (!worldIn.isClientSide) {
               LandUtils.setStone(pointedEntity, true);
            } else if (StoneEntityProperties.get(pointedEntity) != null) {
               StoneEntityProperties.get(pointedEntity).isStone = true;
            }
            LandUtils.stopFlying(pointedEntity);
            LandUtils.dropArmor(pointedEntity);
         }

         if (pointedEntity instanceof EntityGorgon) {
            entity.playSound(IafLand.sound("gorgon_petrify"), 1.0F, 1.0F);
         } else {
            entity.playSound(IafLand.sound("gorgon_turn_stone"), 1.0F, 1.0F);
         }

         SoundEvent deathSound = deathSound((LivingEntity)pointedEntity);
         if (deathSound != null) {
            entity.playSound(deathSound, 1.0F, 1.0F);
         }

         if (!entity.isCreative()) {
            stack.shrink(1);
         }
      }

      stack.setDamageValue(0);
   }

   @Nullable
   private static SoundEvent deathSound(LivingEntity e) {
      try {
         java.lang.reflect.Method m = LivingEntity.class.getDeclaredMethod("getDeathSound");
         m.setAccessible(true);
         return (SoundEvent)m.invoke(e);
      } catch (ReflectiveOperationException ex) {
         return null;
      }
   }
}
