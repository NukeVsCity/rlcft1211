package com.yungnickyoung.minecraft.bettermineshafts.util;

/** A column position (x and z); Better Mineshafts only uses it for surface lookups. */
public class ColPos {
    protected int x, z;

    public ColPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
