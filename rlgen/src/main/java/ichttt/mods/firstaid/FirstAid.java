package ichttt.mods.firstaid;

import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart.PartHealer;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.items.ItemHealing;
import ichttt.mods.firstaid.common.items.ItemMorphine;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.mixin.LivingEntityAccessor;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * First Aid 1.6.22 as in RLCraft 2.9.3, ported from the 1.12 code. The damage model is the firstaid:damage_model attachment; the
 * 1.12 hooks that need the inside of vanilla (LivingHurtEvent cancel, the DataManagerWrapper that watches setHealth and absorption,
 * the poison override) are mixins (ichttt.mods.firstaid.mixin, firstaid.mixins.json).
 */
@Mod(FirstAid.MODID)
public class FirstAid {
   public static final String MODID = "firstaid";
   public static final String VERSION = "1.6.22";
   public static final Logger LOGGER = LogManager.getLogger("firstaid");
   public static final Random RAND = new Random();
   /** client side: the damage model of the server has arrived (MessageConfiguration) */
   public static boolean isSynced = false;

   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

   public static final DeferredItem<Item> BANDAGE = ITEMS.register("bandage", () -> new ItemHealing(new Item.Properties().stacksTo(16)));
   public static final DeferredItem<Item> PLASTER = ITEMS.register("plaster", () -> new ItemHealing(new Item.Properties().stacksTo(16)));
   public static final DeferredItem<Item> MORPHINE_ITEM = ITEMS.register("morphine", () -> new ItemMorphine(new Item.Properties().stacksTo(16)));
   /** EventHandler.MORPHINE: FirstAidPotion(false, 3549, "morphine").setBeneficial() */
   public static final DeferredHolder<MobEffect, MobEffect> MORPHINE = EFFECTS.register("morphine", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 3549) {
   });
   public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = SOUNDS.register("debuff.heartbeat",
      () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "debuff.heartbeat")));
   /** 1.12 capability firstaid:capExtendedHealthSystem. Not copied on death (1.12 gave the respawned player a new model). */
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDamageModel>> DAMAGE_MODEL = ATTACHMENTS.register("damage_model",
      () -> AttachmentType.serializable(PlayerDamageModel::create).build());
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("firstaid", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup.firstaid"))
      .icon(() -> new ItemStack(BANDAGE.get()))
      .displayItems((p, out) -> {
         out.accept(BANDAGE.get());
         out.accept(PLASTER.get());
         out.accept(MORPHINE_ITEM.get());
      }).build());

   public FirstAid(IEventBus modBus) {
      ITEMS.register(modBus);
      EFFECTS.register(modBus);
      SOUNDS.register(modBus);
      ATTACHMENTS.register(modBus);
      TABS.register(modBus);
      modBus.addListener(FirstAidNetwork::register);
      NeoForge.EVENT_BUS.register(EventHandler.class);
      // FirstAidItems.registerItems: bandage and plaster heal one half heart per secondsPerHeal, totalHeals times
      FirstAidConfig.InternalHealing h = FirstAidConfig.internalHealing;
      modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> {
         FirstAidRegistryImpl.INSTANCE.registerHealingType(BANDAGE.get(), s -> new PartHealer(h.bandage.secondsPerHeal * 20, h.bandage.totalHeals, s),
            s -> h.bandage.applyTime);
         FirstAidRegistryImpl.INSTANCE.registerHealingType(PLASTER.get(), s -> new PartHealer(h.plaster.secondsPerHeal * 20, h.plaster.totalHeals, s),
            s -> h.plaster.applyTime);
      });
   }

   public static PlayerDamageModel getDamageModel(Player player) {
      return player.getData(DAMAGE_MODEL);
   }

   private static final Set<Player> UNTRACKED = Collections.newSetFromMap(new WeakHashMap<>());

   /** DataManagerWrapper.toggleTracking */
   public static void setTracking(Player player, boolean track) {
      if (track) {
         UNTRACKED.remove(player);
      } else {
         UNTRACKED.add(player);
      }
   }

   public static boolean isTracking(Player player) {
      return !UNTRACKED.contains(player);
   }

   /** DataManagerWrapper.set_impl(HEALTH): write the vanilla health without the setHealth watcher */
   public static void setHealthImpl(Player player, float health) {
      player.getEntityData().set(LivingEntityAccessor.firstaid$healthId(), health);
   }
}
