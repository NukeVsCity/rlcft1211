#!/usr/bin/env python3
"""Simple Difficulty 0.3.9 assets/data for the 1.21 port (simpledifficulty mod in the rlgen jar).

usage: port_sd_assets.py <decompiled SimpleDifficulty jar dir> <RLCraft pack overrides dir> <rlgen/src/main/resources>

Copies the 1.12 textures/models (1.21 folder names), RLCraft's resource overrides (thirst HUD, frost rod/powder), writes vanilla
blockstates, en_us.json from the 1.12 .lang (+ RLCraft display names), the RLCraft json configs, recipes converted from the 1.12
recipe jsons (+ RLCraft Shiv.zs changes), damage types, enchantments, loot tables and tags.
"""
import json
import os
import re
import shutil
import sys

from PIL import Image

DEC, PACK, RES = sys.argv[1], sys.argv[2], sys.argv[3]
SRC = os.path.join(DEC, 'assets', 'simpledifficulty')
A = os.path.join(RES, 'assets', 'simpledifficulty')
D = os.path.join(RES, 'data', 'simpledifficulty')
LEGACY_ITEMS = json.load(open(os.path.join(RES, 'data', 'rlgen', 'legacy', 'items.json')))


def w(path, obj):
   os.makedirs(os.path.dirname(path), exist_ok=True)
   with open(path, 'w') as f:
      json.dump(obj, f, indent=2)
      f.write('\n')


def w(path, obj, _w=w):
   # shared tag files (data/minecraft, data/c): only add our values to what is already there
   if os.sep + 'tags' + os.sep in path and os.path.exists(path) and isinstance(obj, dict) and 'values' in obj:
      old = json.load(open(path))
      vals = old.get('values', [])
      for v in obj['values']:
         if v not in vals:
            vals.append(v)
      old['values'] = vals
      obj = old
   _w(path, obj)


def cp(src, dst):
   os.makedirs(os.path.dirname(dst), exist_ok=True)
   shutil.copyfile(src, dst)


def fix_tex(s):
   return re.sub(r'(^|:)items/', r'\1item/', re.sub(r'(^|:)blocks/', r'\1block/', s))


# ---- textures ----
for sub, dst in [('items', 'item'), ('blocks', 'block'), ('gui', 'gui'), ('models/armor', 'models/armor'), ('potions', 'mob_effect')]:
   base = os.path.join(SRC, 'textures', sub)
   for root, _, files in os.walk(base):
      for fn in files:
         rel = os.path.relpath(os.path.join(root, fn), base)
         cp(os.path.join(root, fn), os.path.join(A, 'textures', dst, rel))
for fn in os.listdir(os.path.join(SRC, 'textures', 'fluids')):
   cp(os.path.join(SRC, 'textures', 'fluids', fn), os.path.join(A, 'textures', 'block', fn))
ov = os.path.join(PACK, 'resources', 'simpledifficulty', 'textures')
cp(os.path.join(ov, 'gui', 'thirsthud.png'), os.path.join(A, 'textures', 'gui', 'thirsthud.png'))
cp(os.path.join(ov, 'items', 'frost_powder.png'), os.path.join(A, 'textures', 'item', 'frost_powder.png'))
cp(os.path.join(ov, 'items', 'frost_rod.png'), os.path.join(A, 'textures', 'item', 'frost_rod.png'))
# 1.12 particles.png: 8x8 cells, row 0 heater frames 0..8, row 1 chiller
im = Image.open(os.path.join(SRC, 'textures', 'particles', 'particles.png')).convert('RGBA')
os.makedirs(os.path.join(A, 'textures', 'particle'), exist_ok=True)
for row, name in [(0, 'heater'), (1, 'chiller')]:
   for f in range(9):
      im.crop((f * 8, row * 8, f * 8 + 8, row * 8 + 8)).save(os.path.join(A, 'textures', 'particle', '%s_%d.png' % (name, f)))
   w(os.path.join(A, 'particles', name + '.json'), {'textures': ['simpledifficulty:%s_%d' % (name, f) for f in range(9)]})

# ---- models ----
CUTOUT = ('campfire', 'rain_collector', 'spit', 'temper_')
for kind in ('block', 'item'):
   base = os.path.join(SRC, 'models', kind)
   for root, _, files in os.walk(base):
      for fn in files:
         m = json.load(open(os.path.join(root, fn)))
         if 'textures' in m:
            m['textures'] = {k: fix_tex(v) for k, v in m['textures'].items()}
         if 'parent' in m and not m['parent'].startswith(('item/', 'minecraft:', 'block/', 'builtin/')) and ':' in m['parent'] and '/' not in m['parent'].split(':')[1]:
            m['parent'] = m['parent'].replace(':', ':block/')
         if kind == 'block' and fn.startswith(CUTOUT):
            m['render_type'] = 'minecraft:cutout'
         rel = os.path.relpath(os.path.join(root, fn), base)
         w(os.path.join(A, 'models', kind, rel), m)
w(os.path.join(A, 'models', 'block', 'purifiedwater.json'), {'textures': {'particle': 'simpledifficulty:block/purifiedwater_still'}})
w(os.path.join(A, 'models', 'item', 'purifiedwater_bucket.json'),
  {'parent': 'neoforge:item/bucket_drip', 'loader': 'neoforge:fluid_container', 'fluid': 'simpledifficulty:purifiedwater'})
for n in ('rain_collector', 'spit'):
   w(os.path.join(A, 'models', 'item', n + '.json'), {'parent': 'simpledifficulty:block/' + (n + '_0' if n == 'rain_collector' else n)})
w(os.path.join(A, 'models', 'item', 'chiller.json'), {'parent': 'simpledifficulty:block/temper_chill_on'})

# ---- blockstates ----
w(os.path.join(A, 'blockstates', 'campfire.json'), {'variants': {
   'age=%d' % a: {'model': 'simpledifficulty:block/' + ('campfire' if a < 3 else 'campfire_half' if a < 6 else 'campfire_burnt')} for a in range(8)}})
w(os.path.join(A, 'blockstates', 'heater.json'), {'variants': {
   'enabled=true': {'model': 'simpledifficulty:block/temper_heat_on'}, 'enabled=false': {'model': 'simpledifficulty:block/temper_heat_off'}}})
w(os.path.join(A, 'blockstates', 'chiller.json'), {'variants': {
   'enabled=true': {'model': 'simpledifficulty:block/temper_chill_on'}, 'enabled=false': {'model': 'simpledifficulty:block/temper_chill_off'}}})
w(os.path.join(A, 'blockstates', 'rain_collector.json'), {'variants': {
   'level=%d' % l: {'model': 'simpledifficulty:block/rain_collector_%d' % l} for l in range(4)}})
w(os.path.join(A, 'blockstates', 'spit.json'), {'variants': {'': {'model': 'simpledifficulty:block/spit'}}})
w(os.path.join(A, 'blockstates', 'purifiedwater.json'), {'variants': {'': {'model': 'simpledifficulty:block/purifiedwater'}}})

# ---- lang ----
lang = {}
for line in open(os.path.join(SRC, 'lang', 'en_us.lang'), encoding='utf-8'):
   line = line.strip()
   if not line or line.startswith('#') or '=' not in line:
      continue
   k, v = line.split('=', 1)
   if k.endswith('.name'):
      k = k[:-5]
   m = re.match(r'(item|tile)\.simpledifficulty:(.*)', k)
   if m:
      kind, name = m.groups()
      lang[('block' if kind == 'tile' else 'item') + '.simpledifficulty.' + name] = v
      lang[k] = v
      continue
   m = re.match(r'(potion|splash_potion|lingering_potion|tipped_arrow)\.effect\.(.*)', k)
   if m:
      lang['item.minecraft.%s.effect.%s' % m.groups()] = v
      continue
   if k.startswith('effect.'):
      lang['effect.simpledifficulty.' + k[7:]] = v
   elif k.startswith('enchantment.') and not k.startswith('enchantment.simpledifficulty'):
      lang['enchantment.simpledifficulty.' + k[12:]] = v
   elif k == 'fluid.purifiedwater':
      lang[k] = v
   else:
      lang[k] = v
# RLCraft Shiv.zs display names
for k, v in [('heater', 'Heating Coil'), ('chiller', 'Cooling Coil')]:
   lang['block.simpledifficulty.' + k] = v
for k, v in [('wool_helmet', 'Wool Hood'), ('wool_chestplate', 'Wool Coat'), ('wool_leggings', 'Wool Leggings'), ('wool_boots', 'Wool Boots')]:
   lang['item.simpledifficulty.' + k] = v
lang['item.simpledifficulty.purifiedwater_bucket'] = 'Purified Water Bucket'
lang['item.simpledifficulty.juice'] = 'Juice'
lang['item.simpledifficulty.canteen'] = lang['item.simpledifficulty:canteen_empty']
lang['item.simpledifficulty.iron_canteen'] = lang['item.simpledifficulty:iron_canteen_empty']
lang['item.simpledifficulty.dragon_canteen'] = lang['item.simpledifficulty:dragon_canteen_empty']
w(os.path.join(A, 'lang', 'en_us.json'), dict(sorted(lang.items())))

# ---- RLCraft json configs ----
cfg = os.path.join(PACK, 'config', 'simpledifficulty')
for fn in os.listdir(cfg):
   cp(os.path.join(cfg, fn), os.path.join(D, 'rlcraft_config', fn))

# ---- recipes ----
ORE = {'plankWood': {'tag': 'minecraft:planks'}, 'logWood': {'tag': 'minecraft:logs'}, 'leather': {'item': 'minecraft:leather'},
       'cobblestone': {'item': 'minecraft:cobblestone'}, 'dustRedstone': {'item': 'minecraft:redstone'}, 'ingotIron': {'item': 'minecraft:iron_ingot'},
       'paper': {'item': 'minecraft:paper'}, 'string': {'tag': 'c:strings'}, 'stickWood': {'tag': 'c:rods/wooden'},
       'blockGlass': {'tag': 'c:glass_blocks'}, 'wool': {'tag': 'minecraft:wool'}}
CONST = {c['name']: c['ingredient'] for c in json.load(open(os.path.join(SRC, 'recipes', '_constants.json')))}


def item121(name, data=0):
   if name.startswith('simpledifficulty:'):
      return name
   key = '%s:%d' % (name, data if data != 32767 else 0)
   return LEGACY_ITEMS.get(key, LEGACY_ITEMS.get(name + ':0', name))


def ingredient(i):
   if i.get('type') == 'forge:ore_dict':
      return ORE[i['ore']]
   if i.get('item', '').startswith('#'):
      return ingredient(CONST[i['item'][1:]])
   name = i['item']
   if name == 'simpledifficulty:juice' and 'data' in i:
      raise ValueError('juice ingredient')
   return {'item': item121(name, i.get('data', 0))}


def result(r):
   out = {'id': item121(r['item'], r.get('data', 0)), 'count': r.get('count', 1)}
   comps = {}
   if r['item'] == 'simpledifficulty:juice':
      comps['simpledifficulty:metadata'] = r.get('data', 0)
   if 'nbt' in r:
      comps['minecraft:custom_data'] = '{' + ','.join('%s:%d' % kv for kv in r['nbt'].items()) + '}'  # SNBT keeps the 1.12 int tags
   if comps:
      out['components'] = comps
   return out


RECIPE_REMOVED = {'heater', 'chiller', 'spit', 'campfire'}  # Shiv.zs recipes.remove, re-added below with RLCraft patterns
for fn in sorted(os.listdir(os.path.join(SRC, 'recipes'))):
   if fn.startswith('_'):
      continue
   name = fn[:-5]
   if name in RECIPE_REMOVED:
      continue
   r = json.load(open(os.path.join(SRC, 'recipes', fn)))
   t = r['type']
   if t == 'forge:ore_shaped':
      o = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': r['pattern'],
           'key': {k: ingredient(v) for k, v in r['key'].items()}, 'result': result(r['result'])}
   elif t in ('forge:ore_shapeless', 'simpledifficulty:canteen_charcoal_recipe'):
      o = {'type': 'minecraft:crafting_shapeless' if t == 'forge:ore_shapeless' else t, 'category': 'misc',
           'ingredients': [ingredient(i) for i in r['ingredients']], 'result': result(r['result'])}
   else:
      raise ValueError(t)
   w(os.path.join(D, 'recipe', name + '.json'), o)


def shaped(name, pattern, key, res, count=1):
   w(os.path.join(D, 'recipe', name + '.json'), {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern, 'key': key,
                                                 'result': {'id': res, 'count': count}})


# RLCraft Shiv.zs lolarecipe70/71/71a/71b/76/112
shaped('heater', ['B B', 'BCB', 'CRC'], {'B': {'item': 'minecraft:blaze_rod'}, 'C': {'item': 'minecraft:cobblestone'}, 'R': {'item': 'minecraft:redstone'}},
       'simpledifficulty:heater')
shaped('chiller', ['F F', 'FCF', 'CRC'], {'F': {'item': 'simpledifficulty:frost_rod'}, 'C': {'item': 'minecraft:cobblestone'}, 'R': {'item': 'minecraft:redstone'}},
       'simpledifficulty:chiller')
shaped('spit', ['III', 'S S', 'S S'], {'I': {'item': 'minecraft:iron_ingot'}, 'S': {'item': 'minecraft:stick'}}, 'simpledifficulty:spit')
shaped('campfire', [' L ', 'LLL', 'CCC'], {'L': {'tag': 'minecraft:logs'}, 'C': {'item': 'minecraft:cobblestone'}}, 'simpledifficulty:campfire')
shaped('dragon_canteen', ['S', 'C', 'H'], {'S': {'item': 'iceandfire:dragon_skull'},
                                           'C': {'item': 'simpledifficulty:iron_canteen'},
                                           'H': [{'item': 'iceandfire:fire_dragon_heart'}, {'item': 'iceandfire:ice_dragon_heart'}]},
       'simpledifficulty:dragon_canteen')
w(os.path.join(D, 'recipe', 'frost_powder.json'), {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
                                                  'ingredients': [{'item': 'simpledifficulty:frost_rod'}], 'result': {'id': 'simpledifficulty:frost_powder', 'count': 2}})
# 1.12 RegisterRecipes smelting (FurnaceRecipes matched item + metadata only: every potion smelts to purified water)
w(os.path.join(D, 'recipe', 'purified_water_bottle_smelting.json'), {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': {'item': 'minecraft:potion'},
                                                                    'result': {'id': 'simpledifficulty:purified_water_bottle'}, 'experience': 0.0, 'cookingtime': 200})
w(os.path.join(D, 'recipe', 'purifiedwater_bucket_smelting.json'), {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': {'item': 'minecraft:water_bucket'},
                                                                   'result': {'id': 'simpledifficulty:purifiedwater_bucket'}, 'experience': 0.0, 'cookingtime': 200})

# ---- damage types (1.12 SDDamageSources; armor-bypassing sources had hunger damage 0) ----
for n in ('dehydration', 'hyperthermia', 'hypothermia', 'parasites'):
   w(os.path.join(D, 'damage_type', n + '.json'), {'message_id': n, 'exhaustion': 0.0, 'scaling': 'never'})
MCD = os.path.join(RES, 'data', 'minecraft', 'tags')
w(os.path.join(MCD, 'damage_type', 'bypasses_armor.json'), {'replace': False, 'values': ['simpledifficulty:' + n for n in ('dehydration', 'hyperthermia', 'hypothermia', 'parasites')]})
w(os.path.join(MCD, 'damage_type', 'bypasses_effects.json'), {'replace': False, 'values': ['simpledifficulty:dehydration']})
w(os.path.join(MCD, 'damage_type', 'bypasses_enchantments.json'), {'replace': False, 'values': ['simpledifficulty:dehydration']})
w(os.path.join(MCD, 'damage_type', 'witch_resistant_to.json'), {'replace': False, 'values': ['simpledifficulty:parasites']})

# ---- enchantments (1.12 EnchantmentArmorTemperature: COMMON, ARMOR, level 1, min 1+10*level, max min+5, not with each other) ----
for n in ('chilling', 'heating'):
   w(os.path.join(D, 'enchantment', n + '.json'), {
      'description': {'translate': 'enchantment.simpledifficulty.' + n}, 'supported_items': '#minecraft:enchantable/armor',
      'exclusive_set': '#simpledifficulty:armor_temperature', 'weight': 10, 'max_level': 1, 'min_cost': {'base': 11, 'per_level_above_first': 10},
      'max_cost': {'base': 16, 'per_level_above_first': 10}, 'anvil_cost': 1, 'slots': ['armor']})
w(os.path.join(D, 'tags', 'enchantment', 'armor_temperature.json'), {'values': ['simpledifficulty:chilling', 'simpledifficulty:heating']})
for t in ('non_treasure', 'in_enchanting_table', 'tradeable', 'on_random_loot', 'on_mob_spawn_equipment', 'on_traded_equipment'):
   w(os.path.join(MCD, 'enchantment', t + '.json'), {'replace': False, 'values': ['simpledifficulty:chilling', 'simpledifficulty:heating']})

# ---- loot tables (campfire drops nothing, like 1.12 quantityDropped 0) ----
for n in ('rain_collector', 'heater', 'chiller', 'spit'):
   w(os.path.join(D, 'loot_table', 'blocks', n + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [
      {'type': 'minecraft:item', 'name': 'simpledifficulty:' + n}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

# ---- tags ----
w(os.path.join(MCD, 'block', 'mineable', 'pickaxe.json'), {'replace': False, 'values': ['simpledifficulty:rain_collector', 'simpledifficulty:heater', 'simpledifficulty:chiller']})
w(os.path.join(MCD, 'block', 'mineable', 'axe.json'), {'replace': False, 'values': ['simpledifficulty:spit']})
w(os.path.join(MCD, 'fluid', 'water.json'), {'replace': False, 'values': ['simpledifficulty:purifiedwater', 'simpledifficulty:flowing_purifiedwater']})
for slot, tag in (('helmet', 'head_armor'), ('chestplate', 'chest_armor'), ('leggings', 'leg_armor'), ('boots', 'foot_armor')):
   w(os.path.join(MCD, 'item', tag + '.json'), {'replace': False, 'values': ['simpledifficulty:wool_' + slot, 'simpledifficulty:ice_' + slot]})
print('ok')
