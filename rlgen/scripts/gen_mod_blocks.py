"""Generates rlgen copies of modded 1.12 building blocks that RLCraft's structures use (BetterNether, Quark, Traverse, Defiled Lands):
textures from the mod jars, 1.21 models/blockstates in vanilla shapes, loot, tags, lang, and data/rlgen/mod_blocks.json, which
RLGen reads to register them. The mapping from the 1.12 ids is in convert_rc.py (MOD_BLOCKS, kind "rlike").
Usage: python3 -I gen_mod_blocks.py <dir with unzipped mod jars (each <dir>/<jar name>/assets)> <dir with 1.21 vanilla blockstates> <resources dir>"""
import json, os, shutil, sys, glob
JARS, VBS, RES = sys.argv[1:4]
A = RES + "/assets/rlgen/"
D = RES + "/data/"

# name, type, 1.12 block (mod:block[#variant key]), 1.21 block to copy properties from, tool
SPECS = [
    ("nether_brick_tile_large", "cube", "betternether:nether_brick_tile_large", "nether_bricks", "pickaxe"),
    ("nether_brick_tile_small", "cube", "betternether:nether_brick_tile_small", "nether_bricks", "pickaxe"),
    ("nether_brick_tile_stairs", "stairs", "betternether:nether_brick_tile_stairs", "nether_brick_stairs", "pickaxe"),
    ("nether_brick_tile_slab", "slab", "betternether:nether_brick_tile_slab_half", "nether_brick_slab", "pickaxe"),
    ("bone_tile", "cube", "betternether:bone_tile", "bone_block", "pickaxe"),
    ("bone_wall", "wall", "betternether:bone_wall", "cobblestone_wall", "pickaxe"),
    ("bone_stairs", "stairs", "betternether:bone_stairs", "stone_brick_stairs", "pickaxe"),
    ("bone_slab", "slab", "betternether:bone_slab_half", "stone_brick_slab", "pickaxe"),
    ("reeds_block", "pillar", "betternether:reeds_block", "bamboo_block", "axe", {"end": "betternether:blocks/reeds_block_top", "side": "betternether:blocks/reeds_block_side"}),
    ("reeds_stairs", "stairs", "betternether:reeds_stairs", "bamboo_stairs", "axe"),
    ("reeds_fence", "fence", "betternether:reeds_fence", "bamboo_fence", "axe"),
    ("reeds_ladder", "ladder", "betternether:reeds_ladder", "ladder", "axe"),
    ("stalagnate_planks", "cube", "betternether:stalagnate_planks", "oak_planks", "axe"),
    ("stalagnate_bark", "pillar", "betternether:stalagnate_bark", "oak_wood", "axe"),
    ("cincinnasite_forged", "cube", "betternether:cincinnasite_forged", "gold_block", "pickaxe"),
    ("cincinnasite_stairs", "stairs", "betternether:cincinnasite_stairs", "sandstone_stairs", "pickaxe"),
    ("cincinnasite_pillar", "pillar", "betternether:cincinnasite_bricks_pillar", "quartz_pillar", "pickaxe"),
    ("cincinnasite_tile_small", "cube", "betternether:cincinnasite_tile_small", "sandstone", "pickaxe"),
    ("cincinnasite_wall", "wall", "betternether:cincinnasite_wall", "sandstone_wall", "pickaxe"),
    ("cincinnasite_slab", "slab", "betternether:cincinnasite_slab_half", "sandstone_slab", "pickaxe"),
    ("cincinnasite_lantern", "cube", "betternether:cincinnasite_lantern", "glowstone", "pickaxe"),
    ("cincinnasite_pedestal", "cube", "betternether:cincinnasite_pedestal", "chiseled_sandstone", "pickaxe"),
    ("quartz_glass_framed_pane", "pane", "betternether:quartz_glass_framed_pane", "glass_pane", "", {"pane": "betternether:blocks/quartz_glass_framed", "edge": "betternether:blocks/quartz_glass_framed"}),
    ("carved_wood_oak", "cube", "quark:carved_wood", "oak_planks", "axe", {"all": "quark:blocks/carved_oak_wood"}),
    ("carved_wood_spruce", "cube", "quark:carved_wood", "spruce_planks", "axe", {"all": "quark:blocks/carved_spruce_wood"}),
    ("carved_wood_birch", "cube", "quark:carved_wood", "birch_planks", "axe", {"all": "quark:blocks/carved_birch_wood"}),
    ("carved_wood_jungle", "cube", "quark:carved_wood", "jungle_planks", "axe", {"all": "quark:blocks/carved_jungle_wood"}),
    ("carved_wood_acacia", "cube", "quark:carved_wood", "acacia_planks", "axe", {"all": "quark:blocks/carved_acacia_wood"}),
    ("carved_wood_dark_oak", "cube", "quark:carved_wood", "dark_oak_planks", "axe", {"all": "quark:blocks/carved_dark_oak_wood"}),
    ("duskbound_block", "cube", "quark:duskbound_block", "purpur_block", "pickaxe"),
    ("duskbound_block_stairs", "stairs", "quark:duskbound_block_stairs", "purpur_stairs", "pickaxe"),
    ("duskbound_block_slab", "slab", "quark:duskbound_block_slab", "purpur_slab", "pickaxe"),
    ("duskbound_lantern", "cube", "quark:duskbound_lantern", "sea_lantern", "pickaxe"),
    ("quilted_wool_purple", "cube", "quark:quilted_wool", "purple_wool", "", {"all": "quark:blocks/wool_quilted_purple"}),
    ("stained_clay_tiles_blue", "cube", "quark:stained_clay_tiles", "blue_terracotta", "pickaxe", {"all": "quark:blocks/stained_clay_tiles_blue"}),
    ("marble_smooth", "cube", "quark:marble", "polished_diorite", "pickaxe", {"all": "quark:blocks/stone_marble_smooth"}),
    ("marble_bricks", "cube", "quark:world_stone_bricks", "stone_bricks", "pickaxe", {"all": "quark:blocks/stone_marble_bricks"}),
    ("marble_bricks_wall", "wall", "quark:stone_marble_bricks_wall", "stone_brick_wall", "pickaxe"),
    ("midori_block", "cube", "quark:midori_block", "lime_terracotta", "pickaxe"),
    ("grate", "pane", "quark:grate", "iron_bars", "pickaxe"),
    ("fir_planks", "cube", "traverse:fir_planks", "spruce_planks", "axe"),
    ("fir_stairs", "stairs", "traverse:fir_stairs", "spruce_stairs", "axe"),
    ("blue_rock_slab", "slab", "traverse:blue_rock_slab", "stone_slab", "pickaxe", {"all": "rlgen:block/blue_rock"}),
    ("tenebra_planks", "cube", "defiledlands:tenebra_planks", "dark_oak_planks", "axe"),
    ("tenebra_stairs", "stairs", "defiledlands:tenebra_stairs", "dark_oak_stairs", "axe"),
]

ASSETS = {}
for d in glob.glob(JARS + "/*/assets/*"):
    ASSETS.setdefault(os.path.basename(d), d)

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w"), indent=2)

def tex_ref(t, mod):
    """1.12 texture path -> 1.21 (copying modded textures into rlgen/block/<mod>/)"""
    if t.startswith("#") or t.startswith("rlgen:"):
        return t
    ns, path = (t.split(":", 1) + [None])[:2] if ":" in t else ("minecraft", t)
    if path is None:
        ns, path = "minecraft", t
    path = path.replace("blocks/", "block/", 1)
    if ns == "minecraft":
        return "minecraft:" + path
    src = f"{ASSETS[ns]}/textures/{path.replace('block/', 'blocks/', 1)}.png"
    if not os.path.exists(src):
        src = f"{ASSETS[ns]}/textures/{path}.png"
    name = path.split("/")[-1]
    dst = f"{A}textures/block/{ns}/{name}.png"
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copy(src, dst)
    if os.path.exists(src + ".mcmeta"):
        shutil.copy(src + ".mcmeta", dst + ".mcmeta")
    return f"rlgen:block/{ns}/{name}"

def load_model(ref, mod):
    """-> (vanilla parent or None, textures, elements)"""
    textures, elements = {}, None
    while ref:
        ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
        path = path if path.startswith("block/") else "block/" + path
        if ns == "minecraft":
            return path.split("/", 1)[1], textures, elements
        f = f"{ASSETS[ns]}/models/{path}.json"
        m = json.load(open(f))
        for k, v in m.get("textures", {}).items():
            textures.setdefault(k, v)
        if elements is None and "elements" in m:
            elements = m["elements"]
        ref = m.get("parent")
    return None, textures, elements

def variant(block):
    ref, _, key = block.partition("#")
    ns, name = ref.split(":")
    found = glob.glob(f"{ASSETS[ns]}/blockstates/**/{name}.json", recursive=True)
    bs = json.load(open(found[0]))
    defaults = bs.get("defaults", {})
    vs = bs.get("variants")
    if vs is None:  # multipart: the first part (a post)
        return bs["multipart"][0]["apply"]["model"], {}, ns
    if key and key in vs:
        v = vs[key]
    else:
        for k in ("normal", "", "inventory", "axis=y", "half=bottom", "facing=east,half=bottom,shape=straight", "facing=north"):
            if k in vs:
                v = vs[k]
                break
        else:
            v = next(iter(vs.values()))
    if isinstance(v, list):
        v = v[0]
    if isinstance(v, dict) and "model" not in v and len(v) == 1 and isinstance(next(iter(v.values())), dict):
        v = next(iter(v.values()))  # forge format "variant": {"name": {...}}
    model = v.get("model", defaults.get("model"))
    textures = dict(defaults.get("textures", {}))
    textures.update(v.get("textures", {}))
    if model and ":" not in model:
        model = "minecraft:" + model  # unqualified blockstate models are vanilla
    return model, textures, ns

def resolved(block):
    model, extra, ns = variant(block)
    parent, textures, elements = load_model(model, ns)
    t = dict(textures)
    t.update(extra)
    # resolve #refs
    for _ in range(4):
        for k, v in t.items():
            if v.startswith("#") and v[1:] in t:
                t[k] = t[v[1:]]
    return parent, t, elements, ns

def pick(t, *keys):
    for k in keys:
        if k in t and not t[k].startswith("#"):
            return t[k]
    return next(v for v in t.values() if not v.startswith("#"))

def vanilla_bs(name, renames):
    s = open(f"{VBS}/{name}.json").read()
    for a, b in renames.items():
        s = s.replace(f'"minecraft:block/{a}"', f'"rlgen:block/{b}"')
    return json.loads(s)

out = []
for spec in SPECS:
    name, typ, block, like, tool = spec[:5]
    if len(spec) > 5:
        parent, t, elements, ns = "cube_all", dict(spec[5]), None, block.split(":")[0]
    else:
        parent, t, elements, ns = resolved(block)
    T = lambda *k: tex_ref(pick(t, *k), ns)
    if typ == "cube":
        if elements:
            m = {"parent": "minecraft:block/block", "textures": {k: tex_ref(v, ns) for k, v in t.items()}, "elements": elements}
        elif parent in ("cube_all", "cube_column", "cube_bottom_top", "cube", "cube_top", "orientable"):
            m = {"parent": "minecraft:block/" + parent, "textures": {k: tex_ref(v, ns) for k, v in t.items()}}
        else:
            m = {"parent": "minecraft:block/cube_all", "textures": {"all": T("all", "texture", "side", "particle")}}
        m["render_type"] = "minecraft:cutout"
        w(f"{A}models/block/{name}.json", m)
        w(f"{A}blockstates/{name}.json", {"variants": {"": {"model": f"rlgen:block/{name}"}}})
    elif typ == "stairs":
        tx = {"bottom": T("bottom", "all", "texture"), "top": T("top", "all", "texture"), "side": T("side", "all", "texture")}
        for v in ("stairs", "inner_stairs", "outer_stairs"):
            suffix = "" if v == "stairs" else "_" + v.split("_")[0]
            w(f"{A}models/block/{name}{suffix}.json", {"parent": f"minecraft:block/{v}", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("oak_stairs", {"oak_stairs": name, "oak_stairs_inner": name + "_inner", "oak_stairs_outer": name + "_outer"}))
    elif typ == "slab":
        tx = {"bottom": T("bottom", "all", "texture"), "top": T("top", "all", "texture"), "side": T("side", "all", "texture")}
        w(f"{A}models/block/{name}.json", {"parent": "minecraft:block/slab", "textures": tx})
        w(f"{A}models/block/{name}_top.json", {"parent": "minecraft:block/slab_top", "textures": tx})
        w(f"{A}models/block/{name}_double.json", {"parent": "minecraft:block/cube_bottom_top", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("oak_slab", {"oak_slab": name, "oak_slab_top": name + "_top", "oak_planks": name + "_double"}))
    elif typ == "wall":
        tx = {"wall": T("wall", "texture", "side", "all", "particle")}
        for v, sfx in (("template_wall_post", "_post"), ("template_wall_side", "_side"), ("template_wall_side_tall", "_side_tall"), ("wall_inventory", "_inventory")):
            w(f"{A}models/block/{name}{sfx}.json", {"parent": f"minecraft:block/{v}", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("cobblestone_wall", {"cobblestone_wall_post": name + "_post", "cobblestone_wall_side": name + "_side",
                                                                        "cobblestone_wall_side_tall": name + "_side_tall"}))
    elif typ == "fence":
        tx = {"texture": T("texture", "all", "side", "particle")}
        for v, sfx in (("fence_post", "_post"), ("fence_side", "_side"), ("fence_inventory", "_inventory")):
            w(f"{A}models/block/{name}{sfx}.json", {"parent": f"minecraft:block/{v}", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("oak_fence", {"oak_fence_post": name + "_post", "oak_fence_side": name + "_side"}))
    elif typ == "pane":
        tx = {"pane": T("pane", "texture", "all", "particle"), "edge": T("edge", "pane", "texture", "all", "particle")}
        for v, sfx in (("template_glass_pane_post", "_post"), ("template_glass_pane_side", "_side"), ("template_glass_pane_side_alt", "_side_alt"),
                       ("template_glass_pane_noside", "_noside"), ("template_glass_pane_noside_alt", "_noside_alt")):
            w(f"{A}models/block/{name}{sfx}.json", {"parent": f"minecraft:block/{v}", "render_type": "minecraft:cutout", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("glass_pane", {"glass_pane_post": name + "_post", "glass_pane_side": name + "_side",
                                                                  "glass_pane_side_alt": name + "_side_alt", "glass_pane_noside": name + "_noside",
                                                                  "glass_pane_noside_alt": name + "_noside_alt"}))
    elif typ == "ladder":
        w(f"{A}models/block/{name}.json", {"parent": "minecraft:block/ladder", "render_type": "minecraft:cutout", "textures": {"texture": T("texture", "ladder", "all", "particle"), "particle": T("texture", "ladder", "all", "particle")}})
        w(f"{A}blockstates/{name}.json", vanilla_bs("ladder", {"ladder": name}))
    elif typ == "pillar":
        tx = {"end": T("end", "top", "all"), "side": T("side", "all", "texture")}
        w(f"{A}models/block/{name}.json", {"parent": "minecraft:block/cube_column", "textures": tx})
        w(f"{A}models/block/{name}_horizontal.json", {"parent": "minecraft:block/cube_column_horizontal", "textures": tx})
        w(f"{A}blockstates/{name}.json", vanilla_bs("oak_log", {"oak_log": name, "oak_log_horizontal": name + "_horizontal"}))
    item_model = {"wall": "_inventory", "fence": "_inventory"}.get(typ)
    if typ in ("pane", "ladder"):
        w(f"{A}models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": json.load(open(f"{A}models/block/{name}{'_noside' if typ == 'pane' else ''}.json"))["textures"]["pane" if typ == "pane" else "texture"]}})
    else:
        w(f"{A}models/item/{name}.json", {"parent": f"rlgen:block/{name}{item_model or ''}"})
    drops = {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"rlgen:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
    if typ == "slab":
        drops["pools"][0]["entries"][0]["functions"] = [{"function": "minecraft:set_count", "count": 2.0, "conditions": [
            {"condition": "minecraft:block_state_property", "block": f"rlgen:{name}", "properties": {"type": "double"}}]}]
    w(f"{D}rlgen/loot_table/blocks/{name}.json", drops)
    out.append({"name": name, "type": typ, "like": like, "tool": tool})

def tag(t, vals):
    p = D + f"minecraft/tags/block/{t}.json"
    d = json.load(open(p)) if os.path.exists(p) else {"values": []}
    for v in vals:
        if v not in d["values"]:
            d["values"].append(v)
    w(p, d)
for tool in ("pickaxe", "axe"):
    tag(f"mineable/{tool}", [f"rlgen:{o['name']}" for o in out if o["tool"] == tool])
tag("walls", [f"rlgen:{o['name']}" for o in out if o["type"] == "wall"])
tag("fences", [f"rlgen:{o['name']}" for o in out if o["type"] == "fence"])
tag("climbable", [f"rlgen:{o['name']}" for o in out if o["type"] == "ladder"])
L = json.load(open(A + "lang/en_us.json"))
for o in out:
    L[f"block.rlgen.{o['name']}"] = " ".join(x.capitalize() for x in o["name"].split("_"))
json.dump(L, open(A + "lang/en_us.json", "w"), indent=2)
w(D + "rlgen/mod_blocks.json", out)
print(len(out), "blocks")
