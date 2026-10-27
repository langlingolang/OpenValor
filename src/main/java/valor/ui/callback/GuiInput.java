package valor.ui.callback;

import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiInput extends GuiScreen {
   private final String title;
   private final String defaultValue;
   private final Consumer<String> callback;
   private GuiTextField textField;
   private GuiButton buttonOk;
   private GuiScreen caller;

   @Override
   public void updateScreen() {
      this.textField.updateCursorCounter();
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, this.height / 2 - 35, 16777215);
      this.textField.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

   @Override
   protected void keyTyped(char var1, int var2) {
      this.textField.textboxKeyTyped(var1, var2);
      if (var2 != 28 && var2 != 156) {
         if (var2 == 1) {
            this.actionPerformed(null);
         }
      } else {
         this.actionPerformed(this.buttonOk);
      }
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) {
      try {
         super.mouseClicked(var1, var2, var3);
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }

      this.textField.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void actionPerformed(GuiButton var1) {
      if (var1 == this.buttonOk && this.callback != null) {
         this.callback.accept(this.textField.getText());
      }

      this.mc.displayGuiScreen(this.caller);
   }

   @Override
   public void initGui() {
      int var1 = this.width / 2;
      int var2 = this.height / 2;
      this.textField = new GuiTextField(0, this.fontRendererObj, var1 - 100, var2 - 10, 200, 20);
      this.textField.setText(this.defaultValue);
      this.textField.setFocused(true);
      this.buttonList.add(this.buttonOk = new GuiButton(0, var1 - 100, var2 + 20, 95, 20, "Confirm"));
      this.buttonList.add(new GuiButton(1, var1 + 5, var2 + 20, 95, 20, "Cancel"));
   }

   public static void prompt(String var0, String var1, Consumer<String> var2, GuiScreen var3) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiInput(var0, var1, var2, var3));
   }

   public GuiInput(String var1, String var2, Consumer<String> var3, GuiScreen var4) {
      this.title = var1;
      this.defaultValue = var2;
      this.callback = var3;
      this.caller = var4;
   }
}
