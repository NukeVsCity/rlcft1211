package com.lycanitesmobs.core.item.equipment.features;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class HarvestEquipmentFeature extends EquipmentFeature {
   private static final Set<Block> SPADE_HARVEST = Sets.newHashSet(
      new Block[]{
         Blocks.CLAY,
         Blocks.DIRT,
         Blocks.FARMLAND,
         Blocks.GRASS_BLOCK,
         Blocks.GRAVEL,
         Blocks.MYCELIUM,
         Blocks.SAND,
         Blocks.SNOW_BLOCK,
         Blocks.SNOW_BLOCK,
         Blocks.SOUL_SAND,
         Blocks.DIRT_PATH,
         Blocks.WHITE_CONCRETE_POWDER
      }
   );
   private static final Set<Block> PICKAXE_HARVEST = Sets.newHashSet(
      new Block[]{
         Blocks.ACTIVATOR_RAIL,
         Blocks.COAL_ORE,
         Blocks.COBBLESTONE,
         Blocks.DETECTOR_RAIL,
         Blocks.DIAMOND_BLOCK,
         Blocks.DIAMOND_ORE,
         Blocks.SMOOTH_STONE,
         Blocks.GOLDEN_RAIL,
         Blocks.GOLD_BLOCK,
         Blocks.GOLD_ORE,
         Blocks.ICE,
         Blocks.IRON_BLOCK,
         Blocks.IRON_ORE,
         Blocks.LAPIS_BLOCK,
         Blocks.LAPIS_ORE,
         Blocks.REDSTONE_ORE,
         Blocks.MOSSY_COBBLESTONE,
         Blocks.NETHERRACK,
         Blocks.PACKED_ICE,
         Blocks.RAIL,
         Blocks.REDSTONE_ORE,
         Blocks.SANDSTONE,
         Blocks.RED_SANDSTONE,
         Blocks.STONE,
         Blocks.SMOOTH_STONE_SLAB,
         Blocks.STONE_BUTTON,
         Blocks.STONE_PRESSURE_PLATE
      }
   );
   private static final Set<Block> AXE_HARVEST = Sets.newHashSet(
      new Block[]{
         Blocks.OAK_PLANKS,
         Blocks.BOOKSHELF,
         Blocks.OAK_LOG,
         Blocks.LOG2,
         Blocks.CHEST,
         Blocks.PUMPKIN,
         Blocks.JACK_O_LANTERN,
         Blocks.MELON,
         Blocks.LADDER,
         Blocks.OAK_BUTTON,
         Blocks.OAK_PRESSURE_PLATE
      }
   );
   public String harvestType;
   public String harvestShape = "block";
   public float harvestSpeed = 1.0F;
   public int harvestLevel = 3;
   public Vec3i harvestRange = new Vec3i(0, 0, 0);

   @Override
   public void loadFromJSON(JsonObject json) {
      super.loadFromJSON(json);
      this.harvestType = json.get("harvestType").getAsString();
      if (json.has("harvestType")) {
         this.harvestType = json.get("harvestType").getAsString();
      }

      if (json.has("harvestSpeed")) {
         this.harvestSpeed = json.get("harvestSpeed").getAsFloat();
      }

      if (json.has("harvestLevel")) {
         this.harvestLevel = json.get("harvestLevel").getAsInt();
      }

      if (json.has("harvestShape")) {
         this.harvestShape = json.get("harvestShape").getAsString();
      }

      this.harvestRange = JSONHelper.getVec3i(json, "harvestRange");
   }

   @Override
   public String getDescription(ItemStack itemStack, int level) {
      return !this.isActive(itemStack, level)
         ? null
         : LanguageManager.translate("equipment.feature." + this.featureType) + " " + this.getSummary(itemStack, level);
   }

   @Override
   public String getSummary(ItemStack itemStack, int level) {
      if (!this.isActive(itemStack, level)) {
         return null;
      } else {
         String summary = LanguageManager.translate("equipment.harvest.type." + this.harvestType);
         if (this.harvestRange.distSqr(new Vec3i(0, 0, 0)) > 0.0) {
            summary = summary + " (" + LanguageManager.translate("equipment.harvest.shape." + this.harvestShape);
            summary = summary + " " + this.getHarvestRangeString(level);
            summary = summary + ")";
         }

         return summary;
      }
   }

   public String getHarvestRangeString(int level) {
      String harvestRangeString = "" + (this.harvestRange.getX() * 2 + 1);
      harvestRangeString = harvestRangeString + "x" + (this.harvestRange.getY() + 1);
      return harvestRangeString + "x" + (this.harvestRange.getZ() + 1);
   }

   public String getToolType() {
      return this.harvestType;
   }

   public boolean canHarvestBlock(BlockState blockState) {
      Block block = blockState.getBlock();
      Material material = Material.of(blockState);
      if (material == Material.IRON || material == Material.ANVIL || material == Material.ROCK || PICKAXE_HARVEST.contains(block)) {
         return this.harvestType.equalsIgnoreCase("pickaxe");
      } else if (material == Material.WOOD || AXE_HARVEST.contains(block)) {
         return this.harvestType.equalsIgnoreCase("axe");
      } else if (material == Material.PLANTS || material == Material.VINE) {
         return this.harvestType.equalsIgnoreCase("axe") || this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
      } else if (material == Material.WEB || material == Material.LEAVES) {
         return this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
      } else if (material == Material.CLOTH) {
         return this.harvestType.equalsIgnoreCase("shears");
      } else if (material == Material.GROUND
         || material == Material.SAND
         || material == Material.GRASS
         || material == Material.SNOW
         || material == Material.CRAFTED_SNOW
         || SPADE_HARVEST.contains(block)) {
         return this.harvestType.equalsIgnoreCase("shovel");
      } else if (material == Material.CORAL || material == Material.GOURD) {
         return this.harvestType.equalsIgnoreCase("sword");
      } else {
         return block == Blocks.TRIPWIRE ? this.harvestType.equalsIgnoreCase("shears") : false;
      }
   }

   public float getHarvestSpeed(BlockState blockState) {
      return !this.canHarvestBlock(blockState) ? 0.0F : this.harvestSpeed * this.getHarvestMultiplier(blockState);
   }

   public float getHarvestMultiplier(BlockState blockState) {
      Material material = Material.of(blockState);
      if (material == Material.WEB) {
         return 10.0F;
      } else {
         return (material == Material.LEAVES || material == Material.VINE) && this.harvestType.equalsIgnoreCase("shears") ? 10.0F : 1.0F;
      }
   }

   public void onBlockDestroyed(Level world, BlockState harvestedBlockState, BlockPos harvestedPos, LivingEntity livingEntity) {
      if (livingEntity != null && !livingEntity.isShiftKeyDown()) {
         Direction facingH = livingEntity.getDirection();
         Direction facingLat = facingH.getClockWise();
         Direction facing = facingH;
         if (livingEntity.getXRot() > 45.0F) {
            facing = Direction.DOWN;
         } else if (livingEntity.getXRot() < -45.0F) {
            facing = Direction.UP;
         }

         Vec3i[][] selectionRanges = new Vec3i[3][2];
         int lon = 0;
         int lat = 1;
         int vert = 2;
         int min = 0;
         int max = 1;
         selectionRanges[lon][min] = new Vec3i(
            Math.min(0, this.harvestRange.getZ() * facing.getStepX()),
            Math.min(0, this.harvestRange.getZ() * facing.getStepY()),
            Math.min(0, this.harvestRange.getZ() * facing.getStepZ())
         );
         selectionRanges[lon][max] = new Vec3i(
            Math.max(0, this.harvestRange.getZ() * facing.getStepX()),
            Math.max(0, this.harvestRange.getZ() * facing.getStepY()),
            Math.max(0, this.harvestRange.getZ() * facing.getStepZ())
         );
         selectionRanges[lat][min] = new Vec3i(
            this.harvestRange.getX() * -Math.abs(facingLat.getStepX()),
            this.harvestRange.getX() * -Math.abs(facingLat.getStepY()),
            this.harvestRange.getX() * -Math.abs(facingLat.getStepZ())
         );
         selectionRanges[lat][max] = new Vec3i(
            this.harvestRange.getX() * Math.abs(facingLat.getStepX()),
            this.harvestRange.getX() * Math.abs(facingLat.getStepY()),
            this.harvestRange.getX() * Math.abs(facingLat.getStepZ())
         );
         if (facing != Direction.DOWN && facing != Direction.UP) {
            int vertOffset = this.harvestRange.getY() != 0 ? -1 : 0;
            selectionRanges[vert][min] = new Vec3i(0, vertOffset, 0);
            selectionRanges[vert][max] = new Vec3i(0, this.harvestRange.getY() + vertOffset, 0);
         } else {
            selectionRanges[vert][min] = new Vec3i(
               (double)((float)(this.harvestRange.getY() * -Math.abs(facingH.getStepX())) * 0.5F),
               (double)((float)(this.harvestRange.getY() * -Math.abs(facingH.getStepY())) * 0.5F),
               (double)((float)(this.harvestRange.getY() * -Math.abs(facingH.getStepZ())) * 0.5F)
            );
            selectionRanges[vert][max] = new Vec3i(
               (double)((float)(this.harvestRange.getY() * Math.abs(facingH.getStepX())) * 0.5F),
               (double)((float)(this.harvestRange.getY() * Math.abs(facingH.getStepY())) * 0.5F),
               (double)((float)(this.harvestRange.getY() * Math.abs(facingH.getStepZ())) * 0.5F)
            );
         }

         if (!this.harvestShape.equalsIgnoreCase("block") && !this.harvestShape.equalsIgnoreCase("random")) {
            if (this.harvestShape.equalsIgnoreCase("cross")) {
               for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
                  for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
                     for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {
                        for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
                           for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
                              for (int latZ = selectionRanges[lat][min].getZ(); latZ <= selectionRanges[lat][max].getZ(); latZ++) {
                                 BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(latX, latY, latZ);
                                 if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
                                    world.destroyBlock(destroyPos, true);
                                 }
                              }
                           }
                        }

                        for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
                           for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
                              for (int vertZ = selectionRanges[vert][min].getZ(); vertZ <= selectionRanges[vert][max].getZ(); vertZ++) {
                                 BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(vertX, vertY, vertZ);
                                 if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
                                    world.destroyBlock(destroyPos, true);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            boolean random = this.harvestShape.equalsIgnoreCase("random");

            for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
               for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
                  for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {
                     for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
                        for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
                           for (int latZx = selectionRanges[lat][min].getZ(); latZx <= selectionRanges[lat][max].getZ(); latZx++) {
                              for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
                                 for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
                                    for (int vertZx = selectionRanges[vert][min].getZ();
                                       vertZx <= selectionRanges[vert][max].getZ();
                                       vertZx++
                                    ) {
                                       BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ)
                                          .offset(latX, latY, latZx)
                                          .offset(vertX, vertY, vertZx);
                                       if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)
                                          && (!random || world.random.nextBoolean())) {
                                          world.destroyBlock(destroyPos, true);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean shouldHarvestBlock(Level world, BlockState harvestedBlockState, BlockPos harvestedPos, BlockPos targetPos) {
      if (harvestedPos.equals(targetPos)) {
         return false;
      } else {
         BlockState targetBlockState = world.getBlockState(targetPos);
         if (targetBlockState.getBlock() != Blocks.REDSTONE_ORE && targetBlockState.getBlock() != Blocks.REDSTONE_ORE
            || harvestedBlockState.getBlock() != Blocks.REDSTONE_ORE && harvestedBlockState.getBlock() != Blocks.REDSTONE_ORE) {
            if (targetBlockState.getBlock() != harvestedBlockState.getBlock()) {
               return false;
            } else {
               return world.getBlockEntity(targetPos) != null ? false : this.canHarvestBlock(world.getBlockState(targetPos));
            }
         } else {
            return this.canHarvestBlock(world.getBlockState(targetPos));
         }
      }
   }

   public boolean onBlockUsed(Level world, Player player, BlockPos pos, ItemStack itemStack, Direction facing) {
      if (!"hoe".equals(this.harvestType)) {
         return false;
      } else if (!player.canPlayerEdit(pos.relative(facing), facing, itemStack)) {
         return false;
      } else {
         BlockState blockState = world.getBlockState(pos);
         int hook = ForgeEventFactory.onHoeUse(itemStack, player, world, pos);
         if (hook != 0) {
            return false;
         } else {
            BlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (facing != Direction.DOWN && world.isEmptyBlock(pos.above())) {
               if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH) {
                  this.hoeBlock(itemStack, player, world, pos, Blocks.FARMLAND.defaultBlockState());
                  return true;
               }

               if (block == Blocks.DIRT) {
                  switch ((DirtType)iblockstate.getValue(BlockDirt.VARIANT)) {
                     case DIRT:
                        this.hoeBlock(itemStack, player, world, pos, Blocks.FARMLAND.defaultBlockState());
                        return true;
                     case COARSE_DIRT:
                        this.hoeBlock(
                           itemStack, player, world, pos, Blocks.DIRT.defaultBlockState().setValue(BlockDirt.VARIANT, DirtType.DIRT)
                        );
                        return true;
                  }
               }
            }

            return false;
         }
      }
   }

   protected void hoeBlock(ItemStack stack, Player player, Level worldIn, BlockPos pos, BlockState state) {
      worldIn.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (!worldIn.isClientSide) {
         worldIn.setBlock(pos, state, 11);
      }
   }

   public boolean onEntityInteraction(Player player, LivingEntity entity, ItemStack itemStack) {
      if (!"shears".equals(this.harvestType) || player.level().isClientSide) {
         return false;
      } else if (!(entity instanceof IShearable)) {
         return false;
      } else {
         IShearable target = (IShearable)entity;
         BlockPos pos = LycPos.of(entity.getX(), entity.getY(), entity.getZ());
         if (target.isShearable(itemStack, entity.level(), pos)) {
            List<ItemStack> drops = target.onSheared(
               itemStack, entity.level(), pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemStack)
            );
            RandomSource rand = RandomSource.create();

            for (ItemStack stack : drops) {
               ItemEntity ent = entity.spawnAtLocation(stack, 1.0F);
               LycMotion.setY(ent, ent.getDeltaMovement().y + (double)(rand.nextFloat() * 0.05F));
               LycMotion.setX(ent, ent.getDeltaMovement().x + (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F));
               LycMotion.setZ(ent, ent.getDeltaMovement().z + (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F));
            }
         }

         return true;
      }
   }
}
