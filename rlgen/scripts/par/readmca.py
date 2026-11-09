"""1.21 region reader: chunk(region_dir, cx, cz) -> chunk NBT or None; columns(chunk) -> {(x,y,z): block name} (non-air)."""
import os, struct, zlib, io, gzip, nbtlib
def chunk(reg, cx, cz):
    p = os.path.join(reg, f"r.{cx >> 5}.{cz >> 5}.mca")
    if not os.path.exists(p): return None
    with open(p, "rb") as f:
        i = (cx & 31) + (cz & 31) * 32
        f.seek(i * 4); off = int.from_bytes(f.read(3), "big") * 4096
        if not off: return None
        f.seek(off); ln, ct = struct.unpack(">IB", f.read(5)); d = f.read(ln - 1)
    d = zlib.decompress(d) if ct == 2 else gzip.decompress(d) if ct == 1 else d
    return nbtlib.File.parse(io.BytesIO(d))
def columns(n):
    out = {}
    for s in n.get("sections", []):
        bs = s.get("block_states")
        if not bs: continue
        pal = [str(p["Name"]) for p in bs["palette"]]
        y0 = int(s["Y"]) * 16
        if "data" not in bs:
            if pal[0] not in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air"):
                for i in range(4096): out[(i & 15, y0 + (i >> 8), (i >> 4) & 15)] = pal[0]
            continue
        bits = max(4, (len(pal) - 1).bit_length()); per = 64 // bits; mask = (1 << bits) - 1
        data = [int(v) & 0xFFFFFFFFFFFFFFFF for v in bs["data"]]
        for i in range(4096):
            v = (data[i // per] >> ((i % per) * bits)) & mask
            b = pal[v]
            if b not in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air"):
                out[(i & 15, y0 + (i >> 8), (i >> 4) & 15)] = b
    return out
