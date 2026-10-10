package jaredbgreat.dldungeons.builder;

import jaredbgreat.dldungeons.compat.World;

public interface IBlockPlacer {
   void placeNoMeta(World var1, int var2, int var3, int var4);

   void place(World var1, int var2, int var3, int var4);

   Object getContents();
}
