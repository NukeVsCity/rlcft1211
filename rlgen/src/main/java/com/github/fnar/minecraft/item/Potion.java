package com.github.fnar.minecraft.item;

import com.github.fnar.util.Color;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;

public class Potion implements RldItem {
   private Potion.Effect effect;
   private Potion.Form form;
   private final List<com.github.fnar.minecraft.Effect> effects = Lists.newArrayList();
   private boolean isAmplified;
   private boolean isExtended;
   private Color color;

   public Potion() {
      this(Potion.Effect.AWKWARD);
   }

   public Potion(Potion.Effect effect) {
      this(effect, Potion.Form.REGULAR);
   }

   public Potion(Potion.Effect effect, Potion.Form form) {
      this(effect, form, Lists.newArrayList(), false, false);
   }

   public Potion(Potion.Effect effect, Potion.Form form, List<com.github.fnar.minecraft.Effect> effects, boolean isAmplified, boolean isExtended) {
      this.effect = effect;
      this.form = form;
      this.effects.addAll(effects);
      this.isAmplified = isAmplified;
      this.isExtended = isExtended;
   }

   public static Potion newPotion() {
      return new Potion();
   }

   @Override
   public ItemType getItemType() {
      return ItemType.POTION;
   }

   public Potion.Effect getEffect() {
      return this.effect;
   }

   public Potion withEffect(Potion.Effect effect) {
      this.effect = effect;
      return this;
   }

   public Potion.Form getForm() {
      return this.form;
   }

   public Potion withForm(Potion.Form Form) {
      this.form = Form;
      return this;
   }

   public List<com.github.fnar.minecraft.Effect> getEffects() {
      return this.effects;
   }

   public Potion withEffect(com.github.fnar.minecraft.Effect effects) {
      this.effects.add(effects);
      return this;
   }

   public boolean isAmplified() {
      return this.isAmplified;
   }

   public Potion withAmplification() {
      return this.withAmplification(true);
   }

   public Potion withAmplification(boolean isAmplified) {
      this.isAmplified = isAmplified;
      return this;
   }

   public boolean isExtended() {
      return this.isExtended;
   }

   public Potion withExtension() {
      return this.withExtension(true);
   }

   public Potion withExtension(boolean isExtended) {
      this.isExtended = isExtended;
      return this;
   }

   public Potion withColor(Color color) {
      this.color = color;
      return this;
   }

   public Color getColor() {
      return this.color;
   }

   public static enum Amplification {
      UNAMPLIFIED(0),
      LEVEL_ONE(1),
      LEVEL_TWO(2);

      private final int level;

      private Amplification(int level) {
         this.level = level;
      }

      public int getLevel() {
         return this.level;
      }

      public static Potion.Amplification chooseRandom(Random random) {
         return values()[random.nextInt(values().length)];
      }
   }

   public static enum Effect {
      AWKWARD,
      EMPTY,
      FIRE_RESISTANCE,
      HARMING,
      HEALING,
      INVISIBILITY,
      LEAPING,
      LEVITATION,
      LUCK,
      MUNDANE,
      NIGHT_VISION,
      POISON,
      REGENERATION,
      SLOWNESS,
      SLOW_FALLING,
      STRENGTH,
      SWIFTNESS,
      THICK,
      TURTLE_MASTER,
      WATER,
      WATER_BREATHING,
      WEAKNESS;

      public static final Potion.Effect[] BUFF = new Potion.Effect[]{HEALING, LEAPING, REGENERATION, STRENGTH, SWIFTNESS};
      public static final Potion.Effect[] HARMFUL = new Potion.Effect[]{HARMING, POISON, SLOWNESS, WEAKNESS};
      public static final Potion.Effect[] QUIRK = new Potion.Effect[]{FIRE_RESISTANCE, INVISIBILITY, LEVITATION, NIGHT_VISION, SLOW_FALLING, WATER_BREATHING};

      public static Potion.Effect chooseRandom(Random random) {
         return chooseRandomAmong(random, values());
      }

      public static Potion.Effect chooseRandomAmong(Random random, Potion.Effect[] effects) {
         return effects[random.nextInt(effects.length)];
      }

      public Potion asItem() {
         return Potion.newPotion().withEffect(this);
      }
   }

   public static enum Form {
      REGULAR,
      SPLASH,
      LINGERING;

      public static Potion.Form chooseRandom(Random rand) {
         return values()[rand.nextInt(values().length)];
      }
   }
}
