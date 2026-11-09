package com.github.alexthe666.iceandfire.entity;

import net.minecraft.world.entity.player.Player;

/** 1.12 IDragonFlute: entities that react to the dragon flute (EntityDragonBase implements it). */
public interface IDragonFlute {
   void onHearFlute(Player player);
}
