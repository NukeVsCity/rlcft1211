package greymerk.roguelike.dungeon.tasks;

import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.worldgen.WorldEditor;

public interface IDungeonTask {
   void execute(WorldEditor var1, Dungeon var2, DungeonSettings var3);
}
