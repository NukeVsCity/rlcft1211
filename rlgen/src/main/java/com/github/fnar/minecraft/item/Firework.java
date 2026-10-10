package com.github.fnar.minecraft.item;

import com.github.fnar.util.Color;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Firework extends RldBaseItem {
   private final List<Firework.Explosion> explosions = Lists.newArrayList();
   private Firework.FlightLength flightLength = Firework.FlightLength.SHORT;

   public static List<Color> randomColors(Random random) {
      int numberOfColours = random.nextInt(4) + 1;
      return IntStream.range(0, numberOfColours).mapToObj(i -> randomColor(random)).collect(Collectors.toList());
   }

   private static Color randomColor(Random random) {
      return Color.HSLToColor(random.nextFloat(), 1.0F, 0.5F);
   }

   public List<Firework.Explosion> getExplosions() {
      return this.explosions;
   }

   public Firework withExplosion(Firework.Explosion explosion) {
      this.explosions.add(explosion);
      return this;
   }

   public Firework.FlightLength getFlightLength() {
      return this.flightLength;
   }

   public Firework withFlightLength(Firework.FlightLength flightLength) {
      this.flightLength = flightLength;
      return this;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.FIREWORK;
   }

   public static class Explosion {
      private Firework.Explosion.Shape shape = Firework.Explosion.Shape.SMALL_BALL;
      private List<Color> colors = Lists.newArrayList();
      private boolean hasFlicker;
      private boolean hasTrail;

      public Firework.Explosion.Shape getShape() {
         return this.shape;
      }

      public Firework.Explosion withShape(Firework.Explosion.Shape shape) {
         this.shape = shape;
         return this;
      }

      public List<Color> getColors() {
         return this.colors;
      }

      public Firework.Explosion withColors(List<Color> colors) {
         this.colors.addAll(colors);
         return this;
      }

      public Firework.Explosion withColor(Color color) {
         this.colors.add(color);
         return this;
      }

      public boolean hasFlicker() {
         return this.hasFlicker;
      }

      public Firework.Explosion withFlicker(boolean hasFlicker) {
         this.hasFlicker = hasFlicker;
         return this;
      }

      public boolean hasTrail() {
         return this.hasTrail;
      }

      public Firework.Explosion withTrail(boolean hasTrail) {
         this.hasTrail = hasTrail;
         return this;
      }

      public static enum Shape {
         SMALL_BALL,
         LARGE_BALL,
         STAR,
         FACE,
         BURST;
      }
   }

   public static enum FlightLength {
      SHORT,
      MEDIUM,
      LONG;

      public static Firework.FlightLength chooseRandom(Random random) {
         Firework.FlightLength[] values = values();
         return values[random.nextInt(values.length)];
      }
   }
}
