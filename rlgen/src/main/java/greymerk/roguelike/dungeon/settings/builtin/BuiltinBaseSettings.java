package greymerk.roguelike.dungeon.settings.builtin;

import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;

public class BuiltinBaseSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "base");

   public BuiltinBaseSettings() {
      super(ID);
      this.getInherit().add(BuiltinRoomsSettings.ID);
      this.getInherit().add(BuiltinSecretsSettings.ID);
      this.getInherit().add(BuiltinSegmentsSettings.ID);
      this.getInherit().add(BuiltinLayoutSettings.ID);
      this.getInherit().add(BuiltinThemeSettings.ID);
      this.getInherit().add(BuiltinLootSettings.ID);
   }
}
