package valor.ui.impl.clickgui.material;

import java.awt.Color;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import valor.Valor;
import valor.core.ConfigManager;
import valor.util.ChatUtil;
import valor.util.render.GreenRender;

public class ConfigsFrame extends Frame {
   private static final String ACTION_SAVE = "Save";
   private static final String ACTION_NEW = "New";
   private static final String ACTION_FOLDER = "Open folder";
   private final List<String> allNames = new ArrayList<>();
   private final List<String> visibleNames = new ArrayList<>();
   private String filterQuery = "";
   private boolean creating;
   private String createBuffer = "";
   private long lastCursorToggle;
   private boolean cursorVisible = true;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      var3 = this.getCurrentHeight();
      GreenRender.glowRR((float)super.x, (float)(super.y + 2), (float)super.width, var3, 7.0F, 7.0F, new Color(8, 9, 22, 62));
      MaterialTheme.drawGlass((float)super.x, (float)super.y, (float)super.width, var3, 7.0F);
      GreenRender.fillRRCorners(
         (float)super.x,
         (float)super.y,
         (float)super.width,
         (float)MaterialTheme.HEADER_HEIGHT,
         7.0F,
         7.0F,
         super.expanded ? 0.0F : 7.0F,
         super.expanded ? 0.0F : 7.0F,
         MaterialTheme.CATEGORY_HEADER_BG
      );
      this.drawHeader();
      if (!(var3 <= (float)MaterialTheme.HEADER_HEIGHT + 0.5F)) {
         var4 = this.getContentHeight();
         var3 -= (float)MaterialTheme.HEADER_HEIGHT;
         var4 = Math.max(0, var4 - super.bodyLimit);
         if (super.scroll > var4) {
            super.scroll = var4;
         }

         if (super.scroll < 0) {
            super.scroll = 0;
         }

         GreenRender.pushScissor((float)super.x, (float)(super.y + MaterialTheme.HEADER_HEIGHT), (float)super.width, Math.max(0.0F, var3));

         try {
            int var5 = super.y + MaterialTheme.HEADER_HEIGHT - super.scroll;
            this.drawRow("Save", var5, var1, var2, false, true);
            var5 += MaterialTheme.MODULE_HEIGHT;
            String var6;
            if (this.creating) {
               long var8;
               if ((var8 = System.currentTimeMillis()) - this.lastCursorToggle > 500L) {
                  this.cursorVisible = !this.cursorVisible;
                  this.lastCursorToggle = var8;
               }

               var6 = this.createBuffer.isEmpty() ? "Name" : this.createBuffer;
               if (this.cursorVisible) {
                  var6 = var6 + "|";
               }
            } else {
               var6 = "New";
            }

            this.drawRow(var6, var5, var1, var2, this.creating, true);
            var5 += MaterialTheme.MODULE_HEIGHT;
            this.drawRow("Open folder", var5, var1, var2, false, true);
            var5 += MaterialTheme.MODULE_HEIGHT;
            String var20 = ConfigManager.currentConfig;

            for (String var19 : this.visibleNames) {
               boolean var7 = var20 != null && var20.equalsIgnoreCase(var19);
               this.drawRow(var19, var5, var1, var2, var7, false);
               var5 += MaterialTheme.MODULE_HEIGHT;
            }
         } finally {
            GreenRender.popScissor();
         }

         MaterialTheme.drawOverflowFade((float)super.x, (float)(super.y + MaterialTheme.HEADER_HEIGHT) + var3, (float)super.width, var4 - super.scroll);
      }
   }

   private void openFolder() {
      try {
         if (!ConfigManager.CONFIG_DIR.exists()) {
            ConfigManager.CONFIG_DIR.mkdirs();
         }

         Desktop.getDesktop().open(ConfigManager.CONFIG_DIR);
      } catch (Exception var1) {
         ChatUtil.addFormattedChatMessage(String.format("%sFailed to open (&o%s&r)&r", Valor.CHAT_PREFIX, ConfigManager.CONFIG_DIR.getPath()));
      }
   }

   @Override
   public float getCurrentHeight() {
      return (float)MaterialTheme.HEADER_HEIGHT + this.visibleBodyHeight(Math.min(this.getContentHeight(), super.bodyLimit));
   }

   private int actionRows() {
      return 3;
   }

   private void cancelCreate() {
      this.creating = false;
      this.createBuffer = "";
   }

   private void applyFilter() {
      this.visibleNames.clear();
      String var1 = this.filterQuery == null ? "" : this.filterQuery.trim().toLowerCase(Locale.ROOT);

      for (String var3 : this.allNames) {
         if (var1.isEmpty() || var3.toLowerCase(Locale.ROOT).contains(var1)) {
            this.visibleNames.add(var3);
         }
      }

      super.scroll = 0;
   }

   private void commitCreate() {
      String var1 = this.createBuffer.trim();
      this.cancelCreate();
      if (!var1.isEmpty()) {
         new ConfigManager(var1, true).save();
         this.refresh();
      }
   }

   private void drawRow(String var1, int var2, int var3, int var4, boolean var5, boolean var6) {
      boolean var9 = var3 >= super.x
         && var3 <= super.x + super.width
         && var4 >= var2
         && var4 < var2 + MaterialTheme.MODULE_HEIGHT
         && var4 >= super.y + MaterialTheme.HEADER_HEIGHT
         && (float)var4 <= (float)super.y + this.getCurrentHeight();
      if (var5 && !var6) {
         Color var12 = MaterialTheme.accent(MaterialTheme.ACCENT_PURPLE, 0.15F);
         GreenRender.fillRR((float)super.x, (float)var2, (float)super.width, (float)MaterialTheme.MODULE_HEIGHT, 0.0F, var12);
      } else if (var9 || var6 && var5) {
         Color var10 = MaterialTheme.ROW_HOVER;
         GreenRender.fillRR((float)super.x, (float)var2, (float)super.width, (float)MaterialTheme.MODULE_HEIGHT, 0.0F, var10);
      }

      Color var11;
      if (var5 && !var6) {
         var11 = Color.WHITE;
      } else if (var6) {
         if (!this.creating || !var5) {
            var11 = MaterialTheme.isLight()
               ? MaterialTheme.darken(MaterialTheme.ACCENT_PURPLE, 0.22F)
               : MaterialTheme.lighten(MaterialTheme.ACCENT_PURPLE, 0.25F);
         } else {
            var11 = MaterialTheme.TEXT;
         }
      } else {
         var11 = MaterialTheme.isLight() ? MaterialTheme.TEXT_MUTED : MaterialTheme.TEXT_DISABLED;
      }

      float var13 = (float)var2 + ((float)MaterialTheme.MODULE_HEIGHT - GreenRender.fontHSmall()) / 2.0F + 0.5F;
      float var7 = GreenRender.strWSmall(var1);
      float var8 = var6 && this.creating && var5 ? (float)(super.x + 8) : (float)super.x + Math.max(8.0F, ((float)super.width - var7) / 2.0F);
      GreenRender.drawStringSmall(var1, var8, var13, var11);
   }

   private static boolean isAllowedNameChar(char var0) {
      return Character.isLetterOrDigit(var0) || var0 == '-' || var0 == '_' || var0 == '.';
   }

   @Override
   public boolean isAnyBinding() {
      return this.creating;
   }

   @Override
   public void onScroll(int var1, int var2, int var3) {
      if (this.isMouseOverBody(var1, var2)) {
         if ((var1 = this.getContentHeight()) > super.bodyLimit) {
            super.scroll -= var3 * 14;
            var1 = Math.max(0, var1 - super.bodyLimit);
            super.scroll = Math.max(0, Math.min(super.scroll, var1));
         }
      }
   }

   private int getContentHeight() {
      return (this.actionRows() + this.visibleNames.size()) * MaterialTheme.MODULE_HEIGHT;
   }

   @Override
   public void keyTyped(char var1, int var2) {
      if (this.creating) {
         if (var2 == 1) {
            this.cancelCreate();
         } else if (var2 == 28 || var2 == 156) {
            this.commitCreate();
         } else if (var2 == 14) {
            if (!this.createBuffer.isEmpty()) {
               this.createBuffer = this.createBuffer.substring(0, this.createBuffer.length() - 1);
            }
         } else {
            if (var1 >= ' ' && var1 != 127 && isAllowedNameChar(var1) && this.createBuffer.length() < 32) {
               this.createBuffer = this.createBuffer + var1;
            }
         }
      }
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      super.dragging = false;
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (this.isMouseOverHeader(var1, var2)) {
         if (var3 == 0) {
            super.dragging = true;
            super.dragX = var1 - super.x;
            super.dragY = var2 - super.y;
            return true;
         }

         if (var3 == 1) {
            super.expanded = !super.expanded;
            return true;
         }
      }

      if (super.expanded && this.isMouseOverBody(var1, var2)) {
         var1 = Math.min(this.getContentHeight(), super.bodyLimit);
         if ((var2 = var2 - (super.y + MaterialTheme.HEADER_HEIGHT) + super.scroll) >= 0 && var2 < var1 + super.scroll) {
            var1 = var2 / MaterialTheme.MODULE_HEIGHT;
            var2 = this.actionRows() + this.visibleNames.size();
            if (var1 < 0 || var1 >= var2) {
               return true;
            } else if (var1 == 0) {
               if (var3 == 0) {
                  this.cancelCreate();
                  new ConfigManager(ConfigManager.sanitizeConfigName(ConfigManager.currentConfig), true).save();
                  this.refresh();
               }

               return true;
            } else if (var1 == 1) {
               if (var3 == 0) {
                  this.creating = true;
                  this.createBuffer = "";
                  this.lastCursorToggle = System.currentTimeMillis();
                  this.cursorVisible = true;
               }

               return true;
            } else if (var1 == 2) {
               if (var3 == 0) {
                  this.cancelCreate();
                  this.openFolder();
               }

               return true;
            } else {
               String var6 = this.visibleNames.get(var1 - this.actionRows());
               if (var3 == 0) {
                  this.cancelCreate();
                  new ConfigManager(var6, false).load();
                  this.refresh();
               } else if (var3 == 1) {
                  this.cancelCreate();
                  ConfigManager.deleteConfig(var6);
                  this.refresh();
               }

               return true;
            }
         } else {
            return true;
         }
      } else {
         if (this.creating && var3 == 0) {
            this.cancelCreate();
         }

         return false;
      }
   }

   public void refresh() {
      this.allNames.clear();
      this.allNames.addAll(ConfigManager.getConfigList());
      this.applyFilter();
   }

   public ConfigsFrame(int var1, int var2, int var3, int var4) {
      super("Configs", Frame.IconType.CONFIGS, var1, var2, var3, var4);
      this.refresh();
   }

   @Override
   public void filter(String var1) {
      this.filterQuery = var1 == null ? "" : var1;
      this.applyFilter();
   }
}
