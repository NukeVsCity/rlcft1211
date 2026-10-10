package greymerk.roguelike.dungeon.base;

import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.spawner.MobType;
import com.github.fnar.minecraft.block.spawner.Spawner;
import com.github.fnar.minecraft.block.spawner.SpawnerSettings;
import com.github.fnar.roguelike.worldgen.generatables.thresholds.Threshold;
import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.theme.BlockSet;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.IShape;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class BaseRoom {
   private final RoomSetting roomSetting;
   protected final LevelSettings levelSettings;
   protected final WorldEditor worldEditor;
   protected int wallDist = 9;
   protected int depth = 1;
   protected int ceilingHeight = 10 - this.depth - 1;

   public BaseRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      this.roomSetting = roomSetting;
      this.levelSettings = levelSettings;
      this.worldEditor = worldEditor;
   }

   public BaseRoom generate(Coord at, List<Direction> entrances) {
      this.generateWalls(at, entrances);
      this.generateFloor(at, entrances);
      this.generateCeiling(at, entrances);
      this.generateDecorations(at, entrances);
      this.generateDoorways(at, entrances);
      return this;
   }

   protected void generateWalls(Coord at, List<Direction> entrances) {
      IShape wallsRect = at.newHollowRect(this.getWallDist()).withHeight(this.getHeight()).down(this.getDepth());
      this.primaryWallBrush().fill(this.worldEditor, wallsRect, false, true);
   }

   protected void generateFloor(Coord at, List<Direction> entrances) {
      this.primaryFloorBrush().fill(this.worldEditor, at.newRect(this.getWallDist()).down(this.getDepth()));
   }

   protected void generateCeiling(Coord at, List<Direction> entrances) {
      this.primaryWallBrush().fill(this.worldEditor, at.newRect(this.getWallDist()).up(this.getCeilingHeight()));
   }

   protected void generateDecorations(Coord at, List<Direction> entrances) {
   }

   protected void generateDoorways(Coord origin, List<Direction> entrances) {
      this.generateDoorways(origin, entrances, this.getWallDist());
   }

   protected void generateDoorways(Coord origin, List<Direction> entrances, int distanceFromOrigin) {
      entrances.forEach(
         direction -> Threshold.generateDoorway(this.worldEditor, this.levelSettings, origin.copy().translate(direction, distanceFromOrigin), direction)
      );
   }

   protected void generateSpawner(Coord spawnerLocation, MobType... defaultMobs) {
      MobType[] mobTypes = defaultMobs.length > 0 ? defaultMobs : MobType.COMMON_MOBS;
      Spawner spawner = this.chooseSpawner(mobTypes);
      generateSpawnerSafe(this.worldEditor, spawner, spawnerLocation);
   }

   private Spawner chooseSpawner(MobType[] mobTypes) {
      Optional<SpawnerSettings> roomSpawnerSettings = this.getSpawnerSettings(this.roomSetting.getSpawnerId());
      if (roomSpawnerSettings.isPresent()) {
         return roomSpawnerSettings.get().chooseOneAtRandom(this.random());
      } else {
         SpawnerSettings levelSpawnerSettings = this.levelSettings.getSpawnerSettings();
         return !levelSpawnerSettings.isEmpty() ? levelSpawnerSettings.chooseOneAtRandom(this.random()) : MobType.asSpawner(mobTypes);
      }
   }

   private Optional<SpawnerSettings> getSpawnerSettings(String spawnerId) {
      try {
         if (spawnerId != null) {
            return Optional.ofNullable(Dungeon.settingsResolver.getByName(spawnerId))
               .map(dungeonSettings -> dungeonSettings.getLevelSettings(this.levelSettings.getLevel()))
               .map(LevelSettings::getSpawnerSettings);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return Optional.empty();
   }

   public static void generateSpawnerSafe(WorldEditor editor, Spawner spawner, Coord cursor) {
      try {
         editor.generateSpawner(spawner, cursor);
      } catch (Exception var4) {
         throw new RuntimeException("Tried to spawn empty spawner", var4);
      }
   }

   protected ChestType getChestTypeOrUse(ChestType defaultChestType) {
      return this.getRoomSetting().getChestType().orElse(defaultChestType);
   }

   protected void theFloorIsLava(Coord origin) {
      this.primaryLiquidBrush()
         .fill(
            this.worldEditor,
            RectSolid.newRect(
               origin.copy().north(this.getWallDist()).west(this.getWallDist()).down(),
               origin.copy().south(this.getWallDist()).east(this.getWallDist()).down(this.depth)
            ),
            true,
            false
         );
   }

   protected Theme theme() {
      return this.levelSettings.getTheme();
   }

   protected BlockSet primaryTheme() {
      return this.theme().getPrimary();
   }

   protected BlockSet secondaryTheme() {
      return this.theme().getSecondary();
   }

   protected BlockBrush primaryFloorBrush() {
      return this.primaryTheme().getFloor();
   }

   protected BlockBrush primaryLightBrush() {
      return this.primaryTheme().getLightBlock();
   }

   protected BlockBrush primaryLiquidBrush() {
      return this.primaryTheme().getLiquid();
   }

   protected BlockBrush primaryPillarBrush() {
      return this.primaryTheme().getPillar();
   }

   protected StairsBlock primaryStairBrush() {
      return this.primaryTheme().getStair();
   }

   protected BlockBrush primaryWallBrush() {
      return this.primaryTheme().getWall();
   }

   protected BlockBrush secondaryFloorBrush() {
      return this.secondaryTheme().getFloor();
   }

   protected BlockBrush secondaryLightBrush() {
      return this.secondaryTheme().getLightBlock();
   }

   protected BlockBrush secondaryLiquidBrush() {
      return this.secondaryTheme().getLiquid();
   }

   protected BlockBrush secondaryPillarBrush() {
      return this.secondaryTheme().getPillar();
   }

   protected StairsBlock secondaryStairBrush() {
      return this.secondaryTheme().getStair();
   }

   protected BlockBrush secondaryWallBrush() {
      return this.secondaryTheme().getWall();
   }

   protected Random random() {
      return this.worldEditor.getRandom();
   }

   protected Direction getEntrance(List<Direction> entrances) {
      return entrances.get(0);
   }

   protected Coord generateChestLocation(Coord origin) {
      Direction dir0 = Direction.randomCardinal(this.random());
      Direction dir1 = dir0.orthogonals()[this.random().nextBoolean() ? 0 : 1];
      return origin.copy().translate(dir0, 3).translate(dir1, 2);
   }

   public final int getSize() {
      return 1 + this.getWallDist();
   }

   protected final int getWallDist() {
      return this.wallDist;
   }

   public final int getHeight() {
      int depth = this.getDepth();
      int ceilingHeight = this.getCeilingHeight();
      return depth + (depth > 0 ? 1 : 0) + ceilingHeight + (ceilingHeight > 0 ? 1 : 0);
   }

   protected final int getCeilingHeight() {
      return this.ceilingHeight;
   }

   protected final int getDepth() {
      return this.depth;
   }

   public boolean isValidLocation(Coord at, Direction facing) {
      return at.newHollowRect(this.getWallDist()).withHeight(this.getHeight()).down(this.getDepth()).asList().stream().allMatch(this.worldEditor::isSolidBlock);
   }

   protected RoomSetting getRoomSetting() {
      return this.roomSetting;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BaseRoom)) {
         return false;
      } else {
         BaseRoom other = (BaseRoom)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getWallDist() != other.getWallDist()) {
            return false;
         } else if (this.getDepth() != other.getDepth()) {
            return false;
         } else if (this.getCeilingHeight() != other.getCeilingHeight()) {
            return false;
         } else {
            Object this$roomSetting = this.getRoomSetting();
            Object other$roomSetting = other.getRoomSetting();
            if (this$roomSetting == null ? other$roomSetting == null : this$roomSetting.equals(other$roomSetting)) {
               Object this$levelSettings = this.levelSettings;
               Object other$levelSettings = other.levelSettings;
               if (this$levelSettings == null ? other$levelSettings == null : this$levelSettings.equals(other$levelSettings)) {
                  Object this$worldEditor = this.worldEditor;
                  Object other$worldEditor = other.worldEditor;
                  return this$worldEditor == null ? other$worldEditor == null : this$worldEditor.equals(other$worldEditor);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof BaseRoom;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getWallDist();
      result = result * 59 + this.getDepth();
      result = result * 59 + this.getCeilingHeight();
      Object $roomSetting = this.getRoomSetting();
      result = result * 59 + ($roomSetting == null ? 43 : $roomSetting.hashCode());
      Object $levelSettings = this.levelSettings;
      result = result * 59 + ($levelSettings == null ? 43 : $levelSettings.hashCode());
      Object $worldEditor = this.worldEditor;
      return result * 59 + ($worldEditor == null ? 43 : $worldEditor.hashCode());
   }
}
