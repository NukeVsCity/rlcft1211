package jaredbgreat.dldungeons.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import jaredbgreat.dldungeons.compat.World;

public class BlockFamily implements IBlockPlacer {
   private static final Map<String, BlockFamily> FAMILIES = new HashMap<>();
   private final String name;
   private final IBlockPlacer[] blocks;
   private static Random random;

   private BlockFamily(String name, List<IBlockPlacer> theBlocks) {
      this.name = name;
      this.blocks = theBlocks.toArray(new IBlockPlacer[theBlocks.size()]);
      random = new Random();
   }

   public static void setRadnom(Random r) {
      random = r;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public void placeNoMeta(World world, int x, int y, int z) {
      this.blocks[random.nextInt(this.blocks.length)].placeNoMeta(world, x, y, z);
   }

   @Override
   public void place(World world, int x, int y, int z) {
      this.blocks[random.nextInt(this.blocks.length)].place(world, x, y, z);
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof BlockFamily) ? false : this.name.equals(((BlockFamily)other).name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   public static BlockFamily makeBlockFamily(String json) {
      JsonParser parser = new JsonParser();
      JsonObject data = parser.parse(json).getAsJsonObject();
      final List<IBlockPlacer> blocks = new LinkedList<>();
      if (data.has("name")) {
         String name = "$" + data.get("name").getAsString();
         if (data.has("blocks")) {
            data.get("blocks").getAsJsonArray().forEach(new Consumer<JsonElement>() {
               public void accept(JsonElement t) {
                  int id = RegisteredBlock.add(t.getAsString());
                  blocks.add(RegisteredBlock.getPlacer(id));
               }
            });
            if (blocks.isEmpty()) {
               throw new RuntimeException("DLDungeonsJBG tried to load BlockFamily empty block list!");
            } else {
               BlockFamily out = new BlockFamily(name, blocks);
               FAMILIES.put(name, out);
               return out;
            }
         } else {
            throw new RuntimeException("DLDungeonsJBG tried to load BlockFamily with no blocks!");
         }
      } else {
         throw new RuntimeException("DLDungeonsJBG tried to load BlockFamily with no Name!");
      }
   }

   public static BlockFamily getBlockFamily(String name) {
      return FAMILIES.get(name);
   }

   @Override
   public Object getContents() {
      return this.blocks;
   }
}
