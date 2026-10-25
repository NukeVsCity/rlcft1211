"""Converts Recurrent Complex 1.12 structures and inventory generators to rlgen data.

Usage: python3 convert_rc.py <rc jar structures/active dir> <instance structures/active dir> <minecraft-data dir> <resources dir>

minecraft-data dir needs: legacy.json (pc/common), 112_blocks.json, 112_items.json, 1211_blocks.json.

For every .rcst it writes data/rlgen/rc/structures/<id>.nbt (gzipped):
  json        the original structure.json (placement rules and transformers are read at runtime)
  size        [width, height, length]
  palette     [{legacy: "minecraft:stone_stairs", meta: 2b, state: "minecraft:stone_stairs[...]"}]  state "" = keep RC block
  blocks      int[] palette index, index (z * height + y) * width + x (IvBlockCollection order)
  tiles       [{x, y, z, nbt: <1.21 block entity data without id/pos>, loot: [...], script: {...}}]
  entities    [{pos: [x, y, z], nbt: <1.21 entity data>}]
Inventory generators go to data/rlgen/rc/loot/<generatorID>.json, presets and categories are copied as JSON.
Modded blocks/items/mobs are mapped to the closest vanilla thing (see MOD_BLOCKS, MOD_MOBS); unknown ones become air / are dropped.
"""
import glob, gzip, io, json, os, re, sys, zipfile, collections
import nbtlib
from nbtlib import Compound, List, String, Int, Byte, Short, Long, Double, Float, IntArray

RCJAR, INSTANCE, MCDATA, RES = sys.argv[1:5]
OUT = f"{RES}/data/rlgen/rc"

LEGACY = json.load(open(f"{MCDATA}/legacy.json"))
B112 = {x["name"]: x["id"] for x in json.load(open(f"{MCDATA}/112_blocks.json"))}
B112.update({"silver_glazed_terracotta": 243, "silver_shulker_box": 227})
I112 = {x["name"]: x["id"] for x in json.load(open(f"{MCDATA}/112_items.json"))}
MODERN = {x["name"]: {s["name"]: s for s in x.get("states", [])} for x in json.load(open(f"{MCDATA}/1211_blocks.json"))}
STATS = collections.Counter()

# ---------------------------------------------------------------- block states
RENAME_113 = {  # 1.13 flattened name -> 1.21
    "sign": "oak_sign", "wall_sign": "oak_wall_sign", "grass": "short_grass", "grass_path": "dirt_path",
    "stone_slab": "smooth_stone_slab", "portal": "nether_portal",
}

def parse_state(s):
    m = re.match(r"([^\[]+)(?:\[(.*)\])?$", s)
    name = m.group(1).split(":")[-1]
    props = dict(p.split("=") for p in m.group(2).split(",")) if m.group(2) else {}
    return name, props

def fmt_state(name, props):
    valid = MODERN.get(name)
    if valid is None:
        STATS["missing_modern:" + name] += 1
        return "minecraft:air"
    props = {k: v for k, v in props.items() if k in valid and (valid[k]["type"] != "enum" or v in valid[k]["values"])}
    return f"minecraft:{name}" + ("[" + ",".join(f"{k}={v}" for k, v in sorted(props.items())) + "]" if props else "")

def vanilla(name, meta):
    """1.12 vanilla block name + meta -> 1.21 (name, props)."""
    bid = B112.get(name)
    if bid is None:
        return None
    s = LEGACY["blocks"].get(f"{bid}:{meta}") or LEGACY["blocks"].get(f"{bid}:0")
    if s is None:
        return None
    n, p = parse_state(s)
    n = RENAME_113.get(n, n)
    if n == "smooth_stone_slab" and bid == 43:
        n, p = "smooth_stone_slab", {"type": "double"}
    return n, p

def like(vanilla_name, target, extra=None):
    return ("like", vanilla_name, target, extra or {})

def st(target, **props):
    return ("state", target, props)

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak"]

MOD_BLOCKS = {
    # harvestcraft / gardens
    "harvestcraft:pambroccolicrop": st("wheat", age="7"), "harvestcraft:pamcelerycrop": st("carrots", age="7"),
    "harvestcraft:pamcucumbercrop": st("potatoes", age="7"), "harvestcraft:pamzucchinicrop": st("beetroots", age="3"),
    "harvestcraft:windygarden": st("fern"),
    # ice and fire
    "iceandfire:ash": st("gray_concrete_powder"), "iceandfire:chared_cobblestone": st("cobbled_deepslate"),
    "iceandfire:goldpile": st("yellow_carpet"), "iceandfire:silverpile": st("light_gray_carpet"), "iceandfire:podium": st("polished_andesite"),
    "iceandfire:sapphire_block": st("lapis_block"), "iceandfire:silver_block": st("iron_block"),
    # quark
    "quark:bark": ("meta_choice", [f"{w}_wood" for w in WOODS], {"axis": "y"}),
    "quark:bark_spruce_slab": like("wooden_slab", "spruce_slab"), "quark:bark_spruce_stairs": like("oak_stairs", "spruce_stairs"),
    "quark:basalt": st("smooth_basalt"), "quark:basalt_wall": st("cobbled_deepslate_wall"),
    "quark:birch_pressure_plate": like("wooden_pressure_plate", "birch_pressure_plate"),
    "quark:birch_trapdoor": like("trapdoor", "birch_trapdoor"), "quark:dark_oak_trapdoor": like("trapdoor", "dark_oak_trapdoor"),
    "quark:jungle_trapdoor": like("trapdoor", "jungle_trapdoor"), "quark:spruce_trapdoor": like("trapdoor", "spruce_trapdoor"),
    "quark:carved_wood": ("meta_choice", [f"stripped_{w}_wood" for w in WOODS], {"axis": "y"}),
    "quark:charcoal_block": st("coal_block"),
    "quark:charred_nether_brick_slab": like("stone_slab", "nether_brick_slab"), "quark:charred_nether_brick_stairs": like("oak_stairs", "nether_brick_stairs"),
    "quark:charred_nether_bricks": st("nether_bricks"),
    "quark:cobblestone_mossy_slab": like("stone_slab", "mossy_cobblestone_slab"), "quark:cobblestone_mossy_slab_double": st("mossy_cobblestone"),
    "quark:cobblestone_mossy_stairs": like("oak_stairs", "mossy_cobblestone_stairs"),
    "quark:crystal": st("amethyst_block"), "quark:crystal_pane": st("purple_stained_glass_pane"),
    "quark:custom_bookshelf": st("bookshelf"), "quark:custom_chest": like("chest", "chest"),
    "quark:duskbound_block": st("purpur_block"), "quark:duskbound_block_slab": like("stone_slab", "purpur_slab"),
    "quark:duskbound_block_slab_double": st("purpur_block"), "quark:duskbound_block_stairs": like("oak_stairs", "purpur_stairs"),
    "quark:duskbound_block_wall": st("end_stone_brick_wall"), "quark:duskbound_lantern": st("sea_lantern"),
    "quark:elder_prismarine": st("prismarine"), "quark:elder_prismarine_bricks_slab": like("stone_slab", "prismarine_brick_slab"),
    "quark:elder_prismarine_bricks_stairs": like("oak_stairs", "prismarine_brick_stairs"), "quark:elder_sea_lantern": st("sea_lantern"),
    "quark:end_bricks_slab": like("stone_slab", "end_stone_brick_slab"), "quark:end_bricks_slab_double": st("end_stone_bricks"),
    "quark:end_bricks_stairs": like("oak_stairs", "end_stone_brick_stairs"), "quark:end_bricks_wall": st("end_stone_brick_wall"),
    "quark:ender_watcher": st("observer"), "quark:framed_glass": st("glass"), "quark:framed_glass_pane": st("glass_pane"),
    "quark:glowstone_dust_block": st("glowstone"), "quark:grate": st("iron_bars"), "quark:gunpowder_block": st("gray_concrete_powder"),
    "quark:iron_button": like("stone_button", "stone_button"), "quark:iron_ladder": like("ladder", "ladder"),
    "quark:iron_plate": st("iron_block"), "quark:iron_plate_stairs": like("oak_stairs", "polished_andesite_stairs"),
    "quark:limestone": st("calcite"), "quark:lit_lamp": st("redstone_lamp", lit="true"), "quark:marble": st("polished_diorite"),
    "quark:midori_block": st("lime_terracotta"), "quark:midori_block_stairs": like("oak_stairs", "mossy_stone_brick_stairs"),
    "quark:midori_pillar": st("green_terracotta"), "quark:obsidian_pressure_plate": like("stone_pressure_plate", "polished_blackstone_pressure_plate"),
    "quark:paper_lantern": st("lantern"), "quark:paper_wall_big": st("white_stained_glass_pane"), "quark:paper_wall_sakura": st("pink_stained_glass_pane"),
    "quark:prismarine_dark_slab": like("stone_slab", "dark_prismarine_slab"), "quark:prismarine_dark_stairs": like("oak_stairs", "dark_prismarine_stairs"),
    "quark:purpur_block_wall": st("end_stone_brick_wall"), "quark:quilted_wool": ("meta_choice", [f"{c}_wool" for c in COLORS], {}),
    "quark:roots": st("hanging_roots"), "quark:smoker": like("furnace", "smoker"),
    "quark:stained_clay_tiles": ("meta_choice", [f"{c}_terracotta" for c in COLORS], {}),
    "quark:stained_planks_white_stairs": like("oak_stairs", "birch_stairs"),
    "quark:stone_andesite_bricks_slab": like("stone_slab", "polished_andesite_slab"), "quark:stone_andesite_bricks_stairs": like("oak_stairs", "polished_andesite_stairs"),
    "quark:stone_andesite_bricks_wall": st("andesite_wall"), "quark:stone_andesite_wall": st("andesite_wall"),
    "quark:stone_basalt_bricks_slab": like("stone_slab", "polished_blackstone_brick_slab"), "quark:stone_basalt_slab": like("stone_slab", "polished_blackstone_slab"),
    "quark:stone_basalt_stairs": like("oak_stairs", "polished_blackstone_stairs"), "quark:stone_diorite_bricks_wall": st("diorite_wall"),
    "quark:stone_marble_bricks_slab": like("stone_slab", "polished_diorite_slab"), "quark:stone_marble_bricks_wall": st("diorite_wall"),
    "quark:stone_marble_stairs": like("oak_stairs", "polished_diorite_stairs"), "quark:stone_speleothem": st("pointed_dripstone"),
    "quark:stone_stairs": like("oak_stairs", "stone_stairs"), "quark:stonebrick_wall": st("stone_brick_wall"),
    "quark:variant_leaves": st("acacia_leaves", persistent="true"),
    "quark:world_stone_bricks": st("stone_bricks"), "quark:world_stone_carved": st("chiseled_stone_bricks"), "quark:world_stone_pavement": st("smooth_stone"),
    "quark:colored_flowerpot_black": st("flower_pot"), "quark:colored_flowerpot_green": st("flower_pot"), "quark:colored_flowerpot_light_blue": st("flower_pot"),
    "quark:colored_flowerpot_magenta": st("flower_pot"), "quark:colored_flowerpot_white": st("flower_pot"),
    # varied commodities (furniture)
    "variedcommodities:beam": st("stripped_oak_log"), "variedcommodities:book": st("air"), "variedcommodities:candle": st("candle", candles="1", lit="true"),
    "variedcommodities:candle_unlit": st("candle", candles="1"), "variedcommodities:chair": like("oak_stairs", "oak_stairs"),
    "variedcommodities:couch_wood": like("oak_stairs", "spruce_stairs"), "variedcommodities:couch_wool": like("oak_stairs", "spruce_stairs"),
    "variedcommodities:crate": st("barrel", facing="up"), "variedcommodities:lamp": st("lantern"), "variedcommodities:tall_lamp": st("lantern"),
    "variedcommodities:pedestal": st("stone_brick_wall"), "variedcommodities:shelf": st("spruce_slab", type="top"),
    "variedcommodities:sign": like("wall_sign", "oak_wall_sign"), "variedcommodities:stool": st("oak_fence"),
    "variedcommodities:table": st("spruce_slab", type="top"), "variedcommodities:tombstone": st("mossy_cobblestone_wall"),
    "variedcommodities:wall_banner": like("wall_banner", "white_wall_banner"), "variedcommodities:weapon_rack": st("air"),
    # no tree punching
    "notreepunching:cobblestone/basalt": st("cobbled_deepslate"), "notreepunching:cobblestone/limestone": st("cobblestone"),
    "notreepunching:cobblestone/marble": st("cobblestone"), "notreepunching:loose_rock/limestone": st("air"),
    "notreepunching:loose_rock/sandstone": st("air"), "notreepunching:loose_rock/stone": st("air"),
    # charm
    "charm:barrel": st("barrel", facing="up"), "charm:bookshelf_chest": st("bookshelf"), "charm:composter": st("composter"),
    "charm:ender_pearl_block": st("warped_wart_block"), "charm:iron_lantern": st("lantern"), "charm:nether_gold_deposit": st("nether_gold_ore"),
    "charm:rune_portal": st("air"), "charm:rune_portal_frame": st("obsidian"), "charm:variable_redstone_lamp": st("redstone_lamp"),
    # comforts
    "comforts:hammock_white": st("white_wool"), "comforts:rope": st("chain"),
    "comforts:sleeping_bag_brown": st("brown_carpet"), "comforts:sleeping_bag_cyan": st("cyan_carpet"), "comforts:sleeping_bag_green": st("green_carpet"),
    "comforts:sleeping_bag_light_blue": st("light_blue_carpet"), "comforts:sleeping_bag_magenta": st("magenta_carpet"),
    "contenttweaker:bone_pile": st("bone_block"),
    # defiled lands
    "defiledlands:conjuring_altar": st("enchanting_table"), "defiledlands:dirt_defiled": ("rlgen", "rlgen:dirt_defiled"),
    "defiledlands:grass_defiled": ("rlgen", "rlgen:grass_defiled"), "defiledlands:stone_defiled": ("rlgen", "rlgen:stone_defiled"),
    "defiledlands:glass_obscure": st("tinted_glass"), "defiledlands:healing_pad": st("purpur_slab"),
    "defiledlands:hephaestite_block": st("crying_obsidian"), "defiledlands:hephaestite_ore": st("nether_quartz_ore"),
    "defiledlands:ravaging_decoration": st("chiseled_polished_blackstone"), "defiledlands:sandstone_defiled_stairs": like("oak_stairs", "purpur_stairs"),
    "defiledlands:scarlite_block": st("redstone_block"), "defiledlands:scarlite_ore": st("deepslate_redstone_ore"),
    "defiledlands:stone_defiled_bricks_stairs": like("oak_stairs", "polished_blackstone_brick_stairs"),
    "defiledlands:stone_defiled_decoration": st("chiseled_polished_blackstone"), "defiledlands:stone_defiled_slab": like("stone_slab", "polished_blackstone_slab"),
    "defiledlands:stone_defiled_slab_double": st("polished_blackstone"), "defiledlands:stone_defiled_stairs": like("oak_stairs", "polished_blackstone_stairs"),
    "defiledlands:tenebra_door": like("wooden_door", "dark_oak_door"), "defiledlands:tenebra_leaves": ("rlgen", "rlgen:tenebra_leaves"),
    "defiledlands:tenebra_log": ("rlgen_log", "rlgen:tenebra_log"), "defiledlands:tenebra_planks": st("dark_oak_planks"),
    "defiledlands:tenebra_stairs": like("oak_stairs", "dark_oak_stairs"), "defiledlands:umbrium_block": st("coal_block"),
    "defiledlands:umbrium_ore": st("deepslate_coal_ore"), "defiledlands:vilespine": st("dead_bush"), "defiledlands:wood_defiled_slab": like("wooden_slab", "dark_oak_slab"),
    # dynamic trees 1.12 leftovers
    "dynamictrees:birchbranch": st("birch_log"), "dynamictrees:oakbranch": st("oak_log"), "dynamictrees:sprucebranch": st("spruce_log"),
    "dynamictrees:leaves0": st("oak_leaves", persistent="true"), "dynamictrees:rootydirt": st("dirt"),
    "fishingmadebetter:baitbox": st("barrel", facing="up"),
    # inspirations
    "inspirations:bookshelf": st("bookshelf"), "inspirations:enlightened_bush": st("azalea_leaves", persistent="true"),
    "inspirations:flower": st("cornflower"), "inspirations:glass_door": like("wooden_door", "oak_door"),
    "inspirations:glass_trapdoor": like("trapdoor", "oak_trapdoor"),
    "inspirations:carpeted_trapdoor_purple": like("trapdoor", "spruce_trapdoor"), "inspirations:carpeted_trapdoor_black": like("trapdoor", "dark_oak_trapdoor"), "inspirations:mulch": st("coarse_dirt"), "inspirations:path": st("dirt_path"),
    "inspirations:redstone_barrel": st("barrel", facing="up"), "inspirations:redstone_torch_lever": like("redstone_torch", "redstone_torch"),
    "inspirations:redstone_torch_lever_powered": like("redstone_torch", "redstone_torch"), "inspirations:rope": st("chain"),
    # lycanites
    "lycanitesmobs:aberrantcrystal": st("amethyst_cluster"), "lycanitesmobs:aberrantstone": st("purpur_block"),
    "lycanitesmobs:aberrantstone_slab": like("stone_slab", "purpur_slab"), "lycanitesmobs:aberrantstone_slab_double": st("purpur_block"),
    "lycanitesmobs:aberrantstonebrick": st("purpur_block"), "lycanitesmobs:aberrantstonebrick_fence": st("end_stone_brick_wall"),
    "lycanitesmobs:aberrantstonebrick_slab": like("stone_slab", "purpur_slab"), "lycanitesmobs:aberrantstonebrick_stairs": like("oak_stairs", "purpur_stairs"),
    "lycanitesmobs:aberrantstonebrick_wall": st("end_stone_brick_wall"), "lycanitesmobs:aberrantstonechiseled": st("purpur_pillar"),
    "lycanitesmobs:aberrantstonepolished": st("purpur_block"), "lycanitesmobs:aberrantstonetile": st("purpur_block"),
    "lycanitesmobs:aberrantstonetile_slab": like("stone_slab", "purpur_slab"), "lycanitesmobs:aberrantstonetile_stairs": like("oak_stairs", "purpur_stairs"),
    "lycanitesmobs:demoncrystal": st("shroomlight"), "lycanitesmobs:demonstone_slab_double": st("red_nether_bricks"),
    "lycanitesmobs:demonstonebrick": st("red_nether_bricks"), "lycanitesmobs:demonstonebrick_fence": st("red_nether_brick_wall"),
    "lycanitesmobs:demonstonebrick_stairs": like("oak_stairs", "red_nether_brick_stairs"), "lycanitesmobs:demonstonebrick_wall": st("red_nether_brick_wall"),
    "lycanitesmobs:demonstonetile_stairs": like("oak_stairs", "red_nether_brick_stairs"), "lycanitesmobs:lushcrystal": st("glowstone"),
    "lycanitesmobs:lushstonebrick_slab": like("stone_slab", "mossy_stone_brick_slab"), "lycanitesmobs:lushstonebrick_wall": st("mossy_stone_brick_wall"),
    "lycanitesmobs:lushstonetile": st("mossy_stone_bricks"), "lycanitesmobs:moglava": st("lava"), "lycanitesmobs:poison": st("water"),
    "lycanitesmobs:vesspoison": st("water"), "lycanitesmobs:shadowfire": st("soul_fire"),
    "lycanitesmobs:shadowstonebrick": st("deepslate_bricks"), "lycanitesmobs:shadowstonebrick_fence": st("deepslate_brick_wall"),
    "lycanitesmobs:shadowstonebrick_stairs": like("oak_stairs", "deepslate_brick_stairs"), "lycanitesmobs:shadowstonebrick_wall": st("deepslate_brick_wall"),
    # misc
    "mcwbridges:rope_spruce_bridge": st("spruce_slab"), "mcwbridges:rope_spruce_bridge_end": st("spruce_slab"),
    "realistictorches:torch_unlit": like("torch", "torch"),
    "rustic:barrel": st("barrel", facing="up"), "rustic:blood_orchid": st("red_tulip"), "rustic:brewing_barrel": st("barrel", facing="up"),
    "rustic:cabinet": st("barrel", facing="up"), "rustic:candle": st("candle", candles="1", lit="true"), "rustic:candle_lever": st("candle", candles="1", lit="true"),
    "rustic:candle_silver": st("candle", candles="1", lit="true"), "rustic:chain": st("chain"), "rustic:chain_silver": st("chain"),
    "rustic:chandelier": st("lantern", hanging="true"), "rustic:chandelier_gold": st("lantern", hanging="true"), "rustic:chandelier_silver": st("lantern", hanging="true"),
    "rustic:chili_crop": st("potatoes", age="7"), "rustic:tomato_crop": st("carrots", age="7"), "rustic:clay_wall_cross": st("white_terracotta"),
    "rustic:crop_stake": st("oak_fence"), "rustic:stake_tied": st("oak_fence"), "rustic:crushing_tub": st("cauldron"),
    "rustic:deathstalk_mushroom": st("red_mushroom"), "rustic:mooncap_mushroom": st("brown_mushroom"), "rustic:gargoyle": st("chiseled_stone_bricks"),
    "rustic:grape_leaves": st("oak_leaves", persistent="true"), "rustic:grape_stem": st("oak_fence"), "rustic:iron_lantern": st("lantern"),
    "rustic:lantern_wood": st("lantern"), "rustic:iron_lattice": st("iron_bars"), "rustic:leaves0": st("oak_leaves", persistent="true"),
    "rustic:liquid_barrel": st("barrel", facing="up"), "rustic:log": like("log", "oak_log"), "rustic:olivebranch": st("air"),
    "rustic:painted_wood_magenta": st("magenta_terracotta"), "rustic:painted_wood_purple": st("purple_terracotta"), "rustic:rope": st("chain"),
    "rustic:stone_pillar": st("smooth_stone"), "rustic:vase": st("decorated_pot"), "rustic:wildberry_bush": st("sweet_berry_bush", age="3"),
    "scalinghealth:crystalore": st("redstone_ore"), "sereneseasons:greenhouse_glass": st("glass"), "simpledifficulty:campfire": st("campfire"),
    "srparasites:parasitecanister": st("air"), "wearablebackpacks:backpack": st("air"), "bountifulbaubles:reforger": like("anvil", "anvil"),
    "classyhats:hat_stand": st("air"), "Railcraft:residual.heat": st("air"), "abyssalcraft:coraliumstone": st("prismarine"),
    # traverse
    "traverse:blue_rock_bricks": ("rlgen", "rlgen:blue_rock"), "traverse:blue_rock_bricks_slab": like("stone_slab", "deepslate_brick_slab"),
    "traverse:blue_rock_bricks_stairs": like("oak_stairs", "deepslate_brick_stairs"), "traverse:blue_rock_double_slab": ("rlgen", "rlgen:blue_rock"),
    "traverse:blue_rock_slab": like("stone_slab", "cobbled_deepslate_slab"), "traverse:fir_fence": st("spruce_fence"),
    "traverse:fir_fence_gate": like("fence_gate", "spruce_fence_gate"), "traverse:fir_log": ("rlgen_log", "rlgen:fir_log"),
    "traverse:fir_planks": st("spruce_planks"), "traverse:fir_stairs": like("oak_stairs", "spruce_stairs"),
    # better nether
    "betternether:bone_block": st("bone_block"), "betternether:bone_cincinnasite_door": like("iron_door", "iron_door"),
    "betternether:bone_reed_door": like("wooden_door", "bamboo_door"), "betternether:bone_slab_half": like("stone_slab", "smooth_quartz_slab"),
    "betternether:bone_stairs": like("oak_stairs", "smooth_quartz_stairs"), "betternether:bone_tile": st("smooth_quartz"), "betternether:bone_wall": st("diorite_wall"),
    "betternether:cincinnasite_bars": st("iron_bars"), "betternether:cincinnasite_brick_plate": like("stone_pressure_plate", "light_weighted_pressure_plate"),
    "betternether:cincinnasite_bricks_pillar": st("smooth_sandstone"), "betternether:cincinnasite_carved": st("chiseled_sandstone"),
    "betternether:cincinnasite_fire_bowl": st("campfire"), "betternether:cincinnasite_forge": like("furnace", "blast_furnace"),
    "betternether:cincinnasite_forged": st("gold_block"), "betternether:cincinnasite_frame": st("chiseled_sandstone"),
    "betternether:cincinnasite_lantern": st("lantern"), "betternether:cincinnasite_pedestal": st("chiseled_sandstone"),
    "betternether:cincinnasite_pillar": st("smooth_sandstone"), "betternether:cincinnasite_pot": st("flower_pot"),
    "betternether:cincinnasite_slab_double": st("cut_sandstone"), "betternether:cincinnasite_slab_half": like("stone_slab", "sandstone_slab"),
    "betternether:cincinnasite_stairs": like("oak_stairs", "sandstone_stairs"), "betternether:cincinnasite_tile_large": st("smooth_sandstone"),
    "betternether:cincinnasite_tile_small": st("cut_sandstone"), "betternether:cincinnasite_wall": st("sandstone_wall"),
    "betternether:lucis_mushroom": st("shroomlight"), "betternether:nether_brick_tile_large": st("nether_bricks"),
    "betternether:nether_brick_tile_slab_half": like("stone_slab", "nether_brick_slab"), "betternether:nether_brick_tile_small": st("chiseled_nether_bricks"),
    "betternether:nether_brick_tile_stairs": like("oak_stairs", "nether_brick_stairs"), "betternether:nether_brick_wall": st("nether_brick_wall"),
    "betternether:nether_grass": st("crimson_roots"), "betternether:nether_mycelium": st("crimson_nylium"), "betternether:nether_reed": st("sugar_cane"),
    "betternether:netherrack_furnace": like("furnace", "furnace"), "betternether:pig_statue_01": st("air"), "betternether:potted_plant_1": st("potted_crimson_roots"),
    "betternether:quartz_glass_framed": st("glass"), "betternether:quartz_glass_framed_pane": st("glass_pane"),
    "betternether:quartz_stained_glass": ("meta_choice", [f"{c}_stained_glass" for c in COLORS], {}),
    "betternether:quartz_stained_glass_framed": ("meta_choice", [f"{c}_stained_glass" for c in COLORS], {}),
    "betternether:red_mold": st("nether_sprouts"), "betternether:reeds_block": st("bamboo_block"),
    "betternether:reeds_button": like("wooden_button", "bamboo_button"), "betternether:reeds_fence": st("bamboo_fence"),
    "betternether:reeds_ladder": like("ladder", "ladder"), "betternether:reeds_plate": like("wooden_pressure_plate", "bamboo_pressure_plate"),
    "betternether:reeds_slab_double": st("bamboo_planks"), "betternether:reeds_slab_half": like("wooden_slab", "bamboo_slab"),
    "betternether:reeds_stairs": like("oak_stairs", "bamboo_stairs"), "betternether:stalagnate_bark": st("crimson_hyphae"),
    "betternether:stalagnate_bowl_placed": st("flower_pot"), "betternether:stalagnate_planks": st("crimson_planks"),
    "betternether:stalagnate_planks_fence": st("crimson_fence"), "betternether:stalagnate_planks_slab_half": like("wooden_slab", "crimson_slab"),
    "betternether:stalagnate_planks_stairs": like("oak_stairs", "crimson_stairs"), "betternether:stalagnate_stem": st("crimson_stem"),
    "betternether:wart_seed": st("nether_wart", age="3"),
    # waystones / bountiful (setblock commands)
    "waystones:waystone": st("lodestone"), "bountiful:bountyboard": st("lectern"),
}

# Real rlgen blocks for the modded blocks that have one (Ice and Fire / Quark world gen blocks, scripts/gen_mod_blocks.py)
RLIKE_PROPS = {"stairs": ["facing", "half", "shape"], "slab": ["type"], "ladder": ["facing"], "pillar": ["axis"], "roots": ["north", "east", "south", "west", "up"]}
def rlike(kind, vanilla_name, target):
    return ("rlike", kind, vanilla_name, target)
def rmeta(*targets):
    return ("rlgen_meta", list(targets))
QUARK_COLORS_RL = {10: "rlgen:quilted_wool_purple"}
MOD_BLOCKS.update({
    "iceandfire:ash": ("rlgen", "rlgen:ash"), "iceandfire:chared_cobblestone": ("rlgen", "rlgen:chared_cobblestone"),
    "iceandfire:chared_stone": ("rlgen", "rlgen:chared_stone"), "iceandfire:chared_dirt": ("rlgen", "rlgen:chared_dirt"),
    "iceandfire:chared_grass": ("rlgen", "rlgen:chared_grass"), "iceandfire:chared_gravel": ("rlgen", "rlgen:chared_gravel"),
    "iceandfire:chared_grass_path": ("rlgen", "rlgen:chared_grass_path"), "iceandfire:frozen_cobblestone": ("rlgen", "rlgen:frozen_cobblestone"),
    "iceandfire:frozen_stone": ("rlgen", "rlgen:frozen_stone"), "iceandfire:frozen_dirt": ("rlgen", "rlgen:frozen_dirt"),
    "iceandfire:frozen_grass": ("rlgen", "rlgen:frozen_grass"), "iceandfire:frozen_gravel": ("rlgen", "rlgen:frozen_gravel"),
    "iceandfire:frozen_grass_path": ("rlgen", "rlgen:frozen_grass_path"), "iceandfire:frozen_splinters": ("rlgen", "rlgen:frozen_splinters"),
    "iceandfire:goldpile": rmeta(*[f"rlgen:gold_pile[layers={i + 1}]" for i in range(8)]),
    "iceandfire:silverpile": rmeta(*[f"rlgen:silver_pile[layers={i + 1}]" for i in range(8)]),
    "iceandfire:silver_ore": ("rlgen", "rlgen:silver_ore"), "iceandfire:sapphire_ore": ("rlgen", "rlgen:sapphire_ore"),
    "iceandfire:fire_lily": ("rlgen", "rlgen:fire_lily"), "iceandfire:frost_lily": ("rlgen", "rlgen:frost_lily"),
    "iceandfire:myrmex_resin": rmeta("rlgen:desert_myrmex_resin", "rlgen:jungle_myrmex_resin"),
    "iceandfire:myrmex_resin_sticky": rmeta("rlgen:desert_myrmex_resin_sticky", "rlgen:jungle_myrmex_resin_sticky"),
    "iceandfire:pixie_house": ("rlgen", "rlgen:pixie_house"),
    "bountiful:bountyboard": ("rlgen", "rlgen:bounty_board"),
    "quark:crystal": rmeta(*[f"rlgen:crystal_{c}" for c in ["white", "red", "orange", "yellow", "green", "blue", "indigo", "violet", "black"]]),
    "quark:marble": rmeta("rlgen:marble", "rlgen:marble_smooth"), "quark:limestone": rmeta("rlgen:limestone", "rlgen:limestone"),
    "quark:jasper": rmeta("rlgen:jasper", "rlgen:jasper"), "quark:slate": rmeta("rlgen:slate", "rlgen:slate"),
    "quark:biome_cobblestone": rmeta("rlgen:fire_stone", "rlgen:icy_stone", "rlgen:cobbed_stone"),
    "quark:elder_prismarine": rmeta("rlgen:elder_prismarine", "minecraft:prismarine_bricks", "minecraft:dark_prismarine"),
    "quark:elder_sea_lantern": ("rlgen", "rlgen:elder_sea_lantern"), "quark:glowcelium": ("rlgen", "rlgen:glowcelium"),
    "quark:glowshroom": ("rlgen", "rlgen:glowshroom"), "quark:monster_box": ("rlgen", "rlgen:monster_box"),
    "quark:roots": rlike("roots", "vine", "roots"),
    "quark:variant_leaves": rmeta("minecraft:oak_leaves[persistent=true]", "rlgen:sakura_leaves[persistent=true]"),
    "quark:carved_wood": rmeta(*[f"rlgen:carved_wood_{w}" for w in ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak"]]),
    "quark:duskbound_block": ("rlgen", "rlgen:duskbound_block"), "quark:duskbound_lantern": ("rlgen", "rlgen:duskbound_lantern"),
    "quark:duskbound_block_stairs": rlike("stairs", "oak_stairs", "duskbound_block_stairs"),
    "quark:duskbound_block_slab": rlike("slab", "stone_slab", "duskbound_block_slab"),
    "quark:duskbound_block_slab_double": ("rlgen", "rlgen:duskbound_block_slab[type=double]"),
    "quark:world_stone_bricks": rmeta("minecraft:polished_granite", "minecraft:polished_diorite", "minecraft:polished_andesite", "minecraft:polished_basalt",
                                      "rlgen:marble_bricks", "minecraft:stone_bricks", "minecraft:stone_bricks", "minecraft:stone_bricks"),
    "quark:stone_marble_bricks_wall": ("rlgen", "rlgen:marble_bricks_wall"),
    "quark:midori_block": ("rlgen", "rlgen:midori_block"), "quark:grate": ("rlgen", "rlgen:grate"),
    "quark:quilted_wool": rmeta(*[QUARK_COLORS_RL.get(i, f"minecraft:{COLORS[i]}_wool") for i in range(16)]),
    "quark:stained_clay_tiles": rmeta(*["rlgen:stained_clay_tiles_blue" if i == 11 else f"minecraft:{COLORS[i]}_terracotta" for i in range(16)]),
    "defiledlands:stone_defiled_decoration": rmeta("rlgen:stone_bricks_defiled", "rlgen:stone_bricks_defiled_cracked", "rlgen:stone_bricks_defiled_mossy",
                                                   "rlgen:stone_defiled_mossy"),
    "defiledlands:hephaestite_ore": ("rlgen", "rlgen:hephaestite_ore"), "defiledlands:umbrium_ore": ("rlgen", "rlgen:umbrium_ore"),
    "defiledlands:scarlite_ore": ("rlgen", "rlgen:scarlite_ore"), "defiledlands:vilespine": ("rlgen", "rlgen:vilespine"),
    "defiledlands:conjuring_altar": ("rlgen", "rlgen:conjuring_altar"),
    "defiledlands:tenebra_planks": ("rlgen", "rlgen:tenebra_planks"), "defiledlands:tenebra_stairs": rlike("stairs", "oak_stairs", "tenebra_stairs"),
    "traverse:fir_planks": ("rlgen", "rlgen:fir_planks"), "traverse:fir_stairs": rlike("stairs", "oak_stairs", "fir_stairs"),
    "traverse:blue_rock_slab": rlike("slab", "stone_slab", "blue_rock_slab"),
    "betternether:nether_brick_tile_large": ("rlgen", "rlgen:nether_brick_tile_large"),
    "betternether:nether_brick_tile_small": ("rlgen", "rlgen:nether_brick_tile_small"),
    "betternether:nether_brick_tile_stairs": rlike("stairs", "oak_stairs", "nether_brick_tile_stairs"),
    "betternether:nether_brick_tile_slab_half": rlike("slab", "stone_slab", "nether_brick_tile_slab"),
    "betternether:bone_tile": ("rlgen", "rlgen:bone_tile"), "betternether:bone_wall": ("rlgen", "rlgen:bone_wall"),
    "betternether:bone_stairs": rlike("stairs", "oak_stairs", "bone_stairs"), "betternether:bone_slab_half": rlike("slab", "stone_slab", "bone_slab"),
    "betternether:reeds_block": rlike("pillar", "hay_block", "reeds_block"), "betternether:reeds_stairs": rlike("stairs", "oak_stairs", "reeds_stairs"),
    "betternether:reeds_fence": ("rlgen", "rlgen:reeds_fence"), "betternether:reeds_ladder": rlike("ladder", "ladder", "reeds_ladder"),
    "betternether:stalagnate_planks": ("rlgen", "rlgen:stalagnate_planks"), "betternether:stalagnate_bark": rlike("pillar", "hay_block", "stalagnate_bark"),
    "betternether:cincinnasite_forged": ("rlgen", "rlgen:cincinnasite_forged"),
    "betternether:cincinnasite_stairs": rlike("stairs", "oak_stairs", "cincinnasite_stairs"),
    "betternether:cincinnasite_bricks_pillar": rlike("pillar", "hay_block", "cincinnasite_pillar"),
    "betternether:cincinnasite_tile_small": ("rlgen", "rlgen:cincinnasite_tile_small"), "betternether:cincinnasite_wall": ("rlgen", "rlgen:cincinnasite_wall"),
    "betternether:cincinnasite_slab_half": rlike("slab", "stone_slab", "cincinnasite_slab"),
    "betternether:cincinnasite_lantern": ("rlgen", "rlgen:cincinnasite_lantern"), "betternether:cincinnasite_pedestal": ("rlgen", "rlgen:cincinnasite_pedestal"),
    "betternether:quartz_glass_framed_pane": ("rlgen", "rlgen:quartz_glass_framed_pane"),
})
RC_SPECIAL = {"reccomplex:generic_solid", "reccomplex:generic_space", "reccomplex:naturalFloor", "reccomplex:negativeSpace", "reccomplex:spawn_script"}
LEGACY_RC = {"reccomplex:naturalFloor": "reccomplex:generic_solid", "reccomplex:negativeSpace": "reccomplex:generic_space"}

def convert_block(name, meta):
    """-> (state string, legacy name for expressions). state "" means an RC special block."""
    if ":" not in name:
        name = "minecraft:" + name
    if name in RC_SPECIAL:
        return "", LEGACY_RC.get(name, name)
    ns, short = name.split(":", 1)
    if ns == "minecraft":
        v = vanilla(short, meta)
        if v is None:
            STATS["unknown_vanilla:" + name] += 1
            return "minecraft:air", name
        return fmt_state(*v), name
    m = MOD_BLOCKS.get(name)
    if m is None:
        STATS["unmapped:" + name] += 1
        return "minecraft:air", name
    kind = m[0]
    if kind == "state":
        return fmt_state(m[1], dict(m[2])), name
    if kind == "rlgen":
        return m[1], name
    if kind == "rlgen_log":
        axis = {0: "y", 1: "x", 2: "z"}.get((meta >> 2) & 3, "y")
        return f"{m[1]}[axis={axis}]", name
    if kind == "meta_choice":
        choices = m[1]
        return fmt_state(choices[meta % len(choices)], dict(m[2])), name
    if kind == "rlike":
        v = vanilla(m[2], meta)
        props = {k: val for k, val in (dict(v[1]) if v else {}).items() if k in RLIKE_PROPS.get(m[1], [])}
        return f"rlgen:{m[3]}" + ("[" + ",".join(f"{k}={val}" for k, val in sorted(props.items())) + "]" if props else ""), name
    if kind == "rlgen_meta":
        return m[1][meta % len(m[1])], name
    if kind == "like":
        v = vanilla(m[1], meta)
        props = dict(v[1]) if v else {}
        props.update(m[3])
        return fmt_state(m[2], props), name
    raise ValueError(m)

# ---------------------------------------------------------------- items
ITEM_MOD = {
    "iceandfire:silver_ingot": "minecraft:iron_ingot", "iceandfire:sapphire_gem": "minecraft:lapis_lazuli",
    "iceandfire:silver_nugget": "minecraft:iron_nugget", "rustic:honeycomb": "minecraft:honeycomb",
}
ENCH = {0: "protection", 1: "fire_protection", 2: "feather_falling", 3: "blast_protection", 4: "projectile_protection", 5: "respiration",
        6: "aqua_affinity", 7: "thorns", 8: "depth_strider", 9: "frost_walker", 10: "binding_curse", 16: "sharpness", 17: "smite",
        18: "bane_of_arthropods", 19: "knockback", 20: "fire_aspect", 21: "looting", 22: "sweeping_edge", 32: "efficiency", 33: "silk_touch",
        34: "unbreaking", 35: "fortune", 48: "power", 49: "punch", 50: "flame", 51: "infinity", 61: "luck_of_the_sea", 62: "lure",
        70: "mending", 71: "vanishing_curse"}
POTIONS_113 = {"minecraft:empty"}

def item_id(iid, damage):
    if isinstance(iid, (int,)) or str(iid).isdigit():
        num = int(iid)
    else:
        iid = str(iid)
        if ":" not in iid:
            iid = "minecraft:" + iid
        if not iid.startswith("minecraft:"):
            return ITEM_MOD.get(iid)
        short = iid.split(":", 1)[1]
        num = I112.get(short, B112.get(short))
        if num is None:
            return None
    s = LEGACY["items"].get(f"{num}:{damage}") or LEGACY["items"].get(f"{num}:0")
    if s is None:
        b = LEGACY["blocks"].get(f"{num}:{damage}") or LEGACY["blocks"].get(f"{num}:0")
        if b is None:
            return None
        s = b.split("[")[0]
    n = s.split(":")[-1]
    n = {"sign": "oak_sign", "grass": "short_grass", "grass_path": "dirt_path", "stone_slab": "smooth_stone_slab",
         "zombie_pigman_spawn_egg": "zombified_piglin_spawn_egg", "scute": "turtle_scute",
         "rose_red": "red_dye", "dandelion_yellow": "yellow_dye", "cactus_green": "green_dye"}.get(n, n)
    return "minecraft:" + n

def fixed_id(it):
    """Old RC structures store numeric item ids plus SG_ID_FIX_TAG with the real name."""
    for e in it.get("SG_ID_FIX_TAG", []):
        if str(e.get("tagDest", "")) == "id" and str(e.get("type", "item")) in ("item", "block"):
            return str(e["itemID"])
    return it.get("id")

def convert_item(it):
    iid = fixed_id(it)
    if iid is None:
        return None
    damage = int(it.get("Damage", 0))
    new = item_id(iid, damage)
    if new is None:
        STATS["item_dropped:" + str(iid)] += 1
        return None
    out = {"id": new, "count": int(it.get("Count", 1))}
    comps = {}
    tag = it.get("tag", {})
    if "ench" in tag or "StoredEnchantments" in tag:
        key = "minecraft:stored_enchantments" if "StoredEnchantments" in tag else "minecraft:enchantments"
        levels = {}
        for e in tag.get("ench", tag.get("StoredEnchantments", [])):
            n = ENCH.get(int(e["id"]))
            if n:
                levels["minecraft:" + n] = int(e["lvl"])
        if levels:
            comps[key] = {"levels": levels}
    if "Potion" in tag:
        comps["minecraft:potion_contents"] = {"potion": str(tag["Potion"]).replace("minecraft:", "minecraft:")}
    if "display" in tag and "Name" in tag["display"]:
        comps["minecraft:custom_name"] = json.dumps({"text": str(tag["display"]["Name"])})
    if new.endswith("_potion") or new == "minecraft:potion":
        pass
    if damage and new in ("minecraft:iron_sword", "minecraft:bow") or (damage and "sword" in new or "pickaxe" in new or "axe" in new or "shovel" in new or "hoe" in new or "helmet" in new or "chestplate" in new or "leggings" in new or "boots" in new or new.endswith(":bow") or new.endswith(":fishing_rod") or new.endswith(":shield")):
        if damage > 0:
            comps["minecraft:damage"] = damage
    if comps:
        out["components"] = comps
    return out

def convert_items(items):
    res = []
    loot = []
    if isinstance(items, dict):
        items = [items]
    for it in items:
        iid = str(fixed_id(it) or "")
        slot = int(it.get("Slot", 0))
        if iid.startswith("reccomplex:inventory_generation") or iid.startswith("reccomplex:inventoryGeneration"):
            tag = it.get("tag", {})
            key = str(tag.get("itemCollectionKey", ""))
            if not key and "display" in tag:
                key = str(tag["display"].get("Name", ""))
            kind = "single" if "single" in iid.lower() else "multi"
            loot.append({"key": key, "kind": kind, "slot": slot, "min": int(tag.get("itemCountMin", 4)), "max": int(tag.get("itemCountMax", 8)),
                         "chance": float(tag.get("itemChance", 1.0))})
            continue
        c = convert_item(it)
        if c:
            c["Slot"] = slot
            res.append(c)
    return res, loot

# ---------------------------------------------------------------- entities
ENTITY_RENAME = {"villager_golem": "iron_golem", "zombie_pigman": "zombified_piglin", "ender_crystal": "end_crystal", "snowman": "snow_golem",
                 "evocation_illager": "evoker", "vindication_illager": "vindicator", "illusion_illager": "illusioner", "Villager": "villager",
                 "quark:glass_item_frame": "glow_item_frame", "quark:colored_item_frame": "item_frame", "creeper": "creeper", "ocelot": "ocelot"}
MOD_MOBS = {"lycanitesmobs:silex": "silverfish", "lycanitesmobs:pinky": "hoglin", "lycanitesmobs:djinn": "vex", "lycanitesmobs:spectre": "phantom",
            "lycanitesmobs:darkling": "cave_spider", "lycanitesmobs:archvile": "evoker", "lycanitesmobs:behemoth": "ravager",
            "lycanitesmobs:belph": "blaze", "lycanitesmobs:vorach": "piglin_brute", "defiledlands:book_wyrm": "silverfish",
            "battletowers:golem": "iron_golem", "iceandfire:if_troll": "ravager", "familiarfauna:familiarfauna.butterfly": None,
            "testdummy:dummy": None}
PROFESSIONS = {(0, 1): "farmer", (0, 2): "fisherman", (0, 3): "shepherd", (0, 4): "fletcher", (1, 1): "librarian", (1, 2): "cartographer",
               (2, 1): "cleric", (3, 1): "armorer", (3, 2): "weaponsmith", (3, 3): "toolsmith", (4, 1): "butcher", (4, 2): "leatherworker", (5, 1): "nitwit"}
PAINT = {"Kebab": "kebab", "Aztec": "aztec", "Alban": "alban", "Aztec2": "aztec2", "Bomb": "bomb", "Plant": "plant", "Wasteland": "wasteland",
         "Pool": "pool", "Courbet": "courbet", "Sea": "sea", "Sunset": "sunset", "Creebet": "creebet", "Wanderer": "wanderer", "Graham": "graham",
         "Match": "match", "Bust": "bust", "Stage": "stage", "Void": "void", "SkullAndRoses": "skull_and_roses", "Wither": "wither",
         "Fighters": "fighters", "Pointer": "pointer", "Pigscene": "pigscene", "BurningSkull": "burning_skull", "Skeleton": "skeleton", "DonkeyKong": "donkey_kong"}

OLD_ENTITY_NAMES = {  # pre-1.11 names still found in old RC structures and spawners
    "Item": "item", "XPOrb": "experience_orb", "Painting": "painting", "ItemFrame": "item_frame", "ArmorStand": "armor_stand",
    "MinecartChest": "chest_minecart", "MinecartRideable": "minecart", "MinecartHopper": "hopper_minecart", "MinecartTNT": "tnt_minecart",
    "MinecartFurnace": "furnace_minecart", "MinecartSpawner": "spawner_minecart", "EnderCrystal": "end_crystal", "PrimedTnt": "tnt",
    "Bat": "bat", "Zombie": "zombie", "Skeleton": "skeleton", "Spider": "spider", "CaveSpider": "cave_spider", "PigZombie": "zombified_piglin",
    "LavaSlime": "magma_cube", "VillagerGolem": "iron_golem", "SnowMan": "snow_golem", "EntityHorse": "horse", "Ozelot": "ocelot",
    "MushroomCow": "mooshroom", "WitherBoss": "wither", "EnderDragon": "ender_dragon", "Creeper": "creeper", "Ghast": "ghast", "Slime": "slime",
    "Silverfish": "silverfish", "Enderman": "enderman", "Blaze": "blaze", "Witch": "witch", "Endermite": "endermite", "Guardian": "guardian",
    "Shulker": "shulker", "PolarBear": "polar_bear", "Pig": "pig", "Sheep": "sheep", "Cow": "cow", "Chicken": "chicken", "Squid": "squid",
    "Wolf": "wolf", "Rabbit": "rabbit", "Villager": "villager", "Giant": "giant", "Boat": "oak_boat", "FallingSand": "falling_block",
}

def entity_id(raw):
    raw = str(raw)
    raw = OLD_ENTITY_NAMES.get(raw.split(":")[-1] if raw.startswith("minecraft:") else raw, raw)
    if raw in MOD_MOBS:
        v = MOD_MOBS[raw]
        return ("minecraft:" + v) if v else None
    if raw in ENTITY_RENAME:
        return "minecraft:" + ENTITY_RENAME[raw]
    if ":" in raw and not raw.startswith("minecraft:"):
        STATS["entity_dropped:" + raw] += 1
        return None
    short = raw.split(":")[-1]
    return "minecraft:" + ENTITY_RENAME.get(short, short).lower()

def convert_entity(e):
    eid = entity_id(e.get("id", ""))
    if not eid:
        return None
    out = {"id": eid}
    for k in ("Rotation", "CustomName", "NoAI", "Silent", "Invulnerable", "PersistenceRequired", "Facing", "Invisible", "NoGravity", "ShowArms", "Small", "NoBasePlate"):
        if k in e:
            v = e[k]
            if k == "CustomName":
                out[k] = json.dumps({"text": str(v)})
            elif k == "Rotation":
                out[k] = [float(x) for x in v]
            else:
                out[k] = int(v)
    if eid in ("minecraft:item_frame", "minecraft:glow_item_frame"):
        if "Item" in e:
            it = convert_item(e["Item"])
            if it:
                out["Item"] = it
        out["ItemRotation"] = int(e.get("ItemRotation", 0))
        out["Facing"] = {0: 0, 1: 1, 2: 2, 3: 3}.get(int(e.get("Facing", 0)), 0)  # 1.12 horizontal index -> converted below
        f = int(e.get("Facing", 0))  # 1.12: 0 south,1 west,2 north,3 east (horizontal index)
        out["Facing"] = {0: 3, 1: 4, 2: 2, 3: 5}.get(f, 3)
        out["TileX"], out["TileY"], out["TileZ"] = int(e["TileX"]), int(e["TileY"]), int(e["TileZ"])
    if eid == "minecraft:painting":
        out["variant"] = "minecraft:" + PAINT.get(str(e.get("Motive", "Kebab")), "kebab")
        f = int(e.get("Facing", 0))
        out["facing"] = {0: 0, 1: 1, 2: 2, 3: 3}.get(f, 0)
        out["TileX"], out["TileY"], out["TileZ"] = int(e["TileX"]), int(e["TileY"]), int(e["TileZ"])
    if eid == "minecraft:item":
        if "Item" not in e:
            return None
        it = convert_item(e["Item"])
        if not it:
            return None
        out["Item"] = it
    for k in ("ArmorItems", "HandItems"):
        if k in e:
            out[k] = [convert_item(x) or {} for x in e[k]]
    if eid == "minecraft:villager":
        prof = PROFESSIONS.get((int(e.get("Profession", 0)), int(e.get("Career", 1))), "none")
        out["VillagerData"] = {"profession": "minecraft:" + prof, "level": max(1, int(e.get("CareerLevel", 1))), "type": "minecraft:plains"}
    if eid not in ("minecraft:item_frame", "minecraft:glow_item_frame", "minecraft:painting", "minecraft:armor_stand", "minecraft:item", "minecraft:end_crystal"):
        out["PersistenceRequired"] = 1
    return out

# ---------------------------------------------------------------- block entities
BANNER_PAT = {"b": "base", "bs": "stripe_bottom", "ts": "stripe_top", "ls": "stripe_left", "rs": "stripe_right", "cs": "stripe_center",
              "ms": "stripe_middle", "drs": "stripe_downright", "dls": "stripe_downleft", "ss": "small_stripes", "cr": "cross", "sc": "straight_cross",
              "bt": "triangle_bottom", "tt": "triangle_top", "bts": "triangles_bottom", "tts": "triangles_top", "ld": "diagonal_left",
              "rd": "diagonal_up_right", "lud": "diagonal_up_left", "rud": "diagonal_right", "mc": "circle", "mr": "rhombus", "vh": "half_vertical",
              "hh": "half_horizontal", "vhr": "half_vertical_right", "hhb": "half_horizontal_bottom", "bl": "square_bottom_left",
              "br": "square_bottom_right", "tl": "square_top_left", "tr": "square_top_right", "sku": "skull", "cre": "creeper", "flo": "flower",
              "moj": "mojang", "bri": "bricks", "gra": "gradient", "gru": "gradient_up", "cbo": "curly_border", "bo": "border"}
DYE_BY_DAMAGE = ["black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white"]
SKULLS = ["skeleton", "wither_skeleton", "zombie", "player", "creeper", "dragon"]
POT_PLANTS = {("minecraft:red_flower", 0): "poppy", ("minecraft:red_flower", 1): "blue_orchid", ("minecraft:red_flower", 2): "allium",
              ("minecraft:red_flower", 3): "azure_bluet", ("minecraft:red_flower", 4): "red_tulip", ("minecraft:red_flower", 5): "orange_tulip",
              ("minecraft:red_flower", 6): "white_tulip", ("minecraft:red_flower", 7): "pink_tulip", ("minecraft:red_flower", 8): "oxeye_daisy",
              ("minecraft:yellow_flower", 0): "dandelion", ("minecraft:sapling", 0): "oak_sapling", ("minecraft:sapling", 1): "spruce_sapling",
              ("minecraft:sapling", 2): "birch_sapling", ("minecraft:sapling", 3): "jungle_sapling", ("minecraft:sapling", 4): "acacia_sapling",
              ("minecraft:sapling", 5): "dark_oak_sapling", ("minecraft:red_mushroom", 0): "red_mushroom", ("minecraft:brown_mushroom", 0): "brown_mushroom",
              ("minecraft:cactus", 0): "cactus", ("minecraft:deadbush", 0): "dead_bush", ("minecraft:tallgrass", 2): "fern"}

def sign_text(te):
    lines = []
    for i in range(1, 5):
        t = str(te.get(f"Text{i}", '""'))
        lines.append(t if t.startswith("{") or t.startswith('"') else json.dumps(t))
    return {"front_text": {"messages": lines, "has_glowing_text": 0, "color": "black"},
            "back_text": {"messages": ['""'] * 4, "has_glowing_text": 0, "color": "black"}, "is_waxed": 0}

def convert_spawner(te):
    def ent(c):
        eid = entity_id(c.get("id", "pig"))
        return {"id": eid} if eid else None
    out = {}
    sd = te.get("SpawnData")
    e = ent(sd) if sd is not None and "id" in sd else None
    if e is None and "EntityId" in te:
        e = ent({"id": te["EntityId"]})
    if e:
        out["SpawnData"] = {"entity": e}
    pots = []
    for p in te.get("SpawnPotentials", []):
        pe = ent(p.get("Entity", {}))
        if pe:
            pots.append({"weight": int(p.get("Weight", 1)), "data": {"entity": pe}})
    if pots:
        out["SpawnPotentials"] = pots
    for k in ("Delay", "MinSpawnDelay", "MaxSpawnDelay", "SpawnCount", "MaxNearbyEntities", "RequiredPlayerRange", "SpawnRange"):
        if k in te:
            out[k] = int(te[k])
    return out

def convert_tile(te, state):
    """-> (new state or None, be nbt dict or None, loot list, script)."""
    tid = str(te.get("id", ""))
    short = tid.split(":")[-1]
    nbt, loot, script, newstate = None, [], None, None
    if "Items" in te:
        items, loot = convert_items(te["Items"])
        nbt = {"Items": items}
    if short in ("sign",) or tid == "variedcommodities:sign":
        nbt = sign_text(te)
    elif short == "bed":
        color = COLORS[int(te.get("color", 14)) % 16]
        if state.startswith("minecraft:red_bed"):
            newstate = state.replace("minecraft:red_bed", f"minecraft:{color}_bed")
    elif short == "banner":
        base = DYE_BY_DAMAGE[int(te.get("Base", 0)) % 16]
        if "white_banner" in state or "white_wall_banner" in state:
            newstate = state.replace("white_", f"{base}_", 1)
        pats = []
        for p in te.get("Patterns", []):
            pn = BANNER_PAT.get(str(p.get("Pattern")))
            if pn:
                pats.append({"pattern": "minecraft:" + pn, "color": DYE_BY_DAMAGE[int(p.get("Color", 0)) % 16]})
        nbt = {"patterns": pats}
    elif short == "skull":
        kind = SKULLS[int(te.get("SkullType", 0)) % 6]
        if "wall_skull" in state:
            newstate = re.sub(r"minecraft:skeleton_wall_skull", f"minecraft:{kind}_wall_{'head' if kind in ('zombie','player','creeper','dragon') else 'skull'}", state)
        else:
            rot = int(te.get("Rot", 0))
            newstate = f"minecraft:{kind}_{'head' if kind in ('zombie','player','creeper','dragon') else 'skull'}[rotation={rot}]"
        nbt = {}
    elif short == "flower_pot":
        item = str(te.get("Item", ""))
        if item and item != "minecraft:air":
            if ":" not in item:
                item = "minecraft:" + item
            plant = POT_PLANTS.get((item, int(te.get("Data", 0))))
            if plant:
                newstate = f"minecraft:potted_{plant}"
        nbt = None
    elif short in ("mob_spawner",):
        nbt = convert_spawner(te)
    elif tid in ("minecraft:rcspawnscript", "RCSpawnScript"):
        script = convert_script(te.get("script", {}))
        nbt = None
    return newstate, nbt, loot, script

# ---------------------------------------------------------------- RC scripts
SNBT_PARSE = nbtlib.parse_nbt

def convert_command(cmd):
    c = cmd.strip().lstrip("/")
    m = re.match(r"summon\s+(\S+)(?:\s+(\S+)\s+(\S+)\s+(\S+))?(?:\s+(\{.*\}))?\s*$", c)
    if m:
        eid = entity_id(m.group(1))
        if not eid:
            return None
        nbt = {}
        if m.group(5):
            try:
                raw = SNBT_PARSE(m.group(5))
                raw["id"] = m.group(1)
                conv = convert_entity(raw)
                if conv:
                    nbt = {k: v for k, v in conv.items() if k != "id"}
            except Exception:
                STATS["bad_summon_nbt"] += 1
        return {"type": "summon", "entity": eid, "offset": [m.group(2) or "~", m.group(3) or "~", m.group(4) or "~"], "nbt": nbt}
    m = re.match(r"setblock\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)(?:\s+(\d+))?(?:\s+(replace|keep|destroy))?(?:\s+(\S+=\S+))?(?:\s+(\{.*\}))?\s*$", c)
    if m:
        name = m.group(4)
        meta = int(m.group(5) or 0)
        state, legacy = convert_block(name, meta)
        if state == "":
            state = "minecraft:air"
        nbt = None
        if m.group(8):
            try:
                raw = SNBT_PARSE(m.group(8))
                raw["id"] = name
                newstate, nbt, loot, _ = convert_tile(raw, state)
                state = newstate or state
            except Exception:
                STATS["bad_setblock_nbt"] += 1
        return {"type": "setblock", "offset": [m.group(1), m.group(2), m.group(3)], "state": state, "nbt": nbt}
    STATS["command_dropped:" + c.split(" ")[0]] += 1
    return None

def convert_script(s):
    out = {"environment": str(s.get("environmentMatcher", "")), "scripts": []}
    for sc in s.get("scripts", []):
        sid = str(sc.get("id"))
        inner = sc.get("script", {})
        if sid == "command":
            cmds = []
            for c in inner.get("commands", []):
                cc = convert_command(str(c.get("command", ""))) or {"type": "none"}  # keep the weight of commands we can't run
                w = c.get("weight")
                cc["weight"] = float(w) if isinstance(w, (float, nbtlib.tag.Double, nbtlib.tag.Float)) else (int(w) * 0.01 if w is not None else 1.0)
                cmds.append(cc)
            out["scripts"].append({"type": "command", "commands": cmds})
        elif sid == "strucGen":
            out["scripts"].append({"type": "structure", "simple": int(inner.get("simpleMode", 1)) != 0,
                                   "structures": [str(x) for x in inner.get("structures", [])], "list": str(inner.get("structureListID", "")),
                                   "shift": [int(inner.get("structureShift_x", 0)), int(inner.get("structureShift_y", 0)), int(inner.get("structureShift_z", 0))],
                                   "rotation": int(inner.get("structureRotation", 0)) if "structureRotation" in inner else None,
                                   "mirror": bool(int(inner.get("structureMirror", 0))) if "structureMirror" in inner else None,
                                   "front": str(inner["front"]) if "front" in inner else None})
        elif sid == "multi":
            out["scripts"].append({"type": "multi", "inner": convert_script(inner)})
        elif sid == "mazeGen":
            out["scripts"].append({"type": "maze", "maze": inner})  # RC's own compound, read by MazeScript
        elif sid == "holder":
            STATS["script_holder"] += 1
        else:
            STATS["script_" + sid] += 1
    return out

# ---------------------------------------------------------------- structures
def load_nbt(b):
    try:
        b = gzip.decompress(b)
    except Exception:
        pass
    return nbtlib.File.parse(io.BytesIO(b))

def unpack(data, bits, length):
    if bits == 0:
        return [0] * length
    vals, cur, saved = [], 0, 0
    for byte in data:
        cur = (cur << 8) | (byte & 0xFF)
        saved += 8
        while saved >= bits and len(vals) < length:
            vals.append(cur >> (saved - bits) & ((1 << bits) - 1))
            saved -= bits
            cur &= (1 << saved) - 1
    return vals

def to_nbt(o):
    if isinstance(o, dict):
        return Compound({k: to_nbt(v) for k, v in o.items() if v is not None})
    if isinstance(o, bool):
        return Byte(int(o))
    if isinstance(o, int):
        return Int(o)
    if isinstance(o, float):
        return Double(o)
    if isinstance(o, str):
        return String(o)
    if isinstance(o, list):
        if not o:
            return List[Compound]([])
        conv = [to_nbt(x) for x in o]
        return List[type(conv[0])](conv)
    if isinstance(o, nbtlib.tag.Base):
        return o
    raise TypeError(type(o))

DOOR_UPPER = re.compile(r"_door\[")

def fix_two_part_blocks(palette, blocks, w, h, l):
    """1.12 doors / double plants store half the state in each block; copy it across like 1.12's getActualState."""
    def props(s):
        return parse_state(s)[1] if "[" in s else {}
    cache = {}
    for z in range(l):
        for x in range(w):
            for y in range(h - 1):
                i = (z * h + y) * w + x
                j = (z * h + y + 1) * w + x
                lo, up = palette[blocks[i]]["state"], palette[blocks[j]]["state"]
                if "_door[" in lo and "half=lower" in lo and "_door[" in up and "half=upper" in up:
                    pl, pu = props(lo), props(up)
                    name = lo.split("[")[0]
                    nl = dict(pl, hinge=pu.get("hinge", "left"))
                    nu = dict(pu, facing=pl.get("facing", "north"), open=pl.get("open", "false"))
                    for idx, st_ in ((i, nl), (j, nu)):
                        s = name + "[" + ",".join(f"{k}={v}" for k, v in sorted(st_.items())) + "]"
                        key = (palette[blocks[idx]]["legacy"], palette[blocks[idx]]["meta"], s)
                        if key not in cache:
                            cache[key] = len(palette)
                            palette.append({"legacy": key[0], "meta": key[1], "state": s})
                        blocks[idx] = cache[key]
                elif lo.startswith("minecraft:") and "half=lower" in lo and up.startswith("minecraft:tall_grass[half=upper]"):
                    s = lo.replace("half=lower", "half=upper")
                    key = (palette[blocks[j]]["legacy"], palette[blocks[j]]["meta"], s)
                    if key not in cache:
                        cache[key] = len(palette)
                        palette.append({"legacy": key[0], "meta": key[1], "state": s})
                    blocks[j] = cache[key]

def add_dest_states(o):
    """Replace transformers / block presets name 1.12 block+metadata; add the 1.21 state as "_state"."""
    if isinstance(o, dict):
        if "block" in o and "metadata" in o and isinstance(o["block"], str):
            try:
                o["_state"] = convert_block(o["block"], int(o["metadata"]))[0] or "minecraft:air"
            except Exception:
                STATS["dest_unmapped:" + o["block"]] += 1
                o["_state"] = "minecraft:air"
        for v in o.values():
            add_dest_states(v)
    elif isinstance(o, list):
        for v in o:
            add_dest_states(v)
    return o

def convert_structure(sid, raw_json, nbt):
    raw_json = json.dumps(add_dest_states(json.loads(raw_json)))
    bc = nbt["blockCollection"]
    w, h, l = int(bc["width"]), int(bc["height"]), int(bc["length"])
    mapping = [str(x) for x in bc["mapping"]]
    if "blocksCompressed" in bc["blocks"]:
        c = bc["blocks"]["blocksCompressed"]
        ids = unpack(bytes(b & 0xFF for b in c["data_bytes"]), int(c["data_bitLength"]), int(c["data_length"]))
    elif "blockBytes" in bc["blocks"]:
        ids = [b & 0xFF for b in bc["blocks"]["blockBytes"]]
    else:
        ids = [int(b) for b in bc["blocks"]["blockInts"]]
    metas = [m & 0xFF for m in bc["metadata"]]
    palette, index, blocks = [], {}, []
    for bid, meta in zip(ids, metas):
        key = (mapping[bid], meta)
        if key not in index:
            state, legacy = convert_block(*key)
            index[key] = len(palette)
            palette.append({"legacy": legacy, "meta": meta, "state": state})
        blocks.append(index[key])
    fix_two_part_blocks(palette, blocks, w, h, l)
    tiles = []
    for te in nbt.get("tileEntities", []):
        x, y, z = int(te["x"]), int(te["y"]), int(te["z"])
        if not (0 <= x < w and 0 <= y < h and 0 <= z < l):
            continue
        i = (z * h + y) * w + x
        state = palette[blocks[i]]["state"]
        newstate, be, loot, script = convert_tile(te, state)
        if newstate and newstate != state:
            p = dict(palette[blocks[i]], state=newstate)
            palette.append(p)
            blocks[i] = len(palette) - 1
        entry = {"x": x, "y": y, "z": z}
        if be is not None:
            entry["nbt"] = be
        if loot:
            entry["loot"] = loot
        if script and script["scripts"]:
            entry["script"] = script
        if len(entry) > 3:
            tiles.append(entry)
    ents = []
    for e in nbt.get("entities", []):
        conv = convert_entity(e)
        if conv and "Pos" in e:
            pos = [float(v) for v in e["Pos"]]
            ents.append({"pos": pos, "nbt": conv})
    out = Compound({
        "json": String(raw_json),
        "size": IntArray([w, h, l]),
        "palette": List[Compound]([Compound({"legacy": String(p["legacy"]), "meta": Byte(p["meta"]), "state": String(p["state"])}) for p in palette]),
        "blocks": IntArray(blocks),
        "tiles": to_nbt(tiles) if tiles else List[Compound]([]),
        "entities": to_nbt(ents) if ents else List[Compound]([]),
    })
    os.makedirs(f"{OUT}/structures", exist_ok=True)
    nbtlib.File(out).save(f"{OUT}/structures/{sid.lower()}.nbt", gzipped=True)

def collect(pattern_dirs, ext):
    found = {}
    for d in pattern_dirs:
        for f in glob.glob(f"{d}/**/*.{ext}", recursive=True):
            found[os.path.splitext(os.path.basename(f))[0]] = f  # later dirs override earlier (instance over jar)
    return found

def main():
    dirs = [RCJAR, INSTANCE]
    structures = collect(dirs, "rcst")
    index = {}
    for sid, f in sorted(structures.items()):
        z = zipfile.ZipFile(f)
        raw = z.read("structure.json").decode("utf-8")
        try:
            convert_structure(sid, raw, load_nbt(z.read("worldData.nbt")))
            index[sid.lower()] = sid
        except Exception as ex:
            STATS["structure_failed"] += 1
            print("FAILED", sid, ex)
            if os.environ.get("RC_TRACE"): import traceback; traceback.print_exc()
    json.dump(index, open(f"{OUT}/structures/index.json", "w"), indent=1, sort_keys=True)
    # inventory generators: components grouped by inventoryGeneratorID
    loot = collections.defaultdict(list)
    for gid, f in sorted(collect(dirs, "rcig").items()):
        d = json.load(open(f))
        table = d.get("inventoryGeneratorID") or gid
        items = d.get("items")
        if items is None:  # version 1: "contents" with weightedItemStack
            items = [{"weight": c.get("weight", 1) / 100.0, "min": c.get("min", 1), "max": c.get("max", 1), "item": c.get("itemStack", c.get("item", {}))}
                     for c in d.get("contents", [])]
        comp = []
        for it in items:
            st_ = it.get("item", {})
            new = item_id(st_.get("id", "minecraft:air"), int(st_.get("damage", st_.get("Damage", 0))))
            if not new:
                STATS["loot_item_dropped:" + str(st_.get("id"))] += 1
                continue
            entry = {"id": new, "weight": float(it.get("weight", 1.0)), "min": int(it.get("min", 1)), "max": int(it.get("max", 1))}
            if "tag" in st_:
                fake = {"id": st_["id"], "Damage": st_.get("damage", 0), "Count": 1, "tag": SNBT_PARSE(json.dumps(st_["tag"])) if isinstance(st_["tag"], dict) else {}}
                try:
                    conv = convert_item(fake)
                    if conv and "components" in conv:
                        entry["components"] = conv["components"]
                except Exception:
                    pass
            comp.append(entry)
        loot[table].append({"items": comp, "minItems": d.get("minItems"), "maxItems": d.get("maxItems")})
    os.makedirs(f"{OUT}/loot", exist_ok=True)
    for table, comps in loot.items():
        json.dump({"components": comps}, open(f"{OUT}/loot/{table.lower()}.json", "w"), indent=1)
    # presets and categories, copied as-is
    for ext, sub in (("rcnc", "categories"), ("rcbp", "presets/biome"), ("rcdp", "presets/dimension"), ("rcgp", "presets/placer"),
                     ("rcmt", "presets/transformer"), ("rcbm", "presets/blocks")):
        os.makedirs(f"{OUT}/{sub}", exist_ok=True)
        for pid, f in collect(dirs, ext).items():
            data = add_dest_states(json.load(open(f)))
            json.dump(data, open(f"{OUT}/{sub}/{pid.lower()}.json", "w"), indent=1)
    # 1.21 block -> 1.12 name/meta, for id= / metadata= tests on world blocks
    rev = {}
    by_id = {v: k for k, v in B112.items()}
    for key, st_ in sorted(LEGACY["blocks"].items(), key=lambda kv: [int(x) for x in kv[0].split(":")]):
        bid, meta = (int(x) for x in key.split(":"))
        n, _ = parse_state(st_)
        n = RENAME_113.get(n, n)
        if bid in by_id and n not in rev:
            rev[n] = ["minecraft:" + by_id[bid], meta]
    json.dump(rev, open(f"{OUT}/legacy_ids.json", "w"), sort_keys=True)
    print(len(index), "structures,", len(loot), "loot tables")
    for k, v in sorted(STATS.items(), key=lambda kv: -kv[1])[:60]:
        print(v, k)

if __name__ == "__main__":
    main()
