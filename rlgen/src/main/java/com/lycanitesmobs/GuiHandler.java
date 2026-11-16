package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.tileentity.TileEntityBase;


public class GuiHandler implements IGuiHandler {
   public static GuiHandler instance;

   public GuiHandler() {
      instance = this;
   }

   public Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z) {
      if (id == GuiHandler.GuiType.TILEENTITY.id) {
         BlockEntity tileEntity = world.getBlockEntity(LycPos.of(x, y, z));
         if (tileEntity instanceof TileEntityBase) {
            return ((TileEntityBase)tileEntity).getGUI(player);
         }
      } else if (id == GuiHandler.GuiType.ENTITY.id) {
         Entity entity = world.getEntity(x);
         if (entity instanceof BaseCreatureEntity) {
            return new CreatureContainer((BaseCreatureEntity)entity, player.getInventory());
         }
      } else if (id == GuiHandler.GuiType.ITEM.id) {
      }

      return null;
   }

   public Object getClientGuiElement(int id, Player player, Level world, int x, int y, int z) {
      if (id == GuiHandler.GuiType.TILEENTITY.id) {
         BlockEntity tileEntity = world.getBlockEntity(LycPos.of(x, y, z));
         if (tileEntity instanceof TileEntityBase) {
            return ((TileEntityBase)tileEntity).getGUI(player);
         }
      } else if (id == GuiHandler.GuiType.ENTITY.id) {
         Entity entity = world.getEntity(x);
         if (entity instanceof BaseCreatureEntity) {
            return new CreatureInventoryScreen((BaseCreatureEntity)entity, player.getInventory());
         }
      } else if (id != GuiHandler.GuiType.ITEM.id) {
         if (id == GuiHandler.GuiType.BEASTIARY.id) {
            if (x == GuiHandler.Beastiary.INDEX.id) {
               return new IndexBeastiaryScreen(player);
            }

            if (x == GuiHandler.Beastiary.CREATURES.id) {
               return new CreaturesBeastiaryScreen(player);
            }

            if (x == GuiHandler.Beastiary.PETS.id) {
               return new PetsBeastiaryScreen(player);
            }

            if (x == GuiHandler.Beastiary.SUMMONING.id) {
               return new SummoningBeastiaryScreen(player);
            }

            if (x == GuiHandler.Beastiary.ELEMENTS.id) {
               return new ElementsBeastiaryScreen(player);
            }
         } else if (id == GuiHandler.GuiType.PLAYER.id && x == GuiHandler.PlayerGuiType.MINION_SELECTION.id) {
            return new MinionSelectionOverlay(player);
         }
      }

      return null;
   }

   public static enum Beastiary {
      INDEX((byte)0),
      CREATURES((byte)1),
      PETS((byte)2),
      SUMMONING((byte)3),
      ELEMENTS((byte)4);

      public byte id;

      private Beastiary(byte i) {
         this.id = i;
      }
   }

   public static enum GuiType {
      TILEENTITY((byte)0),
      ENTITY((byte)1),
      ITEM((byte)2),
      BEASTIARY((byte)3),
      PLAYER((byte)4);

      public byte id;

      private GuiType(byte i) {
         this.id = i;
      }
   }

   public static enum PlayerGuiType {
      MINION_SELECTION((byte)0);

      public byte id;

      private PlayerGuiType(byte i) {
         this.id = i;
      }
   }
}
