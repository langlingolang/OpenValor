package valor.ui.impl.clickgui.material;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import valor.module.Module;
import valor.util.FloatInterpolator;
import valor.util.render.GreenRender;

public class Frame extends Component {
   protected final String categoryName;
   protected final Frame.IconType iconType;
   private final List<ModuleEntry> allEntries = new ArrayList<>();
   private final List<ModuleEntry> visibleEntries = new ArrayList<>();
   protected boolean expanded = true;
   protected final FloatInterpolator body = new FloatInterpolator(1.0F);
   protected boolean dragging;
   protected int dragX;
   protected int dragY;
   protected int scroll;
   protected int bodyLimit = 380;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      float var12 = this.getCurrentHeight();
      GreenRender.glowRR((float)super.x, (float)(super.y + 2), (float)super.width, var12, 7.0F, 7.0F, new Color(8, 9, 22, 62));
      MaterialTheme.drawGlass((float)super.x, (float)super.y, (float)super.width, var12, 7.0F);
      GreenRender.fillRRCorners(
         (float)super.x,
         (float)super.y,
         (float)super.width,
         (float)MaterialTheme.HEADER_HEIGHT,
         7.0F,
         7.0F,
         this.expanded ? 0.0F : 7.0F,
         this.expanded ? 0.0F : 7.0F,
         MaterialTheme.CATEGORY_HEADER_BG
      );
      this.drawHeader();
      if (var12 > (float)MaterialTheme.HEADER_HEIGHT + 0.5F && !this.visibleEntries.isEmpty()) {
         int var5 = this.getContentHeight();
         float var13 = var12 - (float)MaterialTheme.HEADER_HEIGHT;
         var5 = Math.max(0, var5 - this.bodyLimit);
         if (this.scroll > var5) {
            this.scroll = var5;
         }

         if (this.scroll < 0) {
            this.scroll = 0;
         }

         GreenRender.pushScissor((float)super.x, (float)(super.y + MaterialTheme.HEADER_HEIGHT), (float)super.width, Math.max(0.0F, var13));

         try {
            int var6 = super.y + MaterialTheme.HEADER_HEIGHT - this.scroll;
            int var7 = 0;
            Iterator var8 = this.visibleEntries.iterator();

            while (var8.hasNext()) {
               ModuleEntry var9;
               (var9 = (ModuleEntry)var8.next()).setX(super.x);
               var9.setY(var6);
               var9.setWidth(super.width);
               var9.setRowIndex(var7++);
               var9.setRoundedBottom(var7 == this.visibleEntries.size() && this.scroll == var5);
               if (var6 + var9.getHeight() > super.y + MaterialTheme.HEADER_HEIGHT - 2
                  && (float)var6 < (float)(super.y + MaterialTheme.HEADER_HEIGHT) + var13 + 2.0F) {
                  var9.render(var1, var2, var3, var7);
               }

               var6 += var9.getHeight();
            }
         } finally {
            GreenRender.popScissor();
         }

         MaterialTheme.drawOverflowFade((float)super.x, (float)(super.y + MaterialTheme.HEADER_HEIGHT) + var13, (float)super.width, var5 - this.scroll);
      }
   }

   public int getX() {
      return super.x;
   }

   @Override
   public int getY() {
      return super.y;
   }

   public void setPos(int var1, int var2) {
      super.x = var1;
      super.y = var2;
   }

   public String getCategoryName() {
      return this.categoryName;
   }

   public float getCurrentHeight() {
      return (float)MaterialTheme.HEADER_HEIGHT + this.visibleBodyHeight(Math.min(this.getContentHeight(), this.bodyLimit));
   }

   protected void drawHeader() {
      float var1 = GreenRender.strWBold(this.categoryName) + 8.0F + 5.0F;
      var1 = (float)super.x + ((float)super.width - var1) / 2.0F;
      Color var2 = MaterialTheme.ACCENT_PURPLE;
      CategoryIcons.draw(
         this.iconType,
         var1,
         (float)super.y + ((float)MaterialTheme.HEADER_HEIGHT - 8.0F) / 2.0F - 1.25F,
         8.0F,
         (float)var2.getRed() / 255.0F,
         (float)var2.getGreen() / 255.0F,
         (float)var2.getBlue() / 255.0F,
         1.0F
      );
      GreenRender.drawStringBold(
         this.categoryName,
         var1 + 8.0F + 5.0F,
         (float)super.y + ((float)MaterialTheme.HEADER_HEIGHT - GreenRender.fontH()) / 2.0F,
         this.expanded ? MaterialTheme.TEXT : MaterialTheme.TEXT_MUTED
      );
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public boolean isAnyBinding() {
      if (!this.expanded) {
         return false;
      } else {
         Iterator var1 = this.visibleEntries.iterator();

         while (var1.hasNext()) {
            if (((ModuleEntry)var1.next()).isBinding()) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isMouseOverBody(int var1, int var2) {
      if (!this.expanded) {
         return false;
      } else {
         float var3 = this.getCurrentHeight();
         return var1 >= super.x && var1 <= super.x + super.width && var2 >= super.y + MaterialTheme.HEADER_HEIGHT && (float)var2 < (float)super.y + var3;
      }
   }

   public boolean isMouseOverHeader(int var1, int var2) {
      return var1 >= super.x && var1 <= super.x + super.width && var2 >= super.y && var2 <= super.y + MaterialTheme.HEADER_HEIGHT;
   }

   public void setBodyLimit(int var1) {
      this.bodyLimit = Math.max(MaterialTheme.MODULE_HEIGHT, var1);
   }

   public void setExpanded(boolean var1) {
      this.expanded = var1;
      this.body.reset(var1 ? 1.0F : 0.0F);
   }

   public void updatePosition(int var1, int var2) {
      if (this.dragging) {
         super.x = var1 - this.dragX;
         super.y = var2 - this.dragY;
      }
   }

   protected float visibleBodyHeight(int var1) {
      long var2 = System.currentTimeMillis();
      this.body.interpolateTo(this.expanded ? 1.0F : 0.0F, 200L, var2);
      return (float)var1 * this.body.getValue(var2);
   }

   public void onScroll(int var1, int var2, int var3) {
      if (this.isMouseOverBody(var1, var2)) {
         if ((var1 = this.getContentHeight()) > this.bodyLimit) {
            this.scroll -= var3 * 14;
            var1 = Math.max(0, var1 - this.bodyLimit);
            this.scroll = Math.max(0, Math.min(this.scroll, var1));
         }
      }
   }

   private int getContentHeight() {
      int var1 = 0;

      for (ModuleEntry var3 : this.visibleEntries) {
         var1 += var3.getHeight();
      }

      return var1;
   }

   @Override
   public void keyTyped(char var1, int var2) {
      if (this.expanded) {
         Iterator var3 = this.visibleEntries.iterator();

         while (var3.hasNext()) {
            ((ModuleEntry)var3.next()).keyTyped(var1, var2);
         }
      }
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      this.dragging = false;
      if (this.expanded) {
         Iterator var4 = this.visibleEntries.iterator();

         while (var4.hasNext()) {
            ((ModuleEntry)var4.next()).mouseReleased(var1, var2, var3);
         }
      }
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (this.isMouseOverHeader(var1, var2)) {
         if (var3 == 0) {
            this.dragging = true;
            this.dragX = var1 - super.x;
            this.dragY = var2 - super.y;
            return true;
         }

         if (var3 == 1) {
            this.expanded = !this.expanded;
            return true;
         }
      }

      if (this.expanded && this.isMouseOverBody(var1, var2)) {
         for (ModuleEntry var5 : this.visibleEntries) {
            if (var2 >= var5.getY() && var2 < var5.getY() + var5.getHeight() && var5.mouseClicked(var1, var2, var3)) {
               return true;
            }
         }
      }

      return false;
   }

   protected Frame(String var1, Frame.IconType var2, int var3, int var4, int var5, int var6) {
      super(var3, var4, var5, var6);
      this.categoryName = var1;
      this.iconType = var2;
   }

   public Frame(String var1, Frame.IconType var2, List<Module> var3, int var4, int var5, int var6, int var7) {
      super(var4, var5, var6, var7);
      this.categoryName = var1;
      this.iconType = var2;

      for (Module var9 : var3) {
         this.allEntries.add(new ModuleEntry(var9, var4, 0, var6, MaterialTheme.MODULE_HEIGHT));
      }

      this.visibleEntries.addAll(this.allEntries);
   }

   public void filter(String var1) {
      this.visibleEntries.clear();
      Iterator var2 = this.allEntries.iterator();

      while (var2.hasNext()) {
         ModuleEntry var3;
         if ((var3 = (ModuleEntry)var2.next()).matchesSearch(var1)) {
            this.visibleEntries.add(var3);
         }
      }

      this.scroll = 0;
   }

   public static enum IconType {
      COMBAT,
      MOVEMENT,
      PLAYER,
      VISUAL,
      WORLD,
      CONFIGS;
   }
}
