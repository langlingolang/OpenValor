package valor.util;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import valor.Valor;

public final class BotCheckUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static boolean isBot(EntityPlayer player) {
      if (player == mc.thePlayer) {
         return false;
      } else {
         NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getName());
         if (info == null) {
            return true;
         } else if (!TeamsUtil.isHypixel()) {
            return false;
         } else if (player.getName().startsWith("\u00a7k")) {
            return player.isInvisible();
         } else if (info.getResponseTime() <= 0) {
            return true;
         } else {
            ScorePlayerTeam team = info.getPlayerTeam();
            if (team == null) {
               return false;
            } else {
               return team.getTeamName().isEmpty() && team.getColorPrefix().equals("\u00a7c");
            }
         }
      }
   }

   public static boolean isFriend(EntityPlayer player) {
      return Valor.friendManager.contains(player.getName());
   }

   public static Color getTeamColor(EntityPlayer player, float alpha) {
      int rgb = 16777215;
      ScorePlayerTeam team = (ScorePlayerTeam) player.getTeam();
      String format;
      if (team != null && (format = FontRenderer.getFormatFromString(team.getColorPrefix())).length() >= 2) {
         rgb = mc.fontRendererObj.getColorCode(format.charAt(1));
      }

      return new Color(rgb & 16777215 | (int) (alpha * 255.0F) << 24, true);
   }

   public static boolean isTeammateBedwars(EntityLivingBase entity) {
      if (entity == mc.thePlayer) {
         return true;
      } else {
         NetworkPlayerInfo selfInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
         if (selfInfo == null) {
            return false;
         } else {
            ScorePlayerTeam selfTeam = selfInfo.getPlayerTeam();
            if (selfTeam == null || selfTeam.getColorPrefix().length() < 2) {
               return false;
            } else {
               EntityLivingBase armorStand = mc.theWorld.findNearestEntityWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox(), entity);
               return armorStand != null && armorStand.getName().contains(selfTeam.getColorPrefix().substring(0, 2));
            }
         }
      }
   }

   public static boolean isTeammate(EntityPlayer player) {
      if (player == mc.thePlayer) {
         return true;
      } else {
         NetworkPlayerInfo selfInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
         if (selfInfo == null) {
            return false;
         } else {
            ScorePlayerTeam selfTeam = selfInfo.getPlayerTeam();
            if (selfTeam == null) {
               return false;
            } else {
               NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getUniqueID());
               if (playerInfo == null) {
                  return false;
               } else {
                  ScorePlayerTeam playerTeam = playerInfo.getPlayerTeam();
                  return playerTeam != null && selfTeam.getColorPrefix().equals(playerTeam.getColorPrefix());
               }
            }
         }
      }
   }

   public static List<Entity> getSortedLoadedEntities() {
      return mc.theWorld.loadedEntityList.stream().sorted((e1, e2) -> {
         double dist1 = mc.getRenderManager().getDistanceToCamera(e1.posX, e1.posY, e1.posZ);
         double dist2 = mc.getRenderManager().getDistanceToCamera(e2.posX, e2.posY, e2.posZ);
         if (dist1 < dist2) {
            return 1;
         } else {
            return dist1 > dist2 ? -1 : e1.getUniqueID().toString().compareTo(e2.getUniqueID().toString());
         }
      }).collect(Collectors.toList());
   }

   public static boolean isTarget(EntityPlayer player) {
      return Valor.targetManager.contains(player.getName());
   }

   public static float getEffectiveHealth(EntityLivingBase entity) {
      return entity.getHealth() * (20.0F / (float) entity.getTotalArmorValue());
   }

   public static String getFormattedCleanName(Entity entity) {
      return entity.getDisplayName().getFormattedText().replaceAll("\u00a7\\S$", "").replaceAll("(?i)\u00a7r", "\u00a7f").trim();
   }

   public static boolean isShopNpc(EntityLivingBase entity) {
      if (entity == mc.thePlayer) {
         return false;
      } else {
         EntityLivingBase nearest = mc.theWorld.findNearestEntityWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox(), entity);
         if (nearest == null) {
            return false;
         } else {
            String name = nearest.getName();
            return name.contains("RIGHT CLICK")
               || name.contains("ITEM SHOP")
               || name.contains("UPGRADES")
               || name.contains("BANKER")
               || name.contains("STREAK POWERS");
         }
      }
   }

   public static boolean isEntityLoaded(Entity entity) {
      return entity != null && mc.theWorld.loadedEntityList.contains(entity);
   }
}
