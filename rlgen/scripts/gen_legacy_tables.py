"""Writes data/rlgen/legacy/{blocks,items}.json: 1.12 "name:meta" -> 1.21 block state / item id, for ported 1.12 generators.
Usage: python3 -I gen_legacy_tables.py <minecraft-data dir> <resources dir>   (reuses convert_rc.py's tables)"""
import sys, os, json, importlib.util
here = os.path.dirname(os.path.abspath(__file__))
mcdata, res = sys.argv[1], sys.argv[2]
sys.argv = ["convert_rc.py", "-", "-", mcdata, res]
spec = importlib.util.spec_from_file_location("convert_rc", os.path.join(here, "convert_rc.py"))
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
blocks = {}
for name in sorted(c.B112):
    for meta in range(16):
        s, _ = c.convert_block("minecraft:" + name, meta)
        if s and (meta == 0 or s != blocks.get(f"minecraft:{name}:0")):
            blocks[f"minecraft:{name}:{meta}"] = s
for name in sorted(c.MOD_BLOCKS):
    for meta in range(16):
        s, _ = c.convert_block(name, meta)
        if s and (meta == 0 or s != blocks.get(f"{name}:0")):
            blocks[f"{name}:{meta}"] = s
items = {}
for name in sorted(set(c.I112) | set(c.B112)):
    for dmg in range(16):
        i = c.item_id("minecraft:" + name, dmg)
        if i and (dmg == 0 or i != items.get(f"minecraft:{name}:0")):
            items[f"minecraft:{name}:{dmg}"] = i
for k, v in c.ITEM_MOD.items():
    items[k + ":0"] = v
os.makedirs(f"{res}/data/rlgen/legacy", exist_ok=True)
json.dump(blocks, open(f"{res}/data/rlgen/legacy/blocks.json", "w"), indent=0, sort_keys=True)
json.dump(items, open(f"{res}/data/rlgen/legacy/items.json", "w"), indent=0, sort_keys=True)
print(len(blocks), "block states,", len(items), "items")
