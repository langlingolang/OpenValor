package valor.util;

import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

public final class TeamsUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static ArrayList<String> getScoreboardLines() {
      if (mc.theWorld == null) {
         return new ArrayList<>();
      } else {
         Scoreboard scoreboard = mc.theWorld.getScoreboard();
         if (scoreboard == null) {
            return new ArrayList<>();
         } else {
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            return objective == null
               ? new ArrayList<>()
               : scoreboard.getSortedScores(objective)
                  .stream()
                  .map(score -> ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(score.getPlayerName()), score.getPlayerName()))
                  .collect(Collectors.toCollection(ArrayList::new));
         }
      }
   }

   public static boolean isHypixel() {
      ArrayList<String> lines = getScoreboardLines();
      if (lines.isEmpty()) {
         return false;
      } else {
         String firstLine = lines.get(0);
         return firstLine.equals("\u00a7ewww.hypixel.ne\ud83c\udf82\u00a7et") || firstLine.equals("\u00a7ewww.hypixel.ne\u00a7g\u00a7et");
      }
   }

   public static boolean isInGameLobby() {
      for (String line : getScoreboardLines()) {
         if (line.matches(".*Players: \u00a7a\\d+/\\d+.*")) {
            return true;
         }
      }
      return false;
   }
}
