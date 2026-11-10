package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import net.minecraft.resources.ResourceLocation;

public enum EnumDragonTextures {
   VARIANT1("red_", "blue_"),
   VARIANT2("green_", "white_"),
   VARIANT3("bronze_", "sapphire_"),
   VARIANT4("gray_", "silver_");

   public final ResourceLocation FIRESTAGE1TEXTURE;
   public final ResourceLocation FIRESTAGE2TEXTURE;
   public final ResourceLocation FIRESTAGE3TEXTURE;
   public final ResourceLocation FIRESTAGE4TEXTURE;
   public final ResourceLocation FIRESTAGE5TEXTURE;
   public final ResourceLocation ICESTAGE1TEXTURE;
   public final ResourceLocation ICESTAGE2TEXTURE;
   public final ResourceLocation ICESTAGE3TEXTURE;
   public final ResourceLocation ICESTAGE4TEXTURE;
   public final ResourceLocation ICESTAGE5TEXTURE;
   public final ResourceLocation FIRESTAGE1SLEEPINGTEXTURE;
   public final ResourceLocation FIRESTAGE2SLEEPINGTEXTURE;
   public final ResourceLocation FIRESTAGE3SLEEPINGTEXTURE;
   public final ResourceLocation FIRESTAGE4SLEEPINGTEXTURE;
   public final ResourceLocation FIRESTAGE5SLEEPINGTEXTURE;
   public final ResourceLocation ICESTAGE1SLEEPINGTEXTURE;
   public final ResourceLocation ICESTAGE2SLEEPINGTEXTURE;
   public final ResourceLocation ICESTAGE3SLEEPINGTEXTURE;
   public final ResourceLocation ICESTAGE4SLEEPINGTEXTURE;
   public final ResourceLocation ICESTAGE5SLEEPINGTEXTURE;
   public final ResourceLocation FIRESTAGE1EYESTEXTURE;
   public final ResourceLocation FIRESTAGE2EYESTEXTURE;
   public final ResourceLocation FIRESTAGE3EYESTEXTURE;
   public final ResourceLocation FIRESTAGE4EYESTEXTURE;
   public final ResourceLocation FIRESTAGE5EYESTEXTURE;
   public final ResourceLocation ICESTAGE1EYESTEXTURE;
   public final ResourceLocation ICESTAGE2EYESTEXTURE;
   public final ResourceLocation ICESTAGE3EYESTEXTURE;
   public final ResourceLocation ICESTAGE4EYESTEXTURE;
   public final ResourceLocation ICESTAGE5EYESTEXTURE;
   public final ResourceLocation FIRESTAGE1SKELETONTEXTURE;
   public final ResourceLocation FIRESTAGE2SKELETONTEXTURE;
   public final ResourceLocation FIRESTAGE3SKELETONTEXTURE;
   public final ResourceLocation FIRESTAGE4SKELETONTEXTURE;
   public final ResourceLocation FIRESTAGE5SKELETONTEXTURE;
   public final ResourceLocation ICESTAGE1SKELETONTEXTURE;
   public final ResourceLocation ICESTAGE2SKELETONTEXTURE;
   public final ResourceLocation ICESTAGE3SKELETONTEXTURE;
   public final ResourceLocation ICESTAGE4SKELETONTEXTURE;
   public final ResourceLocation ICESTAGE5SKELETONTEXTURE;

   private EnumDragonTextures(String fireVariant, String iceVariant) {
      this.FIRESTAGE1TEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "1.png");
      this.FIRESTAGE2TEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "2.png");
      this.FIRESTAGE3TEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "3.png");
      this.FIRESTAGE4TEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "4.png");
      this.FIRESTAGE5TEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "5.png");
      this.FIRESTAGE1SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "1_sleeping.png");
      this.FIRESTAGE2SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "2_sleeping.png");
      this.FIRESTAGE3SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "3_sleeping.png");
      this.FIRESTAGE4SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "4_sleeping.png");
      this.FIRESTAGE5SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "5_sleeping.png");
      this.FIRESTAGE1EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "1_eyes.png");
      this.FIRESTAGE2EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "2_eyes.png");
      this.FIRESTAGE3EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "3_eyes.png");
      this.FIRESTAGE4EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "4_eyes.png");
      this.FIRESTAGE5EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + fireVariant + "5_eyes.png");
      this.FIRESTAGE1SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_1.png");
      this.FIRESTAGE2SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_2.png");
      this.FIRESTAGE3SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_3.png");
      this.FIRESTAGE4SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_4.png");
      this.FIRESTAGE5SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_5.png");
      this.ICESTAGE1TEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "1.png");
      this.ICESTAGE2TEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "2.png");
      this.ICESTAGE3TEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "3.png");
      this.ICESTAGE4TEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "4.png");
      this.ICESTAGE5TEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "5.png");
      this.ICESTAGE1SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "1_sleeping.png");
      this.ICESTAGE2SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "2_sleeping.png");
      this.ICESTAGE3SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "3_sleeping.png");
      this.ICESTAGE4SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "4_sleeping.png");
      this.ICESTAGE5SLEEPINGTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "5_sleeping.png");
      this.ICESTAGE1EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "1_eyes.png");
      this.ICESTAGE2EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "2_eyes.png");
      this.ICESTAGE3EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "3_eyes.png");
      this.ICESTAGE4EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "4_eyes.png");
      this.ICESTAGE5EYESTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + iceVariant + "5_eyes.png");
      this.ICESTAGE1SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_1.png");
      this.ICESTAGE2SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_2.png");
      this.ICESTAGE3SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_3.png");
      this.ICESTAGE4SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_4.png");
      this.ICESTAGE5SKELETONTEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_5.png");
   }

   public static ResourceLocation getTextureFromDragon(EntityDragonBase dragon) {
      return dragon instanceof EntityIceDragon ? getIceDragonTextures(dragon) : getFireDragonTextures(dragon);
   }

   public static ResourceLocation getEyeTextureFromDragon(EntityDragonBase dragon) {
      EnumDragonTextures textures = getDragonEnum(dragon);
      if (dragon instanceof EntityIceDragon) {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.ICESTAGE1EYESTEXTURE;
            case 2:
               return textures.ICESTAGE2EYESTEXTURE;
            case 3:
               return textures.ICESTAGE3EYESTEXTURE;
            case 4:
               return textures.ICESTAGE4EYESTEXTURE;
            case 5:
               return textures.ICESTAGE5EYESTEXTURE;
            default:
               return textures.ICESTAGE4EYESTEXTURE;
         }
      } else {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.FIRESTAGE1EYESTEXTURE;
            case 2:
               return textures.FIRESTAGE2EYESTEXTURE;
            case 3:
               return textures.FIRESTAGE3EYESTEXTURE;
            case 4:
               return textures.FIRESTAGE4EYESTEXTURE;
            case 5:
               return textures.FIRESTAGE5EYESTEXTURE;
            default:
               return textures.FIRESTAGE4EYESTEXTURE;
         }
      }
   }

   private static ResourceLocation getFireDragonTextures(EntityDragonBase dragon) {
      EnumDragonTextures textures = getDragonEnum(dragon);
      if (dragon.isModelDead()) {
         if (dragon.getDeathStage() >= dragon.getAgeInDays() / 5 / 2) {
            switch (dragon.getDragonStage()) {
               case 1:
                  return textures.FIRESTAGE1SKELETONTEXTURE;
               case 2:
                  return textures.FIRESTAGE2SKELETONTEXTURE;
               case 3:
                  return textures.FIRESTAGE3SKELETONTEXTURE;
               case 4:
                  return textures.FIRESTAGE4SKELETONTEXTURE;
               case 5:
                  return textures.FIRESTAGE5SKELETONTEXTURE;
               default:
                  return textures.FIRESTAGE4SKELETONTEXTURE;
            }
         } else {
            switch (dragon.getDragonStage()) {
               case 1:
                  return textures.FIRESTAGE1SLEEPINGTEXTURE;
               case 2:
                  return textures.FIRESTAGE2SLEEPINGTEXTURE;
               case 3:
                  return textures.FIRESTAGE3SLEEPINGTEXTURE;
               case 4:
                  return textures.FIRESTAGE4SLEEPINGTEXTURE;
               case 5:
                  return textures.FIRESTAGE5SLEEPINGTEXTURE;
               default:
                  return textures.FIRESTAGE4SLEEPINGTEXTURE;
            }
         }
      } else if (!dragon.isDragonSleeping() && !dragon.isBlinking()) {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.FIRESTAGE1TEXTURE;
            case 2:
               return textures.FIRESTAGE2TEXTURE;
            case 3:
               return textures.FIRESTAGE3TEXTURE;
            case 4:
               return textures.FIRESTAGE4TEXTURE;
            case 5:
               return textures.FIRESTAGE5TEXTURE;
            default:
               return textures.FIRESTAGE4TEXTURE;
         }
      } else {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.FIRESTAGE1SLEEPINGTEXTURE;
            case 2:
               return textures.FIRESTAGE2SLEEPINGTEXTURE;
            case 3:
               return textures.FIRESTAGE3SLEEPINGTEXTURE;
            case 4:
               return textures.FIRESTAGE4SLEEPINGTEXTURE;
            case 5:
               return textures.FIRESTAGE5SLEEPINGTEXTURE;
            default:
               return textures.FIRESTAGE4SLEEPINGTEXTURE;
         }
      }
   }

   private static ResourceLocation getIceDragonTextures(EntityDragonBase dragon) {
      EnumDragonTextures textures = getDragonEnum(dragon);
      if (dragon.isModelDead()) {
         if (dragon.getDeathStage() >= dragon.getAgeInDays() / 5 / 2) {
            switch (dragon.getDragonStage()) {
               case 1:
                  return textures.ICESTAGE1SKELETONTEXTURE;
               case 2:
                  return textures.ICESTAGE2SKELETONTEXTURE;
               case 3:
                  return textures.ICESTAGE3SKELETONTEXTURE;
               case 4:
                  return textures.ICESTAGE4SKELETONTEXTURE;
               case 5:
                  return textures.ICESTAGE5SKELETONTEXTURE;
               default:
                  return textures.ICESTAGE4SKELETONTEXTURE;
            }
         } else {
            switch (dragon.getDragonStage()) {
               case 1:
                  return textures.ICESTAGE1SLEEPINGTEXTURE;
               case 2:
                  return textures.ICESTAGE2SLEEPINGTEXTURE;
               case 3:
                  return textures.ICESTAGE3SLEEPINGTEXTURE;
               case 4:
                  return textures.ICESTAGE4SLEEPINGTEXTURE;
               case 5:
                  return textures.ICESTAGE5SLEEPINGTEXTURE;
               default:
                  return textures.ICESTAGE4SLEEPINGTEXTURE;
            }
         }
      } else if (!dragon.isDragonSleeping() && !dragon.isBlinking()) {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.ICESTAGE1TEXTURE;
            case 2:
               return textures.ICESTAGE2TEXTURE;
            case 3:
               return textures.ICESTAGE3TEXTURE;
            case 4:
               return textures.ICESTAGE4TEXTURE;
            case 5:
               return textures.ICESTAGE5TEXTURE;
            default:
               return textures.ICESTAGE4TEXTURE;
         }
      } else {
         switch (dragon.getDragonStage()) {
            case 1:
               return textures.ICESTAGE1SLEEPINGTEXTURE;
            case 2:
               return textures.ICESTAGE2SLEEPINGTEXTURE;
            case 3:
               return textures.ICESTAGE3SLEEPINGTEXTURE;
            case 4:
               return textures.ICESTAGE4SLEEPINGTEXTURE;
            case 5:
               return textures.ICESTAGE5SLEEPINGTEXTURE;
            default:
               return textures.ICESTAGE4SLEEPINGTEXTURE;
         }
      }
   }

   private static EnumDragonTextures getDragonEnum(EntityDragonBase dragon) {
      switch (dragon.getVariant()) {
         case 1:
            return VARIANT2;
         case 2:
            return VARIANT3;
         case 3:
            return VARIANT4;
         default:
            return VARIANT1;
      }
   }

   public static enum Armor {
      EMPTY(""),
      ARMORBODY1("armor_body_1"),
      ARMORBODY2("armor_body_2"),
      ARMORBODY3("armor_body_3"),
      ARMORHEAD1("armor_head_1"),
      ARMORHEAD2("armor_head_2"),
      ARMORHEAD3("armor_head_3"),
      ARMORNECK1("armor_neck_1"),
      ARMORNECK2("armor_neck_2"),
      ARMORNECK3("armor_neck_3"),
      ARMORTAIL1("armor_tail_1"),
      ARMORTAIL2("armor_tail_2"),
      ARMORTAIL3("armor_tail_3");

      public final ResourceLocation FIRETEXTURE;
      public final ResourceLocation ICETEXTURE;

      private Armor(String resource) {
         if (!resource.isEmpty()) {
            this.FIRETEXTURE = ResourceLocation.parse("iceandfire:textures/models/firedragon/" + resource + ".png");
            this.ICETEXTURE = ResourceLocation.parse("iceandfire:textures/models/icedragon/" + resource + ".png");
         } else {
            this.FIRETEXTURE = null;
            this.ICETEXTURE = null;
         }
      }

      public static EnumDragonTextures.Armor getArmorForDragon(EntityDragonBase dragon, int slot) {
         int armor = dragon.getArmorInSlot(slot);
         switch (slot) {
            case 1:
               switch (armor) {
                  case 1:
                     return ARMORNECK1;
                  case 2:
                     return ARMORNECK2;
                  case 3:
                     return ARMORNECK3;
                  default:
                     return EMPTY;
               }
            case 2:
               switch (armor) {
                  case 1:
                     return ARMORBODY1;
                  case 2:
                     return ARMORBODY2;
                  case 3:
                     return ARMORBODY3;
                  default:
                     return EMPTY;
               }
            case 3:
               switch (armor) {
                  case 1:
                     return ARMORTAIL1;
                  case 2:
                     return ARMORTAIL2;
                  case 3:
                     return ARMORTAIL3;
                  default:
                     return EMPTY;
               }
            default:
               switch (armor) {
                  case 1:
                     return ARMORHEAD1;
                  case 2:
                     return ARMORHEAD2;
                  case 3:
                     return ARMORHEAD3;
                  default:
                     return EMPTY;
               }
         }
      }
   }
}
