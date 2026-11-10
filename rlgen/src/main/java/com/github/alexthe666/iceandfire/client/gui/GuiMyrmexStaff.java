package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.client.MyrmexStaffClient;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.message.MessageGetMyrmexHive;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 1.12 GuiMyrmexStaff: breeding toggle, the hive's rooms 5 per page with delete buttons. Like 1.12 it rebuilds its buttons every
 * frame, button ids are kept (the first delete button shares id 2 with the next page button), and the edited client copy of the
 * hive goes to the server when the screen closes.
 */
public class GuiMyrmexStaff extends Screen {
   private static final ResourceLocation JUNGLE_TEXTURE = ResourceLocation.parse("iceandfire:textures/gui/myrmex_staff_jungle.png");
   private static final ResourceLocation DESERT_TEXTURE = ResourceLocation.parse("iceandfire:textures/gui/myrmex_staff_desert.png");
   static final ResourceLocation WIDGETS = ResourceLocation.parse("iceandfire:textures/gui/bestiary/widgets.png");
   private static final WorldGenMyrmexHive.RoomType[] ROOMS = new WorldGenMyrmexHive.RoomType[]{
      WorldGenMyrmexHive.RoomType.FOOD, WorldGenMyrmexHive.RoomType.NURSERY, WorldGenMyrmexHive.RoomType.EMPTY
   };
   private final ItemStack staff;
   private final boolean jungle;
   public ChangePageButton previousPage;
   public ChangePageButton nextPage;
   int ticksSinceDeleted = 0;
   int currentPage = 0;
   private final List<Room> allRoomPos = Lists.newArrayList();
   private final List<MyrmexDeleteButton> allRoomButtonPos = Lists.newArrayList();
   private static final int ROOMS_PER_PAGE = 5;
   private int hiveCount;

   public GuiMyrmexStaff(ItemStack staff) {
      super(Component.empty());
      this.staff = staff;
      this.jungle = staff.getItem() == ModItems.myrmex_jungle_staff;
   }

   @Override
   protected void init() {
      this.allRoomButtonPos.clear();
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      int x_translate = 193;
      int y_translate = 37;
      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();
      if (hive != null) {
         this.populateRoomMap();
         this.addRenderableWidget(
            Button.builder(
                  Component.literal(hive.reproduces ? I18n.get("myrmex.message.disablebreeding") : I18n.get("myrmex.message.enablebreeding")),
                  b -> this.actionPerformed(0, b)
               )
               .bounds(i + 124, j + 15, 120, 20)
               .build()
         );
         this.previousPage = this.addRenderableWidget(new ChangePageButton(1, i + 5, j + 150, false, this.jungle ? 2 : 1, this::actionPerformed));
         this.nextPage = this.addRenderableWidget(new ChangePageButton(2, i + 225, j + 150, true, this.jungle ? 2 : 1, this::actionPerformed));
         int totalRooms = this.allRoomPos.size();

         for (int rooms = 0; rooms < this.allRoomPos.size(); rooms++) {
            int yIndex = rooms % 5;
            MyrmexDeleteButton button = new MyrmexDeleteButton(
               2 + rooms, i + x_translate, j + y_translate + yIndex * 22, this.allRoomPos.get(rooms).pos, I18n.get("myrmex.message.delete"), this::actionPerformed
            );
            button.visible = rooms < 5 * (this.currentPage + 1) && rooms >= 5 * this.currentPage;
            this.addRenderableWidget(button);
            this.allRoomButtonPos.add(button);
         }

         if (totalRooms <= 5 * this.currentPage && this.currentPage > 0) {
            this.currentPage--;
         }
      }
   }

   private void populateRoomMap() {
      this.allRoomPos.clear();
      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();

      for (WorldGenMyrmexHive.RoomType type : ROOMS) {
         for (BlockPos pos : hive.getRooms(type)) {
            String name = type == WorldGenMyrmexHive.RoomType.FOOD ? "food" : (type == WorldGenMyrmexHive.RoomType.NURSERY ? "nursery" : "misc");
            this.allRoomPos.add(new Room(pos, name));
         }
      }

      for (BlockPos pos : hive.getEntrances().keySet()) {
         this.allRoomPos.add(new Room(pos, "enterance_surface"));
      }

      for (BlockPos pos : hive.getEntranceBottoms().keySet()) {
         this.allRoomPos.add(new Room(pos, "enterance_bottom"));
      }
   }

   /** 1.12 actionPerformed(GuiButton). */
   protected void actionPerformed(int id, Object button) {
      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();
      if (id == 0) {
         hive.reproduces = !hive.reproduces;
      } else if (id == 1 && this.currentPage > 0) {
         this.currentPage--;
      }

      if (id == 2 && this.currentPage < this.allRoomButtonPos.size() / 5) {
         this.currentPage++;
      } else if (button instanceof MyrmexDeleteButton && this.ticksSinceDeleted <= 0) {
         hive.removeRoom(((MyrmexDeleteButton)button).pos);
         this.ticksSinceDeleted = 5;
      }

      this.rebuildWidgets();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   /** 1.12 drawDefaultBackground: the dark gradient (no 1.21 blur) and the staff page. */
   @Override
   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.renderTransparentBackground(graphics);
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      graphics.blit(this.jungle ? JUNGLE_TEXTURE : DESERT_TEXTURE, i, j, 0, 0, 248, 166);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      if (this.ticksSinceDeleted > 0) {
         this.ticksSinceDeleted--;
      }

      this.rebuildWidgets();
      int i = (this.width - 248) / 2 + 10;
      int j = (this.height - 166) / 2 + 8;
      super.render(graphics, mouseX, mouseY, partialTicks);
      int color = this.jungle ? 3533333 : 16760576;
      this.hiveCount = 0;

      for (int rooms = 0; rooms < this.allRoomButtonPos.size(); rooms++) {
         if (rooms < 5 * (this.currentPage + 1) && rooms >= 5 * this.currentPage) {
            this.drawRoomInfo(graphics, this.allRoomPos.get(rooms).string, this.allRoomPos.get(rooms).pos, i, j, color);
         }
      }

      MyrmexHive hive = MyrmexStaffClient.getReferedClientHive();
      if (hive != null) {
         if (!hive.colonyName.isEmpty()) {
            String title = I18n.get("myrmex.message.colony_named", hive.colonyName);
            graphics.drawString(this.font, title, i + 40 - title.length() / 2, j - 3, color, true);
         } else {
            graphics.drawString(this.font, I18n.get("myrmex.message.colony"), i + 80, j - 3, color, true);
         }

         int opinion = hive.getPlayerReputation(Minecraft.getInstance().player.getUUID());
         graphics.drawString(this.font, I18n.get("myrmex.message.hive_opinion", opinion), i, j + 12, color, true);
         graphics.drawString(this.font, I18n.get("myrmex.message.rooms"), i, j + 25, color, true);
      }
   }

   /** 1.12 onGuiClosed. */
   @Override
   public void removed() {
      PacketDistributor.sendToServer(MessageGetMyrmexHive.of(MyrmexStaffClient.getReferedClientHive()));
   }

   private void drawRoomInfo(GuiGraphics graphics, String type, BlockPos pos, int i, int j, int color) {
      String translate = "myrmex.message.room." + type;
      graphics.drawString(this.font, I18n.get(translate, pos.getX(), pos.getY(), pos.getZ()), i, j + 36 + this.hiveCount * 22, color, true);
      this.hiveCount++;
   }

   private static class Room {
      public BlockPos pos;
      public String string;

      public Room(BlockPos pos, String string) {
         this.pos = pos;
         this.string = string;
      }
   }

   public interface Action {
      void perform(int id, Object button);
   }

   /** 1.12 MyrmexDeleteButton: a 50x20 vanilla button that remembers its room. */
   public static class MyrmexDeleteButton extends Button {
      public final BlockPos pos;

      public MyrmexDeleteButton(int id, int x, int y, BlockPos pos, String delete, Action action) {
         super(x, y, 50, 20, Component.literal(delete), b -> action.perform(id, b), DEFAULT_NARRATION);
         this.pos = pos;
      }
   }

   /** 1.12 bestiary ChangePageButton (23x10 arrow from bestiary/widgets.png, colour row 1 = desert, 2 = jungle). */
   public static class ChangePageButton extends AbstractButton {
      private final int id;
      private final boolean right;
      private final int color;
      private final Action action;

      public ChangePageButton(int id, int x, int y, boolean right, int color, Action action) {
         super(x, y, 23, 10, Component.empty());
         this.id = id;
         this.right = right;
         this.color = color;
         this.action = action;
      }

      @Override
      public void onPress() {
         this.action.perform(this.id, this);
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         boolean flag = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
         int i = 0;
         int j = 64;
         if (flag) {
            i += 23;
         }

         if (!this.right) {
            j += 13;
         }

         j += this.color * 23;
         graphics.blit(WIDGETS, this.getX(), this.getY(), i, j, this.width, this.height);
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
      }
   }
}
