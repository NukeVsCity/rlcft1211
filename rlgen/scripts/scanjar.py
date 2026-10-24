import sys, zipfile, struct, os, json, re, io
def parse(data):
    if data[:4] != b'\xca\xfe\xba\xbe': return None
    n = struct.unpack('>H', data[8:10])[0]; i = 10; cp = [None]*n; k = 1
    while k < n:
        t = data[i]
        if t == 1:
            l = struct.unpack('>H', data[i+1:i+3])[0]; cp[k] = ('u', data[i+3:i+3+l].decode('utf8','replace')); i += 3+l
        elif t in (3,4): i += 5
        elif t in (5,6): i += 9; k += 1
        elif t == 7: cp[k] = ('c', struct.unpack('>H', data[i+1:i+3])[0]); i += 3
        elif t == 8: cp[k] = ('s', struct.unpack('>H', data[i+1:i+3])[0]); i += 3
        elif t in (9,10,11,12): cp[k] = ('r', struct.unpack('>HH', data[i+1:i+5])); i += 5
        elif t == 15: i += 4
        elif t == 16: i += 3
        elif t == 18: i += 5
        elif t in (19,20): i += 3
        else: return None
        k += 1
    sup = struct.unpack('>H', data[i+4:i+6])[0]
    utf = [c[1] for c in cp if c and c[0]=='u']
    def cname(ix):
        c = cp[ix]; return cp[c[1]][1] if c and c[0]=='c' else None
    this = cname(struct.unpack('>H', data[i+2:i+4])[0]); s = cname(sup) if sup else None
    return this, s, set(utf)
MARK = {
 'BIOME_ADD': lambda u: 'net/minecraftforge/common/BiomeManager' in u and ('addBiome' in u),
 'BIOME_REMOVE': lambda u: 'net/minecraftforge/common/BiomeManager' in u and 'removeBiome' in u,
 'WORLDGEN': lambda u: 'net/minecraftforge/fml/common/IWorldGenerator' in u,
 'TERRAIN_EVENT': lambda u: any(x.startswith('net/minecraftforge/event/terraingen/') for x in u),
 'POPULATE_EVENT': lambda u: any('PopulateChunkEvent' in x for x in u),
 'DECORATE_EVENT': lambda u: any('DecorateBiomeEvent' in x for x in u),
 'OREGEN_EVENT': lambda u: any('OreGenEvent' in x for x in u),
 'MAPGEN_EVENT': lambda u: any('InitMapGenEvent' in x for x in u),
 'NOISE_EVENT': lambda u: any('InitNoiseGensEvent' in x or 'ChunkGeneratorEvent' in x for x in u),
 'VILLAGE': lambda u: any('StructureVillagePieces' in x or 'VillagerRegistry$IVillageCreationHandler' in x or 'GetVillageBlockID' in x for x in u),
 'STRUCTURE_IO': lambda u: 'net/minecraft/world/gen/structure/MapGenStructureIO' in u,
 'WORLDTYPE': lambda u: 'net/minecraft/world/WorldType' in u,
 'DIMENSION': lambda u: 'net/minecraftforge/common/DimensionManager' in u,
 'LOOT_INJECT': lambda u: any('LootTableLoadEvent' in x for x in u),
 'CHUNKGEN_REF': lambda u: any(x in u for x in ('net/minecraft/world/gen/ChunkGeneratorOverworld','net/minecraft/world/gen/layer/GenLayer','net/minecraft/world/biome/BiomeDecorator','net/minecraft/world/gen/MapGenCaves','net/minecraft/world/gen/MapGenRavine','net/minecraft/world/biome/BiomeProvider')),
 'MIXIN_OR_ASM': lambda u: any(x.startswith('Lorg/spongepowered/asm/mixin/') for x in u) or 'net/minecraft/launchwrapper/IClassTransformer' in u,
}
GENSUPER = ('net/minecraft/world/biome/Biome','net/minecraft/world/gen/feature/WorldGenerator','net/minecraft/world/gen/feature/WorldGenAbstractTree','net/minecraft/world/gen/MapGenBase','net/minecraft/world/gen/structure/MapGenStructure','net/minecraft/world/gen/structure/StructureStart','net/minecraft/world/gen/structure/StructureComponent','net/minecraft/world/WorldType','net/minecraft/world/WorldProvider','net/minecraft/world/gen/layer/GenLayer')
out = {}
for jar in sorted(os.listdir(sys.argv[1])):
    res = {k: [] for k in MARK}; supers = {}
    mixin_targets = []
    try:
        z = zipfile.ZipFile(os.path.join(sys.argv[1], jar))
    except Exception as e:
        out[jar] = {'error': str(e)}; continue
    for n in z.namelist():
        if n.endswith('.json') and 'mixin' in n.lower():
            try: mixin_targets.append(n)
            except: pass
        if not n.endswith('.class'): continue
        p = parse(z.read(n))
        if not p: continue
        this, s, u = p
        for k, f in MARK.items():
            if f(u): res[k].append(this)
        if s in GENSUPER: supers.setdefault(s.split('/')[-1], []).append(this)
        if any(x.startswith('Lorg/spongepowered/asm/mixin/') for x in u):
            for x in u:
                if x.startswith('net/minecraft/world/gen') or x.startswith('net/minecraft/world/biome') or x.startswith('net/minecraft/world/chunk'):
                    mixin_targets.append(this + ' -> ' + x)
    out[jar] = {k: v for k, v in res.items() if v}
    if supers: out[jar]['SUBCLASSES'] = supers
    if mixin_targets: out[jar]['MIXIN_TARGETS'] = mixin_targets[:30]
json.dump(out, open(sys.argv[2], 'w'), indent=1)
