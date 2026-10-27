package valor.module.render;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.minecraft.client.Minecraft;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.ui.impl.clickgui.material.MaterialClickGui;

public class ClickGuiMod extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static void logRuntimeError(String message, Throwable throwable) {
      try {
         File logFile = new File(System.getProperty("user.home"), "AppData\\Roaming\\.minecraft\\valor_runtime.log");
         try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("[Valor] " + message);
            if (throwable != null) {
               throwable.printStackTrace(writer);
            }
         }
      } catch (Throwable ignored) {
      }
   }

   @Override
   public void onEnabled() {
      this.setEnabled(false);

      try {
         MaterialClickGui clickGui = MaterialClickGui.openFresh();
         mc.displayGuiScreen(clickGui);
         if (mc.currentScreen != clickGui) {
            throw new IllegalStateException("Minecraft did not retain the ClickGui screen");
         } else {
            logRuntimeError("ClickGui opened on " + Thread.currentThread().getName(), null);
         }
      } catch (Throwable t) {
         System.out.println("[Valor] Failed to open ClickGui: " + t);
         t.printStackTrace();
         logRuntimeError("ClickGui failed: " + t, t);
      }
   }

   public ClickGuiMod() {
      super("ClickGui", false, ModuleCategory.VISUAL);
      this.setKey(54);
   }
}
