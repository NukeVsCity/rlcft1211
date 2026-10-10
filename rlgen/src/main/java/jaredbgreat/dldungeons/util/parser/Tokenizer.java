package jaredbgreat.dldungeons.util.parser;

import java.util.Arrays;

public class Tokenizer {
   private static final CharSet hex = new CharSet("1234567890abcdefABCDEF");
   private final CharSet delim;
   private String[] tokens;
   private int token = 0;
   private String in;
   private int position = 0;
   private int size = 0;
   private char next = 0;
   private char[] scratchpad;
   boolean onTokens = false;
   boolean atEnd = false;

   public Tokenizer(String input, String delims) {
      this.delim = new CharSet(delims);
      this.in = input;
      this.readTokens();
   }

   private void nextChar() {
      this.next = this.in.charAt(this.position);
      if (this.next == '\\') {
         this.readEscape();
      }

      this.position++;
   }

   private void readTokens() {
      this.tokens = new String[this.in.length() / 5 + 1];
      this.scratchpad = new char[this.in.length()];

      while (this.position < this.in.length()) {
         this.nextChar();
         if (this.delim.contains(this.next)) {
            if (this.onTokens) {
               this.addToken();
               this.onTokens = false;
               this.size = 0;
            }
         } else if (!this.atEnd) {
            this.onTokens = true;
            if (this.next == '"') {
               this.readQuote();
            } else {
               this.scratchpad[this.size] = this.next;
               this.size++;
            }
         }
      }

      if (this.onTokens) {
         this.addToken();
         this.onTokens = false;
         this.size = 0;
      }

      this.tokens = Arrays.copyOf(this.tokens, this.token);
      this.token = 0;
   }

   private void addToken() {
      if (this.token >= this.tokens.length) {
         int newCapacity = Math.min(this.tokens.length * 2, this.tokens.length + 16);
         this.tokens = Arrays.copyOf(this.tokens, newCapacity);
      }

      this.tokens[this.token] = new String(this.scratchpad, 0, this.size);
      this.token++;
   }

   private void readQuote() {
      this.onTokens = true;
      this.nextChar();

      while (this.position < this.in.length() && this.next != '"') {
         this.scratchpad[this.size] = this.next;
         this.size++;
         this.nextChar();
      }
   }

   private void readEscape() {
      do {
         this.onTokens = true;
         this.position++;
         if (this.position >= this.in.length()) {
            return;
         }

         this.next = this.in.charAt(this.position);
         switch (this.next) {
            case '"':
               this.scratchpad[this.size] = '"';
               break;
            case '\'':
               this.scratchpad[this.size] = '\'';
               break;
            case '0':
               this.scratchpad[this.size] = 0;
               break;
            case 'b':
               this.scratchpad[this.size] = '\b';
               break;
            case 'f':
               this.scratchpad[this.size] = '\f';
               break;
            case 'n':
               this.scratchpad[this.size] = '\n';
               break;
            case 'r':
               this.scratchpad[this.size] = '\r';
               break;
            case 's':
               this.scratchpad[this.size] = ' ';
               break;
            case 't':
               this.scratchpad[this.size] = '\t';
               break;
            case 'u':
               this.scratchpad[this.size] = this.parseUnicode();
               break;
            default:
               this.scratchpad[this.size] = this.next;
         }

         this.size++;
         this.position++;
         if (this.position >= this.in.length()) {
            this.atEnd = true;
            return;
         }

         this.next = this.in.charAt(this.position);
      } while (this.next == '\\');
   }

   private char parseUnicode() {
      int l = 0;
      this.nextChar();

      StringBuilder nstring;
      for (nstring = new StringBuilder(); l < 4 && this.position < this.in.length(); l++) {
         this.nextChar();
         nstring.append(this.next);
      }

      this.position--;
      return (char)Integer.parseUnsignedInt(nstring.toString(), 16);
   }

   public int countTokens() {
      return this.tokens.length;
   }

   public boolean hasMoreTokens() {
      return this.token < this.tokens.length;
   }

   public String nextToken() {
      return this.hasMoreTokens() ? this.tokens[this.token++] : null;
   }

   public void reset() {
      this.token = 0;
   }

   public String getToken(int index) {
      return index >= 0 && index < this.tokens.length ? this.tokens[index] : null;
   }

   public String getString() {
      return this.in;
   }
}
