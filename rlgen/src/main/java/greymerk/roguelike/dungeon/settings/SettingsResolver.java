package greymerk.roguelike.dungeon.settings;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;

public class SettingsResolver {
   private final SettingsContainer settingsContainer;

   public SettingsResolver(SettingsContainer settingsContainer) {
      this.settingsContainer = settingsContainer;
   }

   /** rlgen: RLCraft 2.9.3's settings folder, bundled in the mod jar (data/rlgen/roguelike/settings, listed in settings_index.txt). */
   public static SettingsResolver initSettingsResolver() throws Exception {
      Map<String, String> fileByName = new java.util.LinkedHashMap<>();
      try (java.io.InputStream index = SettingsResolver.class.getResourceAsStream("/data/rlgen/roguelike/settings_index.txt")) {
         for (String name : new String(index.readAllBytes(), Charsets.UTF_8).split("\n")) {
            name = name.trim();
            if (name.isEmpty()) {
               continue;
            }
            try (java.io.InputStream in = SettingsResolver.class.getResourceAsStream("/data/rlgen/roguelike/settings/" + name)) {
               fileByName.put(name, new String(in.readAllBytes(), Charsets.UTF_8));
            }
         }
      }
      SettingsContainer settings = new SettingsContainer(fileByName);
      return new SettingsResolver(settings);
   }

   public Optional<DungeonSettings> chooseDungeonSetting(WorldEditor editor, Coord coord) {
      Optional<DungeonSettings> customDungeon = this.chooseRandomCustomDungeonIfPossible(editor, coord);
      return customDungeon.isPresent() ? customDungeon : this.chooseOneBuiltinSettingAtRandom(editor, coord);
   }

   public DungeonSettings getByName(String name) {
      try {
         SettingIdentifier id = new SettingIdentifier(name);
         DungeonSettings dungeonSettings = this.settingsContainer.get(id);
         return this.processInheritance(dungeonSettings);
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public DungeonSettings processInheritance(DungeonSettings dungeonSettings) {
      DungeonSettings accumulatedInheritedSettings = dungeonSettings.getInherit()
         .stream()
         .map(this.settingsContainer::get)
         .map(this::processInheritance)
         .reduce(new DungeonSettings(), (accumulation, toInherit) -> toInherit.inherit(accumulation));
      return dungeonSettings.inherit(accumulatedInheritedSettings);
   }

   private Optional<DungeonSettings> chooseOneBuiltinSettingAtRandom(WorldEditor editor, Coord coord) {
      return !RogueConfig.SPAWNBUILTIN.getBoolean() ? Optional.empty() : this.chooseRandomValid(editor, coord, this.settingsContainer.getBuiltinSettings());
   }

   private Optional<DungeonSettings> chooseRandomCustomDungeonIfPossible(WorldEditor editor, Coord coord) {
      return this.chooseRandomValid(editor, coord, this.settingsContainer.getCustomSettings());
   }

   private Optional<DungeonSettings> chooseRandomValid(WorldEditor editor, Coord coord, Collection<DungeonSettings> settings) {
      List<DungeonSettings> inflatedSettings = settings.stream().map(this::processInheritance).filter(this.isValid(editor, coord)).collect(Collectors.toList());
      WeightedRandomizer<DungeonSettings> settingsRandomizer = this.newWeightedRandomizer(inflatedSettings);
      return Optional.ofNullable(settingsRandomizer.get(editor.getRandom()));
   }

   private Predicate<DungeonSettings> isValid(WorldEditor editor, Coord coord) {
      return setting -> setting.isExclusive() && setting.isValid(editor, coord);
   }

   private WeightedRandomizer<DungeonSettings> newWeightedRandomizer(List<DungeonSettings> dungeonSettings) {
      WeightedRandomizer<DungeonSettings> settingsRandomizer = new WeightedRandomizer<>();
      dungeonSettings.stream().map(setting -> new WeightedChoice<>(setting, setting.getCriteria().getWeight())).forEach(settingsRandomizer::add);
      return settingsRandomizer;
   }

   public String toString(String namespace) {
      return this.settingsContainer
         .getByNamespace(namespace)
         .stream()
         .map(DungeonSettings::getId)
         .map(SettingIdentifier::toString)
         .collect(Collectors.joining(" "));
   }

   @Override
   public String toString() {
      return this.settingsContainer.toString();
   }
}
