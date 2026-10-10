package greymerk.roguelike.dungeon.settings.builtin.dungeon;

import com.github.fnar.minecraft.item.Material;
import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.segment.Segment;
import greymerk.roguelike.dungeon.segment.SegmentGenerator;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;
import greymerk.roguelike.dungeon.settings.TowerSettings;
import greymerk.roguelike.dungeon.settings.builtin.BuiltinBaseSettings;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.treasure.loot.MinecraftItemLootItem;
import greymerk.roguelike.treasure.loot.rule.SingleUseLootRule;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.worldgen.filter.Filter;
import greymerk.roguelike.compat.BiomeDictionary.Type;

public class BuiltinJungleDungeonSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "jungle");

   public BuiltinJungleDungeonSettings() {
      super(ID);
      this.setExclusive(true);
      this.getInherit().add(BuiltinBaseSettings.ID);
      this.getCriteria().setBiomeTypes(Lists.newArrayList(new Type[]{Type.JUNGLE}));
      this.setTowerSettings(new TowerSettings(TowerType.JUNGLE, Themes.JUNGLE));

      for (int i = 0; i < 5; i++) {
         this.getLootRules().add(new SingleUseLootRule(new MinecraftItemLootItem(Material.Type.EMERALD.asItem(), 0, 1, 1 + i, 1), i, 6));
         this.getLootRules().add(new SingleUseLootRule(new WeightedChoice<>(Material.Type.DIAMOND.asItem().asStack(), 1), i, 3 + i * 3));
      }

      Themes[] themes = new Themes[]{Themes.JUNGLE, Themes.JUNGLE, Themes.MOSSY, Themes.MOSSY, Themes.NETHER};

      for (int level = 0; level < 5; level++) {
         LevelSettings levelSettings = new LevelSettings(level);
         if (level < 4) {
            levelSettings.setLevel(3);
            SegmentGenerator segments = new SegmentGenerator(Segment.MOSSYARCH);
            segments.add(Segment.SHELF, 2);
            segments.add(Segment.INSET, 2);
            segments.add(Segment.JUNGLE, 5);
            segments.add(Segment.SKULL, 1);
            segments.add(Segment.ARROW, 1);
            segments.add(Segment.CELL, 1);
            segments.add(Segment.SILVERFISH, 1);
            segments.add(Segment.CHEST, 1);
            segments.add(Segment.SPAWNER, 2);
            levelSettings.setSegments(segments);
            levelSettings.addFilter(Filter.VINE);
         }

         levelSettings.setTheme(themes[level].getThemeBase());
         this.getLevelSettings().put(level, levelSettings);
      }
   }
}
