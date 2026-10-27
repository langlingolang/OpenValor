package valor.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render2DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.DraggableHudElement;
import valor.util.FloatInterpolator;
import valor.util.HudDragManager;
import valor.util.RenderUtil;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public class ArrayListMod extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final Set<String> EXCLUDED_MODULES = new HashSet<>();

   private List<Module> activeModules = new ArrayList<>();
   private final List<Module> animatingModules = new ArrayList<>();
   private final Map<Module, FloatInterpolator> moduleFadeMap = new IdentityHashMap<>();
   private float hudWidth = 80.0F;
   private float hudHeight = 20.0F;
   private boolean hasInitializedPos = false;
   private int lastVerticalMode = -1;

   public final FloatProperty posXProp = new FloatProperty("pos-x", 2.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 2.0F, 0.0F, 4000.0F);
   public final BooleanProperty backgroundProp = new BooleanProperty("background", Boolean.TRUE);
   public final ModeProperty horizontalProp = new ModeProperty("horizontal", 1, new String[]{"LEFT", "RIGHT"});
   public final ModeProperty verticalProp = new ModeProperty("vertical", 0, new String[]{"DOWN", "UP"});

   public boolean isModuleEligible(Module module) {
      if (module.isEnabled() && !module.isHidden()) {
         String name = module.getName() == null ? "" : module.getName().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
         return !EXCLUDED_MODULES.contains(name);
      } else {
         return false;
      }
   }

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

   public int getModuleTextWidth(Module module) {
      String name = module.getName();
      String suffix = formatModuleSuffix(module);
      return Math.round(RenderUtil.getStringWidth(name) + (suffix.isEmpty() ? 0.0F : RenderUtil.getStringWidth(suffix)));
   }

   public static String formatModuleSuffix(Module module) {
      String[] suffixes;
      if ((suffixes = module.getSuffix()) != null && suffixes.length != 0) {
         StringBuilder sb = new StringBuilder();
         for (String s : suffixes) {
            if (s != null && !s.isEmpty()) {
               sb.append(' ').append(s);
            }
         }
         return sb.toString();
      } else {
         return "";
      }
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (this.isEnabled() && event.getType() == EventType.POST) {
         this.activeModules = Valor.moduleManager
            .modules
            .values()
            .stream()
            .filter(this::isModuleEligible)
            .sorted(Comparator.comparingInt(this::getModuleTextWidth).reversed())
            .collect(Collectors.toList());
      }
   }

   @Override
   public void setY(float y) {
      this.posYProp.setValue(this.verticalProp.getValue() == 1 ? y + this.hudHeight : y);
      this.hasInitializedPos = true;
   }

   @Override
   public float getY() {
      return this.verticalProp.getValue() == 1 ? (Float)this.posYProp.getValue() - this.hudHeight : (Float)this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled() && !mc.gameSettings.showDebugInfo;
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
         long currentTime = System.currentTimeMillis();
         ScaledResolution sr = new ScaledResolution(mc);
         float rowHeight = RenderUtil.getFontHeight() + 3.0F;

         for (Module mod : this.activeModules) {
            if (!this.animatingModules.contains(mod)) {
               this.animatingModules.add(mod);
               this.moduleFadeMap.put(mod, new FloatInterpolator(0.0F));
            }
         }

         for (int i = this.animatingModules.size() - 1; i >= 0; i--) {
            Module mod = this.animatingModules.get(i);
            FloatInterpolator interp = this.moduleFadeMap.get(mod);
            boolean active = this.activeModules.contains(mod);
            interp.interpolateTo(active ? 1.0F : 0.0F, active ? 190L : 150L, currentTime);
            if (!active && interp.isFinished(currentTime)) {
               this.animatingModules.remove(i);
               this.moduleFadeMap.remove(mod);
            }
         }

         this.animatingModules.sort(Comparator.comparingInt(this::getModuleTextWidth).reversed());
         ArrayList<Module> visibleModules = new ArrayList<>();

         for (Module mod : this.animatingModules) {
            if (this.moduleFadeMap.get(mod).getValue(currentTime) * rowHeight >= 0.5F) {
               visibleModules.add(mod);
            }
         }

         int visibleCount = visibleModules.size();
         float maxWidth = 80.0F;

         for (Module mod : visibleModules) {
            maxWidth = Math.max(maxWidth, (float)this.getModuleTextWidth(mod) + 4.0F + 2.0F);
         }

         if (!this.hasInitializedPos) {
            if ((Float)this.posXProp.getValue() == 2.0F && (Float)this.posYProp.getValue() == 2.0F) {
               this.posXProp.setValue((float)sr.getScaledWidth() - maxWidth - 2.0F);
            }

            this.hasInitializedPos = true;
         }

         if (this.isHidden()) {
            this.setHidden(false);
         }

         this.hudWidth = maxWidth;
         this.hudHeight = 0.0F;

         for (Module mod : visibleModules) {
            this.hudHeight += rowHeight * this.moduleFadeMap.get(mod).getValue(currentTime);
         }

         this.hudHeight = Math.max(rowHeight, this.hudHeight);

         if (this.lastVerticalMode >= 0 && (Integer)this.verticalProp.getValue() != this.lastVerticalMode) {
            this.posYProp.setValue((Float)this.posYProp.getValue() + (this.verticalProp.getValue() == 1 ? this.hudHeight : -this.hudHeight));
         }

         this.lastVerticalMode = (Integer)this.verticalProp.getValue();
         float drawX = (Float)this.posXProp.getValue();
         float drawY = this.getY();
         if (visibleCount == 0) {
            if (HudDragManager.isChatGuiOpen()) {
               RenderUtil.drawShadowedPanel(drawX, drawY, maxWidth, rowHeight);
               RenderUtil.drawString("ArrayList", drawX + 2.0F, drawY + (rowHeight - RenderUtil.getFontHeight()) / 2.0F, RenderUtil.getSecondaryTextColor());
            }

            HudDragManager.handleDragging(this);
         } else {
            float[] widths = new float[visibleCount];
            float[] heights = new float[visibleCount];
            Color[] colors = new Color[visibleCount];

            for (int k = 0; k < visibleCount; k++) {
               Module mod = visibleModules.get(k);
               widths[k] = (float)this.getModuleTextWidth(mod) + 4.0F + 2.0F;
               heights[k] = rowHeight * this.moduleFadeMap.get(mod).getValue(currentTime);
               colors[k] = RenderUtil.getGradientColor(k, visibleCount, currentTime);
               if (k > 0) {
                  widths[k] = Math.min(widths[k], widths[k - 1]);
                  widths[k] = Math.max(widths[k], widths[k - 1] - 13.0F);
               }
            }

            boolean isConnectedStyle = (Boolean)this.backgroundProp.getValue() && (Integer)this.horizontalProp.getValue() == 1 && (Integer)this.verticalProp.getValue() == 0;
            if (isConnectedStyle) {
               float rightEdge = drawX + maxWidth;
               BlurPipeline.drawConnectedList(rightEdge, drawY, heights, widths, 4.0F);
               RenderUtil.drawConnectedProfile(rightEdge, drawY, heights, widths, colors);
            }

            float currentY = this.verticalProp.getValue() == 1 ? drawY + this.hudHeight : drawY;

            for (int row = 0; row < visibleCount; row++) {
               Module mod = visibleModules.get(row);
               float fadeProgress = this.moduleFadeMap.get(mod).getValue(currentTime);
               float itemHeight = heights[row];
               float itemY;
               if ((Integer)this.verticalProp.getValue() == 1) {
                  currentY -= itemHeight;
                  itemY = currentY;
               } else {
                  itemY = currentY;
                  currentY += itemHeight;
               }

               Color modColor = colors[row];
               String name = mod.getName();
               String suffix = formatModuleSuffix(mod);
               int totalTextW = this.getModuleTextWidth(mod);
               float nameW = RenderUtil.getStringWidth(name);
               float itemX = this.horizontalProp.getValue() == 1 ? drawX + maxWidth - widths[row] : drawX;
               if ((Boolean)this.backgroundProp.getValue() && !isConnectedStyle) {
                  RenderUtil.drawRoundedBox(itemX, itemY, widths[row], itemHeight, RenderUtil.getDarkerShade(modColor));
               }

               GreenRender.pushScissor(itemX, itemY, widths[row], itemHeight);

               try {
                  float excessW = Math.max(0.0F, widths[row] - (float)totalTextW - 4.0F - 2.0F);
                  float textX = isConnectedStyle
                     ? itemX + 4.0F + excessW / 2.0F + (1.0F - fadeProgress) * 6.0F
                     : (this.horizontalProp.getValue() == 1 ? itemX + widths[row] - 2.0F - (float)totalTextW - (1.0F - fadeProgress) * 6.0F : itemX + 4.0F + (1.0F - fadeProgress) * 6.0F);
                  float textY = itemY + (rowHeight - RenderUtil.getFontHeight()) / 2.0F;
                  Color textColor = new Color(modColor.getRed(), modColor.getGreen(), modColor.getBlue(), Math.round(255.0F * fadeProgress));
                  RenderUtil.drawString(name, textX, textY, textColor, false);
                  if (!suffix.isEmpty()) {
                     Color defColor = RenderUtil.DEFAULT_TEXT_COLOR;
                     RenderUtil.drawString(
                        suffix,
                        textX + nameW,
                        textY,
                        new Color(defColor.getRed(), defColor.getGreen(), defColor.getBlue(), Math.round(255.0F * fadeProgress)),
                        false
                     );
                  }
               } finally {
                  GreenRender.popScissor();
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            HudDragManager.handleDragging(this);
         }
      }
   }

   public ArrayListMod() {
      super("ArrayList", true, false, ModuleCategory.VISUAL);
      this.setHidden(false);
   }

   static {
      EXCLUDED_MODULES.add("arraylist");
      EXCLUDED_MODULES.add("theme");
      EXCLUDED_MODULES.add("hud");
      EXCLUDED_MODULES.add("watermark");
      EXCLUDED_MODULES.add("sessionstats");
      EXCLUDED_MODULES.add("guimodule");
      EXCLUDED_MODULES.add("clickgui");
   }
}
