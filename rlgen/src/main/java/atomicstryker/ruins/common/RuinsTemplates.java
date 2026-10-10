package atomicstryker.ruins.common;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

/**
 * rlgen: Ruins' FileHandler for the overworld, reading RLCraft's config/ruins_config and ruins.txt from the jar
 * (data/rlgen/ruins, index.txt lists the templates).
 */
public final class RuinsTemplates {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ROOT = "/data/rlgen/ruins/";
   private static final Pattern patternSpecificBiome = Pattern.compile("specific_([^=]++)=(.++)");
   private static volatile RuinsTemplates instance;

   private final HashMap<String, HashSet<RuinTemplate>> templates = new HashMap<>();
   private final HashMap<String, double[]> vars = new HashMap<>();
   public int triesPerChunkNormal = 6;
   public float chanceToSpawnNormal = 10.0F;
   public float templateInstancesMinDistance = 256.0F;
   public float anyRuinsMinDistance = 64.0F;
   public int anySpawnMinDistance = 32;
   public int anySpawnMaxDistance = Integer.MAX_VALUE;
   /** Largest half width of any template plus leveling buffer, for finding the ruins that reach a chunk. */
   public int maxRadius;

   public static RuinsTemplates get() {
      RuinsTemplates t = instance;
      if (t == null) {
         synchronized (RuinsTemplates.class) {
            t = instance;
            if (t == null) {
               t = new RuinsTemplates();
               t.load();
               instance = t;
            }
         }
      }
      return t;
   }

   public RuinTemplate getTemplate(Random random, String biome) {
      try {
         double rand = random.nextDouble() * this.vars.get(biome)[0];
         RuinTemplate retval = null;

         for (RuinTemplate ruinTemplate : this.templates.get(biome)) {
            retval = ruinTemplate;
            if ((rand -= ruinTemplate.getWeight()) < 0.0) {
               break;
            }
         }

         return retval;
      } catch (Exception var8) {
         return null;
      }
   }

   public boolean useGeneric(Random random, String biome) {
      double[] val = this.vars.get(biome);
      return "generic".equals(biome) || val != null && random.nextDouble() >= val[1];
   }

   private void load() {
      StringWriter log = new StringWriter();
      PrintWriter pw = new PrintWriter(log);
      Map<String, List<String>> byFolder = new HashMap<>();
      for (String line : readLines("index.txt")) {
         int slash = line.indexOf('/');
         if (slash > 0) {
            byFolder.computeIfAbsent(line.substring(0, slash), k -> new ArrayList<>()).add(line);
         }
      }

      HashSet<RuinTemplate> generic = new HashSet<>();
      this.templates.put("generic", generic);
      this.addRuins(pw, byFolder.getOrDefault("generic", List.of()), "generic", generic);

      for (Biome bgb : Biome.all()) {
         String bname = RuinTemplate.pathOf(bgb);
         HashSet<RuinTemplate> set = this.templates.computeIfAbsent(bname, k -> new HashSet<>());
         this.addRuins(pw, byFolder.getOrDefault(bname, List.of()), bname, set);
      }

      for (String bname : this.templates.keySet()) {
         double[] val = new double[2];
         for (RuinTemplate r : this.templates.get(bname)) {
            val[0] += r.getWeight();
         }
         this.vars.put(bname, val);
      }

      this.readOptions();
      pw.flush();
      String problems = log.toString().trim();
      if (!problems.isEmpty()) {
         LOGGER.warn("rlgen ruins template problems:\n{}", problems);
      }
      int count = 0;
      for (HashSet<RuinTemplate> s : this.templates.values()) {
         count += s.size();
      }
      LOGGER.info("rlgen: ruins templates loaded ({} biome entries)", count);
   }

   private void addRuins(PrintWriter pw, List<String> files, String name, HashSet<RuinTemplate> targetList) {
      for (String f : files) {
         try {
            String simple = f.substring(f.lastIndexOf('/') + 1);
            RuinTemplate r = new RuinTemplate(pw, ROOT + f, simple);
            if (r.acceptsDimension("overworld")) {
               targetList.add(r);
               this.maxRadius = Math.max(this.maxRadius, r.radius());

               for (String biomeName : r.getBiomesToSpawnIn()) {
                  for (Biome bgb : Biome.all()) {
                     if (RuinTemplate.pathOf(bgb).equals(biomeName) && !biomeName.equals(name)) {
                        this.templates.computeIfAbsent(biomeName, k -> new HashSet<>()).add(r);
                     }
                  }
               }
            }
         } catch (RuinTemplate.IncompatibleModException var17) {
            pw.println(var17.getMessage());
         } catch (Exception var18) {
            pw.println("There was a problem loading the file: " + f);
            var18.printStackTrace(pw);
         }
      }
   }

   private void readOptions() {
      for (String read : readLines("ruins.txt")) {
         Matcher matcher;
         String[] check = read.split("=");
         if (check[0].equals("tries_per_chunk_normal")) {
            this.triesPerChunkNormal = Integer.parseInt(check[1]);
         } else if (check[0].equals("chance_to_spawn_normal")) {
            this.chanceToSpawnNormal = Float.parseFloat(check[1]);
         } else if (check[0].equals("templateInstancesMinDistance")) {
            this.templateInstancesMinDistance = Float.parseFloat(check[1]);
         } else if (check[0].equals("anyRuinsMinDistance")) {
            this.anyRuinsMinDistance = Float.parseFloat(check[1]);
         } else if (check[0].equals("anySpawnMinDistance")) {
            int value = Integer.parseInt(check[1]);
            this.anySpawnMinDistance = value > 0 ? value : 0;
         } else if (check[0].equals("anySpawnMaxDistance")) {
            int value = Integer.parseInt(check[1]);
            this.anySpawnMaxDistance = value > 0 ? value : Integer.MAX_VALUE;
         } else if ((matcher = patternSpecificBiome.matcher(read)).matches()) {
            double[] val = this.vars.get(matcher.group(1));
            if (val != null) {
               val[1] = Math.min(Math.max(Double.parseDouble(matcher.group(2)) / 100.0, 0.0), 1.0);
            }
         }
      }
   }

   private static List<String> readLines(String file) {
      List<String> out = new ArrayList<>();
      try (InputStream in = RuinsTemplates.class.getResourceAsStream(ROOT + file)) {
         if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
               out.add(line.trim());
            }
         }
      } catch (Exception e) {
         LOGGER.error("rlgen: could not read ruins file {}", file, e);
      }
      return out;
   }
}
