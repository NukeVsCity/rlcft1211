package greymerk.roguelike.dungeon.settings;

import greymerk.roguelike.dungeon.settings.builtin.BuiltinBaseSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinLayoutSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinLootSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinRoomsSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinSecretsSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinSegmentsSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinThemeSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinDesertDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinForestDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinIceDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinJungleDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinMesaDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinMountainDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinPlainsDungeonSettings;
import greymerk.roguelike.dungeon.settings.builtin.dungeon.BuiltinSwampDungeonSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SettingsContainer {
   public static final String DEFAULT_NAMESPACE = "default";
   public static final String BUILTIN_NAMESPACE = "builtin";
   private final Map<String, Map<String, DungeonSettings>> settingsByNamespace = new HashMap<>();

   private SettingsContainer() {
      this.put(
         new BuiltinRoomsSettings(),
         new BuiltinSecretsSettings(),
         new BuiltinSegmentsSettings(),
         new BuiltinLayoutSettings(),
         new BuiltinThemeSettings(),
         new BuiltinLootSettings(),
         new BuiltinBaseSettings(),
         new BuiltinDesertDungeonSettings(),
         new BuiltinPlainsDungeonSettings(),
         new BuiltinJungleDungeonSettings(),
         new BuiltinSwampDungeonSettings(),
         new BuiltinMountainDungeonSettings(),
         BuiltinForestDungeonSettings.create(),
         new BuiltinMesaDungeonSettings(),
         new BuiltinIceDungeonSettings()
      );
   }

   public SettingsContainer(DungeonSettings... dungeonSettings) {
      this();
      this.put(dungeonSettings);
   }

   public SettingsContainer(Map<String, String> dungeonSettingsJsonByFileName) throws Exception {
      this();
      this.put(dungeonSettingsJsonByFileName);
   }

   public void put(Map<String, String> dungeonSettingsJsonByFileName) throws Exception {
      for (Entry<String, String> entry : dungeonSettingsJsonByFileName.entrySet()) {
         String filePath = entry.getKey();
         String setting = entry.getValue();

         try {
            this.put(setting);
         } catch (Exception var7) {
            throw new Exception("Error in: " + filePath + ": " + var7.getMessage(), var7);
         }
      }
   }

   public void put(String dungeonSettingsJson) throws Exception {
      DungeonSettingsParser.parseJson(dungeonSettingsJson).ifPresent(this::put);
   }

   public void put(DungeonSettings... dungeonSettings) {
      Arrays.stream(dungeonSettings).forEach(this::put);
   }

   public void put(List<DungeonSettings> dungeonSettings) {
      dungeonSettings.forEach(this::put);
   }

   private void put(DungeonSettings dungeonSettings) {
      String namespace = dungeonSettings.getNamespace();
      if (!this.containsNamespace(namespace)) {
         this.settingsByNamespace.put(namespace, new HashMap<>());
      }

      this.settingsByNamespace.get(namespace).put(dungeonSettings.getName(), dungeonSettings);
   }

   public Collection<DungeonSettings> getByNamespace(String namespace) {
      return (Collection<DungeonSettings>)(this.containsNamespace(namespace) ? this.settingsByNamespace.get(namespace).values() : new ArrayList<>());
   }

   public Collection<DungeonSettings> getBuiltinSettings() {
      return this.settingsByNamespace
         .entrySet()
         .stream()
         .filter(this::isBuiltIn)
         .map(Entry::getValue)
         .map(Map::values)
         .flatMap(Collection::stream)
         .collect(Collectors.toList());
   }

   private boolean isBuiltIn(Entry<String, Map<String, DungeonSettings>> entry) {
      return "builtin".equals(entry.getKey());
   }

   public Collection<DungeonSettings> getCustomSettings() {
      return this.settingsByNamespace
         .entrySet()
         .stream()
         .filter(e -> !this.isBuiltIn(e))
         .map(Entry::getValue)
         .map(Map::values)
         .flatMap(Collection::stream)
         .collect(Collectors.toList());
   }

   public DungeonSettings get(SettingIdentifier id) {
      if (!this.contains(id)) {
         throw new RuntimeException("Setting not found: " + id.toString());
      } else {
         return this.getNamespace(id).get(id.getName());
      }
   }

   private boolean contains(SettingIdentifier id) {
      return this.containsNamespace(id.getNamespace()) && this.getNamespace(id).containsKey(id.getName());
   }

   private boolean containsNamespace(String namespace) {
      return this.settingsByNamespace.containsKey(namespace);
   }

   private Map<String, DungeonSettings> getNamespace(SettingIdentifier id) {
      return this.settingsByNamespace.get(id.getNamespace());
   }

   @Override
   public String toString() {
      return this.settingsByNamespace
         .values()
         .stream()
         .map(Map::values)
         .flatMap(Collection::stream)
         .map(DungeonSettings::getId)
         .map(SettingIdentifier::toString)
         .collect(Collectors.joining(" "));
   }
}
