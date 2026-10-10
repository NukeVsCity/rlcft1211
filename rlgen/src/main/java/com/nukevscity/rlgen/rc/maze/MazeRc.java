package com.nukevscity.rlgen.rc.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukevscity.rlgen.rc.RcEnv;
import com.nukevscity.rlgen.rc.RcExpr;
import com.nukevscity.rlgen.rc.RcTransform;
import com.nukevscity.rlgen.rc.maze.Maze.ConnectionStrategy;
import com.nukevscity.rlgen.rc.maze.Maze.MazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.MazePassage;
import com.nukevscity.rlgen.rc.maze.Maze.MazePredicate;
import com.nukevscity.rlgen.rc.maze.Maze.MazeRoom;
import com.nukevscity.rlgen.rc.maze.Maze.MorphingMazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.SetMazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.ShiftedMazeComponent;
import com.nukevscity.rlgen.rc.maze.Maze.WeightedMazeComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.tuple.Pair;

/** Recurrent Complex 1.4.8.4 maze generation (world/gen/feature/structure/generic/maze and its rules). */
public final class MazeRc {
   private MazeRc() {
   }

   // ------------------------------------------------------------------ connectors

   public abstract static class Connector {
      public final String id;

      Connector(String id) {
         this.id = id;
      }

      public abstract boolean accepts(@Nullable Connector c);

      @Override
      public boolean equals(Object o) {
         return this == o || o != null && this.getClass() == o.getClass() && this.id.equals(((Connector)o).id);
      }

      @Override
      public int hashCode() {
         return this.id.hashCode();
      }

      @Override
      public String toString() {
         return this.id;
      }
   }

   static final class Hermaphrodite extends Connector {
      Hermaphrodite(String id) {
         super(id);
      }

      @Override
      public boolean accepts(Connector c) {
         return c == null || c == this;
      }
   }

   static final class Gendered extends Connector {
      Gendered partner;

      Gendered(String id) {
         super(id);
      }

      @Override
      public boolean accepts(Connector c) {
         return c == null || c == this.partner;
      }
   }

   public static final class ConnectorFactory {
      private final Map<String, Connector> connectors = new HashMap<>();

      public Connector get(String id) {
         Connector connector = this.connectors.get(id);
         return connector != null ? connector : this.register(id);
      }

      private Connector register(String id) {
         if (id.startsWith("Male:") || id.startsWith("Female:")) {
            String base = id.substring(id.indexOf(':') + 1);
            Gendered male = new Gendered("Male:" + base);
            Gendered female = new Gendered("Female:" + base);
            male.partner = female;
            female.partner = male;
            this.connectors.put(male.id, male);
            this.connectors.put(female.id, female);
            return id.startsWith("Male:") ? male : female;
         }
         Connector c = new Hermaphrodite(id);
         this.connectors.put(id, c);
         return c;
      }
   }

   /** ConnectorStrategy */
   static final ConnectionStrategy<Connector> CONNECTOR_STRATEGY = (passage, existing, add) -> add.accepts(existing) ? 1.0F : -1.0F;

   // ------------------------------------------------------------------ Selection

   record Area(boolean additive, int[] min, int[] max) {
   }

   static final class Selection extends ArrayList<Area> {
      private static void mergeRooms(boolean additive, int dim, int[] min, int[] max, int[] position, Map<MazeRoom, String> rooms) {
         for (int i = min[dim]; i <= max[dim]; i++) {
            position[dim] = i;
            if (dim == position.length - 1) {
               if (additive) {
                  rooms.put(new MazeRoom(position), null);
               } else {
                  rooms.remove(new MazeRoom(position));
               }
            } else {
               mergeRooms(additive, dim + 1, min, max, position, rooms);
            }
         }
      }

      Map<MazeRoom, String> compile(boolean additive) {
         Map<MazeRoom, String> rooms = new HashMap<>();
         if (!additive) {
            int[] min = this.boundsLower();
            int[] max = this.boundsHigher();
            mergeRooms(true, 0, min, max, min.clone(), rooms);
            for (Area area : this) {
               mergeRooms(!area.additive, 0, area.min, area.max, area.min.clone(), rooms);
            }
         } else {
            for (Area area : this) {
               mergeRooms(area.additive, 0, area.min, area.max, area.min.clone(), rooms);
            }
         }
         return rooms;
      }

      int[] boundsLower() {
         int[] min = null;
         for (Area area : this) {
            if (min == null) {
               min = area.min.clone();
            } else {
               for (int d = 0; d < area.min.length; d++) {
                  min[d] = Math.min(min[d], area.min[d]);
               }
            }
         }
         return min;
      }

      int[] boundsHigher() {
         int[] max = null;
         for (Area area : this) {
            if (max == null) {
               max = area.max.clone();
            } else {
               for (int d = 0; d < area.max.length; d++) {
                  max[d] = Math.max(max[d], area.max[d]);
               }
            }
         }
         return max;
      }

      int[] boundsSize() {
         int[] min = this.boundsLower();
         int[] max = this.boundsHigher();
         for (int i = 0; i < max.length; i++) {
            max[i] += 1 - min[i];
         }
         return max;
      }
   }

   // ------------------------------------------------------------------ saved paths, connections, reachability

   static final class SavedMazePath {
      final int pathDimension;
      final MazeRoom sourceRoom;
      final boolean pathGoesUp;

      SavedMazePath(int pathDimension, MazeRoom sourceRoom, boolean pathGoesUp) {
         this.pathDimension = pathDimension;
         this.sourceRoom = sourceRoom;
         this.pathGoesUp = pathGoesUp;
      }

      MazePassage build() {
         return new MazePassage(this.sourceRoom, this.sourceRoom.addInDimension(this.pathDimension, this.pathGoesUp ? 1 : -1));
      }

      static SavedMazePath fromJson(JsonObject o) {
         JsonElement src = o.get("source");
         int[] coords = src.isJsonObject() ? ints(src.getAsJsonObject().getAsJsonArray("coordinates")) : ints(src.getAsJsonArray());
         return new SavedMazePath(o.get("pathDimension").getAsInt(), new MazeRoom(coords), o.get("pathGoesUp").getAsBoolean());
      }

      static SavedMazePath fromNbt(CompoundTag c) {
         int[] coords = c.contains("source", Tag.TAG_COMPOUND) ? c.getCompound("source").getIntArray("coordinates") : c.getIntArray("source");
         return new SavedMazePath(c.getInt("pathDimension"), new MazeRoom(coords), c.getBoolean("pathGoesUp"));
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || this.getClass() != o.getClass()) {
            return false;
         }
         SavedMazePath that = (SavedMazePath)o;
         return this.pathDimension == that.pathDimension && this.pathGoesUp == that.pathGoesUp && this.sourceRoom.equals(that.sourceRoom);
      }

      @Override
      public int hashCode() {
         int result = this.pathDimension;
         result = 31 * result + this.sourceRoom.hashCode();
         return 31 * result + (this.pathGoesUp ? 1 : 0);
      }
   }

   record ConditionalConnector(RcExpr<RcEnv> expression, String connector) {
   }

   static final class SavedMazePathConnection {
      final SavedMazePath path;
      final String connector;
      final List<ConditionalConnector> conditionalConnectors;

      SavedMazePathConnection(SavedMazePath path, String connector, List<ConditionalConnector> conditionalConnectors) {
         this.path = path;
         this.connector = connector;
         this.conditionalConnectors = conditionalConnectors;
      }

      Entry<MazePassage, Connector> build(RcEnv environment, ConnectorFactory factory) {
         String c = this.connector;
         for (ConditionalConnector cc : this.conditionalConnectors) {
            if (cc.expression.test(environment)) {
               c = cc.connector;
               break;
            }
         }
         return Pair.of(this.path.build(), factory.get(c));
      }

      static SavedMazePathConnection fromJson(JsonObject o) {
         List<ConditionalConnector> cond = new ArrayList<>();
         if (o.has("conditionalConnectors")) {
            for (JsonElement e : o.getAsJsonArray("conditionalConnectors")) {
               JsonObject c = e.getAsJsonObject();
               cond.add(new ConditionalConnector(RcEnv.envExpr(str(c, "expression", "")), str(c, "connector", "Path")));
            }
         }
         return new SavedMazePathConnection(SavedMazePath.fromJson(o), str(o, "connector", "Path"), cond);
      }

      static SavedMazePathConnection fromNbt(CompoundTag c) {
         return new SavedMazePathConnection(SavedMazePath.fromNbt(c), c.contains("connector", Tag.TAG_STRING) ? c.getString("connector") : "Path",
            new ArrayList<>());
      }
   }

   static final class SavedMazeReachability {
      final List<Set<SavedMazePath>> groups = new ArrayList<>();
      final List<Pair<SavedMazePath, SavedMazePath>> crossConnections = new ArrayList<>();
      boolean groupByDefault = true;

      static Predicate<MazePassage> notBlocked(Collection<Connector> blocked, Map<MazePassage, Connector> connections) {
         return input -> !blocked.contains(connections.get(input));
      }

      ImmutableMultimap.Builder<MazePassage, MazePassage> build(ImmutableMultimap.Builder<MazePassage, MazePassage> builder, RcTransform transform,
                                                               int[] size, Predicate<MazePassage> filter, Set<MazePassage> connections) {
         Predicate<MazePassage> f = ((Predicate<MazePassage>)connections::contains).and(filter);
         Set<MazePassage> defaultGroup = Sets.newHashSet(connections);
         for (Set<SavedMazePath> group : this.groups) {
            List<MazePassage> passages = group.stream().map(p -> Maze.rotated(p.build(), transform, size)).filter(f).collect(Collectors.toList());
            defaultGroup.removeAll(passages);
            addInterconnections(builder, passages.stream());
         }
         if (this.groupByDefault) {
            addInterconnections(builder, defaultGroup.stream().filter(f));
         }
         for (Pair<SavedMazePath, SavedMazePath> entry : this.crossConnections) {
            MazePassage key = Maze.rotated(entry.getLeft().build(), transform, size);
            MazePassage val = Maze.rotated(entry.getRight().build(), transform, size);
            if (f.test(key) && f.test(val)) {
               builder.put(key, val);
            }
         }
         return builder;
      }

      private static void addInterconnections(ImmutableMultimap.Builder<MazePassage, MazePassage> builder, Stream<MazePassage> existing) {
         existing.reduce((last, current) -> {
            if (last != null) {
               builder.put(last, current);
               builder.put(current, last);
            }
            return current;
         });
      }

      static SavedMazeReachability fromJson(JsonObject o) {
         SavedMazeReachability r = new SavedMazeReachability();
         if (o.has("groups") && o.get("groups").isJsonArray()) {
            for (JsonElement g : o.getAsJsonArray("groups")) {
               Set<SavedMazePath> set = new HashSet<>();
               for (JsonElement p : g.getAsJsonArray()) {
                  set.add(SavedMazePath.fromJson(p.getAsJsonObject()));
               }
               r.groups.add(set);
            }
         }
         if (o.has("crossConnections") && o.get("crossConnections").isJsonArray()) {
            for (JsonElement e : o.getAsJsonArray("crossConnections")) {
               JsonObject c = e.getAsJsonObject();
               r.crossConnections.add(Pair.of(SavedMazePath.fromJson(c.getAsJsonObject("left")), SavedMazePath.fromJson(c.getAsJsonObject("right"))));
            }
         }
         r.groupByDefault = !o.has("groupByDefault") || o.get("groupByDefault").getAsBoolean();
         return r;
      }

      static SavedMazeReachability fromNbt(CompoundTag c) {
         SavedMazeReachability r = new SavedMazeReachability();
         for (Tag g : c.getList("groups", Tag.TAG_LIST)) {
            Set<SavedMazePath> set = new HashSet<>();
            for (Tag p : (ListTag)g) {
               set.add(SavedMazePath.fromNbt((CompoundTag)p));
            }
            r.groups.add(set);
         }
         for (Tag t : c.getList("crossConnections", Tag.TAG_COMPOUND)) {
            CompoundTag cc = (CompoundTag)t;
            r.crossConnections.add(Pair.of(SavedMazePath.fromNbt(cc.getCompound("key")), SavedMazePath.fromNbt(cc.getCompound("val"))));
         }
         r.groupByDefault = c.getBoolean("groupByDefault");
         return r;
      }
   }

   static final class SavedMazeComponent {
      final Selection rooms = new Selection();
      final List<SavedMazePathConnection> exitPaths = new ArrayList<>();
      String defaultConnector;
      SavedMazeReachability reachability = new SavedMazeReachability();

      boolean isValid() {
         return !this.rooms.isEmpty();
      }

      Collection<MazeRoom> getRooms() {
         return this.rooms.compile(true).keySet();
      }

      int[] boundsSize() {
         return this.rooms.boundsSize();
      }

      /** SavedMazeComponent.Serializer (structure.json of maze components). */
      static SavedMazeComponent fromJson(JsonObject o) {
         SavedMazeComponent c = new SavedMazeComponent();
         c.defaultConnector = str(o, "defaultConnector", "Wall");
         if (o.has("roomArea")) {
            for (JsonElement e : o.getAsJsonArray("roomArea")) {
               JsonObject a = e.getAsJsonObject();
               c.rooms.add(new Area(a.get("additive").getAsBoolean(), ints(a.getAsJsonArray("minCoord")), ints(a.getAsJsonArray("maxCoord"))));
            }
         }
         if (o.has("rooms")) {
            for (JsonElement e : o.getAsJsonArray("rooms")) {
               int[] coords = ints(e.getAsJsonObject().getAsJsonArray("coordinates"));
               c.rooms.add(new Area(true, coords, coords.clone()));
            }
         }
         for (JsonElement e : o.getAsJsonArray("exits")) {
            c.exitPaths.add(SavedMazePathConnection.fromJson(e.getAsJsonObject()));
         }
         if (o.has("reachability")) {
            c.reachability = SavedMazeReachability.fromJson(o.getAsJsonObject("reachability"));
         }
         return c;
      }
   }

   /** MazeGeneration (a maze component structure). */
   public record MazeGen(String id, Double weight, String mazeID, SavedMazeComponent component) {
      public double getWeight() {
         return this.weight != null ? this.weight : 1.0;
      }

      public static MazeGen fromJson(JsonObject o) {
         JsonObject componentJson = o.has("component") ? o.getAsJsonObject("component") : new JsonObject();
         Double weight = o.has("weight") ? o.get("weight").getAsDouble() : null;
         if (weight == null) {
            if (componentJson.has("weightD")) {
               weight = componentJson.get("weightD").getAsDouble();
            } else if (componentJson.has("weight")) {
               weight = componentJson.get("weight").getAsInt() * 0.01;
            }
         }
         return new MazeGen(str(o, "id", ""), weight, str(o, "mazeID", ""), SavedMazeComponent.fromJson(componentJson));
      }
   }

   public static final class MazeComponentStructure implements WeightedMazeComponent<Connector> {
      final ImmutableSet<MazeRoom> rooms;
      final ImmutableMap<MazePassage, Connector> exits;
      final ImmutableMultimap<MazePassage, MazePassage> reachability;
      final double weight;
      final String structureID;
      final Map<String, Boolean> variableDomain;
      final RcTransform transform;

      MazeComponentStructure(double weight, String structureID, Map<String, Boolean> variableDomain, RcTransform transform, ImmutableSet<MazeRoom> rooms,
                             ImmutableMap<MazePassage, Connector> exits, ImmutableMultimap<MazePassage, MazePassage> reachability) {
         this.weight = weight;
         this.structureID = structureID;
         this.variableDomain = variableDomain;
         this.transform = transform;
         this.rooms = rooms;
         this.exits = exits;
         this.reachability = reachability;
      }

      @Override
      public double getWeight() {
         return this.weight;
      }

      @Override
      public Set<MazeRoom> rooms() {
         return this.rooms;
      }

      @Override
      public Map<MazePassage, Connector> exits() {
         return this.exits;
      }

      @Override
      public ImmutableMultimap<MazePassage, MazePassage> reachability() {
         return this.reachability;
      }

      /** getSize (with its else-if quirk: a coordinate below the lowest never raises the highest). */
      int[] getSize() {
         int[] lowest = null;
         int[] highest = null;
         for (MazeRoom room : this.rooms) {
            for (int i = 0; i < room.getDimensions(); i++) {
               if (lowest == null) {
                  lowest = room.getCoordinates();
                  highest = room.getCoordinates();
               } else if (room.getCoordinate(i) < lowest[i]) {
                  lowest[i] = room.getCoordinate(i);
               } else if (room.getCoordinate(i) > highest[i]) {
                  highest[i] = room.getCoordinate(i);
               }
            }
         }
         if (lowest == null) {
            throw new UnsupportedOperationException();
         }
         int[] size = new int[lowest.length];
         for (int i = 0; i < size.length; i++) {
            size[i] = highest[i] - lowest[i] + 1;
         }
         return size;
      }
   }

   // ------------------------------------------------------------------ predicates

   static final class BlockedConnectorStrategy<C> implements MazePredicate<C> {
      private final Collection<C> ignored;

      BlockedConnectorStrategy(Collection<C> ignored) {
         this.ignored = ignored;
      }

      @Override
      public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         return true;
      }

      @Override
      public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c) {
         return !this.ignored.contains(c);
      }
   }

   static final class LimitAABBStrategy<C> implements MazePredicate<C>, Predicate<MazeRoom> {
      private final int[] roomNumbers;

      LimitAABBStrategy(int[] roomNumbers) {
         this.roomNumbers = roomNumbers;
      }

      @Override
      public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         return component.rooms().stream().allMatch(this);
      }

      @Override
      public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c) {
         return this.test(dest);
      }

      @Override
      public boolean test(MazeRoom input) {
         for (int i = 0; i < input.getDimensions(); i++) {
            if (input.getCoordinate(i) < 0 || input.getCoordinate(i) >= this.roomNumbers[i]) {
               return false;
            }
         }
         return true;
      }
   }

   /** ReachabilityStrategy (the "connect"/"connectall" rules). */
   static final class ReachabilityStrategy<C> implements MazePredicate<C> {
      private final Collection<Ability<C>> traversalAbilities = new ArrayList<>();
      private ConnectionPoint mainConnectionPoint;
      private final List<ConnectionPoint> connectionPoints = new ArrayList<>();
      private final Map<ConnectionPoint, Integer> stepsReached = new HashMap<>();
      private final Predicate<MazeRoom> confiner;
      private final ConnectionStrategy<C> connectionStrategy;
      private final boolean preventConnection;

      ReachabilityStrategy(Predicate<MazeRoom> confiner, ConnectionStrategy<C> connectionStrategy, boolean preventConnection) {
         this.confiner = confiner;
         this.connectionStrategy = connectionStrategy;
         this.preventConnection = preventConnection;
      }

      static <C> ReachabilityStrategy<C> connect(Collection<Collection<MazePassage>> points, Predicate<MazeRoom> confiner,
                                                 Collection<Ability<C>> traversalAbilities, ConnectionStrategy<C> connectionStrategy) {
         ReachabilityStrategy<C> strategy = new ReachabilityStrategy<>(confiner, connectionStrategy, false);
         strategy.setConnection(points);
         strategy.traversalAbilities.addAll(traversalAbilities);
         return strategy;
      }

      static <C> ReachabilityStrategy<C> preventConnection(Collection<Collection<MazePassage>> points, Predicate<MazeRoom> confiner,
                                                           ConnectionStrategy<C> connectionStrategy) {
         ReachabilityStrategy<C> strategy = new ReachabilityStrategy<>(confiner, connectionStrategy, true);
         strategy.setConnection(points);
         return strategy;
      }

      static <C> Collection<Ability<C>> compileAbilities(Collection<? extends MazeComponent<C>> components) {
         Collection<Ability<C>> abilities = new HashSet<>();
         for (MazeComponent<C> component : components) {
            for (MazePassage source : component.reachability().keySet()) {
               if (component.rooms().contains(source.getSource())) {
                  for (MazePassage exit : traverse(Collections.singleton(component), new HashSet<>(), Collections.singleton(source), null)) {
                     if (!component.rooms().contains(exit.getSource()) && !source.equals(exit.inverse())) {
                        Ability<C> ability = Ability.from(source, exit);
                        Optional<Ability<C>> existing = abilities.stream().filter(a -> a.same(ability)).findFirst();
                        if (existing.isPresent()) {
                           existing.get().add(component);
                        } else {
                           ability.add(component);
                           abilities.add(ability);
                        }
                     }
                  }
               }
            }
         }
         abilities.forEach(ability -> {
            for (int i = 0; i < ability.masks.size(); i++) {
               Ability<C>.Mask mask = ability.masks.get(i);
               ability.masks.remove(i);
               if (approximateCanReach(mask.rooms, (BiPredicate<C, MazePassage>)(c, p) -> compatible(mask.exits.get(p), c), abilities,
                  Collections.singleton(ability.start()), Collections.singleton(ability.destination()), null) != null) {
                  i--;
               } else {
                  ability.masks.add(i, mask);
               }
            }
         });
         abilities.removeIf(a -> a.masks.isEmpty());
         return abilities;
      }

      static <C> boolean compatible(C existing, C add) {
         return existing == null || add == null || existing.equals(add);
      }

      static <C> Set<MazePassage> traverse(Collection<MazeComponent<C>> mazes, Collection<MazePassage> traversed, Collection<MazePassage> connections,
                                           @Nullable Consumer<MazePassage> visitor) {
         if (connections.size() <= 0) {
            return Collections.emptySet();
         }
         Deque<MazePassage> dirty = new ArrayDeque<>(connections);
         Set<MazePassage> added = new HashSet<>();
         MazePassage src;
         while ((src = dirty.pollFirst()) != null) {
            for (MazeComponent<C> maze : mazes) {
               maze.reachability().get(src).forEach(dest -> {
                  if (traversed.add(dest)) {
                     if (visitor != null) {
                        visitor.accept(dest);
                     }
                     added.add(dest);
                     dirty.addLast(dest);
                  }
               });
            }
         }
         return added;
      }

      private static <C> Set<MazeRoom> approximateCanReach(Set<MazeRoom> rooms, BiPredicate<C, MazePassage> connector, Collection<Ability<C>> abilities,
                                                           Set<MazePassage> left, Set<MazePassage> right, Predicate<MazeRoom> confiner) {
         return approximateCanReach(rooms, abilities, Collections.emptyList(), left, right, Collections.emptyList(), confiner, connector);
      }

      private static <C> Set<MazeRoom> approximateCanReach(Set<MazeRoom> rooms, Collection<Ability<C>> abilities, Collection<MazeComponent<C>> mazes,
                                                           Set<MazePassage> left, Set<MazePassage> right, Collection<MazePassage> pTraversed,
                                                           Predicate<MazeRoom> confiner, BiPredicate<C, MazePassage> connector) {
         if (left.size() <= 0 || right.size() <= 0) {
            return null;
         }
         if (left.stream().anyMatch(right::contains)) {
            return null;
         }
         Collection<MazePassage> traversed = Sets.newHashSet(pTraversed);
         Predicate<MazeRoom> roomPlaceable = confiner != null ? o -> confiner.test(o) && !rooms.contains(o) : rooms::contains;
         Predicate<MazePassage> passagePlaceable = o -> roomPlaceable.test(o.getSource());
         Set<MazePassage> visited = Sets.newHashSet(left);
         TreeSet<MazePassage> dirty = Sets.newTreeSet((o1, o2) -> {
            int compare;
            if ((compare = Double.compare(minDistanceSQ(o1, right), minDistanceSQ(o2, right))) != 0) {
               return compare;
            } else if ((compare = compare(o1.getSource().getCoordinates(), o2.getSource().getCoordinates())) != 0) {
               return compare;
            } else {
               return compare(o1.getDest().getCoordinates(), o2.getDest().getCoordinates());
            }
         });
         dirty.addAll(left);
         visited.addAll(left);
         MazePassage curPre;
         while ((curPre = dirty.pollFirst()) != null) {
            MazePassage cur = curPre;
            MazePassage curNormal = curPre.normalize();
            List<Ability<C>> usable = abilities.stream()
               .filter(a -> !visited.contains(a.destination().add(cur.getSource())))
               .filter(a -> a.start.getDest().equals(curNormal.getDest()))
               .filter(a -> a.connect(cur.getSource(), roomPlaceable, connector))
               .collect(Collectors.toList());
            // the original filtered lazily while visiting; "visited" changes inside the loop, so re-check it per ability
            for (Ability<C> ability : usable) {
               if (visited.contains(ability.destination().add(cur.getSource()))) {
                  continue;
               }
               MazePassage dest = ability.destination().add(cur.getSource());
               if (right.contains(dest)) {
                  return compileRoute(visited);
               }
               if (passagePlaceable.test(dest) && visited.add(dest)) {
                  dirty.add(dest);
               }
               for (MazePassage p : traverse(mazes, traversed, Collections.singleton(dest), null).stream().distinct().toList()) {
                  if (right.contains(p)) {
                     return compileRoute(visited);
                  }
                  if (passagePlaceable.test(p) && visited.add(p)) {
                     dirty.add(p);
                  }
               }
            }
         }
         return null;
      }

      static Set<MazeRoom> compileRoute(Set<MazePassage> passages) {
         return passages.stream().map(MazePassage::getSource).collect(Collectors.toSet());
      }

      private static int compare(int[] left, int[] right) {
         for (int i = 0; i < left.length; i++) {
            int cmp = Integer.compare(left[i], right[i]);
            if (cmp != 0) {
               return cmp;
            }
         }
         return 0;
      }

      private static double minDistanceSQ(MazePassage passage, Collection<MazePassage> rooms) {
         return rooms.stream().map(MazePassage::getDest).mapToDouble(o -> o.distanceSQ(passage.getSource())).min().orElseThrow(InternalError::new);
      }

      static <C> C exitFromEither(MazeComponent<C> left, MazeComponent<C> right, MazePassage p) {
         C c = left.exits().get(p);
         return c != null ? c : right.exits().get(p);
      }

      void setConnection(Collection<Collection<MazePassage>> points) {
         this.connectionPoints.addAll(points.stream()
            .map(p -> new ConnectionPoint(p, p.stream().map(MazePassage::inverse).collect(Collectors.toList()))).toList());
         this.mainConnectionPoint = this.connectionPoints.size() > 0 ? this.connectionPoints.remove(0) : null;
      }

      @Override
      public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         if (this.preventConnection && !this.stepsReached.isEmpty()) {
            return true;
         }
         if (this.stepsReached.size() == this.connectionPoints.size()) {
            return true;
         }
         Predicate<MazePassage> isDirtyPre = this.dirtyPassages(maze.exits().keySet());
         boolean[] unconnectable = new boolean[this.connectionPoints.size()];
         for (int i = 0; i < this.connectionPoints.size(); i++) {
            if (this.connectionPoints.get(i).traversed.stream().noneMatch(isDirtyPre)) {
               unconnectable[i] = true;
            }
         }
         this.place(maze, component);
         Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
         Set<MazePassage> exitsFromBoth = Sets.union(maze.exits().keySet(), component.exits().keySet());
         Predicate<MazePassage> isDirty = this.dirtyPassages(exitsFromBoth);
         boolean canPlace;
         if (this.preventConnection) {
            canPlace = this.stepsReached.isEmpty();
         } else {
            canPlace = true;
            for (int i = 0; i < this.connectionPoints.size(); i++) {
               ConnectionPoint point = this.connectionPoints.get(i);
               if (point.intersectsRoute(component)) {
                  canPlace = this.stepsReached.containsKey(point) || unconnectable[i]
                     || (point.route = approximateCanReach(roomsFromBoth, this.traversalAbilities, Arrays.asList(maze, component),
                     point.traversed.stream().filter(isDirty).collect(Collectors.toSet()),
                     this.mainConnectionPoint.traversed.stream().filter(isDirty).map(MazePassage::inverse).collect(Collectors.toSet()),
                     point.traversed, this.confiner,
                     (c, p) -> this.connectionStrategy.connect(p, exitFromEither(maze, component, p.inverse()), c) > 0.0F)) != null;
                  if (!canPlace) {
                     break;
                  }
               }
            }
         }
         this.unplace();
         return canPlace;
      }

      Predicate<MazePassage> dirtyPassages(Set<MazePassage> r) {
         return input -> this.confiner.test(input.getSource()) && !r.contains(input);
      }

      @Override
      public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.place(maze, component);
         this.connectionPoints.stream().filter(p -> p.intersectsRoute(component)).forEach(point -> point.route = null);
      }

      @Override
      public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      @Override
      public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
      }

      void place(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         if (this.stepsReached.size() == this.connectionPoints.size()) {
            this.stepsReached.replaceAll((k, v) -> v + 1);
         } else {
            for (ConnectionPoint point : this.connectionPoints) {
               if (this.stepsReached.containsKey(point)) {
                  this.stepsReached.merge(point, 1, Integer::sum);
               } else {
                  point.order.add(this.traverse(maze, component, point.traversed, this.mainConnectionPoint.traversed, p -> this.stepsReached.put(point, 0)));
               }
            }
            this.mainConnectionPoint.order.add(this.traverse(maze, component, this.mainConnectionPoint.traversed,
               this.connectionPoints.stream().filter(point -> !this.stepsReached.containsKey(point)).flatMap(point -> point.traversed.stream())
                  .collect(Collectors.toList()),
               p -> this.connectionPoints.stream().filter(point -> point.traversed.contains(p)).forEach(point -> this.stepsReached.put(point, 0))));
         }
      }

      Set<MazePassage> traverse(MazeComponent<C> maze, MazeComponent<C> component, Set<MazePassage> traversed, Collection<MazePassage> goal,
                                Consumer<MazePassage> goalConsumer) {
         return traverse(Arrays.asList(maze, component), traversed, Sets.intersection(component.exits().keySet(), traversed), connection -> {
            if (goal.contains(connection)) {
               goalConsumer.accept(connection);
            }
         });
      }

      @Override
      public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.unplace();
      }

      void unplace() {
         this.stepsReached.replaceAll((k, v) -> v - 1);
         this.stepsReached.values().removeIf(i -> i < 0);
         if (this.stepsReached.size() < this.connectionPoints.size()) {
            this.mainConnectionPoint.reverseStep();
            this.connectionPoints.stream().filter(point -> point.order.size() > this.mainConnectionPoint.order.size())
               .forEach(ConnectionPoint::reverseStep);
         }
      }

      @Override
      public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c) {
         return true;
      }

      static final class Ability<C> {
         final MazePassage start;
         final MazePassage destination;
         final List<Mask> masks = new ArrayList<>();

         Ability(MazePassage start, MazePassage destination) {
            this.start = start;
            this.destination = destination;
         }

         static <C> Ability<C> from(MazePassage start, MazePassage destination) {
            return new Ability<>(start.normalize(), destination.sub(start.getSource()));
         }

         boolean same(Ability<C> ability) {
            return this.start.equals(ability.start) && this.destination.equals(ability.destination);
         }

         void add(MazeComponent<C> component) {
            this.masks.add(new Mask(component.rooms().stream().map(r -> r.sub(this.start.getSource())).collect(Collectors.toSet()),
               component.exits().keySet().stream().collect(Collectors.toMap(r -> r.sub(this.start.getSource()), component.exits()::get))));
         }

         boolean connect(MazeRoom reference, Predicate<MazeRoom> roomPlaceable, BiPredicate<C, MazePassage> connector) {
            return this.masks.stream().anyMatch(mask -> mask.rooms.stream().map(r -> r.add(reference)).allMatch(roomPlaceable)
               && mask.exits.keySet().stream().allMatch(p -> connector.test(mask.exits.get(p), p.add(reference))));
         }

         MazePassage start() {
            return this.start;
         }

         MazePassage destination() {
            return this.destination;
         }

         @Override
         public boolean equals(Object o) {
            if (this == o) {
               return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
               return false;
            }
            Ability<?> ability = (Ability<?>)o;
            return this.start.equals(ability.start) && this.destination.equals(ability.destination) && this.masks.equals(ability.masks);
         }

         @Override
         public int hashCode() {
            int result = this.start.hashCode();
            result = 31 * result + this.destination.hashCode();
            return 31 * result + this.masks.hashCode();
         }

         final class Mask {
            final Set<MazeRoom> rooms;
            final Map<MazePassage, C> exits;

            Mask(Set<MazeRoom> rooms, Map<MazePassage, C> exits) {
               this.rooms = rooms;
               this.exits = exits;
            }

            @Override
            public boolean equals(Object o) {
               if (this == o) {
                  return true;
               }
               if (o == null || this.getClass() != o.getClass()) {
                  return false;
               }
               @SuppressWarnings("unchecked")
               Mask mask = (Mask)o;
               return this.rooms.equals(mask.rooms) && this.exits.equals(mask.exits);
            }

            @Override
            public int hashCode() {
               return 31 * this.rooms.hashCode() + this.exits.hashCode();
            }
         }
      }

      final class ConnectionPoint {
         final Set<MazePassage> traversed = new HashSet<>();
         final List<Set<MazePassage>> order = new ArrayList<>();
         Set<MazeRoom> route = null;

         @SafeVarargs
         ConnectionPoint(Collection<MazePassage>... points) {
            Arrays.stream(points).forEach(this.traversed::addAll);
         }

         void reverseStep() {
            this.traversed.removeAll(this.order.remove(this.order.size() - 1));
         }

         boolean intersectsRoute(ShiftedMazeComponent<?, C> component) {
            return this.route == null || component.rooms().stream().anyMatch(r -> this.route.contains(r));
         }
      }
   }

   // ------------------------------------------------------------------ rules

   /** MazeRuleConnectAll ("connectall") and MazeRuleConnect ("connect"). */
   record Rule(String type, boolean additive, boolean preventConnection, List<SavedMazePath> exits, List<SavedMazePath> start, List<SavedMazePath> end) {
      static Rule fromNbt(CompoundTag t) {
         CompoundTag r = t.getCompound("rule");
         return new Rule(t.getString("type"), r.getBoolean("additive"), r.getBoolean("preventConnection"), paths(r.getList("exits", Tag.TAG_COMPOUND)),
            paths(r.getList("expect", Tag.TAG_COMPOUND)), paths(r.getList("end", Tag.TAG_COMPOUND)));
      }

      private static List<SavedMazePath> paths(ListTag l) {
         List<SavedMazePath> out = new ArrayList<>();
         for (Tag t : l) {
            out.add(SavedMazePath.fromNbt((CompoundTag)t));
         }
         return out;
      }

      @Nullable
      MazePredicate<Connector> build(SavedMazeComponent mazeComponent, Set<Connector> blocked, ConnectorFactory factory,
                                     Collection<? extends MazeComponent<Connector>> components, ConnectionStrategy<Connector> strategy) {
         List<Collection<MazePassage>> points;
         if (this.type.equals("connectall")) {
            List<SavedMazePath> paths = this.additive ? this.exits : mazeComponent.exitPaths.stream()
               .filter(e -> !blocked.contains(factory.get(e.connector))).map(e -> e.path).filter(e -> !this.exits.contains(e)).toList();
            if (paths.size() <= 1) {
               return null;
            }
            points = paths.stream().map(SavedMazePath::build).map(p -> (Collection<MazePassage>)Collections.singleton(p)).collect(Collectors.toList());
         } else if (this.type.equals("connect")) {
            if (this.start.isEmpty() || this.end.isEmpty()) {
               return null;
            }
            points = Arrays.asList(this.start.stream().map(SavedMazePath::build).collect(Collectors.toSet()),
               this.end.stream().map(SavedMazePath::build).collect(Collectors.toSet()));
         } else {
            return null;
         }
         Predicate<MazeRoom> confiner = new LimitAABBStrategy<Object>(mazeComponent.boundsSize());
         return this.preventConnection ? ReachabilityStrategy.preventConnection(points, confiner, strategy)
            : ReachabilityStrategy.connect(points, confiner, ReachabilityStrategy.compileAbilities(components), strategy);
      }
   }

   // ------------------------------------------------------------------ WorldGenMaze

   static ImmutableMultimap.Builder<MazePassage, MazePassage> addExternalReachability(ImmutableMultimap.Builder<MazePassage, MazePassage> reachability,
                                                                                   Map<MazePassage, Connector> exits, Collection<Connector> blocked) {
      exits.keySet().stream().filter(passage -> !blocked.contains(exits.get(passage))).forEach(passage -> reachability.put(passage, passage.inverse()));
      return reachability;
   }

   static <C> SetMazeComponent<C> createCompleteComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits, C wall) {
      SetMazeComponent<C> component = new SetMazeComponent<>(rooms, exits, HashMultimap.create());
      addMissingExits(component.rooms, component.exits, wall);
      return component;
   }

   static <C> void addMissingExits(Set<MazeRoom> rooms, Map<MazePassage, C> exits, C connector) {
      for (MazeRoom room : rooms) {
         Maze.neighborPassages(room).filter(p -> !exits.containsKey(p) && (!rooms.contains(p.getLeft()) || !rooms.contains(p.getRight())))
            .forEach(p -> exits.put(p, connector));
      }
   }

   static Stream<Entry<MazePassage, Connector>> buildExitPaths(RcEnv environment, ConnectorFactory factory, List<SavedMazePathConnection> mazeExits,
                                                              Collection<MazeRoom> rooms) {
      return mazeExits.stream().map(c -> c.build(environment, factory))
         .map(e -> rooms.contains(e.getKey().getSource()) ? e : Pair.of(e.getKey().inverse(), e.getValue()));
   }

   // ------------------------------------------------------------------ helpers

   static String str(JsonObject o, String k, String def) {
      return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : def;
   }

   static int[] ints(JsonArray a) {
      int[] out = new int[a.size()];
      for (int i = 0; i < out.length; i++) {
         out[i] = a.get(i).getAsInt();
      }
      return out;
   }
}
