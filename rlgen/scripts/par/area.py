"""usage: area.py <dir with readmca.py> <region dir> x0 y0 z0 x1 y1 z1  -> block counts in the box (non-air), most first"""
import sys, collections
sys.path.insert(0, sys.argv[1]); from readmca import chunk, columns
reg = sys.argv[2]; x0, y0, z0, x1, y1, z1 = map(int, sys.argv[3:9]); c = collections.Counter()
for cx in range(x0 >> 4, (x1 >> 4) + 1):
    for cz in range(z0 >> 4, (z1 >> 4) + 1):
        n = chunk(reg, cx, cz)
        if n is None: continue
        for (x, y, z), b in columns(n).items():
            X, Z = cx * 16 + x, cz * 16 + z
            if x0 <= X <= x1 and y0 <= y <= y1 and z0 <= Z <= z1: c[b] += 1
for b, n in c.most_common(int(__import__("os").environ.get("TOP", "60"))): print(n, b)
