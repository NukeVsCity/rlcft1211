package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

/** 1.12 StandardDamageDistribution: the given slot layers in order (or shuffled), the rest to the neighbours of the last layers. */
public class StandardDamageDistribution extends DamageDistribution {
   private final List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList;
   private final boolean shuffle;
   private final boolean doNeighbours;
   private final EnumSet<EnumPlayerPart> blockedParts;

   public StandardDamageDistribution(List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList, boolean shuffle, boolean doNeighbours) {
      this.partList = partList;
      for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : partList) {
         for (EnumPlayerPart part : pair.getRight()) {
            if (part.slot != pair.getLeft()) {
               throw new RuntimeException(part + " is not a member of " + pair.getLeft());
            }
         }
      }
      this.shuffle = shuffle;
      this.doNeighbours = doNeighbours;
      this.blockedParts = EnumSet.noneOf(EnumPlayerPart.class);
   }

   private StandardDamageDistribution(List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList, boolean shuffle, boolean doNeighbours,
                                      EnumSet<EnumPlayerPart> blockedParts) {
      this.partList = partList;
      this.shuffle = shuffle;
      this.doNeighbours = doNeighbours;
      this.blockedParts = blockedParts;
   }

   @Override
   protected List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList() {
      if (this.shuffle) {
         Collections.shuffle(this.partList);
      }
      return this.partList;
   }

   @Override
   public float distributeDamage(float damage, Player player, DamageSource source, boolean addStat) {
      float rest = super.distributeDamage(damage, player, source, addStat);
      if (rest > 0.0F && this.doNeighbours) {
         EnumSet<EnumPlayerPart> neighboursSet = EnumSet.noneOf(EnumPlayerPart.class);
         EnumSet<EnumPlayerPart> blockedParts = EnumSet.copyOf(this.blockedParts);
         for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : this.partList) {
            blockedParts.addAll(Arrays.asList(pair.getRight()));
         }
         for (int i = this.partList.size() - 1; i >= 0; i--) {
            for (EnumPlayerPart part : this.partList.get(i).getRight()) {
               neighboursSet.addAll(part.getNeighbours());
            }
            neighboursSet.removeIf(blockedParts::contains);
            if (!neighboursSet.isEmpty()) {
               List<EnumPlayerPart> neighbours = new ArrayList<>(neighboursSet);
               Collections.shuffle(neighbours);
               Map<EquipmentSlot, List<EnumPlayerPart>> neighbourMapping = new LinkedHashMap<>();
               for (EnumPlayerPart neighbour : neighbours) {
                  neighbourMapping.computeIfAbsent(neighbour.slot, type -> new ArrayList<>(3)).add(neighbour);
               }
               List<Pair<EquipmentSlot, EnumPlayerPart[]>> neighbourDistributions = new ArrayList<>();
               for (Map.Entry<EquipmentSlot, List<EnumPlayerPart>> entry : neighbourMapping.entrySet()) {
                  neighbourDistributions.add(Pair.of(entry.getKey(), entry.getValue().toArray(new EnumPlayerPart[0])));
               }
               StandardDamageDistribution remainingDistribution = new StandardDamageDistribution(neighbourDistributions, false, true, blockedParts);
               rest = remainingDistribution.distributeDamage(rest, player, source, addStat);
               if (rest <= 0.0F) {
                  break;
               }
               blockedParts = remainingDistribution.blockedParts;
               neighboursSet.clear();
            }
         }
      }
      return rest;
   }
}
