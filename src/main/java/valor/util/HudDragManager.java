package valor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.render.GreenRender;

public final class HudDragManager {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static DraggableHudElement draggedElement = null;
   private static float dragOffsetX;
   private static float dragOffsetY;
   private static boolean wasMouseDown = false;

   public static void updateMouseState() {
      wasMouseDown = Mouse.isButtonDown(0);
      if (!isChatGuiOpen()) {
         draggedElement = null;
         wasMouseDown = false;
      }
   }

   public static void handleDragging(DraggableHudElement element) {
      if (isChatGuiOpen() && element.isHudVisible()) {
         ScaledResolution sr = new ScaledResolution(mc);
         float elemX = element.getX();
         float elemY = element.getY();
         float elemWidth = Math.max(8.0F, element.getWidth());
         float elemHeight = Math.max(8.0F, element.getHeight());
         int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
         int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
         boolean isMouseDown = Mouse.isButtonDown(0);
         boolean isHovered = (float)mouseX >= elemX && (float)mouseX <= elemX + elemWidth && (float)mouseY >= elemY && (float)mouseY <= elemY + elemHeight;

         if (draggedElement == element) {
            if (!isMouseDown) {
               draggedElement = null;
            } else {
               elemX = (float)mouseX - dragOffsetX;
               elemY = (float)mouseY - dragOffsetY;
               float targetY = elemY;
               float targetX = elemX;
               float screenWidth = (float)sr.getScaledWidth();
               float screenHeight = (float)sr.getScaledHeight();
               float centerX = elemX + elemWidth / 2.0F;
               float centerY = elemY + elemHeight / 2.0F;

               if (Math.abs(elemX) < 10.0F) {
                  targetX = 0.0F;
               } else if (Math.abs(elemX + elemWidth - screenWidth) < 10.0F) {
                  targetX = screenWidth - elemWidth;
               } else if (Math.abs(centerX - screenWidth / 2.0F) < 10.0F) {
                  targetX = screenWidth / 2.0F - elemWidth / 2.0F;
               }

               if (Math.abs(elemY) < 10.0F) {
                  targetY = 0.0F;
               } else if (Math.abs(elemY + elemHeight - screenHeight) < 10.0F) {
                  targetY = screenHeight - elemHeight;
               } else if (Math.abs(centerY - screenHeight / 2.0F) < 10.0F) {
                  targetY = screenHeight / 2.0F - elemHeight / 2.0F;
               }

               float clampedX = Math.max(0.0F, Math.min(targetX, screenWidth - Math.max(8.0F, elemWidth)));
               float clampedY = Math.max(0.0F, Math.min(targetY, screenHeight - Math.max(8.0F, elemHeight)));
               element.setX(clampedX);
               element.setY(clampedY);
               elemX = clampedX;
               elemY = clampedY;
            }
         } else if (isMouseDown && !wasMouseDown && isHovered && draggedElement == null) {
            draggedElement = element;
            dragOffsetX = (float)mouseX - elemX;
            dragOffsetY = (float)mouseY - elemY;
         }

         boolean showOutline = isHovered || draggedElement == element;
         if (showOutline) {
            GreenRender.outlineRR(elemX - 1.0F, elemY - 1.0F, elemWidth + 2.0F, elemHeight + 2.0F, 8.0F, 1.2F, MaterialTheme.accent(MaterialTheme.hudAccent(), 0.7F));
            Render3DUtil.setupRender2D();
         }
      } else {
         if (draggedElement == element) {
            draggedElement = null;
         }
      }
   }

   public static boolean isChatGuiOpen() {
      return mc.currentScreen instanceof GuiChat;
   }
}
