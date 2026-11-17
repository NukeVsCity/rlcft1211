#!/usr/bin/env python3
"""No Tree Punching 2.0.21 assets/data for the 1.21 port (notreepunching mod in the rlgen jar).

usage: port_ntp_assets.py <decompiled notreepunching jar dir> <rlgen/src/main/resources>

Textures/sounds (1.21 folder names), models, blockstates, en_us.json (1.12 .lang + RLCraft display names), loot tables, tags and
recipes: the 1.12 json recipes, the generated Saw + Log -> Planks recipes (replaceLogRecipes: the vanilla log -> planks and stick
recipes are switched off), the smelting recipes of ModRecipes.init and RLCraft's Shiv.zs changes (no fire starter recipe, 4 rocks ->
1 sandstone / red sandstone, the sand -> sandstone recipes removed).
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
