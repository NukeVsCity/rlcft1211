package com.nukevscity.rlgen.rc;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;

/**
 * Recurrent Complex boolean expressions ("$FOREST & !$SNOWY", "reccomplex:generic_solid & #1"):
 * operators ! & | and parentheses, & and | share one precedence (left to right), empty = true.
 */
public final class RcExpr<T> implements Predicate<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String source;
   private final Predicate<T> root;

   private RcExpr(String source, Predicate<T> root) {
      this.source = source;
      this.root = root;
   }

   public static <T> RcExpr<T> parse(String s, Function<String, Predicate<T>> vars) {
      if (s == null || s.isBlank()) {
         return new RcExpr<>("", t -> true);
      }
      List<String> tokens = tokenize(s);
      try {
         Parser<T> p = new Parser<>(tokens, vars);
         Predicate<T> root = p.expr();
         if (p.pos != tokens.size()) {
            throw new IllegalArgumentException("trailing " + tokens.subList(p.pos, tokens.size()));
         }
         return new RcExpr<>(s, root);
      } catch (RuntimeException e) {
         LOGGER.warn("rlgen: can't parse RC expression '{}': {}", s, e.getMessage());
         return new RcExpr<>(s, t -> false);
      }
   }

   public boolean isEmpty() {
      return this.source.isEmpty();
   }

   @Override
   public boolean test(T t) {
      return this.root.test(t);
   }

   @Override
   public String toString() {
      return this.source;
   }

   private static List<String> tokenize(String s) {
      List<String> out = new ArrayList<>();
      StringBuilder cur = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '(' || c == ')' || c == '&' || c == '|' || c == '!' || Character.isWhitespace(c)) {
            if (cur.length() > 0) {
               out.add(cur.toString());
               cur.setLength(0);
            }
            if (!Character.isWhitespace(c)) {
               out.add(String.valueOf(c));
            }
         } else {
            cur.append(c);
         }
      }
      if (cur.length() > 0) {
         out.add(cur.toString());
      }
      return out;
   }

   private static final class Parser<T> {
      final List<String> tokens;
      final Function<String, Predicate<T>> vars;
      int pos;

      Parser(List<String> tokens, Function<String, Predicate<T>> vars) {
         this.tokens = tokens;
         this.vars = vars;
      }

      String peek() {
         return this.pos < this.tokens.size() ? this.tokens.get(this.pos) : null;
      }

      Predicate<T> expr() {
         Predicate<T> left = this.unary();
         while (true) {
            String t = this.peek();
            if ("&".equals(t)) {
               this.pos++;
               Predicate<T> l = left;
               Predicate<T> r = this.unary();
               left = a -> l.test(a) && r.test(a);
            } else if ("|".equals(t)) {
               this.pos++;
               Predicate<T> l = left;
               Predicate<T> r = this.unary();
               left = a -> l.test(a) || r.test(a);
            } else {
               return left;
            }
         }
      }

      Predicate<T> unary() {
         String t = this.peek();
         if (t == null) {
            throw new IllegalArgumentException("unexpected end");
         }
         this.pos++;
         if (t.equals("!")) {
            Predicate<T> inner = this.unary();
            return a -> !inner.test(a);
         }
         if (t.equals("(")) {
            Predicate<T> inner = this.expr();
            if (!")".equals(this.peek())) {
               throw new IllegalArgumentException("missing )");
            }
            this.pos++;
            return inner;
         }
         if (t.equals(")") || t.equals("&") || t.equals("|")) {
            throw new IllegalArgumentException("unexpected " + t);
         }
         if (t.equals("true")) {
            return a -> true;
         }
         if (t.equals("false")) {
            return a -> false;
         }
         if (t.startsWith("global:")) {
            boolean v = RcBlocks.global(t.substring(7));
            return a -> v;
         }
         return this.vars.apply(t);
      }
   }

   /** Picks the longest matching prefix; returns the rest of the token, or null. */
   static String strip(String token, String prefix) {
      return token.startsWith(prefix) ? token.substring(prefix.length()) : null;
   }
}
