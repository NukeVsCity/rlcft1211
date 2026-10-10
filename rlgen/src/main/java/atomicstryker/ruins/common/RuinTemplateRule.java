package atomicstryker.ruins.common;

import atomicstryker.ruins.common.RuinsWorld.Tile;
import com.nukevscity.rlgen.legacy.LegacyItemNbt;
import com.nukevscity.rlgen.legacy.LegacyStates;
import java.io.PrintWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/** rlgen: blocks are 1.12 names ("minecraft:stonebrick") with 1.12 metadata, flattened through LegacyStates when placed. */
public class RuinTemplateRule {
   protected final String[] blockIDs;
   protected final String[] blockMDs;
   protected final BlockState[] blockBSs;
   protected final String[] blockStrings;
   protected final RuinTemplateRule.SpecialFlags[] specialFlags;
   protected final RuinTemplateRule.PreservePolicy[] preservePolicies;
   protected final double[] blockWeights;
   protected double blockWeightsTotal;
   private double chance = 1.0;
   private int condition = 0;
   final RuinTemplate owner;
   private final PrintWriter debugPrinter;
   private final boolean excessiveDebugging;
   private static final Pattern patternInitialCommandBlock = Pattern.compile("(?:[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[Ee][+-]?\\d+)?\\*)?[?!]?CommandBlock:");
   private static final Pattern patternCommandBlockPrefix = Pattern.compile("(.*,)(?:([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[Ee][+-]?\\d+)?)\\*)?([?!])?");
   private static final Pattern patternBlockPrefix = Pattern.compile("(?:([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[Ee][+-]?\\d+)?)\\*)?([?!])?(.*)");
   static final int UNSPECIFIED_BLOCKNUM = -1;

   public RuinTemplateRule(PrintWriter dpw, RuinTemplate r, String rule, boolean debug) throws Exception {
      this.debugPrinter = dpw;
      this.owner = r;
      this.excessiveDebugging = debug;
      RuinTextLumper lumper = new RuinTextLumper(this.owner, this.excessiveDebugging ? this.debugPrinter : null);
      rule = lumper.lump(rule);
      String[] blockRules = rule.split(",");
      int numblocks = blockRules.length - 2;
      if (numblocks < 1) {
         throw new Exception("No blockIDs specified for rule [" + rule + "] in template " + this.owner.getName());
      } else {
         this.condition = Integer.parseInt(blockRules[0]);
         this.chance = Math.min(Math.max(Double.parseDouble(blockRules[1]) / 100.0, 0.0), 1.0);
         if (patternInitialCommandBlock.matcher(blockRules[2]).lookingAt()) {
            String[] commandrules = rule.split("CommandBlock:");
            int count = commandrules.length - 1;
            double blockWeight = 1.0;
            RuinTemplateRule.PreservePolicy preservePolicy = RuinTemplateRule.PreservePolicy.IGNORE;
            Matcher matcher0 = patternCommandBlockPrefix.matcher(commandrules[0]);
            if (matcher0.matches()) {
               if (matcher0.group(2) != null) {
                  blockWeight = Math.max(Double.parseDouble(matcher0.group(2)), 0.0);
               }

               if (matcher0.group(3) != null) {
                  preservePolicy = matcher0.group(3).equals("?") ? RuinTemplateRule.PreservePolicy.PRESERVE : RuinTemplateRule.PreservePolicy.CORRUPT;
               }

               commandrules[0] = matcher0.group(1);
            }

            this.blockIDs = new String[count];
            this.blockMDs = new String[count];
            this.blockBSs = new BlockState[count];
            this.blockStrings = new String[count];
            this.specialFlags = new RuinTemplateRule.SpecialFlags[count];
            this.preservePolicies = new RuinTemplateRule.PreservePolicy[count];
            this.blockWeights = new double[count];
            this.blockWeightsTotal = 0.0;

            for (int i = 0; i < count; i++) {
               this.blockBSs[i] = null;
               this.blockWeightsTotal = this.blockWeightsTotal + (this.blockWeights[i] = blockWeight);
               this.preservePolicies[i] = preservePolicy;
               blockWeight = 1.0;
               preservePolicy = RuinTemplateRule.PreservePolicy.IGNORE;
               if (i < count - 1) {
                  Matcher matcher = patternCommandBlockPrefix.matcher(commandrules[i + 1]);
                  if (matcher.matches()) {
                     if (matcher.group(2) != null) {
                        blockWeight = Math.max(Double.parseDouble(matcher.group(2)), 0.0);
                     }

                     if (matcher.group(3) != null) {
                        preservePolicy = matcher.group(3).equals("?") ? RuinTemplateRule.PreservePolicy.PRESERVE : RuinTemplateRule.PreservePolicy.CORRUPT;
                     }

                     commandrules[i + 1] = matcher.group(1);
                  }
               }

               this.blockIDs[i] = null;
               this.blockMDs[i] = null;
               if (commandrules[i + 1].charAt(commandrules[i + 1].length() - 2) == '-') {
                  this.blockMDs[i] = "" + commandrules[i + 1].charAt(commandrules[i + 1].length() - 1);
                  commandrules[i + 1] = commandrules[i + 1].substring(0, commandrules[i + 1].length() - 3);
               }

               this.specialFlags[i] = RuinTemplateRule.SpecialFlags.COMMANDBLOCK;
               this.blockStrings[i] = commandrules[i + 1];
               this.blockStrings[i] = lumper.unlump(this.blockStrings[i]);
               if (this.excessiveDebugging) {
                  this.debugPrinter
                     .println(
                        "template "
                           + this.owner.getName()
                           + " contains Command Block command: "
                           + this.blockStrings[i]
                           + " with meta: "
                           + this.blockMDs[i]
                           + ", weight: "
                           + this.blockWeights[i]
                           + ", preserve: "
                           + this.preservePolicies[i]
                     );
               }
            }
         } else {
            this.blockIDs = new String[numblocks];
            this.blockMDs = new String[numblocks];
            this.blockBSs = new BlockState[numblocks];
            this.blockStrings = new String[numblocks];
            this.specialFlags = new RuinTemplateRule.SpecialFlags[numblocks];
            this.preservePolicies = new RuinTemplateRule.PreservePolicy[numblocks];
            this.blockWeights = new double[numblocks];
            this.blockWeightsTotal = 0.0;

            for (int i = 0; i < numblocks; i++) {
               this.blockBSs[i] = null;
               double blockWeight = 1.0;
               RuinTemplateRule.PreservePolicy preservePolicy = RuinTemplateRule.PreservePolicy.IGNORE;
               Matcher matcher = patternBlockPrefix.matcher(blockRules[i + 2]);
               if (matcher.matches()) {
                  if (matcher.group(1) != null) {
                     blockWeight = Math.max(Double.parseDouble(matcher.group(1)), 0.0);
                  }

                  if (matcher.group(2) != null) {
                     preservePolicy = matcher.group(2).equals("?") ? RuinTemplateRule.PreservePolicy.PRESERVE : RuinTemplateRule.PreservePolicy.CORRUPT;
                  }

                  blockRules[i + 2] = matcher.group(3);
               }

               this.blockWeightsTotal = this.blockWeightsTotal + (this.blockWeights[i] = blockWeight);
               this.preservePolicies[i] = preservePolicy;
               String[] data = blockRules[i + 2].split("-");
               if (data.length <= 1) {
                  if (this.isNumber(blockRules[i + 2])) {
                     this.debugPrinter.println("Rule [" + rule + "] in template " + this.owner.getName() + " still uses numeric blockIDs! ERROR!");
                     this.blockIDs[i] = r.getAirBlock();
                     this.blockStrings[i] = "";
                  } else {
                     this.blockIDs[i] = r.tryFindingBlockOfName(blockRules[i + 2]);
                     if (this.blockIDs[i] == r.getAirBlock() && !this.isAir(blockRules[i + 2])) {
                        this.blockIDs[i] = null;
                     }

                     this.blockStrings[i] = blockRules[i + 2];
                     this.blockStrings[i] = lumper.unlump(this.blockStrings[i]);
                  }

                  this.blockMDs[i] = null;
               } else if (this.isNumber(data[0])) {
                  this.debugPrinter.println("Rule [" + rule + "] in template " + this.owner.getName() + " still uses numeric blockIDs! ERROR!");
                  this.blockIDs[i] = r.getAirBlock();
                  this.blockMDs[i] = null;
                  this.blockStrings[i] = "";
               } else {
                  while (data.length > 1 && !this.isNumber(data[1]) && !data[1].equals("addbonemeal")) {
                     String[] newdata = new String[data.length - 1];
                     newdata[0] = data[0] + "-" + data[1];

                     for (int j = 1; j < data.length - 1; j++) {
                        newdata[j] = data[j + 1];
                     }

                     data = newdata;
                  }

                  this.blockStrings[i] = blockRules[i + 2];
                  this.blockStrings[i] = lumper.unlump(this.blockStrings[i]);
                  this.blockIDs[i] = r.tryFindingBlockOfName(data[0]);
                  if (this.blockIDs[i] == r.getAirBlock() && !this.isAir(data[0])) {
                     this.blockIDs[i] = null;
                     if (!this.isKnownSpecialRule(this.blockStrings[i]) && data[0].contains(":") && !data[0].startsWith("minecraft:")) {
                        this.blockIDs[i] = r.getAirBlock(); // rlgen: a block of a mod we do not have
                     } else if (!this.isKnownSpecialRule(this.blockStrings[i])) {
                        throw new Exception(
                           "Rule ["
                              + rule
                              + "], blockString ["
                              + this.blockStrings[i]
                              + "] in template "
                              + this.owner.getName()
                              + " can absolutely not be mapped to anything known"
                        );
                     }
                  }

                  if (data[data.length - 1].equals("addbonemeal")) {
                     this.specialFlags[i] = RuinTemplateRule.SpecialFlags.ADDBONEMEAL;

                     try {
                        this.blockMDs[i] = data[data.length - 2];
                     } catch (NumberFormatException var18) {
                        this.blockMDs[i] = null;
                     }
                  } else {
                     try {
                        this.blockMDs[i] = data[data.length - 1];
                     } catch (NumberFormatException var17) {
                        this.blockMDs[i] = null;
                     }
                  }
               }

               if (this.excessiveDebugging) {
                  this.debugPrinter
                     .printf(
                        "rule alternative: %d, blockIDs[%s], blockMDs[%s], blockStrings[%s], specialflags:[%s], blockWeights:[%f], preservePolicies:[%s]\n",
                        i + 1,
                        this.blockIDs[i],
                        this.blockMDs[i],
                        this.blockStrings[i],
                        this.specialFlags[i],
                        this.blockWeights[i],
                        this.preservePolicies[i]
                     );
               }
            }
         }
      }
   }

   RuinTemplateRule(PrintWriter dpw, RuinTemplate r, String rule) throws Exception {
      this(dpw, r, rule, false);
   }

   private boolean isNumber(String s) {
      if (s != null && !s.equals("")) {
         try {
            int n = Integer.parseInt(s);
            return true;
         } catch (NumberFormatException var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public void doBlock(RuinsWorld world, Random random, int x, int y, int z, int rotate) {
      if (random.nextDouble() < this.chance) {
         switch (this.condition) {
            case -3:
               this.doUnderBlock(world, random, x, y, z, rotate);
               break;
            case -2:
               this.doAdjacentBlock(world, random, x, y, z, rotate);
               break;
            case -1:
               this.doAboveBlock(world, random, x, y, z, rotate);
               break;
            case 0:
            default:
               this.doNormalBlock(world, random, x, y, z, rotate);
               break;
            case 1:
               this.doAboveBlock(world, random, x, y, z, rotate);
               break;
            case 2:
               this.doAdjacentBlock(world, random, x, y, z, rotate);
               break;
            case 3:
               this.doUnderBlock(world, random, x, y, z, rotate);
         }
      }
   }

   public boolean runLater() {
      switch (this.condition <= 0 ? 0 - this.condition : this.condition) {
         case 1:
            return true;
         case 2:
            return true;
         default:
            return false;
      }
   }

   public boolean runLast() {
      switch (this.condition <= 0 ? 0 - this.condition : this.condition) {
         case 3:
            return true;
         case 7:
            return true;
         default:
            return false;
      }
   }

   private void doNormalBlock(RuinsWorld world, Random random, int x, int y, int z, int rotate) {
      int blocknum = this.getBlockNum(random);
      this.handleBlockSpawning(world, random, x, y, z, blocknum, rotate, this.blockStrings[blocknum]);
   }

   private void doAboveBlock(RuinsWorld world, Random random, int x, int y, int z, int rotate) {
      BlockPos pos = new BlockPos(x, y - 1, z);
      if (!(this.condition <= 0 ^ this.owner.isIgnoredBlock(world.getBlockState(pos)))) {
         int blocknum = this.getBlockNum(random);
         this.handleBlockSpawning(world, random, x, y, z, blocknum, rotate, this.blockStrings[blocknum]);
      }
   }

   private void doAdjacentBlock(RuinsWorld world, Random random, int x, int y, int z, int rotate) {
      BlockPos a = new BlockPos(x + 1, y, z);
      BlockPos b = new BlockPos(x, y, z + 1);
      BlockPos c = new BlockPos(x, y, z - 1);
      BlockPos d = new BlockPos(x - 1, y, z);
      if (!(
         this.condition <= 0
            ^ (
               this.owner.isIgnoredBlock(world.getBlockState(a))
                  && this.owner.isIgnoredBlock(world.getBlockState(b))
                  && this.owner.isIgnoredBlock(world.getBlockState(c))
                  && this.owner.isIgnoredBlock(world.getBlockState(d))
            )
      )) {
         int blocknum = this.getBlockNum(random);
         this.handleBlockSpawning(world, random, x, y, z, blocknum, rotate, this.blockStrings[blocknum]);
      }
   }

   private void doUnderBlock(RuinsWorld world, Random random, int x, int y, int z, int rotate) {
      BlockPos pos = new BlockPos(x, y + 1, z);
      if (!(this.condition <= 0 ^ this.owner.isIgnoredBlock(world.getBlockState(pos)))) {
         int blocknum = this.getBlockNum(random);
         this.handleBlockSpawning(world, random, x, y, z, blocknum, rotate, this.blockStrings[blocknum]);
      }
   }

   private void handleBlockSpawning(RuinsWorld world, Random random, int x, int y, int z, int blocknum, int rotate, String blockString) {
      String blockID = this.blockIDs[blocknum];
      BlockPos pos = new BlockPos(x, y, z);
      if (this.preservePolicies[blocknum] != RuinTemplateRule.PreservePolicy.IGNORE
         && this.preservePolicies[blocknum]
            == RuinTemplateRule.PreservePolicy.PRESERVE
            == this.owner.isIgnoredBlock(world.getBlockState(pos))) {
         if (this.excessiveDebugging) {
            this.debugPrinter
               .println(
                  "Suppressing placement of blockID "
                     + blockID
                     + ", meta "
                     + this.blockMDs[blocknum]
                     + " preserve "
                     + this.preservePolicies[blocknum]
                     + " rotation "
                     + rotate
                     + ", string: "
                     + blockString
               );
         }
      } else {
         if (this.excessiveDebugging) {
            this.debugPrinter
               .println(
                  "About to place blockID "
                     + blockID
                     + ", meta "
                     + this.blockMDs[blocknum]
                     + " preserve "
                     + this.preservePolicies[blocknum]
                     + " rotation "
                     + rotate
                     + ", string: "
                     + blockString
               );
         }

         if (blockID == null) {
            this.doSpecialBlock(world, random, x, y, z, blocknum, rotate, blockString);
         } else {
            this.placeBlock(world, blocknum, x, y, z, rotate);
         }
      }
   }

   private void placeBlock(RuinsWorld world, int blocknum, int x, int y, int z, int rotate) {
      this.realizeBlock(world, x, y, z, this.blockIDs[blocknum], blocknum, rotate);
      if (this.specialFlags[blocknum] != null) {
         switch (this.specialFlags[blocknum]) {
            case ADDBONEMEAL:
               this.owner.markBlockForBonemeal(x, y, z);
         }
      }
   }

   private boolean isKnownSpecialRule(String dataString) {
      if (dataString.equals("preserveBlock")) {
         return true;
      } else if (dataString.startsWith("MobSpawner:")) {
         return true;
      } else if (dataString.equals("UprightMobSpawn")) {
         return true;
      } else if (dataString.equals("EasyMobSpawn")) {
         return true;
      } else if (dataString.equals("MediumMobSpawn")) {
         return true;
      } else if (dataString.equals("HardMobSpawn")) {
         return true;
      } else if (dataString.startsWith("EasyChest")) {
         return true;
      } else if (dataString.startsWith("MediumChest")) {
         return true;
      } else if (dataString.startsWith("HardChest")) {
         return true;
      } else if (dataString.startsWith("ChestGenHook:")) {
         return true;
      } else if (dataString.startsWith("IInventory;")) {
         return LegacyStates.block(dataString.split(";")[1], 0) != null;
      } else if (dataString.equals("EnderCrystal")) {
         return true;
      } else if (dataString.startsWith("CommandBlock:")) {
         return true;
      } else if (dataString.startsWith("StandingSign:")) {
         return true;
      } else if (dataString.startsWith("WallSign:")) {
         return true;
      } else {
         return dataString.startsWith("Skull:") ? true : dataString.startsWith("teBlock;");
      }
   }

   private void doSpecialBlock(RuinsWorld world, Random random, int x, int y, int z, int blocknum, int rotate, String dataString) {
      if (!dataString.equals("preserveBlock")) {
         if (dataString.startsWith("MobSpawner:")) {
            this.addCustomSpawner(world, x, y, z, dataString.substring(dataString.indexOf(":") + 1));
         } else if (dataString.equals("UprightMobSpawn")) {
            this.addUprightMobSpawn(world, random, x, y, z);
         } else if (dataString.equals("EasyMobSpawn")) {
            this.addEasyMobSpawn(world, random, x, y, z);
         } else if (dataString.equals("MediumMobSpawn")) {
            this.addMediumMobSpawn(world, random, x, y, z);
         } else if (dataString.equals("HardMobSpawn")) {
            this.addHardMobSpawn(world, random, x, y, z);
         } else if (dataString.startsWith("EasyChest")) {
            this.addEasyChest(world, random, x, y, z, blocknum, rotate, random.nextInt(3) + 3);
         } else if (dataString.startsWith("MediumChest")) {
            this.addMediumChest(world, random, x, y, z, blocknum, rotate, random.nextInt(4) + 3);
         } else if (dataString.startsWith("HardChest")) {
            this.addHardChest(world, random, x, y, z, blocknum, rotate, random.nextInt(5) + 3);
         } else if (dataString.startsWith("ChestGenHook:")) {
            String[] s = dataString.split(":");
            int targetCount = s.length > 1 ? Integer.valueOf(s[2].split("-")[0]) : 0;
            this.addChestGenChest(world, random, x, y, z, s[1], targetCount, blocknum, rotate);
         } else if (dataString.startsWith("IInventory;")) {
            RuinTextLumper lumper = new RuinTextLumper(this.owner, this.excessiveDebugging ? this.debugPrinter : null);
            String dataWithoutNBT = lumper.lump(dataString);
            String[] s = dataWithoutNBT.split(";");
            String b = RuinTemplate.normalize(s[1]);
            if (LegacyStates.block(b, 0) != null) {
               if (s[2].lastIndexOf("-") > s[2].length() - 5) {
                  this.addIInventoryBlock(world, random, x, y, z, b, s[2].substring(0, s[2].lastIndexOf("-")), lumper, blocknum, rotate);
               } else {
                  this.addIInventoryBlock(world, random, x, y, z, b, s[2], lumper, blocknum, rotate);
               }
            } else {
               System.err.println("Ruins Mod could not determine what IInventory block to spawn for [" + s[1] + "] in Ruin template: " + this.owner.getName());
            }
         } else if (dataString.equals("EnderCrystal")) {
            this.realizeBlock(world, x, y, z, "minecraft:bedrock", -1, 0); // rlgen: the crystal entity itself is not spawned
         } else if (dataString.startsWith("Skull:")) {
            Tile tes = this.realizeBlock(world, x, y, z, "minecraft:skull", blocknum, rotate);
            if (tes != null) {
               String[] splits = dataString.split(":");
               tes.skullType = Integer.valueOf(splits[1]);
               // TileEntitySkull.rotate: the 16-step rotation turns with the template
               int rot = Integer.valueOf(splits[2].split("-")[0]);
               tes.skullRotation = getDirectionalRotation(rotate).rotate(rot, 16);
            }
         } else {
            System.err.println("Ruins Mod could not determine what to spawn for [" + dataString + "] in Ruin template: " + this.owner.getName());
         }
      }
   }

   private int getBlockNum(Random random) {
      int blockIndex = 0;
      double selector = random.nextDouble() * this.blockWeightsTotal;

      while ((selector -= this.blockWeights[blockIndex]) >= 0.0) {
         blockIndex++;
      }

      return blockIndex;
   }

   /** 1.12 EntityList names ("Skeleton", "Cave_Spider") were matched lower case; mobs of mods we lack get a stand-in when the chunk is built. */
   private void addCustomSpawner(RuinsWorld world, int x, int y, int z, String id) {
      Tile mobspawner = this.realizeBlock(world, x, y, z, "minecraft:mob_spawner", -1, 0);
      if (mobspawner != null) {
         mobspawner.spawner = id.contains(":") ? id.toLowerCase() : "minecraft:" + id.toLowerCase();
      }
   }

   private void addEasyMobSpawn(RuinsWorld world, Random random, int x, int y, int z) {
      switch (random.nextInt(2)) {
         case 0:
            this.addCustomSpawner(world, x, y, z, "Skeleton");
            break;
         default:
            this.addCustomSpawner(world, x, y, z, "Zombie");
      }
   }

   private void addMediumMobSpawn(RuinsWorld world, Random random, int x, int y, int z) {
      switch (random.nextInt(4)) {
         case 0:
            this.addCustomSpawner(world, x, y, z, "Spider");
            break;
         case 1:
            this.addCustomSpawner(world, x, y, z, "Skeleton");
            break;
         case 2:
            this.addCustomSpawner(world, x, y, z, "Cave_Spider");
            break;
         default:
            this.addCustomSpawner(world, x, y, z, "Zombie");
      }
   }

   private void addHardMobSpawn(RuinsWorld world, Random random, int x, int y, int z) {
      switch (random.nextInt(4)) {
         case 0:
            this.addCustomSpawner(world, x, y, z, "Creeper");
            break;
         case 1:
            this.addCustomSpawner(world, x, y, z, "Cave_Spider");
            break;
         case 2:
            this.addCustomSpawner(world, x, y, z, "Skeleton");
            break;
         default:
            this.addCustomSpawner(world, x, y, z, "Blaze");
      }
   }

   private void addUprightMobSpawn(RuinsWorld world, Random random, int x, int y, int z) {
      switch (random.nextInt(3)) {
         case 0:
            this.addCustomSpawner(world, x, y, z, "Creeper");
            break;
         case 1:
            this.addCustomSpawner(world, x, y, z, "Skeleton");
            break;
         default:
            this.addCustomSpawner(world, x, y, z, "Zombie");
      }
   }

   private void addEasyChest(RuinsWorld world, Random random, int x, int y, int z, int blocknum, int direction, int items) {
      Tile chest = this.realizeBlock(world, x, y, z, "minecraft:chest", blocknum, direction);
      if (chest != null) {
         for (int i = 0; i < items; i++) {
            ItemStack stack = this.getNormalStack(random);
            if (stack != null) {
               chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
            }
         }
      }
   }

   private void addMediumChest(RuinsWorld world, Random random, int x, int y, int z, int blocknum, int direction, int items) {
      Tile chest = this.realizeBlock(world, x, y, z, "minecraft:chest", blocknum, direction);
      if (chest != null) {
         for (int i = 0; i < items; i++) {
            ItemStack stack;
            if (random.nextInt(20) < 19) {
               stack = this.getNormalStack(random);
            } else {
               stack = this.getLootStack(random);
            }

            if (stack != null) {
               chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
            }
         }
      }
   }

   private void addHardChest(RuinsWorld world, Random random, int x, int y, int z, int blocknum, int direction, int items) {
      Tile chest = this.realizeBlock(world, x, y, z, "minecraft:chest", blocknum, direction);
      if (chest != null) {
         for (int i = 0; i < items; i++) {
            ItemStack stack;
            if (random.nextInt(10) < 9) {
               stack = this.getNormalStack(random);
            } else {
               stack = this.getLootStack(random);
            }

            if (stack != null) {
               chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), stack);
            }
         }
      }
   }

   /** The loot table is rolled when the chunk is built (it needs the server); trimmed to targetCount and spread like 1.12. */
   private void addChestGenChest(RuinsWorld world, Random random, int x, int y, int z, String gen, int targetCount, int blocknum, int direction) {
      Tile chest = this.realizeBlock(world, x, y, z, "minecraft:chest", blocknum, direction);
      if (chest != null) {
         chest.lootTable = gen.contains(":") ? gen : "minecraft:" + gen;
         chest.lootCount = targetCount;
         chest.lootSeed = random.nextLong();
      }
   }

   private void addIInventoryBlock(RuinsWorld world, Random random, int x, int y, int z, String block, String itemDataWithoutNBT, RuinTextLumper lumper,
                                   int blocknum, int direction) {
      Tile te = this.realizeBlock(world, x, y, z, block, blocknum, direction);
      if (te != null) {
         if (itemDataWithoutNBT.startsWith("ChestGenHook:")) {
            String[] input = itemDataWithoutNBT.split(":");
            te.lootTable = "minecraft:" + input[1];
            te.lootCount = 0;
            te.lootSeed = random.nextLong();
         } else {
            this.handleIInventory(te, itemDataWithoutNBT, lumper);
         }
      } else {
         System.err.println("Ruins Mod could not find IInventory instance for [" + block + "] in Ruin template: " + this.owner.getName());
      }
   }

   private void handleIInventory(Tile inv, String itemDataWithoutNBT, RuinTextLumper lumper) {
      String[] itemStrings = itemDataWithoutNBT.split(Pattern.quote("+"));

      for (String itemstring : itemStrings) {
         String[] hashsplit = itemstring.split("#");
         int itemStackSize = 1;
         boolean nbtdata = false;
         if (hashsplit.length > 1) {
            try {
               itemStackSize = Integer.parseInt(hashsplit[1]);
            } catch (NumberFormatException var19) {
               nbtdata = true;
            }
         }

         int itemMeta = hashsplit.length > 2 ? Integer.valueOf(hashsplit[2]) : 0;
         int targetslot = hashsplit.length > 3 ? Integer.valueOf(hashsplit[3]) : -1;
         Item item = LegacyStates.item(RuinTemplate.normalize(hashsplit[0]), itemMeta);
         ItemStack putItem = item != null ? new ItemStack(item, itemStackSize) : null;
         if (putItem != null) {
            if (nbtdata) {
               try {
                  LegacyItemNbt.apply(putItem, TagParser.parseTag(lumper.unlump(hashsplit[1])));
               } catch (Exception var20) {
                  var20.printStackTrace();
               }
            }

            if (targetslot != -1) {
               ItemStack slotItemPrev = inv.getStackInSlot(targetslot);
               if (slotItemPrev.isEmpty()) {
                  inv.setInventorySlotContents(targetslot, putItem);
               } else if (ItemStack.isSameItem(slotItemPrev, putItem)) {
                  int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.getCount();
                  slotItemPrev.grow(Math.min(freeSize, putItem.getCount()));
               }
            } else {
               for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
                  ItemStack slotItemPrev = inv.getStackInSlot(slot);
                  if (slotItemPrev.isEmpty()) {
                     inv.setInventorySlotContents(slot, putItem);
                     break;
                  }

                  if (ItemStack.isSameItem(slotItemPrev, putItem)) {
                     int freeSize = slotItemPrev.getMaxStackSize() - slotItemPrev.getCount();
                     if (freeSize >= putItem.getCount()) {
                        slotItemPrev.grow(putItem.getCount());
                        break;
                     }

                     slotItemPrev.grow(freeSize);
                     putItem.shrink(freeSize);
                  }
               }
            }
         }
      }
   }

   private ItemStack getNormalStack(Random random) {
      int rand = random.nextInt(25);
      switch (rand) {
         case 0:
         case 1:
            return null;
         case 2:
         case 3:
            return new ItemStack(Items.BREAD);
         case 4:
         case 5:
            return new ItemStack(Items.WHEAT, random.nextInt(8) + 8);
         case 6:
            return new ItemStack(Items.IRON_HOE);
         case 7:
            return new ItemStack(Items.IRON_SHOVEL);
         case 8:
         case 9:
            return new ItemStack(Items.STRING, random.nextInt(3) + 1);
         case 10:
         case 11:
         case 12:
            return new ItemStack(Items.WHEAT_SEEDS, random.nextInt(8) + 8);
         case 13:
         case 14:
         case 15:
            return new ItemStack(Items.BOWL, random.nextInt(2) + 1);
         case 16:
            return new ItemStack(Items.BUCKET);
         case 17:
            return new ItemStack(Items.APPLE);
         case 18:
         case 19:
            return new ItemStack(Items.BONE, random.nextInt(4) + 1);
         case 20:
         case 21:
            return new ItemStack(Items.EGG, random.nextInt(2) + 1);
         case 22:
            return new ItemStack(Items.COAL, random.nextInt(5) + 3);
         case 23:
            return new ItemStack(Items.IRON_INGOT, random.nextInt(5) + 3);
         default:
            return this.getLootStack(random);
      }
   }

   private ItemStack getLootStack(Random random) {
      int rand = random.nextInt(25);
      switch (rand) {
         case 0:
         case 1:
         case 2:
         case 3:
            return null;
         case 4:
         case 5:
            return new ItemStack(Items.LEATHER_BOOTS);
         case 6:
         case 7:
            return new ItemStack(Items.LEATHER_LEGGINGS);
         case 8:
         case 9:
            return new ItemStack(Items.FLINT_AND_STEEL);
         case 10:
         case 11:
            return new ItemStack(Items.IRON_AXE);
         case 12:
            return new ItemStack(Items.IRON_SWORD);
         case 13:
            return new ItemStack(Items.IRON_PICKAXE);
         case 14:
         case 15:
            return new ItemStack(Items.IRON_HELMET);
         case 16:
            return new ItemStack(Items.IRON_CHESTPLATE);
         case 17:
         case 18:
            return new ItemStack(Items.BOOK, random.nextInt(3) + 1);
         case 19:
            return new ItemStack(Items.COMPASS);
         case 20:
            return new ItemStack(Items.CLOCK);
         case 21:
            return new ItemStack(Items.REDSTONE, random.nextInt(12) + 12);
         case 22:
            return new ItemStack(Items.GOLDEN_APPLE);
         case 23:
            return new ItemStack(Items.MUSHROOM_STEW, random.nextInt(2) + 1);
         default:
            return new ItemStack(Items.DIAMOND, random.nextInt(4));
      }
   }

   private boolean isAir(String block) {
      return block.equals("air") || block.equals("minecraft:air");
   }

   private static Rotation getDirectionalRotation(int direction) {
      Rotation rotation = Rotation.NONE;
      switch (direction) {
         case 1:
            rotation = Rotation.CLOCKWISE_90;
            break;
         case 2:
            rotation = Rotation.CLOCKWISE_180;
            break;
         case 3:
            rotation = Rotation.COUNTERCLOCKWISE_90;
      }

      return rotation;
   }

   /** World.setBlockState of the rotated 1.12 state; returns the (new) tile entity there, if the block has one. */
   private Tile realizeBlock(RuinsWorld world, int x, int y, int z, String block, int blocknum, int direction) {
      Tile entity = null;
      if (world != null && block != null) {
         BlockPos position = new BlockPos(x, y, z);
         BlockState state = this.getCachedBlockState(block, blocknum);
         if (state != null && world.setBlockState(position, state.rotate(getDirectionalRotation(direction)))) {
            entity = world.getTileEntity(position);
         }
      }

      return entity;
   }

   private BlockState getCachedBlockState(String block, int blocknum) {
      BlockState state = blocknum != -1 ? this.blockBSs[blocknum] : LegacyStates.block(block, 0);
      if (state == null) {
         String metadata = blocknum != -1 ? this.blockMDs[blocknum] : null;
         if (metadata != null && !metadata.isEmpty()) {
            try {
               int value = Integer.parseInt(metadata);
               if (value >= 0 && value < 16) {
                  state = LegacyStates.block(block, value);
               } else {
                  System.err.printf("Warning: Ruins template [%s] contains out-of-range block data value [%d]\n", this.owner.getName(), value);
               }
            } catch (NumberFormatException var6) {
               System.err.printf("Warning: Ruins template [%s] contains non-integer block data value [%s]\n", this.owner.getName(), metadata);
            }
         }

         if (state == null) {
            state = LegacyStates.block(block, 0);
         }

         if (blocknum != -1) {
            this.blockBSs[blocknum] = state;
         }
      }

      return state;
   }

   private static enum PreservePolicy {
      IGNORE,
      PRESERVE,
      CORRUPT;
   }

   public static enum SpecialFlags {
      COMMANDBLOCK,
      ADDBONEMEAL;
   }
}
