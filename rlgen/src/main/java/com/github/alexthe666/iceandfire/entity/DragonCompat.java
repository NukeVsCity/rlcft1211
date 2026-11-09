package com.github.alexthe666.iceandfire.entity;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21 replacements for the 1.12 API the dragon code relied on: block materials, Ice and Fire's damage sources and the
 * entity properties of other parts of the port. Items, blocks and sounds of iaf/core may be plain objects or deferred
 * holders, so they go through {@link #state} and {@link #snd}.
 */
public final class DragonCompat {
   private DragonCompat() {
   }

   /** 1.12 Material, as far as the dragon code tests it. */
   public enum Material {
      AIR, WATER, LAVA, FIRE, WOOD, ROCK, GROUND, GRASS, LEAVES, PLANTS, OTHER
   }

   public static Material material(BlockState state) {
      if (state.isAir()) {
         return Material.AIR;
      }
      Block block = state.getBlock();
      if (block == Blocks.WATER || block == Blocks.BUBBLE_COLUMN) {
         return Material.WATER;
      }
      if (block == Blocks.LAVA) {
         return Material.LAVA;
      }
      if (block instanceof BaseFireBlock) {
         return Material.FIRE;
      }
      if (block instanceof LeavesBlock || state.is(BlockTags.LEAVES)) {
         return Material.LEAVES;
      }
      if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM) {
         return Material.GRASS;
      }
      if (block instanceof BushBlock) {
         return Material.PLANTS;
      }
      NoteBlockInstrument instrument = state.instrument();
      if (instrument == NoteBlockInstrument.BASS) {
         return Material.WOOD;
      }
      if (instrument == NoteBlockInstrument.BASEDRUM) {
         return Material.ROCK;
      }
      if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.FARMLAND || block == Blocks.DIRT_PATH
         || block == Blocks.CLAY || block == Blocks.GRAVEL) {
         return Material.GROUND;
      }
      return Material.OTHER;
   }

   public static Material material(BlockGetter level, BlockPos pos) {
      return material(level.getBlockState(pos));
   }

   /** 1.12 BlockDirt (dirt, coarse dirt, podzol). */
   public static boolean isDirt(Block block) {
      return block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL;
   }

   public static BlockState state(Block block) {
      return block.defaultBlockState();
   }

   public static BlockState state(Supplier<? extends Block> block) {
      return block.get().defaultBlockState();
   }

   public static Block block(Block block) {
      return block;
   }

   public static Block block(Supplier<? extends Block> block) {
      return block.get();
   }

   public static SoundEvent snd(SoundEvent sound) {
      return sound;
   }

   public static SoundEvent snd(Supplier<? extends SoundEvent> sound) {
      return sound.get();
   }

   // ------------------------------------------------------------------ IceAndFire.dragon / dragonFire / dragonIce

   public static final ResourceKey<DamageType> DRAGON = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("iceandfire", "dragon"));
   public static final ResourceKey<DamageType> DRAGON_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("iceandfire", "dragon_fire"));
   public static final ResourceKey<DamageType> DRAGON_ICE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("iceandfire", "dragon_ice"));

   public static DamageSource dragonFire(Level level) {
      return level.damageSources().source(DRAGON_FIRE);
   }

   public static DamageSource dragonIce(Level level) {
      return level.damageSources().source(DRAGON_ICE);
   }

   // ------------------------------------------------------------------ properties owned by other parts of the port

   /** StoneEntityProperties.isStone (gorgon statues). STUB(iaf/mobs-land): wire to the stone attachment. */
   public static boolean isStone(Entity entity) {
      return false;
   }

   /** FrozenEntityProperties.setFrozenFor. STUB(iaf/core): wire to the frozen attachment. */
   public static void setFrozenFor(Entity entity, int ticks) {
   }

   /** MiscPlayerProperties.hasDismountedDragon (kept in the player's persistent data). */
   public static void setDismountedDragon(Player player, boolean value) {
      player.getPersistentData().putBoolean("iceandfire_hasDismountedDragon", value);
   }

   public static boolean hasDismountedDragon(Player player) {
      return player.getPersistentData().getBoolean("iceandfire_hasDismountedDragon");
   }

   /**
    * 1.12 new ItemStack(ModItems.dragon_skull, 1, meta) with the tags Stage, DragonType (always 0 in 1.7.1) and DragonAge.
    * STUB(iaf/core): metadata 0 = fire, 1 = ice is kept as the custom tag "SkullMeta" until the skull item is ported.
    */
   public static net.minecraft.world.item.ItemStack dragonSkull(int meta, int stage, int age) {
      net.minecraft.world.item.ItemStack skull = new net.minecraft.world.item.ItemStack(com.github.alexthe666.iceandfire.core.ModItems.dragon_skull, 1);
      net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
      tag.putInt("Stage", stage);
      tag.putInt("DragonType", 0);
      tag.putInt("DragonAge", age);
      tag.putInt("SkullMeta", meta);
      skull.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
      return skull;
   }

   /** 1.12 dragon horn: the whole dragon NBT goes into the horn's tag. STUB(iaf/core): the horn item reads it back. */
   public static void storeDragonInHorn(net.minecraft.world.item.ItemStack horn, Entity dragon, net.minecraft.nbt.CompoundTag dragonTag) {
      net.minecraft.nbt.CompoundTag tag = dragonTag.copy();
      tag.putString("id", net.minecraft.world.entity.EntityType.getKey(dragon.getType()).toString());
      horn.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
   }

   /** Dragon armor piece of a stack: 1.12 metadata 0 head, 1 neck, 2 body, 3 tail. STUB(iaf/core): "ArmorPart" custom tag. */
   public static int dragonArmorPart(net.minecraft.world.item.ItemStack stack) {
      net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
      return data == null ? 0 : data.copyTag().getInt("ArmorPart");
   }

   /** 1.12 Entity.getLookVec. */
   public static Vec3 lookVec(Entity entity) {
      return entity.getViewVector(1.0F);
   }

   /** 1.12 Entity.getPositionVector. */
   public static Vec3 pos(Entity entity) {
      return entity.position();
   }

   /** 1.12 EntityLivingBase.isEntityAlive. */
   public static boolean alive(LivingEntity entity) {
      return entity.isAlive();
   }
}
