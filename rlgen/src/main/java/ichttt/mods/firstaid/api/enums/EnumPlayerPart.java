package ichttt.mods.firstaid.api.enums;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

/** 1.12 EnumPlayerPart (ids 1..8, neighbours used to pass on damage that does not fit). */
public enum EnumPlayerPart {
   HEAD(EquipmentSlot.HEAD),
   LEFT_ARM(EquipmentSlot.CHEST),
   LEFT_LEG(EquipmentSlot.LEGS),
   LEFT_FOOT(EquipmentSlot.FEET),
   BODY(EquipmentSlot.CHEST),
   RIGHT_ARM(EquipmentSlot.CHEST),
   RIGHT_LEG(EquipmentSlot.LEGS),
   RIGHT_FOOT(EquipmentSlot.FEET);

   public static final EnumPlayerPart[] VALUES = values();
   public final byte id = (byte)(this.ordinal() + 1);
   private ImmutableList<EnumPlayerPart> neighbours;
   public final EquipmentSlot slot;

   EnumPlayerPart(EquipmentSlot slot) {
      this.slot = slot;
   }

   public synchronized ImmutableList<EnumPlayerPart> getNeighbours() {
      if (this.neighbours == null) {
         ImmutableList.Builder<EnumPlayerPart> builder = ImmutableList.builder();
         builder.addAll(this.getNeighboursDown());
         builder.addAll(this.getNeighboursUp());
         builder.addAll(this.getNeighboursLeft());
         builder.addAll(this.getNeighboursRight());
         this.neighbours = builder.build();
      }
      return this.neighbours;
   }

   public static EnumPlayerPart fromID(int id) {
      if (id < 1 || id > 8) {
         throw new IndexOutOfBoundsException("Invalid id " + id);
      }
      return VALUES[id - 1];
   }

   private List<EnumPlayerPart> getNeighboursUp() {
      return switch (this) {
         case BODY -> Collections.singletonList(HEAD);
         case LEFT_LEG, RIGHT_LEG -> Collections.singletonList(BODY);
         case LEFT_FOOT -> Collections.singletonList(LEFT_LEG);
         case RIGHT_FOOT -> Collections.singletonList(RIGHT_LEG);
         default -> Collections.emptyList();
      };
   }

   private List<EnumPlayerPart> getNeighboursDown() {
      return switch (this) {
         case BODY -> Arrays.asList(LEFT_LEG, RIGHT_LEG);
         case LEFT_LEG -> Collections.singletonList(LEFT_FOOT);
         case RIGHT_LEG -> Collections.singletonList(RIGHT_FOOT);
         case HEAD -> Collections.singletonList(BODY);
         default -> Collections.emptyList();
      };
   }

   private List<EnumPlayerPart> getNeighboursLeft() {
      return switch (this) {
         case BODY -> Collections.singletonList(LEFT_ARM);
         case RIGHT_LEG -> Collections.singletonList(LEFT_LEG);
         case RIGHT_FOOT -> Collections.singletonList(LEFT_FOOT);
         case RIGHT_ARM -> Collections.singletonList(BODY);
         default -> Collections.emptyList();
      };
   }

   private List<EnumPlayerPart> getNeighboursRight() {
      return switch (this) {
         case BODY -> Collections.singletonList(RIGHT_ARM);
         case LEFT_LEG -> Collections.singletonList(RIGHT_LEG);
         case LEFT_FOOT -> Collections.singletonList(RIGHT_FOOT);
         case LEFT_ARM -> Collections.singletonList(BODY);
         default -> Collections.emptyList();
      };
   }
}
