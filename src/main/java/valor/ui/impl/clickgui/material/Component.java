package valor.ui.impl.clickgui.material;

public abstract class Component {
   public int x;
   public int y;
   public int width;
   public int height;

   public abstract void render(int var1, int var2, float var3, int var4);

   public int getHeight() {
      return this.height;
   }

   public void setWidth(int var1) {
      this.width = var1;
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

   public boolean isVisible() {
      return true;
   }

   public boolean isHovered(int var1, int var2) {
      return var1 >= this.x && var1 <= this.x + this.width && var2 >= this.y && var2 <= this.y + this.getHeight();
   }

   public boolean isBinding() {
      return false;
   }

   public void keyTyped(char var1, int var2) {
   }

   public abstract void mouseReleased(int var1, int var2, int var3);

   public abstract boolean mouseClicked(int var1, int var2, int var3);

   public Component(int var1, int var2, int var3, int var4) {
      this.x = var1;
      this.y = var2;
      this.width = var3;
      this.height = var4;
   }
}
