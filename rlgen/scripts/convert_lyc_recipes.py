#!/usr/bin/env python3
"""Lycanites Mobs 1.12 recipes (assets/lycanitesmobs/recipes in the 1.12 jar) -> 1.21 recipes and ore dictionary item tags.

usage: convert_lyc_recipes.py <1.12 recipes dir> <rlgen dir>
Items with metadata are flattened with data/rlgen/legacy/items.json. Ore dictionary ingredients become the item tags
lycanitesmobs:ore/<name>, made from the OreDictionary.registerOre calls of ItemManager (and Forge's vanilla gem entries).
"""
import json, os, re, sys

src, rlgen = sys.argv[1], sys.argv[2]
res = os.path.join(rlgen, 'src/main/resources')
items = json.load(open(os.path.join(res, 'data/rlgen/legacy/items.json')))

def item_id(name, data=0):
    if name.startswith('lycanitesmobs:'):
        return name
    if ':' not in name:
        name = 'minecraft:' + name
    return items.get('%s:%d' % (name, data)) or items.get(name + ':0') or name

# ore dictionary: Lycanites' registerOre calls + Forge's vanilla entries used by the recipes
ores = {'gemEmerald': ['minecraft:emerald'], 'gemDiamond': ['minecraft:diamond']}
im = open(os.path.join(rlgen, 'src/main/java/com/lycanitesmobs/core/info/ItemManager.java')).read()
for name, kind, val in re.findall(r'OreDictionary\.registerOre\("(\w+)", (Items|Blocks|ObjectManager\.getItem)\(?\.?"?(\w+)"?\)?\)', im):
    iid = 'lycanitesmobs:' + val if kind.startswith('ObjectManager') else 'minecraft:' + val.lower()
    ores.setdefault(name, [])
    if iid not in ores[name]:
        ores[name].append(iid)

used = set()
def ingredient(j):
    if isinstance(j, list):
        return [ingredient(x) for x in j]
    if j.get('type') == 'forge:ore_dict':
        used.add(j['ore'])
        return {'tag': 'lycanitesmobs:ore/' + j['ore'].lower()}
    return {'item': item_id(j['item'], j.get('data', 0))}

out_dir = os.path.join(res, 'data/lycanitesmobs/recipe')
os.makedirs(out_dir, exist_ok=True)
n = 0
for f in sorted(os.listdir(src)):
    # belpharm_break: its item does not exist in 2.0.8.9 (the recipe failed in 1.12 too)
    if not f.endswith('.json') or f.startswith('_') or f == 'belpharm_break.json':
        continue
    r = json.load(open(os.path.join(src, f)))
    res_j = r['result']
    result = {'id': item_id(res_j['item'], res_j.get('data', 0)), 'count': res_j.get('count', 1)}
    if 'pattern' in r:
        out = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': r['pattern'],
               'key': {k: ingredient(v) for k, v in r['key'].items()}, 'result': result}
    else:
        out = {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
               'ingredients': [ingredient(x) for x in r['ingredients']], 'result': result}
    json.dump(out, open(os.path.join(out_dir, f), 'w'), indent=2)
    n += 1

tag_dir = os.path.join(res, 'data/lycanitesmobs/tags/item/ore')
os.makedirs(tag_dir, exist_ok=True)
for name in sorted(used):
    values = [{'id': v, 'required': False} for v in ores.get(name, [])]
    json.dump({'values': values}, open(os.path.join(tag_dir, name.lower() + '.json'), 'w'), indent=2)
    if not values:
        print('empty ore', name)
print(n, 'recipes,', len(used), 'ore tags')
