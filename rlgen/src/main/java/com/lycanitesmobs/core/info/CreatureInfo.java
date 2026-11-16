package com.lycanitesmobs.core.info;

// STUB(lyc/server): stand-in for the 1.12 CreatureInfo until lyc/server pushes the real one. Only what the client uses.
public class CreatureInfo {
   public String name;
   public boolean enabled = true;
   public boolean dummy = false;
   public ModInfo modInfo;
   public String modelClassName;
   public double width = 0.8;
   public double height = 1.8;
   public double sizeScale = 1.0;

   public String getName() {
      return this.name;
   }
}
