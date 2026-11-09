package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.player.Player;

/** 1.12 MyrmexAIAttackPlayers: players without a hive or with a reputation below 25. */
public class MyrmexAIAttackPlayers extends EntityAINearestAttackableTarget<Player> {
   public MyrmexAIAttackPlayers(final EntityMyrmexBase myrmex) {
      super(myrmex, Player.class, 10, true, true, entity -> entity != null && (myrmex.getHive() == null || myrmex.getHive().isPlayerReputationTooLowToFight(entity.getUUID())));
   }
}
