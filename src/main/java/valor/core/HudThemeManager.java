package valor.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import valor.Valor;
import valor.command.CommandManager;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.setting.Property;
import valor.util.ColorUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;

public final class HudThemeManager {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final HudThemeManager INSTANCE = new HudThemeManager();

   public static final int STYLE_VALOR = 0;
   public static final int STYLE_BLUR = 1;

   public final ModeProperty hudStyleProp = new ModeProperty("hud-style", 1, new String[]{"VALOR", "BLUR"});
   public final ModeProperty colorProp = new ModeProperty("color", 6, new String[]{"RAINBOW", "ASTOLFO", "CUSTOM", "RISE", "RAVEN", "SLINKY", "STATIC"});
   public final FloatProperty colorSpeedProp = new FloatProperty("color-speed", 0.45F, 0.15F, 1.5F);
   public final FloatProperty colorDistProp = new FloatProperty("color-dist", 10.0F, 1.0F, 100.0F);
   public final ColorProperty customColor1Prop = new ColorProperty("custom-color-1", new Color(85, 255, 255).getRGB(), () -> (Integer)this.colorProp.getValue() == 2);
   public final ColorProperty customColor2Prop = new ColorProperty("custom-color-2", new Color(255, 85, 255).getRGB(), () -> (Integer)this.colorProp.getValue() == 2);
   public final ColorProperty staticColorProp = new ColorProperty("static-color", new Color(174, 157, 218).getRGB(), () -> (Integer)this.colorProp.getValue() == 6);
   public final FloatProperty scaleProp = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
   public final BooleanProperty shadowProp = new BooleanProperty("shadow", Boolean.TRUE);
   public final ModeProperty guiSizeProp = new ModeProperty("gui-size", 1, new String[]{"COMPACT", "NORMAL", "LARGE"});
   public final ModeProperty hudSizeProp = new ModeProperty("hud-size", 1, new String[]{"COMPACT", "NORMAL", "LARGE"});
   public final ModeProperty fontProp = new ModeProperty("font", 0, new String[]{"INTER", "COMFORTAA", "SANS", "VANILLA"});
   public final BooleanProperty chatOutlineProp = new BooleanProperty("chat-outline", Boolean.TRUE);
   public final BooleanProperty blinkTimerProp = new BooleanProperty("blink-timer", Boolean.TRUE);
   public final BooleanProperty toggleAlertsProp = new BooleanProperty("toggle-alerts", Boolean.FALSE);

   public static HudThemeManager getInstance() {
      return INSTANCE;
   }

   public static HudThemeManager getTheme() {
      return INSTANCE;
   }

   public final List<Property<?>> getProperties() {
      return new ArrayList<>(
         Arrays.asList(
            this.colorProp,
            this.colorSpeedProp,
            this.colorDistProp,
            this.customColor1Prop,
            this.customColor2Prop,
            this.staticColorProp,
            this.scaleProp,
            this.shadowProp,
            this.guiSizeProp,
            this.hudSizeProp,
            this.fontProp,
            this.chatOutlineProp,
            this.blinkTimerProp,
            this.toggleAlertsProp
         )
      );
   }

   public static float getFontHeight() {
      return RenderUtil.getFontHeight();
   }

   public static float getStringWidth(String text) {
      return RenderUtil.getStringWidth(text);
   }

   public static void drawString(String text, float x, float y, int color, boolean shadow) {
      RenderUtil.drawString(text, x, y, new Color(color, true), shadow);
   }

   private long getColorCycleTime() {
      float speed = Math.min(Math.max(0.15F, (Float)this.colorSpeedProp.getValue()), 1.5F);
      return Math.max(1200L, (long)(5200.0 / Math.pow((double)speed, 2.0)));
   }

   public final Color getColor(long time, long offset) {
      long cycleTime = this.getColorCycleTime();
      float fraction = 1.0F - (float)(Math.abs(time - offset * 18L) % cycleTime) / (float)cycleTime;
      return this.getColorAtFraction(fraction);
   }

   public final Color getColor(int index, int total, long time) {
      total = Math.max(1, total);
      long cycleTime = this.getColorCycleTime();
      float progress = (float)(time % cycleTime) / (float)cycleTime;
      float spread;
      if (total <= 1) {
         spread = 0.0F;
      } else if ((Integer)this.colorProp.getValue() == 6) {
         spread = 0.4F;
      } else {
         spread = Math.min(0.7F, 0.38F + (float)(total - 1) * 0.014F);
      }

      float step = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      return this.getColorAtFraction(progress + step * spread);
   }

   private static float cosineInterpolate(float value) {
      return 0.5F - 0.5F * (float)Math.cos((double)value * 2.0 * Math.PI);
   }

   private Color getColorAtFraction(float fraction) {
      float f = fraction % 1.0F;
      fraction = f < 0.0F ? f + 1.0F : f;
      switch (this.colorProp.getValue()) {
         case 1:
            return ColorUtil.fromHSB(fraction < 0.5F ? 1.0F - fraction : fraction, 0.55F);
         case 2:
            return ColorUtil.lerp(cosineInterpolate(fraction), new Color((Integer)this.customColor1Prop.getValue()), new Color((Integer)this.customColor2Prop.getValue()));
         case 3:
            return ColorUtil.lerp(cosineInterpolate(fraction), new Color(255, 90, 90), new Color(255, 140, 100));
         case 4:
            return ColorUtil.lerp(cosineInterpolate(fraction), new Color(255, 80, 180), new Color(220, 40, 140));
         case 5:
            return ColorUtil.lerp(cosineInterpolate(fraction), new Color(80, 220, 255), new Color(40, 160, 230));
         case 6:
            Color staticCol = new Color((Integer)this.staticColorProp.getValue());
            float[] hsb = Color.RGBtoHSB(staticCol.getRed(), staticCol.getGreen(), staticCol.getBlue(), null);
            fraction = Math.min(1.0F, Math.max(0.45F, hsb[2] * (0.86F + 0.14F * cosineInterpolate(fraction))));
            return Color.getHSBColor(hsb[0], hsb[1], fraction);
         default:
            return ColorUtil.fromHSB(fraction, 1.0F);
      }
   }

   @EventTarget
   public final void onRender2D(Render2DEvent event) {
      if ((Boolean)this.chatOutlineProp.getValue() && mc.currentScreen instanceof GuiChat) {
         GuiTextField inputField;
         String text = (inputField = ReflectionUtil.getInputField((GuiChat)mc.currentScreen)) == null ? "" : inputField.getText().trim();
         if (Valor.commandManager != null && CommandManager.isCommand(text)) {
            Render3DUtil.setupRender3D();
            Render3DUtil.drawRectOutline(
               (float)(mc.currentScreen.height - 14),
               (float)(mc.currentScreen.width - 2),
               (float)(mc.currentScreen.height - 2),
               this.getColor(System.currentTimeMillis(), 0L).getRGB()
            );
            Render3DUtil.setupRender2D();
         }
      }

      long currentTime = System.currentTimeMillis();
      if ((Boolean)this.blinkTimerProp.getValue() && Valor.blinkManager != null && Valor.blinkManager.isBlinking) {
         long count = Valor.blinkManager.getPlayerPacketCount();
         String blinkText = String.format("%.1fs", (float)count / 20.0F);
         float scale = (Float)this.scaleProp.getValue();
         ScaledResolution sr = new ScaledResolution(mc);
         float x = (float)sr.getScaledWidth() / 2.0F;
         float y = (float)sr.getScaledHeight() / 2.0F + 12.0F * scale;
         GlStateManager.pushMatrix();
         GlStateManager.scale(scale, scale, 1.0F);
         RenderUtil.drawString(
            blinkText,
            (x - RenderUtil.getStringWidth(blinkText) * scale / 2.0F) / scale,
            y / scale,
            this.getColor(currentTime, 0L),
            (Boolean)this.shadowProp.getValue()
         );
         GlStateManager.popMatrix();
      }
   }
}
