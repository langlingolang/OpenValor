package valor.util;

import net.minecraft.client.renderer.GlStateManager;
import valor.util.render.GreenRender;

public final class GlScaleUtil {
   public static void endScale() {
      GlStateManager.popMatrix();
      GreenRender.popScissorTransform();
      GreenRender.endBatch();
   }

   public static void beginScale(float x, float y, float width, float height, float scale) {
      GreenRender.beginBatch();
      scale = 0.72F + 0.28F * Math.max(0.0F, Math.min(1.0F, scale));
      x += width / 2.0F;
      y += height / 2.0F;
      GreenRender.pushScissorTransform(x, y, scale);
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0.0F);
      GlStateManager.scale(scale, scale, 1.0F);
      GlStateManager.translate(-x, -y, 0.0F);
   }
}
