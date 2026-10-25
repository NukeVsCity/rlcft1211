"""Lycanites Mobs dungeon JSONs as RLCraft loaded them: the jar's defaults, replaced by the config copy when that one says
"loadDefault": false (JSONLoader.writeDefaultJSONObjects), plus config-only files. Keyed by their "name".
Usage: python3 -I gen_lyc_dungeons.py <jar assets/lycanitesmobs/dungeons> <config/lycanitesmobs/dungeons> <resources dir>"""
import json, glob, os, sys
jar, cfg, res = sys.argv[1:4]
for t in ["schematics", "sectors", "structures", "themes"]:
    d = {}
    for f in sorted(glob.glob(f"{jar}/{t}/**/*.json", recursive=True)):
        j = json.load(open(f)); d[j["name"]] = j
    c = {}
    for f in sorted(glob.glob(f"{cfg}/{t}/**/*.json", recursive=True)):
        j = json.load(open(f)); c[j["name"]] = j
    out = {}
    for n, j in d.items():
        cj = c.get(n)
        load_default = True if cj is None else cj.get("loadDefault", False)
        out[n] = j if load_default else cj
    for n, j in c.items():
        if n not in d:
            out[n] = j
    dst = f"{res}/data/rlgen/lycanites/dungeons/{t}"
    os.makedirs(dst, exist_ok=True)
    for n, j in out.items():
        json.dump(j, open(f"{dst}/{n}.json", "w"), indent=1)
    names = sorted(out)
    json.dump(names, open(f"{dst}/index.json", "w"))
    print(t, names)
