"""Moves the converted data from rlgen's Ice and Fire stand-ins to the real blocks and items of the Ice and Fire port, without a full
convert_rc.py run (that needs the Recurrent Complex jar): Recurrent Complex structure palettes take the 1.21 state that
data/rlgen/legacy/blocks.json (gen_legacy_tables.py, from convert_rc.py's MOD_BLOCKS) now gives their 1.12 name and meta, and the
Recurrent Complex loot components whose .rcig lost its Ice and Fire items (the old converter mapped silver to iron and dropped the rest)
are converted again. Run gen_legacy_tables.py first.
Usage: python3 -I iaf_real_blocks.py <minecraft-data dir> <resources dir> <dir with the instance's .rcig files>"""
import sys, os, json, glob, importlib.util
import nbtlib
from nbtlib import String

here = os.path.dirname(os.path.abspath(__file__))
mcdata, res, rcig_dir = sys.argv[1:4]
sys.argv = ["convert_rc.py", "-", "-", mcdata, res]
spec = importlib.util.spec_from_file_location("convert_rc", os.path.join(here, "convert_rc.py"))
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)

table = json.load(open(f"{res}/data/rlgen/legacy/blocks.json"))
rc = f"{res}/data/rlgen/rc"

# structure palettes
changed = 0
for f in sorted(glob.glob(f"{rc}/structures/*.nbt")):
    t = nbtlib.load(f)
    hit = False
    for p in t["palette"]:
        legacy = str(p["legacy"])
        if legacy.startswith("iceandfire:"):
            new = table.get(f"{legacy}:{int(p['meta'])}") or table.get(f"{legacy}:0")
            if new and new != str(p["state"]):
                p["state"] = String(new)
                hit = True
    if hit:
        t.save(f, gzipped=True)
        changed += 1
print(changed, "structures")

# loot components (convert_rc.main, one .rcig at a time)
def component(d):
    items = d.get("items")
    if items is None:
        items = [{"weight": x.get("weight", 1) / 100.0, "min": x.get("min", 1), "max": x.get("max", 1), "item": x.get("itemStack", x.get("item", {}))}
                 for x in d.get("contents", [])]
    comp = []
    for it in items:
        st_ = it.get("item", {})
        new = c.item_id(st_.get("id", "minecraft:air"), int(st_.get("damage", st_.get("Damage", 0))))
        if not new:
            continue
        entry = {"id": new, "weight": float(it.get("weight", 1.0)), "min": int(it.get("min", 1)), "max": int(it.get("max", 1))}
        if "tag" in st_:
            fake = {"id": st_["id"], "Damage": st_.get("damage", 0), "Count": 1, "tag": c.SNBT_PARSE(json.dumps(st_["tag"])) if isinstance(st_["tag"], dict) else {}}
            try:
                conv = c.convert_item(fake)
                if conv and "components" in conv:
                    entry["components"] = conv["components"]
            except Exception:
                pass
        comp.append(entry)
    return {"items": comp, "minItems": d.get("minItems"), "maxItems": d.get("maxItems")}

OLD = {"iceandfire:silver_ingot": "minecraft:iron_ingot", "iceandfire:sapphire_gem": "minecraft:lapis_lazuli",
       "iceandfire:silver_nugget": "minecraft:iron_nugget"}
new_item_id = c.item_id
def old_item_id(iid, damage):
    s = str(iid)
    if s.startswith("iceandfire:"):
        return OLD.get(s)
    return new_item_id(iid, damage)

used = set()
for f in sorted(glob.glob(f"{rcig_dir}/*.rcig")):
    d = json.load(open(f))
    if "iceandfire:" not in json.dumps(d):
        continue
    tid = (d.get("inventoryGeneratorID") or os.path.splitext(os.path.basename(f))[0]).lower()
    path = f"{rc}/loot/{tid}.json"
    loot = json.load(open(path))
    c.item_id = old_item_id
    old = component(d)
    c.item_id = new_item_id
    new = component(d)
    idx = [i for i, x in enumerate(loot["components"]) if x == old and (tid, i) not in used]
    if len(idx) > 1:
        # identical old components (several .rcig whose items were all dropped): convert_rc.main wrote a table's components in
        # sorted file order, so this one is at its rank among the .rcig of the same table (all in the instance for these)
        same = sorted(os.path.splitext(os.path.basename(g))[0] for g in glob.glob(f"{rcig_dir}/*.rcig")
                      if (json.load(open(g)).get("inventoryGeneratorID") or "").lower() == tid)
        rank = same.index(os.path.splitext(os.path.basename(f))[0])
        idx = [rank] if rank in idx else []
    if not idx:
        print("NOT FOUND", f, tid)
        continue
    used.add((tid, idx[0]))
    loot["components"][idx[0]] = new
    json.dump(loot, open(path, "w"), indent=1)
    print("loot", tid, len(old["items"]), "->", len(new["items"]), "items")

# Better Mineshafts variants and rlgen loot tables that named the stand-in ore
for f in [f"{res}/data/rlgen/bettermineshafts/variants.json"] + glob.glob(f"{res}/data/rlgen/loot_table/**/*.json", recursive=True):
    s = open(f).read()
    n = s.replace('"rlgen:silver_ore"', '"iceandfire:silver_ore"')
    if n != s:
        open(f, "w").write(n)
        print("silver ore", os.path.relpath(f, res))
