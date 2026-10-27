package valor.module.render;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.scoreboard.ScoreObjective;
import org.lwjgl.opengl.GL11;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.render.WorldOverlayStyle;

public class TNTTimer extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static final int BEDWARS_FUSE_OFFSET = 28;
   public static final float BEDWARS_MAX_FUSE = 52.0F;
   public static final float DEFAULT_MAX_FUSE = 80.0F;

   public final FloatProperty scaleProp = new FloatProperty("scale", 1.0F, 0.5F, 3.0F);
   public final DecimalFormat fuseFormat = new DecimalFormat("0.0");
   public final ArrayList<EntityTNTPrimed> tntEntities = new ArrayList<>();
   public int tntCount = 0;
   public boolean isBedwars = false;

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.isEnabled() && this.tntCount != 0 && mc.thePlayer != null) {
         float partialTicks = event.getPartialTicks();
         double viewerX = ReflectionUtil.getRenderPosX(mc.getRenderManager());
         double viewerY = ReflectionUtil.getRenderPosY(mc.getRenderManager());
         double viewerZ = ReflectionUtil.getRenderPosZ(mc.getRenderManager());

         for (int i = 0; i < this.tntCount; i++) {
            EntityTNTPrimed tnt = this.tntEntities.get(i);
            int fuse;
            if (tnt != null && !tnt.isDead && (fuse = this.isBedwars ? tnt.fuse - BEDWARS_FUSE_OFFSET : tnt.fuse) > 0) {
               double renderX = tnt.lastTickPosX + (tnt.posX - tnt.lastTickPosX) * (double) partialTicks - viewerX;
               double renderY = tnt.lastTickPosY + (tnt.posY - tnt.lastTickPosY) * (double) partialTicks - viewerY;
               double renderZ = tnt.lastTickPosZ + (tnt.posZ - tnt.lastTickPosZ) * (double) partialTicks - viewerZ;
               String formattedFuse = this.fuseFormat.format((double) (((float) fuse - partialTicks) / 20.0F));
               float maxFuse = this.isBedwars ? BEDWARS_MAX_FUSE : DEFAULT_MAX_FUSE;
               Color textColor = Math.min((float) fuse / maxFuse, 1.0F) < 0.25F ? new Color(255, 105, 122) : RenderUtil.getLightTextColor();
               float scale = 0.02666667F * (Float) this.scaleProp.getValue();

               GL11.glPushMatrix();
               GlStateManager.translate((float) renderX, (float) renderY + tnt.height + 0.5F, (float) renderZ);
               GL11.glNormal3f(0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(mc.getRenderManager().playerViewX * (float) (mc.gameSettings.thirdPersonView == 2 ? -1 : 1), 1.0F, 0.0F, 0.0F);
               GlStateManager.scale(-scale, -scale, scale);

               float textWidth = RenderUtil.getStringWidth(formattedFuse);
               float fontHeight = RenderUtil.getFontHeight();
               Render3DUtil.setupRender3D();
               WorldOverlayStyle.drawPill(-textWidth / 2.0F - 6.0F, -3.0F, textWidth / 2.0F + 6.0F, fontHeight + 3.0F, 4.0F, RenderUtil.getThemeColor(), 224);
               Render3DUtil.setupRender2D();
               RenderUtil.drawString(formattedFuse, -textWidth / 2.0F, 0.0F, textColor, false);
               Render3DUtil.endRender3D();
               GlStateManager.depthMask(true);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GL11.glPopMatrix();
            }
         }
      }
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (this.isEnabled() && event.getType() == EventType.POST) {
         this.tntCount = 0;
         this.isBedwars = false;

         if (mc.theWorld != null && mc.thePlayer != null) {
            boolean isBedwarsMode = false;
            net.minecraft.scoreboard.Scoreboard scoreboard;
            ScoreObjective objective;
            if ((scoreboard = mc.theWorld.getScoreboard()) != null && (objective = scoreboard.getObjectiveInDisplaySlot(1)) != null) {
               String displayName = objective.getDisplayName();
               if (displayName == null) {
                  displayName = "";
               }
               String cleanName = displayName.replaceAll("(?i)\u00a7[0-9a-fk-or]", "").toUpperCase();
               if (cleanName.contains("BED WARS") || cleanName.contains("BEDWARS")) {
                  isBedwarsMode = true;
               }
            }

            this.isBedwars = isBedwarsMode;

            for (Entity entity : mc.theWorld.loadedEntityList) {
               if (entity instanceof EntityTNTPrimed) {
                  EntityTNTPrimed tnt = (EntityTNTPrimed) entity;
                  int fuse = this.isBedwars ? tnt.fuse - BEDWARS_FUSE_OFFSET : tnt.fuse;
                  if (fuse > 0 && !(tnt.getDistanceSqToEntity(mc.thePlayer) > 4096.0)) {
                     if (this.tntCount >= this.tntEntities.size()) {
                        this.tntEntities.add(tnt);
                     } else {
                        this.tntEntities.set(this.tntCount, tnt);
                     }
                     this.tntCount++;
                  }
               }
            }
         }
      }
   }

   public TNTTimer() {
      super("TNTTimer", false, ModuleCategory.VISUAL);
   }
}
