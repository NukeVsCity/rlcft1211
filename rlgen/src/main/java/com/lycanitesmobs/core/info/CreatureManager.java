package com.lycanitesmobs.core.info;

import com.lycanitesmobs.LycanitesMobs;
import java.util.LinkedHashMap;
import java.util.Map;

// STUB(lyc/server): stand-in for the 1.12 CreatureManager until lyc/server pushes the real one. Only what the client uses.
public class CreatureManager {
   private static CreatureManager INSTANCE;
   public final Map<String, CreatureInfo> creatures = new LinkedHashMap<>();
   public final CreatureConfig config = new CreatureConfig();

   /** name, model class, width, height, sizeScale (jar creature JSON merged with RLCraft's override). */
   static final String[][] TABLE = {
      {"abaia", "ModelAbaia", "0.9", "0.9", "1"},
      {"abtu", "ModelAbtu", "1.8", "1.8", "1"},
      {"aegis", "ModelAegis", "0.8", "1.6", "1"},
      {"afrit", "ModelAfrit", "0.8", "0.9", "1"},
      {"aglebemu", "ModelAglebemu", "0.9", "0.9", "1"},
      {"amalgalich", "ModelAmalgalich", "18.0", "24.0", "1"},
      {"archvile", "ModelArchvile", "1.0", "3.5", "1"},
      {"argus", "ModelArgus", "0.8", "1.8", "1"},
      {"arisaur", "ModelArisaur", "4.9", "4.2", "1"},
      {"arix", "ModelArix", "0.8", "0.9", "1"},
      {"asmodeus", "ModelAsmodeus", "20.0", "21.0", "1"},
      {"aspid", "ModelAspid", "0.9", "2.2", "1"},
      {"astaroth", "ModelAstaroth", "4.2", "3.0", "1"},
      {"balayang", "ModelBalayang", "0.8", "1.2", "1"},
      {"banshee", "ModelBanshee", "0.8", "1.6", "1"},
      {"barghest", "ModelBarghest", "0.8", "1.9", "1"},
      {"behemoth", "ModelBehemoth", "1.0", "3.5", "1"},
      {"beholder", "ModelBeholder", "3.9", "3.9", "1"},
      {"belph", "ModelBelph", "0.6", "1.8", "1"},
      {"bobeko", "ModelBobeko", "1.8", "2.8", "1"},
      {"cacodemon", "ModelCacodemon", "2.5", "2.5", "1"},
      {"calpod", "ModelCalpod", "1.3", "0.9", "1"},
      {"cephignis", "ModelCephignis", "1.8", "0.8", "1"},
      {"chupacabra", "ModelChupacabra", "0.9", "0.9", "1"},
      {"cinder", "ModelCinder", "0.8", "1.6", "1"},
      {"clink", "ModelClink", "0.6", "1.8", "1"},
      {"cockatrice", "ModelCockatrice", "2.8", "4.8", "1"},
      {"conba", "ModelConba", "0.6", "0.9", "1"},
      {"concapede", "ModelConcapedeHead", "0.9", "0.9", "1"},
      {"concapedesegment", "ModelConcapedeSegment", "0.9", "0.9", "1"},
      {"crusk", "ModelCrusk", "5.8", "1.8", "1"},
      {"cryptkeeper", "ModelCryptkeeper", "0.6", "1.95", "1"},
      {"darkling", "ModelDarkling", "0.5", "0.5", "1"},
      {"dawon", "ModelDawon", "1.6", "1.4", "1"},
      {"djinn", "ModelDjinn", "0.8", "1.6", "1"},
      {"dweller", "ModelDweller", "0.8", "1.6", "1"},
      {"eechetik", "ModelEechetik", "0.8", "1.6", "1"},
      {"ent", "ModelEnt", "0.8", "1.8", "1"},
      {"epion", "ModelEpion", "1.8", "0.8", "1"},
      {"erepede", "ModelErepede", "0.8", "1.5", "1"},
      {"ettin", "ModelEttin", "1.5", "3.2", "1"},
      {"eyewig", "ModelEyewig", "0.8", "1.2", "1"},
      {"fear", "", "0.8", "1.8", "1"},
      {"feradon", "ModelFeradon", "0.8", "1.5", "1"},
      {"frostweaver", "ModelFrostweaver", "0.8", "1.2", "1"},
      {"geist", "ModelGeist", "0.6", "2.4", "1"},
      {"geken", "ModelGeken", "0.6", "1.9", "1"},
      {"geonach", "ModelGeonach", "0.8", "1.6", "1"},
      {"ghoul", "ModelGhoul", "0.8", "1.6", "1"},
      {"gnekk", "ModelGnekk", "0.8", "0.9", "1"},
      {"gorger", "ModelGorger", "6.8", "1.8", "1"},
      {"gorgomite", "ModelGorgomite", "0.9", "0.9", "1"},
      {"grell", "ModelGrell", "2.5", "2.5", "1"},
      {"grigori", "ModelGrigori", "0.9", "0.9", "1"},
      {"grue", "ModelGrue", "0.8", "1.6", "1"},
      {"herma", "ModelHerma", "0.9", "0.9", "1"},
      {"ignibus", "ModelIgnibus", "8.9", "8.9", "1"},
      {"ika", "ModelIka", "0.9", "0.9", "1"},
      {"ioray", "ModelIoray", "2.8", "0.95", "1"},
      {"jabberwock", "ModelJabberwock", "0.8", "3.6", "1"},
      {"jengu", "ModelJengu", "0.8", "1.6", "1"},
      {"joust", "ModelJoust", "0.9", "2.2", "1"},
      {"joustalpha", "ModelJoustAlpha", "0.9", "2.2", "1"},
      {"khalk", "ModelKhalk", "4.5", "3.5", "1"},
      {"kobold", "ModelKobold", "0.5", "0.9", "1"},
      {"krake", "ModelKrake", "0.9", "2.2", "1"},
      {"lacedon", "ModelLacedon", "0.8", "1.6", "1"},
      {"lobber", "ModelLobber", "1.9", "3.5", "1"},
      {"lurker", "ModelLurker", "0.8", "1.5", "1"},
      {"maka", "ModelMaka", "2.9", "3.9", "1"},
      {"makaalpha", "ModelMakaAlpha", "3.9", "3.9", "1"},
      {"maug", "ModelMaug", "0.8", "1.5", "1"},
      {"morock", "ModelMorock", "2.8", "1.8", "1"},
      {"necrovore", "ModelNecrovore", "0.9", "0.9", "1"},
      {"nymph", "ModelNymph", "0.8", "1.8", "1"},
      {"pinky", "ModelPinky", "1.6", "1.9", "1"},
      {"pixen", "ModelPixen", "0.5", "0.5", "1"},
      {"quetzodracl", "ModelQuetzodracl", "2.8", "1.8", "1"},
      {"quillbeast", "ModelQuillbeast", "0.6", "0.9", "1"},
      {"rahovart", "ModelRahovart", "7.0", "25.0", "1"},
      {"raiko", "ModelRaiko", "0.8", "1.8", "1"},
      {"reaper", "ModelReaper", "0.9", "1.9", "1"},
      {"reiver", "ModelReiver", "0.8", "1.6", "1"},
      {"remobra", "ModelRemobra", "0.8", "1.2", "1"},
      {"roa", "ModelRoa", "2.3", "1.8", "1"},
      {"roc", "ModelRoc", "0.8", "1.8", "1"},
      {"salamander", "ModelSalamander", "2.5", "0.9", "1"},
      {"serpix", "ModelSerpix", "6.8", "1.8", "1"},
      {"shade", "ModelShade", "1.9", "2.9", "1"},
      {"shambler", "ModelShambler", "1.5", "3.5", "1"},
      {"silex", "ModelSilex", "0.8", "0.8", "1"},
      {"skylus", "ModelSkylus", "1.5", "1.6", "1"},
      {"spectre", "ModelSpectre", "0.8", "1.6", "1"},
      {"spriggan", "ModelSpriggan", "0.8", "1.6", "1"},
      {"strider", "ModelStrider", "3.0", "10.0", "1"},
      {"sutiramu", "ModelSutiramu", "1.9", "1.9", "1"},
      {"sylph", "ModelSylph", "0.8", "1.6", "1"},
      {"tarantula", "ModelTarantula", "0.8", "1.2", "1"},
      {"thresher", "ModelThresher", "8.0", "8.0", "1"},
      {"tpumpkyn", "ModelTpumpkyn", "0.9", "0.9", "1"},
      {"treant", "ModelTreant", "2.9", "7.9", "1"},
      {"tremor", "ModelTremor", "0.8", "1.6", "1"},
      {"triffid", "ModelTriffid", "1.8", "2.8", "1"},
      {"trite", "ModelTrite", "0.9", "0.9", "1"},
      {"troll", "ModelTroll", "1.5", "3.2", "1"},
      {"uvaraptor", "ModelUvaraptor", "0.8", "1.5", "1"},
      {"vapula", "ModelVapula", "0.8", "1.6", "1"},
      {"ventoraptor", "ModelVentoraptor", "0.8", "1.5", "1"},
      {"vespid", "ModelVespid", "0.9", "1.5", "1"},
      {"vespidqueen", "ModelVespidQueen", "1.6", "1.9", "1"},
      {"volcan", "ModelVolcan", "0.8", "1.6", "1"},
      {"vorach", "ModelVorach", "1.5", "3.5", "1"},
      {"warg", "ModelWarg", "0.8", "1.5", "1"},
      {"wendigo", "ModelWendigo", "1.0", "4.5", "1"},
      {"wildkin", "ModelWildkin", "1.5", "3.2", "1"},
      {"wisp", "ModelWisp", "0.8", "1.8", "1"},
      {"wraamon", "ModelWraamon", "0.5", "0.5", "1"},
      {"wraith", "ModelWraith", "0.8", "1.6", "1"},
      {"xaphan", "ModelXaphan", "0.8", "1.6", "1"},
      {"yale", "ModelYale", "0.9", "1.8", "1"},
      {"zephyr", "ModelZephyr", "0.8", "1.6", "1"},
      {"zoataur", "ModelZoataur", "2.8", "2.8", "1"},
   };

   private CreatureManager() {
      for (String[] row : TABLE) {
         CreatureInfo info = new CreatureInfo();
         info.name = row[0];
         info.modInfo = LycanitesMobs.modInfo;
         info.modelClassName = "com.lycanitesmobs.client.model.creature." + row[1];
         info.width = Double.parseDouble(row[2]);
         info.height = Double.parseDouble(row[3]);
         info.sizeScale = Double.parseDouble(row[4]);
         this.creatures.put(info.name, info);
      }
   }

   public static CreatureManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new CreatureManager();
      }
      return INSTANCE;
   }

   public CreatureInfo getCreature(String name) {
      return name == null ? null : this.creatures.get(name.toLowerCase());
   }

   public static class CreatureConfig {
      public boolean disableModelAlpha = false;
   }
}
