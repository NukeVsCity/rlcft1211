package greymerk.roguelike.dungeon.settings.builtin;

import greymerk.roguelike.dungeon.base.SecretsSetting;
import greymerk.roguelike.dungeon.settings.DungeonSettings;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.dungeon.settings.SettingIdentifier;

public class BuiltinSecretsSettings extends DungeonSettings {
   public static final SettingIdentifier ID = new SettingIdentifier("builtin", "secrets");

   public BuiltinSecretsSettings() {
      super(ID);

      for (int level = 0; level < 5; level++) {
         SecretsSetting factory = new SecretsSetting();
         switch (level) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            default:
               LevelSettings levelSettings = new LevelSettings(level);
               levelSettings.setSecrets(factory);
               this.getLevelSettings().put(level, levelSettings);
         }
      }
   }
}
