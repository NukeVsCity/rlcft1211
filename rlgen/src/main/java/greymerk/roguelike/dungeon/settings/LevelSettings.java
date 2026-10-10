package greymerk.roguelike.dungeon.settings;

import com.github.fnar.minecraft.block.spawner.SpawnerSettings;
import com.google.common.collect.Sets;
import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.dungeon.base.RoomsSetting;
import greymerk.roguelike.dungeon.base.SecretsSetting;
import greymerk.roguelike.dungeon.layout.LayoutGenerator;
import greymerk.roguelike.dungeon.segment.SegmentGenerator;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.filter.Filter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelSettings {
   private static final int NUM_ROOMS = 12;
   private static final int LEVEL_RANGE = 80;
   private static final int MINIMUM_SCATTER = 12;
   private static final int DEFAULT_LEVEL = -1;
   private int numRooms = 12;
   private int range = 80;
   private int scatter = 12;
   private int level = -1;
   private RoomsSetting rooms = new RoomsSetting();
   private SecretsSetting secrets = new SecretsSetting();
   private Theme theme;
   private SegmentGenerator segments = new SegmentGenerator();
   private SpawnerSettings spawners = new SpawnerSettings();
   private LayoutGenerator.Type generator;
   private Set<Filter> filters = new HashSet<>();

   public LevelSettings(int level) {
      this.level = level;
   }

   public LevelSettings(LevelSettings toCopy) {
      this.init(toCopy);
   }

   public LevelSettings inherit(LevelSettings parent, Set<SettingsType> overrides) {
      this.numRooms = this.numRooms != parent.numRooms && this.numRooms != 12 ? this.numRooms : parent.numRooms;
      this.range = this.range != parent.range && this.range != 80 ? this.range : parent.range;
      this.setScatter(this.scatter != parent.scatter && this.scatter != 12 ? this.scatter : parent.scatter);
      this.level = (parent.level == this.level || this.level == -1) && parent.level != -1 ? parent.level : this.level;
      this.rooms = overrides.contains(SettingsType.ROOMS) ? new RoomsSetting(this.rooms) : this.rooms.inherit(parent.rooms);
      this.setSecrets(overrides.contains(SettingsType.SECRETS) ? new SecretsSetting(this.secrets) : new SecretsSetting(parent.secrets, this.secrets));
      this.setTheme(this.chooseTheme(parent, overrides));
      this.segments = this.segments.inherit(parent.segments);
      this.spawners = new SpawnerSettings(parent.spawners, this.spawners);
      this.generator = this.generator == null ? parent.generator : this.generator;
      this.filters.addAll(parent.filters);
      return this;
   }

   private Theme chooseTheme(LevelSettings parent, Set<SettingsType> overrides) {
      if (overrides.contains(SettingsType.THEMES) || parent.theme == null) {
         return this.theme;
      } else {
         return this.theme == null ? parent.theme : Theme.inherit(parent.theme, this.theme);
      }
   }

   private void init(LevelSettings toCopy) {
      this.numRooms = toCopy.numRooms;
      this.range = toCopy.range;
      this.scatter = toCopy.scatter;
      this.level = toCopy.level;
      this.rooms = toCopy.rooms != null ? new RoomsSetting(toCopy.rooms) : null;
      this.secrets = toCopy.secrets != null ? new SecretsSetting(toCopy.secrets) : null;
      this.theme = toCopy.theme;
      this.segments.add(toCopy.segments);
      this.spawners = new SpawnerSettings(toCopy.spawners);
      this.filters = Sets.newHashSet(toCopy.filters);
      this.generator = toCopy.generator;
   }

   public LayoutGenerator.Type getGeneratorType() {
      return this.generator != null ? this.generator : LayoutGenerator.Type.CLASSIC;
   }

   public void setGenerator(LayoutGenerator.Type type) {
      this.generator = type;
   }

   public int getScatter() {
      return this.scatter;
   }

   public void setScatter(int scatter) {
      this.scatter = Math.max(12, scatter);
   }

   public int getNumRooms() {
      return this.numRooms;
   }

   public void setNumRooms(int num) {
      this.numRooms = num;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int num) {
      this.level = num;
   }

   public void setLevel(Coord coord) {
      this.level = Dungeon.getLevel(coord.getY());
   }

   public LevelSettings withLevel(int level) {
      this.level = level;
      return this;
   }

   public RoomsSetting getRooms() {
      return this.rooms;
   }

   public void setRooms(RoomsSetting rooms) {
      this.rooms = rooms;
   }

   public SecretsSetting getSecrets() {
      return this.secrets;
   }

   public void setSecrets(SecretsSetting secrets) {
      this.secrets = secrets;
   }

   public SegmentGenerator getSegments() {
      return this.segments;
   }

   public void setSegments(SegmentGenerator segments) {
      this.segments = segments;
   }

   public Theme getTheme() {
      return this.theme != null ? this.theme : Themes.STONE.getThemeBase();
   }

   public void setTheme(Theme theme) {
      this.theme = theme;
   }

   public void setTheme(Themes theme) {
      this.theme = theme.getThemeBase();
   }

   public SpawnerSettings getSpawnerSettings() {
      return this.spawners;
   }

   public int getRange() {
      return this.range;
   }

   public void setRange(int range) {
      this.range = range;
   }

   public List<Filter> getFilters() {
      return new ArrayList<>(this.filters);
   }

   public void addFilter(Filter filter) {
      this.filters.add(filter);
   }

   @Override
   public boolean equals(Object o) {
      LevelSettings other = (LevelSettings)o;
      if (other.generator != this.generator) {
         return false;
      } else {
         return !this.secrets.equals(other.secrets) ? false : this.rooms.equals(other.rooms);
      }
   }

   public LayoutGenerator getLayoutGenerator() {
      return this.getGeneratorType().instantiate(this);
   }

   @Override
   public String toString() {
      return "LevelSettings(numRooms="
         + this.getNumRooms()
         + ", range="
         + this.getRange()
         + ", scatter="
         + this.getScatter()
         + ", level="
         + this.getLevel()
         + ", rooms="
         + this.getRooms()
         + ", secrets="
         + this.getSecrets()
         + ", theme="
         + this.getTheme()
         + ", segments="
         + this.getSegments()
         + ", spawners="
         + this.spawners
         + ", generator="
         + this.generator
         + ", filters="
         + this.getFilters()
         + ")";
   }
}
