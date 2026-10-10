package greymerk.roguelike.dungeon.tasks;

import greymerk.roguelike.dungeon.DungeonStage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DungeonTaskRegistry {
   private static final DungeonTaskRegistry singleton = new DungeonTaskRegistry();
   private final Map<DungeonStage, List<IDungeonTask>> tasks = new HashMap<>();

   private DungeonTaskRegistry() {
      this.addTask(DungeonStage.LAYOUT, new DungeonTaskLayout());
      this.addTask(DungeonStage.ENCASE, new DungeonTaskEncase());
      this.addTask(DungeonStage.TUNNELS, new DungeonTaskTunnels());
      this.addTask(DungeonStage.SEGMENTS, new DungeonTaskSegments());
      this.addTask(DungeonStage.ROOMS, new DungeonTaskRooms());
      this.addTask(DungeonStage.LINKS, new DungeonTaskLinks());
      this.addTask(DungeonStage.TOWER, new DungeonTaskTower());
      this.addTask(DungeonStage.FILTERS, new DungeonTaskFilters());
      this.addTask(DungeonStage.LOOT, new DungeonTaskLoot());
   }

   public static DungeonTaskRegistry getInstance() {
      return singleton;
   }

   public void addTask(DungeonStage stage, IDungeonTask task) {
      if (!this.tasks.containsKey(stage)) {
         this.tasks.put(stage, new ArrayList<>());
      }

      this.tasks.get(stage).add(task);
   }

   public List<IDungeonTask> getTasks(DungeonStage stage) {
      return (List<IDungeonTask>)(!this.tasks.containsKey(stage) ? new ArrayList<>() : this.tasks.get(stage));
   }
}
