package jaredbgreat.dldungeons.util.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Logging {
   private static Logger logger;
   private static Logging log;

   private Logging() {
      logger = LogManager.getLogger("DLDUNGEONS");
   }

   public static Logging getInstance() {
      if (log == null) {
         log = new Logging();
      }

      return log;
   }

   public static void logError(String error) {
      if (log == null) {
         log = new Logging();
      }

      System.err.println(error);
      logger.error(error);
   }

   public static void logInfo(String info) {
      if (log == null) {
         log = new Logging();
      }

      logger.info(info);
   }
}
