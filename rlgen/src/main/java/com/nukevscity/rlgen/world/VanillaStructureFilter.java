package com.nukevscity.rlgen.world;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * The structure sets a legacy world places: no 1.21 overworld structures (1.12 had its own strongholds, temples, monuments and mansions,
 * ported as rlgen structures; the rest never existed). Biome tags can't do it: the vanilla has_structure tags include #is_overworld,
 * #is_ocean etc., which the rlgen biomes are in, and the 1.21 cave biomes below Y 0 are vanilla biomes. Only the deep dark ancient
 * cities (below Y 0) stay; other mods' sets are kept.
 */
final class VanillaStructureFilter implements HolderLookup<StructureSet> {
   private static final Set<String> KEEP = Set.of("minecraft:ancient_cities");

   private final HolderLookup<StructureSet> parent;

   VanillaStructureFilter(HolderLookup<StructureSet> parent) {
      this.parent = parent;
   }

   static boolean placed(ResourceKey<StructureSet> key) {
      return !key.location().getNamespace().equals("minecraft") || KEEP.contains(key.location().toString());
   }

   @Override
   public Stream<Holder.Reference<StructureSet>> listElements() {
      return this.parent.listElements().filter(h -> placed(h.key()));
   }

   @Override
   public Stream<HolderSet.Named<StructureSet>> listTags() {
      return this.parent.listTags();
   }

   @Override
   public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> key) {
      return placed(key) ? this.parent.get(key) : Optional.empty();
   }

   @Override
   public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> tag) {
      return this.parent.get(tag);
   }
}
