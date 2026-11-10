package com.github.alexthe666.iceandfire.client.model.util;

import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;

public enum EnumSeaSerpentAnimations {
   T_POSE(""),
   SWIM1("Swim1"),
   SWIM2("Swim2"),
   SWIM3("Swim3"),
   SWIM4("Swim4"),
   SWIM5("Swim5"),
   SWIM6("Swim6"),
   BITE1("Bite1"),
   BITE2("Bite2"),
   BITE3("Bite3"),
   ROAR1("Roar1"),
   ROAR2("Roar2"),
   ROAR3("Roar3"),
   DEAD("Dead"),
   JUMPING1("Jumping1"),
   JUMPING2("Jumping2");
   public IceAndFireTabulaModel seaserpent_model;
   private String fileSuffix;

   private EnumSeaSerpentAnimations(String fileSuffix) {
      this.fileSuffix = fileSuffix;
   }
   public static void initializeDragonModels() {
      {
         for (EnumSeaSerpentAnimations animation : values()) {
            try {
               animation.seaserpent_model = new IceAndFireTabulaModel(
                  TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/seaserpent/seaserpent" + animation.fileSuffix)
               );
            } catch (Exception var5) {
               System.out.println("sea serpent model at: seaserpent" + animation.fileSuffix + ".tbl doesn't exist!");
               var5.printStackTrace();
            }
         }
      }
   }
}
