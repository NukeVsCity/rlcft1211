#!/usr/bin/env python3
"""compare112.py - compare a real RLCraft 1.12.2 save with what rlgen generates for the same seed.

Usage:
  python3 compare112.py <old 1.12 save> <new 1.21 world> <out dir> [--box X0 Z0 X1 Z1] [--level-dat FILE]
                        [--inhabited TICKS] [--keep-inhabited] [--top N]

  <old 1.12 save>  folder with the uploaded files: r.X.Z.mca (in it or in region/) and level.dat (in it or its parent).
  <new 1.21 world> world folder (region/ and entities/ inside) or a folder holding the 1.21 r.X.Z.mca files.
                   Make it with gen_compare_world.sh (same seed, same block box).
  --box            block box to compare (default: every chunk present in both).
  --level-dat      the 1.12 level.dat (block/biome ids of modded blocks come from its Forge registry, FML -> Registries;
                   without it vanilla ids are assumed and modded blocks/biomes show up as "?id").
  --inhabited      chunks whose 1.12 InhabitedTime is above this (ticks, default 36000 = 30 min) are flagged as
                   "player area" and left out of the totals unless --keep-inhabited.
Needs Python 3, numpy, Pillow and nbtlib (pip install nbtlib).

What it does
  * Reads 1.12 Anvil chunks (Level.Sections Blocks/Add/Data, Biomes, TileEntities, Entities, TerrainPopulated,
    LightPopulated, InhabitedTime). Block id -> 1.12 name via the level.dat registry (fallback: vanilla ids), then
    name:meta -> 1.21 state via data/rlgen/legacy/blocks.json (the table the mod itself uses for 1.12 blocks).
    Biome id -> name via level.dat minecraft:biomes (fallback: legacy ids from legacy/biome/Biomes.java), then
    rlgen:<name> like world/LegacyBiomeMap.java.
  * Reads the 1.21 chunks (Y 0..255 only: the 1.12 terrain sits at the same Y in rlgen worlds).
  * Chunks are skipped when the 1.12 chunk is missing or not populated (TerrainPopulated=0), or the 1.21 chunk is
    missing or not "full".
  * Blocks are compared by block name (properties ignored) after normalising: deepslate/bedrock -> stone (rlgen turns
    1.12 bedrock into stone and stone below Y8 into deepslate), deepslate_x_ore -> x_ore, cave_air -> air, every log /
    Dynamic Trees branch / big mushroom block -> "wood", every leaves block -> "leaves", rooty soil -> "rooty"
    (tree species are not compared, only where trees are).
  * Every differing block is put in one bucket:
      trees       a log/branch/leaves/rooty block on either side
      structures  a block that is none of the other kinds (planks, cobble, chests, spawners, modded blocks, ...)
      plants      grass, flowers, cactus, reeds, pumpkins, snow layers, mushrooms, Rustic herbs, ...
      ores        ores, granite/diorite/andesite/Quark stones, speleothems, crystals, underground-biome blocks
      liquids     water/lava vs air (springs, lakes, flowing water), or liquid vs terrain above the sea floor
      terrain     solid terrain vs air/ocean water at/above the lower of the two surfaces (terrain shape)
      caves       solid terrain vs air/liquid below both surfaces (caves, ravines, lava in caves)
      surface     terrain vs other terrain (grass vs sand, stone vs gravel, mesa bands, ...)
    "terrain match" = per block air/liquid/solid class equal, with plants and trees counted as air.
    "height" = top solid terrain block per column (no trees, plants, structures).
    "biome match" = 1.12 biome at every 4th block vs the 1.21 biome of that 4x4 cell (rlgen samples there).
  * Writes <out>/summary.txt (also printed), chunks.csv (one row per chunk), top.png (1.12 | 1.21 top view),
    biomes.png (1.12 | 1.21 biomes with legend), heat.png (differing blocks per chunk, grey = skipped,
    blue frame = player area) and height.png (1.21 height minus 1.12 height per column, red = higher in 1.21).
"""
import argparse, collections, csv, gzip, hashlib, io, json, math, os, re, struct, sys, zlib
import numpy as np
import nbtlib
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
RLGEN = os.path.dirname(HERE)
RES = os.path.join(RLGEN, "src/main/resources/data/rlgen")
BIOMES_JAVA = os.path.join(RLGEN, "src/main/java/com/nukevscity/rlgen/legacy/biome/Biomes.java")

# vanilla 1.12.2 block ids 0..252 (255 = structure_block); Forge never moves these.
VANILLA_IDS = """air stone grass dirt cobblestone planks sapling bedrock flowing_water water flowing_lava lava sand gravel
gold_ore iron_ore coal_ore log leaves sponge glass lapis_ore lapis_block dispenser sandstone noteblock bed golden_rail
detector_rail sticky_piston web tallgrass deadbush piston piston_head wool piston_extension yellow_flower red_flower
brown_mushroom red_mushroom gold_block iron_block double_stone_slab stone_slab brick_block tnt bookshelf mossy_cobblestone
obsidian torch fire mob_spawner oak_stairs chest redstone_wire diamond_ore diamond_block crafting_table wheat farmland
furnace lit_furnace standing_sign wooden_door ladder rail stone_stairs wall_sign lever stone_pressure_plate iron_door
wooden_pressure_plate redstone_ore lit_redstone_ore unlit_redstone_torch redstone_torch stone_button snow_layer ice snow
cactus clay reeds jukebox fence pumpkin netherrack soul_sand glowstone portal lit_pumpkin cake unpowered_repeater
powered_repeater stained_glass trapdoor monster_egg stonebrick brown_mushroom_block red_mushroom_block iron_bars glass_pane
melon_block pumpkin_stem melon_stem vine fence_gate brick_stairs stone_brick_stairs mycelium waterlily nether_brick
nether_brick_fence nether_brick_stairs nether_wart enchanting_table brewing_stand cauldron end_portal end_portal_frame
end_stone dragon_egg redstone_lamp lit_redstone_lamp double_wooden_slab wooden_slab cocoa sandstone_stairs emerald_ore
ender_chest tripwire_hook tripwire emerald_block spruce_stairs birch_stairs jungle_stairs command_block beacon
cobblestone_wall flower_pot carrots potatoes wooden_button skull anvil trapped_chest light_weighted_pressure_plate
heavy_weighted_pressure_plate unpowered_comparator powered_comparator daylight_detector redstone_block quartz_ore hopper
quartz_block quartz_stairs activator_rail dropper stained_hardened_clay stained_glass_pane leaves2 log2 acacia_stairs
dark_oak_stairs slime barrier iron_trapdoor prismarine sea_lantern hay_block carpet hardened_clay coal_block packed_ice
double_plant standing_banner wall_banner daylight_detector_inverted red_sandstone red_sandstone_stairs double_stone_slab2
stone_slab2 spruce_fence_gate birch_fence_gate jungle_fence_gate dark_oak_fence_gate acacia_fence_gate spruce_fence
birch_fence jungle_fence dark_oak_fence acacia_fence spruce_door birch_door jungle_door acacia_door dark_oak_door end_rod
chorus_plant chorus_flower purpur_block purpur_pillar purpur_stairs purpur_double_slab purpur_slab end_bricks beetroots
grass_path end_gateway repeating_command_block chain_command_block frosted_ice magma nether_wart_block red_nether_brick
bone_block structure_void observer white_shulker_box orange_shulker_box magenta_shulker_box light_blue_shulker_box
yellow_shulker_box lime_shulker_box pink_shulker_box gray_shulker_box light_gray_shulker_box cyan_shulker_box
purple_shulker_box blue_shulker_box brown_shulker_box green_shulker_box red_shulker_box black_shulker_box
white_glazed_terracotta orange_glazed_terracotta magenta_glazed_terracotta light_blue_glazed_terracotta
yellow_glazed_terracotta lime_glazed_terracotta pink_glazed_terracotta gray_glazed_terracotta
light_gray_glazed_terracotta cyan_glazed_terracotta purple_glazed_terracotta blue_glazed_terracotta
brown_glazed_terracotta green_glazed_terracotta red_glazed_terracotta black_glazed_terracotta concrete concrete_powder""".split()

# ---------------------------------------------------------------- block kinds
AIR, LIQUID, TERRAIN, ORE, PLANT, WOOD, LEAVES, STRUCT = range(8)
KIND_NAMES = ["air", "liquid", "terrain", "ore", "plant", "wood", "leaves", "structure"]
BUCKETS = ["terrain", "caves", "surface", "ores", "liquids", "plants", "trees", "structures"]
COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
           "blue", "brown", "green", "red", "black"]
TERRAIN_NAMES = set("""stone dirt coarse_dirt podzol grass_block mycelium sand red_sand gravel sandstone red_sandstone clay
terracotta ice packed_ice blue_ice snow_block netherrack soul_sand soul_soil end_stone dirt_path farmland
grass_defiled dirt_defiled sand_defiled sandstone_defiled stone_defiled gravel_defiled red_rock blue_rock
frozen_stone frozen_dirt frozen_grass frozen_gravel chared_stone chared_dirt chared_grass chared_gravel""".split()) | {
    c + "_terracotta" for c in COLOURS}
LIQUID_NAMES = {"water", "lava", "bubble_column", "ooze", "poison", "acid"}
ORE_NAMES = set("""granite diorite andesite marble limestone jasper slate glowcelium glowshroom icy_stone fire_stone
monster_box obsidian magma_block roots roots_black_flower roots_blue_flower roots_white_flower amethyst_block
budding_amethyst calcite smooth_basalt tuff infested_stone""".split())
PLANT_NAMES = set("""short_grass tall_grass fern large_fern dead_bush dandelion poppy blue_orchid allium azure_bluet
red_tulip orange_tulip white_tulip pink_tulip oxeye_daisy cornflower lily_of_the_valley sunflower lilac rose_bush peony
sugar_cane cactus pumpkin melon lily_pad vine brown_mushroom red_mushroom snow cocoa seagrass tall_seagrass kelp
kelp_plant sweet_berry_bush aloe_vera blood_orchid chamomile cloudsbluff cohosh core_root ginseng horsetail marsh_mallow
mooncap_mushroom wind_thistle wildberry_bush vilespine blastem scuronotte fire_lily frost_lily cold_grass dead_grass
reeds_block frozen_splinters""".split())
WOOD_NAMES = {"mushroom_stem", "brown_mushroom_block", "red_mushroom_block"}


def kind_of(name):
    """name: 1.21 block id ('minecraft:stone') or a raw unmapped 1.12 id ('?mod:block')."""
    base = name.split(":", 1)[-1]
    if base in ("air", "cave_air", "void_air"):
        return AIR
    if base.startswith("deepslate") or base == "bedrock":
        base = "stone" if base in ("deepslate", "bedrock") else base[len("deepslate_"):]
    if base in LIQUID_NAMES or base.startswith("flowing_"):
        return LIQUID
    if "leaves" in base:
        return LEAVES
    if (base in WOOD_NAMES or "branch" in base or "rooty" in base or base.endswith("_log") or base.endswith("_wood")
            or base.endswith("_root") or base in ("log", "log2") or base.startswith("dt_trunk")):
        return WOOD
    if base.startswith("potted_"):
        return STRUCT
    if base in TERRAIN_NAMES:
        return TERRAIN
    if base.endswith("_ore") or base.endswith("speleothem") or base in ORE_NAMES or base.startswith("crystal_") \
            or base.endswith("crystal") or base.startswith("loose_rock"):
        return ORE if not base.startswith("loose_rock") else PLANT
    if base in PLANT_NAMES or base.endswith("_sapling") or base.endswith("_tulip") or base.endswith("_flower") \
            or (base.endswith("_mushroom") and "block" not in base) or base.endswith("_bush") or base.endswith("lily"):
        return PLANT
    return STRUCT


def eq_key(name):
    """What has to be equal for two blocks to count as the same."""
    k = kind_of(name)
    base = name.split(":", 1)[-1]
    if k == AIR:
        return "air"
    if k == LEAVES:
        return "#leaves"
    if k == WOOD:
        return "#rooty" if "rooty" in base else "#wood"
    if base in ("deepslate", "bedrock"):
        return "minecraft:stone"
    if base.startswith("deepslate_"):
        return "minecraft:" + base[len("deepslate_"):]
    if base.startswith("flowing_"):
        return "minecraft:" + base[len("flowing_"):]
    return name


class Names:
    """Interns block names; per name kind and comparison key as numpy lookup tables."""

    def __init__(self):
        self.idx = {}
        self.names = []
        self.kinds = []
        self.keys = []
        self.keyidx = {}

    def get(self, name):
        i = self.idx.get(name)
        if i is None:
            i = self.idx[name] = len(self.names)
            self.names.append(name)
            self.kinds.append(kind_of(name))
            self.keys.append(self.keyidx.setdefault(eq_key(name), len(self.keyidx)))
        return i

    def tables(self):
        return np.array(self.kinds, np.uint8), np.array(self.keys, np.int32)


NAMES = Names()
NAMES.get("minecraft:air")  # index 0
NAMES.get("minecraft:snow")


def base_name(state):
    return state.split("[", 1)[0]


# ---------------------------------------------------------------- 1.12 id tables
def load_level_dat(path):
    """-> (blocks {id: name}, biomes {id: name}, info dict)"""
    root = nbtlib.load(path)
    data = root.get("Data", {})
    info = {"seed": int(data["RandomSeed"]) if "RandomSeed" in data else None,
            "generator": str(data.get("generatorName", "")), "name": str(data.get("LevelName", ""))}
    blocks, biomes = {}, {}
    fml = root.get("FML")
    if fml is not None:
        regs = fml.get("Registries")
        if regs is not None:
            for rname, target in (("minecraft:blocks", blocks), ("minecraft:biomes", biomes)):
                r = regs.get(rname)
                if r is not None:
                    for e in r.get("ids", []):
                        target[int(e["V"])] = str(e["K"])
        for e in fml.get("ItemData", []):  # pre-1.12 Forge format: "\x01name" = block
            k = str(e["K"])
            if k.startswith("\x01"):
                blocks[int(e["V"])] = k[1:]
    return blocks, biomes, info


def legacy_biome_ids():
    """Fallback 1.12 biome ids from Biomes.java (vanilla ids + the ids rlgen uses for mod biomes)."""
    out = {}
    src = open(BIOMES_JAVA).read()
    for m in re.finditer(r'register(?:Vanilla|Mod)?Biome\(\s*(\d+),\s*"([^"]+)"', src):
        n = m.group(2)
        out[int(m.group(1))] = n if ":" in n else "minecraft:" + n
    return out


SPECIAL_BIOMES = {"minecraft:hell": "minecraft:nether_wastes", "minecraft:sky": "minecraft:the_end",
                  "minecraft:void": "minecraft:the_void", "iceandfire:glacier": "rlgen:iceandfire_glacier"}


def modern_biome(name112):
    if name112 in SPECIAL_BIOMES:
        return SPECIAL_BIOMES[name112]
    return "rlgen:" + name112.split(":", 1)[-1]


class Old112:
    def __init__(self, level_dat):
        legacy = json.load(open(os.path.join(RES, "legacy/blocks.json")))
        reg_blocks, reg_biomes, self.info = load_level_dat(level_dat) if level_dat else ({}, {}, {})
        self.has_registry = bool(reg_blocks)
        ids = {i: "minecraft:" + n for i, n in enumerate(VANILLA_IDS)}
        ids[255] = "minecraft:structure_block"
        ids.update(reg_blocks)
        self.block_ids = ids
        # (id << 4 | meta) -> interned 1.21 name
        self.lut = np.zeros(4096 * 16, np.int32)
        self.unmapped = collections.Counter()
        self.raw_unmapped = set()
        for i, n in ids.items():
            if i >= 4096:
                continue
            for meta in range(16):
                s = legacy.get(f"{n}:{meta}") or legacy.get(f"{n}:0")
                if s is None:
                    nm = "?" + n  # kind from the raw name (dynamictrees:oakbranch -> wood, ...)
                    self.raw_unmapped.add(n)
                else:
                    nm = base_name(s)
                    if ("rooty" in n or "branch" in n) and kind_of(nm) not in (WOOD, LEAVES):
                        nm = "?" + n  # DT rooty dirt maps to plain dirt for structures; here it is part of a tree
                self.lut[i << 4 | meta] = NAMES.get(nm)
        for i in range(4096):
            if i not in ids:
                for meta in range(16):
                    self.lut[i << 4 | meta] = NAMES.get(f"?id{i}")
        bio = legacy_biome_ids() if not reg_biomes else {}
        bio.update(reg_biomes)
        self.biome_names = {i: modern_biome(n) for i, n in bio.items()}

    def biome(self, i):
        return self.biome_names.get(i, f"?biome{i}")


# ---------------------------------------------------------------- region files
class Region:
    def __init__(self, folder):
        self.folder = folder
        self.cache = {}

    def _file(self, rx, rz):
        k = (rx, rz)
        if k not in self.cache:
            p = os.path.join(self.folder, f"r.{rx}.{rz}.mca")
            self.cache[k] = open(p, "rb").read() if os.path.exists(p) else None
        return self.cache[k]

    def chunk(self, cx, cz):
        f = self._file(cx >> 5, cz >> 5)
        if not f:
            return None
        i = 4 * ((cx & 31) + (cz & 31) * 32)
        off = int.from_bytes(f[i:i + 3], "big") * 4096
        if off == 0 or off + 5 > len(f):
            return None
        ln = struct.unpack(">I", f[off:off + 4])[0]
        ctype = f[off + 4]
        raw = f[off + 5:off + 4 + ln]
        if ctype & 128:
            p = os.path.join(self.folder, f"c.{cx}.{cz}.mcc")
            raw = open(p, "rb").read()
            ctype &= 127
        data = {1: gzip.decompress, 2: zlib.decompress, 3: lambda b: b}[ctype](raw)
        return nbtlib.File.parse(io.BytesIO(data))

    def chunks(self):
        out = []
        for fn in os.listdir(self.folder):
            m = re.fullmatch(r"r\.(-?\d+)\.(-?\d+)\.mca", fn)
            if not m:
                continue
            rx, rz = int(m.group(1)), int(m.group(2))
            f = self._file(rx, rz)
            for i in range(1024):
                if f and len(f) >= 4096 and int.from_bytes(f[4 * i:4 * i + 3], "big"):
                    out.append((rx * 32 + (i & 31), rz * 32 + (i >> 5)))
        return out


def find_region(path):
    for p in (os.path.join(path, "region"), path):
        if os.path.isdir(p) and any(f.endswith(".mca") for f in os.listdir(p)):
            return p
    sys.exit(f"no r.X.Z.mca files in {path} or {path}/region")


def nbt_id(t):
    s = str(t.get("id", "?"))
    s = s if ":" in s else "minecraft:" + s.lower()
    return {"minecraft:mob_spawner": "minecraft:spawner", "minecraft:villager_golem": "minecraft:iron_golem"}.get(s, s)


# ---------------------------------------------------------------- chunk readers -> arrays [y][z][x]
def nibbles(arr):
    d = np.asarray(arr).view(np.uint8)
    out = np.empty(d.size * 2, np.uint8)
    out[0::2] = d & 15
    out[1::2] = d >> 4
    return out


def read112(old, nbt):
    lv = nbt["Level"] if "Level" in nbt else nbt
    blocks = np.zeros((256, 16, 16), np.int32)  # interned names, 0 = air
    for sec in lv.get("Sections", []):
        y = int(sec["Y"])
        if not 0 <= y < 16:
            continue
        ids = np.asarray(sec["Blocks"]).view(np.uint8).astype(np.int32)
        if "Add" in sec:
            ids |= nibbles(sec["Add"]).astype(np.int32) << 8
        meta = nibbles(sec["Data"]).astype(np.int32)
        blocks[y * 16:y * 16 + 16] = old.lut[(ids << 4) | meta].reshape(16, 16, 16)
    bio = np.asarray(lv["Biomes"]).view(np.uint8).reshape(16, 16) if "Biomes" in lv else np.full((16, 16), 255, np.uint8)
    return {
        "blocks": blocks,
        "biomes": np.vectorize(lambda i: old.biome(int(i)), otypes=[object])(bio),  # [z][x]
        "populated": bool(int(lv.get("TerrainPopulated", 0))),
        "lit": bool(int(lv.get("LightPopulated", 0))),
        "inhabited": int(lv.get("InhabitedTime", 0)),
        "tiles": collections.Counter(nbt_id(t) for t in lv.get("TileEntities", [])),
        "entities": collections.Counter(nbt_id(e) for e in lv.get("Entities", [])),
    }


def unpack(longs, bits, n):
    v = np.asarray(longs).astype(np.int64).view(np.uint64)
    per = 64 // bits
    i = np.arange(n)
    return ((v[i // per] >> ((i % per) * bits).astype(np.uint64)) & np.uint64((1 << bits) - 1)).astype(np.int32)


def read121(nbt, ents=None):
    blocks = np.zeros((256, 16, 16), np.int32)
    biomes = np.full((4, 4), "?", object)  # quart cell at Y 64..67, [z][x]
    for sec in nbt.get("sections", []):
        y = int(sec["Y"])
        if not 0 <= y < 16:
            continue
        bs = sec.get("block_states")
        if bs is not None:
            pal = np.array([NAMES.get(str(p["Name"])) for p in bs["palette"]], np.int32)
            if "data" in bs and len(pal) > 1:
                idx = unpack(bs["data"], max(4, math.ceil(math.log2(len(pal)))), 4096)
                blocks[y * 16:y * 16 + 16] = pal[idx].reshape(16, 16, 16)
            else:
                blocks[y * 16:y * 16 + 16] = pal[0]
        if y == 4 and "biomes" in sec:
            pal = [str(p) for p in sec["biomes"]["palette"]]
            if "data" in sec["biomes"] and len(pal) > 1:
                idx = unpack(sec["biomes"]["data"], math.ceil(math.log2(len(pal))), 64)
                biomes = np.array(pal, object)[idx[:16]].reshape(4, 4)
            else:
                biomes = np.full((4, 4), pal[0], object)
    return {
        "blocks": blocks, "biomes": biomes,
        "full": str(nbt.get("Status", "")) in ("minecraft:full", "full"),
        "tiles": collections.Counter(nbt_id(t) for t in nbt.get("block_entities", [])),
        "entities": collections.Counter(nbt_id(e) for e in (ents or [])),
    }


# ---------------------------------------------------------------- comparison
def heights(kind):
    """top solid terrain (terrain or ore) per column, -1 if none. kind: [y][z][x]"""
    solid = (kind == TERRAIN) | (kind == ORE)
    any_ = solid.any(axis=0)
    top = 255 - np.argmax(solid[::-1], axis=0)
    return np.where(any_, top, -1)


def compare(a, b, kinds, keys):
    ka, kb = kinds[a], kinds[b]
    diff = keys[a] != keys[b]
    ha, hb = heights(ka), heights(kb)
    low = np.minimum(ha, hb)[None, :, :]
    y = np.arange(256)[:, None, None]
    # ocean / river columns: water at sea level (Y 62) on that side
    oa = (ka[62] == LIQUID)[None]
    ob = (kb[62] == LIQUID)[None]
    tree = (ka == WOOD) | (ka == LEAVES) | (kb == WOOD) | (kb == LEAVES)
    struct_ = (ka == STRUCT) | (kb == STRUCT)
    plant = (ka == PLANT) | (kb == PLANT)
    ore = (ka == ORE) | (kb == ORE)
    terr_a, terr_b = ka == TERRAIN, kb == TERRAIN
    open_a, open_b = (ka == AIR) | (ka == LIQUID), (kb == AIR) | (kb == LIQUID)
    shape = (terr_a & open_b) | (terr_b & open_a)
    liq_vs_terr = ((ka == LIQUID) & terr_b) | ((kb == LIQUID) & terr_a)
    sea_floor = liq_vs_terr & (y <= 62) & np.where(ka == LIQUID, oa, ob)
    bucket = np.select(
        [tree, struct_, plant, ore,
         shape & (~liq_vs_terr | sea_floor) & (y >= low),  # terrain shape (incl. ocean floor)
         shape & ~(liq_vs_terr & ~sea_floor & (y >= low)),  # caves (incl. lava/water filled caves)
         terr_a & terr_b],
        [6, 7, 5, 3, 0, 1, 2], default=4)  # default: liquids
    counts = np.bincount(bucket[diff], minlength=8)
    pair_code, pair_n = np.unique((bucket[diff].astype(np.int64) << 40) | (a[diff].astype(np.int64) << 20) | b[diff],
                                  return_counts=True)
    ca = np.where((ka == PLANT) | (ka == WOOD) | (ka == LEAVES) | (ka == AIR), 0, np.where(ka == LIQUID, 1, 2))
    cb = np.where((kb == PLANT) | (kb == WOOD) | (kb == LEAVES) | (kb == AIR), 0, np.where(kb == LIQUID, 1, 2))
    valid = (ha >= 0) & (hb >= 0)
    return {"counts": counts, "diff": int(diff.sum()), "terrain_eq": int((ca == cb).sum()), "ha": ha, "hb": hb,
            "hdiff": np.where(valid, hb - ha, 0), "hvalid": valid,
            "pairs": zip(pair_code.tolist(), pair_n.tolist())}


def short(i):
    n = NAMES.names[i]
    return n[len("minecraft:"):] if n.startswith("minecraft:") else n


# ---------------------------------------------------------------- images
FIX = {"minecraft:grass_block": (90, 160, 60), "minecraft:water": (40, 70, 200), "minecraft:lava": (230, 90, 10),
       "minecraft:sand": (220, 210, 150), "minecraft:red_sand": (200, 110, 40), "minecraft:stone": (140, 140, 140),
       "minecraft:gravel": (130, 120, 115), "minecraft:dirt": (130, 95, 60), "minecraft:coarse_dirt": (120, 85, 55),
       "minecraft:podzol": (100, 70, 30), "minecraft:snow": (250, 250, 250), "minecraft:snow_block": (245, 245, 250),
       "minecraft:ice": (160, 190, 250), "minecraft:packed_ice": (140, 170, 240), "minecraft:sandstone": (215, 200, 140),
       "minecraft:terracotta": (150, 90, 65), "minecraft:clay": (160, 165, 180), "minecraft:mycelium": (120, 100, 120),
       "minecraft:cobblestone": (110, 110, 110), "minecraft:mossy_cobblestone": (80, 120, 80),
       "minecraft:oak_planks": (180, 140, 80), "minecraft:dirt_path": (150, 120, 60), "minecraft:chest": (255, 0, 255),
       "minecraft:cactus": (20, 120, 30), "minecraft:sugar_cane": (140, 200, 90), "minecraft:pumpkin": (230, 140, 20)}


def colour(name):
    c = FIX.get(name)
    if c is not None:
        return c
    h = hashlib.md5(name.encode()).digest()
    k = kind_of(name)
    if k == LEAVES:
        return (30, 90 + h[0] % 60, 30)
    if k == WOOD:
        return (90 + h[0] % 40, 60 + h[1] % 30, 30)
    if k == TERRAIN and name.endswith("_terracotta"):
        return (150 + h[0] % 60, 80 + h[1] % 60, 50 + h[2] % 60)
    return (h[0], h[1], h[2])


def topview(blocks, kinds):
    """-> RGB [z][x] of the highest non-air non-plant block, shaded by height"""
    k = kinds[blocks]
    vis = (k != AIR) & ((k != PLANT) | (blocks == NAMES.get("minecraft:snow")))
    top = 255 - np.argmax(vis[::-1], axis=0)
    names = np.take_along_axis(blocks, top[None], axis=0)[0]
    img = np.zeros((16, 16, 3), np.uint8)
    for z in range(16):
        for x in range(16):
            c = colour(NAMES.names[names[z, x]]) if vis[:, z, x].any() else (0, 0, 0)
            f = 0.55 + 0.45 * max(0.0, min(1.0, (top[z, x] - 40) / 100))
            img[z, x] = [int(v * f) for v in c]
    return img


def biome_colour(name):
    h = hashlib.md5(name.encode()).digest()
    return (60 + h[0] % 196, 60 + h[1] % 196, 60 + h[2] % 196)


# ---------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("old")
    ap.add_argument("new")
    ap.add_argument("out")
    ap.add_argument("--box", nargs=4, type=int, metavar=("X0", "Z0", "X1", "Z1"))
    ap.add_argument("--level-dat")
    ap.add_argument("--inhabited", type=int, default=36000)
    ap.add_argument("--keep-inhabited", action="store_true")
    ap.add_argument("--top", type=int, default=12)
    ap.add_argument("--scale", type=int, default=0, help="pixels per block in the PNGs (default: automatic)")
    args = ap.parse_args()
    os.makedirs(args.out, exist_ok=True)

    oldreg = Region(find_region(args.old))
    newreg = Region(find_region(args.new))
    newroot = os.path.dirname(newreg.folder) if os.path.basename(newreg.folder) == "region" else newreg.folder
    entreg = Region(os.path.join(newroot, "entities")) if os.path.isdir(os.path.join(newroot, "entities")) else None
    lvl = args.level_dat
    if lvl is None:
        for p in (os.path.join(args.old, "level.dat"), os.path.join(os.path.dirname(oldreg.folder), "level.dat")):
            if os.path.exists(p):
                lvl = p
                break
    old = Old112(lvl)

    if args.box:
        x0, z0, x1, z1 = args.box
        cxs = range(min(x0, x1) >> 4, (max(x0, x1) >> 4) + 1)
        czs = range(min(z0, z1) >> 4, (max(z0, z1) >> 4) + 1)
        coords = [(cx, cz) for cz in czs for cx in cxs]
    else:
        newset = set(newreg.chunks())
        coords = sorted(c for c in oldreg.chunks() if c in newset)
        if not coords:
            sys.exit("no chunk is in both worlds")
    cx0, cx1 = min(c[0] for c in coords), max(c[0] for c in coords)
    cz0, cz1 = min(c[1] for c in coords), max(c[1] for c in coords)
    W, H = (cx1 - cx0 + 1) * 16, (cz1 - cz0 + 1) * 16

    top_a = np.zeros((H, W, 3), np.uint8)
    top_b = np.zeros((H, W, 3), np.uint8)
    bio_a = np.full((H, W), "", object)
    bio_b = np.full((H, W), "", object)
    hmap = np.full((H, W), np.nan)
    rows, skipped = [], collections.Counter()
    tot = np.zeros(8, np.int64)
    tot_diff = tot_blocks = tot_terr = bio_eq = bio_n = h_eq = h_n = 0
    h_abs = 0.0
    bio_pairs = collections.Counter()
    blk_pairs = collections.Counter()
    tiles_a, tiles_b, ents_a, ents_b = (collections.Counter() for _ in range(4))
    ent_chunks = {}
    if entreg:
        for (cx, cz) in coords:
            e = entreg.chunk(cx, cz)
            if e is not None:
                ent_chunks[(cx, cz)] = e.get("Entities", [])

    for n, (cx, cz) in enumerate(coords):
        px, pz = (cx - cx0) * 16, (cz - cz0) * 16
        row = {"cx": cx, "cz": cz, "x": cx * 16 + 8, "z": cz * 16 + 8, "status": "ok"}
        rows.append(row)
        on, nn = oldreg.chunk(cx, cz), newreg.chunk(cx, cz)
        if on is None:
            row["status"] = "no 1.12 chunk"
        elif nn is None:
            row["status"] = "no 1.21 chunk"
        if row["status"] != "ok":
            skipped[row["status"]] += 1
            continue
        A = read112(old, on)
        B = read121(nn, ent_chunks.get((cx, cz)))
        kinds, keys = NAMES.tables()
        top_a[pz:pz + 16, px:px + 16] = topview(A["blocks"], kinds)
        top_b[pz:pz + 16, px:px + 16] = topview(B["blocks"], kinds)
        bio_a[pz:pz + 16, px:px + 16] = A["biomes"]
        bio_b[pz:pz + 16, px:px + 16] = np.repeat(np.repeat(B["biomes"], 4, 0), 4, 1)
        row["inhabited"] = A["inhabited"]
        if not A["populated"]:
            row["status"] = "1.12 not populated"
        elif not B["full"]:
            row["status"] = "1.21 not full"
        elif A["inhabited"] > args.inhabited:
            row["status"] = "player area"
        if row["status"] not in ("ok", "player area") or (row["status"] == "player area" and not args.keep_inhabited):
            skipped[row["status"]] += 1
            if row["status"] != "player area":
                continue
        c = compare(A["blocks"], B["blocks"], kinds, keys)
        sb = A["biomes"][::4, ::4]
        beq = int((sb == B["biomes"]).sum())
        row.update({"diff": c["diff"], "terrain_match": round(100 * c["terrain_eq"] / 65536, 2),
                    "biome_match": round(100 * beq / 16, 1),
                    "height_mean_abs": round(float(np.abs(c["hdiff"][c["hvalid"]]).mean()) if c["hvalid"].any() else 0, 2),
                    "tiles_112": sum(A["tiles"].values()), "tiles_121": sum(B["tiles"].values())})
        for i, bk in enumerate(BUCKETS):
            row[bk] = int(c["counts"][i])
        hmap[pz:pz + 16, px:px + 16] = np.where(c["hvalid"], c["hdiff"], np.nan)
        if row["status"] == "player area" and not args.keep_inhabited:
            continue
        tot += c["counts"]
        for code, k in c["pairs"]:
            blk_pairs[code] += k
        tot_diff += c["diff"]
        tot_blocks += 65536
        tot_terr += c["terrain_eq"]
        bio_eq += beq
        bio_n += 16
        for p, q in zip(sb.ravel(), B["biomes"].ravel()):
            if p != q:
                bio_pairs[(p, q)] += 1
        v = c["hvalid"]
        h_n += int(v.sum())
        h_eq += int((c["hdiff"][v] == 0).sum())
        h_abs += float(np.abs(c["hdiff"][v]).sum())
        tiles_a.update(A["tiles"])
        tiles_b.update(B["tiles"])
        ents_a.update(A["entities"])
        ents_b.update(B["entities"])
        if (n + 1) % 100 == 0:
            print(f"  {n + 1}/{len(coords)} chunks", file=sys.stderr)

    # ---- text summary
    L = []
    cmp_rows = [r for r in rows if "diff" in r and (r["status"] == "ok" or args.keep_inhabited)]
    info = old.info
    L.append(f"1.12: {oldreg.folder}" + (f"  (level.dat: seed {info.get('seed')}, generator '{info.get('generator')}')"
                                         if info else "  (no level.dat: vanilla ids assumed)"))
    L.append(f"1.21: {newreg.folder}")
    L.append(f"area: chunks {cx0},{cz0} .. {cx1},{cz1} = blocks {cx0 * 16},{cz0 * 16} .. {cx1 * 16 + 15},{cz1 * 16 + 15}")
    L.append(f"compared {len(cmp_rows)} chunks" + "".join(f", {v} {k}" for k, v in sorted(skipped.items())))
    if old.raw_unmapped and old.has_registry:
        mods = collections.Counter(n.split(":")[0] for n in old.raw_unmapped)
        L.append("1.12 blocks with no 1.21 mapping (registry has them): "
                 + ", ".join(f"{m} {k}" for m, k in mods.most_common(8)))
    if cmp_rows:
        L.append(f"biome match   {100 * bio_eq / bio_n:6.2f} %  (1.12 biome at every 4th block vs the 1.21 4x4 cell)")
        L.append(f"terrain match {100 * tot_terr / tot_blocks:6.2f} %  (air/liquid/solid per block, Y 0..255)")
        L.append(f"height        {100 * h_eq / max(1, h_n):6.2f} % of columns same top terrain block, mean |diff| "
                 f"{h_abs / max(1, h_n):.2f} blocks")
        L.append(f"blocks differing {tot_diff} of {tot_blocks} ({100 * tot_diff / tot_blocks:.2f} %):")
        for i, bk in enumerate(BUCKETS):
            top3 = sorted(((k, code) for code, k in blk_pairs.items() if code >> 40 == i), reverse=True)[:3]
            L.append(f"  {bk:<11}{tot[i]:>10}  ({tot[i] / len(cmp_rows):.0f} per chunk)  "
                     + ", ".join(f"{short(code >> 20 & 0xFFFFF)}->{short(code & 0xFFFFF)} {k}" for k, code in top3))
        if bio_pairs:
            L.append("most common biome differences (1.12 -> 1.21, 4x4 cells):")
            for (p, q), k in bio_pairs.most_common(8):
                L.append(f"  {k:>6}  {p} -> {q}")
        keys_t = sorted(set(tiles_a) | set(tiles_b), key=lambda k: -(tiles_a[k] + tiles_b[k]))[:10]
        if keys_t:
            L.append("block entities 1.12 / 1.21: " + ", ".join(f"{k.split(':')[-1]} {tiles_a[k]}/{tiles_b[k]}" for k in keys_t))
        keys_e = sorted(set(ents_a) | set(ents_b), key=lambda k: -(ents_a[k] + ents_b[k]))[:10]
        if keys_e:
            L.append("entities 1.12 / 1.21: " + ", ".join(f"{k.split(':')[-1]} {ents_a[k]}/{ents_b[k]}" for k in keys_e))
        L.append(f"most different chunks (block centre x z, differing blocks, biggest buckets):")
        for r in sorted(cmp_rows, key=lambda r: -r["diff"])[:args.top]:
            big = sorted(BUCKETS, key=lambda b: -r[b])[:3]
            L.append(f"  chunk {r['cx']:>5},{r['cz']:<5} at {r['x']:>6} {r['z']:<6} {r['diff']:>6}  "
                     + ", ".join(f"{b} {r[b]}" for b in big if r[b])
                     + f"  biome {r['biome_match']}%  height |d| {r['height_mean_abs']}")
    pa = [r for r in rows if r["status"] == "player area"]
    if pa:
        L.append(f"player areas (InhabitedTime > {args.inhabited}, {'kept' if args.keep_inhabited else 'left out'}): "
                 + " ".join(f"{r['cx']},{r['cz']}" for r in pa[:30]) + (" ..." if len(pa) > 30 else ""))
    text = "\n".join(L)
    print(text)
    open(os.path.join(args.out, "summary.txt"), "w").write(text + "\n")
    fields = ["cx", "cz", "x", "z", "status", "inhabited", "diff", "terrain_match", "biome_match", "height_mean_abs"] \
        + BUCKETS + ["tiles_112", "tiles_121"]
    with open(os.path.join(args.out, "chunks.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fields, extrasaction="ignore")
        w.writeheader()
        w.writerows(rows)

    # ---- images
    sc = args.scale or max(1, min(4, 1024 // max(W, H)))

    def big(a):
        return np.repeat(np.repeat(a, sc, 0), sc, 1)

    def pair(a, b, title_a, title_b, legend=None):
        gap = 8
        img = Image.new("RGB", (2 * W * sc + gap, H * sc + 16 + (len(legend) * 12 + 4 if legend else 0)), (32, 32, 32))
        img.paste(Image.fromarray(big(a)), (0, 16))
        img.paste(Image.fromarray(big(b)), (W * sc + gap, 16))
        d = ImageDraw.Draw(img)
        d.text((2, 2), title_a, fill=(255, 255, 255))
        d.text((W * sc + gap + 2, 2), title_b, fill=(255, 255, 255))
        if legend:
            for i, (nm, col) in enumerate(legend):
                y0 = H * sc + 18 + i * 12
                d.rectangle([2, y0, 12, y0 + 9], fill=col)
                d.text((16, y0 - 1), nm, fill=(255, 255, 255))
        return img

    pair(top_a, top_b, "1.12 save", "rlgen 1.21").save(os.path.join(args.out, "top.png"))
    allb = collections.Counter(bio_a.ravel()) + collections.Counter(bio_b.ravel())
    allb.pop("", None)
    lut = {nm: biome_colour(nm) for nm in allb}
    lut[""] = (0, 0, 0)

    def bimg(m):
        out = np.zeros((H, W, 3), np.uint8)
        for nm, col in lut.items():
            out[m == nm] = col
        return out

    legend = [(f"{nm}  ({k})", lut[nm]) for nm, k in allb.most_common(30)]
    pair(bimg(bio_a), bimg(bio_b), "1.12 biomes", "rlgen 1.21 biomes", legend).save(os.path.join(args.out, "biomes.png"))

    # heat map: one cell per chunk
    cw = max(8, sc * 4)
    nx, nz = cx1 - cx0 + 1, cz1 - cz0 + 1
    heat = Image.new("RGB", (max(nx * cw, 420), nz * cw + 30), (32, 32, 32))
    d = ImageDraw.Draw(heat)
    vals = [r["diff"] for r in cmp_rows]
    vmax = max(1.0, float(np.percentile(vals, 95))) if vals else 1.0
    for r in rows:
        x, y = (r["cx"] - cx0) * cw, (r["cz"] - cz0) * cw + 30
        if "diff" in r:
            t = min(1.0, r["diff"] / vmax)
            col = (int(255 * min(1, 2 * t)), int(255 * min(1, 2 * (1 - t))), 0)
        else:
            col = (90, 90, 90) if r["status"] != "no 1.12 chunk" else (20, 20, 20)
        d.rectangle([x, y, x + cw - 1, y + cw - 1], fill=col)
        if r["status"] == "player area":
            d.rectangle([x, y, x + cw - 1, y + cw - 1], outline=(0, 120, 255))
    d.text((2, 2), f"differing blocks per chunk: green 0 .. red >= {vmax:.0f}", fill=(255, 255, 255))
    d.text((2, 15), f"top left = chunk {cx0},{cz0}; grey = skipped, blue frame = player area", fill=(255, 255, 255))
    heat.save(os.path.join(args.out, "heat.png"))

    # height difference per column
    hm = np.zeros((H, W, 3), np.uint8)
    t = np.clip(np.nan_to_num(hmap) / 16.0, -1, 1)
    hm[..., 0] = np.where(t > 0, 255, 255 * (1 + t)).astype(np.uint8)
    hm[..., 1] = (255 * (1 - np.abs(t))).astype(np.uint8)
    hm[..., 2] = np.where(t < 0, 255, 255 * (1 - t)).astype(np.uint8)
    hm[np.isnan(hmap)] = (60, 60, 60)
    hm[::16, :] = hm[::16, :] // 2 + 40  # chunk grid
    hm[:, ::16] = hm[:, ::16] // 2 + 40
    himg = Image.new("RGB", (W * sc, H * sc + 16), (32, 32, 32))
    himg.paste(Image.fromarray(big(hm)), (0, 16))
    ImageDraw.Draw(himg).text((2, 2), "height 1.21 - 1.12: blue -16 .. white 0 .. red +16", fill=(255, 255, 255))
    himg.save(os.path.join(args.out, "height.png"))
    print(f"wrote {args.out}/summary.txt chunks.csv top.png biomes.png heat.png height.png")


if __name__ == "__main__":
    main()
