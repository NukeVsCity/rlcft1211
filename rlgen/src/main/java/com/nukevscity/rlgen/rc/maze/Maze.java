package com.nukevscity.rlgen.rc.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.rc.RcTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;

/** IvToolkit 1.3.3 maze components (ivorius.ivtoolkit.maze.components) and the weighted helpers they use. */
public final class Maze {
   private static final Logger LOGGER = LogUtils.getLogger();

   private Maze() {
   }

   // ------------------------------------------------------------------ rooms and passages

   public static final class MazeRoom {
      private final int[] coordinates;

      public MazeRoom(int... coordinates) {
         this.coordinates = coordinates.clone();
      }

      public int getDimensions() {
         return this.coordinates.length;
      }

      public int[] getCoordinates() {
         return this.coordinates.clone();
      }

      public int getCoordinate(int index) {
         return this.coordinates[index];
      }

      public MazeRoom add(MazeRoom room) {
         int[] c = this.coordinates.clone();
         for (int i = 0; i < c.length; i++) {
            c[i] += room.coordinates[i];
         }
         return new MazeRoom(c);
      }

      public MazeRoom addInDimension(int dimension, int count) {
         int[] c = this.coordinates.clone();
         c[dimension] += count;
         return new MazeRoom(c);
      }

      public MazeRoom sub(MazeRoom room) {
         int[] c = this.coordinates.clone();
         for (int i = 0; i < c.length; i++) {
            c[i] -= room.coordinates[i];
         }
         return new MazeRoom(c);
      }

      public double distanceSQ(MazeRoom room) {
         double d = 0.0;
         for (int i = 0; i < this.coordinates.length; i++) {
            double v = this.coordinates[i] - room.coordinates[i];
            d += v * v;
         }
         return d;
      }

      @Override
      public boolean equals(Object o) {
         return this == o || o != null && this.getClass() == o.getClass() && Arrays.equals(this.coordinates, ((MazeRoom)o).coordinates);
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.coordinates);
      }

      @Override
      public String toString() {
         return Arrays.toString(this.coordinates);
      }
   }

   /** MazePassage extends commons-lang Pair, which adds its own equals/hashCode on top. */
   public static final class MazePassage extends Pair<MazeRoom, MazeRoom> {
      private final MazeRoom source;
      private final MazeRoom dest;

      public MazePassage(MazeRoom source, MazeRoom dest) {
         this.source = source;
         this.dest = dest;
         if (source.getDimensions() != dest.getDimensions()) {
            throw new IllegalArgumentException();
         }
      }

      public MazeRoom getSource() {
         return this.source;
      }

      public MazeRoom getDest() {
         return this.dest;
      }

      @Override
      public MazeRoom getLeft() {
         return this.source;
      }

      @Override
      public MazeRoom getRight() {
         return this.dest;
      }

      public MazePassage add(MazeRoom add) {
         return new MazePassage(this.source.add(add), this.dest.add(add));
      }

      public MazePassage sub(MazeRoom sub) {
         return new MazePassage(this.source.sub(sub), this.dest.sub(sub));
      }

      @Nullable
      public MazeRoom inverseDistance(MazePassage connection) {
         MazeRoom leftDist = connection.source.sub(this.dest);
         return leftDist.equals(connection.dest.sub(this.source)) ? leftDist : null;
      }

      public MazePassage inverse() {
         return new MazePassage(this.dest, this.source);
      }

      public MazePassage normalize() {
         return new MazePassage(new MazeRoom(new int[this.source.getDimensions()]), this.dest.sub(this.source));
      }

      public boolean has(MazeRoom room) {
         return Objects.equals(this.source, room) || Objects.equals(this.dest, room);
      }

      @Override
      public MazeRoom setValue(MazeRoom value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || this.getClass() != o.getClass() || !super.equals(o)) {
            return false;
         }
         MazePassage that = (MazePassage)o;
         return Objects.equals(this.source, that.source) && Objects.equals(this.dest, that.dest);
      }

      @Override
      public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + (this.source != null ? this.source.hashCode() : 0);
         return 31 * result + (this.dest != null ? this.dest.hashCode() : 0);
      }

      @Override
      public String toString() {
         return "MazePassage{source=" + this.source + ", dest=" + this.dest + '}';
      }
   }

   /** MazeRooms / MazePassages */
   public static MazeRoom rotated(MazeRoom room, RcTransform transform, int[] size) {
      if (room.getDimensions() < 3) {
         throw new IllegalArgumentException();
      }
      return new MazeRoom(applyOn(transform, room.getCoordinates(), size, 1));
   }

   public static MazePassage rotated(MazePassage passage, RcTransform transform, int[] size) {
      return new MazePassage(rotated(passage.getSource(), transform, size), rotated(passage.getDest(), transform, size));
   }

   /** AxisAlignedTransform2D.applyOn(int[], int[], int) */
   static int[] applyOn(RcTransform t, int[] position, int[] size, int centerCorrection) {
      int positionX = t.mirrorX() ? size[0] - centerCorrection - position[0] : position[0];
      int x;
      int z;
      switch (t.rotation()) {
         case 0 -> {
            x = positionX;
            z = position[2];
         }
         case 1 -> {
            x = size[2] - centerCorrection - position[2];
            z = positionX;
         }
         case 2 -> {
            x = size[0] - centerCorrection - positionX;
            z = size[2] - centerCorrection - position[2];
         }
         default -> {
            x = position[2];
            z = size[0] - centerCorrection - positionX;
         }
      }
      position[0] = x;
      position[2] = z;
      return position;
   }

   public static Stream<MazeRoom> neighbors(MazeRoom room) {
      return IntStream.range(0, room.getDimensions()).mapToObj(d -> IntStream.of(1, -1).mapToObj(m -> room.addInDimension(d, m))).flatMap(t -> t);
   }

   public static Stream<MazePassage> neighborPassages(MazeRoom room) {
      return neighbors(room).map(r -> new MazePassage(room, r));
   }

   // ------------------------------------------------------------------ components

   public interface MazeComponent<C> {
      Set<MazeRoom> rooms();

      Map<MazePassage, C> exits();

      Multimap<MazePassage, MazePassage> reachability();
   }

   public interface WeightedMazeComponent<C> extends MazeComponent<C> {
      double getWeight();
   }

   public interface MorphingMazeComponent<C> extends MazeComponent<C> {
      void add(MazeComponent<C> component);

      void set(MazeComponent<C> component);

      MorphingMazeComponent<C> copy();
   }

   public interface ConnectionStrategy<T> {
      float connect(MazePassage passage, @Nullable T existing, T add);
   }

   public interface MazePredicate<C> {
      boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component);

      void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component);

      void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component);

      void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component);

      void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component);

      boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c);
   }

   public static final class MazePredicateMany<C> implements MazePredicate<C> {
      public final List<MazePredicate<C>> predicates = new ArrayList<>();

      public MazePredicateMany(List<MazePredicate<C>> predicates) {
         this.predicates.addAll(predicates);
      }

      @Override
      public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         return this.predicates.stream().allMatch(p -> p.canPlace(maze, component));
      }

      @Override
      public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.predicates.forEach(p -> p.willPlace(maze, component));
      }

      @Override
      public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.predicates.forEach(p -> p.didPlace(maze, component));
      }

      @Override
      public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.predicates.forEach(p -> p.willUnplace(maze, component));
      }

      @Override
      public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component) {
         this.predicates.forEach(p -> p.didUnplace(maze, component));
      }

      @Override
      public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c) {
         return this.predicates.stream().allMatch(p -> p.isDirtyConnection(dest, source, c));
      }
   }

   public static final class SetMazeComponent<C> implements MorphingMazeComponent<C> {
      public final Set<MazeRoom> rooms = new HashSet<>();
      public final Map<MazePassage, C> exits = new HashMap<>();
      public final Multimap<MazePassage, MazePassage> reachability = HashMultimap.create();

      public SetMazeComponent() {
      }

      public SetMazeComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits, Multimap<MazePassage, MazePassage> reachability) {
         this.rooms.addAll(rooms);
         this.exits.putAll(exits);
         this.reachability.putAll(reachability);
      }

      @Override
      public Set<MazeRoom> rooms() {
         return this.rooms;
      }

      @Override
      public Map<MazePassage, C> exits() {
         return this.exits;
      }

      @Override
      public Multimap<MazePassage, MazePassage> reachability() {
         return this.reachability;
      }

      @Override
      public void add(MazeComponent<C> component) {
         this.rooms.addAll(component.rooms());
         component.exits().entrySet().stream().filter(entry -> this.exits.remove(entry.getKey()) == null)
            .forEach(entry -> this.exits.put(entry.getKey(), entry.getValue()));
         this.reachability.putAll(component.reachability());
      }

      @Override
      public void set(MazeComponent<C> component) {
         this.rooms.clear();
         this.rooms.addAll(component.rooms());
         this.exits.clear();
         this.exits.putAll(component.exits());
         this.reachability.clear();
         this.reachability.putAll(component.reachability());
      }

      @Override
      public MorphingMazeComponent<C> copy() {
         return new SetMazeComponent<>(this.rooms, this.exits, this.reachability);
      }
   }

   public static final class ShiftedMazeComponent<M extends MazeComponent<C>, C> implements MazeComponent<C> {
      private final M component;
      private final MazeRoom shift;
      private final ImmutableSet<MazeRoom> rooms;
      private final ImmutableMap<MazePassage, C> exits;
      private final ImmutableMultimap<MazePassage, MazePassage> reachability;

      public ShiftedMazeComponent(M component, MazeRoom shift, ImmutableSet<MazeRoom> rooms, ImmutableMap<MazePassage, C> exits,
                                  ImmutableMultimap<MazePassage, MazePassage> reachability) {
         this.component = component;
         this.shift = shift;
         this.rooms = rooms;
         this.exits = exits;
         this.reachability = reachability;
      }

      public M getComponent() {
         return this.component;
      }

      public MazeRoom getShift() {
         return this.shift;
      }

      @Override
      public Set<MazeRoom> rooms() {
         return this.rooms;
      }

      @Override
      public Map<MazePassage, C> exits() {
         return this.exits;
      }

      @Override
      public Multimap<MazePassage, MazePassage> reachability() {
         return this.reachability;
      }
   }

   public static final class MultiMazeComponent<M extends WeightedMazeComponent<C>, C> implements WeightedMazeComponent<C> {
      private final List<M> components;
      private final double weight;

      public MultiMazeComponent(List<M> components) {
         this.components = components;
         this.weight = components.stream().mapToDouble(WeightedMazeComponent::getWeight).sum();
      }

      public M first() {
         return this.components.get(0);
      }

      @Override
      public Set<MazeRoom> rooms() {
         return this.first().rooms();
      }

      @Override
      public Map<MazePassage, C> exits() {
         return this.first().exits();
      }

      @Override
      public Multimap<MazePassage, MazePassage> reachability() {
         return this.first().reachability();
      }

      @Override
      public double getWeight() {
         return this.weight;
      }

      public PlacedMazeComponent<M, C> place(MazeRoom shift, Random random) {
         return new PlacedMazeComponent<>(selectItem(random, this.components, WeightedMazeComponent::getWeight, false), shift);
      }
   }

   public record PlacedMazeComponent<M extends WeightedMazeComponent<C>, C>(M component, MazeRoom shift) {
   }

   // ------------------------------------------------------------------ MazeComponents

   static <M extends MazeComponent<C>, C> Function<M, Stream<ShiftedMazeComponent<M, C>>> shiftAllFunction(MazePassage connection, C connector,
                                                                                                         ConnectionStrategy<C> strategy) {
      return component -> component.exits().entrySet().stream().map(entry -> {
         MazeRoom dist = entry.getKey().inverseDistance(connection);
         return dist != null && strategy.connect(connection, connector, entry.getValue()) >= 0.0F ? shift(component, dist) : null;
      }).filter(Objects::nonNull);
   }

   static <M extends MazeComponent<C>, C> ShiftedMazeComponent<M, C> shift(M component, MazeRoom shift) {
      ImmutableSet<MazeRoom> rooms = component.rooms().stream().map(r -> r != null ? r.add(shift) : null).collect(immutableSet());
      ImmutableMap<MazePassage, C> exits = component.exits().keySet().stream()
         .collect(toImmutableMap(c1 -> c1 != null ? c1.add(shift) : null, component.exits()::get));
      ImmutableMultimap<MazePassage, MazePassage> reachability = component.reachability().keySet().stream()
         .collect(toImmutableMultimap(c -> c.add(shift), c -> component.reachability().get(c).stream().map(c2 -> c2.add(shift))::iterator));
      return new ShiftedMazeComponent<>(component, shift, rooms, exits, reachability);
   }

   static boolean overlap(MazeComponent<?> left, MazeComponent<?> right) {
      return Sets.intersection(left.rooms(), right.rooms()).size() > 0;
   }

   static <C> float connectWeight(MazeComponent<C> existing, MazeComponent<C> add, ConnectionStrategy<C> strategy) {
      float total = 1.0F;
      for (Entry<MazePassage, C> entry : add.exits().entrySet()) {
         if ((total *= strategy.connect(entry.getKey(), existing.exits().get(entry.getKey().inverse()), entry.getValue())) < 0.0F) {
            return -1.0F;
         }
      }
      return total;
   }

   // GuavaCollectors
   static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> immutableSet() {
      return Collector.of(ImmutableSet.Builder::new, ImmutableSet.Builder::add, (l, r) -> l.addAll(r.build()), ImmutableSet.Builder::build,
         Collector.Characteristics.UNORDERED);
   }

   static <T, K, U> Collector<T, ?, ImmutableMap<K, U>> toImmutableMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
      return Collector.of(ImmutableMap.Builder<K, U>::new, (r, t) -> r.put(keyMapper.apply(t), valueMapper.apply(t)), (l, r) -> l.putAll(r.build()),
         ImmutableMap.Builder::build, Collector.Characteristics.UNORDERED);
   }

   static <T, K, U> Collector<T, ?, ImmutableMultimap<K, U>> toImmutableMultimap(Function<? super T, ? extends K> keyMapper,
                                                                              Function<? super T, Iterable<? extends U>> valueMapper) {
      return Collector.of(ImmutableMultimap.Builder<K, U>::new, (r, t) -> r.putAll(keyMapper.apply(t), valueMapper.apply(t)),
         (l, r) -> l.putAll(r.build()), ImmutableMultimap.Builder::build, Collector.Characteristics.UNORDERED);
   }

   // ------------------------------------------------------------------ WeightedSelector / WeightedShuffler

   static <T> T selectItem(Random rand, Collection<T> items, ToDoubleFunction<T> weight, boolean remove) {
      double total = items.stream().mapToDouble(weight).reduce(0.0, Double::sum);
      return select(rand, items, weight, total, remove);
   }

   static <T> T select(Random rand, Collection<T> items, ToDoubleFunction<T> weightFunction, double totalWeight, boolean remove) {
      if (items.isEmpty()) {
         throw new IndexOutOfBoundsException();
      }
      double random = rand.nextDouble() * totalWeight;
      int counted = 0;
      for (Iterator<T> iterator = items.iterator(); iterator.hasNext(); counted++) {
         T t = iterator.next();
         random -= weightFunction.applyAsDouble(t);
         if (random <= 0.0) {
            if (remove) {
               iterator.remove();
            }
            return t;
         }
      }
      // selectWeightless (does not remove, like IvToolkit)
      counted = rand.nextInt(counted);
      Iterator<T> iterator = items.iterator();
      T item;
      do {
         item = iterator.next();
      } while (counted-- != 0 && iterator.hasNext());
      return item;
   }

   record WeightedItem<T>(double weight, T item) {
   }

   static <T> Iterator<T> iterateShuffled(Random rand, List<WeightedItem<T>> items) {
      return new Iterator<>() {
         double totalWeight = items.stream().mapToDouble(WeightedItem::weight).reduce(0.0, Double::sum);

         @Override
         public boolean hasNext() {
            return !items.isEmpty();
         }

         @Override
         public T next() {
            WeightedItem<T> next = this.totalWeight > 1.0E-7 ? select(rand, items, WeightedItem::weight, this.totalWeight, true)
               : items.remove(rand.nextInt(items.size()));
            this.totalWeight -= next.weight();
            return next.item();
         }
      };
   }

   // ------------------------------------------------------------------ MazeComponentConnector

   public static final int INFINITE_REVERSES = -1;

   private static final class ReverseInfo<M extends MazeComponent<C>, C> {
      long shuffleSeed;
      int triedIndices;
      MorphingMazeComponent<C> maze;
      LinkedList<Triple<MazeRoom, MazePassage, C>> exitStack;
      ShiftedMazeComponent<M, C> placed;
   }

   /** RC gives up on a maze after RCConfig.mazeTimeout. */
   public static final class TimeoutException extends RuntimeException {
      TimeoutException() {
         super("rlgen: maze generation timed out");
      }
   }

   public static <M extends WeightedMazeComponent<C>, C> List<PlacedMazeComponent<M, C>> connect(MorphingMazeComponent<C> maze, List<M> components,
                                                                                                 ConnectionStrategy<C> connectionStrategy,
                                                                                                 MazePredicate<C> predicate, Random random, int reverses,
                                                                                                 long deadline) {
      // IvFunctions.group: ImmutableMultimap keyed by (rooms, exits, reachability), first-seen order
      ImmutableMultimap.Builder<Object, M> grouped = ImmutableMultimap.builder();
      for (M c : components) {
         grouped.put(Triple.of(c.rooms(), c.exits(), c.reachability()), c);
      }
      List<MultiMazeComponent<M, C>> multis = grouped.build().asMap().values().stream()
         .map(c -> new MultiMazeComponent<M, C>(Lists.newArrayList(c))).collect(Collectors.toList());
      return connectMulti(maze, multis, connectionStrategy, predicate, random, reverses, deadline).stream()
         .map(placed -> placed.component().place(placed.shift(), random)).collect(Collectors.toList());
   }

   @SuppressWarnings("unchecked")
   static <M extends MultiMazeComponent<MO, C>, MO extends WeightedMazeComponent<C>, C> List<PlacedMazeComponent<M, C>> connectMulti(
      MorphingMazeComponent<C> maze, List<M> components, ConnectionStrategy<C> connectionStrategy, MazePredicate<C> predicate, Random random, int reverses,
      long deadline) {
      List<ReverseInfo<M, C>> placeOrder = new ArrayList<>();
      ReverseInfo<M, C> reversing = null;
      List<PlacedMazeComponent<M, C>> result = new ArrayList<>();
      LinkedList<Triple<MazeRoom, MazePassage, C>> exitStack = new LinkedList<>();
      Predicate<ShiftedMazeComponent<M, C>> componentPredicate = ((Predicate<ShiftedMazeComponent<M, C>>)input -> !overlap(maze, input))
         .and(input -> predicate.canPlace(maze, input));
      addAllExits(predicate, exitStack, maze.exits().entrySet(), random);

      while (exitStack.size() > 0) {
         if (System.nanoTime() > deadline) {
            throw new TimeoutException();
         }
         if (reversing == null) {
            if (maze.rooms().contains(exitStack.peekLast().getLeft())) {
               exitStack.removeLast();
               continue;
            }
            reversing = new ReverseInfo<>();
            reversing.exitStack = (LinkedList<Triple<MazeRoom, MazePassage, C>>)exitStack.clone();
            reversing.maze = maze.copy();
            reversing.shuffleSeed = random.nextLong();
         } else {
            predicate.willUnplace(maze, reversing.placed);
            exitStack = (LinkedList<Triple<MazeRoom, MazePassage, C>>)reversing.exitStack.clone();
            maze.set(reversing.maze);
            predicate.didUnplace(maze, reversing.placed);
            result.remove(result.size() - 1);
         }

         Triple<MazeRoom, MazePassage, C> triple = exitStack.removeLast();
         MazeRoom room = triple.getLeft();
         MazePassage exit = triple.getMiddle();
         C connection = triple.getRight();
         ToDoubleFunction<ShiftedMazeComponent<M, C>> weightFunction = shifted -> shifted.getComponent().getWeight()
            * (double)connectWeight(maze, shifted, connectionStrategy);
         List<WeightedItem<ShiftedMazeComponent<M, C>>> shiftedComponents = components.stream()
            .flatMap(Maze.<M, C>shiftAllFunction(exit, connection, connectionStrategy))
            .map(input -> new WeightedItem<>(weightFunction.applyAsDouble(input), input))
            .filter(w -> w.weight() >= 0.0)
            .collect(Collectors.toCollection(ArrayList::new));
         ShiftedMazeComponent<M, C> placing = null;
         if (reversing.triedIndices > shiftedComponents.size()) {
            throw new RuntimeException("Maze component selection not static.");
         }
         if (reversing.triedIndices < shiftedComponents.size()) {
            Iterator<ShiftedMazeComponent<M, C>> shuffler = iterateShuffled(new Random(reversing.shuffleSeed), shiftedComponents);
            for (int i = 0; i < reversing.triedIndices; i++) {
               shuffler.next();
            }
            while (shuffler.hasNext()) {
               ShiftedMazeComponent<M, C> comp = shuffler.next();
               reversing.triedIndices++;
               if (componentPredicate.test(comp)) {
                  placing = comp;
                  break;
               }
            }
         }

         if (placing == null) {
            if (reverses == 0) {
               LOGGER.debug("rlgen: maze: no fitting component at {}", room);
               reversing = null;
            } else {
               if (reverses > 0) {
                  reverses--;
               }
               if (placeOrder.size() == 0) {
                  LOGGER.debug("rlgen: maze is not completable, switching to flawed mode");
                  reverses = 0;
                  reversing = null;
               } else {
                  reversing = placeOrder.remove(placeOrder.size() - 1);
               }
            }
         } else {
            reversing.placed = placing;
            predicate.willPlace(maze, placing);
            addAllExits(predicate, exitStack, placing.exits().entrySet(), random);
            maze.add(placing);
            result.add(new PlacedMazeComponent<>(placing.getComponent(), placing.getShift()));
            predicate.didPlace(maze, placing);
            placeOrder.add(reversing);
            reversing = null;
         }
      }
      return result;
   }

   private static <C> void addAllExits(MazePredicate<C> placementStrategy, List<Triple<MazeRoom, MazePassage, C>> exitStack,
                                       Set<Entry<MazePassage, C>> entries, Random random) {
      for (Entry<MazePassage, C> exit : entries) {
         MazePassage connection = exit.getKey();
         C c = exit.getValue();
         if (placementStrategy.isDirtyConnection(connection.getLeft(), connection.getRight(), c)) {
            exitStack.add(random.nextInt(exitStack.size() + 1), Triple.of(connection.getLeft(), connection, c));
         }
         if (placementStrategy.isDirtyConnection(connection.getRight(), connection.getLeft(), c)) {
            exitStack.add(random.nextInt(exitStack.size() + 1), Triple.of(connection.getRight(), connection, c));
         }
      }
   }

   static <K, V> Map<K, V> mapOf(Collection<Entry<K, V>> entries) {
      Map<K, V> m = new HashMap<>();
      for (Entry<K, V> e : entries) {
         m.put(e.getKey(), e.getValue());
      }
      return m;
   }

   static <T> Set<T> setOf(Collection<T> c) {
      return new HashSet<>(c);
   }

   static <T> List<T> emptyList() {
      return Collections.emptyList();
   }
}
