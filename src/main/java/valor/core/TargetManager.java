package valor.core;

import java.awt.Color;
import java.io.File;
import valor.util.ChatColor;
import valor.util.ValorPaths;

public class TargetManager extends PlayerListManager {
   public TargetManager() {
      super(new File(ValorPaths.CONFIG_DIR, "enemies.txt"), new Color(ChatColor.DARK_RED.rgb));
   }
}
