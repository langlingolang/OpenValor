package valor.module.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;

public class GhostHand extends Module {
   public final BooleanProperty teamOnlyProp = new BooleanProperty("team-only", Boolean.TRUE);
   public final BooleanProperty ignoreWeaponsProp = new BooleanProperty("ignore-weapons", Boolean.FALSE);

   public boolean shouldIgnoreEntity(Entity var1) {
      return var1 instanceof EntityPlayer
         && !BotCheckUtil.isBot((EntityPlayer)var1)
         && (!(Boolean)this.teamOnlyProp.getValue() || BotCheckUtil.isTeammate((EntityPlayer)var1))
         && (!(Boolean)this.ignoreWeaponsProp.getValue() || !ItemUtil.isHoldingWeapon());
   }

   public GhostHand() {
      super("GhostHand", false, ModuleCategory.PLAYER);
   }
}
