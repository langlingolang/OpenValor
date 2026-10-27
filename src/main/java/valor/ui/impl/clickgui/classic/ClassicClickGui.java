package valor.ui.impl.clickgui.classic;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.render.ClickGuiMod;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.setting.Property;
import valor.setting.TextProperty;
import valor.ui.callback.GuiInput;
import valor.ui.dataset.Slider;
import valor.ui.dataset.impl.FloatSlider;
import valor.ui.dataset.impl.IntSlider;
import valor.ui.dataset.impl.PercentageSlider;
import valor.util.Render3DUtil;
import valor.util.ValorPaths;
import valor.util.font.Fonts;
import valor.util.font.TTFontRenderer;
public class ClassicClickGui extends GuiScreen {
   private static ClassicClickGui instance;
   private final File configFile = new File(ValorPaths.CONFIG_DIR, "clickgui_classic.txt");
   private final ArrayList<ClassicClickGui.CategoryComponent> categoryList;
   private static TTFontRenderer font() {
      if (!Fonts.isReady()) {
         Fonts.init();
      }
      return Fonts.get().comfortaaSmall;
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
               for (ClassicClickGui.CategoryComponent var5 : this.categoryList) {
                  if (var3.has(var5.getName())) {
                     JsonObject var6 = var3.getAsJsonObject(var5.getName());
                     var5.setX(var6.get("x").getAsInt());
                     var5.setY(var6.get("y").getAsInt());
                     var5.setOpened(var6.get("open").getAsBoolean());
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
         } catch (IOException var15) {
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
      for (ClassicClickGui.CategoryComponent var3 : this.categoryList) {
         JsonObject var4;
         (var4 = new JsonObject()).addProperty("x", var3.getX());
         var4.addProperty("y", var3.getY());
         var4.addProperty("open", var3.isOpened());
         var14.add(var3.getName(), var4);
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
   }
   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }
   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      Iterator var5 = this.categoryList.iterator();
      while (var5.hasNext()) {
         ClassicClickGui.CategoryComponent var4;
         (var4 = (ClassicClickGui.CategoryComponent)var5.next()).render(this.fontRendererObj);
         var4.handleDrag(var1, var2);
         Iterator var8 = var4.getModules().iterator();
         while (var8.hasNext()) {
            ((ClassicClickGui.Component)var8.next()).update(var1, var2);
         }
      }
      int var6 = new Color(60, 162, 253).getRGB();
      HudThemeManager var9;
      if ((var9 = HudThemeManager.getInstance()) != null) {
         var6 = var9.getColor(System.currentTimeMillis(), 0L).getRGB();
      }
      font().drawString("Valor", 4.0F, (float)(this.height - 3 - (int)font().getHeight()), var6);
      int var10;
      if ((var10 = Mouse.getDWheel()) != 0) {
         int var11 = var10 > 0 ? 1 : -1;
         Iterator var7 = this.categoryList.iterator();
         while (var7.hasNext()) {
            ((ClassicClickGui.CategoryComponent)var7.next()).onScroll(var1, var2, var11);
         }
      }
   }
   @Override
   public void keyTyped(char var1, int var2) {
      if (var2 == 1) {
         this.mc.displayGuiScreen(null);
      } else {
         Iterator var3 = this.categoryList.iterator();
         while (var3.hasNext()) {
            ClassicClickGui.CategoryComponent var4;
            if ((var4 = (ClassicClickGui.CategoryComponent)var3.next()).isOpened() && !var4.getModules().isEmpty()) {
               Iterator var5 = var4.getModules().iterator();
               while (var5.hasNext()) {
                  ((ClassicClickGui.Component)var5.next()).keyTyped(var1, var2);
               }
            }
         }
      }
   }
   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      if (var3 == 0) {
         Iterator var4 = this.categoryList.iterator();
         while (var4.hasNext()) {
            ((ClassicClickGui.CategoryComponent)var4.next()).mousePressed(false);
         }
         var4 = this.categoryList.iterator();
         while (var4.hasNext()) {
            ClassicClickGui.CategoryComponent var5;
            if ((var5 = (ClassicClickGui.CategoryComponent)var4.next()).isOpened() && !var5.getModules().isEmpty()) {
               Iterator var7 = var5.getModules().iterator();
               while (var7.hasNext()) {
                  ((ClassicClickGui.Component)var7.next()).mouseReleased(var1, var2, var3);
               }
            }
         }
      }
   }
   @Override
   public void mouseClicked(int var1, int var2, int var3) {
      Iterator var4 = this.categoryList.iterator();
      while (var4.hasNext()) {
         ClassicClickGui.CategoryComponent var5;
         if ((var5 = (ClassicClickGui.CategoryComponent)var4.next()).insideArea(var1, var2) && !var5.isHovered(var1, var2) && var3 == 0) {
            var5.mousePressed(true);
            var5.dragOffsetX = var1 - var5.getX();
            var5.dragOffsetY = var2 - var5.getY();
         }
         if (var5.isHovered(var1, var2) && var3 == 0) {
            var5.setOpened(!var5.isOpened());
         }
         if (var5.isOpened() && !var5.getModules().isEmpty()) {
            Iterator var6 = var5.getModules().iterator();
            while (var6.hasNext()) {
               ((ClassicClickGui.Component)var6.next()).mouseDown(var1, var2, var3);
            }
         }
      }
   }
   @Override
   public void initGui() {
      super.initGui();
   }
   public ClassicClickGui() {
      instance = this;
      EnumMap var1 = new EnumMap<>(ModuleCategory.class);
      ModuleCategory[] var2;
      for (ModuleCategory var5 : var2 = ModuleCategory.values()) {
         var1.put(var5, new ArrayList());
      }
      for (Module var11 : Valor.moduleManager.modules.values()) {
         ((List)var1.get(var11.getCategory())).add(var11);
      }
      Comparator<Module> var9 = Comparator.comparing((Module var0) -> var0.getName().toLowerCase());
      Iterator var12 = var1.values().iterator();
      while (var12.hasNext()) {
         ((List)var12.next()).sort(var9);
      }
      this.categoryList = new ArrayList<>();
      byte var13 = 105;
      ModuleCategory[] var15;
      for (ModuleCategory var6 : var15 = ModuleCategory.values()) {
         List var7 = (List)var1.get(var6);
         ClassicClickGui.CategoryComponent var16;
         (var16 = new ClassicClickGui.CategoryComponent(var6.displayName.toLowerCase(Locale.ROOT), var7)).setX(var13);
         var16.setY(25);
         this.categoryList.add(var16);
         var13 += 105;
      }
      this.loadPositions();
   }
   public static ClassicClickGui getInstance() {
      if (instance == null) {
         instance = new ClassicClickGui();
      }
      return instance;
   }
   class BindComponent implements ClassicClickGui.Component {
      private boolean isBinding;
      private final ClassicClickGui.ModuleComponent parentModule;
      private int offsetY;
      private int x;
      private int y;
      @Override
      public int getHeight() {
         return 12;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isHovered(var1, var2) && var3 == 0 && this.parentModule.panelExpand) {
            this.isBinding = !this.isBinding;
         }
      }
      @Override
      public boolean isVisible() {
         return true;
      }
      private void renderText(String var1, int var2) {
         Minecraft.getMinecraft()
            .fontRendererObj
            .drawStringWithShadow(
               var1, (float)(this.parentModule.category.getX() + 4 << 1), (float)(this.parentModule.category.getY() + this.offsetY + 3 << 1), var2
            );
      }
      @Override
      public void draw(AtomicInteger var1) {
         GL11.glPushMatrix();
         GL11.glScaled(0.5, 0.5, 0.5);
         this.renderText(
            this.isBinding ? "Press a key..." : "Bind: " + Keyboard.getKeyName(this.parentModule.mod.getKey()),
            HudThemeManager.getInstance().getColor(System.currentTimeMillis(), (long)var1.get()).getRGB()
         );
         GL11.glPopMatrix();
      }
      public boolean isHovered(int var1, int var2) {
         return var1 > this.x && var1 < this.x + this.parentModule.category.getWidth() && var2 > this.y - 1 && var2 < this.y + 12;
      }
      @Override
      public void keyTyped(char var1, int var2) {
         if (this.isBinding) {
            if (var2 == 11) {
               if (this.parentModule.mod instanceof ClickGuiMod) {
                  this.parentModule.mod.setKey(210);
               } else {
                  this.parentModule.mod.setKey(0);
               }
            } else {
               this.parentModule.mod.setKey(var2);
            }
            this.isBinding = false;
         }
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public BindComponent(ClassicClickGui.ModuleComponent var2, int var3) {
         this.parentModule = var2;
         this.x = var2.category.getX() + var2.category.getWidth();
         this.y = var2.category.getY() + var2.offsetY;
         this.offsetY = var3;
      }
      @Override
      public void update(int var1, int var2) {
         this.y = this.parentModule.category.getY() + this.offsetY;
         this.x = this.parentModule.category.getX();
      }
   }
   class CategoryComponent {
      private final int MAX_HEIGHT = 320;
      public ArrayList<ClassicClickGui.Component> modulesInCategory = new ArrayList<>();
      public String categoryName;
      private boolean categoryOpened;
      private int width;
      private int y;
      private int x;
      private final int barHeight;
      public boolean dragging;
      public int dragOffsetX;
      public int dragOffsetY;
      public boolean pin = false;
      private int scroll = 0;
      private double animScroll = 0.0;
      private int height = 0;
      private float openAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      public ArrayList<ClassicClickGui.Component> getModules() {
         return this.modulesInCategory;
      }
      public void render(FontRenderer var1) {
         this.width = 95;
         this.update();
         long var2;
         float var9 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         float var16 = this.categoryOpened ? 1.0F : 0.0F;
         this.openAnimation = this.openAnimation + (var16 - this.openAnimation) * Math.min(var9 * 8.0F, 1.0F);
         this.height = 0;
         for (ClassicClickGui.Component var17 : this.modulesInCategory) {
            this.height = this.height + var17.getHeight();
         }
         int var11 = Math.max(0, this.height - 320);
         if (this.scroll > var11) {
            this.scroll = var11;
         }
         if (this.animScroll > (double)var11) {
            this.animScroll = (double)var11;
         }
         this.animScroll = this.animScroll + ((double)this.scroll - this.animScroll) * 0.2;
         if (!this.modulesInCategory.isEmpty() && this.openAnimation > 0.01F) {
            int var12 = (int)((float)Math.min(this.height, 320) * this.openAnimation);
            Gui.drawRect(this.x, this.y + this.barHeight, this.x + this.width, this.y + this.barHeight + var12, new Color(20, 20, 25, 250).getRGB());
         }
         Render3DUtil.drawGradientRect(
            this.x, this.y, (float)(this.x + this.width), this.y + this.barHeight, new Color(30, 30, 35, 255).getRGB(), new Color(25, 25, 30, 255).getRGB()
         );
         GlStateManager.pushMatrix();
         String var18 = this.categoryName.substring(0, 1).toUpperCase() + this.categoryName.substring(1);
         float var13 = (float)(this.x + 4);
         ClassicClickGui.font().drawString(var18, var13, (float)(this.y + 5), new Color(200, 200, 200).getRGB());
         String var14 = "X";
         float var19 = (float)(this.x + this.width) - ClassicClickGui.font().getWidth(var14) - 5.0F;
         ClassicClickGui.font().drawString(var14, var19, (float)(this.y + 5), new Color(180, 180, 180).getRGB());
         GlStateManager.popMatrix();
         if (this.openAnimation > 0.01F && !this.modulesInCategory.isEmpty()) {
            int var15 = 0;
            ScaledResolution var20;
            double var7 = (double)(var20 = new ScaledResolution(Minecraft.getMinecraft())).getScaleFactor();
            int var3 = (int)((float)Math.min(this.height, 320) * this.openAnimation);
            int var4 = this.y + this.barHeight + var3;
            GL11.glEnable(3089);
            GL11.glScissor(
               (int)((double)this.x * var7),
               (int)((double)(var20.getScaledHeight() - var4) * var7),
               (int)((double)this.width * var7),
               (int)((double)var3 * var7)
            );
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, this.openAnimation);
            Iterator var21 = this.modulesInCategory.iterator();
            while (var21.hasNext()) {
               ClassicClickGui.Component var23;
               int var5 = (var23 = (ClassicClickGui.Component)var21.next()).getHeight();
               if ((double)(var15 + var5) > this.animScroll && (double)var15 < this.animScroll + 320.0) {
                  int var6 = (int)((double)var15 - this.animScroll);
                  var23.setComponentStartAt(this.barHeight + var6);
                  var23.draw(new AtomicInteger(0));
               }
               var15 += var5;
            }
            GlStateManager.popMatrix();
            GL11.glDisable(3089);
            if (this.height > 320) {
               float var22 = 102400.0F / (float)this.height;
               float var24 = (float)this.y + (float)this.barHeight + (float)(this.animScroll * 320.0 / (double)this.height);
               Gui.drawRect(this.x + this.width - 2, this.y + this.barHeight, this.x + this.width, this.y + this.barHeight + var3, new Color(15, 15, 20, 180).getRGB());
               Gui.drawRect(this.x + this.width - 2, (int)var24, this.x + this.width, (int)(var24 + var22), new Color(100, 60, 180, 200).getRGB());
            }
         }
      }
      public int getWidth() {
         return this.width;
      }
      public int getX() {
         return this.x;
      }
      public int getY() {
         return this.y;
      }
      public void setY(int var1) {
         this.y = var1;
      }
      public void setX(int var1) {
         this.x = var1;
      }
      public boolean isHovered(int var1, int var2) {
         return var1 >= this.x + this.width - 20 && var1 <= this.x + this.width && var2 >= this.y && var2 <= this.y + this.barHeight;
      }
      public boolean isPin() {
         return this.pin;
      }
      public void onScroll(int var1, int var2, int var3) {
         if (this.categoryOpened && this.height > 320) {
            int var4 = this.y + this.barHeight;
            int var5 = this.y + this.barHeight + 320;
            if (var1 >= this.x && var1 <= this.x + this.width && var2 >= var4 && var2 <= var5) {
               this.scroll -= var3 * 12;
               this.scroll = Math.max(0, Math.min(this.scroll, this.height - 320));
            }
         }
      }
      public void setOpened(boolean var1) {
         this.categoryOpened = var1;
      }
      public void handleDrag(int var1, int var2) {
         if (this.dragging) {
            this.setX(var1 - this.dragOffsetX);
            this.setY(var2 - this.dragOffsetY);
         }
      }
      public boolean isOpened() {
         return this.categoryOpened;
      }
      public boolean insideArea(int var1, int var2) {
         return var1 >= this.x && var1 <= this.x + this.width && var2 >= this.y && var2 <= this.y + this.barHeight;
      }
      public void setPin(boolean var1) {
         this.pin = var1;
      }
      public void mousePressed(boolean var1) {
         this.dragging = var1;
      }
      public void setLocation(int var1, int var2) {
         this.x = var1;
         this.y = var2;
      }
      public String getName() {
         return this.categoryName;
      }
      public CategoryComponent(String var2, List<Module> var3) {
         this.categoryName = var2;
         this.width = 95;
         this.x = 5;
         this.y = 5;
         this.barHeight = 16;
         this.dragOffsetX = 0;
         this.categoryOpened = true;
         this.dragging = false;
         int var5 = this.barHeight;
         for (Module var4 : var3) {
            ClassicClickGui.ModuleComponent var7 = ClassicClickGui.this.new ModuleComponent(var4, this, var5);
            this.modulesInCategory.add(var7);
            var5 += 16;
         }
      }
      public void update() {
         int var1 = this.barHeight;
         Iterator var2 = this.modulesInCategory.iterator();
         while (var2.hasNext()) {
            ClassicClickGui.Component var3;
            (var3 = (ClassicClickGui.Component)var2.next()).setComponentStartAt(var1);
            var1 += var3.getHeight();
         }
      }
   }
   class CheckBoxComponent implements ClassicClickGui.Component {
      private final BooleanProperty property;
      private final ClassicClickGui.ModuleComponent module;
      private int offsetY;
      private int x;
      private int y;
      private float hoverAnimation = 0.0F;
      private float checkAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         return 14;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isHovered(var1, var2) && var3 == 0 && this.module.panelExpand) {
            this.property.setValue(!(Boolean)this.property.getValue());
         }
      }
      @Override
      public boolean isVisible() {
         return this.property.isVisible();
      }
      private void drawHollowRect(int var1, int var2, int var3, int var4, int var5) {
         Gui.drawRect(var1, var2, var3, var2 + 1, var5);
         Gui.drawRect(var1, var4 - 1, var3, var4, var5);
         Gui.drawRect(var1, var2, var1 + 1, var4, var5);
         Gui.drawRect(var3 - 1, var2, var3, var4, var5);
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var6 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         int var9 = 0;
         int var3 = 0;
         try {
            var9 = Mouse.getX() * this.module.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.module.category.getWidth() - Mouse.getY() * this.module.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var5) {
         }
         float var10 = this.isHovered(var9, var3) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var10 - this.hoverAnimation) * Math.min(var6 * 12.0F, 1.0F);
         float var11 = this.property.getValue() ? 1.0F : 0.0F;
         this.checkAnimation = this.checkAnimation + (var11 - this.checkAnimation) * Math.min(var6 * 10.0F, 1.0F);
         int var7 = this.module.category.getX() + this.module.category.getWidth() - 10 - 5;
         int var12 = this.module.category.getY() + this.offsetY + 2;
         if (this.hoverAnimation > 0.01F) {
            var3 = (int)(40.0F * this.hoverAnimation);
            Gui.drawRect(
               this.module.category.getX() + 2,
               this.module.category.getY() + this.offsetY,
               this.module.category.getX() + this.module.category.getWidth() - 2,
               this.module.category.getY() + this.offsetY + 14,
               new Color(60, 60, 70, var3).getRGB()
            );
         }
         Gui.drawRect(var7, var12, var7 + 10, var12 + 10, new Color(20, 20, 25, 220).getRGB());
         Color var14 = this.checkAnimation > 0.01F ? new Color(80, 150, 255) : new Color(60, 60, 70);
         this.drawHollowRect(var7, var12, var7 + 10, var12 + 10, var14.getRGB());
         if (this.checkAnimation > 0.01F) {
            var3 = (int)(8.0F * this.checkAnimation);
            int var4 = (10 - var3) / 2;
            Gui.drawRect(
               var7 + var4, var12 + var4, var7 + var4 + var3, var12 + var4 + var3, new Color(80, 150, 255, (int)(200.0F * this.checkAnimation)).getRGB()
            );
         }
         if (this.checkAnimation > 0.4F) {
            float var16 = Math.min(1.0F, (this.checkAnimation - 0.4F) / 0.6F);
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.5F);
            GL11.glBegin(3);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, var16);
            float var18 = Math.min(1.0F, (this.checkAnimation - 0.4F) / 0.6F);
            GL11.glVertex2f((float)(var7 + 2), (float)(var12 + 5));
            if (var18 > 0.3F) {
               GL11.glVertex2f((float)(var7 + 4), (float)(var12 + 7));
            }
            if (var18 > 0.6F) {
               GL11.glVertex2f((float)(var7 + 8), (float)(var12 + 3));
            }
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
         }
         String var17 = this.property.getName().replace("-", " ");
         Color var19;
         if (this.checkAnimation > 0.01F) {
            var19 = new Color(220, 220, 220);
         } else {
            int var8 = 170 + (int)(30.0F * this.hoverAnimation);
            var19 = new Color(var8, var8, var8);
         }
         ClassicClickGui.font()
            .drawString(var17, (float)(this.module.category.getX() + 5), (float)(this.module.category.getY() + this.offsetY + 4), var19.getRGB());
      }
      public boolean isHovered(int var1, int var2) {
         return var1 > this.x && var1 < this.x + this.module.category.getWidth() && var2 > this.y && var2 < this.y + 14;
      }
      @Override
      public void keyTyped(char var1, int var2) {
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public CheckBoxComponent(BooleanProperty var2, ClassicClickGui.ModuleComponent var3, int var4) {
         this.property = var2;
         this.module = var3;
         this.x = var3.category.getX() + var3.category.getWidth();
         this.y = var3.category.getY() + var3.offsetY;
         this.offsetY = var4;
         this.checkAnimation = var2.getValue() ? 1.0F : 0.0F;
      }
      @Override
      public void update(int var1, int var2) {
         this.y = this.module.category.getY() + this.offsetY;
         this.x = this.module.category.getX();
      }
   }
   class ColorSliderComponent implements ClassicClickGui.Component {
      private final ClassicClickGui.ModuleComponent parentModule;
      private final ColorProperty property;
      private int offsetY;
      private boolean draggingHue;
      private boolean draggingSat;
      private boolean draggingBri;
      private float hue;
      private float saturation;
      private float brightness;
      private float hoverAnimation = 0.0F;
      private float dragAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         return 28;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (var3 == 0 && this.parentModule.panelExpand) {
            var3 = this.parentModule.category.getY() + this.offsetY + 10;
            if (this.isHovered(var1, var2, var3)) {
               this.draggingHue = true;
            } else if (this.isHovered(var1, var2, var3 + 6)) {
               this.draggingSat = true;
            } else {
               if (this.isHovered(var1, var2, var3 + 12)) {
                  this.draggingBri = true;
               }
            }
         }
      }
      @Override
      public boolean isVisible() {
         return this.property.isVisible();
      }
      private void drawGradientBar(int var1, int var2, int var3, int var4, int var5) {
         Render3DUtil.drawGradientRect(var1, var2, (float)(var1 + var3), var2 + 4, var4, var5);
      }
      private void drawPointer(int var1, int var2, int var3, float var4, boolean var5) {
         int var6;
         Gui.drawRect((var6 = var1 + (int)((float)var3 * var4)) - 1, var2, var6 + 1, var2 + 4, new Color(255, 255, 255, 240).getRGB());
         if (var5) {
            Gui.drawRect(var6 - 2, var2 - 1, var6 + 2, var2 + 5, new Color(255, 255, 255, 100).getRGB());
         }
      }
      private float getSliderValue(int var1, int var2, int var3) {
         double var4 = (double)Math.min(var3, Math.max(0, var1 - var2));
         return (float)this.roundToPrecision(var4 / (double)var3, 3);
      }
      private void drawHueBar(int var1, int var2, int var3) {
         for (int var4 = 0; var4 < var3; var4++) {
            int var5 = Color.HSBtoRGB((float)var4 / (float)var3, 1.0F, 1.0F);
            Gui.drawRect(var1 + var4, var2, var1 + var4 + 1, var2 + 4, var5);
         }
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var10 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         int var12 = 0;
         int var3 = 0;
         try {
            var12 = Mouse.getX() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.parentModule.category.getWidth() - Mouse.getY() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var9) {
         }
         int var4;
         int var5;
         int var6 = (var5 = (var4 = this.parentModule.category.getY() + this.offsetY + 10) + 6) + 6;
         float var13 = this.isHovered(var12, var3, var4) || this.isHovered(var12, var3, var5) || this.isHovered(var12, var3, var6) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var13 - this.hoverAnimation) * Math.min(var10 * 12.0F, 1.0F);
         float var14 = this.draggingHue || this.draggingSat || this.draggingBri ? 1.0F : 0.0F;
         this.dragAnimation = this.dragAnimation + (var14 - this.dragAnimation) * Math.min(var10 * 10.0F, 1.0F);
         int var11 = this.parentModule.category.getX() + 5;
         int var15 = this.parentModule.category.getY() + this.offsetY;
         var3 = this.parentModule.category.getWidth() - 10;
         String var7 = this.property.getName().replace("-", " ") + ": ";
         ClassicClickGui.font().drawString(var7, (float)var11, (float)(var15 + 3), new Color(170, 170, 170).getRGB());
         if (!this.draggingHue && !this.draggingSat && !this.draggingBri) {
            Color var18;
            float[] var19 = Color.RGBtoHSB((var18 = new Color((Integer)this.property.getValue())).getRed(), var18.getGreen(), var18.getBlue(), null);
            this.hue = var19[0];
            this.saturation = var19[1];
            this.brightness = var19[2];
         }
         int var20 = var11 + var3 - 8;
         int var16 = var15 + 2;
         int var8 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
         Gui.drawRect(var20, var16, var20 + 8, var16 + 8, var8);
         this.drawHueBar(var11, var4, var3);
         this.drawPointer(var11, var4, var3, this.hue, this.draggingHue);
         this.drawGradientBar(var11, var5, var3, Color.WHITE.getRGB(), Color.getHSBColor(this.hue, 1.0F, 1.0F).getRGB());
         this.drawPointer(var11, var5, var3, this.saturation, this.draggingSat);
         this.drawGradientBar(var11, var6, var3, Color.BLACK.getRGB(), Color.getHSBColor(this.hue, this.saturation, 1.0F).getRGB());
         this.drawPointer(var11, var6, var3, this.brightness, this.draggingBri);
      }
      private boolean isHovered(int var1, int var2, int var3) {
         int var4;
         int var5 = (var4 = this.parentModule.category.getX() + 5) + this.parentModule.category.getWidth() - 10;
         return var1 >= var4 && var1 <= var5 && var2 >= var3 && var2 <= var3 + 4;
      }
      private double roundToPrecision(double var1, int var3) {
         return new BigDecimal(var1).setScale(var3, RoundingMode.HALF_UP).doubleValue();
      }
      @Override
      public void keyTyped(char var1, int var2) {
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
         this.draggingHue = this.draggingSat = this.draggingBri = false;
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public ColorSliderComponent(ColorProperty var2, ClassicClickGui.ModuleComponent var3, int var4) {
         this.parentModule = var3;
         this.offsetY = var4;
         this.property = var2;
         Color var5;
         float[] var6 = Color.RGBtoHSB((var5 = new Color((Integer)var2.getValue())).getRed(), var5.getGreen(), var5.getBlue(), null);
         this.hue = var6[0];
         this.saturation = var6[1];
         this.brightness = var6[2];
      }
      @Override
      public void update(int var1, int var2) {
         var2 = this.parentModule.category.getX() + 5;
         int var3 = this.parentModule.category.getWidth() - 10;
         boolean var4 = false;
         if (this.draggingHue) {
            this.hue = this.getSliderValue(var1, var2, var3);
            var4 = true;
         }
         if (this.draggingSat) {
            this.saturation = this.getSliderValue(var1, var2, var3);
            var4 = true;
         }
         if (this.draggingBri) {
            this.brightness = this.getSliderValue(var1, var2, var3);
            var4 = true;
         }
         if (var4) {
            var1 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
            this.property.setValue(new Color(var1).getRGB() & 16777215);
         }
      }
   }
   interface Component {
      int getHeight();
      void mouseDown(int var1, int var2, int var3);
      boolean isVisible();
      void draw(AtomicInteger var1);
      void keyTyped(char var1, int var2);
      void mouseReleased(int var1, int var2, int var3);
      void setComponentStartAt(int var1);
      void update(int var1, int var2);
   }
   class ModeComponent implements ClassicClickGui.Component {
      private final ModeProperty property;
      private final ClassicClickGui.ModuleComponent parentModule;
      private int x;
      private int y;
      private int offsetY;
      private float hoverAnimation = 0.0F;
      private float changeAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         return 14;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isHovered(var1, var2)) {
            if (var3 == 0) {
               this.property.nextMode();
               this.changeAnimation = 1.0F;
               return;
            }
            if (var3 == 1) {
               this.property.previousMode();
               this.changeAnimation = 1.0F;
            }
         }
      }
      @Override
      public boolean isVisible() {
         return this.property.isVisible();
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var4 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         int var10 = 0;
         int var3 = 0;
         try {
            var10 = Mouse.getX() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.parentModule.category.getWidth() - Mouse.getY() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var6) {
         }
         float var11 = this.isHovered(var10, var3) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var11 - this.hoverAnimation) * Math.min(var4 * 12.0F, 1.0F);
         if (this.changeAnimation > 0.0F) {
            this.changeAnimation = Math.max(0.0F, this.changeAnimation - var4 * 4.0F);
         }
         if (this.hoverAnimation > 0.01F) {
            int var12 = (int)(40.0F * this.hoverAnimation);
            Gui.drawRect(
               this.parentModule.category.getX() + 2,
               this.parentModule.category.getY() + this.offsetY,
               this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
               this.parentModule.category.getY() + this.offsetY + 14,
               new Color(60, 60, 70, var12).getRGB()
            );
         }
         if (this.changeAnimation > 0.01F) {
            Color var13 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), (long)var1.get());
            var3 = (int)(50.0F * this.changeAnimation);
            Gui.drawRect(
               this.parentModule.category.getX() + 2,
               this.parentModule.category.getY() + this.offsetY,
               this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
               this.parentModule.category.getY() + this.offsetY + 14,
               new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), var3).getRGB()
            );
         }
         String var14;
         if (!(var14 = this.property.getModeName().replace("_", " ")).isEmpty()) {
            var14 = var14.substring(0, 1).toUpperCase() + var14.substring(1).toLowerCase();
         }
         String var16 = this.property.getName() + ": ";
         Color var18 = new Color(170, 170, 170);
         ClassicClickGui.font()
            .drawString(var16, (float)(this.parentModule.category.getX() + 5), (float)(this.parentModule.category.getY() + this.offsetY + 4), var18.getRGB());
         Color var7;
         int var19 = (var7 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), (long)var1.get())).getRed();
         int var5 = var7.getGreen();
         int var8 = var7.getBlue();
         if (this.hoverAnimation > 0.01F) {
            var19 = Math.min(255, (int)((float)var19 + 50.0F * this.hoverAnimation));
            var5 = Math.min(255, (int)((float)var5 + 50.0F * this.hoverAnimation));
            var8 = Math.min(255, (int)((float)var8 + 50.0F * this.hoverAnimation));
         }
         if (this.changeAnimation > 0.01F) {
            var19 = Math.min(255, (int)((float)var19 + 80.0F * this.changeAnimation));
            var5 = Math.min(255, (int)((float)var5 + 80.0F * this.changeAnimation));
            var8 = Math.min(255, (int)((float)var8 + 80.0F * this.changeAnimation));
         }
         float var17 = (float)(this.parentModule.category.getX() + 5) + ClassicClickGui.font().getWidth(var16);
         ClassicClickGui.font().drawString(var14, var17, (float)(this.parentModule.category.getY() + this.offsetY + 4), new Color(var19, var5, var8).getRGB());
         if (this.hoverAnimation > 0.3F) {
            int var9 = (int)(180.0F * this.hoverAnimation);
            ClassicClickGui.font()
               .drawString(
                  "<",
                  (float)(this.parentModule.category.getX() + this.parentModule.category.getWidth() - 20),
                  (float)(this.parentModule.category.getY() + this.offsetY + 4),
                  new Color(150, 150, 150, var9).getRGB()
               );
            ClassicClickGui.font()
               .drawString(
                  ">",
                  (float)(this.parentModule.category.getX() + this.parentModule.category.getWidth() - 10),
                  (float)(this.parentModule.category.getY() + this.offsetY + 4),
                  new Color(150, 150, 150, var9).getRGB()
               );
         }
      }
      private boolean isHovered(int var1, int var2) {
         return var1 > this.x && var1 < this.x + this.parentModule.category.getWidth() && var2 > this.y && var2 < this.y + 14;
      }
      @Override
      public void keyTyped(char var1, int var2) {
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public ModeComponent(ModeProperty var2, ClassicClickGui.ModuleComponent var3, int var4) {
         this.property = var2;
         this.parentModule = var3;
         this.x = var3.category.getX() + var3.category.getWidth();
         this.y = var3.category.getY() + var3.offsetY;
         this.offsetY = var4;
      }
      @Override
      public void update(int var1, int var2) {
         this.y = this.parentModule.category.getY() + this.offsetY;
         this.x = this.parentModule.category.getX();
      }
   }
   class ModuleComponent implements ClassicClickGui.Component {
      public Module mod;
      public ClassicClickGui.CategoryComponent category;
      public int offsetY;
      private final ArrayList<ClassicClickGui.Component> settings;
      public boolean panelExpand;
      private float hoverAnimation = 0.0F;
      private float enableAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         if (!this.panelExpand) {
            return 16;
         } else {
            int var1 = 16;
            Iterator var2 = this.settings.iterator();
            while (var2.hasNext()) {
               ClassicClickGui.Component var3;
               if ((var3 = (ClassicClickGui.Component)var2.next()).isVisible()) {
                  var1 += var3.getHeight();
               }
            }
            return var1;
         }
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isHovered(var1, var2) && var3 == 0) {
            this.mod.toggle();
         }
         if (this.isHovered(var1, var2) && var3 == 1) {
            this.panelExpand = !this.panelExpand;
         }
         if (this.panelExpand) {
            Iterator var4 = this.settings.iterator();
            while (var4.hasNext()) {
               ClassicClickGui.Component var5;
               if ((var5 = (ClassicClickGui.Component)var4.next()).isVisible()) {
                  var5.mouseDown(var1, var2, var3);
               }
            }
         }
      }
      @Override
      public boolean isVisible() {
         return true;
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var4 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         float var7 = this.mod.isEnabled() ? 1.0F : 0.0F;
         this.enableAnimation = this.enableAnimation + (var7 - this.enableAnimation) * Math.min(var4 * 8.0F, 1.0F);
         int var8 = 0;
         int var3 = 0;
         try {
            var8 = Mouse.getX() * this.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.category.getWidth() - Mouse.getY() * this.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var6) {
         }
         float var9 = this.isHovered(var8, var3) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var9 - this.hoverAnimation) * Math.min(var4 * 12.0F, 1.0F);
         int var10 = this.category.getX();
         var3 = this.category.getY() + this.offsetY;
         int var18 = this.category.getX() + this.category.getWidth();
         if (this.enableAnimation > 0.01F) {
            int var5 = (int)(25.0F * this.enableAnimation);
            Gui.drawRect(var10 + 1, var3, var18 - 1, var3 + 16, new Color(100, 60, 180, var5).getRGB());
         }
         if (this.hoverAnimation > 0.01F && !this.mod.isEnabled()) {
            int var19 = (int)(20.0F * this.hoverAnimation);
            Gui.drawRect(var10 + 1, var3, var18 - 1, var3 + 16, new Color(60, 60, 70, var19).getRGB());
         }
         int var20;
         if (this.mod.isEnabled()) {
            var20 = new Color(220, 220, 220).getRGB();
         } else {
            int var11 = 140 + (int)(30.0F * this.hoverAnimation);
            var20 = new Color(var11, var11, var11).getRGB();
         }
         float var12 = (float)(this.category.getX() + 6);
         ClassicClickGui.font().drawString(this.mod.getName(), var12, (float)(this.category.getY() + this.offsetY + 5), var20);
         if (!this.settings.isEmpty()) {
            String var13 = this.panelExpand ? "-" : "+";
            float var16 = (float)(this.category.getX() + this.category.getWidth() - 10);
            ClassicClickGui.font().drawString(var13, var16, (float)(this.category.getY() + this.offsetY + 5), new Color(150, 150, 150).getRGB());
         }
         if (this.panelExpand && !this.settings.isEmpty()) {
            Iterator var14 = this.settings.iterator();
            while (var14.hasNext()) {
               ClassicClickGui.Component var17;
               if ((var17 = (ClassicClickGui.Component)var14.next()).isVisible()) {
                  var17.draw(var1);
                  var1.incrementAndGet();
               }
            }
         }
      }
      public boolean isHovered(int var1, int var2) {
         return var1 > this.category.getX()
            && var1 < this.category.getX() + this.category.getWidth()
            && var2 > this.category.getY() + this.offsetY
            && var2 < this.category.getY() + 16 + this.offsetY;
      }
      @Override
      public void keyTyped(char var1, int var2) {
         if (this.panelExpand) {
            Iterator var3 = this.settings.iterator();
            while (var3.hasNext()) {
               ClassicClickGui.Component var4;
               if ((var4 = (ClassicClickGui.Component)var3.next()).isVisible()) {
                  var4.keyTyped(var1, var2);
               }
            }
         }
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
         if (this.panelExpand) {
            Iterator var4 = this.settings.iterator();
            while (var4.hasNext()) {
               ClassicClickGui.Component var5;
               if ((var5 = (ClassicClickGui.Component)var4.next()).isVisible()) {
                  var5.mouseReleased(var1, var2, var3);
               }
            }
         }
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
         var1 = this.offsetY + 16;
         Iterator var2 = this.settings.iterator();
         while (var2.hasNext()) {
            ClassicClickGui.Component var3;
            (var3 = (ClassicClickGui.Component)var2.next()).setComponentStartAt(var1);
            if (var3.isVisible()) {
               var1 += var3.getHeight();
            }
         }
      }
      public ModuleComponent(Module var2, ClassicClickGui.CategoryComponent var3, int var4) {
         this.mod = var2;
         this.category = var3;
         this.offsetY = var4;
         this.settings = new ArrayList<>();
         this.panelExpand = false;
         int var7 = var4 + 10;
         List var5;
         if ((var5 = Valor.propertyManager.propertiesByModule.get(var2.getClass())) != null && !var5.isEmpty()) {
            Iterator var6 = var5.iterator();
            while (var6.hasNext()) {
               Property var8;
               if ((var8 = (Property)var6.next()) instanceof BooleanProperty) {
                  ClassicClickGui.CheckBoxComponent var9 = ClassicClickGui.this.new CheckBoxComponent((BooleanProperty)var8, this, var7);
                  this.settings.add(var9);
                  var7 += var9.getHeight();
               } else if (var8 instanceof FloatProperty) {
                  ClassicClickGui.SliderComponent var10 = ClassicClickGui.this.new SliderComponent(new FloatSlider((FloatProperty)var8), this, var7);
                  this.settings.add(var10);
                  var7 += var10.getHeight();
               } else if (var8 instanceof IntProperty) {
                  ClassicClickGui.SliderComponent var11 = ClassicClickGui.this.new SliderComponent(new IntSlider((IntProperty)var8), this, var7);
                  this.settings.add(var11);
                  var7 += var11.getHeight();
               } else if (var8 instanceof PercentageProperty) {
                  ClassicClickGui.SliderComponent var12 = ClassicClickGui.this.new SliderComponent(new PercentageSlider((PercentageProperty)var8), this, var7);
                  this.settings.add(var12);
                  var7 += var12.getHeight();
               } else if (var8 instanceof ModeProperty) {
                  ClassicClickGui.ModeComponent var13 = ClassicClickGui.this.new ModeComponent((ModeProperty)var8, this, var7);
                  this.settings.add(var13);
                  var7 += var13.getHeight();
               } else if (var8 instanceof ColorProperty) {
                  ClassicClickGui.ColorSliderComponent var14 = ClassicClickGui.this.new ColorSliderComponent((ColorProperty)var8, this, var7);
                  this.settings.add(var14);
                  var7 += var14.getHeight();
               } else if (var8 instanceof TextProperty) {
                  ClassicClickGui.TextComponent var15 = ClassicClickGui.this.new TextComponent((TextProperty)var8, this, var7);
                  this.settings.add(var15);
                  var7 += var15.getHeight();
               }
            }
         }
         this.settings.add(ClassicClickGui.this.new BindComponent(this, var7));
      }
      @Override
      public void update(int var1, int var2) {
         if (this.panelExpand) {
            if (!this.settings.isEmpty()) {
               Iterator var3 = this.settings.iterator();
               while (var3.hasNext()) {
                  ClassicClickGui.Component var4;
                  if ((var4 = (ClassicClickGui.Component)var3.next()).isVisible()) {
                     var4.update(var1, var2);
                  }
               }
            }
         }
      }
   }
   class SliderComponent implements ClassicClickGui.Component {
      private final Slider slider;
      private final ClassicClickGui.ModuleComponent parentModule;
      private int offsetY;
      private int x;
      private int y;
      private boolean dragging = false;
      private double sliderWidth;
      private float hoverAnimation = 0.0F;
      private float dragAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         return 18;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isLeftHalfHovered(var1, var2) && var3 == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
         }
         if (this.isRightHalfHovered(var1, var2) && var3 == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
         }
      }
      @Override
      public boolean isVisible() {
         return this.slider.isVisible();
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var4 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         int var10 = 0;
         int var3 = 0;
         try {
            var10 = Mouse.getX() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.parentModule.category.getWidth() - Mouse.getY() * this.parentModule.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var7) {
         }
         float var11 = this.isLeftHalfHovered(var10, var3) || this.isRightHalfHovered(var10, var3) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var11 - this.hoverAnimation) * Math.min(var4 * 12.0F, 1.0F);
         float var12 = this.dragging ? 1.0F : 0.0F;
         this.dragAnimation = this.dragAnimation + (var12 - this.dragAnimation) * Math.min(var4 * 10.0F, 1.0F);
         if (this.hoverAnimation > 0.01F) {
            int var13 = (int)(40.0F * this.hoverAnimation);
            Gui.drawRect(
               this.parentModule.category.getX() + 2,
               this.parentModule.category.getY() + this.offsetY,
               this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
               this.parentModule.category.getY() + this.offsetY + 18,
               new Color(60, 60, 70, var13).getRGB()
            );
         }
         String var14 = this.slider.getName() + ": ";
         String var17 = this.slider.getValueString();
         Color var20 = new Color(170, 170, 170);
         ClassicClickGui.font()
            .drawString(var14, (float)(this.parentModule.category.getX() + 5), (float)(this.parentModule.category.getY() + this.offsetY + 3), var20.getRGB());
         Color var21 = new Color(200, 200, 200);
         float var15 = (float)(this.parentModule.category.getX() + 5) + ClassicClickGui.font().getWidth(var14);
         ClassicClickGui.font().drawString(var17, var15, (float)(this.parentModule.category.getY() + this.offsetY + 3), var21.getRGB());
         int var16 = this.parentModule.category.getY() + this.offsetY + 12;
         var3 = this.parentModule.category.getX() + 5;
         int var22 = this.parentModule.category.getX() + this.parentModule.category.getWidth() - 5;
         Gui.drawRect(var3, var16, var22, var16 + 3, new Color(20, 20, 25, 200).getRGB());
         if ((var3 = var3 + (int)this.sliderWidth) - var3 > this.parentModule.category.getWidth() - 10) {
            var3 = var3 + this.parentModule.category.getWidth() - 10;
         }
         if (var3 > var3) {
            Color var8 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), (long)var1.get());
            int var5 = (int)(30.0F * Math.max(this.hoverAnimation, this.dragAnimation));
            Color var6 = new Color(Math.min(255, var8.getRed() + var5), Math.min(255, var8.getGreen() + var5), Math.min(255, var8.getBlue() + var5), 230);
            Gui.drawRect(var3, var16, var3, var16 + 3, var6.getRGB());
         }
         if (var3 > var3) {
            int var9 = 3 + (int)(2.0F * this.dragAnimation);
            int var24 = var3 - var9 / 2;
            int var25 = var16 - 1;
            Gui.drawRect(var24, var25, var24 + var9, var25 + 5, new Color(255, 255, 255, 240).getRGB());
         }
      }
      public boolean isLeftHalfHovered(int var1, int var2) {
         return var1 > this.x && var1 < this.x + this.parentModule.category.getWidth() / 2 + 1 && var2 > this.y && var2 < this.y + 18;
      }
      public boolean isRightHalfHovered(int var1, int var2) {
         return var1 > this.x + this.parentModule.category.getWidth() / 2
            && var1 < this.x + this.parentModule.category.getWidth()
            && var2 > this.y
            && var2 < this.y + 18;
      }
      private double roundToPrecision(double var1, int var3) {
         return var3 < 0 ? 0.0 : new BigDecimal(var1).setScale(var3, RoundingMode.HALF_UP).doubleValue();
      }
      @Override
      public void keyTyped(char var1, int var2) {
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
         this.dragging = false;
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public SliderComponent(Slider var2, ClassicClickGui.ModuleComponent var3, int var4) {
         this.slider = var2;
         this.parentModule = var3;
         this.x = var3.category.getX() + var3.category.getWidth();
         this.y = var3.category.getY() + var3.offsetY;
         this.offsetY = var4;
      }
      @Override
      public void update(int var1, int var2) {
         this.y = this.parentModule.category.getY() + this.offsetY;
         this.x = this.parentModule.category.getX();
         double var3 = (double)Math.min(this.parentModule.category.getWidth() - 10, Math.max(0, var1 - this.x - 5));
         this.sliderWidth = (double)(this.parentModule.category.getWidth() - 10)
            * (this.slider.getInput() - this.slider.getMin())
            / (this.slider.getMax() - this.slider.getMin());
         if (this.dragging) {
            if (var3 == 0.0) {
               this.slider.setValue(this.slider.getMin());
               return;
            }
            double var5 = var3 / (double)(this.parentModule.category.getWidth() - 10) * (this.slider.getMax() - this.slider.getMin()) + this.slider.getMin();
            double var7;
            if ((var7 = this.slider.getIncrement()) > 0.0) {
               var5 = (double)Math.round(var5 / var7) * var7;
            }
            double var9 = this.roundToPrecision(var5, 2);
            var9 = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), var9));
            this.slider.setValue(var9);
         }
      }
   }
   class TextComponent implements ClassicClickGui.Component {
      private final TextProperty property;
      private final ClassicClickGui.ModuleComponent module;
      private int offsetY;
      private int x;
      private int y;
      private float hoverAnimation = 0.0F;
      private long lastFrameTime = System.currentTimeMillis();
      @Override
      public int getHeight() {
         return 14;
      }
      @Override
      public void mouseDown(int var1, int var2, int var3) {
         if (this.isHovered(var1, var2) && var3 == 0 && this.module.panelExpand) {
            GuiInput.prompt(this.property.getName().replace("-", " "), (String)this.property.getValue(), this.property::setValue, ClassicClickGui.getInstance());
         }
      }
      @Override
      public boolean isVisible() {
         return this.property.isVisible();
      }
      @Override
      public void draw(AtomicInteger var1) {
         long var2;
         float var4 = (float)((var2 = System.currentTimeMillis()) - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var2;
         int var10 = 0;
         int var3 = 0;
         try {
            var10 = Mouse.getX() * this.module.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            var3 = this.module.category.getWidth() - Mouse.getY() * this.module.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
         } catch (Exception var7) {
         }
         float var11 = this.isHovered(var10, var3) ? 1.0F : 0.0F;
         this.hoverAnimation = this.hoverAnimation + (var11 - this.hoverAnimation) * Math.min(var4 * 12.0F, 1.0F);
         if (this.hoverAnimation > 0.01F) {
            int var12 = (int)(40.0F * this.hoverAnimation);
            Gui.drawRect(
               this.module.category.getX() + 2,
               this.module.category.getY() + this.offsetY,
               this.module.category.getX() + this.module.category.getWidth() - 2,
               this.module.category.getY() + this.offsetY + 14,
               new Color(60, 60, 70, var12).getRGB()
            );
         }
         int var13 = this.module.category.getX() + this.module.category.getWidth() - 12;
         var3 = this.module.category.getY() + this.offsetY + 3;
         Color var8 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), (long)var1.get());
         int var18 = 150 + (int)(50.0F * this.hoverAnimation);
         Gui.drawRect(var13, var3, var13 + 8, var3 + 8, new Color(40, 40, 50, var18).getRGB());
         if (this.hoverAnimation > 0.01F) {
            int var19 = (int)(200.0F * this.hoverAnimation);
            Gui.drawRect(var13 + 3, var3 + 2, var13 + 4, var3 + 3, new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), var19).getRGB());
            Gui.drawRect(var13 + 2, var3 + 3, var13 + 3, var3 + 4, new Color(180, 180, 180, var19).getRGB());
            Gui.drawRect(var13 + 1, var3 + 4, var13 + 2, var3 + 5, new Color(160, 160, 160, var19).getRGB());
         }
         String var20 = this.property.getName().replace("-", " ");
         String var14 = (String)this.property.getValue();
         String var16 = var20 + ": ";
         ClassicClickGui.font()
            .drawString(
               var16, (float)(this.module.category.getX() + 5), (float)(this.module.category.getY() + this.offsetY + 4), new Color(170, 170, 170).getRGB()
            );
         int var21 = var8.getRed();
         int var5 = var8.getGreen();
         int var9 = var8.getBlue();
         if (this.hoverAnimation > 0.01F) {
            var21 = Math.min(255, (int)((float)var21 + 40.0F * this.hoverAnimation));
            var5 = Math.min(255, (int)((float)var5 + 40.0F * this.hoverAnimation));
            var9 = Math.min(255, (int)((float)var9 + 40.0F * this.hoverAnimation));
         }
         float var6 = (float)(this.module.category.getWidth() - 30);
         if (ClassicClickGui.font().getWidth(var14) > var6) {
            while (ClassicClickGui.font().getWidth(var14 + "...") > var6 && var14.length() > 0) {
               var14 = var14.substring(0, var14.length() - 1);
            }
            var14 = var14 + "...";
         }
         float var17 = (float)(this.module.category.getX() + 5) + ClassicClickGui.font().getWidth(var16);
         ClassicClickGui.font().drawString(var14, var17, (float)(this.module.category.getY() + this.offsetY + 4), new Color(var21, var5, var9).getRGB());
      }
      public boolean isHovered(int var1, int var2) {
         return var1 > this.x && var1 < this.x + this.module.category.getWidth() && var2 > this.y && var2 < this.y + 14;
      }
      @Override
      public void keyTyped(char var1, int var2) {
      }
      @Override
      public void mouseReleased(int var1, int var2, int var3) {
      }
      @Override
      public void setComponentStartAt(int var1) {
         this.offsetY = var1;
      }
      public TextComponent(TextProperty var2, ClassicClickGui.ModuleComponent var3, int var4) {
         this.property = var2;
         this.module = var3;
         this.x = var3.category.getX() + var3.category.getWidth();
         this.y = var3.category.getY() + var3.offsetY;
         this.offsetY = var4;
      }
      @Override
      public void update(int var1, int var2) {
         this.y = this.module.category.getY() + this.offsetY;
         this.x = this.module.category.getX();
      }
   }
}
