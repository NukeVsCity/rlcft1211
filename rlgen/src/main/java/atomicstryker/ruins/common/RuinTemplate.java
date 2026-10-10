package atomicstryker.ruins.common;

import com.nukevscity.rlgen.legacy.LegacyStates;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** rlgen: surfaces and rule blocks are 1.12 block names; templates are read from the jar (RLCraft's config/ruins_config). */
public class RuinTemplate {
   private final String name;
   private String[] acceptedSurfaces;
   private String[] deniedSurfaces;
   private int height = 0;
   private int width = 0;
   private int length = 0;
   private int overhang = 0;
   private int embed = 0;
   private int randomOffMin = 0;
   private int randomOffMax = 0;
   private double weight = 1.0;
   private int leveling = 2;
   private int lbuffer = 0;
   private int w_off = 0;
   private int l_off = 0;
   public int uniqueMinDistance = 0;
   public int spawnMinDistance = 0;
   public int spawnMaxDistance = Integer.MAX_VALUE;
   private boolean preserveWater = false;
   private boolean preserveLava = false;
   private final RuinTemplate.VariantRuleset variantRuleset;
   private final ArrayList<RuinTemplateLayer> layers;
   private final HashSet<String> biomes;
   private final PrintWriter debugPrinter;
   private final boolean debugging;
   private boolean preventRotation = false;
   private final ArrayList<Integer> bonemealMarkers;
   private final ArrayList<RuinTemplate.AdjoiningTemplateData> adjoiningTemplates;
   private final Set<String> acceptedDimensions = new HashSet<>();
   private static final Pattern patternRuleRaw = Pattern.compile("(?:[^*=^]*\\*)?(?:rule[^=^]*)?[=^]");
   private static final Pattern patternRule = Pattern.compile("(?:([1-9]\\d{0,4})\\*)?(rule[^=^]*)?([=^])(?:([1-9]\\d{0,4})\\*)?(.*)");
      
   public RuinTemplate(PrintWriter out, String filename, String simpleName, boolean debug) throws Exception {
      this.name = simpleName;
      this.debugPrinter = out;
      this.debugging = debug;
      ArrayList<String> lines = new ArrayList<>();
      this.variantRuleset = new RuinTemplate.VariantRuleset();
      this.layers = new ArrayList<>();
      this.biomes = new HashSet<>();
      this.bonemealMarkers = new ArrayList<>();
      this.adjoiningTemplates = new ArrayList<>();
      InputStream in = RuinTemplate.class.getResourceAsStream(filename);
      if (in == null) {
         throw new Exception("missing template " + filename);
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

      for (String read = br.readLine(); read != null; read = br.readLine()) {
         lines.add(read);
      }

      this.parseFile(lines);
      br.close();
   }

   public RuinTemplate(PrintWriter out, String filename, String simpleName) throws Exception {
      this(out, filename, simpleName, false);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof RuinTemplate && ((RuinTemplate)o).name.equals(this.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   public String getName() {
      return this.name;
   }

   public double getWeight() {
      return this.weight;
   }

   public HashSet<String> getBiomesToSpawnIn() {
      return this.biomes;
   }

   static final String AIR = "minecraft:air";

   /** 1.12 registry names: lower case, "minecraft:" when no domain. */
   static String normalize(String name) {
      String n = name.trim().toLowerCase(Locale.ROOT);
      return n.contains(":") ? n : "minecraft:" + n;
   }

   public boolean isIgnoredBlock(BlockState state) {
      Block blockID = state.getBlock();
      return state.isAir() || blockID == Blocks.SNOW || blockID == Blocks.COBWEB || this.isPlant(state) || this.preserveBlock(state);
   }

   /** IShearable, BlockBush, IPlantable, leaves and wood in 1.12; Dynamic Trees branches and leaves count as wood and leaves. */
   private boolean isPlant(BlockState state) {
      Block b = state.getBlock();
      return b instanceof BushBlock || b instanceof VineBlock || b instanceof SugarCaneBlock || b instanceof CactusBlock || b instanceof BambooStalkBlock
         || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("dynamictrees");
   }

   public boolean preserveBlock(BlockState state) {
      if (this.preserveWater && (state.is(Blocks.WATER) || state.is(Blocks.ICE))) {
         return true;
      }

      return this.preserveLava && state.is(Blocks.LAVA);
   }

   public boolean isAcceptableSurface(BlockState state) {
      String id = LegacyStates.legacyName(state.getBlock());
      for (String b : this.deniedSurfaces) {
         if (id.equals(b)) {
            return false;
         }
      }

      if (this.acceptedSurfaces.length == 0) {
         return true;
      } else {
         for (String bx : this.acceptedSurfaces) {
            if (id.equals(bx)) {
               return true;
            }
         }

         return false;
      }
   }

   public int checkArea(RuinsWorld world, int xBase, int y, int zBase, int rotate) {
      return this.checkArea(world, xBase, y, zBase, rotate, 0);
   }

   private int checkArea(RuinsWorld world, int xBase, int y, int zBase, int rotate, int additionalYRangeChecked) {
      int x = xBase + this.w_off;
      int z = zBase + this.l_off;
      int xDim = this.width;
      int zDim = this.length;
      if (!this.preventRotation && (rotate == 1 || rotate == 3)) {
         x = xBase + this.l_off;
         z = zBase + this.w_off;
         xDim = this.length;
         zDim = this.width;
      }

      int topYguess = y - 1 + this.height + additionalYRangeChecked;
      int minimalCheckedY = y - 1 - this.height - additionalYRangeChecked;
      int[][] heightMap = new int[xDim][zDim];
      int lastX = x + xDim;
      int lastZ = z + zDim;

      for (int ix = x; ix < lastX; ix++) {
         for (int iz = z; iz < lastZ; iz++) {
            boolean foundSurface = false;

            for (int iy = topYguess; iy >= minimalCheckedY; iy--) {
               BlockPos pos = new BlockPos(ix, iy, iz);
               BlockState curBlock = world.getBlockState(pos);
               if (!this.isIgnoredBlock(curBlock)) {
                  if (!this.isAcceptableSurface(curBlock)) {
                     return -1;
                  }

                  heightMap[ix - x][iz - z] = iy;
                  foundSurface = true;
                  break;
               }
            }

            if (!foundSurface) {
               heightMap[ix - x][iz - z] = -1;
            }
         }
      }

      double sum = 0.0;
      double vals = 0.0;

      for (int[] row : heightMap) {
         for (int value : row) {
            if (value > 0) {
               vals++;
               sum += (double)value;
            }
         }
      }

      int newY = vals > 0.0 ? (int)Math.round(sum / vals) : y;
      int localOverhang = this.overhang;

      for (int[] row : heightMap) {
         for (int valuex : row) {
            if (valuex < 0) {
               if (--localOverhang < 0) {
                  return -1;
               }
            } else if (Math.abs(newY - valuex) > this.leveling) {
               return -1;
            }
         }
      }

      return newY + 1;
   }

   public RuinData getRuinData(int x, int y, int z, int rotate) {
      int add = 0;
      int y_add = 0;
      if (this.leveling > 0 && this.lbuffer >= 0) {
         add = this.lbuffer;
         y_add = this.leveling;
      }

      int xMin;
      int xMax;
      int zMin;
      int zMax;
      if (rotate != 1 && rotate != 3) {
         xMin = x + this.w_off - add;
         xMax = xMin + this.width + add;
         zMin = z + this.l_off - add;
         zMax = zMin + this.length + add;
      } else {
         xMin = x + this.l_off - add;
         xMax = xMin + this.length + add;
         zMin = z + this.w_off - add;
         zMax = zMin + this.width + add;
      }

      return new RuinData(xMin, xMax, Math.max(y - y_add, 0), y + this.height - 1, zMin, zMax, this.name);
   }

   public int doBuild(RuinsWorld world, Random random, int xBase, int yBase, int zBase, int rotate, boolean is_player, boolean ignore_ceiling) {
      try {
         return this.doBuildNested(world, random, xBase, yBase, zBase, rotate, is_player, ignore_ceiling);
      } catch (Exception var10) {
         this.debugPrinter.printf("An Exception was thrown while building Ruin: %s\n", this.getName());
         System.err.println("Faulty Template name: " + this.getName());
         var10.printStackTrace();
         return -1;
      }
   }

   private int doBuildNested(RuinsWorld world, Random random, int xBase, int yBase, int zBase, int rotate, boolean is_player, boolean ignore_ceiling) {
      ArrayList<RuinRuleProcess> laterun = new ArrayList<>();
      ArrayList<RuinRuleProcess> lastrun = new ArrayList<>();
      Iterator<RuinTemplateLayer> layeriter = this.layers.iterator();
      int y_off = -this.embed + (this.randomOffMax > this.randomOffMin ? random.nextInt(this.randomOffMax - this.randomOffMin) + this.randomOffMin : 0);
      int ceiling = world.getActualHeight();
      int yReturn = Math.max(Math.min(yBase + y_off, ceiling - this.height), 8);
      int y = yReturn - y_off;
      if (this.preventRotation) {
         rotate = 0;
      }

      {
         int x;
         int z;
         int xDim;
         int zDim;
         boolean eastwest;
         if (rotate != 1 && rotate != 3) {
            eastwest = false;
            x = xBase + this.w_off;
            xDim = this.width;
            z = zBase + this.l_off;
            zDim = this.length;
         } else {
            eastwest = true;
            x = xBase + this.l_off;
            xDim = this.length;
            z = zBase + this.w_off;
            zDim = this.width;
         }

         ArrayList<RuinTemplateRule> rules = this.variantRuleset.getVariants(random);
         if (this.leveling > 0 && this.lbuffer >= 0) {
            BlockState fill_block = this.getLevelingFillBlock(world, xBase, yReturn, zBase);
            if (fill_block != null) {
               this.levelSite(world, fill_block, xBase, yReturn, zBase, eastwest, random, rotate, rules.get(0));
            }
         }

         while (layeriter.hasNext()) {
            RuinTemplateLayer curlayer = layeriter.next();

            for (int x0 = 0; x0 < this.width; x0++) {
               for (int z0 = 0; z0 < this.length; z0++) {
                  int rulenum = curlayer.getRuleAt(x0, z0);
                  int x1 = x0;
                  int z1 = z0;
                  switch (rotate) {
                     case 1:
                        x1 = xDim - 1 - z0;
                        z1 = x0;
                        break;
                     case 2:
                        x1 = xDim - 1 - x0;
                        z1 = zDim - 1 - z0;
                        break;
                     case 3:
                        x1 = z0;
                        z1 = zDim - 1 - x0;
                  }

                  RuinTemplateRule curRule = rules.get(rulenum);
                  if (curRule.runLater()) {
                     laterun.add(new RuinRuleProcess(curRule, x + x1, y + y_off, z + z1, rotate));
                     rules.get(0).doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
                  } else if (curRule.runLast()) {
                     lastrun.add(new RuinRuleProcess(curRule, x + x1, y + y_off, z + z1, rotate));
                     rules.get(0).doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
                  } else {
                     curRule.doBlock(world, random, x + x1, y + y_off, z + z1, rotate);
                  }
               }
            }

            y_off++;
         }

         this.doLateRuns(world, random, laterun, lastrun);

         this.bonemealMarkers.clear(); // rlgen: no RLCraft template uses addbonemeal or adjoining templates
         return yReturn;
      }
   }

   /** The 1.12 block (its default state) under the site. */
   private BlockState getLevelingFillBlock(RuinsWorld world, int x, int y, int z) {
      BlockState fill_block = null;
      int y_end = Math.max(y - this.height, 0) - 1;

      for (int y_surface = y - 1; y_surface > y_end; y_surface--) {
         BlockPos pos = new BlockPos(x, y_surface, z);
         BlockState block = world.getBlockState(pos);
         if (!this.isIgnoredBlock(block)) {
            if (this.isAcceptableSurface(block)) {
               fill_block = LegacyStates.block(LegacyStates.legacyName(block.getBlock()), 0);
               if (fill_block == null) {
                  fill_block = block.getBlock().defaultBlockState();
               }
            }
            break;
         }
      }

      return fill_block;
   }

   private void doLateRuns(RuinsWorld world, Random random, ArrayList<RuinRuleProcess> laterun, ArrayList<RuinRuleProcess> lastrun) {
      for (RuinRuleProcess rp : laterun) {
         rp.doBlock(world, random);
      }

      for (RuinRuleProcess rp : lastrun) {
         rp.doBlock(world, random);
      }
   }

   private void levelSite(RuinsWorld world, BlockState fillBlockID, int xBase, int y, int zBase, boolean eastwest, Random random, int rotate, RuinTemplateRule rule0) {
      int x = xBase + this.w_off - this.lbuffer;
      int z = zBase + this.l_off - this.lbuffer;
      int xDim = this.width + 2 * this.lbuffer;
      int zDim = this.length + 2 * this.lbuffer;
      if (eastwest) {
         x = xBase + this.l_off - this.lbuffer;
         z = zBase + this.w_off - this.lbuffer;
         xDim = this.length + 2 * this.lbuffer;
         zDim = this.width + 2 * this.lbuffer;
      }

      int lastX = x + xDim;
      int lastZ = z + zDim;
      int lastY = y + this.leveling;

      for (int xi = x; xi < lastX; xi++) {
         for (int zi = z; zi < lastZ; zi++) {
            for (int yi = y - this.leveling; yi < y; yi++) {
               BlockPos pos = new BlockPos(xi, yi, zi);
               if (this.isIgnoredBlock(world.getBlockState(pos))) {
                  world.setBlockState(pos, fillBlockID);
               }
            }

            for (int yix = y; yix < lastY; yix++) {
               rule0.doBlock(world, random, xi, yix, zi, rotate);
            }
         }
      }
   }

   private void parseFile(ArrayList<String> lines) throws Exception {
      this.parseVariables(lines);
      Iterator<String> i = lines.iterator();
      int lineIndex = 0;
      int ruleIndex = 0;
      int groupIndex = 0;
      int groupSize = 0;
      int repeatCountPrevious = 0;
      int variantIndex = 0;
      RuinTemplate.ParserState parserState = RuinTemplate.ParserState.PRE_RULE_PHASE;

      while (i.hasNext()) {
         String line = i.next();
         lineIndex++;
         if (!line.startsWith("#") && !line.isEmpty()) {
            if (line.startsWith("layer")) {
               if (repeatCountPrevious > 1) {
                  int ruleIndexPrevious = ruleIndex;
                  ruleIndex += (repeatCountPrevious - 1) * (ruleIndex - groupIndex + 1);
                  if (this.debugging) {
                     this.debugPrinter
                        .printf(
                           "template [%s] line [%d]: duplicating group from rules #%d-#%d (%d times) to create rules #%d-#%d\n",
                           this.name,
                           lineIndex,
                           groupIndex,
                           ruleIndexPrevious,
                           repeatCountPrevious,
                           ruleIndexPrevious + 1,
                           ruleIndex
                        );
                  }

                  repeatCountPrevious = 0;
               }

               parserState = RuinTemplate.ParserState.POST_RULE_PHASE;
               ArrayList<String> layerlines = new ArrayList<>();

               for (String var20 = i.next(); !var20.startsWith("endlayer"); var20 = i.next()) {
                  if (var20.charAt(0) != '#') {
                     layerlines.add(var20);
                  }
               }

               this.layers.add(new RuinTemplateLayer(layerlines, this.width, this.length, this.variantRuleset.size()));
            } else {
               Matcher matcher;
               if ((matcher = patternRule.matcher(line)).matches()) {
                  if (parserState == RuinTemplate.ParserState.POST_RULE_PHASE) {
                     throw new Exception("Template file problem: A Rule was defined after a layer! Define all rules before the first layer!");
                  }

                  boolean isFirstRule = parserState == RuinTemplate.ParserState.PRE_RULE_PHASE;
                  boolean hasRepeatCount = matcher.group(1) != null;
                  int repeatCount = hasRepeatCount ? Integer.parseInt(matcher.group(1)) : 1;
                  boolean hasName = matcher.group(2) != null;
                  boolean isVariant = matcher.group(3).equals("^");
                  int weight = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 1;
                  RuinTemplateRule rule = new RuinTemplateRule(this.debugPrinter, this, matcher.group(5), this.debugging);
                  parserState = RuinTemplate.ParserState.RULE_PHASE;
                  if (isVariant) {
                     if (isFirstRule) {
                        this.debugPrinter
                           .printf("template [%s] line [%d]: first rule must start a new variant group (i.e., use = instead of ^)\n", this.name, lineIndex);
                        throw new Exception("Template file problem: First rule cannot join nonexistent variant group!");
                     }

                     if (hasRepeatCount) {
                        this.debugPrinter.printf("template [%s] line [%d]: only first rule variant in a group can have repeat count\n", this.name, lineIndex);
                        throw new Exception("Template file problem: Unexpected repeat count before variant rule group member!");
                     }

                     if (hasName) {
                        ruleIndex++;
                        variantIndex = 1;
                        if (this.debugging) {
                           this.debugPrinter
                              .printf(
                                 "template [%s] line [%d]: adding new rule #%d to variant group with rule #%d\n", this.name, lineIndex, ruleIndex, groupIndex
                              );
                        }

                        this.variantRuleset.addVariantRule(weight, rule);
                     } else {
                        if (ruleIndex == groupIndex) {
                           groupSize++;
                        } else if (variantIndex == groupSize && this.debugging) {
                           this.debugPrinter
                              .printf(
                                 "template [%s] line [%d]: rule #%d has more variants than first rule in group (rule #%d with %d variants); excess will be ignored\n",
                                 this.name,
                                 lineIndex,
                                 ruleIndex,
                                 groupIndex,
                                 groupSize
                              );
                        }

                        variantIndex++;
                        if (this.debugging) {
                           this.debugPrinter.printf("template [%s] line [%d]: adding variant #%d to rule #%d\n", this.name, lineIndex, variantIndex, ruleIndex);
                        }

                        this.variantRuleset.addVariant(weight, rule);
                     }
                  } else {
                     if (repeatCountPrevious > 1) {
                        int ruleIndexPrevious = ruleIndex;
                        ruleIndex += (repeatCountPrevious - 1) * (ruleIndex - groupIndex + 1);
                        if (this.debugging) {
                           this.debugPrinter
                              .printf(
                                 "template [%s] line [%d]: duplicating variant group from rules #%d-#%d (%d times) to create rules #%d-#%d\n",
                                 this.name,
                                 lineIndex,
                                 groupIndex,
                                 ruleIndexPrevious,
                                 repeatCountPrevious,
                                 ruleIndexPrevious + 1,
                                 ruleIndex
                              );
                        }
                     }

                     groupIndex = ++ruleIndex;
                     groupSize = 1;
                     repeatCountPrevious = repeatCount;
                     variantIndex = 1;
                     if (isFirstRule) {
                        if (!hasName) {
                           if (hasRepeatCount) {
                              this.debugPrinter
                                 .printf("template [%s] line [%d]: unnamed rule (alternate rule0) cannot have repeat count\n", this.name, lineIndex);
                              throw new Exception("Template file problem: Unexpected repeat count before unnamed rule!");
                           }

                           ruleIndex = 0;
                           groupIndex = 0;
                           if (this.debugging) {
                              this.debugPrinter.printf("template [%s] line [%d]: alternate rule #0 specified\n", this.name, lineIndex);
                           }
                        } else {
                           if (this.debugging) {
                              this.debugPrinter.printf("template [%s] line [%d]: creating default (preserving air) rule #0\n", this.name, lineIndex);
                           }

                           this.variantRuleset.addVariantGroup(1, 1, new RuinTemplateRule(this.debugPrinter, this, "0,100,?air"));
                        }
                     } else if (!hasName) {
                        this.debugPrinter
                           .printf("template [%s] line [%d]: unnamed rule (alternate rule0) must appear before all other rules\n", this.name, lineIndex);
                        throw new Exception("Template file problem: Rule name missing!");
                     }

                     if (this.debugging) {
                        this.debugPrinter.printf("template [%s] line [%d]: creating new rule #%d\n", this.name, lineIndex, ruleIndex);
                     }

                     this.variantRuleset.addVariantGroup(repeatCount, weight, rule);
                  }
               } else if (patternRuleRaw.matcher(line).lookingAt()) {
                  this.debugPrinter.printf("template [%s] line [%d]: invalid rule syntax\n", this.name, lineIndex);
                  throw new Exception("Template file problem: Cannot parse rule!");
               }
            }
         }
      }
   }

   private void parseVariables(ArrayList<String> variables) throws Exception {

      Set<String> included_biomes = new HashSet<>();
      Set<String> excluded_biomes = new HashSet<>();
      List<RuinVennCriterion> biome_type_criteria = new ArrayList<>();

      for (String line : variables) {
         if (!line.startsWith("#")) {
            if (line.startsWith("acceptable_target_blocks")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  check = check[1].split(",");
                  HashSet<String> acceptables = new HashSet<>();

                  for (String aCheck : check) {
                     String b = normalize(aCheck);
                     if (!b.equals(AIR) && LegacyStates.block(b, 0) != null) {
                        acceptables.add(b);
                     }
                  }

                  this.acceptedSurfaces = new String[acceptables.size()];
                  this.acceptedSurfaces = acceptables.toArray(this.acceptedSurfaces);
               }
            } else if (line.startsWith("unacceptable_target_blocks")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  check = check[1].split(",");
                  HashSet<String> inacceptables = new HashSet<>();

                  for (String aCheckx : check) {
                     String b = normalize(aCheckx);
                     if (!b.equals(AIR) && LegacyStates.block(b, 0) != null) {
                        inacceptables.add(b);
                     }
                  }

                  this.deniedSurfaces = new String[inacceptables.size()];
                  this.deniedSurfaces = inacceptables.toArray(this.deniedSurfaces);
               }
            } else if (line.startsWith("dimensionsToSpawnIn")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  Collections.addAll(this.acceptedDimensions, check[1].split(","));
               }
            } else if (line.startsWith("dimensions")) {
               String[] check = line.split("=");
               check = check[1].split(",");
               this.height = Integer.parseInt(check[0]);
               this.width = Integer.parseInt(check[1]);
               this.length = Integer.parseInt(check[2]);
            } else if (line.startsWith("biomesToSpawnIn")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  Collections.addAll(included_biomes, check[1].split(","));
               }
            } else if (line.startsWith("biomesToNotSpawnIn")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  Collections.addAll(excluded_biomes, check[1].split(","));
               }
            } else if (line.startsWith("biomeTypesToSpawnIn")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  if (this.debugging) {
                     this.debugPrinter.printf("adding biomeTypesToSpawnIn criterion for template \"%s\": specs=\"%s\"\n", this.name, check[1]);
                  }

                  RuinVennCriterion criterion = null;

                  try {
                     criterion = RuinVennCriterion.parseExpression(check[1]);
                  } catch (RuntimeException var15) {
                     System.err.printf("template [%s]: invalid biomeTypesToSpawnIn expression [%s]\n", this.name, check[1]);
                     System.err.println(var15);
                  }

                  if (criterion != null && !criterion.isEmpty()) {
                     biome_type_criteria.add(criterion);
                  }
               }
            } else if (line.startsWith("weight")) {
               String[] check = line.split("=");
               this.weight = Math.max(Double.parseDouble(check[1]), 0.0);
            } else if (line.startsWith("embed_into_distance")) {
               String[] check = line.split("=");
               this.embed = Integer.parseInt(check[1]);
            } else if (line.startsWith("allowable_overhang")) {
               String[] check = line.split("=");
               this.overhang = Integer.parseInt(check[1]);
            } else if (line.startsWith("max_leveling")) {
               String[] check = line.split("=");
               this.leveling = Integer.parseInt(check[1]);
            } else if (line.startsWith("leveling_buffer")) {
               String[] check = line.split("=");
               this.lbuffer = Integer.parseInt(check[1]);
               if (this.lbuffer > 5) {
                  this.lbuffer = 5;
               }
            } else if (line.startsWith("preserve_water")) {
               String[] check = line.split("=");
               if (Integer.parseInt(check[1]) == 1) {
                  this.preserveWater = true;
               }
            } else if (line.startsWith("preserve_lava")) {
               String[] check = line.split("=");
               if (Integer.parseInt(check[1]) == 1) {
                  this.preserveLava = true;
               }
            } else if (line.startsWith("random_height_offset")) {
               String[] check = line.split("=");
               String[] bounds = check[1].split(",");
               this.randomOffMin = Integer.parseInt(bounds[0]);
               this.randomOffMax = Math.max(this.randomOffMin, Integer.parseInt(bounds[1]));
            } else if (line.startsWith("uniqueMinDistance")) {
               String[] check = line.split("=");
               this.uniqueMinDistance = Integer.parseInt(check[1]);
            } else if (line.startsWith("spawnMinDistance")) {
               int value = Integer.parseInt(line.split("=")[1]);
               this.spawnMinDistance = value > 0 ? value : 0;
            } else if (line.startsWith("spawnMaxDistance")) {
               int value = Integer.parseInt(line.split("=")[1]);
               this.spawnMaxDistance = value > 0 ? value : Integer.MAX_VALUE;
            } else if (line.startsWith("preventRotation")) {
               this.preventRotation = Integer.parseInt(line.split("=")[1]) == 1;
            } else if (line.startsWith("requiredMods")) {
               String[] check = line.split("=");
               if (check.length > 1) {
                  if (this.debugging) {
                     this.debugPrinter.printf("checking requiredMods criterion for template \"%s\": specs=\"%s\"\n", this.name, check[1]);
                  }

                  RuinVennCriterion criterion = null;

                  try {
                     criterion = RuinVennCriterion.parseExpression(check[1]);
                  } catch (RuntimeException var14) {
                     System.err.printf("template [%s]: invalid requiredMods expression [%s]\n", this.name, check[1]);
                     System.err.println(var14);
                  }

                  if (criterion != null && !criterion.isEmpty() && !criterion.isSatisfiedBy(RLCRAFT_MODS)) {
                     throw new RuinTemplate.IncompatibleModException(this.name);
                  }
               }
            }
         }
      }

      for (Biome biome : Biome.all()) {
         String biome_name = pathOf(biome);
         if (!this.biomes.contains(biome_name)) {
            if (included_biomes.contains(biome_name)) {
               this.biomes.add(biome_name);
            } else if (!excluded_biomes.contains(biome_name) && !biome_type_criteria.isEmpty()) {
               Set<String> biome_types = new HashSet<>();
               biome_types.addAll(BiomeDict.TYPES.getOrDefault(biome.getRegistryName(), Set.of()));

               for (RuinVennCriterion criterion : biome_type_criteria) {
                  if (criterion.isSatisfiedBy(biome_types)) {
                     this.biomes.add(biome_name);
                     break;
                  }
               }
            }
         }
      }

      if (this.debugging) {
         this.debugPrinter.printf("final biomesToSpawnIn list for template \"%s\":", this.name);
         this.biomes.forEach(biome_namex -> this.debugPrinter.printf(" %s", biome_namex));
         this.debugPrinter.printf("\n");
      }

      if (this.acceptedSurfaces == null) {
         this.acceptedSurfaces = new String[0];
      }

      if (this.deniedSurfaces == null) {
         this.deniedSurfaces = new String[0];
      }

      if (this.width % 2 == 1) {
         this.w_off = 0 - (this.width - 1) / 2;
      } else {
         this.w_off = 0 - this.width / 2;
      }

      if (this.length % 2 == 1) {
         this.l_off = 0 - (this.length - 1) / 2;
      } else {
         this.l_off = 0 - this.length / 2;
      }
   }

   public void markBlockForBonemeal(int x, int y, int z) {
      this.bonemealMarkers.add(x);
      this.bonemealMarkers.add(y);
      this.bonemealMarkers.add(z);
   }

   /** Block.REGISTRY.getObject: unknown names gave air. */
   protected String tryFindingBlockOfName(String blockName) {
      String n = normalize(blockName);
      return n.equals(AIR) || LegacyStates.block(n, 0) == null ? AIR : n;
   }

   /** Half the larger side plus the leveling buffer: how far from (x, z) a build can write. */
   public int radius() {
      return Math.max(this.width, this.length) / 2 + Math.max(this.lbuffer, 0) + 2;
   }

   public String getAirBlock() {
      return AIR;
   }

   /** 1.12 registry path of a biome ("plains", "canyon"). */
   public static String pathOf(Biome biome) {
      String n = biome.getRegistryName();
      return n.substring(n.indexOf(':') + 1);
   }

   /** The mods of RLCraft 2.9.3 a requiredMods line could ask for. */
   private static final Set<String> RLCRAFT_MODS = Set.of("minecraft", "forge", "traverse", "defiledlands", "dynamictrees", "quark", "iceandfire",
      "lycanitesmobs", "recurrentcomplex", "battletowers", "roguelike", "dldungeonsjbg", "ruins", "betternether", "charm", "inspirations", "rustic");

   public boolean acceptsDimension(String dimension) {
      return this.acceptedDimensions.isEmpty() || dimension != null && !dimension.isEmpty() && this.acceptedDimensions.contains(dimension);
   }

   private class AdjoiningTemplateData {
      RuinTemplate adjoiningTemplate;
      int relativeX;
      int acceptableY;
      int relativeZ;
      float spawnchance;

      private AdjoiningTemplateData() {
      }
   }

   public static class IncompatibleModException extends RuntimeException {
      private static final long serialVersionUID = -6208915606622752271L;

      public IncompatibleModException(String template) {
         super("template \"" + template + "\" not installed due to incompatibility with other mods");
      }
   }

   private static enum ParserState {
      PRE_RULE_PHASE,
      RULE_PHASE,
      POST_RULE_PHASE;
   }

   private class VariantRuleset {
      private final ArrayList<RuinTemplate.VariantRuleset.VariantGroup> variantGroups = new ArrayList<>();

      VariantRuleset() {
      }

      public void addVariant(int weight, RuinTemplateRule variant) {
         this.variantGroups.get(this.variantGroups.size() - 1).addVariant(weight, variant);
      }

      public void addVariantRule(int weight, RuinTemplateRule variant) {
         this.variantGroups.get(this.variantGroups.size() - 1).addVariantRule(weight, variant);
      }

      public void addVariantGroup(int repeatCount, int weight, RuinTemplateRule variant) {
         this.variantGroups.add(new RuinTemplate.VariantRuleset.VariantGroup(repeatCount, weight, variant));
      }

      public ArrayList<RuinTemplateRule> getVariants(Random random) {
         ArrayList<RuinTemplateRule> variants = new ArrayList<>();

         for (RuinTemplate.VariantRuleset.VariantGroup variantGroup : this.variantGroups) {
            variants.addAll(variantGroup.getVariants(random, variants.size()));
         }

         return variants;
      }

      public int size() {
         int total = 0;

         for (RuinTemplate.VariantRuleset.VariantGroup variantGroup : this.variantGroups) {
            total += variantGroup.size();
         }

         return total;
      }

      private class VariantGroup {
         private final int repeatCount;
         private final ArrayList<RuinTemplate.VariantRuleset.VariantGroup.VariantRule> variantRules;

         VariantGroup(int repeatCountSpec, int weight, RuinTemplateRule variant) {
            this.repeatCount = repeatCountSpec;
            this.variantRules = new ArrayList<>();
            this.addVariantRule(weight, variant);
         }

         public void addVariant(int weight, RuinTemplateRule variant) {
            this.variantRules.get(this.variantRules.size() - 1).addVariant(weight, variant);
         }

         public void addVariantRule(int weight, RuinTemplateRule variant) {
            this.variantRules.add(new RuinTemplate.VariantRuleset.VariantGroup.VariantRule(weight, variant));
         }

         public ArrayList<RuinTemplateRule> getVariants(Random random, int ruleIndexInitial) {
            ArrayList<RuinTemplateRule> variants = new ArrayList<>();

            for (int i = 0; i < this.repeatCount; i++) {
               int selector = this.variantRules.get(0).getRandomSelector(random, ruleIndexInitial + variants.size());

               for (RuinTemplate.VariantRuleset.VariantGroup.VariantRule variantRule : this.variantRules) {
                  variants.add(variantRule.getVariant(selector, ruleIndexInitial + variants.size()));
               }
            }

            return variants;
         }

         public int size() {
            return this.repeatCount * this.variantRules.size();
         }

         private class VariantRule {
            private final ArrayList<Integer> weights = new ArrayList<>();
            private int weightsTotal = 0;
            private final ArrayList<RuinTemplateRule> variants = new ArrayList<>();

            public VariantRule(int weight, RuinTemplateRule variant) {
               this.addVariant(weight, variant);
            }

            public void addVariant(int weight, RuinTemplateRule variant) {
               this.weights.add(weight);
               this.weightsTotal += weight;
               this.variants.add(variant);
            }

            public int getRandomSelector(Random random, int ruleIndex) {
               int selector = random.nextInt(this.weightsTotal);
               if (RuinTemplate.this.debugging && this.weightsTotal > 1) {
                  RuinTemplate.this.debugPrinter
                     .printf(
                        "template [%s] rule [%d]: group selector drawn (from 1-%d) = %d\n", RuinTemplate.this.name, ruleIndex, this.weightsTotal, selector + 1
                     );
               }

               return selector;
            }

            public RuinTemplateRule getVariant(int selectorInitial, int ruleIndex) {
               int index = this.variants.size() - 1;
               if (selectorInitial < this.weightsTotal) {
                  index = 0;
                  int selector = selectorInitial;

                  while ((selector -= this.weights.get(index)) >= 0) {
                     index++;
                  }
               }

               if (RuinTemplate.this.debugging && this.weightsTotal > 1) {
                  if (selectorInitial < this.weightsTotal) {
                     RuinTemplate.this.debugPrinter
                        .printf(
                           "template [%s] rule [%d]: variant #%d with weight %d chosen by selector = %d\n",
                           RuinTemplate.this.name,
                           ruleIndex,
                           index + 1,
                           this.weights.get(index),
                           selectorInitial + 1
                        );
                  } else {
                     RuinTemplate.this.debugPrinter
                        .printf(
                           "template [%s] rule [%d]: last variant #%d chosen by selector = %d\n",
                           RuinTemplate.this.name,
                           ruleIndex,
                           index + 1,
                           selectorInitial + 1
                        );
                  }
               }

               return this.variants.get(index);
            }
         }
      }
   }
}
