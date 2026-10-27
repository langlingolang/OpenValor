package valor.ui.impl.clickgui.material.modal;

import java.awt.Color;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.ui.impl.clickgui.material.UiIcons;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.render.GreenRender;

public abstract class Modal {
   protected final String title;
   protected int x;
   protected int y;
   protected final int width;
   protected final int height;
   private boolean closing;
   private final FloatInterpolator visibility = new FloatInterpolator(0.0F);
   protected int screenWidth;
   protected int screenHeight;

   public boolean isClosed() {
      return this.closing && this.visibility.isFinished(System.currentTimeMillis());
   }

   public final void render(int var1, int var2, float var3) {
      long var4 = System.currentTimeMillis();
      this.visibility.interpolateTo(this.closing ? 0.0F : 1.0F, this.closing ? 130L : 190L, var4);
      float var10 = this.visibility.getValue(var4);
      GreenRender.fillRect(
         0.0F,
         0.0F,
         (float)this.screenWidth,
         (float)this.screenHeight,
         MaterialTheme.accent(MaterialTheme.MODAL_OVERLAY, (float)MaterialTheme.MODAL_OVERLAY.getAlpha() / 255.0F * var10)
      );
      if (!(var10 <= 0.0F)) {
         GlScaleUtil.beginScale((float)this.x, (float)this.y, (float)this.width, (float)this.height, var10);

         try {
            GreenRender.glowRR((float)(this.x + 2), (float)(this.y + 5), (float)this.width, (float)this.height, 7.0F, 16.0F, new Color(0, 0, 0, 126));
            MaterialTheme.drawGlass((float)this.x, (float)this.y, (float)this.width, (float)this.height, 7.0F);
            GreenRender.fillRRCorners(
               (float)this.x, (float)this.y, (float)this.width, (float)this.headerHeight(), 7.0F, 7.0F, 0.0F, 0.0F, MaterialTheme.HEADER_BG
            );
            GreenRender.fillRect((float)(this.x + 12), (float)(this.y + this.headerHeight() - 1), (float)(this.width - 24), 1.0F, MaterialTheme.GLASS_EDGE_SOFT);
            float var11 = (float)this.y + ((float)this.headerHeight() - GreenRender.fontHSmall()) / 2.0F + 0.5F;
            GreenRender.drawStringSmall(this.title, (float)(this.x + 14), var11, MaterialTheme.TEXT);
            this.renderHeaderAccessory(var1, var2);
            float var12 = this.closeSize();
            float var5 = (float)(this.x + this.width) - var12 - 8.0F;
            float var6 = (float)this.y + ((float)this.headerHeight() - var12) / 2.0F;
            boolean var7 = (float)var1 >= var5 && (float)var1 <= var5 + var12 && (float)var2 >= var6 && (float)var2 <= var6 + var12;
            GreenRender.fillRR(var5, var6, var12, var12, var12 / 2.0F, var7 ? MaterialTheme.accent(MaterialTheme.MODAL_DANGER, 0.78F) : MaterialTheme.MODAL_ROW);
            float var13 = (var12 - 10.0F) / 2.0F;
            Color var14 = var7 ? Color.WHITE : MaterialTheme.TEXT_MUTED;
            UiIcons.draw(
               1,
               var5 + var13,
               var6 + var13,
               10.0F,
               (float)var14.getRed() / 255.0F,
               (float)var14.getGreen() / 255.0F,
               (float)var14.getBlue() / 255.0F,
               (float)var14.getAlpha() / 255.0F
            );
            this.renderBody(var1, var2, var3);
         } finally {
            GlScaleUtil.endScale();
         }
      }
   }

   protected float bodyX() {
      return (float)this.x;
   }

   protected float closeSize() {
      return 16.0F;
   }

   protected abstract boolean clickBody(int var1, int var2, int var3);

   protected float bodyH() {
      return (float)(this.height - this.headerHeight());
   }

   protected float bodyY() {
      return (float)(this.y + this.headerHeight());
   }

   protected float bodyW() {
      return (float)this.width;
   }

   protected int headerHeight() {
      return 30;
   }

   protected abstract void renderBody(int var1, int var2, float var3);

   public void onScroll(int var1, int var2, int var3) {
   }

   public final void layout(int var1, int var2) {
      this.screenWidth = var1;
      this.screenHeight = var2;
      this.x = (var1 - this.width) / 2;
      this.y = (var2 - this.height) / 2;
   }

   public void keyTyped(char var1, int var2) {
   }

   public void mouseReleased(int var1, int var2, int var3) {
   }

   public final boolean mouseClicked(int var1, int var2, int var3) {
      if (!this.closing && !(this.visibility.getValue(System.currentTimeMillis()) < 0.999F)) {
         float var4 = this.closeSize();
         float var5 = (float)(this.x + this.width) - var4 - 8.0F;
         float var6 = (float)this.y + ((float)this.headerHeight() - var4) / 2.0F;
         if (var3 == 0 && (float)var1 >= var5 && (float)var1 <= var5 + var4 && (float)var2 >= var6 && (float)var2 <= var6 + var4) {
            this.close();
            return true;
         } else if (var3 != 0 || var1 >= this.x && var1 <= this.x + this.width && var2 >= this.y && var2 <= this.y + this.height) {
            if (var3 == 0 && this.clickHeaderAccessory(var1, var2, var3)) {
               return true;
            } else {
               return var2 >= this.y && var2 <= this.y + this.headerHeight() ? true : this.clickBody(var1, var2, var3);
            }
         } else {
            this.close();
            return true;
         }
      } else {
         return true;
      }
   }

   protected boolean clickHeaderAccessory(int var1, int var2, int var3) {
      return false;
   }

   protected void renderHeaderAccessory(int var1, int var2) {
   }

   protected Modal(String var1, int var2, int var3) {
      this.title = var1;
      this.width = var2;
      this.height = var3;
   }

   public void close() {
      this.closing = true;
      this.visibility.interpolateTo(0.0F, 130L, System.currentTimeMillis());
   }
}
