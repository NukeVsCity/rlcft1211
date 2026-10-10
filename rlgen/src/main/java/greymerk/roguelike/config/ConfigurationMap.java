package greymerk.roguelike.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigurationMap {
   private static final Pattern TRUE_PATTERN = Pattern.compile("^\\s*(?:t(?:rue)?|y(?:es)?)\\s*$");
   private static final Pattern FALSE_PATTERN = Pattern.compile("^\\s*(?:f(?:alse)?|no?)\\s*$");
   protected Map<String, Configuration> configurationsByName = new HashMap<>();

   public void remove(String name) {
      this.configurationsByName.remove(name);
   }

   public void put(String key, String value) {
      this.configurationsByName.put(key, new Configuration(key, value));
   }

   public boolean containsKey(String key) {
      return this.configurationsByName.containsKey(key);
   }

   public String get(String key) {
      if (key == null) {
         return null;
      } else {
         Configuration configuration = this.configurationsByName.get(key);
         return configuration == null ? null : configuration.getValue();
      }
   }

   public String get(String key, String fallback) {
      String value = this.get(key);
      return value == null ? fallback : value;
   }

   public double getDouble(String key, double fallback) {
      String value = this.get(key);
      if (value == null) {
         return fallback;
      } else {
         try {
            return Double.parseDouble(value);
         } catch (NumberFormatException var6) {
            return fallback;
         }
      }
   }

   public int getInteger(String key, int fallback) {
      String value = this.get(key);
      if (value == null) {
         return fallback;
      } else {
         try {
            return Integer.parseInt(value);
         } catch (NumberFormatException var5) {
            return fallback;
         }
      }
   }

   public boolean GetBoolean(String key, boolean fallback) {
      String value = this.get(key);
      if (value == null) {
         return fallback;
      } else if (TRUE_PATTERN.matcher(value).find()) {
         return true;
      } else {
         return FALSE_PATTERN.matcher(value).find() ? false : fallback;
      }
   }

   public List<Integer> getIntegers(String key, List<Integer> fallback) {
      String value = this.get(key);
      if (value == null) {
         return fallback;
      } else {
         String[] values = value.split(",");
         List<Integer> ints = new ArrayList<>();

         for (String s : values) {
            try {
               ints.add(Integer.parseInt(s));
            } catch (NumberFormatException var11) {
            }
         }

         return ints;
      }
   }

   public List<Double> getDoubles(String key, List<Double> fallback) {
      String value = this.get(key);
      if (value == null) {
         return fallback;
      } else {
         String[] values = value.split(",");
         List<Double> ints = new ArrayList<>();

         for (String s : values) {
            try {
               ints.add(Double.parseDouble(s));
            } catch (NumberFormatException var11) {
            }
         }

         return ints;
      }
   }

   public List<Configuration> asList() {
      return this.configurationsByName.values().stream().sorted(Comparator.comparing(Configuration::getKey)).collect(Collectors.toList());
   }
}
