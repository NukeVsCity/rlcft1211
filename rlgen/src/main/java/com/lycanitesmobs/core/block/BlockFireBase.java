package com.lycanitesmobs.core.block;

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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;
import java.util.Random;


public class BlockFireBase extends BlockBase {
   public static final BooleanProperty PERMANENT = BooleanProperty.create("permanent");
   public static final BooleanProperty STATIC = BooleanProperty.create("static");
   public static final BooleanProperty NORTH = BooleanProperty.create("north");
   public static final BooleanProperty EAST = BooleanProperty.create("east");
   public static final BooleanProperty SOUTH = BooleanProperty.create("south");
   public static final BooleanProperty WEST = BooleanProperty.create("west");
   public static final BooleanProperty UPPER = BooleanProperty.create("up");
   public static final EntityDamageSource COLD_FIRE = EntityDamageSource.named("cold_fire").setDamageBypassesArmor().setMagicDamage();
   public boolean dieInRain = true;
   public boolean triggerTNT = true;
   public boolean tickRandomly = true;
   public int agingRate = 3;
   public float spreadChance = 1.0F;
   public boolean removeOnNoFireTick = false;

   public BlockFireBase(Material material, ModInfo group, String name) {
      super(material, group, name);
      this.registerDefaultState(this.stateDefinition.any()
            .setValue(AGE, 0)
            .setValue(PERMANENT, false)
            .setValue(STATIC, false)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UPPER, false)
      );
      this.removeOnTick = false;
      this.removeOnNoFireTick = false;
      this.loopTicks = true;
      this.canBeCrushed = true;
      this.noEntityCollision = true;
      this.noBreakCollision = false;
      this.isOpaque = false;
      this.tickRate = 30;
      this.setLightOpacity(1);
      this.setTickRandomly(this.tickRandomly);
      this.setSoundType(SoundType.WOOL);
      this.disableStats();
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(AGE, PERMANENT, STATIC, NORTH, EAST, SOUTH, WEST, UPPER);
   }

      public String getLocalizedName() {
      return LanguageManager.translate(this.getDescriptionId() + ".name");
   }

      public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   @Override
   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate(this.getDescriptionId() + ".description");
   }

   public BlockState getActualState(BlockState state, BlockGetter worldIn, BlockPos pos) {
      return !com.lycanitesmobs.compat.LycMath.isSideSolid(worldIn, pos.below(), Direction.UP)
            && !this.canCatchFire(worldIn, pos.below(), Direction.UP)
         ? state.setValue(NORTH, this.canCatchFire(worldIn, pos.north(), Direction.SOUTH))
            .setValue(EAST, this.canCatchFire(worldIn, pos.east(), Direction.WEST))
            .setValue(SOUTH, this.canCatchFire(worldIn, pos.south(), Direction.NORTH))
            .setValue(WEST, this.canCatchFire(worldIn, pos.west(), Direction.EAST))
            .setValue(UPPER, this.canCatchFire(worldIn, pos.above(), Direction.DOWN))
            .setValue(PERMANENT, false)
            .setValue(STATIC, false)
         : this.defaultBlockState();
   }

      public BlockState getStateFromMeta(int meta) {
      return this.defaultBlockState().setValue(AGE, meta);
   }

      public int getMetaFromState(BlockState state) {
      return (Integer)state.getValue(AGE);
   }

      public int tickRate(Level world) {
      return this.tickRate;
   }

   @Override
   public void tick(BlockState blockState, ServerLevel world, BlockPos pos, RandomSource rand) {
      if (!(Boolean)blockState.getValue(STATIC) && world.isAreaLoaded(pos, 2)) {
         boolean permanent = (Boolean)blockState.getValue(PERMANENT);
         if (!world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (this.removeOnNoFireTick && !permanent) {
               world.removeBlock(pos, false);
            }
         } else {
            if (!this.canPlaceBlockAt(world, pos) || this.removeOnTick) {
               world.removeBlock(pos, false);
            }

            Block blockBelow = world.getBlockState(pos.below()).getBlock();
            boolean isOnFireSource = permanent || this.isBlockFireSource(blockBelow, world, pos.below(), Direction.UP);
            int age = (Integer)blockState.getValue(AGE);
            if (!isOnFireSource && this.canDie(world, pos) && rand.nextFloat() < 0.2F + (float)age * 0.03F) {
               world.removeBlock(pos, false);
            } else {
               if (age < 15) {
                  blockState = blockState.setValue(AGE, Math.max(age + Math.round((float)rand.nextInt(this.agingRate) / 2.0F), 15));
                  world.setBlock(pos, blockState, 4);
               }

               if (this.loopTicks) {
                  world.scheduleTick(pos, this, this.tickRate(world) + rand.nextInt(10));
               }

               if (!isOnFireSource) {
                  if (!this.canNeighborCatchFire(world, pos)) {
                     if (age > 3) {
                        world.removeBlock(pos, false);
                     }

                     return;
                  }

                  if (!this.canCatchFire(world, pos.below(), Direction.UP) && age == 15 && rand.nextInt(4) == 0) {
                     world.removeBlock(pos, false);
                     return;
                  }
               }

               if (!(this.spreadChance <= 0.0F) && !permanent) {
                  boolean highHumidity = world.getBiome(pos).value().getModifiedClimateSettings().downfall() > 0.85F;
                  int humidityChance = 0;
                  if (highHumidity) {
                     humidityChance = -50;
                  }

                  this.tryCatchFire(world, pos.east(), 300 + humidityChance, rand, age, Direction.WEST);
                  this.tryCatchFire(world, pos.west(), 300 + humidityChance, rand, age, Direction.EAST);
                  this.tryCatchFire(world, pos.below(), 250 + humidityChance, rand, age, Direction.UP);
                  this.tryCatchFire(world, pos.above(), 250 + humidityChance, rand, age, Direction.DOWN);
                  this.tryCatchFire(world, pos.north(), 300 + humidityChance, rand, age, Direction.SOUTH);
                  this.tryCatchFire(world, pos.south(), 300 + humidityChance, rand, age, Direction.NORTH);

                  for (int offsetX = -1; offsetX <= 1; offsetX++) {
                     for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        for (int offsetY = -1; offsetY <= 4; offsetY++) {
                           if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                              int chance = 100;
                              if (offsetY > 1) {
                                 chance += (offsetY - 1) * 100;
                              }

                              BlockPos spreadPos = pos.offset(offsetX, offsetY, offsetZ);
                              int spreadEncouragement = this.getNeighborEncouragement(world, spreadPos);
                              if (spreadEncouragement > 0) {
                                 int spreadFlammability = (spreadEncouragement + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                                 if (highHumidity) {
                                    spreadFlammability /= 2;
                                 }

                                 if (spreadFlammability > 0
                                    && rand.nextInt(chance) <= spreadFlammability
                                    && (!world.isRaining() || !this.canDie(world, spreadPos))) {
                                    int spreadAge = age + rand.nextInt(5) / 4;
                                    if (spreadAge > 15) {
                                       spreadAge = 15;
                                    }

                                    world.setBlock(spreadPos, blockState.setValue(AGE, spreadAge).setValue(PERMANENT, false), 3);
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

   public boolean requiresUpdates() {
      return !this.tickRandomly;
   }

   public boolean canPlaceBlockAt(Level world, BlockPos pos) {
      try {
         return com.lycanitesmobs.compat.LycMath.isSideSolid(world, pos, Direction.UP) || this.canNeighborCatchFire(world, pos);
      } catch (Exception var4) {
         return false;
      }
   }

   @Override
   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos triggerPos, boolean lycMoving) {
   }

   @Override
   protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState lycOldState, boolean lycMoved) {
      super.onPlace(state, world, pos, lycOldState, lycMoved);
   }

   protected boolean canNeighborCatchFire(Level worldIn, BlockPos pos) {
      for (Direction enumfacing : Direction.values()) {
         if (this.canCatchFire(worldIn, pos.relative(enumfacing), enumfacing.getOpposite())) {
            return true;
         }
      }

      return false;
   }

   protected int getNeighborEncouragement(Level worldIn, BlockPos pos) {
      if (!worldIn.isEmptyBlock(pos)) {
         return 0;
      } else {
         int i = 0;

         for (Direction enumfacing : Direction.values()) {
            i = Math.max(
               worldIn.getBlockState(pos.relative(enumfacing)).getFlammability(worldIn, pos.relative(enumfacing), enumfacing.getOpposite()),
               i
            );
         }

         return i;
      }
   }

   private void tryCatchFire(Level world, BlockPos pos, int chance, RandomSource random, int age, Direction face) {
      int flammability = this.getBlockFlammability(world, pos, face);
      if (Math.round((float)random.nextInt(chance) / this.spreadChance) < flammability) {
         BlockState blockState = world.getBlockState(pos);
         if (random.nextInt(age + 10) < 5 && !world.isRainingAt(pos)) {
            int newFireAge = age + random.nextInt(5) / 4;
            if (newFireAge > 15) {
               newFireAge = 15;
            }

            this.burnBlockReplace(world, pos, newFireAge);
         } else {
            this.burnBlockDestroy(world, pos);
         }

         if (this.triggerTNT && blockState.getBlock() == Blocks.TNT) {
            // 1.12 BlockTNT.onPlayerDestroy with EXPLODE: primes the TNT
            net.minecraft.world.level.block.TntBlock.explode(world, pos);
         }
      }
   }

   public void burnBlockReplace(Level world, BlockPos pos, int newFireAge) {
      world.setBlock(pos, this.defaultBlockState().setValue(AGE, newFireAge), 3);
   }

   public void burnBlockDestroy(Level world, BlockPos pos) {
      world.removeBlock(pos, false);
   }

   public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
      return world.getBlockState(pos).isFlammable(world, pos, face);
   }

   public boolean isBlockFireSource(Block block, Level world, BlockPos pos, Direction side) {
      return world.getBlockState(pos).isFireSource(world, pos, side);
   }

   public int getBlockFlammability(BlockGetter world, BlockPos pos, Direction face) {
      return world.getBlockState(pos).getFlammability(world, pos, face);
   }

   protected boolean canDie(Level world, BlockPos pos) {
      return world.isRainingAt(pos)
         || world.isRainingAt(pos.west())
         || world.isRainingAt(pos.east())
         || world.isRainingAt(pos.north())
         || world.isRainingAt(pos.south());
   }

      @Override
      public int quantityDropped(RandomSource random) {
      return 0;
   }

   protected ItemStack getSilkTouchDrop(BlockState state) {
      return ItemStack.EMPTY;
   }

}
