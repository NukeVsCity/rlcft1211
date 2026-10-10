package greymerk.roguelike.util;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {
   List<String> args = new ArrayList<>();

   public ArgumentParser(List<String> args) {
      this.args.addAll(args);
   }

   public boolean hasEntry(int index) {
      return index < this.args.size();
   }

   public boolean match(int index, String toCompare) {
      return !this.hasEntry(index) ? false : this.args.get(index).equals(toCompare);
   }

   public String get(int index) {
      return !this.hasEntry(index) ? null : this.args.get(index);
   }

   @Override
   public String toString() {
      return this.args.toString();
   }
}
