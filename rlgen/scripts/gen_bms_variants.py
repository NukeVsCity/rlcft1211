"""Converts Better Mineshafts' variants.json (RLCraft's copy, 1.12 block states) to 1.21 block states.
Usage: python3 -I gen_bms_variants.py <minecraft-data dir> <in variants.json> <out json>"""
import sys, os, importlib.util, re, json
here = os.path.dirname(os.path.abspath(__file__))
mcdata, src, dst = sys.argv[1:4]
sys.argv = ["convert_rc.py", "-", "-", mcdata, "-"]
spec = importlib.util.spec_from_file_location("convert_rc", os.path.join(here, "convert_rc.py"))
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
META = {}
for line in open(os.path.join(here, "states112.tsv")):
    k, m = line.rstrip("\n").split("\t")
    META[k] = int(m)

def parse(state):
    m = re.match(r"([^\[]+)(?:\[(.*)\])?$", state)
    props = dict(p.split("=") for p in m.group(2).split(",")) if m.group(2) else {}
    return m.group(1), props

def stairs(block, props):
    p = {"facing": props.get("facing", "north"), "half": props.get("half", "bottom"), "shape": "straight", "waterlogged": "false"}
    return block + "[" + ",".join(f"{k}={v}" for k, v in sorted(p.items())) + "]"

def wall(block):
    return block + "[east=none,north=none,south=none,up=true,waterlogged=false,west=none]"

# Quark blocks the RLCraft variants use (Quark is not in 1.21: rlgen blocks with its textures, or the closest vanilla block)
QUARK = {
    "carved_wood": lambda p: "rlgen:carved_wood_" + p.get("variant", "carved_oak_wood")[len("carved_"):-len("_wood")],
    "stone_stairs": lambda p: stairs("minecraft:stone_stairs", p),
    "stone_andesite_stairs": lambda p: stairs("minecraft:andesite_stairs", p),
    "sandstone_bricks_stairs": lambda p: stairs("minecraft:sandstone_stairs", p),
    "icy_stone_stairs": lambda p: stairs("minecraft:stone_brick_stairs", p),
    "soul_sandstone_stairs": lambda p: stairs("minecraft:sandstone_stairs", p),
    "bark_dark_oak_stairs": lambda p: stairs("minecraft:dark_oak_stairs", p),
    "bark_dark_oak_slab": lambda p: "minecraft:dark_oak_slab[type=bottom,waterlogged=false]",
    "icy_stone_brick_slab": lambda p: "minecraft:stone_brick_slab[type=" + p.get("half", "bottom") + ",waterlogged=false]",
    "soul_sandstone": lambda p: {"soul_sandstone": "minecraft:sandstone", "smooth_soul_sandstone": "minecraft:smooth_sandstone",
                                 "chiseled_soul_sandstone": "minecraft:chiseled_sandstone"}[p.get("variant", "soul_sandstone")],
    "sandstone_new": lambda p: {"sandstone_smooth": "minecraft:smooth_sandstone", "sandstone_bricks": "minecraft:cut_sandstone",
                                "red_sandstone_smooth": "minecraft:smooth_red_sandstone", "red_sandstone_bricks": "minecraft:cut_red_sandstone"}[p["variant"]],
    "sandstone_wall": lambda p: wall("minecraft:sandstone_wall"),
    "red_sandstone_wall": lambda p: wall("minecraft:red_sandstone_wall"),
    "icy_stone_brick_wall": lambda p: wall("minecraft:stone_brick_wall"),
    "biome_cobblestone": lambda p: {"fire_stone": "rlgen:fire_stone", "icy_stone": "rlgen:icy_stone", "cobbed_stone": "rlgen:cobbed_stone"}[p["variant"]],
    "biome_brick": lambda p: "minecraft:stone_bricks",
    "jasper": lambda p: "rlgen:jasper",
    "world_stone_bricks": lambda p: {"stone_jasper_bricks": "minecraft:stone_bricks"}.get(p.get("variant"), "minecraft:stone_bricks"),
    "world_stone_pavement": lambda p: "minecraft:smooth_stone",
    "iron_plate": lambda p: "minecraft:iron_block",
    "custom_bookshelf": lambda p: "minecraft:bookshelf",
}
MODS = {"iceandfire:silver_ore": "rlgen:silver_ore", "notreepunching:cobblestone/andesite": "minecraft:cobblestone"}
seen = {}

def conv(s):
    name, props = parse(s)
    ns, short = name.split(":", 1)
    if name in MODS:
        return MODS[name]
    if ns == "quark":
        return QUARK[short](props)
    cands = [(m, k) for k, m in META.items() if k.startswith(name + "[") and all(f"{a}={b}" in k[len(name):].strip("[]").split(",") for a, b in props.items())]
    if not cands:
        raise SystemExit("unknown " + s)
    meta = min(cands)[0]
    out = c.convert_block(name, meta)[0]
    if name == "minecraft:tnt" and props.get("explode") == "true":
        out = "minecraft:tnt[unstable=true]"
    return re.sub(r"shape=\w+", "shape=straight", out)

STATE = re.compile(r"^[a-z_]+:[a-z_0-9/]+(\[[^\]]*\])?$")
def walk(o):
    if isinstance(o, dict) and "entries" in o:
        # Gson filled a LinkedHashMap in JSON order; kept as pairs since two 1.12 states can flatten to one 1.21 state
        return {"entries": [[walk(k), v] for k, v in o["entries"].items()], **{k: walk(v) for k, v in o.items() if k != "entries"}}
    if isinstance(o, dict):
        return {(conv(k) if STATE.match(k) else k): walk(v) for k, v in o.items()}
    if isinstance(o, list):
        return [walk(v) for v in o]
    if isinstance(o, str) and STATE.match(o):
        seen[o] = conv(o)
        return seen[o]
    return o
out = walk(json.load(open(src)))
json.dump(out, open(dst, "w"), indent=1)
for k, v in sorted(seen.items()):
    print(k, "->", v)
