package valor.module.render;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;
import valor.Valor;
import valor.event.EventTarget;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BotCheckUtil;
import valor.util.ChatColor;
import valor.util.ColorUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.render.WorldOverlayStyle;

public class NameTags extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

   public final FloatProperty scaleProp = new FloatProperty("scale", 1.0F, 0.5F, 2.0F);
   public final BooleanProperty autoScaleProp = new BooleanProperty("auto-scale", Boolean.TRUE);
   public final PercentageProperty backgroundProp = new PercentageProperty("background", 25);
   public final BooleanProperty shadowProp = new BooleanProperty("shadow", Boolean.TRUE);
   public final ModeProperty distanceProp = new ModeProperty("distance", 0, new String[]{"NONE", "DEFAULT", "VAPE"});
   public final ModeProperty healthProp = new ModeProperty("health", 2, new String[]{"NONE", "HP", "HEARTS", "TAB"});
   public final BooleanProperty armorProp = new BooleanProperty("armor", Boolean.TRUE);
   public final BooleanProperty effectsProp = new BooleanProperty("effects", Boolean.TRUE);
   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty friendsProp = new BooleanProperty("friends", Boolean.TRUE);
   public final BooleanProperty enemiesProp = new BooleanProperty("enemies", Boolean.TRUE);
   public final BooleanProperty bossesProp = new BooleanProperty("bosses", Boolean.FALSE);
   public final BooleanProperty mobsProp = new BooleanProperty("mobs", Boolean.FALSE);
   public final BooleanProperty creepersProp = new BooleanProperty("creepers", Boolean.FALSE);
   public final BooleanProperty endermenProp = new BooleanProperty("endermen", Boolean.FALSE);
   public final BooleanProperty blazesProp = new BooleanProperty("blazes", Boolean.FALSE);
   public final BooleanProperty animalsProp = new BooleanProperty("animals", Boolean.FALSE);
   public final BooleanProperty selfProp = new BooleanProperty("self", Boolean.FALSE);
   public final BooleanProperty botsProp = new BooleanProperty("bots", Boolean.FALSE);

   public boolean isValidEntity(EntityLivingBase entity) {
      if (entity.deathTime > 0) {
         return false;
      } else if (mc.getRenderViewEntity().getDistanceToEntity(entity) > 512.0F) {
         return false;
      } else if (entity instanceof EntityPlayer) {
         if (entity != mc.thePlayer && entity != mc.getRenderViewEntity()) {
            if (BotCheckUtil.isBot((EntityPlayer) entity)) {
               return (Boolean) this.botsProp.getValue();
            } else if (BotCheckUtil.isFriend((EntityPlayer) entity)) {
               return (Boolean) this.friendsProp.getValue();
            } else {
               return BotCheckUtil.isTarget((EntityPlayer) entity) ? (Boolean) this.enemiesProp.getValue() : (Boolean) this.playersProp.getValue();
            }
         } else {
            return (Boolean) this.selfProp.getValue() && mc.gameSettings.thirdPersonView != 0;
         }
      } else if (!(entity instanceof EntityDragon) && !(entity instanceof EntityWither)) {
         if (!(entity instanceof EntityMob) && !(entity instanceof EntitySlime)) {
            return (entity instanceof EntityAnimal || entity instanceof EntityBat || entity instanceof EntitySquid || entity instanceof EntityVillager)
               && (Boolean) this.animalsProp.getValue();
         } else if (entity instanceof EntityCreeper) {
            return (Boolean) this.creepersProp.getValue();
         } else if (entity instanceof EntityEnderman) {
            return (Boolean) this.endermenProp.getValue();
         } else {
            return entity instanceof EntityBlaze ? (Boolean) this.blazesProp.getValue() : (Boolean) this.mobsProp.getValue();
         }
      } else {
         return !entity.isInvisible() && (Boolean) this.bossesProp.getValue();
      }
   }

   @EventTarget
   public void onRender(Render3DEvent event) {
      if (!this.isEnabled()) {
         return;
      }

      for (Entity entity : BotCheckUtil.getSortedLoadedEntities()) {
         String formattedName;
         if (entity instanceof EntityLivingBase
            && this.isValidEntity((EntityLivingBase) entity)
            && (entity.ignoreFrustumCheck || Render3DUtil.isBoundingBoxInFrustum(entity.getEntityBoundingBox(), 10.0))
            && !StringUtils.isBlank(EnumChatFormatting.getTextWithoutFormattingCodes(formattedName = BotCheckUtil.getFormattedCleanName(entity)))) {

            double renderX = Render3DUtil.interpolate(entity.posX, entity.lastTickPosX, (double) event.getPartialTicks()) - ReflectionUtil.getRenderPosX(mc.getRenderManager());
            double renderY = Render3DUtil.interpolate(entity.posY, entity.lastTickPosY, (double) event.getPartialTicks())
               - ReflectionUtil.getRenderPosY(mc.getRenderManager())
               + (double) entity.getEyeHeight();
            double renderZ = Render3DUtil.interpolate(entity.posZ, entity.lastTickPosZ, (double) event.getPartialTicks()) - ReflectionUtil.getRenderPosZ(mc.getRenderManager());
            double distance = (double) mc.getRenderViewEntity().getDistanceToEntity(entity);

            GlStateManager.pushMatrix();
            GlStateManager.translate(renderX, renderY + (entity.isSneaking() ? 0.225 : 0.4), renderZ);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            float viewPitch = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(mc.getRenderManager().playerViewX, viewPitch, 0.0F, 0.0F);

            double autoScaleDist = (Boolean) this.autoScaleProp.getValue() ? distance : 0.0;
            double scaleFactor = Math.pow(Math.min(Math.max(autoScaleDist, 6.0), 128.0), 0.75) * 0.0075 * (double) ((Float) this.scaleProp.getValue()).floatValue();
            GlStateManager.scale(-scaleFactor, -scaleFactor, 1.0);

            String distanceText = "";
            switch ((Integer) this.distanceProp.getValue()) {
               case 1:
                  distanceText = String.format("&7%dm&r ", (int) distance);
                  break;
               case 2:
                  distanceText = String.format("&a[&f%d&a]&r ", (int) distance);
            }

            EntityLivingBase livingEntity = (EntityLivingBase) entity;
            float health = livingEntity.getHealth();
            float absorption = livingEntity.getAbsorptionAmount();
            float maxHealth = livingEntity.getMaxHealth();
            float healthRatio = Math.min(Math.max((health + absorption) / maxHealth, 0.0F), 1.0F);

            String healthText = "";
            switch ((Integer) this.healthProp.getValue()) {
               case 1:
                  healthText = String.format(" %d%s", (int) health, absorption > 0.0F ? String.format(" &6%d&r", (int) absorption) : "&r");
                  break;
               case 2:
                  healthText = String.format(
                     " %s%s", HEALTH_FORMAT.format((double) health / 2.0), absorption > 0.0F ? String.format(" &6%s&r", HEALTH_FORMAT.format((double) absorption / 2.0)) : "&r"
                  );
                  break;
               case 3:
                  Score score;
                  net.minecraft.scoreboard.Scoreboard scoreboard;
                  ScoreObjective objective;
                  if (entity instanceof EntityPlayer
                     && (scoreboard = mc.theWorld.getScoreboard()) != null
                     && (objective = scoreboard.getObjectiveInDisplaySlot(2)) != null
                     && (score = scoreboard.getValueFromObjective(entity.getName(), objective)) != null) {
                     healthText = String.format(" &e%d&r", score.getScorePoints());
                  }
            }

            String fullTagText = ChatColor.format(String.format("%s&f%s&r%s", distanceText, formattedName, healthText));
            float textWidth = RenderUtil.getStringWidth(fullTagText);
            float fontHeight = RenderUtil.getFontHeight();

            if ((Integer) this.backgroundProp.getValue() > 0) {
               int bgAlpha = Math.round(175.0F + (float) ((Integer) this.backgroundProp.getValue()).intValue() * 0.65F);
               Color pillColor = RenderUtil.getThemeColor();
               if (entity instanceof EntityPlayer) {
                  if (BotCheckUtil.isFriend((EntityPlayer) entity)) {
                     pillColor = Valor.friendManager.color;
                  } else if (BotCheckUtil.isTarget((EntityPlayer) entity)) {
                     pillColor = Valor.targetManager.color;
                  }
               }

               Render3DUtil.setupRender3D();
               WorldOverlayStyle.drawPill(-textWidth / 2.0F - 5.0F, -fontHeight - 3.0F, textWidth / 2.0F + 5.0F, 3.0F, 4.0F, pillColor, bgAlpha);
               Render3DUtil.endRender3D();
            }

            GlStateManager.disableDepth();
            RenderUtil.drawString(fullTagText, -textWidth / 2.0F, -fontHeight, ColorUtil.getHealthColor(healthRatio), (Boolean) this.shadowProp.getValue());
            GlStateManager.enableDepth();

            if (entity instanceof EntityPlayer) {
               int yOffset = Math.round(fontHeight) + 2;
               if ((Boolean) this.armorProp.getValue()) {
                  ArrayList<ItemStack> armorList = new ArrayList<>();
                  for (int slot = 4; slot >= 0; slot--) {
                     ItemStack stack = slot == 0 ? ((EntityPlayer) entity).getHeldItem() : ((EntityPlayer) entity).inventory.armorInventory[slot - 1];
                     if (stack != null) {
                        armorList.add(stack);
                     }
                  }

                  if (!armorList.isEmpty()) {
                     int xOffset = armorList.size() * -8;
                     for (int i = 0; i < armorList.size(); i++) {
                        Render3DUtil.renderItemStack(armorList.get(i), xOffset + (i << 4), -yOffset - 16);
                     }
                     yOffset += 16;
                  }
               }

               if ((Boolean) this.effectsProp.getValue()) {
                  List<PotionEffect> activeEffects = ((EntityPlayer) entity)
                     .getActivePotionEffects()
                     .stream()
                     .filter(pe -> Potion.potionTypes[pe.getPotionID()].hasStatusIcon())
                     .collect(Collectors.toList());

                  if (!activeEffects.isEmpty()) {
                     GlStateManager.pushMatrix();
                     GlStateManager.scale(0.5F, 0.5F, 1.0F);
                     int effectXOffset = activeEffects.size() * -9;
                     for (int i = 0; i < activeEffects.size(); i++) {
                        Render3DUtil.renderPotionIcon(activeEffects.get(i), effectXOffset + i * 18, -(yOffset << 1) - 18);
                     }
                     GlStateManager.popMatrix();
                  }
               }
            }

            GlStateManager.popMatrix();
         }
      }
   }

   public NameTags() {
      super("NameTags", false, ModuleCategory.VISUAL);
   }
}
