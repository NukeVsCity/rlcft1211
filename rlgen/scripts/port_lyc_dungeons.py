"""One-off translation of Lycanites Mobs 2.0.8.9's decompiled dungeon code (MCP names) to 1.21 types; hand fixes followed.
Usage: python3 port_lyc_dungeons.py <decompiled core/dungeon dir> <rlgen src/main/java>"""
import re, sys, os
src, dst = sys.argv[1:3]
R = [
 (r'import net\.minecraft\.world\.World;', 'import com.yungnickyoung.minecraft.bettermineshafts.mc.World;'),
 (r'import net\.minecraft\.(block|block\.state|block\.material|util|util\.math|init|world|world\.chunk|world\.biome|tileentity|entity|nbt)\.[\w.*]+;\n', ''),
 (r'import net\.minecraftforge\.[\w.*]+;\n', ''),
 (r'import com\.lycanitesmobs\.(?!core\.dungeon)[\w.*]+;\n', ''),
 (r'import org\.apache\.commons\.lang3\.math\.NumberUtils;', 'import org.apache.commons.lang3.math.NumberUtils;'),
 (r'(package [\w.]+;\n)', r'\1\nimport net.minecraft.core.BlockPos;\nimport net.minecraft.core.BlockPos.MutableBlockPos;\nimport net.minecraft.core.Direction;\nimport net.minecraft.core.Vec3i;\nimport net.minecraft.world.level.ChunkPos;\nimport net.minecraft.world.level.block.*;\nimport net.minecraft.world.level.block.state.BlockState;\nimport com.nukevscity.rlgen.lyc.dungeon.*;\n'),
 (r'\bIBlockState\b', 'BlockState'), (r'\bEnumFacing\b', 'Direction'),
 (r'Direction\.HORIZONTALS', 'HORIZONTALS'),
 (r'\.getDefaultState\(\)', '.defaultBlockState()'), (r'\.withRotation\(', '.rotate('),
 (r'\.setPos\(', '.set('), (r'\b(bounds|position|occupiedBoundsMin|getCenter\(\)|getOccupiedBoundsMax\(\)|getOccupiedBoundsMin\(\)|getRoomBoundsMax\(\)|getRoomBoundsMin\(\))\.add\(', r'\1.offset('),
 (r'\.getXStart\(\)', '.getMinBlockX()'), (r'\.getXEnd\(\)', '.getMaxBlockX()'), (r'\.getZStart\(\)', '.getMinBlockZ()'), (r'\.getZEnd\(\)', '.getMaxBlockZ()'),
 (r'world\.getHeight\(\)', '256'),
 (r'Blocks\.MOB_SPAWNER\.getStateFromMeta\(1\)', 'Blocks.SPAWNER.defaultBlockState()'),
 (r'LycanitesMobs\.log(Debug|Info)\([^;]*\);', ''),
 (r'LycanitesMobs\.logWarning\(', 'Lyc.warn('),
]
for root, _, files in os.walk(src):
    for f in files:
        p = os.path.join(root, f)
        rel = os.path.relpath(p, src)
        s = open(p).read()
        for a, b in R:
            s = re.sub(a, b, s)
        out = os.path.join(dst, "com/lycanitesmobs/core/dungeon", rel)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        open(out, "w").write(s)
