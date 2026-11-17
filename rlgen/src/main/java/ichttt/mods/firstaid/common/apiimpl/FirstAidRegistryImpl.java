package ichttt.mods.firstaid.common.apiimpl;

import com.charles445.simpledifficulty.api.SDDamageSources;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart.PartHealer;
import ichttt.mods.firstaid.common.damagesystem.debuff.AbstractDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 1.12 FirstAidRegistryImpl + RegistryManager.registerDefaults + the builders: which distribution a damage source uses, the healing
 * items and the debuffs. 1.12 keyed static distributions by DamageSource.damageType, here by the 1.21 damage type.
 */
public class FirstAidRegistryImpl {
   public static final FirstAidRegistryImpl INSTANCE = new FirstAidRegistryImpl();
   private final ArrayList<Pair<Predicate<DamageSource>, IDamageDistribution>> distributionsDynamic = new ArrayList<>();
   private final Map<ResourceKey<DamageType>, IDamageDistribution> distributionsStatic = new HashMap<>();
   private final Map<Item, Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>>> healerMap = new HashMap<>();
   private final Multimap<EnumDebuffSlot, Supplier<IDebuff>> debuffs = HashMultimap.create();
   private boolean defaultsDone;

   /** RegistryManager.registerDefaults (+ SimpleDifficulty's FirstAidCompat for dehydration) */
   public synchronized void registerDefaults() {
      if (this.defaultsDone) {
         return;
      }
      this.defaultsDone = true;
      this.registerStatic(standard(false, true, layer(EquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT),
         layer(EquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)), DamageTypes.FALL, DamageTypes.HOT_FLOOR);
      this.registerStatic(standard(false, true, layer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)), DamageTypes.FALLING_ANVIL);
      this.registerStatic(standard(true, true, layer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD), layer(EquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM,
         EnumPlayerPart.RIGHT_ARM)), DamageTypes.LIGHTNING_BOLT);
      this.registerStatic(RandomDamageDistribution.ANY_KILL, DamageTypes.MAGIC);
      if (FirstAidConfig.hardMode) {
         this.registerStatic(standard(false, false, layer(EquipmentSlot.CHEST, EnumPlayerPart.BODY)), DamageTypes.STARVE);
         this.registerStatic(standard(true, false, layer(EquipmentSlot.CHEST, EnumPlayerPart.BODY), layer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)),
            DamageTypes.DROWN);
      } else {
         this.registerStatic(RandomDamageDistribution.ANY_NOKILL, DamageTypes.STARVE, DamageTypes.DROWN);
      }
      this.registerStatic(RandomDamageDistribution.ANY_NOKILL, DamageTypes.IN_WALL, DamageTypes.CRAMMING);
      this.distributionsDynamic.add(Pair.of(s -> s.is(DamageTypeTags.IS_EXPLOSION), new EqualDamageDistribution(false, 0.8F)));
      // SimpleDifficulty 0.3.9 FirstAidCompat: dehydration hits body (chest armor) and head, any order, no neighbour rest
      this.registerStatic(standard(true, false, layer(EquipmentSlot.CHEST, EnumPlayerPart.BODY), layer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)),
         SDDamageSources.DEHYDRATION);
      FirstAidConfig.Debuffs d = FirstAidConfig.debuffs;
      this.loadOnHit("blindness", () -> d.head.blindness, FirstAid.HEARTBEAT.get(), d.head.blindnessConditions, EnumDebuffSlot.HEAD);
      this.loadOnHit("nausea", () -> d.head.nausea, null, d.head.nauseaConditions, EnumDebuffSlot.HEAD);
      this.loadOnHit("nausea", () -> d.body.nausea, null, d.body.nauseaConditions, EnumDebuffSlot.BODY);
      this.loadConstant("weakness", () -> d.body.weakness, d.body.weaknessConditions, EnumDebuffSlot.BODY);
      this.loadConstant("mining_fatigue", () -> d.arms.mining_fatigue, d.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);
      this.loadConstant("slowness", () -> d.legsAndFeet.slowness, d.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
   }

   @SafeVarargs
   private static List<Pair<EquipmentSlot, EnumPlayerPart[]>> list(Pair<EquipmentSlot, EnumPlayerPart[]>... layers) {
      return new ArrayList<>(Arrays.asList(layers));
   }

   private static Pair<EquipmentSlot, EnumPlayerPart[]> layer(EquipmentSlot slot, EnumPlayerPart... parts) {
      return Pair.of(slot, parts);
   }

   /** StandardDamageDistributionBuilder.build(ignoreOrder, !disableNeighbourRestDistribution) */
   @SafeVarargs
   private static IDamageDistribution standard(boolean ignoreOrder, boolean neighbours, Pair<EquipmentSlot, EnumPlayerPart[]>... layers) {
      return new StandardDamageDistribution(list(layers), ignoreOrder, neighbours);
   }

   @SafeVarargs
   private void registerStatic(IDamageDistribution distribution, ResourceKey<DamageType>... sources) {
      for (ResourceKey<DamageType> type : sources) {
         if (this.distributionsStatic.containsKey(type)) {
            FirstAid.LOGGER.info("Damage Distribution override detected for source " + type);
         }
         this.distributionsStatic.put(type, distribution);
      }
   }

   private void loadOnHit(String potionName, BooleanSupplier enable, @Nullable SoundEvent sound, FirstAidConfig.Debuffs.ConditionOnHit config, EnumDebuffSlot slot) {
      if (config.debuffLength.length != config.damageTaken.length || config.debuffLength.length == 0) {
         FirstAid.LOGGER.warn("Invalid config entry for debuff {} at part {}", potionName, slot);
         return;
      }
      Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
      for (int i = 0; i < config.damageTaken.length; i++) {
         map.put(config.damageTaken[i], config.debuffLength[i]);
      }
      this.registerDebuff(slot, () -> new AbstractDebuff.OnHitDebuff(potionName, map, enable, sound));
   }

   private void loadConstant(String potionName, BooleanSupplier enable, FirstAidConfig.Debuffs.ConditionConstant config, EnumDebuffSlot slot) {
      if (config.debuffStrength.length != config.healthPercentageLeft.length || config.healthPercentageLeft.length == 0) {
         FirstAid.LOGGER.warn("Invalid config entry for debuff {} at part {}", potionName, slot);
         return;
      }
      Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
      for (int i = 0; i < config.healthPercentageLeft.length; i++) {
         map.put(config.healthPercentageLeft[i], config.debuffStrength[i]);
      }
      this.registerDebuff(slot, () -> new AbstractDebuff.ConstantDebuff(potionName, map, enable));
   }

   public void registerDebuff(EnumDebuffSlot slot, Supplier<IDebuff> debuff) {
      if (slot.playerParts.length > 1) {
         this.debuffs.put(slot, () -> new SharedDebuff(debuff.get(), slot));
      } else {
         this.debuffs.put(slot, debuff);
      }
   }

   public void registerHealingType(Item item, Function<ItemStack, PartHealer> factory, Function<ItemStack, Integer> applyTime) {
      this.healerMap.put(item, Pair.of(factory, applyTime));
   }

   @Nullable
   public PartHealer getPartHealer(ItemStack type) {
      Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(type.getItem());
      return pair != null ? pair.getLeft().apply(type) : null;
   }

   @Nullable
   public Integer getPartHealingTime(ItemStack stack) {
      Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(stack.getItem());
      return pair != null ? pair.getRight().apply(stack) : null;
   }

   @Nullable
   public IDamageDistribution getDamageDistributionForSource(DamageSource source) {
      IDamageDistribution distribution = source.typeHolder().unwrapKey().map(this.distributionsStatic::get).orElse(null);
      if (distribution == null) {
         for (Pair<Predicate<DamageSource>, IDamageDistribution> pair : this.distributionsDynamic) {
            if (pair.getLeft().test(source)) {
               distribution = pair.getRight();
               break;
            }
         }
      }
      return distribution;
   }

   public IDebuff[] getDebuffs(EnumDebuffSlot slot) {
      this.registerDefaults();
      return this.debuffs.get(slot).stream().map(Supplier::get).toArray(IDebuff[]::new);
   }
}
