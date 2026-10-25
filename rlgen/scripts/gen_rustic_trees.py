"""Splices Rustic 1.1.7's Dynamic Trees species (BiomeDataBasePopulator, SPLICE_BEFORE) into trees/rlgen/world_gen/default.json.
RLCraft rustic.cfg: olive chance 0.02 -> weight 10/1000, ironwood 0.01 -> 5/1000. Run after gen_worldgen_data.py (idempotent)."""
import json, os, re

RES = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
JAVA = os.path.join(os.path.dirname(__file__), "..", "src", "main", "java", "com", "nukevscity", "rlgen", "rc", "BiomeDict.java")
OLIVE, IRON = int(0.02 * 500), int(0.01 * 500)

src = open(JAVA).read()
types = {k: set(re.findall(r'"([A-Z_]+)"', v)) for k, v in re.findall(r'Map\.entry\("([^"]+)", Set\.of\(([^)]*)\)\)', src)}
biomes = {os.path.splitext(f)[0] for f in os.listdir(f"{RES}/data/rlgen/worldgen/biome")}
path = f"{RES}/trees/rlgen/world_gen/default.json"
pop = [e for e in json.load(open(path)) if not (isinstance(e["apply"].get("species"), dict) and e["apply"]["species"].get("method") == "splice_before")]
for k, t in sorted(types.items()):
    b = "iceandfire_glacier" if k == "iceandfire:glacier" else k.split(":")[-1]
    if b not in biomes:
        continue
    olive = not t & {"SPOOKY", "SNOWY", "DEAD", "SAVANNA"} and bool(t & {"FOREST", "PLAINS", "MOUNTAIN"})
    iron = not t & {"SPOOKY", "DRY", "DEAD", "SAVANNA"} and bool(t & {"FOREST", "PLAINS", "MOUNTAIN", "SWAMP", "JUNGLE"})
    if not (olive or iron):
        continue
    r = {"...": 1000 - (OLIVE if olive else 0) - (IRON if iron else 0)}
    if olive:
        r["rlgen:olive"] = OLIVE
    if iron:
        r["rlgen:ironwood"] = IRON
    pop.append({"select": {"name": "rlgen:" + b}, "apply": {"species": {"method": "splice_before", "random": r}}})
json.dump(pop, open(path, "w"), indent=2)
