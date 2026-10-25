import re, sys, os
src_root, dst_root = sys.argv[1:3]
FILES = ["world/generator/pieces/" + f for f in os.listdir(os.path.join(src_root, "world/generator/pieces"))] + [
    "util/BlockSetSelector.java", "util/BoxUtil.java", "util/ColPos.java", "util/Pair.java", "util/SurfaceUtil.java",
    "world/generator/BetterMineshaftGenerator.java", "world/generator/MineshaftVariantSettings.java"]
R = [
 (r'import net\.minecraft\.world\.World;', 'import com.yungnickyoung.minecraft.bettermineshafts.mc.World;'),
 (r'import net\.minecraft\.world\.gen\.structure\.StructureBoundingBox;', 'import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureBoundingBox;'),
 (r'import net\.minecraft\.world\.gen\.structure\.StructureComponent;', 'import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureComponent;'),
 (r'import net\.minecraft\.util\.EnumFacing;', 'import net.minecraft.core.Direction;'),
 (r'import net\.minecraft\.block\.state\.IBlockState;', 'import net.minecraft.world.level.block.state.BlockState;'),
 (r'import net\.minecraft\.init\.Blocks;', 'import net.minecraft.world.level.block.Blocks;'),
 (r'import net\.minecraft\.util\.math\.BlockPos;', 'import net.minecraft.core.BlockPos;'),
 (r'import net\.minecraft\.(nbt|world\.gen\.structure\.template|world\.storage\.loot|block|block\.state|block\.material|tileentity|entity\.item|item|util|util\.math|init|world\.chunk)\.[\w*]+;\n', ''),
 (r'import net\.minecraftforge\.common\.BiomeDictionary;\n', ''),
 (r'import javax\.annotation\.ParametersAreNonnullByDefault;\n', ''),
 (r'@ParametersAreNonnullByDefault\n\s*', ''),
 (r'\bIBlockState\b', 'BlockState'),
 (r'EnumFacing\.Axis', 'Direction.Axis'),
 (r'\bEnumFacing\b', 'Direction'),
 (r'\.getXOffset\(\)', '.getStepX()'), (r'\.getZOffset\(\)', '.getStepZ()'), (r'\.getYOffset\(\)', '.getStepY()'),
 (r'\.rotateYCCW\(\)', '.getCounterClockWise()'), (r'\.rotateY\(\)', '.getClockWise()'),
 (r'\.getDefaultState\(\)', '.defaultBlockState()'),
 (r'\.withProperty\(', '.setValue('),
 (r'\.up\(\)', '.above()'), (r'\.down\(\)', '.below()'), (r'\.up\((\w+)\)', r'.above(\1)'), (r'\.down\((\w+)\)', r'.below(\1)'),
 (r'\.setPos\(', '.set('),
 (r'Blocks\.WEB\b', 'Blocks.COBWEB'), (r'Blocks\.MOB_SPAWNER\b', 'Blocks.SPAWNER'), (r'Blocks\.STONEBRICK\b', 'Blocks.STONE_BRICKS'),
 (r'Blocks\.SNOW_LAYER\b', 'Blocks.SNOW'), (r'Blocks\.DEADBUSH\b', 'Blocks.DEAD_BUSH'), (r'Blocks\.HARDENED_CLAY\b', 'Blocks.TERRACOTTA'),
 (r'Blocks\.TRAPDOOR\b', 'Blocks.OAK_TRAPDOOR'), (r'Blocks\.STONE_STAIRS\b', 'Blocks.COBBLESTONE_STAIRS'),
 (r'LootTableList\.CHESTS_ABANDONED_MINESHAFT', 'BuiltInLootTables.ABANDONED_MINESHAFT'),
 (r'BiomeDictionary\.hasType\(([^,]+), BiomeDictionary\.Type\.(\w+)\)', r'BiomeDict.hasType(\1, "\2")'),
 (r'\bBiomeDictionary\.Type\b', 'String'),
 (r'Blocks\.STONE_SLAB\.defaultBlockState\(\)\.setValue\(BlockStoneSlab\.VARIANT, BlockStoneSlab\.EnumType\.COBBLESTONE\)', 'Blocks.COBBLESTONE_SLAB.defaultBlockState()'),
 (r'Blocks\.STONE_SLAB\.defaultBlockState\(\)\.setValue\(BlockSlab\.HALF, BlockSlab\.EnumBlockHalf\.TOP\)', 'Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)'),
 (r'BlockRail(Base)?\.EnumRailDirection\.', 'RailShape.'), (r'BlockRail\.SHAPE', 'RailBlock.SHAPE'),
 (r'BlockStairs\.EnumHalf\.', 'Half.'), (r'BlockStairs\.EnumShape\.', 'StairsShape.'), (r'BlockStairs\.', 'StairBlock.'),
 (r'BlockBed\.EnumPartType\.', 'BedPart.'), (r'BlockBed\.', 'BedBlock.'),
 (r'BlockDoor\.EnumDoorHalf\.', 'DoubleBlockHalf.'), (r'BlockDoor\.', 'DoorBlock.'),
 (r'BlockLadder\.', 'LadderBlock.'), (r'BlockFurnace\.', 'FurnaceBlock.'), (r'BlockTrapDoor\.', 'TrapDoorBlock.'),
 (r'BlockFence\.', 'FenceBlock.'), (r'BlockVine\.', 'VineBlock.'), (r'BlockSnow\.', 'SnowLayerBlock.'), (r'BlockCactus\.', 'CactusBlock.'),
]
for f in FILES:
    s = open(os.path.join(src_root, f)).read()
    for a, b in R:
        s = re.sub(a, b, s)
    out = os.path.join(dst_root, f)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    open(out, "w").write(s)

IMPORTS = """import com.yungnickyoung.minecraft.bettermineshafts.mc.*;
import com.nukevscity.rlgen.rc.BiomeDict;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
"""

def drop_method(s, name):
    while True:
        m = re.search(r'\n(\s*@Override\s*\n)?\s*(protected|public|private)[^\n;{]*\b' + name + r'\s*\([^)]*\)\s*\{', s)
        if not m:
            return s
        i = m.end()
        depth = 1
        while depth:
            if s[i] == '{': depth += 1
            elif s[i] == '}': depth -= 1
            i += 1
        s = s[:m.start()] + s[i:]

for f in FILES:
    out = os.path.join(dst_root, f)
    s = open(out).read()
    for name in ["writeStructureToNBT", "readStructureFromNBT"]:
        s = drop_method(s, name)
    # the 1.12 no-arg constructors were for loading from NBT
    s = re.sub(r'\n\s*public (\w+)\(\) \{\s*\}\n', '\n', s)
    s = re.sub(r'(package [\w.]+;\n)', r'\1\n' + IMPORTS.replace('\\', '\\\\'), s, count=1)
    s = re.sub(r'import com\.yungnickyoung\.minecraft\.bettermineshafts\.mc\.(World|StructureBoundingBox|StructureComponent);\n', '', s)
    open(out, "w").write(s)

def edit(f, pairs):
    p = os.path.join(dst_root, f)
    s = open(p).read()
    for a, b in pairs:
        if a not in s:
            raise SystemExit("edit miss in " + f + ": " + a)
        s = s.replace(a, b)
    open(p, "w").write(s)

edit("util/BlockSetSelector.java", [
    ("private Map<BlockState, Float> entries = new HashMap<>();", "private List<Map.Entry<BlockState, Float>> entries = new ArrayList<>();"),
    ("for (Map.Entry<BlockState, Float> entry : entries.entrySet())", "for (Map.Entry<BlockState, Float> entry : entries)"),
    ("float currTotal = entries.values().stream()", "float currTotal = entries.stream().map(Map.Entry::getValue)"),
    ("entries.put(blockState, chance);", "entries.add(Map.entry(blockState, chance));"),
    ("    public BlockState get(Random random) {", "    /** An entry as Gson read it from variants.json (no checks, JSON order). */\n    public void put(BlockState blockState, float chance) {\n        entries.add(Map.entry(blockState, chance));\n    }\n\n    public BlockState get(Random random) {"),
])
edit("world/generator/pieces/SmallTunnel.java", [
    ("leftTorch.getBlock().canPlaceBlockAt(world, pos)", "Integrations.canPlaceBlockAt(leftTorch, world, pos)"),
    ("rightTorch.getBlock().canPlaceBlockAt(world, pos)", "Integrations.canPlaceBlockAt(rightTorch, world, pos)"),
])
p = os.path.join(dst_root, "util/SurfaceUtil.java"); open(p, "w").write(open(p).read().replace("world.getBlockState(", "world.getTerrainState("))
