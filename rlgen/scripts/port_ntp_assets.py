#!/usr/bin/env python3
"""No Tree Punching 2.0.21 assets/data for the 1.21 port (notreepunching mod in the rlgen jar).

usage: port_ntp_assets.py <decompiled notreepunching jar dir> <rlgen/src/main/resources>

Textures/sounds (1.21 folder names), models, blockstates, en_us.json (1.12 .lang + RLCraft display names), loot tables, tags and
recipes: the 1.12 json recipes, the generated Saw + Log -> Planks recipes (replaceLogRecipes: the vanilla log -> planks and stick
recipes are switched off), the smelting recipes of ModRecipes.init and RLCraft's Shiv.zs changes (no fire starter recipe, 4 rocks ->
1 sandstone / red sandstone, the sand -> sandstone recipes removed, no furnace recipe for the ceramic small vessel).
"""
import json
import os
import re
import shutil
import sys

DEC, RES = sys.argv[1], sys.argv[2]
SRC = os.path.join(DEC, 'assets', 'notreepunching')
A = os.path.join(RES, 'assets', 'notreepunching')
D = os.path.join(RES, 'data', 'notreepunching')
MC = os.path.join(RES, 'data', 'minecraft')
STONES = ['stone', 'andesite', 'granite', 'diorite', 'sandstone', 'red_sandstone', 'marble', 'limestone', 'basalt']
COBBLES = ['andesite', 'granite', 'diorite', 'marble', 'limestone', 'basalt']
METALS = {'iron': 'minecraft:iron_ingot', 'gold': 'minecraft:gold_ingot', 'diamond': 'minecraft:diamond'}


def w(path, obj):
   os.makedirs(os.path.dirname(path), exist_ok=True)
   if os.sep + 'tags' + os.sep in path and os.path.exists(path) and 'values' in obj:
      old = json.load(open(path))
      for v in obj['values']:
         if v not in old.setdefault('values', []):
            old['values'].append(v)
      obj = old
   with open(path, 'w') as f:
      json.dump(obj, f, indent=2)
      f.write('\n')


def cp(src, dst):
   os.makedirs(os.path.dirname(dst), exist_ok=True)
   shutil.copyfile(src, dst)


for root, _, files in os.walk(os.path.join(SRC, 'textures', 'items')):
   for fn in files:
      rel = os.path.relpath(os.path.join(root, fn), os.path.join(SRC, 'textures', 'items'))
      cp(os.path.join(root, fn), os.path.join(A, 'textures', 'item', rel))
for fn in os.listdir(os.path.join(SRC, 'textures', 'blocks', 'cobblestone')):
   cp(os.path.join(SRC, 'textures', 'blocks', 'cobblestone', fn), os.path.join(A, 'textures', 'block', 'cobblestone', fn))
for fn in os.listdir(os.path.join(SRC, 'sounds', 'knapping')):
   cp(os.path.join(SRC, 'sounds', 'knapping', fn), os.path.join(A, 'sounds', 'knapping', fn))
w(os.path.join(A, 'sounds.json'), {'knapping': {'subtitle': 'notreepunching.subtitle.knapping',
                                                'sounds': ['notreepunching:knapping/%d' % i for i in range(1, 5)]}})

# ---- item models ----
def item_model(name, handheld=False):
   w(os.path.join(A, 'models', 'item', name + '.json'), {'parent': 'item/handheld' if handheld else 'item/generated',
                                                         'textures': {'layer0': 'notreepunching:item/' + name}})


for n in ('grass_fiber', 'grass_string', 'flint_shard', 'clay_brick'):
   item_model(n)
item_model('clay_tool', True)
item_model('fire_starter', True)
for s in STONES:
   item_model('rock/' + s)
for t in ('pickaxe', 'axe', 'shovel', 'hoe', 'knife'):
   item_model(t + '/flint', True)
for t in ('knife', 'mattock', 'saw'):
   for m in METALS:
      item_model(t + '/' + m, True)

# ---- blocks: loose rocks (1.12 loose_rock model, stone texture per type) and cobblestone ----
rock_model = json.load(open(os.path.join(SRC, 'models', 'block', 'loose_rock.json')))
rock_model.pop('__comment', None)
TEX = {'quark:blocks/stone_marble': 'rlgen:block/marble', 'quark:blocks/stone_limestone': 'rlgen:block/limestone',
       'quark:blocks/stone_basalt': 'rlgen:block/basalt', 'minecraft:blocks/stone_andesite': 'minecraft:block/andesite',
       'minecraft:blocks/stone_diorite': 'minecraft:block/diorite', 'minecraft:blocks/stone_granite': 'minecraft:block/granite'}
for s in STONES:
   bs = json.load(open(os.path.join(SRC, 'blockstates', 'loose_rock', s + '.json')))
   tex = bs['defaults']['textures']['all']
   tex = TEX.get(tex, re.sub(r'^minecraft:blocks/', 'minecraft:block/', tex))
   m = dict(rock_model)
   m['parent'] = 'block/block'
   m['textures'] = {'all': tex, 'particle': tex}
   w(os.path.join(A, 'models', 'block', 'loose_rock', s + '.json'), m)
   w(os.path.join(A, 'blockstates', 'loose_rock', s + '.json'), {'variants': {'': {'model': 'notreepunching:block/loose_rock/' + s}}})
   w(os.path.join(D, 'loot_table', 'blocks', 'loose_rock', s + '.json'), {'type': 'minecraft:block', 'pools': [
      {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'notreepunching:rock/' + s}]}]})
for s in COBBLES:
   w(os.path.join(A, 'models', 'block', 'cobblestone', s + '.json'), {'parent': 'block/cube_all', 'textures': {'all': 'notreepunching:block/cobblestone/' + s}})
   w(os.path.join(A, 'blockstates', 'cobblestone', s + '.json'), {'variants': {'': {'model': 'notreepunching:block/cobblestone/' + s}}})
   w(os.path.join(A, 'models', 'item', 'cobblestone', s + '.json'), {'parent': 'notreepunching:block/cobblestone/' + s})
   w(os.path.join(D, 'loot_table', 'blocks', 'cobblestone', s + '.json'), {'type': 'minecraft:block', 'pools': [
      {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'notreepunching:cobblestone/' + s}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

# ---- fire pit, pottery, ceramic vessels/bucket (ModBlocks, ModItems, ModGuiHandler textures) ----
for fn in ('fire_pit.png', 'large_vessel.png', 'small_vessel.png'):
   cp(os.path.join(SRC, 'textures', 'gui', fn), os.path.join(A, 'textures', 'gui', fn))
cp(os.path.join(SRC, 'textures', 'blocks', 'ceramic.png'), os.path.join(A, 'textures', 'block', 'ceramic.png'))


def block_model(src, dst, textures):
   m = json.load(open(os.path.join(SRC, 'models', 'block', src + '.json')))
   m.pop('__comment', None)
   m.pop('credit', None)
   for e in m.get('elements', []):
      e.pop('name', None)
      e.pop('type', None)
   m['parent'] = 'block/block'
   m['textures'] = textures
   w(os.path.join(A, 'models', 'block', dst + '.json'), m)


def drop_self(name):
   w(os.path.join(D, 'loot_table', 'blocks', name + '.json'), {'type': 'minecraft:block', 'pools': [
      {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'notreepunching:' + name}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})


# fire_pit: one model for both lit states (StateMapper ignores LIT), drops 2 sticks
block_model('fire_pit', 'fire_pit', {'bark': 'minecraft:block/oak_log', 'end': 'minecraft:block/oak_log_top', 'particle': 'minecraft:block/oak_log'})
w(os.path.join(A, 'blockstates', 'fire_pit.json'), {'variants': {'lit=false': {'model': 'notreepunching:block/fire_pit'},
                                                               'lit=true': {'model': 'notreepunching:block/fire_pit'}}})
w(os.path.join(D, 'loot_table', 'blocks', 'fire_pit.json'), {'type': 'minecraft:block', 'pools': [
   {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:stick',
                             'functions': [{'function': 'minecraft:set_count', 'count': 2}]}]}]})
# pottery/<type>: clay texture; item models: large vessel = block model (forge:default-block), the others the 1.12 item sprites
POTTERY = ['worked', 'large_vessel', 'small_vessel', 'bucket', 'flower_pot']
CLAY = {'all': 'minecraft:block/clay', 'particle': 'minecraft:block/clay'}
for t in POTTERY:
   block_model('pottery/' + t, 'pottery/' + t, CLAY)
   w(os.path.join(A, 'blockstates', 'pottery', t + '.json'), {'variants': {'': {'model': 'notreepunching:block/pottery/' + t}}})
   if t == 'worked':
      w(os.path.join(D, 'loot_table', 'blocks', 'pottery', 'worked.json'), {'type': 'minecraft:block', 'pools': [
         {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:clay_ball',
                                   'functions': [{'function': 'minecraft:set_count', 'count': 3}]}],
          'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
      continue
   drop_self('pottery/' + t)
   if t == 'large_vessel':
      w(os.path.join(A, 'models', 'item', 'pottery', t + '.json'), {'parent': 'notreepunching:block/pottery/large_vessel'})
   else:
      im = json.load(open(os.path.join(SRC, 'models', 'item', 'pottery', t + '.json')))
      item_tex = im['textures']['layer0'].replace('notreepunching:items/', 'notreepunching:item/')
      w(os.path.join(A, 'models', 'item', 'pottery', t + '.json'), {'parent': 'item/generated', 'textures': {'layer0': item_tex}})
# ceramic_large_vessel: the large vessel model with the ceramic texture
block_model('pottery/large_vessel', 'ceramic_large_vessel', {'all': 'notreepunching:block/ceramic', 'particle': 'notreepunching:block/ceramic'})
w(os.path.join(A, 'blockstates', 'ceramic_large_vessel.json'), {'variants': {'': {'model': 'notreepunching:block/ceramic_large_vessel'}}})
w(os.path.join(A, 'models', 'item', 'ceramic_large_vessel.json'), {'parent': 'notreepunching:block/ceramic_large_vessel'})
drop_self('ceramic_large_vessel')
item_model('ceramic_small_vessel')
# ceramic_bucket: forge:forgebucket (base, masked fluid, cover drawn on top, flipGas) = neoforge:fluid_container with cover_is_mask false
w(os.path.join(A, 'models', 'item', 'ceramic_bucket.json'), {'loader': 'neoforge:fluid_container', 'parent': 'neoforge:item/default',
                                                             'fluid': 'minecraft:empty', 'flip_gas': True, 'cover_is_mask': False,
                                                             'textures': {'base': 'notreepunching:item/ceramic_bucket',
                                                                          'fluid': 'neoforge:item/mask/bucket_fluid',
                                                                          'cover': 'notreepunching:item/ceramic_bucket_cover'}})
w(os.path.join(MC, 'tags', 'block', 'mineable', 'shovel.json'), {'values': ['notreepunching:pottery/' + t for t in POTTERY]})
w(os.path.join(MC, 'tags', 'block', 'mineable', 'pickaxe.json'), {'values': ['notreepunching:ceramic_large_vessel']})
w(os.path.join(MC, 'tags', 'item', 'enchantable', 'durability.json'), {'values': ['notreepunching:clay_tool']})
# ModRecipes.init: pottery -> fired type in the furnace (0.1 xp); Shiv.zs furnace.remove(<notreepunching:ceramic_small_vessel>)
FIRED = {'large_vessel': 'notreepunching:ceramic_large_vessel', 'bucket': 'notreepunching:ceramic_bucket', 'flower_pot': 'minecraft:flower_pot'}
for t, out in FIRED.items():
   w(os.path.join(D, 'recipe', 'smelting', 'pottery_' + t + '.json'), {'type': 'minecraft:smelting', 'category': 'misc',
                                                                     'ingredient': {'item': 'notreepunching:pottery/' + t}, 'result': {'id': out},
                                                                     'experience': 0.1, 'cookingtime': 200})

# ---- lang ----
lang = {}
for line in open(os.path.join(SRC, 'lang', 'en_us.lang'), encoding='utf-8'):
   line = line.strip()
   if not line or line.startswith('#') or '=' not in line:
      continue
   k, v = line.split('=', 1)
   m = re.match(r'(item|tile)\.notreepunching:(.*)\.name$', k)
   if m:
      lang[('block' if m.group(1) == 'tile' else 'item') + '.notreepunching.' + m.group(2)] = v
   else:
      lang[k] = v
lang['item.notreepunching.rock.basalt'] = 'Basalt Rock'  # Shiv.zs displayName
lang['block.notreepunching.cobblestone.basalt'] = 'Basalt Cobblestone'
for s in STONES:
   lang['block.notreepunching.loose_rock.' + s] = lang.get('item.notreepunching.rock.' + s, 'Rock')
lang['itemGroup.notreepunching.items'] = 'No Tree Punching | Items'
lang['itemGroup.notreepunching.tools'] = 'No Tree Punching | Tools'
lang['notreepunching.subtitle.knapping'] = 'Flint knapped'
w(os.path.join(A, 'lang', 'en_us.json'), dict(sorted(lang.items())))

# ---- tags ----
w(os.path.join(MC, 'tags', 'block', 'mineable', 'pickaxe.json'), {'values': ['notreepunching:cobblestone/' + s for s in COBBLES]})
w(os.path.join(D, 'tags', 'block', 'mineable', 'mattock.json'), {'values': ['#minecraft:mineable/axe', '#minecraft:mineable/shovel']})
w(os.path.join(MC, 'tags', 'item', 'pickaxes.json'), {'values': ['notreepunching:pickaxe/flint']})
w(os.path.join(MC, 'tags', 'item', 'axes.json'), {'values': ['notreepunching:axe/flint']})
w(os.path.join(MC, 'tags', 'item', 'shovels.json'), {'values': ['notreepunching:shovel/flint']})
w(os.path.join(MC, 'tags', 'item', 'hoes.json'), {'values': ['notreepunching:hoe/flint']})
knives = ['notreepunching:knife/flint'] + ['notreepunching:knife/' + m for m in METALS]
others = ['notreepunching:%s/%s' % (t, m) for t in ('saw', 'mattock') for m in METALS]
w(os.path.join(MC, 'tags', 'item', 'enchantable', 'durability.json'), {'values': knives + others})
w(os.path.join(MC, 'tags', 'item', 'enchantable', 'sharp_weapon.json'), {'values': knives})
w(os.path.join(MC, 'tags', 'item', 'enchantable', 'weapon.json'), {'values': knives})
w(os.path.join(MC, 'tags', 'item', 'enchantable', 'mining.json'), {'values': others})
w(os.path.join(RES, 'data', 'c', 'tags', 'item', 'strings.json'), {'values': ['notreepunching:grass_string']})

# ---- recipes ----
ORE = {'shardFlint': {'item': 'notreepunching:flint_shard'}, 'stickWood': {'tag': 'c:rods/wooden'}, 'string': {'tag': 'c:strings'},
       'ingotIron': {'item': 'minecraft:iron_ingot'}, 'ingotGold': {'item': 'minecraft:gold_ingot'}, 'gemDiamond': {'item': 'minecraft:diamond'},
       'toolClay': {'item': 'notreepunching:clay_tool'}, 'toolSaw': {'tag': 'notreepunching:saws'}, 'logWood': {'tag': 'minecraft:logs'},
       'plankWood': {'tag': 'minecraft:planks'}}
for s in STONES:
   ORE['rock' + ''.join(p.capitalize() for p in s.split('_'))] = {'item': 'notreepunching:rock/' + s}
w(os.path.join(D, 'tags', 'item', 'saws.json'), {'values': ['notreepunching:saw/' + m for m in METALS]})
SKIP = {'fire_starter', 'rocks_to_sandstone', 'rocks_to_red_sandstone'}  # Shiv.zs


def ing(i):
   if i.get('type') == 'forge:ore_dict':
      return ORE[i['ore']]
   return {'item': i['item']}


for root, _, files in os.walk(os.path.join(SRC, 'recipes')):
   for fn in files:
      name = fn[:-5]
      if name in SKIP or name.startswith('_'):
         continue
      r = json.load(open(os.path.join(root, fn)))
      res = {'id': r['result']['item'], 'count': r['result'].get('count', 1)}
      if r['type'] == 'forge:ore_shaped':
         o = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': r['pattern'], 'key': {k: ing(v) for k, v in r['key'].items()}, 'result': res}
      else:
         o = {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [ing(i) for i in r['ingredients']], 'result': res}
      w(os.path.join(D, 'recipe', os.path.relpath(root, os.path.join(SRC, 'recipes')), fn), o)
# ModRecipes.registerRecipes: cobblestone of the non-default stones
for s in ('marble', 'limestone', 'basalt'):
   w(os.path.join(D, 'recipe', 'blocks', 'cobblestone_' + s + '.json'), {'type': 'minecraft:crafting_shaped', 'category': 'building', 'pattern': ['RR', 'RR'],
                                                                         'key': {'R': {'item': 'notreepunching:rock/' + s}}, 'result': {'id': 'notreepunching:cobblestone/' + s}})
# Shiv.zs lolarecipe62/63: 4 rocks -> 1 sandstone / red sandstone
for s in ('sandstone', 'red_sandstone'):
   w(os.path.join(D, 'recipe', 'blocks', 'rocks_to_' + s + '.json'), {'type': 'minecraft:crafting_shaped', 'category': 'building', 'pattern': ['RR', 'RR'],
                                                                      'key': {'R': {'item': 'notreepunching:rock/' + s}}, 'result': {'id': 'minecraft:' + s, 'count': 1}})
# ModRecipes.init smelting
SMELT = {'cobblestone/andesite': 'minecraft:andesite', 'cobblestone/granite': 'minecraft:granite', 'cobblestone/diorite': 'minecraft:diorite',
         'cobblestone/marble': 'rlgen:marble', 'cobblestone/limestone': 'rlgen:limestone', 'cobblestone/basalt': 'minecraft:smooth_basalt',
         'grass_string': 'minecraft:string', 'clay_brick': 'minecraft:brick'}
for src, out in SMELT.items():
   w(os.path.join(D, 'recipe', 'smelting', src.replace('/', '_') + '.json'), {'type': 'minecraft:smelting', 'category': 'misc',
                                                                             'ingredient': {'item': 'notreepunching:' + src}, 'result': {'id': out},
                                                                             'experience': 0.1, 'cookingtime': 200})
# WoodRecipeHandler (replaceLogRecipes): Saw + Log -> Planks for every log -> planks recipe, the originals and minecraft:stick off
PLANKS = {'oak': '#minecraft:oak_logs', 'spruce': '#minecraft:spruce_logs', 'birch': '#minecraft:birch_logs', 'jungle': '#minecraft:jungle_logs',
          'acacia': '#minecraft:acacia_logs', 'dark_oak': '#minecraft:dark_oak_logs', 'mangrove': '#minecraft:mangrove_logs',
          'cherry': '#minecraft:cherry_logs', 'crimson': '#minecraft:crimson_stems', 'warped': '#minecraft:warped_stems',
          'bamboo': '#minecraft:bamboo_blocks'}
OFF = {'neoforge:conditions': [{'type': 'neoforge:false'}]}
for i, (wood, tag) in enumerate(PLANKS.items(), 1):
   w(os.path.join(D, 'recipe', 'saw_planks_%d.json' % i), {'type': 'minecraft:crafting_shaped', 'category': 'building', 'pattern': ['S', 'W'],
                                                           'key': {'S': {'tag': 'notreepunching:saws'}, 'W': {'tag': tag[1:]}},
                                                           'result': {'id': 'minecraft:%s_planks' % wood, 'count': 2 if wood == 'bamboo' else 4}})
   w(os.path.join(MC, 'recipe', wood + '_planks.json'), OFF)
for n in ('stick', 'sandstone', 'red_sandstone'):
   w(os.path.join(MC, 'recipe', n + '.json'), OFF)
print('ok')
