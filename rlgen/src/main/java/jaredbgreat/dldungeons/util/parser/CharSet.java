package jaredbgreat.dldungeons.util.parser;

public class CharSet {
   private static final int BYTES = 8;
   private int[] data = new int[8];
   private int bit;
   private int loc;

   public CharSet() {
   }

   public CharSet(char[] in) {
      this.add(in);
   }

   public CharSet(String in) {
      this.add(in.toCharArray());
   }

   public void add(char in) {
      if (in <= 255) {
         this.bit = in % ' ';
         this.loc = in / ' ';
         if ((this.data[this.loc] & 1 << this.bit) == 0) {
            this.data[this.loc] = this.data[this.loc] | 1 << this.bit;
         }
      }
   }

   public void remove(char in) {
      if (in <= 255) {
         this.bit = in % ' ';
         this.loc = in / ' ';
         if ((this.data[this.loc] & 1 << this.bit) != 0) {
            this.data[this.loc] = this.data[this.loc] & ~(1 << this.bit);
         }
      }
   }

   public void add(char[] in) {
      for (int i = 0; i < in.length; i++) {
         this.add(in[i]);
      }
   }

   public void remove(char[] in) {
      for (int i = 0; i < in.length; i++) {
         this.remove(in[i]);
      }
   }

   public void add(CharSet in) {
      for (int i = 0; i < 8; i++) {
         this.data[i] = this.data[i] | in.data[i];
      }
   }

   public void remove(CharSet in) {
      for (int i = 0; i < 8; i++) {
         this.data[i] = this.data[i] & ~in.data[i];
      }
   }

   public CharSet union(CharSet in) {
      CharSet out = new CharSet();

      for (int i = 0; i < 8; i++) {
         out.data[i] = in.data[i] | this.data[i];
      }

      return out;
   }

   public CharSet intersection(CharSet in) {
      CharSet out = new CharSet();

      for (int i = 0; i < 8; i++) {
         out.data[i] = in.data[i] & this.data[i];
      }

      return out;
   }

   public CharSet complement() {
      CharSet out = new CharSet();

      for (int i = 0; i < this.data.length; i++) {
         out.data[i] = ~this.data[i];
      }

      return out;
   }

   public boolean contains(char in) {
      if (in > 255) {
         return false;
      } else {
         this.bit = in % ' ';
         this.loc = in / ' ';
         return (this.data[this.loc] & 1 << this.bit) != 0;
      }
   }

   @Override
   public String toString() {
      StringBuilder b = new StringBuilder();
      b.append('[').append(System.lineSeparator());

      for (int i = 0; i < 256; i++) {
         if (this.contains((char)i)) {
            b.append((char)i);
         }
      }

      b.append(System.lineSeparator()).append('[').append(System.lineSeparator());
      return b.toString();
   }
}
