package com.yungnickyoung.minecraft.bettermineshafts.world;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureComponent;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariants;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.MineshaftPiece;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.VerticalEntrance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** MapGenBetterMineshaft.getStructureStart and Start: the variant for the biome, then the entrance builds every piece. */
public class MapGenBetterMineshaft {
    public static List<StructureComponent> getStructureStart(Biome biome, Random random, int chunkX, int chunkZ) {
        // Determine mineshaft settings
        MineshaftVariantSettings settings = MineshaftVariants.get().getDefault();
        boolean found = true;
        for (MineshaftVariantSettings variant : MineshaftVariants.get().getVariants()) {
            for (List<String> tagList : variant.biomeTags) {
                found = true;
                for (String tag : tagList) {
                    if (!BiomeDict.hasType(biome, tag)) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    settings = variant;
                    break;
                }
            }
            if (found) break;
        }

        List<StructureComponent> components = new ArrayList<>();
        Direction direction = Direction.NORTH;
        int r = random.nextInt(4);
        switch (r) {
            case 0:
                direction = Direction.NORTH;
                break;
            case 1:
                direction = Direction.SOUTH;
                break;
            case 2:
                direction = Direction.EAST;
                break;
            case 3:
                direction = Direction.WEST;
        }
        int minY = settings.minY == 0 ? 17 : settings.minY;
        int maxY = settings.maxY == 0 ? 37 : settings.maxY;
        int y = random.nextInt(maxY - minY + 1) + minY;
        BlockPos.MutableBlockPos startingPos = new BlockPos.MutableBlockPos((chunkX << 4) + 2, y, (chunkZ << 4) + 2);

        // Entrypoint
        MineshaftPiece entryPoint = new VerticalEntrance(0, -1, random, startingPos, direction, settings);
        components.add(entryPoint);
        // Build room component. This also populates the children list, effectively building the entire mineshaft.
        entryPoint.buildComponent(entryPoint, components, random);
        return components;
    }
}
