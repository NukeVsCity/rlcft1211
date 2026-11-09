"""usage: srg2mcp.py <stable dir with fields.csv/methods.csv/params.csv> <src dir>  -- renames func_/field_/p_ SRG names in .java files in place"""
import csv, os, re, sys
m = {}
for f in ("fields.csv", "methods.csv", "params.csv"):
    with open(os.path.join(sys.argv[1], f)) as fh:
        for row in csv.DictReader(fh):
            m[row.get("searge") or row.get("param")] = row["name"]
pat = re.compile(r"\b(func_\d+_[a-zA-Z_]+|field_\d+_[a-zA-Z_]+|p_i?\d+_\d+_)\b")
for root, _, files in os.walk(sys.argv[2]):
    for fn in files:
        if fn.endswith(".java"):
            p = os.path.join(root, fn)
            s = open(p, encoding="utf-8", errors="replace").read()
            t = pat.sub(lambda x: m.get(x.group(1), x.group(1)), s)
            if t != s:
                open(p, "w", encoding="utf-8").write(t)
