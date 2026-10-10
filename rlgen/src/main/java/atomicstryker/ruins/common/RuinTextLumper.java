package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RuinTextLumper {
   private RuinTemplate template_;
   private PrintWriter log_;
   private List<String> lumps_;
   private static final Pattern LUMP_PATTERN = Pattern.compile("(\\{)|\\[[^]]*+]|Lu\\d++mP");
   private static final Pattern BRACED_LUMP_PATTERN = Pattern.compile("(})|(\")|\\{");
   private static final Pattern QUOTED_LUMP_PATTERN = Pattern.compile("(\")|\\\\.");
   private static final Pattern UNLUMP_PATTERN = Pattern.compile("Lu(\\d++)mP");

   public RuinTextLumper(RuinTemplate template, PrintWriter log) {
      this.template_ = template;
      this.log_ = log;
      this.lumps_ = new ArrayList<>();
   }

   public String lump(String input) {
      this.lumps_.clear();
      StringBuilder output = new StringBuilder();
      int start = 0;
      int end = input.length();
      Matcher matcher = LUMP_PATTERN.matcher(input);

      while (start < end) {
         if (matcher.find(start)) {
            int extent = matcher.end();
            if (matcher.group(1) != null) {
               extent = this.extendNestedLump(BRACED_LUMP_PATTERN, input, extent, end);
            }

            if (extent >= start) {
               output.append(input.substring(start, matcher.start())).append("Lu").append(this.lumps_.size()).append("mP");
               String lump = input.substring(matcher.start(), extent);
               this.lumps_.add(lump);
               if (this.log_ != null) {
                  this.log_.println("template " + this.template_.getName() + " contains lump: " + lump);
               }

               start = extent;
               continue;
            }

            System.err.println("Unbalanced nesting in Ruins template " + this.template_.getName() + ", offending rule: " + input);
            output.append(input.substring(start));
            break;
         }

         output.append(input.substring(start));
         break;
      }

      return output.toString();
   }

   private int extendNestedLump(Pattern pattern, String input, int start, int end) {
      int depth = 0;
      Matcher matcher = pattern.matcher(input);

      while (start < end && matcher.find(start)) {
         start = matcher.end();
         if (matcher.group(1) != null) {
            if (--depth < 0) {
               return start;
            }
         } else if (matcher.group(2) != null) {
            if ((start = this.extendQuotedLump(input, start, end)) < 0) {
               break;
            }
         } else {
            depth++;
         }
      }

      return -1;
   }

   private int extendQuotedLump(String input, int start, int end) {
      Matcher matcher = QUOTED_LUMP_PATTERN.matcher(input);

      while (start < end && matcher.find(start)) {
         start = matcher.end();
         if (matcher.group(1) != null) {
            return start;
         }
      }

      return -1;
   }

   public String unlump(String input) {
      StringBuilder output = new StringBuilder();
      int start = 0;
      int end = input.length();

      for (Matcher matcher = UNLUMP_PATTERN.matcher(input); start < end; start = matcher.end()) {
         if (!matcher.find()) {
            output.append(input.substring(start));
            break;
         }

         output.append(input.substring(start, matcher.start())).append(this.lumps_.get(Integer.parseInt(matcher.group(1))));
      }

      return output.toString();
   }
}
