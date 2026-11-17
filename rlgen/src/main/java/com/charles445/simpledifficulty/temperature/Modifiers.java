package com.charles445.simpledifficulty.temperature;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDDamageSources;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry.ITemperatureModifier;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry.ITemperatureTileEntity;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry.TemporaryModifier;
import com.charles445.simpledifficulty.config.JsonConfigInternal;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.LegacyClimate;
import com.charles445.simpledifficulty.util.TemperatureUtil;
import com.charles445.simpledifficulty.util.WorldUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;

/** The 1.12 temperature modifiers (package temperature), registered in the 1.12 CommonProxy order. */
public final class Modifiers {
   private Modifiers() {
   }

   public static void registerAll() {
      TemperatureRegistry.registerModifier(new ModifierDefault());
      TemperatureRegistry.registerModifier(new ModifierAltitude());
      TemperatureRegistry.registerModifier(new ModifierArmor());
      TemperatureRegistry.registerModifier(new ModifierBiome());
      TemperatureRegistry.registerModifier(new ModifierBlocksTiles());
      TemperatureRegistry.registerModifier(new ModifierDimension());
      TemperatureRegistry.registerModifier(new ModifierHeldItems());
      TemperatureRegistry.registerModifier(new ModifierSnow());
      TemperatureRegistry.registerModifier(new ModifierSprint());
      TemperatureRegistry.registerModifier(new ModifierTemporary());
      TemperatureRegistry.registerModifier(new ModifierTime());
      TemperatureRegistry.registerModifier(new ModifierWet());
   }

   public abstract static class ModifierBase implements ITemperatureModifier {
      private final String name;
      protected final float defaultTemperature;

      protected ModifierBase(String name) {
         this.name = name;
         this.defaultTemperature = (float)((TemperatureEnum.NORMAL.getUpperBound() + TemperatureEnum.COLD.getUpperBound()) / 2);
      }

      @Override
      public float getPlayerInfluence(Player player) {
         return 0.0F;
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         return 0.0F;
      }

      @Override
      public String getName() {
         return this.name;
      }

      protected float applyUndergroundEffect(float temperature, Level world, BlockPos pos) {
         if (pos.getY() >= 64) {
            return temperature;
         } else if (!ModConfig.server.temperature.undergroundEffect || !LegacyClimate.isSurfaceWorld(world)) {
            return temperature;
         } else if (!world.canSeeSky(pos) && !world.canSeeSky(pos.above())) {
            int cutoff = ModConfig.server.temperature.undergroundEffectCutoff;
            return pos.getY() > cutoff && cutoff != 64 ? temperature * (float)(pos.getY() - cutoff) / (64.0F - (float)cutoff) : 0.0F;
         } else {
            return temperature;
         }
      }

      protected float getTempForBiome(Holder<Biome> biome) {
         return Mth.clamp(LegacyClimate.getDefaultTemperature(biome), 0.0F, 1.35F) / 1.35F;
      }

      protected float normalizeToPlusMinus(float value) {
         return value * 2.0F - 1.0F;
      }
   }

   public static class ModifierDefault extends ModifierBase {
      public ModifierDefault() {
         super("Default");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         return this.defaultTemperature;
      }
   }

   public static class ModifierAltitude extends ModifierBase {
      public ModifierAltitude() {
         super("Altitude");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         return !LegacyClimate.isSurfaceWorld(world)
            ? 0.0F
            : -1.0F * Math.abs((64.0F - (float)pos.getY()) / 64.0F * (float)ModConfig.server.temperature.altitudeMultiplier + 1.0F);
      }
   }

   public static class ModifierArmor extends ModifierBase {
      public ModifierArmor() {
         super("Armor");
      }

      @Override
      public float getPlayerInfluence(Player player) {
         float value = 0.0F;
         value += this.checkArmorSlot(player, player.getItemBySlot(EquipmentSlot.HEAD));
         value += this.checkArmorSlot(player, player.getItemBySlot(EquipmentSlot.CHEST));
         value += this.checkArmorSlot(player, player.getItemBySlot(EquipmentSlot.LEGS));
         return value + this.checkArmorSlot(player, player.getItemBySlot(EquipmentSlot.FEET));
      }

      private float checkArmorSlot(Player player, ItemStack stack) {
         if (stack.isEmpty()) {
            return 0.0F;
         } else {
            float sum = 0.0F;
            if (ModConfig.server.miscellaneous.registerEnchantments) {
               var reg = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
               Holder<Enchantment> chilling = reg.getHolder(SDDamageSources.CHILLING).orElse(null);
               Holder<Enchantment> heating = reg.getHolder(SDDamageSources.HEATING).orElse(null);
               if (chilling != null && EnchantmentHelper.getItemEnchantmentLevel(chilling, stack) > 0) {
                  sum -= (float)ModConfig.server.temperature.enchantmentTemperature;
               } else if (heating != null && EnchantmentHelper.getItemEnchantmentLevel(heating, stack) > 0) {
                  sum += (float)ModConfig.server.temperature.enchantmentTemperature;
               }
            }
            sum += processStackJSON(JsonConfig.armorTemperatures, stack);
            return sum + TemperatureUtil.getArmorTemperatureTag(stack);
         }
      }
   }

   static float processStackJSON(Map<net.minecraft.world.item.Item, List<JsonConfig.JsonTemperatureIdentity>> map, ItemStack stack) {
      List<JsonConfig.JsonTemperatureIdentity> list = map.get(stack.getItem());
      if (list != null) {
         for (JsonConfig.JsonTemperatureIdentity jtm : list) {
            if (jtm != null && jtm.matches(stack)) {
               return jtm.temperature;
            }
         }
      }
      return 0.0F;
   }

   public static class ModifierBiome extends ModifierBase {
      public ModifierBiome() {
         super("Biome");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         float biomeAverage = (this.t(world, pos.offset(10, 0, 0)) + this.t(world, pos.offset(-10, 0, 0)) + this.t(world, pos.offset(0, 0, 10))
            + this.t(world, pos.offset(0, 0, -10)) + this.t(world, pos.offset(7, 0, 7)) + this.t(world, pos.offset(7, 0, -7))
            + this.t(world, pos.offset(-7, 0, 7)) + this.t(world, pos.offset(-7, 0, -7)) + this.t(world, pos)) / 9.0F;
         return this.applyUndergroundEffect(this.normalizeToPlusMinus(biomeAverage) * (float)ModConfig.server.temperature.biomeMultiplier, world, pos);
      }

      private float t(Level world, BlockPos pos) {
         return this.getTempForBiome(LegacyClimate.getBiome(world, pos));
      }
   }

   public static class ModifierBlocksTiles extends ModifierBase {
      private float coldestValue = 0.0F;
      private float hottestValue = 0.0F;
      private float hotTotal = 0.0F;
      private float coldTotal = 0.0F;

      public ModifierBlocksTiles() {
         super("BlocksTiles");
      }

      @Override
      public synchronized float getWorldInfluence(Level world, BlockPos pos) {
         this.resetHeat();
         this.doBlocksRoutine(world, pos);
         if (ModConfig.server.temperature.blocksTilesSeparate) {
            float result = this.consolidateHeat();
            this.resetHeat();
            this.doTileEntitiesRoutine(world, pos);
            return result + this.consolidateHeat();
         } else {
            this.doTileEntitiesRoutine(world, pos);
            return this.consolidateHeat();
         }
      }

      private void resetHeat() {
         this.coldestValue = 0.0F;
         this.hottestValue = 0.0F;
         this.hotTotal = 0.0F;
         this.coldTotal = 0.0F;
      }

      private float consolidateHeat() {
         if (!ModConfig.server.temperature.stackingTemperature) {
            return this.hottestValue + this.coldestValue;
         } else {
            this.hotTotal = this.hotTotal - this.hottestValue;
            this.coldTotal = this.coldTotal - this.coldestValue;
            float hotLogValue = this.hottestValue * (float)Math.sqrt((double)easyLog(this.hotTotal));
            float coldLogValue = this.coldestValue * (float)Math.sqrt((double)easyLog(this.coldTotal));
            float result = hotLogValue + coldLogValue;
            if (result > this.hottestValue) {
               return Math.min(this.hottestValue + (float)ModConfig.server.temperature.stackingTemperatureLimit, result);
            } else {
               return result < this.coldestValue ? Math.max(this.coldestValue - (float)ModConfig.server.temperature.stackingTemperatureLimit, result) : result;
            }
         }
      }

      private void doBlocksRoutine(Level world, BlockPos pos) {
         BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
         for (int x = -4; x <= 4; x++) {
            for (int y = -3; y <= 1; y++) {
               for (int z = -4; z <= 4; z++) {
                  m.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                  if (!world.isLoaded(m)) {
                     continue; // 1.12 ChunkCache: an unloaded chunk reads as air
                  }
                  BlockState blockstate = world.getBlockState(m);
                  List<JsonConfig.JsonPropertyTemperature> tempInfoList = JsonConfig.blockTemperatures.get(blockstate.getBlock());
                  if (tempInfoList != null) {
                     for (JsonConfig.JsonPropertyTemperature tempInfo : tempInfoList) {
                        if (tempInfo != null) {
                           float blockTemp = tempInfo.temperature;
                           if (blockTemp != 0.0F && tempInfo.matchesState(blockstate)) {
                              this.processTemp(blockTemp);
                           }
                        }
                     }
                  } else if (blockstate.is(BlockTags.FIRE)) {
                     this.processTemp(JsonConfigInternal.materialTemperatureFire);
                  }
               }
            }
         }
      }

      private void doTileEntitiesRoutine(Level world, BlockPos pos) {
         if (ModConfig.server.temperatureTileEntities) {
            for (int x = -3; x <= 3; x++) {
               for (int z = -3; z <= 3; z++) {
                  BlockPos cpos = pos.offset(x * 16, 0, z * 16);
                  if (WorldUtil.isChunkLoaded(world, cpos) && world.getChunkSource().getChunkNow(cpos.getX() >> 4, cpos.getZ() >> 4) instanceof LevelChunk chunk) {
                     for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        this.processTemp(checkTileEntity(entry.getKey(), entry.getValue(), pos));
                     }
                  }
               }
            }
         }
      }

      private void processTemp(float blockTemp) {
         if (blockTemp != 0.0F) {
            if (blockTemp >= 0.0F) {
               this.hotTotal += blockTemp;
               if (blockTemp > this.hottestValue) {
                  this.hottestValue = blockTemp;
               }
            } else {
               this.coldTotal += blockTemp;
               if (blockTemp < this.coldestValue) {
                  this.coldestValue = blockTemp;
               }
            }
         }
      }

      private static float checkTileEntity(BlockPos pos, BlockEntity tileEntity, BlockPos selfPos) {
         double distance = pos.distSqr(selfPos);
         return distance < 2500.0 && tileEntity instanceof ITemperatureTileEntity t ? t.getInfluence(selfPos, distance) : 0.0F;
      }

      private static float easyLog(float f) {
         return f >= 0.0F ? (float)Math.log10((double)(f + 10.0F)) : (float)Math.log10((double)(-1.0F * f + 10.0F));
      }
   }

   public static class ModifierDimension extends ModifierBase {
      public ModifierDimension() {
         super("Dimension");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         JsonConfig.JsonTemperature tempInfo = JsonConfig.dimensionTemperature.get("" + LegacyClimate.dimensionId(world));
         return tempInfo != null ? tempInfo.temperature : 0.0F;
      }
   }

   public static class ModifierHeldItems extends ModifierBase {
      public ModifierHeldItems() {
         super("HeldItems");
      }

      @Override
      public float getPlayerInfluence(Player player) {
         ItemStack mainHand = player.getMainHandItem();
         ItemStack offHand = player.getOffhandItem();
         float sum = 0.0F;
         if (!mainHand.isEmpty()) {
            sum += processStackJSON(JsonConfig.heldItemTemperatures, mainHand);
         }
         if (!offHand.isEmpty()) {
            sum += processStackJSON(JsonConfig.heldItemTemperatures, offHand);
         }
         return sum;
      }
   }

   public static class ModifierSnow extends ModifierBase {
      public ModifierSnow() {
         super("Snow");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         if (world.isRaining() && world.canSeeSky(pos)) {
            Holder<Biome> biome = LegacyClimate.getBiome(world, pos);
            if (LegacyClimate.getEnableSnow(biome, pos)) {
               return (float)ModConfig.server.temperature.snowValue;
            } else {
               return LegacyClimate.canSnowAt(world, pos) ? (float)ModConfig.server.temperature.snowValue : 0.0F;
            }
         } else {
            return 0.0F;
         }
      }
   }

   public static class ModifierSprint extends ModifierBase {
      public ModifierSprint() {
         super("Sprint");
      }

      @Override
      public float getPlayerInfluence(Player player) {
         return player.isSprinting() ? (float)ModConfig.server.temperature.sprintingValue : 0.0F;
      }
   }

   public static class ModifierTemporary extends ModifierBase {
      public ModifierTemporary() {
         super("Temporary");
      }

      @Override
      public float getPlayerInfluence(Player player) {
         float sum = 0.0F;
         for (TemporaryModifier tm : SDCapabilities.getTemperatureData(player).getTemporaryModifiers().values()) {
            sum += tm.temperature;
         }
         return sum;
      }
   }

   public static class ModifierTime extends ModifierBase {
      public ModifierTime() {
         super("Time");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         if (!LegacyClimate.isSurfaceWorld(world)) {
            return 0.0F;
         } else {
            long time = world.getDayTime() % 24000L;
            if (time < 12000L && !ModConfig.server.temperature.timeTemperatureDay) {
               return 0.0F;
            } else if (time >= 12000L && !ModConfig.server.temperature.timeTemperatureNight) {
               return 0.0F;
            } else {
               float timetemperature = (Math.abs(((float)time % 12000.0F - 6000.0F) / 6000.0F) - 1.0F) * (float)ModConfig.server.temperature.timeMultiplier;
               if (time < 12000L) {
                  timetemperature *= -1.0F;
               }
               float biomeMultiplier = 1.0F + Math.abs(this.normalizeToPlusMinus(this.getTempForBiome(LegacyClimate.getBiome(world, pos))))
                  * ((float)ModConfig.server.temperature.timeBiomeMultiplier - 1.0F);
               timetemperature *= biomeMultiplier;
               int shadeConf = ModConfig.server.temperature.timeTemperatureShade;
               if (timetemperature > 0.0F && shadeConf != 0 && !world.canSeeSky(pos) && !world.canSeeSky(pos.above())) {
                  timetemperature = Math.max(0.0F, timetemperature + (float)shadeConf);
               }
               return this.applyUndergroundEffect(timetemperature, world, pos);
            }
         }
      }
   }

   public static class ModifierWet extends ModifierBase {
      public ModifierWet() {
         super("Wet");
      }

      @Override
      public float getWorldInfluence(Level world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         FluidState fluid = state.getFluidState();
         if (!fluid.isEmpty()) {
            var key = BuiltInRegistries.FLUID.getKey(fluid.getType());
            if (!key.getNamespace().equals("minecraft")) { // 1.12 IFluidBlock: Forge fluids only, named without the flowing_ twin
               JsonConfig.JsonTemperature tempInfo = JsonConfig.fluidTemperatures.get(key.getPath().replace("flowing_", ""));
               if (tempInfo != null) {
                  return tempInfo.temperature;
               }
            }
         }
         if (fluid.is(FluidTags.WATER)) {
            return (float)ModConfig.server.temperature.wetValue;
         } else {
            return LegacyClimate.isRainingAt(world, pos) ? (float)ModConfig.server.temperature.wetValue : 0.0F;
         }
      }
   }
}
