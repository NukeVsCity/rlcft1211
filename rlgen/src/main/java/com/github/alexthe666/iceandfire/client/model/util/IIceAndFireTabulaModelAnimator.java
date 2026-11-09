package com.github.alexthe666.iceandfire.client.model.util;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface IIceAndFireTabulaModelAnimator<T extends Entity> {
   void setRotationAngles(IceAndFireTabulaModel var1, T var2, float var3, float var4, float var5, float var6, float var7, float var8);
}
