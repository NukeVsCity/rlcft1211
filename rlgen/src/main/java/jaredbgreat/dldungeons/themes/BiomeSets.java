package jaredbgreat.dldungeons.themes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import com.nukevscity.rlgen.legacy.biome.Biome;
import greymerk.roguelike.compat.BiomeDictionary;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class BiomeSets {
   static HashSet<Theme> forest = new HashSet<>();
   static HashSet<Theme> plains = new HashSet<>();
   static HashSet<Theme> mountain = new HashSet<>();
   static HashSet<Theme> hills = new HashSet<>();
   static HashSet<Theme> swamp = new HashSet<>();
   static HashSet<Theme> water = new HashSet<>();
   static HashSet<Theme> desert = new HashSet<>();
   static HashSet<Theme> frozen = new HashSet<>();
   static HashSet<Theme> jungle = new HashSet<>();
   static HashSet<Theme> wasteland = new HashSet<>();
   static HashSet<Theme> nether = new HashSet<>();
   static HashSet<Theme> end = new HashSet<>();
   static HashSet<Theme> mushroom = new HashSet<>();
   static HashSet<Theme> magical = new HashSet<>();
   static HashSet<Theme> hot = new HashSet<>();
   static HashSet<Theme> cold = new HashSet<>();
   static HashSet<Theme> sparse = new HashSet<>();
   static HashSet<Theme> dense = new HashSet<>();
   static HashSet<Theme> wet = new HashSet<>();
   static HashSet<Theme> dry = new HashSet<>();
   static HashSet<Theme> savanna = new HashSet<>();
   static HashSet<Theme> coniferous = new HashSet<>();
   static HashSet<Theme> spooky = new HashSet<>();
   static HashSet<Theme> dead = new HashSet<>();
   static HashSet<Theme> lush = new HashSet<>();
   static HashSet<Theme> mesa = new HashSet<>();
   static HashSet<Theme> ocean = new HashSet<>();
   static HashSet<Theme> sandy = new HashSet<>();
   static HashSet<Theme> snowy = frozen;
   static HashSet<Theme> minigame = new HashSet<>();
   static HashSet<Theme> nforest = new HashSet<>();
   static HashSet<Theme> nplains = new HashSet<>();
   static HashSet<Theme> nmountain = new HashSet<>();
   static HashSet<Theme> nhills = new HashSet<>();
   static HashSet<Theme> nswamp = new HashSet<>();
   static HashSet<Theme> nwater = new HashSet<>();
   static HashSet<Theme> ndesert = new HashSet<>();
   static HashSet<Theme> nfrozen = new HashSet<>();
   static HashSet<Theme> njungle = new HashSet<>();
   static HashSet<Theme> nwasteland = new HashSet<>();
   static HashSet<Theme> nnether = new HashSet<>();
   static HashSet<Theme> nend = new HashSet<>();
   static HashSet<Theme> nmushroom = new HashSet<>();
   static HashSet<Theme> nmagical = new HashSet<>();
   static HashSet<Theme> nhot = new HashSet<>();
   static HashSet<Theme> ncold = new HashSet<>();
   static HashSet<Theme> nsparse = new HashSet<>();
   static HashSet<Theme> ndense = new HashSet<>();
   static HashSet<Theme> nwet = new HashSet<>();
   static HashSet<Theme> ndry = new HashSet<>();
   static HashSet<Theme> nsavanna = new HashSet<>();
   static HashSet<Theme> nconiferous = new HashSet<>();
   static HashSet<Theme> nspooky = new HashSet<>();
   static HashSet<Theme> ndead = new HashSet<>();
   static HashSet<Theme> nlush = new HashSet<>();
   static HashSet<Theme> nmesa = new HashSet<>();
   static HashSet<Theme> nocean = new HashSet<>();
   static HashSet<Theme> nsandy = new HashSet<>();
   static HashSet<Theme> nsnowy = nfrozen;

   public static void registerTheme(Theme theme) {
      if (theme.biomes.contains(Type.END)) {
         end.add(theme);
      }

      if (theme.biomes.contains(Type.FOREST)) {
         forest.add(theme);
      }

      if (theme.biomes.contains(Type.SNOWY)) {
         frozen.add(theme);
      }

      if (theme.biomes.contains(Type.HILLS)) {
         hills.add(theme);
      }

      if (theme.biomes.contains(Type.JUNGLE)) {
         jungle.add(theme);
      }

      if (theme.biomes.contains(Type.MAGICAL)) {
         magical.add(theme);
      }

      if (theme.biomes.contains(Type.MOUNTAIN)) {
         mountain.add(theme);
      }

      if (theme.biomes.contains(Type.MUSHROOM)) {
         mushroom.add(theme);
      }

      if (theme.biomes.contains(Type.NETHER)) {
         nether.add(theme);
      }

      if (theme.biomes.contains(Type.PLAINS)) {
         plains.add(theme);
      }

      if (theme.biomes.contains(Type.SWAMP)) {
         swamp.add(theme);
      }

      if (theme.biomes.contains(Type.WASTELAND)) {
         wasteland.add(theme);
      }

      if (theme.biomes.contains(Type.WATER) || theme.biomes.contains(Type.BEACH)) {
         water.add(theme);
      }

      if (theme.biomes.contains(Type.OCEAN)) {
         ocean.add(theme);
      }

      if (theme.biomes.contains(Type.HOT)) {
         hot.add(theme);
      }

      if (theme.biomes.contains(Type.COLD)) {
         cold.add(theme);
      }

      if (theme.biomes.contains(Type.SPARSE)) {
         sparse.add(theme);
      }

      if (theme.biomes.contains(Type.DENSE)) {
         dense.add(theme);
      }

      if (theme.biomes.contains(Type.WET)) {
         wet.add(theme);
      }

      if (theme.biomes.contains(Type.DRY)) {
         dry.add(theme);
      }

      if (theme.biomes.contains(Type.SAVANNA)) {
         savanna.add(theme);
      }

      if (theme.biomes.contains(Type.CONIFEROUS)) {
         coniferous.add(theme);
      }

      if (theme.biomes.contains(Type.SPOOKY)) {
         spooky.add(theme);
      }

      if (theme.biomes.contains(Type.DEAD)) {
         dead.add(theme);
      }

      if (theme.biomes.contains(Type.LUSH)) {
         lush.add(theme);
      }

      if (theme.biomes.contains(Type.CONIFEROUS)) {
         coniferous.add(theme);
      }

      if (theme.biomes.contains(Type.MESA)) {
         mesa.add(theme);
      }

      if (theme.biomes.contains(Type.SANDY)) {
         sandy.add(theme);
      }
   }

   public static void removeTheme(Theme theme) {
      if (theme.notIn.contains(Type.END)) {
         nend.add(theme);
      }

      if (theme.notIn.contains(Type.FOREST)) {
         nforest.add(theme);
      }

      if (theme.notIn.contains(Type.SNOWY)) {
         nfrozen.add(theme);
      }

      if (theme.notIn.contains(Type.HILLS)) {
         nhills.add(theme);
      }

      if (theme.notIn.contains(Type.JUNGLE)) {
         njungle.add(theme);
      }

      if (theme.notIn.contains(Type.MAGICAL)) {
         nmagical.add(theme);
      }

      if (theme.notIn.contains(Type.MOUNTAIN)) {
         nmountain.add(theme);
      }

      if (theme.notIn.contains(Type.MUSHROOM)) {
         nmushroom.add(theme);
      }

      if (theme.notIn.contains(Type.NETHER)) {
         nnether.add(theme);
      }

      if (theme.notIn.contains(Type.PLAINS)) {
         nplains.add(theme);
      }

      if (theme.notIn.contains(Type.SWAMP)) {
         nswamp.add(theme);
      }

      if (theme.notIn.contains(Type.WASTELAND)) {
         nwasteland.add(theme);
      }

      if (theme.notIn.contains(Type.WATER) || theme.notIn.contains(Type.BEACH)) {
         nwater.add(theme);
      }

      if (theme.notIn.contains(Type.OCEAN)) {
         nocean.add(theme);
      }

      if (theme.notIn.contains(Type.HOT)) {
         nhot.add(theme);
      }

      if (theme.notIn.contains(Type.COLD)) {
         ncold.add(theme);
      }

      if (theme.notIn.contains(Type.SPARSE)) {
         nsparse.add(theme);
      }

      if (theme.notIn.contains(Type.DENSE)) {
         ndense.add(theme);
      }

      if (theme.notIn.contains(Type.WET)) {
         nwet.add(theme);
      }

      if (theme.notIn.contains(Type.DRY)) {
         ndry.add(theme);
      }

      if (theme.notIn.contains(Type.SAVANNA)) {
         nsavanna.add(theme);
      }

      if (theme.notIn.contains(Type.CONIFEROUS)) {
         nconiferous.add(theme);
      }

      if (theme.notIn.contains(Type.SPOOKY)) {
         nspooky.add(theme);
      }

      if (theme.notIn.contains(Type.DEAD)) {
         ndead.add(theme);
      }

      if (theme.notIn.contains(Type.LUSH)) {
         nlush.add(theme);
      }

      if (theme.notIn.contains(Type.CONIFEROUS)) {
         nconiferous.add(theme);
      }

      if (theme.notIn.contains(Type.MESA)) {
         nmesa.add(theme);
      }

      if (theme.notIn.contains(Type.SANDY)) {
         nsandy.add(theme);
      }
   }

   public static Theme getTheme(Biome biome, Random random, int dimID) {
      HashSet<Theme> set = new HashSet<>();
      ArrayList<Theme> use = new ArrayList<>();
      set.clear();
      use.clear();
      if (BiomeDictionary.hasType(biome, Type.END)) {
         set.addAll(end);
      }

      if (BiomeDictionary.hasType(biome, Type.FOREST)) {
         set.addAll(forest);
      }

      if (BiomeDictionary.hasType(biome, Type.SNOWY)) {
         set.addAll(frozen);
      }

      if (BiomeDictionary.hasType(biome, Type.HILLS)) {
         set.addAll(hills);
      }

      if (BiomeDictionary.hasType(biome, Type.JUNGLE)) {
         set.addAll(jungle);
      }

      if (BiomeDictionary.hasType(biome, Type.MAGICAL)) {
         set.addAll(magical);
      }

      if (BiomeDictionary.hasType(biome, Type.MOUNTAIN)) {
         set.addAll(mountain);
      }

      if (BiomeDictionary.hasType(biome, Type.MUSHROOM)) {
         set.addAll(mushroom);
      }

      if (BiomeDictionary.hasType(biome, Type.NETHER)) {
         set.addAll(nether);
      }

      if (BiomeDictionary.hasType(biome, Type.PLAINS)) {
         set.addAll(plains);
      }

      if (BiomeDictionary.hasType(biome, Type.SWAMP)) {
         set.addAll(swamp);
      }

      if (BiomeDictionary.hasType(biome, Type.WASTELAND)) {
         set.addAll(wasteland);
      }

      if (BiomeDictionary.hasType(biome, Type.WATER) || BiomeDictionary.hasType(biome, Type.BEACH)) {
         set.addAll(water);
      }

      if (BiomeDictionary.hasType(biome, Type.HOT)) {
         set.addAll(hot);
      }

      if (BiomeDictionary.hasType(biome, Type.COLD)) {
         set.addAll(cold);
      }

      if (BiomeDictionary.hasType(biome, Type.DENSE)) {
         set.addAll(dense);
      }

      if (BiomeDictionary.hasType(biome, Type.SPARSE)) {
         set.addAll(sparse);
      }

      if (BiomeDictionary.hasType(biome, Type.DRY)) {
         set.addAll(dry);
      }

      if (BiomeDictionary.hasType(biome, Type.WET)) {
         set.addAll(wet);
      }

      if (BiomeDictionary.hasType(biome, Type.SAVANNA)) {
         set.addAll(savanna);
      }

      if (BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
         set.addAll(coniferous);
      }

      if (BiomeDictionary.hasType(biome, Type.SPOOKY)) {
         set.addAll(spooky);
      }

      if (BiomeDictionary.hasType(biome, Type.DEAD)) {
         set.addAll(dead);
      }

      if (BiomeDictionary.hasType(biome, Type.LUSH)) {
         set.addAll(lush);
      }

      if (BiomeDictionary.hasType(biome, Type.MESA)) {
         set.addAll(mesa);
      }

      if (BiomeDictionary.hasType(biome, Type.SANDY)) {
         set.addAll(sandy);
      }

      if (BiomeDictionary.hasType(biome, Type.END)) {
         set.removeAll(nend);
      }

      if (BiomeDictionary.hasType(biome, Type.FOREST)) {
         set.removeAll(nforest);
      }

      if (BiomeDictionary.hasType(biome, Type.HILLS)) {
         set.removeAll(nhills);
      }

      if (BiomeDictionary.hasType(biome, Type.JUNGLE)) {
         set.removeAll(njungle);
      }

      if (BiomeDictionary.hasType(biome, Type.MAGICAL)) {
         set.removeAll(nmagical);
      }

      if (BiomeDictionary.hasType(biome, Type.MOUNTAIN)) {
         set.removeAll(nmountain);
      }

      if (BiomeDictionary.hasType(biome, Type.MUSHROOM)) {
         set.removeAll(nmushroom);
      }

      if (BiomeDictionary.hasType(biome, Type.NETHER)) {
         set.removeAll(nnether);
      }

      if (BiomeDictionary.hasType(biome, Type.PLAINS)) {
         set.removeAll(nplains);
      }

      if (BiomeDictionary.hasType(biome, Type.SWAMP)) {
         set.removeAll(nswamp);
      }

      if (BiomeDictionary.hasType(biome, Type.WASTELAND)) {
         set.removeAll(nwasteland);
      }

      if (BiomeDictionary.hasType(biome, Type.WATER) || BiomeDictionary.hasType(biome, Type.BEACH)) {
         set.removeAll(nwater);
      }

      if (BiomeDictionary.hasType(biome, Type.OCEAN)) {
         set.removeAll(nocean);
      }

      if (BiomeDictionary.hasType(biome, Type.HOT)) {
         set.removeAll(nhot);
      }

      if (BiomeDictionary.hasType(biome, Type.COLD)) {
         set.removeAll(ncold);
      }

      if (BiomeDictionary.hasType(biome, Type.DENSE)) {
         set.removeAll(ndense);
      }

      if (BiomeDictionary.hasType(biome, Type.SPARSE)) {
         set.removeAll(nsparse);
      }

      if (BiomeDictionary.hasType(biome, Type.DRY)) {
         set.removeAll(ndry);
      }

      if (BiomeDictionary.hasType(biome, Type.WET)) {
         set.removeAll(nwet);
      }

      if (BiomeDictionary.hasType(biome, Type.SAVANNA)) {
         set.removeAll(nsavanna);
      }

      if (BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
         set.removeAll(nconiferous);
      }

      if (BiomeDictionary.hasType(biome, Type.SPOOKY)) {
         set.removeAll(nspooky);
      }

      if (BiomeDictionary.hasType(biome, Type.DEAD)) {
         set.removeAll(ndead);
      }

      if (BiomeDictionary.hasType(biome, Type.LUSH)) {
         set.removeAll(nlush);
      }

      if (BiomeDictionary.hasType(biome, Type.MESA)) {
         set.removeAll(nmesa);
      }

      if (BiomeDictionary.hasType(biome, Type.SANDY)) {
         set.removeAll(nsandy);
      }

      HashSet<Theme> remove = new HashSet<>();

      for (Theme theme : set) {
         if (theme.dimensionWhitelist.length > 0) {
            boolean allowed = false;

            for (int dim : theme.dimensionWhitelist) {
               allowed = allowed || dim == dimID;
            }

            if (!allowed) {
               remove.add(theme);
            }
         }

         if (!theme.biomewl.isEmpty() && !theme.biomewl.contains(biome)) {
            remove.add(theme);
         }

         if (theme.biomebl.contains(biome)) {
            remove.add(theme);
         }
      }

      set.removeAll(remove);
      if (set.isEmpty()) {
         return null;
      } else {
         use.addAll(set);

         for (Theme var15 : use) {
            ;
         }

         return use.get(random.nextInt(use.size()));
      }
   }

   public static void reset() {
      forest.clear();
      plains.clear();
      mountain.clear();
      hills.clear();
      swamp.clear();
      water.clear();
      desert.clear();
      frozen.clear();
      jungle.clear();
      wasteland.clear();
      nether.clear();
      end.clear();
      mushroom.clear();
      magical.clear();
      hot.clear();
      cold.clear();
      sparse.clear();
      dense.clear();
      wet.clear();
      dry.clear();
      savanna.clear();
      coniferous.clear();
      spooky.clear();
      dead.clear();
      lush.clear();
      mesa.clear();
      ocean.clear();
      sandy.clear();
      snowy.clear();
      nforest.clear();
      nplains.clear();
      nmountain.clear();
      nhills.clear();
      nswamp.clear();
      nwater.clear();
      ndesert.clear();
      nfrozen.clear();
      njungle.clear();
      nwasteland.clear();
      nnether.clear();
      nend.clear();
      nmushroom.clear();
      nmagical.clear();
      nhot.clear();
      ncold.clear();
      nsparse.clear();
      ndense.clear();
      nwet.clear();
      ndry.clear();
      nsavanna.clear();
      nconiferous.clear();
      nspooky.clear();
      ndead.clear();
      nlush.clear();
      nmesa.clear();
      nocean.clear();
      nsandy.clear();
      nsnowy.clear();
      ThemeReader.readThemes();
   }
}
