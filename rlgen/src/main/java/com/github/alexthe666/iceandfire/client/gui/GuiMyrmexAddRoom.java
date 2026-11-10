package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.client.MyrmexStaffClient;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.message.MessageGetMyrmexHive;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** 1.12 GuiMyrmexAddRoom: marks the clicked block as a food/nursery/misc room or a surface/bottom entrance of the staff's hive. */
public class GuiMyrmexAddRoom extends Screen {
   private static final ResourceLocation JUNGLE_TEXTURE = ResourceLocation.parse("iceandfire:textures/gui/myrmex_staff_jungle.png");
   private static final ResourceLocation DESERT_TEXTURE = ResourceLocation.parse("iceandfire:textures/gui/myrmex_staff_desert.png");
   private final ItemStack staff;
   private final boolean jungle;
   private final BlockPos interactPos;
   private final Direction facing;

   public GuiMyrmexAddRoom(ItemStack staff, BlockPos interactPos, Direction facing) {
      super(Component.empty());
      this.staff = staff;
      this.jungle = staff.getItem() == ModItems.myrmex_jungle_staff;
      this.interactPos = interactPos;
      this.facing = facing;
   }

   @Override
   protected void init() {
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      if (MyrmexStaffClient.getReferedClientHive() != null) {
         this.button(0, i + 50, j + 35, "myrmex.message.establishroom_food");
         this.button(1, i + 50, j + 60, "myrmex.message.establishroom_nursery");
         this.button(2, i + 50, j + 85, "myrmex.message.establishroom_enterance_surface");
         this.button(3, i + 50, j + 110, "myrmex.message.establishroom_enterance_bottom");
         this.button(4, i + 50, j + 135, "myrmex.message.establishroom_misc");
      }
   }

   private void button(int id, int x, int y, String key) {
      this.addRenderableWidget(Button.builder(Component.literal(I18n.get(key)), b -> this.actionPerformed(id)).bounds(x, y, 150, 20).build());
   }

   protected void actionPerformed(int id) {
      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();
      if (hive != null) {
         Player player = Minecraft.getInstance().player;
         switch (id) {
            case 0:
               hive.addRoomWithMessage(player, this.interactPos, WorldGenMyrmexHive.RoomType.FOOD);
               break;
            case 1:
               hive.addRoomWithMessage(player, this.interactPos, WorldGenMyrmexHive.RoomType.NURSERY);
               break;
            case 2:
               hive.addEnteranceWithMessage(player, false, this.interactPos, this.facing);
               break;
            case 3:
               hive.addEnteranceWithMessage(player, true, this.interactPos, this.facing);
               break;
            case 4:
               hive.addRoomWithMessage(player, this.interactPos, WorldGenMyrmexHive.RoomType.EMPTY);
         }

         // 1.12: onGuiClosed() and then displayGuiScreen(null), which calls onGuiClosed() again
         this.removed();
         Minecraft.getInstance().setScreen(null);
      }
   }

   @Override
   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.renderTransparentBackground(graphics);
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      graphics.blit(this.jungle ? JUNGLE_TEXTURE : DESERT_TEXTURE, i, j, 0, 0, 248, 166);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.rebuildWidgets();
      int i = (this.width - 248) / 2 + 10;
      int j = (this.height - 166) / 2 + 8;
      super.render(graphics, mouseX, mouseY, partialTicks);
      int color = this.jungle ? 3533333 : 16760576;
      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();
      if (hive != null) {
         if (!hive.colonyName.isEmpty()) {
            String title = I18n.get("myrmex.message.colony_named", hive.colonyName);
            graphics.drawString(this.font, title, i + 40 - title.length() / 2, j - 3, color, true);
         } else {
            graphics.drawString(this.font, I18n.get("myrmex.message.colony"), i + 80, j - 3, color, true);
         }

         graphics.drawString(
            this.font,
            I18n.get("myrmex.message.create_new_room", this.interactPos.getX(), this.interactPos.getY(), this.interactPos.getZ()),
            i + 30,
            j + 6,
            color,
            true
         );
      }
   }

   @Override
   public void removed() {
      PacketDistributor.sendToServer(MessageGetMyrmexHive.of(MyrmexStaffClient.getReferedClientHive()));
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
