"""Converts the 1.12 igloo templates (assets/minecraft/structures/igloo/*.nbt in the 1.12.2 client jar) to data/rlgen/structure/igloo
with convert_template112.py, then turns the block entities and the two mobs the 1.12 data still described in 1.12 terms into 1.21 nbt:
sign text, the brewing stand's splash potion of weakness, the potted cactus, the half-full cauldron, the priest villager and zombie
villager (1.21 cleric, plains type like the 1.12 look).
Usage: python3 -I convert_igloo112.py <minecraft-data dir> <1.12 client.jar> <out dir>"""
import json, os, subprocess, sys, tempfile, zipfile
import nbtlib
from nbtlib import Byte, Compound, Double, Float, Int, List, String

here = os.path.dirname(os.path.abspath(__file__))
mcdata, jar, out = sys.argv[1:4]
os.makedirs(out, exist_ok=True)
tmp = tempfile.mkdtemp()
with zipfile.ZipFile(jar) as z:
    for n in ("top", "middle", "bottom"):
        src = os.path.join(tmp, f"igloo_{n}.nbt")
        with open(src, "wb") as f:
            f.write(z.read(f"assets/minecraft/structures/igloo/igloo_{n}.nbt"))
        subprocess.run([sys.executable, "-I", os.path.join(here, "convert_template112.py"), mcdata, src, os.path.join(out, f"igloo_{n}.nbt")],
                       check=True, stdout=subprocess.DEVNULL)


def state(t, name, props=None):
    pal = t["palette"]
    for i, p in enumerate(pal):
        if str(p["Name"]) == name and dict(p.get("Properties", {})) == (props or {}):
            return i
    e = Compound({"Name": String(name)})
    if props:
        e["Properties"] = Compound({k: String(v) for k, v in props.items()})
    pal.append(e)
    return len(pal) - 1


def text(s):
    return String(json.dumps({"text": s}))


for n in ("top", "middle", "bottom"):
    path = os.path.join(out, f"igloo_{n}.nbt")
    t = nbtlib.load(path)
    for b in t["blocks"]:
        name = str(t["palette"][int(b["state"])]["Name"])
        nbt = b.get("nbt")
        bid = str(nbt.get("id")) if nbt is not None else None
        if bid == "Sign":
            lines = List[String]([text(json.loads(str(nbt[f"Text{i}"]))["text"]) for i in range(1, 5)])
            b["nbt"] = Compound({"id": String("minecraft:sign"), "is_waxed": Byte(0),
                                 "front_text": Compound({"messages": lines, "color": String("black"), "has_glowing_text": Byte(0)}),
                                 "back_text": Compound({"messages": List[String]([text("")] * 4), "color": String("black"), "has_glowing_text": Byte(0)})})
        elif bid == "Chest":
            b["nbt"] = Compound({"id": String("minecraft:chest")})
        elif bid == "Furnace":
            b["nbt"] = Compound({"id": String("minecraft:furnace")})
        elif bid == "Cauldron":  # 1.12's id of the brewing stand
            b["nbt"] = Compound({"id": String("minecraft:brewing_stand"), "Items": List[Compound]([Compound({
                "Slot": Byte(1), "id": String("minecraft:splash_potion"), "count": Int(1),
                "components": Compound({"minecraft:potion_contents": Compound({"potion": String("minecraft:weakness")})})})])})
        elif bid == "FlowerPot":
            b["state"] = Int(state(t, "minecraft:potted_" + str(nbt["Item"]).split(":")[1]))
            del b["nbt"]
        elif name == "minecraft:cauldron":  # 1.12 cauldron level=2
            b["state"] = Int(state(t, "minecraft:water_cauldron", {"level": "2"}))
    ents = []
    for e in t["entities"]:
        old = e["nbt"]
        nbt = Compound({"id": old["id"], "PersistenceRequired": Byte(1), "Rotation": old["Rotation"],
                        "Pos": old["Pos"], "Motion": List[Double]([Double(0), Double(0), Double(0)]),
                        "VillagerData": Compound({"profession": String("minecraft:cleric"), "type": String("minecraft:plains"), "level": Int(1)})})
        if str(old["id"]) == "minecraft:villager":
            nbt["Xp"] = Int(1)  # keeps the profession without a brewing stand claimed
        else:
            nbt["ConversionTime"] = Int(-1)
        ents.append(Compound({"blockPos": e["blockPos"], "pos": e["pos"], "nbt": nbt}))
    t["entities"] = List[Compound](ents)
    t.save(path, gzipped=True)
    print(n, len(t["blocks"]), "blocks,", len(ents), "entities")
