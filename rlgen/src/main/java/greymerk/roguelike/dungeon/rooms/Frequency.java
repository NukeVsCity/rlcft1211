package greymerk.roguelike.dungeon.rooms;

public enum Frequency {
   RANDOM,
   SECRET,
   SINGLE;

   public boolean isSingle() {
      return this.equals(SINGLE);
   }

   public boolean isRandom() {
      return this.equals(RANDOM);
   }

   public boolean isSecret() {
      return this.equals(SECRET);
   }
}
