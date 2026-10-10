package greymerk.roguelike.monster;

import com.github.fnar.minecraft.Difficulty;
import com.github.fnar.minecraft.block.spawner.MobType;
import com.github.fnar.minecraft.entity.Slot;
import com.github.fnar.minecraft.item.Armour;
import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.Arrow;
import com.github.fnar.minecraft.item.RldBaseItem;
import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.minecraft.item.Shield;
import com.github.fnar.minecraft.item.ToolType;
import com.github.fnar.minecraft.item.WeaponType;
import com.github.fnar.roguelike.loot.special.armour.SpecialArmour;
import com.github.fnar.roguelike.loot.special.tools.SpecialTool;
import com.github.fnar.roguelike.loot.special.weapons.SpecialBow;
import com.github.fnar.roguelike.loot.special.weapons.SpecialSword;
import com.github.fnar.util.Color;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.treasure.loot.Equipment;
import greymerk.roguelike.treasure.loot.provider.LootItem;
import greymerk.roguelike.treasure.loot.provider.SpecialtyLootItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Mob {
   private boolean isChild = false;
   private final Map<Slot, RldItemStack> items = Maps.newEnumMap(Slot.class);
   private String name;
   private MobType mobType = MobType.ZOMBIE;

   public Mob withRandomEquipment(int level, Random random) {
      if (!this.isEquippable()) {
         return this;
      } else {
         RldBaseItem mainHand = chooseMainhand(random, level);
         if (mainHand != null) {
            this.equipMainhand(mainHand.asStack());
         }

         RldBaseItem offHand = this.chooseOffhand(random);
         if (offHand != null) {
            this.equipOffhand(offHand.asStack());
         }

         this.equip(Slot.FEET, chooseArmourItem(random, level, ArmourType.BOOTS).asStack());
         this.equip(Slot.LEGS, chooseArmourItem(random, level, ArmourType.LEGGINGS).asStack());
         this.equip(Slot.CHEST, chooseArmourItem(random, level, ArmourType.CHESTPLATE).asStack());
         this.equip(Slot.HEAD, chooseArmourItem(random, level, ArmourType.HELMET).asStack());
         return this;
      }
   }

   public boolean isChild() {
      return this.isChild;
   }

   public Map<Slot, RldItemStack> getItems() {
      return this.items;
   }

   public String getName() {
      return this.name;
   }

   public boolean isEquippable() {
      return this.getMobType().isEquippable();
   }

   public void equip(Slot slot, RldItemStack rldItemStack) {
      this.items.put(slot, rldItemStack);
   }

   public void setChild(boolean isChild) {
      this.isChild = isChild;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void equipArmor(Random random, int level, Color color, int difficulty) {
      Arrays.stream(ArmourType.values())
         .filter(armourType -> !armourType.equals(ArmourType.HORSE))
         .forEach(armourType -> this.equip(armourType.asSlot(), this.createArmor(random, level, armourType, color, Difficulty.fromInt(difficulty))));
   }

   private RldItemStack createArmor(Random random, int level, ArmourType armourType, Color color, Difficulty difficulty) {
      return rollForSpecial(random, level, difficulty)
         ? SpecialArmour.createArmour(random, armourType, Equipment.rollQuality(random, level)).asStack()
         : armourType.asItem()
            .withQuality(Equipment.rollQuality(random, level))
            .withColor(color)
            .plzEnchantAtLevel(getEnchantmentLevel(random, level, difficulty))
            .asStack();
   }

   public void equipSword(Random random, int level, Difficulty difficulty) {
      this.equipMainhand(createSword(random, level, difficulty));
   }

   private static RldItemStack createSword(Random random, int level, Difficulty difficulty) {
      return rollForSpecial(random, level, difficulty)
         ? SpecialSword.newSpecialSword(random, Equipment.rollQuality(random, level)).asStack()
         : WeaponType.SWORD
            .asItem()
            .withQuality(Equipment.rollQuality(random, level))
            .plzEnchantAtLevel(getEnchantmentLevel(random, level, difficulty))
            .asStack();
   }

   public void equipBow(Random random, int level, Difficulty difficulty) {
      this.equipMainhand(this.createBow(random, level, difficulty));
   }

   private RldItemStack createBow(Random random, int level, Difficulty difficulty) {
      return rollForSpecial(random, level, difficulty)
         ? SpecialBow.newSpecialBow(random, Equipment.rollQuality(random, level)).asStack()
         : WeaponType.BOW
            .asItem()
            .withQuality(Equipment.rollQuality(random, level))
            .plzEnchantAtLevel(getEnchantmentLevel(random, level, difficulty))
            .asStack();
   }

   public void equipTool(Random random, int level, Difficulty difficulty) {
      this.equipMainhand(createTool(random, level, difficulty));
   }

   private static RldItemStack createTool(Random random, int level, Difficulty difficulty) {
      if (rollForSpecial(random, level, difficulty)) {
         return SpecialTool.createTool(random, Equipment.rollQuality(random, level));
      } else {
         return RogueConfig.MOBS_ITEMS_TIEFIGHTERS_ENABLED.getBoolean() && random.nextDouble() < 0.1
            ? createPatternedShield(random).asStack()
            : ToolType.random(random)
               .asItem()
               .withQuality(Equipment.rollQuality(random, level))
               .plzEnchantAtLevel(getEnchantmentLevel(random, level, difficulty))
               .asStack();
      }
   }

   private static boolean rollForSpecial(Random random, int level, Difficulty difficulty) {
      return SpecialtyLootItem.rollForSpecial(random) && rollForEnchanted(random, level, difficulty);
   }

   private static int getEnchantmentLevel(Random random, int level, Difficulty difficulty) {
      return rollForEnchanted(random, level, difficulty)
         ? RogueConfig.MOBS_ITEMS_ENCHANTMENTS_LEVELS.getIntAtIndexIfNonNegative(level).orElse(LootItem.getEnchantmentLevel(random, level))
         : 0;
   }

   public static boolean rollForEnchanted(Random random, int level, Difficulty difficulty) {
      double roll = random.nextDouble();
      return roll < getEnchantmentChance(level, difficulty);
   }

   private static Double getEnchantmentChance(int level, Difficulty difficulty) {
      return RogueConfig.MOBS_ITEMS_ENCHANTMENTS_CHANCE
         .getDoubleAtIndexIfNonNegative(level)
         .orElseGet(() -> 0.05 + getEnchantmentChanceGrowth(difficulty) * (double)level);
   }

   private static double getEnchantmentChanceGrowth(Difficulty difficulty) {
      switch (difficulty) {
         case PEACEFUL:
         default:
            return 0.0;
         case EASY:
            return 0.01;
         case NORMAL:
            return 0.02;
         case DIFFICULT:
            return 0.03;
      }
   }

   public void equipArrows(Arrow arrow) {
      this.equipOffhand(arrow.asStack());
   }

   public void equipShield(Random rand) {
      this.equipOffhand(createPatternedShield(rand).asStack());
   }

   public void equipOffhand(RldItemStack item) {
      this.equip(Slot.OFFHAND, item);
   }

   public void equipMainhand(RldItemStack itemStack) {
      this.equip(Slot.MAINHAND, itemStack);
   }

   public Mob apply(MonsterProfileType monsterProfile, int level, int difficulty, Random random) {
      return monsterProfile.apply(this, level, difficulty, random);
   }

   private static RldBaseItem chooseMainhand(Random random, int level) {
      if (random.nextBoolean()) {
         return WeaponType.random(random).asItem().withQuality(Equipment.rollQuality(random, level));
      } else {
         return random.nextBoolean() ? ToolType.random(random).asItem().withQuality(Equipment.rollQuality(random, level)) : null;
      }
   }

   private static Shield createPatternedShield(Random random) {
      return Shield.newShield().withRandomPatterns(random, 1 + random.nextInt(8));
   }

   private RldBaseItem chooseOffhand(Random random) {
      return random.nextBoolean() ? createPatternedShield(random) : null;
   }

   private static ArrayList<ToolType> someTools() {
      return Lists.newArrayList(new ToolType[]{ToolType.AXE, ToolType.HOE, ToolType.PICKAXE, ToolType.SHOVEL});
   }

   private static Armour chooseArmourItem(Random random, int level, ArmourType armourItem) {
      return armourItem.asItem().withQuality(Equipment.rollQuality(random, level));
   }

   public RldItem getMainhand() {
      return Optional.ofNullable(this.items.get(Slot.MAINHAND)).map(RldItemStack::getItem).orElse(null);
   }

   public RldItem getOffhand() {
      return Optional.ofNullable(this.items.get(Slot.OFFHAND)).map(RldItemStack::getItem).orElse(null);
   }

   public RldItem getBoots() {
      return Optional.ofNullable(this.items.get(Slot.FEET)).map(RldItemStack::getItem).orElse(null);
   }

   public RldItem getLeggings() {
      return Optional.ofNullable(this.items.get(Slot.LEGS)).map(RldItemStack::getItem).orElse(null);
   }

   public RldItem getChestplate() {
      return Optional.ofNullable(this.items.get(Slot.CHEST)).map(RldItemStack::getItem).orElse(null);
   }

   public RldItem getHelmet() {
      return Optional.ofNullable(this.items.get(Slot.HEAD)).map(RldItemStack::getItem).orElse(null);
   }

   public void setMobType(MobType mobType) {
      this.mobType = mobType;
   }

   public MobType getMobType() {
      return this.mobType;
   }

   public Mob withMobType(MobType mobType) {
      this.setMobType(mobType);
      return this;
   }

   @Override
   public String toString() {
      return "Mob{isChild=" + this.isChild + ", items=" + this.items + ", name='" + this.name + '\'' + ", mobType=" + this.mobType + '}';
   }
}
