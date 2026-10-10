package com.nukevscity.rlgen.rc.maze;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.nukevscity.rlgen.rc.RcEnv;
import com.nukevscity.rlgen.rc.RcRegistry;
import com.nukevscity.rlgen.rc.RcStructureDef;
import com.nukevscity.rlgen.rc.RcTransform;
import com.nukevscity.rlgen.rc.maze.Maze.MazePassage;
import com.nukevscity.rlgen.rc.maze.Maze.MazePredicate;
import com.nukevscity.rlgen.rc.maze.Maze.MazeRoom;
import com.nukevscity.rlgen.rc.maze.Maze.MorphingMazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.PlacedMazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.SetMazeComponent;
import com.nukevscity.rlgen.rc.maze.MazeRc.Area;
import com.nukevscity.rlgen.rc.maze.MazeRc.Connector;
import com.nukevscity.rlgen.rc.maze.MazeRc.ConnectorFactory;
import com.nukevscity.rlgen.rc.maze.MazeRc.MazeComponentStructure;
import com.nukevscity.rlgen.rc.maze.MazeRc.MazeGen;
import com.nukevscity.rlgen.rc.maze.MazeRc.SavedMazeComponent;
import com.nukevscity.rlgen.rc.maze.MazeRc.SavedMazePathConnection;
import com.nukevscity.rlgen.rc.maze.MazeRc.SavedMazeReachability;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** WorldScriptMazeGenerator ("mazeGen" world script) and the planning half of WorldGenMaze. */
public final class MazeScript {
   /** RCConfig in RLCraft: mazePlacementReversesPerRoom=3.0, mazeTimeout=10000. */
   private static final float REVERSES_PER_ROOM = 3.0F;
   static final long TIMEOUT_MS = 10000L;

   final String mazeID;
   final BlockPos structureShift;
   final int[] roomSize;
   final SavedMazeComponent mazeComponent;
   final List<MazeRc.Rule> rules = new ArrayList<>();

   private MazeScript(String mazeID, BlockPos structureShift, int[] roomSize, SavedMazeComponent mazeComponent) {
      this.mazeID = mazeID;
      this.structureShift = structureShift;
      this.roomSize = roomSize;
      this.mazeComponent = mazeComponent;
   }

   /** WorldScriptMazeGenerator.readFromNBT (the converter keeps RC's own script compound). */
   public static MazeScript fromNbt(CompoundTag c) {
      SavedMazeComponent comp = new SavedMazeComponent();
      comp.reachability.groupByDefault = false;
      if (c.contains("mazeComponent", Tag.TAG_COMPOUND)) {
         CompoundTag m = c.getCompound("mazeComponent");
         if (m.contains("roomArea", Tag.TAG_COMPOUND)) {
            readAreas(comp, m.getCompound("roomArea"));
         } else {
            comp.rooms.add(new Area(true, new int[3], new int[3]));
         }
         for (Tag t : m.getList("exits", Tag.TAG_COMPOUND)) {
            comp.exitPaths.add(SavedMazePathConnection.fromNbt((CompoundTag)t));
         }
         comp.defaultConnector = m.contains("defaultConnector", Tag.TAG_STRING) ? m.getString("defaultConnector") : "Path";
         if (!m.contains("reachability")) {
            comp.reachability.groupByDefault = false;
         } else {
            comp.reachability = SavedMazeReachability.fromNbt(m.getCompound("reachability"));
         }
      } else {
         readAreas(comp, c.getCompound("rooms"));
         if (c.contains("roomNumbers", Tag.TAG_INT_ARRAY)) {
            int[] n = c.getIntArray("roomNumbers");
            comp.rooms.add(new Area(true, new int[]{0, 0, 0}, new int[]{n[0] - 1, n[1] - 1, n[2] - 1}));
         }
         for (Tag t : c.getList("blockedRoomAreas", Tag.TAG_COMPOUND)) {
            CompoundTag b = (CompoundTag)t;
            comp.rooms.add(new Area(false, b.getIntArray("min"), b.getIntArray("max")));
         }
         for (Tag t : c.getList("mazeExits", Tag.TAG_COMPOUND)) {
            comp.exitPaths.add(SavedMazePathConnection.fromNbt((CompoundTag)t));
         }
         comp.defaultConnector = "Wall";
         comp.reachability = new SavedMazeReachability();
         comp.reachability.groupByDefault = false;
      }
      int[] roomSize = c.getIntArray("roomSize");
      MazeScript s = new MazeScript(c.getString("mazeID"), new BlockPos(c.getInt("structureShift_x"), c.getInt("structureShift_y"), c.getInt("structureShift_z")),
         roomSize.length == 3 ? roomSize : new int[]{3, 5, 3}, comp);
      for (Tag t : c.getList("rules", Tag.TAG_COMPOUND)) {
         s.rules.add(MazeRc.Rule.fromNbt((CompoundTag)t));
      }
      return s;
   }

   private static void readAreas(SavedMazeComponent comp, CompoundTag selection) {
      comp.rooms.clear();
      for (Tag t : selection.getList("areas", Tag.TAG_COMPOUND)) {
         CompoundTag a = (CompoundTag)t;
         comp.rooms.add(new Area(a.getBoolean("additive"), a.getIntArray("min"), a.getIntArray("max")));
      }
   }

   public String mazeID() {
      return this.mazeID;
   }

   /** One room of the finished maze: which structure, turned how, and its lower corner (before the parent's transform). */
   public record Placed(RcStructureDef def, RcTransform transform, BlockPos lowerPos, Map<String, Boolean> variables) {
   }

   /** getPlacedRooms + the WorldGenMaze.place geometry; null when the maze has no rooms. */
   public List<Placed> place(Random random, RcTransform transform, RcEnv environment) {
      List<PlacedMazeComponent<MazeComponentStructure, Connector>> placed = this.getPlacedRooms(random, transform, environment);
      if (placed == null) {
         return null;
      }
      List<Placed> out = new ArrayList<>();
      for (PlacedMazeComponent<MazeComponentStructure, Connector> p : placed) {
         MazeComponentStructure info = p.component();
         RcStructureDef def = RcRegistry.get().structure(info.structureID);
         if (def == null) {
            continue;
         }
         BlockPos compLowerPos = boundingBox(this.roomSize, p, def, info.transform).offset(this.structureShift);
         out.add(new Placed(def, info.transform, compLowerPos, info.variableDomain));
      }
      return out;
   }

   /** WorldGenMaze.getBoundingBox */
   static BlockPos boundingBox(int[] roomSize, PlacedMazeComponent<MazeComponentStructure, Connector> placed, RcStructureDef def, RcTransform transform) {
      int[] shift = placed.shift().getCoordinates();
      int[] size = transform.applySize(def.size);
      int[] expected = placed.component().getSize();
      int[] dep = new int[3];
      for (int i = 0; i < 3; i++) {
         dep[i] = (expected[i] * roomSize[i] - size[i]) / 2;
      }
      return new BlockPos(shift[0] * roomSize[0] + dep[0], shift[1] * roomSize[1] + dep[1], shift[2] * roomSize[2] + dep[2]);
   }

   /** WorldGenMaze.lowerCoord */
   public static BlockPos lowerCoord(RcStructureDef def, BlockPos lowerCoord, RcTransform placedTransform, BlockPos pos, RcTransform transform) {
      int[] placedSize = placedTransform.applySize(def.size);
      return transform.apply(lowerCoord, new int[]{2 - placedSize[0], 2 - placedSize[1], 2 - placedSize[2]}).offset(pos);
   }

   /** Transforms.apply */
   public static RcTransform compose(RcTransform first, RcTransform second) {
      return RcTransform.from(first.rotation() * (second.mirrorX() ? -1 : 1) + second.rotation(), first.mirrorX() != second.mirrorX());
   }

   /** AxisAlignedTransform2D.apply(int direction) */
   static int applyDirection(RcTransform t, int direction) {
      boolean mirrorApplies = t.mirrorX() && t.rotation() % 2 == 1;
      return (direction + t.rotation() + (mirrorApplies ? 2 : 0)) % 4;
   }

   static void blockRooms(MorphingMazeComponent<Connector> component, Set<MazeRoom> rooms, Connector wall) {
      component.add(MazeRc.createCompleteComponent(rooms, Collections.emptyMap(), wall));
   }

   static void enclose(MorphingMazeComponent<Connector> component, MazeRoom lower, MazeRoom higher, Connector wall) {
      Set<MazeRoom> rooms = new HashSet<>();
      int[] lo = lower.getCoordinates();
      int[] hi = higher.getCoordinates();
      for (int i = 0; i < lo.length; i++) {
         // IntAreas.visitCoordsExcept over the other dimensions, setting dimension i to its lowest and highest value
         List<Integer> dims = new ArrayList<>();
         for (int d = 0; d < lo.length; d++) {
            if (d != i) {
               dims.add(d);
            }
         }
         int fi = i;
         visit(lo, hi, lo.clone(), dims, 0, ints -> {
            ints[fi] = lo[fi];
            rooms.add(new MazeRoom(ints));
            ints[fi] = hi[fi];
            rooms.add(new MazeRoom(ints));
         });
      }
      blockRooms(component, rooms, wall);
   }

   private static void visit(int[] lower, int[] higher, int[] coord, List<Integer> dims, int index, java.util.function.Consumer<int[]> visitor) {
      int dim = dims.get(index);
      for (coord[dim] = lower[dim]; coord[dim] <= higher[dim]; coord[dim]++) {
         if (index == dims.size() - 1) {
            visitor.accept(coord);
         } else {
            visit(lower, higher, coord, dims, index + 1, visitor);
         }
      }
   }

   /** GenericVariableDomain.omega(environment, logical = true) */
   static Stream<Map<String, Boolean>> omega(RcStructureDef def, RcEnv environment) {
      Stream<Map<String, Boolean>> stream = Stream.of(new HashMap<>(environment.variables));
      for (RcStructureDef.Variable variable : def.variables) {
         if (variable.affectsLogic() && !environment.variables.containsKey(variable.id())) {
            stream = stream.flatMap(d -> {
               RcEnv probe = environment.child();
               probe.variables.putAll(d);
               boolean top = variable.chance() > 0.0F && variable.condition().test(probe);
               boolean bottom = variable.chance() < 1.0F;
               if (top && bottom) {
                  return IntStream.of(0, 1).mapToObj(i -> {
                     Map<String, Boolean> copy = new HashMap<>(d);
                     copy.put(variable.id(), i != 0);
                     return copy;
                  });
               }
               d.put(variable.id(), top);
               return Stream.of(d);
            });
         }
      }
      return stream;
   }

   /** GenericVariableDomain.chance */
   static float chance(RcStructureDef def, RcEnv environment) {
      float chance = 1.0F;
      for (RcStructureDef.Variable v : def.variables) {
         boolean value = environment.variables.getOrDefault(v.id(), false);
         chance *= v.condition().test(environment) ? (value ? v.chance() : 1.0F - v.chance()) : (value ? 0 : 1);
      }
      return chance;
   }

   /** Environment.copy(domain) */
   static RcEnv copy(RcEnv environment, Map<String, Boolean> domain) {
      RcEnv copy = environment.child();
      domain.forEach(copy.variables::putIfAbsent);
      return copy;
   }

   static Stream<MazeComponentStructure> transforms(RcStructureDef info, MazeGen mazeInfo, RcTransform transform, ConnectorFactory factory, RcEnv environment,
                                                     Collection<Connector> blocked) {
      int[] compSize = mazeInfo.component().boundsSize();
      IntPredicate rotate = r -> info.rotatable || applyDirection(transform, r) == 0;
      IntPredicate mirror = m -> info.mirrorable || m == 0;
      List<RcTransform> transforms = IntStream.of(0, 1, 2, 3).filter(rotate).boxed()
         .flatMap(r -> IntStream.of(0, 1).filter(mirror).mapToObj(m -> RcTransform.from(r, m != 0))).collect(Collectors.toList());
      double compWeight = mazeInfo.getWeight() * (double)chance(info, environment);
      double split = compWeight / (double)transforms.size();
      return transforms.stream().map(t -> transform(info, mazeInfo.component(), t, compSize, split, factory, environment, blocked));
   }

   static MazeComponentStructure transform(RcStructureDef info, SavedMazeComponent comp, RcTransform transform, int[] size, double weight,
                                           ConnectorFactory factory, RcEnv environment, Collection<Connector> blocked) {
      Collection<MazeRoom> rooms = comp.getRooms();
      Set<MazeRoom> transformedRooms = rooms.stream().map(r -> Maze.rotated(r, transform, size)).collect(Collectors.toSet());
      Map<MazePassage, Connector> transformedExits = new HashMap<>();
      MazeRc.buildExitPaths(environment, factory, comp.exitPaths, rooms)
         .forEach(path -> transformedExits.put(Maze.rotated(path.getKey(), transform, size), path.getValue()));
      MazeRc.addMissingExits(transformedRooms, transformedExits, factory.get(comp.defaultConnector));
      ImmutableMultimap.Builder<MazePassage, MazePassage> reachability = ImmutableMultimap.builder();
      comp.reachability.build(reachability, transform, size, SavedMazeReachability.notBlocked(blocked, transformedExits), transformedExits.keySet());
      MazeRc.addExternalReachability(reachability, transformedExits, blocked);
      return new MazeComponentStructure(weight, info.id, environment.variables, transform, ImmutableSet.copyOf(transformedRooms),
         ImmutableMap.copyOf(transformedExits), reachability.build());
   }

   List<PlacedMazeComponent<MazeComponentStructure, Connector>> getPlacedRooms(Random random, RcTransform transform, RcEnv environment) {
      if (this.mazeComponent.rooms.isEmpty()) {
         return null;
      }
      ConnectorFactory factory = new ConnectorFactory();
      Connector wall = factory.get("Wall");
      Connector defaultConnector = factory.get(this.mazeComponent.defaultConnector);
      Set<Connector> blocked = Collections.singleton(wall);
      int[] higher = this.mazeComponent.rooms.boundsHigher();
      int[] lower = this.mazeComponent.rooms.boundsLower();
      int[] outsideHigher = new int[higher.length];
      int[] outsideLower = new int[lower.length];
      for (int i = 0; i < higher.length; i++) {
         outsideHigher[i] = higher[i] + 1;
         outsideLower[i] = lower[i] - 1;
      }
      List<MazeComponentStructure> components = new ArrayList<>();
      for (RcStructureDef def : RcRegistry.get().structures()) {
         for (MazeGen gen : def.mazeGens) {
            if (this.mazeID.equals(gen.mazeID()) && gen.component().isValid()) {
               omega(def, environment).flatMap(domain -> transforms(def, gen, transform, factory, copy(environment, domain), blocked))
                  .forEach(components::add);
            }
         }
      }
      MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();
      enclose(maze, new MazeRoom(outsideLower), new MazeRoom(outsideHigher), defaultConnector);
      blockRooms(maze, this.mazeComponent.rooms.compile(false).keySet(), defaultConnector);
      MazeRc.buildExitPaths(environment, factory, this.mazeComponent.exitPaths, maze.rooms()).forEach(path -> maze.exits().put(path.getKey(), path.getValue()));
      maze.reachability().putAll(MazeRc.addExternalReachability(ImmutableMultimap.builder(), maze.exits(), blocked).build());
      maze.reachability().putAll(this.mazeComponent.reachability.build(ImmutableMultimap.builder(), RcTransform.ORIGINAL, this.mazeComponent.boundsSize(),
         SavedMazeReachability.notBlocked(blocked, maze.exits()), maze.exits().keySet()).build());
      List<MazePredicate<Connector>> predicates = this.rules.stream()
         .map(r -> r.build(this.mazeComponent, blocked, factory, components, MazeRc.CONNECTOR_STRATEGY)).filter(Objects::nonNull)
         .collect(Collectors.toCollection(ArrayList::new));
      predicates.add(new MazeRc.LimitAABBStrategy<>(outsideHigher));
      predicates.add(new MazeRc.BlockedConnectorStrategy<>(blocked));
      int totalRooms = this.mazeComponent.rooms.compile(true).size();
      int reverses = (int)Math.floor(totalRooms * REVERSES_PER_ROOM + 0.5F);
      return Maze.connect(maze, components, MazeRc.CONNECTOR_STRATEGY, new Maze.MazePredicateMany<>(predicates), random, reverses,
         System.nanoTime() + TIMEOUT_MS * 1_000_000L);
   }
}
