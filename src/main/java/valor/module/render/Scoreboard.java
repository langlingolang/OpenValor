package valor.module.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import valor.Valor;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.DraggableHudElement;
import valor.util.HudDragManager;
import valor.util.RenderUtil;

public class Scoreboard extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final Pattern IP_PATTERN = Pattern.compile("(?i)(?:[a-z0-9-]+\\.)+[a-z]{2,}(?::\\d{2,5})?|(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{2,5})?");
   public static final Pattern SERVER_PATTERN = Pattern.compile("(?i)(?:www\\.)?hypixel\\.net");
   public static final String REPLACEMENT_DOMAIN = "localhost";

   private float hudWidth = 120.0F;
   private float hudHeight = 80.0F;
   private boolean hasInitializedPos;

   public final FloatProperty posXProp = new FloatProperty("pos-x", -1.0F, -1.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", -1.0F, -1.0F, 4000.0F);
   public final BooleanProperty replaceServerAddressProp = new BooleanProperty("replace-server-address", Boolean.FALSE);
   public final BooleanProperty showNumbersProp = new BooleanProperty("show-numbers", Boolean.TRUE);

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   @Override
   public float getX() {
      return (Float)this.posXProp.getValue();
   }

   @Override
   public void setX(float x) {
      this.posXProp.setValue(x);
      this.hasInitializedPos = true;
   }

   @Override
   public void setY(float y) {
      this.posYProp.setValue(y);
      this.hasInitializedPos = true;
   }

   @Override
   public float getY() {
      return (Float)this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   public String sanitizeScoreboardText(String text) {
      if (text == null) {
         return "";
      } else {
         if ((Boolean)this.replaceServerAddressProp.getValue()) {
            StringBuilder clean = new StringBuilder(text.length());
            int[] indices = new int[text.length() + 1];
            int cleanLen = 0;
            int i = 0;

            while (i < text.length()) {
               if (text.charAt(i) == 167 && i + 1 < text.length()) {
                  i += 2;
               } else {
                  indices[cleanLen++] = i;
                  clean.append(text.charAt(i++));
               }
            }

            indices[cleanLen] = text.length();
            Matcher serverMatcher = SERVER_PATTERN.matcher(clean);
            ArrayList<int[]> replacements = new ArrayList<>();

            while (serverMatcher.find()) {
               replacements.add(new int[]{indices[serverMatcher.start()], indices[serverMatcher.end()]});
            }

            if (replacements.isEmpty()) {
               Matcher ipMatcher = IP_PATTERN.matcher(clean);
               while (ipMatcher.find()) {
                  replacements.add(new int[]{indices[ipMatcher.start()], indices[ipMatcher.end()]});
               }
            }

            String replacedText;
            if (replacements.isEmpty()) {
               replacedText = text;
            } else {
               StringBuilder sb = new StringBuilder(text);
               for (int r = replacements.size() - 1; r >= 0; r--) {
                  int[] span = replacements.get(r);
                  sb.replace(span[0], span[1], REPLACEMENT_DOMAIN);
               }
               replacedText = sb.toString();
            }

            text = replacedText;
         }

         Module nickHider;
         if (Valor.moduleManager != null && (nickHider = Valor.moduleManager.getModule(NickHider.class)) instanceof NickHider && nickHider.isEnabled()) {
            text = ((NickHider)nickHider).replaceNick(text);
         }

         StringBuilder formatted = new StringBuilder(text.length());
         for (int j = 0; j < text.length(); j++) {
            char ch = text.charAt(j);
            formatted.append(ch);
            if (ch == 167 && j + 1 < text.length()) {
               formatted.append(Character.toUpperCase(text.charAt(++j)));
            }
         }

         return formatted.toString();
      }
   }

   @EventTarget
   public void onRender(Render2DEvent event) {
      if (this.isEnabled() && mc.theWorld != null && !mc.gameSettings.showDebugInfo) {
         net.minecraft.scoreboard.Scoreboard worldScoreboard;
         ScoreObjective objective = (worldScoreboard = mc.theWorld.getScoreboard()) == null ? null : worldScoreboard.getObjectiveInDisplaySlot(1);
         ScorePlayerTeam team;
         int colorIndex;
         ScoreObjective teamObjective;
         if (worldScoreboard != null
            && mc.thePlayer != null
            && (team = worldScoreboard.getPlayersTeam(mc.thePlayer.getName())) != null
            && team.getChatFormat() != null
            && (colorIndex = team.getChatFormat().getColorIndex()) >= 0
            && (teamObjective = worldScoreboard.getObjectiveInDisplaySlot(colorIndex + 3)) != null) {
            objective = teamObjective;
         }

         boolean isChatPreview = objective == null && HudDragManager.isChatGuiOpen();
         if (objective != null || isChatPreview) {
            String title = this.sanitizeScoreboardText(isChatPreview ? "Scoreboard" : objective.getDisplayName());
            ArrayList<String> lines = new ArrayList<>();
            ArrayList<String> scores = new ArrayList<>();
            if (isChatPreview) {
               lines.add("Server");
               scores.add("");
               lines.add(this.replaceServerAddressProp.getValue() ? REPLACEMENT_DOMAIN : "play.example.net");
               scores.add("");
            } else {
               ArrayList<Score> scoreList = new ArrayList<>();
               Iterator it = worldScoreboard.getSortedScores(objective).iterator();

               while (it.hasNext()) {
                  Score sc;
                  String playerName;
                  if ((playerName = (sc = (Score)it.next()).getPlayerName()) != null && !playerName.startsWith("#")) {
                     scoreList.add(sc);
                  }
               }

               int startIndex = Math.max(0, scoreList.size() - 15);
               for (int idx = scoreList.size() - 1; idx >= startIndex; idx--) {
                  Score sc = scoreList.get(idx);
                  String pName = sc.getPlayerName();
                  lines.add(this.sanitizeScoreboardText(ScorePlayerTeam.formatPlayerName(worldScoreboard.getPlayersTeam(pName), pName)));
                  scores.add(String.valueOf(sc.getScorePoints()));
               }
            }

            float titleHeight = RenderUtil.getFontHeight() + 10.0F;
            float lineRowHeight = RenderUtil.getFontHeight() + 4.0F;
            float totalWidth = Math.max(90.0F, RenderUtil.getStringWidth(title) + 16.0F);

            for (int k = 0; k < lines.size(); k++) {
               float lineWidth = RenderUtil.getStringWidth(lines.get(k));
               if ((Boolean)this.showNumbersProp.getValue() && !scores.get(k).isEmpty()) {
                  lineWidth += 10.0F + RenderUtil.getStringWidth(scores.get(k));
               }

               totalWidth = Math.max(totalWidth, lineWidth + 16.0F);
            }

            float totalHeight = titleHeight + 8.0F;
            for (String lineText : lines) {
               totalHeight += EnumChatFormatting.getTextWithoutFormattingCodes(lineText).trim().isEmpty() ? 5.0F : lineRowHeight;
            }

            if (!this.hasInitializedPos) {
               if ((Float)this.posXProp.getValue() < 0.0F || (Float)this.posYProp.getValue() < 0.0F) {
                  ScaledResolution sr = new ScaledResolution(mc);
                  this.posXProp.setValue(Math.max(0.0F, (float)sr.getScaledWidth() - totalWidth - 6.0F));
                  this.posYProp.setValue(Math.max(0.0F, ((float)sr.getScaledHeight() - totalHeight) / 2.0F));
               }

               this.hasInitializedPos = true;
            }

            float drawX = (Float)this.posXProp.getValue();
            float drawY = (Float)this.posYProp.getValue();
            RenderUtil.drawHudBackground(drawX, drawY, totalWidth, totalHeight);
            long time = System.currentTimeMillis();
            RenderUtil.getGradientColor(0, Math.max(1, lines.size()), time);
            RenderUtil.drawBoldString(title, drawX + 8.0F, drawY + 7.0F, RenderUtil.getLightTextColor());
            float curRowY = drawY + titleHeight;

            for (int row = 0; row < lines.size(); row++) {
               if (EnumChatFormatting.getTextWithoutFormattingCodes(lines.get(row)).trim().isEmpty()) {
                  curRowY += 5.0F;
               } else {
                  float textY = curRowY + (lineRowHeight - RenderUtil.getFontHeight()) / 2.0F;
                  curRowY += lineRowHeight;
                  RenderUtil.drawString(lines.get(row), drawX + 8.0F, textY, RenderUtil.getLightTextColor(), false);
                  String scoreStr = scores.get(row);
                  if ((Boolean)this.showNumbersProp.getValue() && !scoreStr.isEmpty()) {
                     RenderUtil.drawString(scoreStr, drawX + totalWidth - 8.0F - RenderUtil.getStringWidth(scoreStr), textY, RenderUtil.getDefaultTextColor(), false);
                  }
               }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hudWidth = totalWidth;
            this.hudHeight = totalHeight;
            HudDragManager.handleDragging(this);
         }
      }
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled() && !mc.gameSettings.showDebugInfo;
   }

   public Scoreboard() {
      super("InfoHUD", false, false, ModuleCategory.VISUAL);
   }
}
