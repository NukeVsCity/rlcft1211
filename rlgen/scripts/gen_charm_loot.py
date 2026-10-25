"""Charm 1.4.1's village and treasure chest loot tables -> 1.21 (rlgen:charm/<path>), items through data/rlgen/legacy/items.json.
Usage: python3 -I gen_charm_loot.py <charm jar assets/charm/loot_tables> <resources dir>"""
import json, sys, os, re
src, res = sys.argv[1], sys.argv[2]
items = json.load(open(res + "/data/rlgen/legacy/items.json"))

def rng(v):
    if isinstance(v, dict):
        return {"type": "minecraft:uniform", "min": v["min"], "max": v["max"]}
    return v

def conv(t):
    pools = []
    for p in t["pools"]:
        entries = []
        for e in p["entries"]:
            if e.get("type", "item") != "item":
                raise SystemExit("entry type " + e["type"])
            name = e["name"] if ":" in e["name"] else "minecraft:" + e["name"]
            data = 0
            fns = []
            for f in e.get("functions", []):
                fn = f["function"].replace("minecraft:", "")
                if fn == "set_count":
                    fns.append({"function": "minecraft:set_count", "count": rng(f["count"])})
                elif fn == "set_data":
                    data = f["data"]
                elif fn == "set_nbt" and "EntityTag" in f["tag"]:
                    mob = re.search(r'id:"(?:minecraft:)?(\w+)"', f["tag"]).group(1)
                    mob = {"vindication_illager": "vindicator", "evocation_illager": "evoker", "zombie_pigman": "zombified_piglin",
                           "villager_golem": "iron_golem", "snowman": "snow_golem", "ocelot": "ocelot"}.get(mob, mob)
                    name, data = "egg:" + mob, 0
                elif fn == "set_nbt":
                    m = re.match(r'\{Potion:"(?:minecraft:)?(\w+)"\}', f["tag"])
                    if not m:
                        raise SystemExit("nbt " + f["tag"])
                    fns.append({"function": "minecraft:set_potion", "id": "minecraft:" + m.group(1)})
                elif fn == "enchant_with_levels":
                    fns.append({"function": "minecraft:enchant_with_levels", "levels": rng(f["levels"]),
                                "options": "#minecraft:on_random_loot" if f.get("treasure") else "#minecraft:in_enchanting_table"})
                else:
                    raise SystemExit("unhandled " + fn)
            if isinstance(data, dict):
                data = data.get("min", 0)
            nid = ("minecraft:" + name[4:] + "_spawn_egg") if name.startswith("egg:") else items.get(f"{name}:{data}") or items.get(f"{name}:0")
            if nid is None:
                raise SystemExit("no item for " + name)
            ne = {"type": "minecraft:item", "name": nid, "weight": e.get("weight", 1)}
            if e.get("quality"):
                ne["quality"] = e["quality"]
            if fns:
                ne["functions"] = fns
            entries.append(ne)
        pool = {"rolls": rng(p["rolls"]), "entries": entries}
        if p.get("bonus_rolls"):
            pool["bonus_rolls"] = rng(p["bonus_rolls"])
        pools.append(pool)
    return {"type": "minecraft:chest", "pools": pools}

for sub in ["village", "treasure"]:
    out = f"{res}/data/rlgen/loot_table/charm/{sub}/"
    os.makedirs(out, exist_ok=True)
    for f in sorted(os.listdir(f"{src}/{sub}")):
        json.dump(conv(json.load(open(f"{src}/{sub}/{f}"))), open(out + f, "w"), indent=2)
        print(sub, f)
