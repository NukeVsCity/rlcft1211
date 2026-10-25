"""Converts a 1.12 structure template (.nbt, palette of 1.12 names + properties) to a 1.21 one, through convert_rc.py's tables.
1.21's own data fixer chokes on some 1.12 templates, so the palette is flattened here: 1.12 state -> meta (states112.tsv, dumped
from the 1.12.2 jar: every valid state and its metadata) -> 1.21 state (convert_rc.convert_block). Skull types and rotations come
from the skull block entities. Entities from mods (e.g. iceandfire:gorgon) are dropped.
Usage: python3 -I convert_template112.py <minecraft-data dir> <in.nbt> <out.nbt>"""
import sys, os, importlib.util, re
import nbtlib
from nbtlib import Compound, List, String, Int
here = os.path.dirname(os.path.abspath(__file__))
mcdata, src, dst = sys.argv[1:4]
sys.argv = ["convert_rc.py", "-", "-", mcdata, "-"]
spec = importlib.util.spec_from_file_location("convert_rc", os.path.join(here, "convert_rc.py"))
c = importlib.util.module_from_spec(spec)
spec.loader.exec_module(c)
META = {}
for line in open(os.path.join(here, "states112.tsv")):
    k, m = line.rstrip("\n").split("\t")
    META[k] = int(m)
SKULLS = ["skeleton", "wither_skeleton", "zombie", "player", "creeper", "dragon"]

def key(entry):
    props = entry.get("Properties", {})
    return str(entry["Name"]) + "[" + ",".join(f"{k}={str(v)}" for k, v in sorted(props.items())) + "]"

def parse(state):
    m = re.match(r"([^\[]+)(?:\[(.*)\])?$", state)
    props = dict(p.split("=") for p in m.group(2).split(",")) if m.group(2) else {}
    return m.group(1), props

def entry(name, props):
    e = Compound({"Name": String(name)})
    if props:
        e["Properties"] = Compound({k: String(v) for k, v in props.items()})
    return e

t = nbtlib.load(src)
pal = []
for p in t["palette"]:
    k = key(p)
    meta = META.get(k)
    if meta is None and not str(p["Name"]).startswith("minecraft:"):
        meta = 0  # modded blocks map through convert_rc.MOD_BLOCKS
    if meta is None:
        raise SystemExit("unknown 1.12 state " + k)
    pal.append(parse(c.convert_block(str(p["Name"]), meta)[0]))
new_pal, index = [], {}
def idx(name, props):
    k = (name, tuple(sorted(props.items())))
    if k not in index:
        index[k] = len(new_pal)
        new_pal.append(entry(name, props))
    return index[k]
blocks = []
for b in t["blocks"]:
    name, props = pal[int(b["state"])]
    nb = Compound({"pos": b["pos"]})
    nbt = b.get("nbt")
    if nbt is not None and str(nbt.get("id")) == "Structure":  # pre-1.11 block entity ids
        nbt["id"] = String("minecraft:structure_block")
    if nbt is not None and str(nbt.get("id")) in ("minecraft:skull", "Skull"):
        kind = SKULLS[int(nbt.get("SkullType", 0))]
        wall = "wall" in name
        base = "skull" if kind in ("skeleton", "wither_skeleton") else "head"
        name = f"minecraft:{kind}_{'wall_' if wall else ''}{base}"
        props = {"facing": props["facing"]} if wall else {"rotation": str(int(nbt.get("Rot", 0)) & 15)}
        nbt = None
    nb["state"] = Int(idx(name, props))
    if nbt is not None:
        nb["nbt"] = nbt
    blocks.append(nb)
t["palette"] = List[Compound](new_pal)
t["blocks"] = List[Compound](blocks)
def item(i):
    if "id" not in i:
        return Compound({})
    return Compound({"id": String(c.item_id(str(i["id"]), int(i.get("Damage", 0))) or "minecraft:air"), "count": Int(int(i.get("Count", 1)))})
ents = []
for e in t["entities"]:
    n = e["nbt"]
    if not str(n["id"]).startswith("minecraft:"):
        continue
    for k in ("HandItems", "ArmorItems"):
        if k in n:
            n[k] = List[Compound]([item(i) for i in n[k]])
    ents.append(e)
t["entities"] = List[Compound](ents)
t["DataVersion"] = Int(3955)
t.pop("ForgeDataVersion", None)
t.save(dst, gzipped=True)
print(len(new_pal), "states,", len(blocks), "blocks,", len(t["entities"]), "entities")
for e in new_pal:
    print(" ", dict(e))
