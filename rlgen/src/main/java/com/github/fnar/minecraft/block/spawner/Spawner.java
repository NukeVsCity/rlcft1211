package com.github.fnar.minecraft.block.spawner;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.google.common.collect.Lists;
import java.util.List;

public class Spawner extends SingleBlockBrush {
   private final List<SpawnPotential> potentials = Lists.newArrayList();

   public Spawner(List<SpawnPotential> spawnPotentials) {
      super(BlockType.MOB_SPAWNER);
      this.potentials.addAll(spawnPotentials);
   }

   public List<SpawnPotential> getPotentials() {
      return this.potentials;
   }
}
