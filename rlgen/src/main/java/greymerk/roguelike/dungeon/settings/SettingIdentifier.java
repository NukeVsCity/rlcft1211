package greymerk.roguelike.dungeon.settings;


public class SettingIdentifier {
   private final String namespace;
   private final String path;

   public SettingIdentifier(String namespace, String name) {
      this.namespace = namespace.toLowerCase(java.util.Locale.ROOT);
      this.path = name.toLowerCase(java.util.Locale.ROOT);
   }

   public SettingIdentifier(String name) {
      String[] parts = name.split(":");
      String namespace = parts.length > 1 ? parts[0] : "default";
      String uniqueName = parts.length > 1 ? parts[1] : name;
      this.namespace = namespace.toLowerCase(java.util.Locale.ROOT);
      this.path = uniqueName.toLowerCase(java.util.Locale.ROOT);
   }

   public String getNamespace() {
      return this.namespace;
   }

   public String getName() {
      return this.path;
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof SettingIdentifier s && this.namespace.equals(s.namespace) && this.path.equals(s.path);
   }

   @Override
   public int hashCode() {
      return this.namespace.hashCode() * 31 + this.path.hashCode();
   }

   @Override
   public String toString() {
      return this.getNamespace() + ":" + this.getName();
   }
}
