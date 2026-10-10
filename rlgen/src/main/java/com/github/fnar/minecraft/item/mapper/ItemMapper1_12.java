package com.github.fnar.minecraft.item.mapper;

import com.github.fnar.minecraft.Effect;
import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.item.Armour;
import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.Arrow;
import com.github.fnar.minecraft.item.Banner;
import com.github.fnar.minecraft.item.BlockItem;
import com.github.fnar.minecraft.item.CouldNotMapItemException;
import com.github.fnar.minecraft.item.Design;
import com.github.fnar.minecraft.item.DesignPattern;
import com.github.fnar.minecraft.item.Dye;
import com.github.fnar.minecraft.item.Firework;
import com.github.fnar.minecraft.item.Food;
import com.github.fnar.minecraft.item.Ingredient;
import com.github.fnar.minecraft.item.ItemMapper;
import com.github.fnar.minecraft.item.Material;
import com.github.fnar.minecraft.item.Miscellaneous;
import com.github.fnar.minecraft.item.Potion;
import com.github.fnar.minecraft.item.Record;
import com.github.fnar.minecraft.item.RldBaseItem;
import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.minecraft.item.Sapling;
import com.github.fnar.minecraft.item.Seed;
import com.github.fnar.minecraft.item.Shield;
import com.github.fnar.minecraft.item.StringlyNamedItem;
import com.github.fnar.minecraft.item.Tool;
import com.github.fnar.minecraft.item.Weapon;
import com.github.fnar.minecraft.material.Crop;
import com.github.fnar.util.Color;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.treasure.loot.Book;
import greymerk.roguelike.treasure.loot.Quality;
import greymerk.roguelike.util.DyeColor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/** rlgen: all of Roguelike Dungeons' 1.12 item mappers in one, giving 1.21 items (with the 1.12 quirks noted where kept). */
public class ItemMapper1_12 implements ItemMapper {
   /** RldBaseItemMapper1_12.RAND: one fixed random for every enchantment roll. */
   private static final Random RAND = new Random(0L);

   @Override
   public ItemStack map(RldItemStack rldItemStack) {
      RldItem item = rldItemStack.getItem();
      if (item instanceof Arrow arrow) {
         return arrow(arrow); // ArrowMapper ignored the stack size
      }
      ItemStack stack = switch (item.getItemType()) {
         case ARMOUR -> armour((Armour)item);
         case BOOK -> book((Book)item);
         case BANNER -> banner((Banner)item);
         case BLOCK -> block((BlockItem)item);
         case DYE -> new ItemStack(dye(((Dye)item).getDyeColor()));
         case FIREWORK -> enchanted((Firework)item, firework((Firework)item));
         case FOOD -> new ItemStack(food((Food)item));
         case INGREDIENT -> new ItemStack(ingredient((Ingredient)item));
         case MATERIAL -> new ItemStack(material((Material)item));
         case MISCELLANEOUS -> enchanted((Miscellaneous)item, new ItemStack(miscellaneous((Miscellaneous)item)));
         case POTION -> potion((Potion)item);
         case RECORD -> new ItemStack(record((Record)item));
         case SAPLING -> sapling((Sapling)item);
         case SEED -> new ItemStack(seed((Seed)item));
         case SHIELD -> shield((Shield)item);
         case STRINGLY_NAMED -> enchanted((StringlyNamedItem)item, stringly((StringlyNamedItem)item));
         case TOOL -> enchanted((Tool)item, new ItemStack(tool((Tool)item)));
         case WEAPON -> enchanted((Weapon)item, new ItemStack(weapon((Weapon)item)));
         default -> throw new CouldNotMapItemException(rldItemStack);
      };
      // BaseItemMapper1_12.map: damage, count, raw NBT, tags
      stack = Mc.withDamage(stack, rldItemStack.getDamage());
      stack.setCount(Math.max(1, rldItemStack.getCount()));
      if (rldItemStack.getTags() != null) {
         Mc.applyTags(stack, rldItemStack.getTags());
      }
      return stack;
   }

   // ------------------------------------------------------------------ RldBaseItemMapper1_12

   private static ItemStack enchanted(RldBaseItem rldItem, ItemStack itemStack) {
      if (rldItem.isPlzEnchant()) {
         itemStack = enchantItem(RAND, itemStack, rldItem.getPlzEnchantLevel());
      }
      for (com.github.fnar.minecraft.item.Enchantment e : rldItem.getEnchantments()) {
         Holder<Enchantment> holder = Mc.enchantment(enchantmentName(e.getEnchant()));
         if (holder != null) {
            itemStack.enchant(holder, e.getLevel());
         }
      }
      return itemStack;
   }

   public static ItemStack enchantItem(Random rand, ItemStack item, int enchantLevel) {
      if (item == null || Mc.access() == null) {
         return item;
      }
      return EnchantmentHelper.enchantItem(new LegacyRandomSource(rand.nextLong()), item, enchantLevel, Mc.access(),
         Mc.access().registryOrThrow(Registries.ENCHANTMENT).getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE));
   }

   private static String enchantmentName(com.github.fnar.minecraft.item.Enchantment.Effect effect) {
      return switch (effect) {
         case SHARPNESS -> "sharpness";
         case SMITE -> "smite";
         case BANE_OF_ARTHROPODS -> "bane_of_arthropods";
         case LOOTING -> "looting";
         case KNOCKBACK -> "knockback";
         case FIRE_ASPECT -> "fire_aspect";
         case AQUA_AFFINITY -> "aqua_affinity";
         case RESPIRATION -> "respiration";
         case FEATHER_FALLING -> "feather_falling";
         case DEPTH_STRIDER -> "depth_strider";
         case PROTECTION -> "protection";
         case BLAST_PROTECTION -> "blast_protection";
         case FIRE_PROTECTION -> "fire_protection";
         case PROJECTILE_PROTECTION -> "projectile_protection";
         case THORNS -> "thorns";
         case UNBREAKING -> "unbreaking";
         case EFFICIENCY -> "efficiency";
         case SILK_TOUCH -> "silk_touch";
         case FORTUNE -> "fortune";
         case POWER -> "power";
         case PUNCH -> "punch";
         case FLAME -> "flame";
         case INFINITY -> "infinity";
         case LURE -> "lure";
         case LUCK_OF_THE_SEA -> "luck_of_the_sea";
         case MENDING -> "mending";
         default -> "efficiency";
      };
   }

   // ------------------------------------------------------------------ equipment

   private static Item quality(Quality quality, Item wooden, Item stone, Item iron, Item golden, Item diamond) {
      return switch (quality) {
         case WOOD -> wooden;
         case STONE -> stone;
         case IRON -> iron;
         case GOLD -> golden;
         case DIAMOND -> diamond;
         default -> null;
      };
   }

   private static ItemStack armour(Armour rldItem) {
      Quality q = rldItem.getQuality();
      Item item = switch (rldItem.getArmourType()) {
         case HELMET -> quality(q, Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET);
         case CHESTPLATE -> quality(q, Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE);
         case HORSE -> quality(q, Items.SADDLE, Items.SADDLE, Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR);
         case LEGGINGS -> quality(q, Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS);
         case BOOTS -> quality(q, Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS);
         default -> throw new CouldNotMapItemException(rldItem);
      };
      ItemStack stack = enchanted(rldItem, new ItemStack(item));
      Color color = rldItem.getColor();
      if (color != null && Quality.WOOD.equals(q) && !ArmourType.HORSE.equals(rldItem.getArmourType())) {
         stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.asInt(), true));
      }
      return stack;
   }

   private static Item weapon(Weapon rldItem) {
      Quality q = rldItem.getQuality();
      return switch (rldItem.getWeaponType()) {
         case BOW -> Items.BOW;
         case SWORD -> quality(q, Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD);
         default -> throw new CouldNotMapItemException(rldItem);
      };
   }

   private static Item tool(Tool tool) {
      Quality q = tool.getQuality();
      return switch (tool.getToolType()) {
         case AXE -> quality(q, Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE);
         case FISHING_ROD -> Items.FISHING_ROD;
         case FLINT_AND_STEEL -> Items.FLINT_AND_STEEL;
         case HOE -> quality(q, Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE);
         case PICKAXE -> quality(q, Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE);
         case SHEARS -> Items.SHEARS;
         case SHOVEL -> quality(q, Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL);
         default -> throw new CouldNotMapItemException(tool);
      };
   }

   // ------------------------------------------------------------------ plain items

   private static Item material(Material item) {
      return switch (item.getMaterialType()) {
         case BONE -> Items.BONE;
         case BOOK -> Items.BOOK;
         case CLAY_BALL -> Items.CLAY_BALL;
         case COAL -> Items.COAL;
         case DIAMOND -> Items.DIAMOND;
         case EMERALD -> Items.EMERALD;
         case FEATHER -> Items.FEATHER;
         case FLINT -> Items.FLINT;
         case GOLD_INGOT -> Items.GOLD_INGOT;
         case GOLD_NUGGET -> Items.GOLD_NUGGET;
         case IRON_INGOT -> Items.IRON_INGOT;
         case IRON_NUGGET -> Items.IRON_NUGGET;
         case LEATHER -> Items.LEATHER;
         case PAPER -> Items.PAPER;
         case SLIME_BALL -> Items.SLIME_BALL;
         case SNOWBALL -> Items.SNOWBALL;
         case STICK -> Items.STICK;
         case STRING -> Items.STRING;
         case WHEAT -> Items.WHEAT;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   private static Item ingredient(Ingredient item) {
      return switch (item.getType()) {
         case BLAZE_POWDER -> Items.BLAZE_POWDER;
         case BROWN_MUSHROOM -> Items.BROWN_MUSHROOM;
         case FERMENTED_SPIDER_EYE -> Items.FERMENTED_SPIDER_EYE;
         case FISH -> Items.COD;
         case GHAST_TEAR -> Items.GHAST_TEAR;
         case GLASS_BOTTLE -> Items.GLASS_BOTTLE;
         case GLOWSTONE_DUST -> Items.GLOWSTONE_DUST;
         case GUNPOWDER -> Items.GUNPOWDER;
         case MAGMA_CREAM -> Items.MAGMA_CREAM;
         case NETHER_WART -> Items.NETHER_WART;
         case RABBIT_FOOT -> Items.RABBIT_FOOT;
         case REDSTONE -> Items.REDSTONE;
         case RED_MUSHROOM -> Items.RED_MUSHROOM;
         case SPECKLED_MELON -> Items.GLISTERING_MELON_SLICE;
         case SPIDER_EYE -> Items.SPIDER_EYE;
         case SUGAR -> Items.SUGAR;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   private static Item food(Food item) {
      return switch (item.getFoodType()) {
         case APPLE -> Items.APPLE;
         case BAKED_POTATO -> Items.BAKED_POTATO;
         case BREAD -> Items.BREAD;
         case CARROT -> Items.CARROT;
         case COOKED_BEEF -> Items.COOKED_BEEF;
         case COOKED_CHICKEN -> Items.COOKED_CHICKEN;
         case COOKED_FISH -> Items.COOKED_COD;
         case COOKED_MUTTON -> Items.COOKED_MUTTON;
         case COOKED_PORKCHOP -> Items.COOKED_PORKCHOP;
         case COOKIE -> Items.COOKIE;
         case GOLDEN_APPLE -> Items.GOLDEN_APPLE;
         case GOLDEN_CARROT -> Items.GOLDEN_CARROT;
         case MILK_BUCKET -> Items.MILK_BUCKET;
         case MUSHROOM_STEW -> Items.MUSHROOM_STEW;
         case RAW_COD -> Items.COD;
         case ROTTEN_FLESH -> Items.ROTTEN_FLESH;
         case POTATO -> Items.POTATO;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   private static Item miscellaneous(Miscellaneous item) {
      return switch (item.getType()) {
         case BOOK -> Items.BOOK;
         case ENCHANTED_BOOK -> Items.ENCHANTED_BOOK;
         case ENDER_PEARL -> Items.ENDER_PEARL;
         case ENDER_EYE -> Items.ENDER_EYE;
         case EXPERIENCE_BOTTLE -> Items.EXPERIENCE_BOTTLE;
         case GLASS_BOTTLE -> Items.GLASS_BOTTLE;
         case LEAD -> Items.LEAD;
         case SADDLE -> Items.SADDLE;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   private static Item record(Record item) {
      return switch (item.getSong()) {
         case THIRTEEN -> Items.MUSIC_DISC_13;
         case CAT -> Items.MUSIC_DISC_CAT;
         case BLOCKS -> Items.MUSIC_DISC_BLOCKS;
         case CHIRP -> Items.MUSIC_DISC_CHIRP;
         case FAR -> Items.MUSIC_DISC_FAR;
         case MALL -> Items.MUSIC_DISC_MALL;
         case MELLOHI -> Items.MUSIC_DISC_MELLOHI;
         case STAL -> Items.MUSIC_DISC_STAL;
         case STRAD -> Items.MUSIC_DISC_STRAD;
         case WARD -> Items.MUSIC_DISC_WARD;
         case ELEVEN -> Items.MUSIC_DISC_11;
         case WAIT -> Items.MUSIC_DISC_WAIT;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   /** SeedMapper1_12 gave the crop items, not seeds (beetroot, carrot, melon, wheat). */
   private static Item seed(Seed item) {
      return switch (item.getCrop()) {
         case BEETROOTS -> Items.BEETROOT;
         case CARROTS -> Items.CARROT;
         case COCOA -> com.github.fnar.minecraft.block.BlockMapper1_12.map(Crop.COCOA.getBrush()).asItem();
         case MELON -> Items.MELON;
         case NETHER_WART -> Items.NETHER_WART;
         case POTATOES -> Items.POTATO;
         case PUMPKIN -> Items.PUMPKIN_SEEDS;
         case WHEAT -> Items.WHEAT;
         default -> throw new CouldNotMapItemException(item);
      };
   }

   private static ItemStack sapling(Sapling item) {
      BlockType type = switch (item.getWood()) {
         case OAK -> BlockType.OAK_SAPLING;
         case SPRUCE -> BlockType.SPRUCE_SAPLING;
         case BIRCH -> BlockType.BIRCH_SAPLING;
         case JUNGLE -> BlockType.JUNGLE_SAPLING;
         case ACACIA -> BlockType.ACACIA_SAPLING;
         case DARK_OAK -> BlockType.DARK_OAK_SAPLING;
         default -> throw new CouldNotMapItemException(item);
      };
      return block(type.asItem());
   }

   private static ItemStack block(BlockItem block) {
      return new ItemStack(com.github.fnar.minecraft.block.BlockMapper1_12.map(block.getBlockType()).asItem());
   }

   private static ItemStack stringly(StringlyNamedItem item) {
      String name = item.getItemName();
      String[] parts = name.split(":");
      // settings use 1.12 names ("minecraft:planks"), so look them up through the 1.12 table first
      Item legacy = parts.length >= 2 && parts[0].equals("minecraft") ? com.nukevscity.rlgen.legacy.LegacyStates.item(parts[0] + ":" + parts[1], 0) : null;
      Item object = legacy != null ? legacy : Mc.item(name);
      if (object == null) {
         throw new DungeonSettingParseException("Invalid item: " + name + ". Check for typos, and ensure that it exists in this pack.");
      }
      return new ItemStack(object);
   }

   public static Item dye(DyeColor color) {
      Item i = Mc.item(color.name().toLowerCase(Locale.ROOT) + "_dye");
      return i != null ? i : Items.WHITE_DYE;
   }

   // ------------------------------------------------------------------ potions, arrows

   private static ItemStack potion(Potion potion) {
      Item form = potion.getForm() == Potion.Form.REGULAR ? Items.POTION : potion.getForm() == Potion.Form.SPLASH ? Items.SPLASH_POTION
         : Items.LINGERING_POTION;
      ItemStack stack = new ItemStack(form);
      List<MobEffectInstance> custom = new ArrayList<>();
      for (Effect effect : potion.getEffects()) {
         Holder<net.minecraft.world.effect.MobEffect> e = Mc.effect(effect.getType().getEffectID());
         if (e != null) {
            custom.add(new MobEffectInstance(e, effect.getDuration() * 20, effect.getAmplification(), false, true));
         }
      }
      Optional<Integer> color = Optional.ofNullable(potion.getColor()).map(Color::asInt);
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.of(potionType(potion.getEffect(), potion.isAmplified(), potion.isExtended())), color,
         custom));
      return stack;
   }

   static Holder<net.minecraft.world.item.alchemy.Potion> potionType(Potion.Effect effect, boolean amplified, boolean extended) {
      return switch (effect) {
         case HEALING -> amplified ? Potions.STRONG_HEALING : Potions.HEALING;
         case HARMING -> amplified ? Potions.STRONG_HARMING : Potions.HARMING;
         case REGENERATION -> extended ? Potions.LONG_REGENERATION : amplified ? Potions.STRONG_REGENERATION : Potions.REGENERATION;
         case POISON -> extended ? Potions.LONG_POISON : amplified ? Potions.STRONG_POISON : Potions.POISON;
         case STRENGTH -> extended ? Potions.LONG_STRENGTH : amplified ? Potions.STRONG_STRENGTH : Potions.STRENGTH;
         case WEAKNESS -> extended ? Potions.LONG_WEAKNESS : Potions.WEAKNESS;
         case SLOWNESS -> extended ? Potions.LONG_SLOWNESS : Potions.SLOWNESS;
         case SWIFTNESS -> extended ? Potions.LONG_SWIFTNESS : amplified ? Potions.STRONG_SWIFTNESS : Potions.SWIFTNESS;
         case FIRE_RESISTANCE -> extended ? Potions.LONG_FIRE_RESISTANCE : Potions.FIRE_RESISTANCE;
         case INVISIBILITY -> extended ? Potions.LONG_INVISIBILITY : Potions.INVISIBILITY;
         case LEAPING -> extended ? Potions.LONG_LEAPING : Potions.LEAPING;
         case NIGHT_VISION -> extended ? Potions.LONG_NIGHT_VISION : Potions.NIGHT_VISION;
         case WATER_BREATHING -> extended ? Potions.LONG_WATER_BREATHING : Potions.WATER_BREATHING;
         default -> Potions.AWKWARD; // 1.12 had no luck, levitation, turtle master or slow falling potion types here
      };
   }

   private static ItemStack arrow(Arrow arrow) {
      return arrow.getTip().map(potion -> {
         ItemStack stack = new ItemStack(Items.TIPPED_ARROW);
         stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionType(potion.getEffect(), false, false)));
         return stack;
      }).orElseGet(() -> new ItemStack(Items.ARROW));
   }

   // ------------------------------------------------------------------ books, banners, shields, fireworks

   private static ItemStack book(Book book) {
      ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
      List<Filterable<Component>> pages = new ArrayList<>();
      for (String page : book.getPages()) {
         pages.add(Filterable.passThrough(Component.literal(page)));
      }
      String title = Optional.ofNullable(book.getTitle()).orElse("Book");
      stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title.length() > 32 ? title.substring(0, 32) : title),
         Optional.ofNullable(book.getAuthor()).orElse("Anonymous"), 0, pages, true));
      return stack;
   }

   /** 1.12 dye colour names: light gray was "silver", so it fell back to white. */
   private static net.minecraft.world.item.DyeColor dyeColor(DyeColor color) {
      return color == DyeColor.LIGHT_GRAY ? net.minecraft.world.item.DyeColor.WHITE
         : net.minecraft.world.item.DyeColor.byName(color.name().toLowerCase(Locale.ROOT), net.minecraft.world.item.DyeColor.WHITE);
   }

   private static String patternName(DesignPattern p) {
      return switch (p.name()) {
         case "STRIPE_SMALL" -> "small_stripes";
         case "DIAGONAL_RIGHT" -> "diagonal_up_right";
         case "DIAGONAL_LEFT_MIRROR" -> "diagonal_up_left";
         case "DIAGONAL_RIGHT_MIRROR" -> "diagonal_right";
         case "CIRCLE_MIDDLE" -> "circle";
         case "RHOMBUS_MIDDLE" -> "rhombus";
         case "HALF_VERTICAL_MIRROR" -> "half_vertical_right";
         case "HALF_HORIZONTAL_MIRROR" -> "half_horizontal_bottom";
         default -> p.name().toLowerCase(Locale.ROOT);
      };
   }

   /** BannerMapper1_12 reused one NBT compound for every layer, so every layer came out as the last design. */
   private static BannerPatternLayers layers(List<Design> designs) {
      List<BannerPatternLayers.Layer> layers = new ArrayList<>();
      if (Mc.access() == null || designs.isEmpty()) {
         return BannerPatternLayers.EMPTY;
      }
      Design last = designs.get(designs.size() - 1);
      Optional<Holder.Reference<BannerPattern>> pattern = Mc.access().registryOrThrow(Registries.BANNER_PATTERN)
         .getHolder(ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.withDefaultNamespace(patternName(last.getDesignPattern()))));
      if (pattern.isEmpty()) {
         return BannerPatternLayers.EMPTY;
      }
      for (int i = 0; i < designs.size(); i++) {
         layers.add(new BannerPatternLayers.Layer(pattern.get(), dyeColor(last.getColor())));
      }
      return new BannerPatternLayers(layers);
   }

   private static ItemStack banner(Banner banner) {
      ItemStack stack = new ItemStack(Items.BLACK_BANNER); // 1.12 banner item, damage 0 = black
      stack.set(DataComponents.BANNER_PATTERNS, layers(banner.getDesigns()));
      return stack;
   }

   private static ItemStack shield(Shield shield) {
      ItemStack stack = new ItemStack(Items.SHIELD);
      stack.set(DataComponents.BANNER_PATTERNS, layers(shield.getDesigns()));
      stack.set(DataComponents.BASE_COLOR, net.minecraft.world.item.DyeColor.BLACK);
      return stack;
   }

   private static ItemStack firework(Firework firework) {
      List<FireworkExplosion> explosions = new ArrayList<>();
      for (Firework.Explosion e : firework.getExplosions()) {
         IntArrayList colors = new IntArrayList();
         e.getColors().forEach(c -> colors.add(c.asInt()));
         explosions.add(new FireworkExplosion(FireworkExplosion.Shape.byId(e.getShape().ordinal()), colors, new IntArrayList(), e.hasTrail(), e.hasFlicker()));
      }
      ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
      rocket.set(DataComponents.FIREWORKS, new Fireworks(firework.getFlightLength().ordinal(), explosions));
      return rocket;
   }
}
