package valor.ui.impl.clickgui.material;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.ui.impl.clickgui.material.modal.AltManagerModal;
import valor.ui.impl.clickgui.material.modal.CloudConfigsModal;
import valor.ui.impl.clickgui.material.modal.Modal;
import valor.ui.impl.clickgui.material.modal.ThemesModal;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.ValorPaths;
import valor.util.font.Fonts;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;
public class MaterialClickGui extends GuiScreen {
   private static MaterialClickGui instance;
   private final File configFile = new File(ValorPaths.CONFIG_DIR, "clickgui_material.txt");
   private final ArrayList<Frame> frames = new ArrayList<>();
   private Frame draggingFrame;
   private final ConfigsFrame configsFrame;
   private boolean configsOpen;
   private final FloatInterpolator configsVisibility = new FloatInterpolator(0.0F);
   private String searchQuery = "";
   private boolean searchFocused;
   private long lastCursorToggle;
   private boolean cursorVisible = true;
   private Modal activeModal;
   private static final int TOPBTN_SIZE = 42;
   private static final int TOPBTN_GAP = 3;
   private static final int MIN_FRAME_Y = 18;
   private static final int FRAME_GAP = 6;
   private static final long OPEN_ANIM_MS = 200L;
   private static final long CLOSE_ANIM_MS = 150L;
   private final int[] topBtnX = new int[4];
   private final int[] topBtnY = new int[4];
   private final FloatInterpolator[] topHover = new FloatInterpolator[]{
      new FloatInterpolator(0.0F), new FloatInterpolator(0.0F), new FloatInterpolator(0.0F), new FloatInterpolator(0.0F)
   };
   private final FloatInterpolator searchHighlight = new FloatInterpolator(0.0F);
   private final FloatInterpolator guiVisibility = new FloatInterpolator(0.0F);
   private boolean closing;
   private boolean closeDispatched;
   private void applySearch() {
      Iterator var1 = this.frames.iterator();
      while (var1.hasNext()) {
         ((Frame)var1.next()).filter(this.searchQuery);
      }
   }
   private void drawTopButtons(int var1, int var2) {
      this.layoutTopButtons();
      String[] var3 = new String[]{"Theme", "Accounts", "Cloud", "Configs"};
      for (int var4 = 0; var4 < var3.length; var4++) {
         int var5 = this.topBtnX[var4];
         int var6 = this.topBtnY[var4];
         boolean var7 = var1 >= var5 && var1 <= var5 + 42 && var2 >= var6 && var2 <= var6 + 20;
         long var8 = System.currentTimeMillis();
         this.topHover[var4].interpolateTo(var7 ? 1.0F : 0.0F, 120L, var8);
         float var10 = this.topHover[var4].getValue(var8);
         GreenRender.fillRR((float)var5, (float)var6, 42.0F, 20.0F, 5.0F, new Color(23, 24, 41, 150 + Math.round(70.0F * var10)));
         GreenRender.drawStringSmall(
            var3[var4],
            (float)var5 + (42.0F - GreenRender.strWSmall(var3[var4])) / 2.0F,
            (float)var6 + (20.0F - GreenRender.fontHSmall()) / 2.0F,
            GreenRender.lerp(MaterialTheme.TEXT_MUTED, MaterialTheme.TEXT, var10)
         );
      }
   }
   private static void ensureFonts() {
      if (!Fonts.isReady()) {
         Fonts.init();
      }
   }
   private void drawSearchBar(int var1, int var2) {
      float var3 = (float)MaterialTheme.SEARCH_WIDTH;
      float var4 = (float)MaterialTheme.SEARCH_HEIGHT;
      float var5 = ((float)this.width - var3) / 2.0F;
      float var6 = this.searchY();
      boolean var11 = (float)var1 >= var5 && (float)var1 <= var5 + var3 && (float)var2 >= var6 && (float)var2 <= var6 + var4;
      long var7 = System.currentTimeMillis();
      this.searchHighlight.interpolateTo(this.searchFocused ? 1.0F : (var11 ? 0.6F : 0.0F), 130L, var7);
      float var12 = this.searchHighlight.getValue(var7);
      GreenRender.glowRR(var5, var6 + 1.0F, var3, var4, 8.0F, 9.0F, new Color(0, 0, 0, 84));
      BlurPipeline.drawRegionRounded(var5, var6, var3, var4, 8.0F);
      GreenRender.fillRR(var5, var6, var3, var4, 8.0F, MaterialTheme.SEARCH_BG);
      if (var12 > 0.01F) {
         GreenRender.fillRR(var5, var6, var3, var4, 8.0F, MaterialTheme.accent(MaterialTheme.ACCENT_PURPLE, 0.07F * var12));
      }
      String var13;
      Color var14;
      if (this.searchQuery.isEmpty() && !this.searchFocused) {
         var13 = "Search";
         var14 = MaterialTheme.TEXT_MUTED;
      } else {
         var13 = this.searchQuery;
         var14 = MaterialTheme.TEXT;
         if (this.searchFocused) {
            if (var7 - this.lastCursorToggle > 500L) {
               this.cursorVisible = !this.cursorVisible;
               this.lastCursorToggle = var7;
            }
            if (this.cursorVisible) {
               var13 = var13 + "|";
            }
         }
      }
      GreenRender.pushScissor(var5 + 12.0F, var6, var3 - 24.0F, var4);
      try {
         while (GreenRender.strW(var13) > var3 - 24.0F && var13.length() > 1) {
            var13 = var13.substring(1);
         }
         GreenRender.drawString(var13, var5 + 12.0F, var6 + (var4 - GreenRender.fontH()) / 2.0F, var14);
      } finally {
         GreenRender.popScissor();
      }
   }
   private MaterialClickGui.TopAction hitTopButton(int var1, int var2) {
      this.layoutTopButtons();
      for (int var3 = 0; var3 < 4; var3++) {
         if (var1 >= this.topBtnX[var3] && var1 <= this.topBtnX[var3] + 42 && var2 >= this.topBtnY[var3] && var2 <= this.topBtnY[var3] + 20) {
            return MaterialClickGui.TopAction.values()[var3];
         }
      }
      return null;
   }
   private boolean isOverSearch(int var1, int var2) {
      float var3 = (float)MaterialTheme.SEARCH_WIDTH;
      float var4 = (float)MaterialTheme.SEARCH_HEIGHT;
      float var5 = ((float)this.width - var3) / 2.0F;
      float var6 = this.searchY();
      return (float)var1 >= var5 && (float)var1 <= var5 + var3 && (float)var2 >= var6 && (float)var2 <= var6 + var4;
   }
   public static void resetInstance() {
      instance = null;
   }
   private void layoutTopButtons() {
      int var1 = this.width - 177 - 12;
      int var2 = this.width < 660 ? this.height - 58 : this.height - 33;
      for (int var3 = 0; var3 < 4; var3++) {
         this.topBtnX[var3] = var1 + var3 * 45;
         this.topBtnY[var3] = var2;
      }
   }
   private void openModal(MaterialClickGui.TopAction var1) {
      if (var1 == MaterialClickGui.TopAction.CONFIGS) {
         this.configsFrame.refresh();
         this.configsFrame.setExpanded(true);
         this.configsOpen = true;
      } else {
         switch (var1) {
            case ALTS:
               this.activeModal = new AltManagerModal();
               break;
            case CLOUD:
               this.activeModal = new CloudConfigsModal();
               break;
            default:
               this.activeModal = new ThemesModal();
         }
         this.activeModal.layout(this.width, this.height);
      }
   }
   private void requestClose() {
      if (!this.closing) {
         this.closing = true;
         this.guiVisibility.interpolateTo(0.0F, 150L, System.currentTimeMillis());
         this.searchFocused = false;
         this.draggingFrame = null;
         this.activeModal = null;
      }
   }
   private float searchY() {
      return (float)(this.height - MaterialTheme.SEARCH_HEIGHT - 12);
   }
   private float uiAlpha() {
      long var1 = System.currentTimeMillis();
      if (this.closing && this.guiVisibility.isFinished(var1)) {
         if (!this.closeDispatched) {
            this.closeDispatched = true;
            this.mc.displayGuiScreen(null);
         }
         return 0.0F;
      } else {
         return this.guiVisibility.getValue(var1);
      }
   }
   private void loadPositions() {
      if (this.configFile.exists()) {
         try {
            FileReader var1 = new FileReader(this.configFile);
            Throwable var2 = null;
            boolean var11 = false ;
            try {
               var11 = true;
               JsonObject var3 = new JsonParser().parse(var1).getAsJsonObject();
               Iterator var4 = this.frames.iterator();
               while (var4.hasNext()) {
                  Frame var5;
                  String var6 = (var5 = (Frame)var4.next()).getCategoryName();
                  if (!var3.has(var6) && "World".equals(var6) && var3.has("Other")) {
                     var6 = "Other";
                  }
                  if (var3.has(var6)) {
                     JsonObject var16 = var3.getAsJsonObject(var6);
                     var5.setPos(var16.get("x").getAsInt(), var16.get("y").getAsInt());
                     if (var16.has("open")) {
                        var5.setExpanded(var16.get("open").getAsBoolean());
                     }
                  }
               }
               var11 = false;
            } catch (Throwable var13) {
               var2 = var13;
               throw var13;
            } finally {
               if (var11) {
                  if (var2 != null) {
                     try {
                        var1.close();
                     } catch (Throwable var12) {
                        var2.addSuppressed(var12);
                     }
                  } else {
                     var1.close();
                  }
               }
            }
            var1.close();
         } catch (Exception var15) {
            var15.printStackTrace();
         }
      }
   }
   private void savePositions() {
      File var1;
      if ((var1 = this.configFile.getParentFile()) != null && !var1.exists()) {
         var1.mkdirs();
      }
      JsonObject var14 = new JsonObject();
      for (Frame var3 : this.frames) {
         JsonObject var4;
         (var4 = new JsonObject()).addProperty("x", var3.getX());
         var4.addProperty("y", var3.getY());
         var4.addProperty("open", var3.isExpanded());
         var14.add(var3.getCategoryName(), var4);
      }
      try {
         FileWriter var15 = new FileWriter(this.configFile);
         Throwable var16 = null;
         boolean var9 = false ;
         try {
            var9 = true;
            new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)var14, var15);
            var9 = false;
         } catch (Throwable var11) {
            var16 = var11;
            throw var11;
         } finally {
            if (var9) {
               if (var16 != null) {
                  try {
                     var15.close();
                  } catch (Throwable var10) {
                     var16.addSuppressed(var10);
                  }
               } else {
                  var15.close();
               }
            }
         }
         var15.close();
      } catch (IOException var13) {
         var13.printStackTrace();
      }
   }
   @Override
   public void onGuiClosed() {
      this.savePositions();
      this.searchFocused = false;
   }
   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }
   @Override
   public void drawScreen(int var1, int var2, float var3) {
      try {
         ensureFonts();
         MaterialTheme.refresh();
         float var4;
         if (!((var4 = this.uiAlpha()) <= 0.0F) || !this.closing) {
            BlurPipeline.beginFrame();
            BlurPipeline.prepare(3.0F);
            BlurPipeline.drawRegionRounded(0.0F, 0.0F, (float)this.width, (float)this.height, 0.0F);
            Color var5 = MaterialTheme.accent(MaterialTheme.OVERLAY, (float)MaterialTheme.OVERLAY.getAlpha() / 255.0F * var4);
            Color var6 = MaterialTheme.accent(MaterialTheme.OVERLAY_TINT, (float)MaterialTheme.OVERLAY_TINT.getAlpha() / 255.0F * var4);
            GreenRender.fillRect(0.0F, 0.0F, (float)this.width, (float)this.height, var5);
            GreenRender.fillRect(0.0F, 0.0F, (float)this.width, (float)this.height, var6);
            GreenRender.pushScissor(0.0F, 0.0F, (float)this.width, (float)this.height * var4);
            var4 = (1.0F - var4) * (this.closing ? -6.0F : 6.0F);
            GlStateManager.pushMatrix();
            try {
               GL11.glTranslatef(0.0F, var4, 0.0F);
               if (this.draggingFrame != null) {
                  this.draggingFrame.updatePosition(var1, var2);
               }
               Iterator var19 = this.frames.iterator();
               while (var19.hasNext()) {
                  ((Frame)var19.next())
                     .render(this.activeModal == null && !this.configsOpen ? var1 : -1, this.activeModal == null && !this.configsOpen ? var2 : -1, var3, 0);
               }
               this.drawSearchBar(var1, var2);
               this.drawTopButtons(var1, var2);
            } finally {
               GlStateManager.popMatrix();
               GreenRender.popScissor();
            }
            int var20;
            if ((var20 = this.closing ? 0 : Mouse.getDWheel()) != 0) {
               int var7 = var20 > 0 ? 1 : -1;
               if (this.activeModal != null) {
                  this.activeModal.onScroll(var1, var2, var7);
               } else if (this.configsOpen) {
                  this.configsFrame.onScroll(var1, var2, var7);
               } else {
                  Iterator var8 = this.frames.iterator();
                  while (var8.hasNext()) {
                     ((Frame)var8.next()).onScroll(var1, var2, var7);
                  }
               }
            }
            long var22 = System.currentTimeMillis();
            this.configsVisibility.interpolateTo(this.configsOpen ? 1.0F : 0.0F, this.configsOpen ? 190L : 130L, var22);
            if ((var4 = this.configsVisibility.getValue(var22)) > 0.001F) {
               this.configsFrame.setBodyLimit(Math.max(16, this.height - 120));
               this.configsFrame.setPos((this.width - 180) / 2, Math.max(18, (this.height - (int)this.configsFrame.getCurrentHeight()) / 2));
               GreenRender.fillRect(0.0F, 0.0F, (float)this.width, (float)this.height, MaterialTheme.accent(MaterialTheme.MODAL_OVERLAY, 0.5F * var4));
               GlScaleUtil.beginScale((float)this.configsFrame.getX(), (float)this.configsFrame.getY(), 180.0F, this.configsFrame.getCurrentHeight(), var4);
               try {
                  this.configsFrame.render(var4 < 0.999F ? -1 : var1, var4 < 0.999F ? -1 : var2, var3, 0);
               } finally {
                  GlScaleUtil.endScale();
               }
            }
            if (this.activeModal != null) {
               this.activeModal.layout(this.width, this.height);
               this.activeModal.render(var1, var2, var3);
               if (this.activeModal.isClosed()) {
                  this.activeModal = null;
               }
            }
            super.drawScreen(var1, var2, var3);
         }
      } catch (Throwable var17) {
         System.out.println("[Valor] drawScreen ERROR: ".concat(String.valueOf(var17)));
         var17.printStackTrace(System.out);
      }
   }
   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      if (!this.closing) {
         if (this.configsOpen) {
            if (var2 == 1) {
               this.configsOpen = false;
            } else {
               this.configsFrame.keyTyped(var1, var2);
            }
         } else if (this.activeModal != null) {
            if (var2 == 1) {
               this.activeModal.close();
            } else {
               this.activeModal.keyTyped(var1, var2);
            }
         } else {
            boolean var3 = false;
            Iterator var4 = this.frames.iterator();
            while (var4.hasNext()) {
               if (((Frame)var4.next()).isAnyBinding()) {
                  var3 = true;
                  break;
               }
            }
            if (var3) {
               var4 = this.frames.iterator();
               while (var4.hasNext()) {
                  ((Frame)var4.next()).keyTyped(var1, var2);
               }
            } else if (var2 == 1) {
               this.requestClose();
            } else if (this.searchFocused) {
               if (var2 == 14) {
                  if (!this.searchQuery.isEmpty()) {
                     this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                     this.applySearch();
                  }
               } else if (var2 == 28) {
                  this.searchFocused = false;
               } else if (var1 >= ' ' && var1 != 127 && this.searchQuery.length() < 128) {
                  this.searchQuery = this.searchQuery + var1;
                  this.applySearch();
               }
            } else {
               var4 = this.frames.iterator();
               while (var4.hasNext()) {
                  ((Frame)var4.next()).keyTyped(var1, var2);
               }
            }
         }
      }
   }
   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      if (this.activeModal != null) {
         this.activeModal.mouseReleased(var1, var2, var3);
      } else {
         if (this.draggingFrame != null) {
            this.draggingFrame.mouseReleased(var1, var2, var3);
            this.draggingFrame = null;
         }
         Iterator var4 = this.frames.iterator();
         while (var4.hasNext()) {
            ((Frame)var4.next()).mouseReleased(var1, var2, var3);
         }
         super.mouseReleased(var1, var2, var3);
      }
   }
   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (!this.closing) {
         if (this.configsOpen || !(this.configsVisibility.getValue(System.currentTimeMillis()) > 0.001F)) {
            if (this.configsOpen) {
               if (!(this.configsVisibility.getValue(System.currentTimeMillis()) < 0.999F)) {
                  if (!this.configsFrame.isMouseOverHeader(var1, var2) && !this.configsFrame.isMouseOverBody(var1, var2)) {
                     this.configsOpen = false;
                  } else {
                     if (!this.configsFrame.isMouseOverHeader(var1, var2)) {
                        this.configsFrame.mouseClicked(var1, var2, var3);
                     }
                  }
               }
            } else if (this.activeModal != null) {
               this.activeModal.mouseClicked(var1, var2, var3);
            } else {
               MaterialClickGui.TopAction var4;
               if (var3 == 0 && (var4 = this.hitTopButton(var1, var2)) != null) {
                  this.openModal(var4);
               } else if (this.isOverSearch(var1, var2) && var3 == 0) {
                  this.searchFocused = true;
               } else {
                  this.searchFocused = false;
                  for (int var6 = this.frames.size() - 1; var6 >= 0; var6--) {
                     Frame var5;
                     if ((var5 = this.frames.get(var6)).mouseClicked(var1, var2, var3)) {
                        if (var3 == 0 && var5.isMouseOverHeader(var1, var2)) {
                           this.draggingFrame = var5;
                        }
                        this.frames.remove(var6);
                        this.frames.add(var5);
                        return;
                     }
                  }
                  super.mouseClicked(var1, var2, var3);
               }
            }
         }
      }
   }
   @Override
   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
      if (this.draggingFrame != null) {
         this.draggingFrame.updatePosition(var1, var2);
      }
      super.mouseClickMove(var1, var2, var3, var4);
   }
   @Override
   public void initGui() {
      super.initGui();
      ensureFonts();
      this.searchFocused = false;
      this.configsOpen = false;
      this.closing = false;
      this.closeDispatched = false;
      this.guiVisibility.reset(0.0F);
      this.guiVisibility.interpolateTo(1.0F, 200L, System.currentTimeMillis());
      this.layoutFramesCentered();
   }
   private void layoutFramesCentered() {
      if (!this.frames.isEmpty() && this.width > 0) {
         int var1 = MaterialTheme.FRAME_WIDTH;
         int var2 = Math.max(1, Math.min(this.frames.size(), (this.width - 16 + 6) / (var1 + 6)));
         int var3 = (this.frames.size() + var2 - 1) / var2;
         var3 = Math.max(MaterialTheme.HEADER_HEIGHT + MaterialTheme.MODULE_HEIGHT, (this.height - 18 - 65) / var3);
         int var4 = Math.max(8, (this.width - var2 * var1 - (var2 - 1) * 6) / 2);
         for (int var5 = 0; var5 < this.frames.size(); var5++) {
            Frame var6;
            (var6 = this.frames.get(var5)).setPos(var4 + var5 % var2 * (var1 + 6), 18 + var5 / var2 * var3);
            var6.setBodyLimit(Math.min(380, var3 - MaterialTheme.HEADER_HEIGHT - 10));
         }
      }
   }
   public static MaterialClickGui openFresh() {
      return instance = new MaterialClickGui();
   }
   public MaterialClickGui() {
      instance = this;
      ensureFonts();
      MaterialTheme.applySize();
      EnumMap var1 = new EnumMap<>(ModuleCategory.class);
      ModuleCategory[] var2;
      for (ModuleCategory var5 : var2 = ModuleCategory.values()) {
         var1.put(var5, new ArrayList());
      }
      Iterator var11 = Valor.moduleManager.modules.values().iterator();
      while (var11.hasNext()) {
         Module var14;
         if (!(var14 = (Module)var11.next()).isHidden()) {
            ((List)var1.get(var14.getCategory())).add(var14);
         }
      }
      Comparator<Module> var12 = Comparator.comparing((Module var0) -> var0.getName().toLowerCase(Locale.ROOT));
      Iterator var15 = var1.values().iterator();
      while (var15.hasNext()) {
         ((List)var15.next()).sort(var12);
      }
      int var16 = MaterialTheme.FRAME_WIDTH;
      int var17 = MaterialTheme.HEADER_HEIGHT;
      int var18 = 20;
      for (ModuleCategory var8 : var2 = ModuleCategory.values()) {
         List var9;
         if (!(var9 = (List)var1.get(var8)).isEmpty()) {
            Frame.IconType var10 = Frame.IconType.valueOf(var8.name());
            this.frames.add(new Frame(var8.displayName, var10, var9, var18, 18, var16, var17));
            var18 += var16 + 6;
         }
      }
      this.configsFrame = new ConfigsFrame(0, 0, 180, var17);
      this.loadPositions();
   }
   public static MaterialClickGui getInstance() {
      if (instance == null) {
         instance = new MaterialClickGui();
      }
      return instance;
   }
   static enum TopAction {
      THEMES,
      ALTS,
      CLOUD,
      CONFIGS;
   }
}
