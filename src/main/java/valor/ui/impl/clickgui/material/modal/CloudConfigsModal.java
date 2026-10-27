package valor.ui.impl.clickgui.material.modal;

import java.awt.Color;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.ui.impl.clickgui.material.UiIcons;
import valor.util.render.GreenRender;

public class CloudConfigsModal extends Modal {
   private static final int MODAL_W = 340;
   private static final int MODAL_H = 180;

   @Override
   protected boolean clickBody(int var1, int var2, int var3) {
      return true;
   }

   @Override
   protected void renderBody(int var1, int var2, float var3) {
      float var6 = this.bodyX();
      float var7 = this.bodyY();
      var3 = this.bodyW();
      this.bodyH();
      float var4 = var6 + (var3 - 36.0F) / 2.0F;
      float var8 = var7 + 22.0F;
      Color var5 = MaterialTheme.hudAccent();
      GreenRender.glowRR(var4 + 7.0F, var8 + 7.0F, 22.0F, 22.0F, 22.0F / 2.0F, 14.0F, MaterialTheme.accent(var5, 0.18F));
      UiIcons.draw(3, var4, var8, 36.0F, (float)var5.getRed() / 255.0F, (float)var5.getGreen() / 255.0F, (float)var5.getBlue() / 255.0F, 0.9F);
      String var10 = "Coming soon";
      GreenRender.drawStringBold("Coming soon", var6 + (var3 - GreenRender.strW(var10)) / 2.0F, var8 + 36.0F + 14.0F, MaterialTheme.TEXT);
      String var11 = "Sync and share configs from the cloud.";
      String var12 = "This feature is under development.";
      GreenRender.drawStringSmall(var11, var6 + (var3 - GreenRender.strWSmall(var11)) / 2.0F, var8 + 36.0F + 36.0F, MaterialTheme.TEXT_MUTED);
      GreenRender.drawStringSmall(var12, var6 + (var3 - GreenRender.strWSmall(var12)) / 2.0F, var8 + 36.0F + 50.0F, MaterialTheme.TEXT_MUTED);
   }

   public CloudConfigsModal() {
      super("Cloud Configs", 340, 180);
   }
}
