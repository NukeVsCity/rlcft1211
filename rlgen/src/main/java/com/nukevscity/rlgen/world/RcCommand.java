package com.nukevscity.rlgen.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nukevscity.rlgen.rc.RcEnv;
import com.nukevscity.rlgen.rc.RcGenerator;
import com.nukevscity.rlgen.rc.RcRegistry;
import com.nukevscity.rlgen.rc.RcStructureDef;
import java.util.Random;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** /rlgen rc <structure>: places a Recurrent Complex structure at your position the way RC's natural generation would. */
public final class RcCommand {
   private RcCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("rlgen").requires(s -> s.hasPermission(2))
         .then(Commands.literal("rc").then(Commands.argument("structure", StringArgumentType.word())
            .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
               java.util.stream.StreamSupport.stream(RcRegistry.get().structures().spliterator(), false).map(d -> d.id), b))
            .executes(ctx -> place(ctx.getSource(), StringArgumentType.getString(ctx, "structure"))))));
   }

   private static int place(CommandSourceStack source, String id) {
      ServerLevel level = source.getLevel();
      if (!(level.getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator)) {
         source.sendFailure(Component.literal("Not an RLCraft Legacy world"));
         return 0;
      }
      RcStructureDef def = RcRegistry.get().structure(id);
      if (def == null) {
         source.sendFailure(Component.literal("Unknown structure " + id));
         return 0;
      }
      BlockPos at = BlockPos.containing(source.getPosition());
      long seed = new Random().nextLong();
      RcGenerator.Oracle oracle = generator.oracle();
      RcGenerator.Instance inst = RcGenerator.planAt(def, at, seed, oracle);
      if (inst == null) {
         source.sendFailure(Component.literal(def.id + " found no place here (its placer or ensure rules refused)"));
         return 0;
      }
      BoundingBox extent = inst.extent(oracle);
      for (int cx = extent.minX() >> 4; cx <= extent.maxX() >> 4; cx++) {
         for (int cz = extent.minZ() >> 4; cz <= extent.maxZ() >> 4; cz++) {
            ChunkPos cp = new ChunkPos(cx, cz);
            BoundingBox box = new BoundingBox(cp.getMinBlockX(), level.getMinBuildHeight(), cp.getMinBlockZ(), cp.getMaxBlockX(), level.getMaxBuildHeight() - 1,
               cp.getMaxBlockZ());
            RcPiece.LiveView view = new RcPiece.LiveView(level, cp, oracle);
            long s = cp.equals(new ChunkPos(at)) ? seed : RcGenerator.chunkSeed(seed, cp);
            RcGenerator.spawn(inst, view, box, s, new RcEnv(inst.env().biome, "natural", inst.genId, null));
            view.finish();
         }
      }
      // placing over grown terrain breaks trees and grass (natural generation runs before them); drop the leftovers
      net.minecraft.world.phys.AABB box = net.minecraft.world.phys.AABB.of(extent).inflate(8.0);
      level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box).forEach(e -> e.discard());
      level.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class, box).forEach(e -> e.discard());
      source.sendSuccess(() -> Component.literal("Placed " + def.id + " at " + inst.lower.toShortString()), true);
      return 1;
   }
}
