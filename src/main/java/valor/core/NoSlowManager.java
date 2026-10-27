package valor.core;

import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;
import valor.event.EventTarget;
import valor.events.PlayerUpdateEvent;

public class NoSlowManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final LinkedHashMap<NoSlowType, Boolean> noSlowMap = new LinkedHashMap<>();
    public boolean isNoSlow = false;

    public final void setNoSlow(boolean enable, NoSlowType type) {
        this.noSlowMap.put(type, enable);
    }

    @EventTarget
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if ((this.noSlowMap.containsValue(Boolean.TRUE) || this.isNoSlow) && mc.thePlayer != null && mc.thePlayer.onGround && mc.thePlayer.posY - mc.thePlayer.lastTickPosY < 0.0 && mc.thePlayer.motionY < 0.0) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.001, mc.thePlayer.posZ);
            this.isNoSlow = true;
            return;
        }
        this.isNoSlow = false;
    }
}
