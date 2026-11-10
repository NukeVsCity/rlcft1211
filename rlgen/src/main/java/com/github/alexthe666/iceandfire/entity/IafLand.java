package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Access to Ice and Fire items, blocks and sounds by their 1.12 registry names, so the land mobs do not depend on how the
 * core part exposes them in Java. Plus a few 1.12 world helpers (Material checks) the mobs use everywhere.
 */
public final class IafLand {
   private IafLand() {
   }

   public static final ResourceKey<DamageType> GORGON = ResourceKey.create(Registries.DAMAGE_TYPE, rl("gorgon"));

   public static ResourceLocation rl(String path) {
      return ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, path);
   }

   /** iceandfire:name, or AIR while that item is not registered. */
   public static Item item(String name) {
      return BuiltInRegistries.ITEM.get(rl(name));
   }

   public static boolean isItem(Item item, String name) {
      return item != Items.AIR && BuiltInRegistries.ITEM.getKey(item).equals(rl(name));
   }

   public static Block block(String name) {
      return BuiltInRegistries.BLOCK.get(rl(name));
   }

   /** iceandfire:name sound (registered by the core part; an unregistered event still plays from sounds.json). */
   public static SoundEvent sound(String name) {
      ResourceLocation id = rl(name);
      SoundEvent s = BuiltInRegistries.SOUND_EVENT.get(id);
      return s != null ? s : SoundEvent.createVariableRangeEvent(id);
   }

   public static DamageSource gorgon(Level level) {
      return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(GORGON));
   }

   /**
    * 1.12 Material.SAND: sand, red sand, gravel (BlockFalling), soul sand, concrete powder, Ice and Fire ash; plus the 1.21
    * suspicious sand/gravel and Defiled Lands sand (rlgen), which were sand material blocks too.
    */
   public static boolean isSandMaterial(BlockState state) {
      Block b = state.getBlock();
      return b instanceof net.minecraft.world.level.block.ColoredFallingBlock
         || b instanceof net.minecraft.world.level.block.ConcretePowderBlock
         || b instanceof net.minecraft.world.level.block.BrushableBlock
         || b == Blocks.SOUL_SAND
         || b == block("ash") && b != Blocks.AIR
         || "rlgen:sand_defiled".equals(BuiltInRegistries.BLOCK.getKey(b).toString());
   }

   /** 1.12 World.isMaterialInBB(bb, Material.SAND). */
   public static boolean isSandInBB(Level level, AABB bb) {
      int x0 = (int)Math.floor(bb.minX);
      int x1 = (int)Math.ceil(bb.maxX);
      int y0 = (int)Math.floor(bb.minY);
      int y1 = (int)Math.ceil(bb.maxY);
      int z0 = (int)Math.floor(bb.minZ);
      int z1 = (int)Math.ceil(bb.maxZ);
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for (int x = x0; x < x1; x++) {
         for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
               if (isSandMaterial(level.getBlockState(pos.set(x, y, z)))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static Holder<SoundEvent> holder(SoundEvent s) {
      return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s);
   }

   public static double width(Entity e) {
      return e.getBbWidth();
   }
}
