#!/usr/bin/env python3
"""First Aid 1.6.22 assets/data for the 1.21 port (firstaid mod in the rlgen jar).

usage: port_fa_assets.py <decompiled firstaid jar dir> <1.12.2 client jar> <1.21.1 vanilla data dir (data/minecraft)> <rlgen/src/main/resources>

Textures (1.21 folder names) incl. the 1.12 gui/icons.png the part hearts are drawn from, the heartbeat sound, models, en_us.json
from the 1.12 .lang, RLCraft's Shiv.zs recipes (plaster, bandage; morphine as in 1.12), and the 1.12 LootTableLoadEvent additions
(bandage/plaster/morphine in the bonus chest, stronghold corridor/crossing and mineshaft main pools) as overrides of those tables.
"""
import json
import os
import re
import shutil
import sys
import zipfile

DEC, CLIENT, VANILLA, RES = sys.argv[1:5]
SRC = os.path.join(DEC, 'assets', 'firstaid')
A = os.path.join(RES, 'assets', 'firstaid')
D = os.path.join(RES, 'data', 'firstaid')


def w(path, obj):
   os.makedirs(os.path.dirname(path), exist_ok=True)
   with open(path, 'w') as f:
      json.dump(obj, f, indent=2)
      f.write('\n')


def cp(src, dst):
   os.makedirs(os.path.dirname(dst), exist_ok=True)
   shutil.copyfile(src, dst)


for fn in os.listdir(os.path.join(SRC, 'textures', 'items')):
   cp(os.path.join(SRC, 'textures', 'items', fn), os.path.join(A, 'textures', 'item', fn))
for fn in os.listdir(os.path.join(SRC, 'textures', 'gui')):
   cp(os.path.join(SRC, 'textures', 'gui', fn), os.path.join(A, 'textures', 'gui', fn))
cp(os.path.join(SRC, 'textures', 'items', 'morphine.png'), os.path.join(A, 'textures', 'mob_effect', 'morphine.png'))
with zipfile.ZipFile(CLIENT) as z:
   os.makedirs(os.path.join(A, 'textures', 'gui'), exist_ok=True)
   with open(os.path.join(A, 'textures', 'gui', 'icons.png'), 'wb') as f:
      f.write(z.read('assets/minecraft/textures/gui/icons.png'))
cp(os.path.join(SRC, 'sounds', 'heartbeat.ogg'), os.path.join(A, 'sounds', 'heartbeat.ogg'))
cp(os.path.join(SRC, 'sounds.json'), os.path.join(A, 'sounds.json'))
for n in ('bandage', 'plaster', 'morphine'):
   w(os.path.join(A, 'models', 'item', n + '.json'), {'parent': 'item/generated', 'textures': {'layer0': 'firstaid:item/' + n}})

lang = {}
for line in open(os.path.join(SRC, 'lang', 'en_us.lang'), encoding='utf-8'):
   line = line.strip()
   if not line or line.startswith('#') or '=' not in line:
      continue
   k, v = line.split('=', 1)
   m = re.match(r'item\.(\w+)\.name$', k)
   if m:
      lang['item.firstaid.' + m.group(1)] = v
      if m.group(1) == 'morphine':
         lang['effect.firstaid.morphine'] = v
      continue
   lang[k] = v
w(os.path.join(A, 'lang', 'en_us.json'), dict(sorted(lang.items())))

# Shiv.zs: recipes.remove(<firstaid:plaster>), recipes.remove(<firstaid:bandage>), lolarecipe10 and lolarecipe11
w(os.path.join(D, 'recipe', 'plaster.json'), {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
                                             'ingredients': [{'tag': 'c:strings'}, {'item': 'minecraft:paper'}], 'result': {'id': 'firstaid:plaster', 'count': 1}})
w(os.path.join(D, 'recipe', 'bandage.json'), {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['SWS'],
                                             'key': {'S': {'tag': 'c:strings'}, 'W': {'tag': 'minecraft:wool'}}, 'result': {'id': 'firstaid:bandage', 'count': 2}})
w(os.path.join(D, 'recipe', 'morphine.json'), {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [
   {'item': 'minecraft:glass_bottle'}, {'item': 'minecraft:fermented_spider_eye'}, {'item': 'minecraft:fermented_spider_eye'}],
   'result': {'id': 'firstaid:morphine', 'count': 1}})


def entry(item, weight, lo, hi):
   return {'type': 'minecraft:item', 'name': item, 'weight': weight,
           'functions': [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}]}


for table, (b, p, m) in [('spawn_bonus_chest', (8, 16, 4)), ('stronghold_corridor', (20, 24, 8)), ('stronghold_crossing', (20, 24, 8)),
                         ('abandoned_mineshaft', (20, 24, 8))]:
   out = os.path.join(RES, 'data', 'minecraft', 'loot_table', 'chests', table + '.json')
   src = out if os.path.exists(out) else os.path.join(VANILLA, 'loot_table', 'chests', table + '.json')
   t = json.load(open(src))
   pool = t['pools'][0]
   names = {e.get('name') for e in pool['entries']}
   for e in (entry('firstaid:bandage', b, 1, 3), entry('firstaid:plaster', p, 1, 5), entry('firstaid:morphine', m, 1, 2)):
      if e['name'] not in names:
         pool['entries'].append(e)
   w(out, t)
print('ok')
