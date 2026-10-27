package valor.core;

import java.awt.Color;
import java.io.File;
import valor.util.ChatColor;
import valor.util.ValorPaths;

public class FriendManager extends PlayerListManager {
   public FriendManager() {
      super(new File(ValorPaths.CONFIG_DIR, "friends.txt"), new Color(ChatColor.GRAY.rgb));
   }
}
