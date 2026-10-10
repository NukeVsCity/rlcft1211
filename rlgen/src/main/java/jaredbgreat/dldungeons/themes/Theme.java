package jaredbgreat.dldungeons.themes;

import jaredbgreat.dldungeons.builder.RegisteredBlock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import com.nukevscity.rlgen.legacy.biome.Biome;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class Theme {
   public String name;
   public int version = 0;
   public HashSet<Type> biomes = new HashSet<>();
   public HashSet<Type> notIn = new HashSet<>();
   public HashSet<Biome> biomewl = new HashSet<>();
   public HashSet<Biome> biomebl = new HashSet<>();
   public EnumSet<ThemeType> type = EnumSet.noneOf(ThemeType.class);
   public EnumSet<ThemeFlags> flags = EnumSet.noneOf(ThemeFlags.class);
   public int minY;
   public int maxY;
   public boolean buildFoundation;
   public SizeElement sizes;
   public Element outside;
   public Element liquids;
   public Element subrooms;
   public Element islands;
   public Element pillars;
   public Element fences;
   public Element symmetry;
   public Element variability;
   public Element degeneracy;
   public Element complexity;
   public Element verticle;
   public Element entrances;
   public Element naturals;
   public int[] air;
   public int[] walls;
   public int[] caveWalls;
   public int[] floors;
   public int[] ceilings;
   public int[] fencing;
   public int[] liquid;
   public int[] pillarBlock;
   public int[] dimensionWhitelist;
   public ArrayList<String> commonMobs = new ArrayList<>();
   public ArrayList<String> hardMobs = new ArrayList<>();
   public ArrayList<String> bruteMobs = new ArrayList<>();
   public ArrayList<String> eliteMobs = new ArrayList<>();
   public ArrayList<String> bossMobs = new ArrayList<>();
   public ArrayList<String>[] allMobs;
   public String lootCat;

   public Theme() {
      this.minY = 10;
      this.maxY = 50;
      this.buildFoundation = false;
      this.sizes = new SizeElement(2, 5, 10, 5, 1);
      this.outside = new Element(25, 0, 0, 0, 0, 0);
      this.liquids = new Element(1, 30, 50, 20, 10, 0);
      this.subrooms = new Element(0, 5, 25, 60, 25, 5);
      this.islands = new Element(5, 50, 10, 50, 20, 0);
      this.pillars = new Element(5, 30, 60, 40, 20, 0);
      this.symmetry = new Element(5, 15, 30, 75, 25, 0);
      this.variability = new Element(5, 10, 25, 75, 50, 25);
      this.degeneracy = new Element(50, 5, 15, 50, 10, 0);
      this.complexity = new Element(5, 10, 25, 75, 15, 0);
      this.verticle = new Element(5, 10, 25, 20, 10, 0);
      this.entrances = new Element(2, 5, 25, 50, 15, 3);
      this.fences = new Element(15, 25, 55, 15, 0, 0);
      this.naturals = new Element(25, 5, 20, 10, 0, 0);
      this.air = makeBlockList(new String[0]);
      this.walls = makeBlockList(new String[0]);
      this.caveWalls = makeBlockList(new String[0]);
      this.floors = makeBlockList(new String[0]);
      this.ceilings = makeBlockList(new String[0]);
      this.fencing = makeBlockList(new String[0]);
      this.liquid = makeBlockList(new String[0]);
      this.pillarBlock = makeBlockList(new String[0]);
      this.dimensionWhitelist = new int[0];
      this.lootCat = "chests.cfg";
      this.fixMobs();
   }

   public void addMob(String mob, int level) {
      switch (level) {
         case 0:
            if (!this.commonMobs.contains(mob)) {
               this.commonMobs.add(mob);
            }

            return;
         case 1:
            if (!this.hardMobs.contains(mob)) {
               this.hardMobs.add(mob);
            }

            return;
         case 2:
            if (!this.bruteMobs.contains(mob)) {
               this.bruteMobs.add(mob);
            }

            return;
         case 3:
            if (!this.eliteMobs.contains(mob)) {
               this.eliteMobs.add(mob);
            }

            return;
         default:
            System.err.println("[DLDUNGEONS] Failed to add mob " + mob + " to theme " + this.name + ", illegal difficulty level " + level + " (use 0 to 3).");
      }
   }

   public void removeMob(String mob, int level) {
      switch (level) {
         case 0:
            while (this.commonMobs.contains(mob)) {
               this.commonMobs.remove(mob);
            }

            return;
         case 1:
            while (this.hardMobs.contains(mob)) {
               this.hardMobs.remove(mob);
            }

            return;
         case 2:
            while (this.bruteMobs.contains(mob)) {
               this.bruteMobs.remove(mob);
            }

            return;
         case 3:
            while (this.eliteMobs.contains(mob)) {
               this.eliteMobs.remove(mob);
            }

            return;
         default:
            System.err
               .println("[DLDUNGEONS] Failed to remove mob " + mob + " to theme " + this.name + ", illegal difficulty level " + level + " (use 0 to 3).");
      }
   }

   public void fixMobs() {
      this.allMobs = new ArrayList[5];
      this.allMobs[0] = this.printListDebug(this.commonMobs);
      this.allMobs[1] = this.printListDebug(this.hardMobs);
      this.allMobs[2] = this.printListDebug(this.bruteMobs);
      this.allMobs[3] = this.printListDebug(this.eliteMobs);
      this.allMobs[4] = this.printListDebug(this.bossMobs);
   }

   private ArrayList printListDebug(ArrayList list) {
      return list;
   }

   public void biomeRegister() {
      BiomeSets.registerTheme(this);
      BiomeSets.removeTheme(this);
   }

   @Override
   public String toString() {
      return this.name == null ? "null:" + super.toString() : this.name;
   }

   private static int makeDBlock(String block) {
      return RegisteredBlock.add(block);
   }

   private static int[] makeBlockList(String[] in) {
      int[] out = new int[in.length];

      for (int i = 0; i < in.length; i++) {
         out[i] = makeDBlock(in[i]);
      }

      return out;
   }
}
