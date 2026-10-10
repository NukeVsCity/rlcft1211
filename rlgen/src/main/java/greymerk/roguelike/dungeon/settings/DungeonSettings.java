package greymerk.roguelike.dungeon.settings;

import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.treasure.TreasureManager;
import greymerk.roguelike.treasure.loot.LootRuleManager;
import greymerk.roguelike.treasure.loot.LootTableRule;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.PositionInfo;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class DungeonSettings {
   public static final int MAXIMUM_COUNT_OF_LEVELS = 5;
   private SettingIdentifier id;
   private final List<SettingIdentifier> inherit = new ArrayList<>();
   private boolean exclusive;
   private TowerSettings towerSettings;
   private final Map<Integer, LevelSettings> levels = new HashMap<>();
   private SpawnCriteria spawnCriteria = new SpawnCriteria();
   private final LootRuleManager lootRules = new LootRuleManager();
   private final List<LootTableRule> lootTables = new ArrayList<>();
   private final Set<SettingsType> overrides = new HashSet<>();

   public DungeonSettings() {
      IntStream.range(0, 5).mapToObj(LevelSettings::new).forEach(level -> {
         LevelSettings var10000 = this.levels.put(level.getLevel(), level);
      });
   }

   public DungeonSettings(String id) {
      this(new SettingIdentifier(id));
   }

   public DungeonSettings(SettingIdentifier id) {
      this();
      this.withId(id);
   }

   public DungeonSettings inherit(DungeonSettings toInherit) {
      DungeonSettings dungeonSettings = new DungeonSettings();
      dungeonSettings.id = this.id;
      dungeonSettings.exclusive = this.isExclusive();
      dungeonSettings.lootRules.merge(this.lootRules);
      dungeonSettings.lootTables.addAll(this.lootTables);
      if (!this.overrides.contains(SettingsType.LOOTRULES)) {
         dungeonSettings.lootRules.merge(toInherit.lootRules);
         dungeonSettings.lootTables.addAll(toInherit.lootTables);
      }

      dungeonSettings.towerSettings = dungeonSettings.getTowerSettings(toInherit, this);
      dungeonSettings.spawnCriteria = this.spawnCriteria.inherit(toInherit.spawnCriteria);
      IntStream.range(0, 5)
         .forEach(
            level -> {
               LevelSettings parent = toInherit.levels.get(level);
               LevelSettings child = this.levels.get(level);
               dungeonSettings.levels
                  .put(
                     level,
                     parent == null
                        ? (child == null ? new LevelSettings(level) : new LevelSettings(child).withLevel(level))
                        : (child == null ? new LevelSettings(parent) : new LevelSettings(child).inherit(parent, dungeonSettings.overrides))
                  );
            }
         );
      return dungeonSettings;
   }

   private TowerSettings getTowerSettings(DungeonSettings parent, DungeonSettings child) {
      if (this.getOverrides().contains(SettingsType.TOWER) && child.towerSettings != null) {
         return new TowerSettings(child.towerSettings);
      } else {
         return parent.towerSettings == null && child.towerSettings == null ? null : new TowerSettings(parent.towerSettings, child.towerSettings);
      }
   }

   public DungeonSettings(DungeonSettings toCopy) {
      this.id = toCopy.id;
      this.inherit.addAll(toCopy.inherit);
      this.overrides.addAll(toCopy.overrides);
      this.exclusive = toCopy.exclusive;
      this.lootRules.merge(toCopy.lootRules);
      this.lootTables.addAll(toCopy.lootTables);

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = toCopy.levels.get(level);
         this.levels.put(level, Optional.ofNullable(levelSettings).map(LevelSettings::new).orElse(new LevelSettings(level)));
      }

      this.spawnCriteria = new SpawnCriteria(toCopy.spawnCriteria);
      this.towerSettings = toCopy.towerSettings != null ? new TowerSettings(toCopy.towerSettings) : null;
   }

   public SettingIdentifier getId() {
      return this.id;
   }

   public void setId(SettingIdentifier id) {
      this.id = id;
   }

   public DungeonSettings withId(SettingIdentifier id) {
      this.id = id;
      return this;
   }

   public String getNamespace() {
      return Optional.ofNullable(this.getId().getNamespace()).orElse("default");
   }

   public String getName() {
      return this.getId().getName();
   }

   public void setSpawnCriteria(SpawnCriteria spawnCriteria) {
      this.spawnCriteria = spawnCriteria;
   }

   public boolean isValid(WorldEditor editor, Coord pos) {
      PositionInfo positionInfo = editor.getInfo(pos);
      return this.getCriteria().isValid(positionInfo);
   }

   public LevelSettings getLevelSettings(int level) {
      return this.getLevelSettings().get(level);
   }

   public TowerSettings getTower() {
      return this.towerSettings == null ? new TowerSettings(TowerType.ROGUE, Themes.STONE) : this.towerSettings;
   }

   public int getNumLevels() {
      return 5;
   }

   public Set<SettingsType> getOverrides() {
      return this.overrides;
   }

   public boolean isExclusive() {
      return this.exclusive;
   }

   public void processLoot(TreasureManager treasure) {
      this.getLootRules().process(treasure);
      this.getLootTables().forEach(table -> table.process(treasure));
   }

   public LootRuleManager getLootRules() {
      return this.lootRules;
   }

   public void setExclusive(boolean exclusive) {
      this.exclusive = exclusive;
   }

   public void setTowerSettings(TowerSettings towerSettings) {
      this.towerSettings = towerSettings;
   }

   public List<SettingIdentifier> getInherit() {
      return this.inherit;
   }

   public Map<Integer, LevelSettings> getLevelSettings() {
      return this.levels;
   }

   public SpawnCriteria getCriteria() {
      return this.spawnCriteria;
   }

   public List<LootTableRule> getLootTables() {
      return this.lootTables;
   }
}
