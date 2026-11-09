package net.ilexiconn.llibrary.server.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** LLibrary 1.7.20 NetworkHandler: registers {@link AbstractMessage} classes as NeoForge payloads. */
public enum NetworkHandler {
   INSTANCE;

   private final List<SimpleNetworkWrapper> wrappers = new ArrayList<>();
   private final Map<Class<?>, CustomPacketPayload.Type<?>> types = new HashMap<>();

   void addWrapper(SimpleNetworkWrapper wrapper) {
      this.wrappers.add(wrapper);
   }

   public <T extends AbstractMessage<T>> void registerMessage(SimpleNetworkWrapper networkWrapper, Class<T> clazz) {
      if (this.types.containsKey(clazz)) {
         return;
      }
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath(networkWrapper.channel, clazz.getSimpleName().toLowerCase(Locale.ROOT));
      this.types.put(clazz, new CustomPacketPayload.Type<T>(id));
      networkWrapper.messages.add(clazz);
   }

   public CustomPacketPayload.Type<? extends CustomPacketPayload> getType(Class<?> clazz) {
      CustomPacketPayload.Type<?> type = this.types.get(clazz);
      if (type == null) {
         throw new IllegalStateException("Message " + clazz.getName() + " was never registered with a SimpleNetworkWrapper");
      }
      return type;
   }

   /** 1.12 injectNetworkWrapper: fills every static {@link NetworkWrapper} field of holder with a wrapper for channel. */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void injectNetworkWrapper(Class<?> holder, String channel) {
      for (Field field : holder.getDeclaredFields()) {
         NetworkWrapper annotation = field.getAnnotation(NetworkWrapper.class);
         if (annotation == null || !Modifier.isStatic(field.getModifiers())) {
            continue;
         }
         try {
            field.setAccessible(true);
            SimpleNetworkWrapper networkWrapper = new SimpleNetworkWrapper(channel);
            field.set(null, networkWrapper);
            for (Class messageClass : annotation.value()) {
               this.registerMessage(networkWrapper, messageClass);
            }
         } catch (Exception e) {
            LLibrary.LOGGER.fatal("Failed to inject network wrapper for {}", holder, e);
         }
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
      for (SimpleNetworkWrapper wrapper : this.wrappers) {
         PayloadRegistrar registrar = event.registrar("1");
         for (Class messageClass : wrapper.messages) {
            this.register(registrar, messageClass);
         }
      }
   }

   private <T extends AbstractMessage<T>> void register(PayloadRegistrar registrar, Class<T> clazz) {
      Constructor<T> constructor;
      try {
         constructor = clazz.getDeclaredConstructor();
         constructor.setAccessible(true);
      } catch (NoSuchMethodException e) {
         throw new IllegalStateException("Message " + clazz.getName() + " needs a no-arg constructor", e);
      }
      @SuppressWarnings("unchecked")
      CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>)this.types.get(clazz);
      StreamCodec<RegistryFriendlyByteBuf, T> codec = StreamCodec.of((buf, message) -> message.toBytes(buf), buf -> {
         try {
            T message = constructor.newInstance();
            message.fromBytes(buf);
            return message;
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
         }
      });
      registrar.playBidirectional(type, codec, (message, context) -> LLibrary.PROXY.handleMessage(message, new MessageContext(context)));
   }
}
