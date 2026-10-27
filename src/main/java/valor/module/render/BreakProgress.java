package valor.module.render;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ModeProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.render.WorldOverlayStyle;

public class BreakProgress extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"PERCENTAGE", "SECOND", "DECIMAL"});
   public final BooleanProperty showManualProp = new BooleanProperty("show-manual", Boolean.TRUE);
   public final BooleanProperty progressBarProp = new BooleanProperty("progress-bar", Boolean.FALSE);

   private double currentProgress;
   private double interpolatedProgress;
   private BlockPos targetBlockPos;
   private String progressText = "";

   @Override
   public String getDescription() {
      return "Shows block breaking progress.";
   }

   public void resetProgress() {
      this.currentProgress = 0.0;
      this.interpolatedProgress = 0.0;
      this.targetBlockPos = null;
      this.progressText = "";
   }

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled() && this.targetBlockPos != null && !this.progressText.isEmpty() && mc.thePlayer != null) {
         float var2 = var1.getPartialTicks();
         double var7 = (double)this.targetBlockPos.getX() + 0.5 - ReflectionUtil.getRenderPosX(mc.getRenderManager());
         double var4 = (double)this.targetBlockPos.getY() + 0.5 - ReflectionUtil.getRenderPosY(mc.getRenderManager());
         double var6 = (double)this.targetBlockPos.getZ() + 0.5 - ReflectionUtil.getRenderPosZ(mc.getRenderManager());
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)var7, (float)var4, (float)var6);
         GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(mc.getRenderManager().playerViewX * (float)(mc.gameSettings.thirdPersonView == 2 ? -1 : 1), 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
         GlStateManager.depthMask(false);
         float var8 = RenderUtil.getStringWidth(this.progressText);
         float var9 = RenderUtil.getFontHeight();
         float var3 = Math.max(var8 + 12.0F, this.progressBarProp.getValue() ? 54.0F : 0.0F);
         float var11 = var9 + (this.progressBarProp.getValue() ? 13.0F : 6.0F);
         Render3DUtil.setupRender3D();
         WorldOverlayStyle.drawPill(-var3 / 2.0F, -var9 - 3.0F, var3 / 2.0F, -var9 - 3.0F + var11, 5.0F, RenderUtil.getThemeColor(), 224);
         if ((Boolean)this.progressBarProp.getValue()) {
            int var10 = (int)Math.round(42.0 * Math.max(0.0, Math.min(1.0, this.interpolatedProgress)));
            Render3DUtil.drawRect(-21.0F, 2.0F, 21.0F, 6.0F, -1341386726);
            Color var12 = RenderUtil.getThemeColor();
            Render3DUtil.drawRect(-21.0F, 2.0F, (float)(-21 + var10), 6.0F, var12.getRGB());
         }

         Render3DUtil.setupRender2D();
         RenderUtil.drawString(this.progressText, -var8 / 2.0F, -var9, RenderUtil.getLightTextColor(), false);
         Render3DUtil.endRender3D();
         GlStateManager.depthMask(true);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.POST) {
         if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.capabilities.isCreativeMode || !mc.thePlayer.capabilities.allowEdit) {
            this.resetProgress();
         } else if ((Boolean)this.showManualProp.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
            this.currentProgress = (double)ReflectionUtil.getCurBlockDamageMP(mc.playerController);
            if (this.currentProgress == 0.0) {
               this.resetProgress();
            } else {
               this.targetBlockPos = mc.objectMouseOver.getBlockPos();
               switch (this.modeProp.getValue()) {
                  case 0:
                     this.progressText = (int)(100.0 * this.currentProgress) + "%";
                     break;
                  case 1:
                     float var2;
                     double var3;
                     this.progressText = this.targetBlockPos == null
                        ? "0s"
                        : (
                           (var2 = mc.theWorld.getBlockState(this.targetBlockPos).getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, this.targetBlockPos)) <= 0.0F
                              ? "0s"
                              : ((var3 = (double)Math.round(Math.max(0.0, 1.0 - this.currentProgress) / (double)var2 / 20.0 * 10.0) / 10.0) == 0.0 ? "0" : var3 + "s")
                        );
                     break;
                  case 2:
                     this.progressText = String.format("%.2f", this.currentProgress);
                     break;
                  default:
                     this.progressText = "";
               }

               this.interpolatedProgress = this.interpolatedProgress + (this.currentProgress - this.interpolatedProgress) * 0.3;
            }
         } else {
            this.resetProgress();
         }
      }
   }

   public BreakProgress() {
      super("BreakProgress", false, ModuleCategory.VISUAL);
   }
}
