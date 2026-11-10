package com.github.alexthe666.iceandfire.entity;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ice and Fire 1.7.1 EntitySnowVillager: a villager of the snow villages with one of three Ice and Fire professions (fisherman, craftsman,
 * shaman) and their ModVillagers trades, paid in sapphires. The villager AI is the 1.21 villager's (the 1.12 villager AI is not ported);
 * as in 1.12 every trade of an unlocked level is offered (1.21 villagers pick two).
 */
public class EntitySnowVillager extends Villager {
   public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, "iceandfire");
   public static final DeferredHolder<VillagerProfession, VillagerProfession> FISHERMAN = profession("fisherman");
   public static final DeferredHolder<VillagerProfession, VillagerProfession> CRAFTSMAN = profession("craftsman");
   public static final DeferredHolder<VillagerProfession, VillagerProfession> SHAMAN = profession("shaman");

   private static DeferredHolder<VillagerProfession, VillagerProfession> profession(String name) {
      return PROFESSIONS.register(name, () -> new VillagerProfession("iceandfire:" + name, h -> false, h -> false, ImmutableSet.of(), ImmutableSet.of(), null));
   }

   public static void register(IEventBus modBus) {
      PROFESSIONS.register(modBus);
   }

   public EntitySnowVillager(EntityType<? extends EntitySnowVillager> type, Level level) {
      super(type, level);
   }

   /** ModVillagers.professions: id 0 fisherman, 1 craftsman, 2 shaman */
   public static VillagerProfession byId(int id) {
      return switch (id) {
         case 1 -> CRAFTSMAN.get();
         case 2 -> SHAMAN.get();
         default -> FISHERMAN.get();
      };
   }

   public int getProfessionId() {
      VillagerProfession p = this.getVillagerData().getProfession();
      return p == CRAFTSMAN.get() ? 1 : p == SHAMAN.get() ? 2 : 0;
   }

   public void setProfessionId(int id) {
      this.setVillagerData(this.getVillagerData().setProfession(byId(Math.min(id, 2))));
      if (this.getVillagerXp() == 0) {
         this.setVillagerXp(1);
      }
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      return AirWaterUtils.isStone(this) ? InteractionResult.PASS : super.mobInteract(player, hand);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setProfessionId(this.getRandom().nextInt(3));
      return data;
   }

   @Override
   public Villager getBreedOffspring(ServerLevel level, AgeableMob other) {
      EntitySnowVillager child = AirWaterEntities.SNOW_VILLAGER.get().create(level);
      if (child != null) {
         child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.BREEDING, null);
      }
      return child;
   }

   @Override
   public void tick() {
      super.tick();
      VillagerProfession p = this.getVillagerData().getProfession();
      if (!this.level().isClientSide && p != FISHERMAN.get() && p != CRAFTSMAN.get() && p != SHAMAN.get()) {
         this.setProfessionId(this.getRandom().nextInt(3));
      }
   }

   /** 1.12 careers added every trade of the new level; 1.21 picks two. */
   @Override
   protected void updateTrades() {
      MerchantOffers offers = this.getOffers();
      for (Trade t : trades(this.getProfessionId(), this.getVillagerData().getLevel())) {
         MerchantOffer offer = t.make(this.random);
         if (offer != null) {
            offers.add(offer);
         }
      }
   }

   static Item iaf(String name) {
      return AirWaterRefs.item(name);
   }

   private record Price(int min, int max) {
      int get(RandomSource random) {
         return this.min >= this.max ? this.min : this.min + random.nextInt(this.max - this.min + 1);
      }
   }

   /** SapphireForItems (sell == false) and ListItemForSapphires (sell == true) */
   private record Trade(Supplier<ItemStack> item, Price price, boolean sell) {
      MerchantOffer make(RandomSource random) {
         Item sapphire = iaf("sapphire_gem");
         ItemStack stack = this.item.get();
         if (stack.isEmpty() || sapphire == Items.AIR) {
            return null;
         }
         int i = this.price.get(random);
         if (!this.sell) {
            return new MerchantOffer(new ItemCost(stack.getItem(), i), new ItemStack(sapphire), 7, 5, 0.05F);
         } else if (i < 0) {
            return new MerchantOffer(new ItemCost(sapphire, 1), stack.copyWithCount(-i), 7, 5, 0.05F);
         } else {
            return new MerchantOffer(new ItemCost(sapphire, i), stack.copyWithCount(1), 7, 5, 0.05F);
         }
      }
   }

   private static Trade buy(Item item, int min, int max) {
      return new Trade(() -> new ItemStack(item), new Price(min, max), false);
   }

   private static Trade buyIaf(String item, int min, int max) {
      return new Trade(() -> new ItemStack(iaf(item)), new Price(min, max), false);
   }

   private static Trade sell(Item item, int min, int max) {
      return new Trade(() -> new ItemStack(item), new Price(min, max), true);
   }

   private static Trade sellIaf(String item, int min, int max) {
      return new Trade(() -> new ItemStack(iaf(item)), new Price(min, max), true);
   }

   /** ModVillagers.init: the careers' trades, in their 1.12 order */
   static List<Trade> trades(int profession, int level) {
      return switch (profession) {
         case 0 -> switch (level) {
            case 1 -> List.of(buy(Items.COD, 1, 10), sellIaf("fishing_spear", 1, 5));
            case 2 -> List.of(sell(Items.FISHING_ROD, 1, 3), sell(Items.COOKED_COD, 1, 5), sell(Items.COOKED_COD, 1, 5));
            case 3 -> List.of(sell(Items.INK_SAC, 2, 1), sell(Items.TRIPWIRE_HOOK, 1, 3), sell(Items.PUFFERFISH, 1, 4));
            default -> List.of();
         };
         case 1 -> switch (level) {
            case 1 -> List.of(buy(Items.SNOW_BLOCK, 1, 32), sell(Items.IRON_SHOVEL, 1, 4));
            case 2 -> List.of(sell(Items.PACKED_ICE, 1, 3), sellIaf("silver_shovel", 1, 5), sell(Items.LEATHER, 10, 1));
            case 3 -> List.of(sellIaf("dragon_ice", 4, 1), sell(Items.DIAMOND_SHOVEL, 1, 9), sell(Items.LEATHER_BOOTS, 1, 3), sell(Items.LEATHER_HELMET, 1, 4),
               sell(Items.LEATHER_CHESTPLATE, 1, 6), sell(Items.LEATHER_LEGGINGS, 1, 6), sell(Items.DIAMOND_SHOVEL, 1, 7), sellIaf("troll_leather_frost", 1, 5));
            default -> List.of();
         };
         default -> switch (level) {
            case 1 -> List.of(buy(Items.BLAZE_POWDER, 2, 3), buy(Items.GHAST_TEAR, 1, 4), buyIaf("dragonbone", 1, 8), sellIaf("manuscript", 1, 2));
            case 2 -> List.of(buy(Items.BREWING_STAND, 9, 1), sellIaf("bestiary", 1, 3), sell(Items.ENDER_EYE, 2, 5), sellIaf("witherbone", 2, 5),
               sellIaf("wither_shard", 3, 2));
            case 3 -> List.of(sellIaf("ice_dragon_flesh", 1, 5), sellIaf("ice_dragon_blood", 1, 12), sellIaf("dragon_flute", 1, 5));
            default -> List.of();
         };
      };
   }
}
