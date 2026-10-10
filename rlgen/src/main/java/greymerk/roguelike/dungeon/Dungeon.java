package greymerk.roguelike.dungeon;

import com.github.fnar.minecraft.block.Material;
import com.github.fnar.util.ReportThisIssueException;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.SettingsRandom;
import greymerk.roguelike.dungeon.settings.SettingsResolver;
import greymerk.roguelike.dungeon.settings.SpawnCriteria;
import greymerk.roguelike.dungeon.tasks.DungeonTaskRegistry;
import greymerk.roguelike.dungeon.tasks.IDungeonTask;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.VanillaStructure;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.filter.Filter;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dungeon {
   public static final int NUM_LAYERS = 5;
   public static final int VERTICAL_SPACING = 10;
   public static final int TOPLEVEL = 50;
   public static final int CHUNK_SIZE = 16;
   public static final int BOTTOM_OF_WORLD_HEIGHT = 5;
   public static SettingsResolver settingsResolver;
   public static final String MOD_ID = "roguelike";
   private static final Logger logger;
   private Coord origin;
   private final List<DungeonLevel> levels = new ArrayList<>();
   private final WorldEditor editor;

   public Dungeon(WorldEditor editor) {
      this.editor = editor;
   }

   public static void initResolver() throws Exception {
      settingsResolver = SettingsResolver.initSettingsResolver();
   }

   public static boolean canSpawnInChunk(int chunkX, int chunkZ, WorldEditor editor) {
      return RogueConfig.DUNGEONS_SPAWN_ENABLED.getBoolean()
         && SpawnCriteria.isValidDimension(getDimension(chunkX, chunkZ, editor))
         && isSpawnFrequencyHit(chunkX, chunkZ)
         && isSpawnChanceHit(chunkX, chunkZ);
   }

   private static int getDimension(int chunkX, int chunkZ, WorldEditor editor) {
      Coord coord = new Coord(chunkX * 16, 0, chunkZ * 16);
      return editor.getInfo(coord).getDimension();
   }

   private static boolean isSpawnFrequencyHit(int chunkX, int chunkZ) {
      int frequency = getSpawnFrequency();
      return chunkX % frequency == 0 && chunkZ % frequency == 0;
   }

   private static int getSpawnFrequency() {
      return 3 * Math.max(2, RogueConfig.DUNGEONS_SPAWN_FREQUENCY.getInt());
   }

   private static boolean isSpawnChanceHit(int chunkX, int chunkZ) {
      double spawnChance = RogueConfig.DUNGEONS_SPAWN_CHANCE.getDouble();
      Random rand = new Random((long)Objects.hash(chunkX, chunkZ, 31));
      return (double)rand.nextFloat() < spawnChance;
   }

   public static int getLevel(int y) {
      if (y >= 45) {
         return 0;
      } else if (y >= 35) {
         return 1;
      } else if (y >= 25) {
         return 2;
      } else {
         return y >= 15 ? 3 : 4;
      }
   }

   public void generate(DungeonSettings dungeonSettings, Coord coord) {
      logger.info("Trying to spawn dungeon with id {} at {}...", dungeonSettings.getId(), coord);

      try {
         this.origin = new Coord(coord.getX(), 50, coord.getZ());
         IntStream.range(0, dungeonSettings.getNumLevels()).mapToObj(dungeonSettings::getLevelSettings).map(DungeonLevel::new).forEach(this.levels::add);
         Arrays.stream(DungeonStage.values())
            .flatMap(stage -> DungeonTaskRegistry.getInstance().getTasks(stage).stream())
            .forEach(task -> this.performTaskSafely(dungeonSettings, task));
         logger.info("Successfully generated dungeon with id {} at {}.", dungeonSettings.getId(), coord);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   private void performTaskSafely(DungeonSettings dungeonSettings, IDungeonTask task) {
      try {
         task.execute(this.editor, this, dungeonSettings);
      } catch (Exception var4) {
         new ReportThisIssueException(var4).printStackTrace();
      }
   }

   public void spawnInChunk(Random rand, int chunkX, int chunkZ) {
      if (canSpawnInChunk(chunkX, chunkZ, this.editor)) {
         logger.info("Trying to spawn dungeon at chunkX {} and chunkZ {}...", chunkX, chunkZ);
         int x = chunkX * 16;
         int z = chunkZ * 16;
         this.selectLocation(rand, x, z)
            .ifPresent(coord -> this.getDungeonSettingsMaybe(coord).ifPresent(dungeonSettings -> this.generate(dungeonSettings, coord)));
      }
   }

   private Optional<Coord> selectLocation(Random rand, int x, int z) {
      int attempts = RogueConfig.DUNGEONS_SPAWN_ATTEMPTS.getInt();
      return IntStream.range(0, attempts).mapToObj(i -> getNearbyCoord(rand, x, z)).filter(this::canGenerateDungeonHere).findFirst();
   }

   private static Coord getNearbyCoord(Random random, int x, int z) {
      int distance = random.nextInt(getSpawnRadius());
      double angle = random.nextDouble() * 2.0 * Math.PI;
      int xOffset = (int)(Math.cos(angle) * (double)distance);
      int zOffset = (int)(Math.sin(angle) * (double)distance);
      return new Coord(x + xOffset, 0, z + zOffset);
   }

   private static int getSpawnRadius() {
      int spawnDiameter = getSpawnFrequency() * 16;
      return spawnDiameter / 2;
   }

   public boolean canGenerateDungeonHere(Coord coord) {
      Predicate<VanillaStructure> isTooCloseTo = structure -> this.hasStructureTooCloseBy(coord, structure);
      Set<VanillaStructure> structuresToCheckDistanceTo = RogueConfig.vanillaStructuresToCheckDistanceTo();
      if (!structuresToCheckDistanceTo.isEmpty() && structuresToCheckDistanceTo.stream().anyMatch(isTooCloseTo)) {
         return false;
      } else {
         int upperLimit = RogueConfig.UPPERLIMIT.getInt();
         int lowerLimit = RogueConfig.LOWERLIMIT.getInt();
         Coord cursor = new Coord(coord.getX(), upperLimit, coord.getZ());
         boolean ok = this.editor.isAirBlock(cursor) && this.canFindStartingCoord(lowerLimit, cursor) && this.isFreeOverhead(cursor) && this.isSolidBelow(cursor);
         if (Boolean.getBoolean("rlgen.rcdebug")) {
            logger.info("rlgen debug: site {} ok={} air={} ground={} cursorY={}", coord, ok, this.editor.isAirBlock(new Coord(coord.getX(), upperLimit, coord.getZ())),
               this.canFindStartingCoord(lowerLimit, new Coord(coord.getX(), upperLimit, coord.getZ())), cursor.getY());
         }
         return ok;
      }
   }

   private boolean hasStructureTooCloseBy(Coord coord, VanillaStructure structure) {
      int minimumDistanceRequired = RogueConfig.SPAWN_MINIMUM_DISTANCE_FROM_VANILLA_STRUCTURES.getInt();
      Coord structureCoord = this.editor.findNearestStructure(structure, coord);
      if (structureCoord == null) {
         logger.info("Did not detect structure \"{}\" within {} blocks of potential spawn location {}.", structure.name(), minimumDistanceRequired, coord);
         return false;
      } else {
         return coord.distance(structureCoord) < (double)minimumDistanceRequired;
      }
   }

   private boolean canFindStartingCoord(int lowerLimit, Coord cursor) {
      while (!this.editor.isValidGroundBlock(cursor)) {
         cursor.down();
         if (cursor.getY() < lowerLimit) {
            return false;
         }

         if (this.editor.isMaterialAt(Material.WATER, cursor)) {
            return false;
         }
      }

      return true;
   }

   private boolean isFreeOverhead(Coord cursor) {
      Coord start = cursor.copy().translate(new Coord(-4, 4, -4));
      Coord end = cursor.copy().translate(new Coord(4, 4, 4));

      for (Coord c : RectSolid.newRect(start, end)) {
         if (this.editor.isValidGroundBlock(c)) {
            return false;
         }
      }

      return true;
   }

   private boolean isSolidBelow(Coord cursor) {
      Coord start1 = cursor.copy().translate(new Coord(-4, -3, -4));
      Coord end1 = cursor.copy().translate(new Coord(4, -3, 4));
      int airCount = 0;

      for (Coord c : RectSolid.newRect(start1, end1)) {
         if (!this.editor.isValidGroundBlock(c)) {
            airCount++;
         }

         if (airCount > 8) {
            return false;
         }
      }

      return true;
   }

   private Optional<DungeonSettings> getDungeonSettingsMaybe(Coord coord) {
      if (RogueConfig.RANDOM.getBoolean()) {
         return Optional.of(new SettingsRandom(this.editor.getRandom()));
      } else {
         return settingsResolver == null ? Optional.empty() : settingsResolver.chooseDungeonSetting(this.editor, coord);
      }
   }

   public Coord getPosition() {
      return this.origin.copy();
   }

   public List<DungeonLevel> getLevels() {
      return this.levels;
   }

   public void generateLayout(WorldEditor editor) {
      Coord start = this.getPosition();

      for (DungeonLevel level : this.getLevels()) {
         start = level.generateLayout(editor, start).down(10);
      }
   }

   public void encase(WorldEditor editor) {
      if (RogueConfig.ENCASE.getBoolean()) {
         this.getLevels().forEach(level -> level.filter(editor, Filter.get(Filter.ENCASE)));
      }
   }

   public void tunnel(WorldEditor editor) {
      this.getLevels().forEach(level -> level.tunnel(editor));
   }

   public void generateSegments(WorldEditor editor) {
      this.getLevels().forEach(level -> level.generateSegments(editor));
   }

   public void generateRooms() {
      this.getLevels().forEach(DungeonLevel::generateRooms);
   }

   public void linkLevels(WorldEditor editor) {
      this.getLevels().stream().reduce(null, (prev, level) -> level.generateLinkers(editor, prev));
   }

   public void generateTower(WorldEditor editor, DungeonSettings settings) {
      TowerType tower = settings.getTower().getType();
      Theme theme = settings.getTower().getTheme();
      Coord at = this.getPosition();
      TowerType.instantiate(tower, editor, theme).generate(at);
   }

   public void applyFilters(WorldEditor editor) {
      this.getLevels().forEach(level -> level.applyFilters(editor));
   }

   static {
      try {
         RogueConfig.reload(false);
         initResolver();
      } catch (Exception var1) {
         org.apache.logging.log4j.LogManager.getLogger("roguelike").error("rlgen: roguelike settings failed to load", var1);
      }

      logger = LogManager.getLogger("roguelike");
   }
}
