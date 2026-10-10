package greymerk.roguelike.dungeon.base;

import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SecretsSetting {
   private final List<RoomSetting> secretRoomSettings = Lists.newArrayList();

   public SecretsSetting() {
   }

   public SecretsSetting(SecretsSetting toCopy) {
      this.secretRoomSettings.addAll(toCopy.secretRoomSettings);
   }

   public SecretsSetting(SecretsSetting base, SecretsSetting other) {
      if (base != null) {
         this.secretRoomSettings.addAll(base.secretRoomSettings);
      }

      if (other != null) {
         this.secretRoomSettings.addAll(other.secretRoomSettings);
      }
   }

   public static SecretsSetting getRandom(Random rand, int count) {
      SecretsSetting secrets = new SecretsSetting();
      IntStream.range(0, count).mapToObj(i -> RoomType.getRandomSecret(rand)).forEach(type -> secrets.add(type.newSingleRoomSetting()));
      return secrets;
   }

   public void add(RoomSetting roomSetting) {
      IntStream.range(0, roomSetting.getCount()).forEach(value -> this.secretRoomSettings.add(roomSetting));
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SecretsSetting)) {
         return false;
      } else {
         SecretsSetting other = (SecretsSetting)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$secretRoomSettings = this.getSecretRoomSettings();
            Object other$secretRoomSettings = other.getSecretRoomSettings();
            return this$secretRoomSettings == null ? other$secretRoomSettings == null : this$secretRoomSettings.equals(other$secretRoomSettings);
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof SecretsSetting;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $secretRoomSettings = this.getSecretRoomSettings();
      return result * 59 + ($secretRoomSettings == null ? 43 : $secretRoomSettings.hashCode());
   }

   public List<RoomSetting> getSecretRoomSettings() {
      return this.secretRoomSettings;
   }

   @Override
   public String toString() {
      return "SecretsSetting(secretRoomSettings=" + this.getSecretRoomSettings() + ")";
   }
}
