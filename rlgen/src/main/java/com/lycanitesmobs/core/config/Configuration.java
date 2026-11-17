package com.lycanitesmobs.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The parts of 1.12 net.minecraftforge.common.config.Configuration that Lycanites ConfigBase uses, reading and writing the same
 * .cfg format (so RLCraft's lycanitesmobs/*.cfg files load unchanged).
 */
public class Configuration {
   private final File file;
   private final Map<String, Category> categories = new TreeMap<>();

   public Configuration(File file) {
      this.file = file;
   }

   public static class Category {
      public String comment;
      public final Map<String, Property> properties = new TreeMap<>();

      public java.util.Set<String> keySet() {
         return this.properties.keySet();
      }

      public boolean containsKey(String key) {
         return this.properties.containsKey(key);
      }
   }

   public Category getCategory(String name) {
      return this.categories.computeIfAbsent(name, n -> new Category());
   }

   public boolean hasCategory(String name) {
      return this.categories.containsKey(name);
   }

   public void addCustomCategoryComment(String category, String comment) {
      this.getCategory(category).comment = comment;
   }

   private Property get(String category, String key, String def, Property.Type type) {
      Category cat = this.getCategory(category);
      Property p = cat.properties.get(key);
      if (p == null) {
         p = new Property(key, def, type);
         cat.properties.put(key, p);
      } else if (p.type != type && !p.isList()) {
         p.type = type;
      }
      return p;
   }

   public Property get(String category, String key, boolean def) {
      return this.get(category, key, Boolean.toString(def), Property.Type.BOOLEAN);
   }

   public Property get(String category, String key, int def) {
      return this.get(category, key, Integer.toString(def), Property.Type.INTEGER);
   }

   public Property get(String category, String key, double def) {
      return this.get(category, key, Double.toString(def), Property.Type.DOUBLE);
   }

   public Property get(String category, String key, String def) {
      return this.get(category, key, def, Property.Type.STRING);
   }

   public Property get(String category, String key, String[] def) {
      Category cat = this.getCategory(category);
      Property p = cat.properties.get(key);
      if (p == null) {
         p = new Property(key, def, Property.Type.STRING);
         cat.properties.put(key, p);
      }
      return p;
   }

   public Property get(String category, String key, boolean[] def) {
      String[] values = new String[def.length];
      for (int i = 0; i < def.length; i++) {
         values[i] = Boolean.toString(def[i]);
      }
      return this.getList(category, key, values, Property.Type.BOOLEAN);
   }

   public Property get(String category, String key, int[] def) {
      String[] values = new String[def.length];
      for (int i = 0; i < def.length; i++) {
         values[i] = Integer.toString(def[i]);
      }
      return this.getList(category, key, values, Property.Type.INTEGER);
   }

   public Property get(String category, String key, double[] def) {
      String[] values = new String[def.length];
      for (int i = 0; i < def.length; i++) {
         values[i] = Double.toString(def[i]);
      }
      return this.getList(category, key, values, Property.Type.DOUBLE);
   }

   private Property getList(String category, String key, String[] def, Property.Type type) {
      Category cat = this.getCategory(category);
      Property p = cat.properties.get(key);
      if (p == null) {
         p = new Property(key, def, type);
         cat.properties.put(key, p);
      }
      return p;
   }

   public void load() {
      if (!this.file.exists()) {
         return;
      }
      try (BufferedReader r = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8)) {
         String line;
         Category current = null;
         Property listProp = null;
         List<String> listValues = null;
         while ((line = r.readLine()) != null) {
            String t = line.trim();
            if (listProp != null) {
               if (t.equals(">")) {
                  listProp.setList(listValues.toArray(new String[0]));
                  listProp = null;
               } else if (!t.isEmpty()) {
                  listValues.add(t);
               }
               continue;
            }
            if (t.isEmpty() || t.startsWith("#")) {
               continue;
            }
            if (t.endsWith("{")) {
               String name = unquote(t.substring(0, t.length() - 1).trim());
               current = this.getCategory(name);
               continue;
            }
            if (t.equals("}")) {
               current = null;
               continue;
            }
            if (current == null || t.length() < 3 || t.charAt(1) != ':') {
               continue;
            }
            Property.Type type = Property.Type.of(t.charAt(0));
            String rest = t.substring(2);
            if (rest.endsWith("<")) {
               String key = unquote(rest.substring(0, rest.length() - 1).trim());
               listProp = new Property(key, new String[0], type);
               current.properties.put(key, listProp);
               listValues = new ArrayList<>();
               continue;
            }
            String key;
            String value;
            if (rest.startsWith("\"")) {
               int end = rest.indexOf('"', 1);
               key = rest.substring(1, end);
               int eq = rest.indexOf('=', end);
               value = eq < 0 ? "" : rest.substring(eq + 1);
            } else {
               int eq = rest.indexOf('=');
               key = rest.substring(0, eq);
               value = rest.substring(eq + 1);
            }
            current.properties.put(key, new Property(key, value, type));
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static String unquote(String s) {
      return s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s;
   }

   private static String quote(String key) {
      for (char c : key.toCharArray()) {
         if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.') {
            return "\"" + key + "\"";
         }
      }
      return key;
   }

   public void save() {
      try {
         if (this.file.getParentFile() != null) {
            this.file.getParentFile().mkdirs();
         }
         try (Writer w = Files.newBufferedWriter(this.file.toPath(), StandardCharsets.UTF_8)) {
            String nl = System.lineSeparator();
            w.write("# Configuration file" + nl + nl);
            String bar = "##########################################################################################################";
            for (Map.Entry<String, Category> e : this.categories.entrySet()) {
               Category cat = e.getValue();
               w.write(bar + nl + "# " + e.getKey() + nl + "#--------------------------------------------------------------------------------------------------------#" + nl);
               if (cat.comment != null) {
                  for (String c : cat.comment.split("\n")) {
                     w.write("# " + c + nl);
                  }
               }
               w.write(bar + nl + nl + quote(e.getKey()) + " {" + nl);
               boolean first = true;
               for (Property p : cat.properties.values()) {
                  if (p.comment != null) {
                     if (!first) {
                        w.write(nl);
                     }
                     for (String c : p.comment.split("\n")) {
                        w.write("    # " + c + nl);
                     }
                  }
                  first = false;
                  if (p.isList()) {
                     w.write("    " + p.type.id + ":" + quote(p.getName()) + " <" + nl);
                     for (String v : p.getStringList()) {
                        w.write("        " + v + nl);
                     }
                     w.write("     >" + nl);
                  } else {
                     w.write("    " + p.type.id + ":" + quote(p.getName()) + "=" + p.getString() + nl);
                  }
               }
               w.write("}" + nl + nl + nl);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
