package com.github.alexthe666.iceandfire.core;

import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Hooks from the items/blocks (iaf/core) into the mob code (iaf/dragons, iaf/mobs-land, iaf/mobs-air-water). Every item that spawns or
 * talks to an Ice and Fire entity calls one of these; the branch that ports the entity sets the field from its own init (one line each,
 * e.g. {@code ModEntityHooks.spawnDragonEgg = (level, type, pos, player) -> { EntityDragonEgg egg = ...; }}). Until then the item
 * does nothing (the stack is not used up). The 1.12 code each hook replaces is quoted on the field.
 */
public final class ModEntityHooks {
   private ModEntityHooks() {
   }

   /** Entity type id check without the entity class, e.g. {@code is(target, "icedragon")} for 1.12 {@code instanceof EntityIceDragon}. */
   public static boolean is(@Nullable Entity entity, String iafName) {
      return entity != null && ResourceLocation.fromNamespaceAndPath("iceandfire", iafName).equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
   }

   /** 1.12 {@code instanceof EntityMyrmexBase}. */
   public static boolean isMyrmex(@Nullable Entity entity) {
      if (entity == null) {
         return false;
      }
      ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return id.getNamespace().equals("iceandfire") && id.getPath().startsWith("myrmex_") && !id.getPath().equals("myrmex_egg");
   }

   /** 1.12 {@code instanceof EntityDragonBase}. */
   public static boolean isDragon(@Nullable Entity entity) {
      return is(entity, "firedragon") || is(entity, "icedragon");
   }

   public interface DragonEggSpawner {
      /** ItemDragonEgg.onItemUse: new EntityDragonEgg, setType, setPosition(pos x+0.5, y+1, z+0.5), onPlayerPlace(player), spawn. */
      void spawn(Level level, EnumDragonEgg type, BlockPos pos, Player player);
   }

   public interface DragonSkullSpawner {
      /** ItemDragonSkull.onItemUse: new EntityDragonSkull, setType(meta), setStage, setDragonAge, location (x, y, z), setYaw(yaw), spawn. */
      void spawn(Level level, int type, int stage, int dragonAge, double x, double y, double z, float yaw);
   }

   public interface DragonHornRelease {
      /** ItemDragonHornActive.onPlayerStoppedUsing: new EntityFire/IceDragon at (pos x+0.5, y+1, z+0.5), readEntityFromNBT(tag) if not null,
       *  setFlying(false), setHovering(false), getNavigator().clearPath(), spawn (server only). */
      void release(Level level, boolean fire, @Nullable CompoundTag tag, BlockPos pos);
   }

   public interface Thrower {
      /** Throwable egg items: create the entity at the player, shoot(player, pitch, yaw, 0, 1.5, 1.0) and spawn it (server only). */
      void throwFrom(Level level, Player player, ItemStack stack);
   }

   public interface MyrmexEggPlacer {
      /** ItemMyrmexEgg.onItemUse: new EntityMyrmexEgg, setJungle, setPosition(pos x+0.5, y+1, z+0.5), onPlayerPlace(player),
       *  setMyrmexCaste(meta), spawn (server only). */
      void place(Level level, boolean jungle, int caste, BlockPos pos, Player player);
   }

   public interface ArrowFactory {
      /** The arrow entity for a bow/dispenser: shooter may be null (dispenser), then x/y/z are the position. */
      AbstractArrow create(Level level, @Nullable LivingEntity shooter, double x, double y, double z, ItemStack ammo);
   }

   public interface PixieRelease {
      /** TileEntityJar/TileEntityPixieHouse.releasePixie: new EntityPixie at (x, y, z) with random yaw, held item, setColor(color),
       *  ticksUntilHouseAI = 500, setTamed(tamed), setOwnerId(owner), spawn (server only). */
      void release(Level level, double x, double y, double z, ItemStack held, int color, boolean tamed, @Nullable java.util.UUID owner);
   }

   public interface StoneStatueSpawner {
      /** ItemStoneStatue.onItemUse: spawn the EntityStoneStatue stored in the stack NBT at the clicked face (mobs-land). Returns true if used. */
      boolean place(Level level, Player player, ItemStack stack, BlockPos pos, net.minecraft.core.Direction side);
   }

   public interface GorgonHeadUse {
      /** ItemGorgonHead.onPlayerStoppedUsing: turn the looked-at living entity to stone (mobs-land, gorgon stone statue mechanic). */
      void use(Level level, Player player, ItemStack stack, int timeLeft);
   }

   public interface IceEggHook {
      /** TileEntityEggInIce: spawnEgg (new EntityDragonEgg, setType, position (pos x+0.5, y+1, z+0.5), setOwnerId, spawn on server) or
       *  the hatch in update (new EntityIceDragon at (x+0.5, y+1, z+0.5), setVariant(type.ordinal() - 4), setGender(new Random().nextBoolean()),
       *  setTamed(true), setHunger(50), setOwnerId(owner), spawn on server). */
      void spawn(Level level, EnumDragonEgg type, BlockPos pos, @Nullable java.util.UUID owner);
   }

   public static volatile IceEggHook spawnEggFromIce;
   public static volatile IceEggHook hatchIceDragon;
   public static volatile DragonEggSpawner spawnDragonEgg;
   public static volatile DragonSkullSpawner spawnDragonSkull;
   public static volatile DragonHornRelease releaseDragonFromHorn;
   /** EntityHippogryphEgg(world, player, stack) (meta = EnumHippogryphTypes ordinal). */
   public static volatile Thrower throwHippogryphEgg;
   /** EntityCockatriceEgg(world, player). */
   public static volatile Thrower throwRottenEgg;
   /** EntityDeathWormEgg(world, player, meta == 1 /giant/). */
   public static volatile Thrower throwDeathwormEgg;
   public static volatile MyrmexEggPlacer placeMyrmexEgg;
   /** EntityDragonArrow (dragonbone_arrow), used by the dragonbone bow and the dispenser. */
   public static volatile ArrowFactory dragonArrow;
   /** EntityStymphalianArrow. */
   public static volatile ArrowFactory stymphalianArrow;
   /** EntityAmphithereArrow. */
   public static volatile ArrowFactory amphithereArrow;
   /** EntitySeaSerpentArrow. */
   public static volatile ArrowFactory seaSerpentArrow;
   public interface MyrmexStaffUse {
      /** ItemMyrmexStaff: right click (pos == null) or sneak-use on a block (pos, side): look up the hive by "HiveUUID", send
       *  MessageGetMyrmexHive / MessageSetMyrmexHiveNull, open GuiMyrmexStaff / GuiMyrmexAddRoom on the client (iaf/mobs-land). */
      void use(Level level, Player player, ItemStack staff, @Nullable BlockPos pos, @Nullable net.minecraft.core.Direction side);
   }

   public static volatile MyrmexStaffUse useMyrmexStaff;
   public static volatile PixieRelease releasePixie;
   public static volatile StoneStatueSpawner placeStoneStatue;
   public static volatile GorgonHeadUse useGorgonHead;
   public interface ParticleHook {
      void spawn(Level level, double x, double y, double z, float r, float g, float b);
   }

   /** CommonProxy.spawnParticle("if_pixie", ...): the pixie dust particle (client). Default: a dust particle of that colour until
    *  iaf/mobs-air-water sets the real ParticlePixieDust. */
   public static volatile ParticleHook pixieParticle = (level, x, y, z, r, g, b) ->
      level.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0F), x, y, z, 0.0, 0.0, 0.0);

   public static void spawnPixieParticle(Level level, double x, double y, double z, float r, float g, float b) {
      if (level.isClientSide && pixieParticle != null) {
         pixieParticle.spawn(level, x, y, z, r, g, b);
      }
   }

   /** EntityPixie.PARTICLE_RGB (pixie dust colours of the 5 pixie types); mobs-air-water may replace it. */
   public static float[][] PIXIE_PARTICLE_RGB = new float[][]{{1.0F, 0.752F, 0.792F}, {0.831F, 0.662F, 1.0F}, {0.513F, 0.843F, 1.0F},
      {0.654F, 0.909F, 0.615F}, {0.996F, 0.788F, 0.407F}};
}
