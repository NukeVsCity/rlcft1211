package com.github.fnar.minecraft.entity;

import com.github.fnar.minecraft.CouldNotMapException;
import net.minecraft.world.entity.EquipmentSlot;

public class SlotMapper1_12 {
   public EquipmentSlot map(Slot slot) {
      switch (slot) {
         case CHEST:
            return EquipmentSlot.CHEST;
         case FEET:
            return EquipmentSlot.FEET;
         case HEAD:
            return EquipmentSlot.HEAD;
         case LEGS:
            return EquipmentSlot.LEGS;
         case MAINHAND:
            return EquipmentSlot.MAINHAND;
         case OFFHAND:
            return EquipmentSlot.OFFHAND;
         default:
            throw new CouldNotMapException(slot.toString());
      }
   }
}
