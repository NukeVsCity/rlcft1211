package com.yungnickyoung.minecraft.bettermineshafts.world.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yungnickyoung.minecraft.bettermineshafts.util.BlockSetSelector;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/**
 * RLCraft's config/bettermineshafts/1_12_2/variants.json, with the block states flattened to 1.21 by scripts/gen_bms_variants.py
 * (data/rlgen/bettermineshafts/variants.json). 1.12 read it with Gson straight into these fields.
 */
public class MineshaftVariants {
    private static MineshaftVariants instance;
    private final List<MineshaftVariantSettings> variants = new ArrayList<>();
    private MineshaftVariantSettings defaultVariant;

    public static synchronized MineshaftVariants get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public List<MineshaftVariantSettings> getVariants() {
        return this.variants;
    }

    public MineshaftVariantSettings getDefault() {
        return this.defaultVariant;
    }

    private static MineshaftVariants load() {
        MineshaftVariants v = new MineshaftVariants();
        try (Reader r = new InputStreamReader(MineshaftVariants.class.getResourceAsStream("/data/rlgen/bettermineshafts/variants.json"), StandardCharsets.UTF_8)) {
            JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
            for (JsonElement e : o.getAsJsonArray("variants")) {
                v.variants.add(settings(e.getAsJsonObject()));
            }
            v.defaultVariant = settings(o.getAsJsonObject("defaultVariant"));
        } catch (Exception e) {
            throw new IllegalStateException("rlgen: can't read Better Mineshafts variants", e);
        }
        return v;
    }

    private static BlockState state(JsonElement e) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), e.getAsString(), false).blockState();
        } catch (Exception ex) {
            com.yungnickyoung.minecraft.bettermineshafts.BetterMineshafts.LOGGER.warn("rlgen: unknown block {} in Better Mineshafts variants", e.getAsString());
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
    }

    private static BlockSetSelector selector(JsonObject o) {
        BlockSetSelector s = new BlockSetSelector(state(o.get("defaultBlock")));
        for (JsonElement e : o.getAsJsonArray("entries")) {
            JsonArray a = e.getAsJsonArray();
            s.put(state(a.get(0)), a.get(1).getAsFloat());
        }
        return s;
    }

    private static MineshaftVariantSettings settings(JsonObject o) {
        MineshaftVariantSettings s = new MineshaftVariantSettings();
        s.biomeTags = new ArrayList<>();
        if (o.has("biomeTags")) {
            for (JsonElement l : o.getAsJsonArray("biomeTags")) {
                List<String> tags = new ArrayList<>();
                for (JsonElement t : l.getAsJsonArray()) {
                    tags.add(t.getAsString());
                }
                s.biomeTags.add(tags);
            }
        }
        s.mainSelector = selector(o.getAsJsonObject("mainSelector"));
        s.floorSelector = selector(o.getAsJsonObject("floorSelector"));
        s.brickSelector = selector(o.getAsJsonObject("brickSelector"));
        s.legSelector = selector(o.getAsJsonObject("legSelector"));
        s.mainBlock = state(o.get("mainBlock"));
        s.supportBlock = state(o.get("supportBlock"));
        s.slabBlock = state(o.get("slabBlock"));
        s.gravelBlock = state(o.get("gravelBlock"));
        s.stoneWallBlock = state(o.get("stoneWallBlock"));
        s.stoneSlabBlock = state(o.get("stoneSlabBlock"));
        s.minY = o.get("minY").getAsInt();
        s.maxY = o.get("maxY").getAsInt();
        s.vineChance = o.get("vineChance").getAsFloat();
        s.snowChance = o.get("snowChance").getAsFloat();
        s.cactusChance = o.get("cactusChance").getAsFloat();
        s.deadBushChance = o.get("deadBushChance").getAsFloat();
        s.mushroomChance = o.get("mushroomChance").getAsFloat();
        s.legVariant = o.get("legVariant").getAsInt();
        s.replacementRate = o.get("replacementRate").getAsFloat();
        return s;
    }
}
