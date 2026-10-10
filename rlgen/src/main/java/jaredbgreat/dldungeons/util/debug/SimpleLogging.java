package jaredbgreat.dldungeons.util.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleLogging {
   private static Logger logger;
   private static SimpleLogging log;

   private SimpleLogging() {
      logger = LogManager.getLogger("JBGDungeons");
   }

   public static SimpleLogging getInstance() {
      if (log == null) {
         log = new SimpleLogging();
      }

      return log;
   }

   public static void logError(String error) {
      if (log == null) {
         log = new SimpleLogging();
      }

      System.err.println(error);
      logger.error(error);
   }

   public static void logInfo(String info) {
      if (log == null) {
         log = new SimpleLogging();
      }

      logger.info(info);
   }
}
