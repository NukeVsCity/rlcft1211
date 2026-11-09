package net.ilexiconn.llibrary;

import java.util.function.Supplier;
import net.ilexiconn.llibrary.server.ServerEventHandler;
import net.ilexiconn.llibrary.server.ServerProxy;
import net.ilexiconn.llibrary.server.capability.EntityDataCapabilityImplementation;
import net.ilexiconn.llibrary.server.network.AnimationMessage;
import net.ilexiconn.llibrary.server.network.NetworkHandler;
import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.ilexiconn.llibrary.server.network.PropertiesMessage;
import net.ilexiconn.llibrary.server.network.SimpleNetworkWrapper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** LLibrary 1.7.20 (the parts Ice and Fire 1.7.1 uses) ported to 1.21.1, keeping the 1.12 package and class names. */
@Mod(LLibrary.MODID)
public final class LLibrary {
   public static final String MODID = "llibrary";
   public static final String VERSION = "1.7.20";
   public static final Logger LOGGER = LogManager.getLogger("LLibrary");
   public static ServerProxy PROXY = createProxy();
   public static LLibrary INSTANCE;
   @NetworkWrapper({AnimationMessage.class, PropertiesMessage.class})
   public static SimpleNetworkWrapper NETWORK_WRAPPER;

   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
   /** 1.12 ENTITY_DATA_CAPABILITY: the per-entity EntityProperties store. */
   public static final Supplier<AttachmentType<EntityDataCapabilityImplementation>> ENTITY_DATA_CAPABILITY = ATTACHMENT_TYPES.register("entity_data",
      () -> AttachmentType.builder(ServerEventHandler.INSTANCE::createEntityData).serialize(ServerEventHandler.SERIALIZER).copyOnDeath().build());

   public LLibrary(IEventBus modBus) {
      INSTANCE = this;
      ATTACHMENT_TYPES.register(modBus);
      NetworkHandler.INSTANCE.injectNetworkWrapper(LLibrary.class, MODID);
      modBus.addListener(NetworkHandler.INSTANCE::onRegisterPayloads);
      PROXY.onPreInit();
   }

   private static ServerProxy createProxy() {
      if (FMLEnvironment.dist.isClient()) {
         try {
            return (ServerProxy)Class.forName("net.ilexiconn.llibrary.client.ClientProxy").getDeclaredConstructor().newInstance();
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
         }
      }
      return new ServerProxy();
   }
}
