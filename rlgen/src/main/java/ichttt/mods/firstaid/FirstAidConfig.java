package ichttt.mods.firstaid;

/**
 * 1.12 FirstAidConfig: same structure and field names, values from RLCraft 2.9.3 firstaid.cfg (not read from a file; edit here).
 */
public class FirstAidConfig {
   public static boolean capMaxHealth = false;
   public static boolean debug = false;
   public static boolean enableSoundSystem = true;
   public static boolean hardMode = true;
   public static boolean scaleMaxHealth = true;
   public static boolean useFriendlyRandomDistribution = false;
   public static VanillaHealthCalculationMode vanillaHealthCalculation = VanillaHealthCalculationMode.AVERAGE_ALL;
   public static boolean watchSetHealth = true;
   public static final DamageSystem damageSystem = new DamageSystem();
   public static final Overlay overlay = new Overlay();
   public static final ExternalHealing externalHealing = new ExternalHealing();
   public static final Debuffs debuffs = new Debuffs();
   public static final InternalHealing internalHealing = new InternalHealing();
   public static final LocationalArmor locationalArmor = new LocationalArmor();
   public static final EnchantmentHandling enchantmentHandling = new EnchantmentHandling();

   public static class DamageSystem {
      public boolean causeDeathBody = true;
      public boolean causeDeathHead = true;
      public int maxHealthBody = 6;
      public int maxHealthHead = 6;
      public int maxHealthLeftArm = 6;
      public int maxHealthLeftFoot = 6;
      public int maxHealthLeftLeg = 6;
      public int maxHealthRightArm = 6;
      public int maxHealthRightFoot = 6;
      public int maxHealthRightLeg = 6;
   }

   public static class Overlay {
      public int alpha = 50;
      public TooltipMode armorTooltipMode = TooltipMode.REPLACE;
      public boolean enableEasterEggs = true;
      public OverlayMode overlayMode = OverlayMode.PLAYER_MODEL;
      public Position pos = Position.BOTTOM_RIGHT;
      public VanillaHealthbarMode vanillaHealthBarMode = VanillaHealthbarMode.NORMAL;
      public int xOffset = 0;
      public int yOffset = 26;
      public final DisplayMode displayMode = new DisplayMode();

      public static class DisplayMode {
         public boolean flash = true;
         public int visibleDurationTicks = 200;
      }

      public enum OverlayMode {
         OFF,
         NUMBERS,
         HEARTS,
         PLAYER_MODEL,
         PLAYER_MODEL_4_COLORS,
         PLAYER_MODEL_OLD_COLORS,
         PLAYER_MODEL_OLD_4_COLORS;

         public boolean isPlayerModel() {
            return this == PLAYER_MODEL || this == PLAYER_MODEL_4_COLORS || this == PLAYER_MODEL_OLD_COLORS || this == PLAYER_MODEL_OLD_4_COLORS;
         }
      }

      public enum Position {
         TOP_LEFT,
         TOP_RIGHT,
         BOTTOM_LEFT,
         BOTTOM_RIGHT
      }

      public enum TooltipMode {
         REPLACE,
         APPEND,
         NONE
      }

      public enum VanillaHealthbarMode {
         NORMAL,
         HIGHLIGHT_CRITICAL_PATH,
         HIDE
      }
   }

   public static class ExternalHealing {
      public boolean allowNaturalRegeneration = false;
      public boolean allowOtherHealingItems = true;
      public double naturalRegenMultiplier = 1.0;
      public double otherRegenMultiplier = 1.0;
      public int resistanceReductionPercentPerLevel = 20;
      public double sleepHealPercentage = 0.50;
   }

   public static class Debuffs {
      public final Head head = new Head();
      public final Body body = new Body();
      public final Arms arms = new Arms();
      public final LegsAndFeet legsAndFeet = new LegsAndFeet();

      public static class ConditionOnHit {
         public float[] damageTaken;
         public int[] debuffLength;

         ConditionOnHit(float[] damageTaken, int[] debuffLength) {
            this.damageTaken = damageTaken;
            this.debuffLength = debuffLength;
         }
      }

      public static class ConditionConstant {
         public int[] debuffStrength;
         public float[] healthPercentageLeft;

         ConditionConstant(int[] debuffStrength, float[] healthPercentageLeft) {
            this.debuffStrength = debuffStrength;
            this.healthPercentageLeft = healthPercentageLeft;
         }
      }

      public static class Head {
         public boolean blindness = true;
         public boolean nausea = false;
         public final ConditionOnHit blindnessConditions = new ConditionOnHit(new float[]{6.0F, 3.0F}, new int[]{50, 30});
         public final ConditionOnHit nauseaConditions = new ConditionOnHit(new float[]{6.0F, 3.0F}, new int[]{150, 100});
      }

      public static class Body {
         public boolean nausea = true;
         public boolean weakness = true;
         public final ConditionOnHit nauseaConditions = new ConditionOnHit(new float[]{6.0F, 3.0F}, new int[]{100, 50});
         public final ConditionConstant weaknessConditions = new ConditionConstant(new int[]{2, 1}, new float[]{0.11F, 0.21F});
      }

      public static class Arms {
         public boolean mining_fatigue = true;
         public final ConditionConstant miningFatigueConditions = new ConditionConstant(new int[]{2, 1}, new float[]{0.01F, 0.21F});
      }

      public static class LegsAndFeet {
         public boolean slowness = true;
         public final ConditionConstant slownessConditions = new ConditionConstant(new int[]{4, 2, 1}, new float[]{0.01F, 0.21F, 0.41F});
      }
   }

   public static class InternalHealing {
      public final Item bandage = new Item(3000, 4, 6);
      public final Item plaster = new Item(2000, 2, 3);

      public static class Item {
         public int applyTime;
         public int secondsPerHeal;
         public int totalHeals;

         Item(int applyTime, int secondsPerHeal, int totalHeals) {
            this.applyTime = applyTime;
            this.secondsPerHeal = secondsPerHeal;
            this.totalHeals = totalHeals;
         }
      }
   }

   public static class LocationalArmor {
      public final Armor armor = new Armor();
      public final Toughness toughness = new Toughness();

      public static class Armor {
         public double chestArmorMultiplier = 2.0;
         public double chestArmorOffset = 3.0;
         public double feetArmorMultiplier = 3.0;
         public double feetArmorOffset = 3.0;
         public double headArmorMultiplier = 4.0;
         public double headArmorOffset = 3.0;
         public double legsArmorMultiplier = 2.0;
         public double legsArmorOffset = 4.0;
      }

      public static class Toughness {
         public double chestToughnessMultiplier = 1.0;
         public double chestToughnessOffset = 0.0;
         public double feetToughnessMultiplier = 1.0;
         public double feetToughnessOffset = 0.0;
         public double headToughnessMultiplier = 1.0;
         public double headToughnessOffset = 0.0;
         public double legsToughnessMultiplier = 1.0;
         public double legsToughnessOffset = 0.0;
      }
   }

   public static class EnchantmentHandling {
      public ArmorEnchantmentMode armorEnchantmentMode = ArmorEnchantmentMode.LOCAL_ENCHANTMENTS;
      public int enchantmentMultiplier = 4;
      public final OverrideEntries overrideEntries = new OverrideEntries();

      public static class OverrideEntries {
         public int[] multiplierOverride = {1, 1, 2, 2, 2, 3, 3, 2, 2, 2, 2};
         public String[] resourceLocation = {"minecraft:feather_falling", "somanyenchantments:advancedfeatherfalling", "minecraft:projectile_protection",
            "somanyenchantments:advancedprojectileprotection", "somanyenchantments:physicalprotection", "somanyenchantments:magicprotection",
            "somanyenchantments:advancedprotection", "minecraft:fire_protection", "minecraft:blast_protection", "somanyenchantments:advancedfireprotection",
            "somanyenchantments:advancedblastprotection"};
      }

      public enum ArmorEnchantmentMode {
         GLOBAL_ENCHANTMENTS,
         LOCAL_ENCHANTMENTS
      }
   }

   public enum VanillaHealthCalculationMode {
      AVERAGE_ALL,
      AVERAGE_CRITICAL,
      MIN_CRITICAL,
      CRITICAL_50_PERCENT_OTHER_50_PERCENT
   }
}
