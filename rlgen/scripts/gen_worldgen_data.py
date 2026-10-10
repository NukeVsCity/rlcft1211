"""Generates rlgen biome JSONs, biome tags and the Dynamic Trees populator.

Usage: python3 gen_worldgen_data.py <biomes112.tsv> <1.21 biome json dir> <1.21 biome tag data dir> <resources dir>
biomes112.tsv: name, temperature, rainfall, snow, rain, display name (dumped from legacy Biomes).

Every 1.12 biome gets its own rlgen:<name> biome, built from the closest 1.21 biome (TEMPLATE), with the
1.12 climate and the same biome tags as the template. Tree rules replicate Dynamic Trees 1.12.2-0.9.29
assets/dynamictrees/worldgen/default.json, the DT Traverse 2.1 populator and the DT Defiled Lands populator.
"""
import json, os, re, sys, glob

TSV, VBIOMES, VTAGS, RES = sys.argv[1:5]

TEMPLATE = {
 "ocean": "ocean", "plains": "plains", "desert": "desert", "extreme_hills": "windswept_hills", "forest": "forest",
 "taiga": "taiga", "swampland": "swamp", "river": "river", "frozen_ocean": "frozen_ocean", "frozen_river": "frozen_river",
 "ice_flats": "snowy_plains", "ice_mountains": "snowy_plains", "mushroom_island": "mushroom_fields",
 "mushroom_island_shore": "mushroom_fields", "beaches": "beach", "desert_hills": "desert", "forest_hills": "forest",
 "taiga_hills": "taiga", "smaller_extreme_hills": "windswept_hills", "jungle": "jungle", "jungle_hills": "jungle",
 "jungle_edge": "sparse_jungle", "deep_ocean": "deep_ocean", "stone_beach": "stony_shore", "cold_beach": "snowy_beach",
 "birch_forest": "birch_forest", "birch_forest_hills": "birch_forest", "roofed_forest": "dark_forest",
 "taiga_cold": "snowy_taiga", "taiga_cold_hills": "snowy_taiga", "redwood_taiga": "old_growth_pine_taiga",
 "redwood_taiga_hills": "old_growth_pine_taiga", "extreme_hills_with_trees": "windswept_forest", "savanna": "savanna",
 "savanna_rock": "savanna_plateau", "mesa": "badlands", "mesa_rock": "wooded_badlands", "mesa_clear_rock": "badlands",
 "mutated_plains": "sunflower_plains", "mutated_desert": "desert", "mutated_extreme_hills": "windswept_gravelly_hills",
 "mutated_forest": "flower_forest", "mutated_taiga": "taiga", "mutated_swampland": "swamp", "mutated_ice_flats": "ice_spikes",
 "mutated_jungle": "jungle", "mutated_jungle_edge": "sparse_jungle", "mutated_birch_forest": "old_growth_birch_forest",
 "mutated_birch_forest_hills": "old_growth_birch_forest", "mutated_roofed_forest": "dark_forest",
 "mutated_taiga_cold": "snowy_taiga", "mutated_redwood_taiga": "old_growth_spruce_taiga",
 "mutated_redwood_taiga_hills": "old_growth_spruce_taiga", "mutated_extreme_hills_with_trees": "windswept_gravelly_hills",
 "mutated_savanna": "windswept_savanna", "mutated_savanna_rock": "windswept_savanna", "mutated_mesa": "eroded_badlands",
 "mutated_mesa_rock": "wooded_badlands", "mutated_mesa_clear_rock": "badlands",
 # Traverse
 "traverse:autumnal_woods": "forest", "traverse:woodlands": "forest", "traverse:mini_jungle": "jungle",
 "traverse:meadow": "plains", "traverse:green_swamp": "swamp", "traverse:red_desert": "desert",
 "traverse:temperate_rainforest": "taiga", "traverse:badlands": "plains", "traverse:mountainous_desert": "desert",
 "traverse:rocky_plateau": "savanna_plateau", "traverse:forested_hills": "forest", "traverse:birch_forested_hills": "birch_forest",
 "traverse:autumnal_wooded_hills": "forest", "traverse:cliffs": "windswept_hills", "traverse:glacier": "snowy_plains",
 "traverse:glacier_spikes": "ice_spikes", "traverse:snowy_coniferous_forest": "snowy_taiga", "traverse:lush_hills": "plains",
 "traverse:canyon": "badlands", "traverse:crag_cliffs": "windswept_hills", "traverse:desert_shrubland": "desert",
 "traverse:thicket": "forest", "traverse:arid_highland": "savanna", "traverse:rocky_plains": "plains",
 # Defiled Lands
 "defiledlands:desert_defiled": "desert", "defiledlands:plains_defiled": "plains", "defiledlands:forest_tenebra": "dark_forest",
 "defiledlands:forest_vilespine": "plains", "defiledlands:hills_defiled": "windswept_hills",
 "defiledlands:swamp_defiled": "swamp", "defiledlands:ice_plains_defiled": "snowy_plains",
}
SKIP = {"hell", "sky", "void"}

# colours from the Traverse / Defiled Lands 1.12 biome classes: grass, foliage, sky, water
COLOURS = {
 "autumnal_woods": (-2702787, -2960609, -1053479, None), "woodlands": (-6706859, -8085942, -7818015, None),
 "mini_jungle": (None, None, -3997717, -14372836), "meadow": (-10105773, -10627510, None, None),
 "green_swamp": (-11675592, -11675592, None, None), "red_desert": (None, None, -6691, None),
 "temperate_rainforest": (-13401547, -13401547, None, None), "badlands": (-2373764, -4013720, -12650, None),
 "autumnal_wooded_hills": (-2702787, -2960609, -1053479, None), "snowy_coniferous_forest": (-13401547, -13401547, None, None),
 "lush_hills": (-8396738, -10950093, None, None), "canyon": (-7306931, -6389427, -7807489, None),
 "crag_cliffs": (-7306931, -6389427, -7807489, None), "desert_shrubland": (-2044324, None, None, None),
 "thicket": (-9122463, -9588895, None, None), "arid_highland": (-4534920, -8347602, None, None),
}
DEFILED_COLOURS = (4468548, 7035752, 2228275, 7035752)

LEGACY_FEATURES = [[], ["rlgen:legacy_lakes"], ["rlgen:underground/amethyst_geode"], ["minecraft:monster_room_deep"], [], [], ["rlgen:legacy_ores", "minecraft:ore_tuff", "rlgen:underground/ore_iron", "rlgen:underground/ore_gold", "minecraft:ore_gold_lower", "rlgen:underground/ore_redstone", "minecraft:ore_redstone_lower", "rlgen:underground/ore_diamond", "minecraft:ore_diamond_medium", "rlgen:underground/ore_lapis", "rlgen:underground/ore_copper"], [], [], [], ["rlgen:legacy_plants"]]

def rl(name):
    return "rlgen:" + name.split(":")[-1]

# ---------------- biomes ----------------
rows = [l.rstrip("\n").split("\t") for l in open(TSV) if l.strip()]
biomes = {}
for name, temp, rain, snow, canrain, disp in rows:
    if name in SKIP:
        continue
    biomes[name] = dict(temp=float(temp), rain=float(rain), snow=snow == "true", canrain=canrain == "true", disp=disp)

lang = {}
langpath = f"{RES}/assets/rlgen/lang/en_us.json"
if os.path.exists(langpath):
    lang = json.load(open(langpath))
for name, b in biomes.items():
    t = json.load(open(f"{VBIOMES}/{TEMPLATE[name]}.json"))
    t["temperature"] = b["temp"]
    t["downfall"] = max(0.0, min(1.0, b["rain"]))
    t["has_precipitation"] = b["canrain"] or b["snow"]
    t.pop("temperature_modifier", None)
    e = t["effects"]
    short = name.split(":")[-1]
    col = DEFILED_COLOURS if name.startswith("defiledlands:") else COLOURS.get(short)
    if col:
        g, f, s, w = col
        if g is not None:
            e["grass_color"] = g & 0xFFFFFF
            e.pop("grass_color_modifier", None)
        if f is not None: e["foliage_color"] = f & 0xFFFFFF
        if s is not None: e["sky_color"] = s & 0xFFFFFF
        if w is not None: e["water_color"] = w & 0xFFFFFF
    t["features"] = LEGACY_FEATURES  # 1.12 population (rlgen:legacy_*) + 1.21 ores/geodes below Y 0 only
    os.makedirs(f"{RES}/data/rlgen/worldgen/biome", exist_ok=True)
    json.dump(t, open(f"{RES}/data/rlgen/worldgen/biome/{short}.json", "w"), indent=2)
    lang[f"biome.rlgen.{short}"] = b["disp"]
json.dump(lang, open(langpath, "w"), indent=2, sort_keys=True)

# ---------------- tags: same memberships as the template ----------------
tags = {}
for p in glob.glob(f"{VTAGS}/data/*/tags/worldgen/biome/**/*.json", recursive=True):
    ns = p.split("/data/")[1].split("/")[0]
    path = p.split("/tags/worldgen/biome/")[1][:-5]
    vals = json.load(open(p)).get("values", [])
    tags.setdefault(f"{ns}:{path}", []).extend(v if isinstance(v, str) else v["id"] for v in vals)
memo = {}
def members(tag, seen=()):
    if tag in memo: return memo[tag]
    out = set()
    for v in tags.get(tag, []):
        if v.startswith("#"):
            if v[1:] not in seen: out |= members(v[1:], seen + (tag,))
        else:
            out.add(v)
    memo[tag] = out
    return out
added = {}
for name in biomes:
    tmpl = "minecraft:" + TEMPLATE[name]
    for tag in tags:
        if tmpl in members(tag):
            added.setdefault(tag, []).append(rl(name))
# rlgen biomes must never be in the 1.21 overworld noise list, keep everything else
for tag, vals in added.items():
    ns, path = tag.split(":")
    d = f"{RES}/data/{ns}/tags/worldgen/biome/{os.path.dirname(path)}"
    os.makedirs(d, exist_ok=True)
    json.dump({"replace": False, "values": sorted(set(vals))}, open(f"{RES}/data/{ns}/tags/worldgen/biome/{path}.json", "w"), indent=2)

# ---------------- Dynamic Trees populator ----------------
DICT = {  # Forge 1.12 BiomeDictionary.registerVanillaBiomes
 "ocean": "OCEAN", "plains": "PLAINS", "desert": "HOT DRY SANDY", "extreme_hills": "MOUNTAIN HILLS", "forest": "FOREST",
 "taiga": "COLD CONIFEROUS FOREST", "swampland": "WET SWAMP", "river": "RIVER", "frozen_ocean": "COLD OCEAN SNOWY",
 "frozen_river": "COLD RIVER SNOWY", "ice_flats": "COLD SNOWY WASTELAND", "ice_mountains": "COLD SNOWY MOUNTAIN",
 "mushroom_island": "MUSHROOM RARE", "mushroom_island_shore": "MUSHROOM BEACH RARE", "beaches": "BEACH",
 "desert_hills": "HOT DRY SANDY HILLS", "forest_hills": "FOREST HILLS", "taiga_hills": "COLD CONIFEROUS FOREST HILLS",
 "smaller_extreme_hills": "MOUNTAIN", "jungle": "HOT WET DENSE JUNGLE", "jungle_hills": "HOT WET DENSE JUNGLE HILLS",
 "jungle_edge": "HOT WET JUNGLE FOREST RARE", "deep_ocean": "OCEAN", "stone_beach": "BEACH", "cold_beach": "COLD BEACH SNOWY",
 "birch_forest": "FOREST", "birch_forest_hills": "FOREST HILLS", "roofed_forest": "SPOOKY DENSE FOREST",
 "taiga_cold": "COLD CONIFEROUS FOREST SNOWY", "taiga_cold_hills": "COLD CONIFEROUS FOREST SNOWY HILLS",
 "redwood_taiga": "COLD CONIFEROUS FOREST", "redwood_taiga_hills": "COLD CONIFEROUS FOREST HILLS",
 "extreme_hills_with_trees": "MOUNTAIN FOREST SPARSE", "savanna": "HOT SAVANNA PLAINS SPARSE",
 "savanna_rock": "HOT SAVANNA PLAINS SPARSE RARE", "mesa": "MESA SANDY DRY", "mesa_rock": "MESA SANDY DRY SPARSE",
 "mesa_clear_rock": "MESA SANDY DRY", "mutated_plains": "PLAINS RARE", "mutated_desert": "HOT DRY SANDY RARE",
 "mutated_extreme_hills": "MOUNTAIN SPARSE RARE", "mutated_forest": "FOREST HILLS RARE",
 "mutated_taiga": "COLD CONIFEROUS FOREST MOUNTAIN RARE", "mutated_swampland": "WET SWAMP HILLS RARE",
 "mutated_ice_flats": "COLD SNOWY HILLS RARE", "mutated_jungle": "HOT WET DENSE JUNGLE MOUNTAIN RARE",
 "mutated_jungle_edge": "HOT SPARSE JUNGLE HILLS RARE", "mutated_birch_forest": "FOREST DENSE HILLS RARE",
 "mutated_birch_forest_hills": "FOREST DENSE MOUNTAIN RARE", "mutated_roofed_forest": "SPOOKY DENSE FOREST MOUNTAIN RARE",
 "mutated_taiga_cold": "COLD CONIFEROUS FOREST SNOWY MOUNTAIN RARE", "mutated_redwood_taiga": "DENSE FOREST RARE",
 "mutated_redwood_taiga_hills": "DENSE FOREST HILLS RARE", "mutated_extreme_hills_with_trees": "MOUNTAIN SPARSE RARE",
 "mutated_savanna": "HOT DRY SPARSE SAVANNA MOUNTAIN RARE", "mutated_savanna_rock": "HOT DRY SPARSE SAVANNA HILLS RARE",
 "mutated_mesa": "HOT DRY SPARSE MOUNTAIN RARE", "mutated_mesa_rock": "HOT DRY SPARSE HILLS RARE",
 "mutated_mesa_clear_rock": "HOT DRY SPARSE MOUNTAIN RARE",
}
# 1.12 decorator.treesPerChunk (vanilla biome constructors); DT "trees" = clamp(tpc / 10, -1, 1)
TPC = {"plains": 0, "mutated_plains": 0, "desert": -999, "desert_hills": -999, "mutated_desert": -999,
 "forest": 10, "forest_hills": 10, "birch_forest": 10, "birch_forest_hills": 10, "mutated_birch_forest": 10,
 "mutated_birch_forest_hills": 10, "mutated_forest": 6, "roofed_forest": -999, "mutated_roofed_forest": -999,
 "smaller_extreme_hills": 3, "extreme_hills_with_trees": 3, "mutated_extreme_hills_with_trees": 3,
 "jungle": 50, "jungle_hills": 50, "mutated_jungle": 50, "jungle_edge": 2, "mutated_jungle_edge": 2,
 "mesa": -999, "mesa_clear_rock": -999, "mutated_mesa": -999, "mutated_mesa_clear_rock": -999, "mesa_rock": 5, "mutated_mesa_rock": 5,
 "mushroom_island": -100, "mushroom_island_shore": -100, "savanna": 1, "savanna_rock": 1,
 "mutated_savanna": 2, "mutated_savanna_rock": 2, "beaches": -999, "cold_beach": -999, "stone_beach": -999,
 "swampland": 2, "mutated_swampland": 2}
for n in DICT:
    if "taiga" in n and n not in TPC:
        TPC[n] = 10
SPECIES = {"oak": "oak", "birch": "birch", "spruce": "spruce", "acacia": "acacia", "jungle": "jungle",
 "megajungle": "mega_jungle", "darkoak": "dark_oak", "oakswamp": "swamp_oak", "tallbirch": "tall_birch",
 "megaspruce": "mega_spruce", "apple": "apple_oak", "cactus": "...", "mushroomred": "...", "mushroombrn": "...", "...": "..."}

def sp(x):
    if isinstance(x, str):
        return {"random": {SPECIES[x]: 1}} if SPECIES[x] != "..." else "..."
    out = {}
    for k, v in x.items():
        k2 = SPECIES.get(k, k)
        out[k2] = out.get(k2, 0) + v
    return {"random": out}

entries = []
for name in DICT:
    types = set(DICT[name].split())
    trees = max(-1.0, min(1.0, TPC.get(name, 0) / 10.0))
    a = {"species": sp("oak"), "density": f"noise() * {trees}", "chance": "standard"}
    def rule(cond, **kw):
        if cond: a.update(kw)
    rule("RIVER" in types, chance=0.0)
    rule("FOREST" in types, species=sp({"oak": 4, "birch": 1}), chance=1.0, forestness=1.0)
    rule("COLD" in types, species=sp("spruce"))
    rule("WASTELAND" in types, species="...")
    rule("SANDY" in types, species=sp("cactus"), density="noise() * 0.6 + 0.4", chance=0.075)
    rule("SWAMP" in types, species=sp("oakswamp"), chance=0.75)
    rule("SAVANNA" in types, species=sp("acacia"))
    rule("JUNGLE" in types, species=sp({"jungle": 5, "megajungle": 1}), forestness=1.0)
    rule("MUSHROOM" in types, species=sp({"mushroomred": 4, "mushroombrn": 3}), density="noise() * 0.25 + 0.25", chance=0.66)
    rule("CONIFEROUS" in types, species=sp("spruce"), chance={"math": {"ifgt": ["radius", 6, 0.5, 1.0]}})
    rule("SPOOKY" in types, species=sp("darkoak"), density="noise() / 3.0", multipass=True)
    rule(re.fullmatch(r".*mesa.*", name) is not None, chance=0.0)
    rule(re.fullmatch(r".*beach.*", name) is not None, chance=0.0)
    rule(name == "mesa_rock", species=sp("oak"), chance=0.5)
    rule(re.fullmatch(r".*birch.*", name) is not None, species=sp("birch"))
    rule(re.fullmatch(r".*mutated_birch.*", name) is not None, species=sp("tallbirch"))
    rule(re.fullmatch(r".*plains", name) is not None, species=sp({"oak": 24, "apple": 1}))
    rule(re.fullmatch(r".*extreme_hills.*", name) is not None, species=sp({"spruce": 2, "oak": 1}))
    rule(re.fullmatch(r".*redwood_taiga.*", name) is not None, species=sp("megaspruce"))
    entries.append({"select": {"name": rl(name)}, "apply": a})

TRAVERSE = {  # DT Traverse BiomeDataBasePopulator: species, density scale of noise, chance
 "autumnal_woods": ({"rlgen:autumn_yellow": 4, "rlgen:autumn_orange": 5, "oak": 1, "rlgen:autumn_brown": 2, "rlgen:autumn_red": 4}, 0.9, 1.0),
 "autumnal_wooded_hills": ({"rlgen:autumn_yellow": 4, "rlgen:autumn_orange": 5, "oak": 1, "rlgen:autumn_brown": 2, "rlgen:autumn_red": 4}, 0.9, 1.0),
 "temperate_rainforest": ({"rlgen:fir": 1}, None, 1.0),
 "snowy_coniferous_forest": ({"rlgen:fir": 1}, None, 1.0),
 "arid_highland": ({"acacia": 4, "oak": 1, "...": 1}, 0.1, 0.2),
 "badlands": ({"rlgen:autumn_brown": 1, "oak": 1}, 0.03, 0.0625),
 "birch_forested_hills": ({"birch": 1}, None, 1.25),
 "forested_hills": ({"oak": 4, "birch": 2}, 0.9, 1.0),
 "lush_hills": ({"oak": 4, "swamp_oak": 1}, 0.4, 1.0),
 "green_swamp": ({"swamp_oak": 4, "oak": 1}, 0.6, 1.0),
 "meadow": ({"oak": 2, "birch": 1}, 0.01, 0.5),
 "rocky_plains": ({"oak": 2}, 0.01, 0.05),
 "rocky_plateau": ({"oak": 2}, 0.01, 0.5),
 "thicket": ({"oak": 4, "dark_oak": 2}, 1.5, 1.0),
 "woodlands": ({"oak": 1}, 0.6, 0.75),
 "red_desert": ({"...": 1}, 2.0, 0.05),
 "mountainous_desert": ({"...": 1}, 2.0, 0.05),
 "mini_jungle": ({"oak": 1, "rlgen:mini_jungle": 4}, 2.0, 0.85),
 "cliffs": ({"oak": 2, "...": 4}, 2.0, 0.5),
 "glacier": ({"spruce": 2, "...": 8}, 2.0, 0.5),
 "glacier_spikes": ({"spruce": 2, "...": 8}, 2.0, 0.5),
 "desert_shrubland": ({"rlgen:oak_shrub": 3, "...": 8}, 0.8, 0.6),
 "canyon": None, "crag_cliffs": None,
}
for name, v in TRAVERSE.items():
    if v is None:
        entries.append({"select": {"name": "rlgen:" + name}, "apply": {"species": "...", "chance": 0.0}})
        continue
    s, d, c = v
    a = {"species": {"random": s} if list(s) != ["..."] else "...", "chance": min(c, 1.0)}
    a["density"] = "noise()" if d is None else f"noise() * {d}"
    entries.append({"select": {"name": "rlgen:" + name}, "apply": a})
DEFILED = {"forest_tenebra": (0.8, 1.0), "plains_defiled": (0.1, 0.2), "hills_defiled": (0.1, 0.05),
           "ice_plains_defiled": (0.1, 0.4), "swamp_defiled": (0.5, 0.5)}
for name, (d, c) in DEFILED.items():
    entries.append({"select": {"name": "rlgen:" + name}, "apply": {"species": {"random": {"rlgen:tenebra": 1}}, "density": d, "chance": c}})
for name in ("desert_defiled", "forest_vilespine"):
    entries.append({"select": {"name": "rlgen:" + name}, "apply": {"species": "...", "chance": 0.0}})
os.makedirs(f"{RES}/trees/rlgen/world_gen", exist_ok=True)
json.dump(entries, open(f"{RES}/trees/rlgen/world_gen/default.json", "w"), indent=2)
json.dump([{"__comment": "RLCraft: Dynamic Trees replaces all normal trees in the 1.12 biomes.",
            "select": {"name": "rlgen:.*"}, "cancellers": {"type": "tree", "namespace": "minecraft"}}],
          open(f"{RES}/trees/rlgen/world_gen/feature_cancellers.json", "w"), indent=2)
print(len(biomes), "biomes,", len(added), "tags,", len(entries), "tree rules")
