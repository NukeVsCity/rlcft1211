package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

/** 1.12 RandomDamageDistribution: a random start slot then walking up or down (nearest first), or fully shuffled slots. */
public class RandomDamageDistribution extends DamageDistribution {
   public static final RandomDamageDistribution NEAREST_NOKILL = new RandomDamageDistribution(true, true);
   public static final RandomDamageDistribution NEAREST_KILL = new RandomDamageDistribution(true, false);
   public static final RandomDamageDistribution ANY_NOKILL = new RandomDamageDistribution(false, true);
   public static final RandomDamageDistribution ANY_KILL = new RandomDamageDistribution(false, false);
   private static final Random RANDOM = new Random();
   private final boolean nearestFirst;
   private final boolean tryNoKill;

   public static RandomDamageDistribution getDefault() {
      return FirstAidConfig.useFriendlyRandomDistribution ? NEAREST_NOKILL : NEAREST_KILL;
   }

   protected RandomDamageDistribution(boolean nearestFirst, boolean tryNoKill) {
      this.nearestFirst = nearestFirst;
      this.tryNoKill = tryNoKill;
   }

   @Override
   protected float minHealth(Player player, DamageablePart playerPart) {
      return this.tryNoKill && playerPart.canCauseDeath ? 1.0F : 0.0F;
   }

   @Override
   protected List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList() {
      if (this.nearestFirst) {
         int startValue = RANDOM.nextInt(4);
         return addAllRandom(startValue, RANDOM.nextBoolean());
      }
      List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
      // 1.12 EntityEquipmentSlot.values(): MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD
      List<EquipmentSlot> slots = new ArrayList<>(Arrays.asList(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS,
         EquipmentSlot.CHEST, EquipmentSlot.HEAD));
      Collections.shuffle(slots, RANDOM);
      for (EquipmentSlot slot : slots) {
         if (CommonUtils.isValidArmorSlot(slot)) {
            List<EnumPlayerPart> parts = CommonUtils.getPartListForSlot(slot);
            Collections.shuffle(parts);
            partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
         }
      }
      return partList;
   }

   public static List<Pair<EquipmentSlot, EnumPlayerPart[]>> addAllRandom(int startValue, boolean up) {
      List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
      for (int i = 0; i < CommonUtils.ARMOR_SLOTS.length; i++) {
         int posInArray = Math.abs(i - (up ? 0 : 3)) + startValue;
         if (posInArray > 3) {
            posInArray -= 4;
         }
         EquipmentSlot slot = CommonUtils.ARMOR_SLOTS[posInArray];
         List<EnumPlayerPart> parts = CommonUtils.getPartListForSlot(slot);
         Collections.shuffle(parts);
         partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
      }
      return partList;
   }
}
