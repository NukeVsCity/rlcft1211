package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import java.lang.reflect.Method;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** The dragon parts of 1.12 EventClient: the dragon third-person views (key F7) while riding a dragon. */
@EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT)
public final class DragonClientEvents {
   /** 1.12 ClientProxy.thirdPersonViewDragon (0..3). */
   private static int thirdPersonViewDragon = 0;
   private static Method cameraMove;

   private DragonClientEvents() {
   }

   public static int getDragon3rdPersonView() {
      return thirdPersonViewDragon;
   }

   public static void setDragon3rdPersonView(int view) {
      thirdPersonViewDragon = view;
   }

   /** 1.12 onLivingUpdate (client player): dragon_change_view cycles 0, 1, 2, 3. */
   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      Player player = Minecraft.getInstance().player;
      if (player != null && ModKeys.dragon_change_view != null && ModKeys.dragon_change_view.consumeClick()) {
         int currentView = getDragon3rdPersonView();
         if (currentView + 1 > 3) {
            currentView = 0;
         } else {
            currentView++;
         }

         setDragon3rdPersonView(currentView);
      }
   }

   /**
    * 1.12 onCameraSetup: a GL translation applied in eye space after the third-person zoom (not clipped by blocks). A GL
    * translate(x, 0, z) of the scene moves the camera by (-x, 0, -z) in eye space, i.e. forward z, right -x (back view).
    */
   @SubscribeEvent
   public static void onCameraDistance(CalculateDetachedCameraDistanceEvent event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player == null || !(player.getVehicle() instanceof EntityDragonBase dragon)) {
         return;
      }
      int currentView = getDragon3rdPersonView();
      if (currentView == 0) {
         return;
      }
      float scale = dragon.getRenderSize() / 3.0F;
      float tx = 0.0F;
      float tz = 0.0F;
      if (mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
         if (currentView == 1) {
            tx = scale * 0.5F;
            tz = -scale * 3.0F;
         } else if (currentView == 2) {
            tz = -scale * 3.0F;
         } else if (currentView == 3) {
            tx = scale * 0.5F;
            tz = -scale * 0.5F;
         }
      } else if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
         if (currentView == 1) {
            tx = -scale * 1.2F;
            tz = 5.0F;
         } else if (currentView == 2) {
            tx = scale * 1.2F;
            tz = 5.0F;
         } else if (currentView == 3) {
            tx = scale * -1.5F;
            tz = scale * 1.5F;
         }
      }
      if (event.isCameraFlipped()) {
         // 1.12 front view: the scene was rotated 180 degrees about Y before this translate, which flips x and z in eye space
         moveCamera(event.getCamera(), -tz, 0.0F, tx);
      } else {
         moveCamera(event.getCamera(), tz, 0.0F, -tx);
      }
   }

   /** Camera.move(forward, up, right) is protected. */
   private static void moveCamera(Camera camera, float forward, float up, float right) {
      try {
         if (cameraMove == null) {
            cameraMove = Camera.class.getDeclaredMethod("move", float.class, float.class, float.class);
            cameraMove.setAccessible(true);
         }
         cameraMove.invoke(camera, forward, up, right);
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Camera.move not found", e);
      }
   }
}
