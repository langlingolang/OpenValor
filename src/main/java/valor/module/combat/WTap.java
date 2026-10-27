package valor.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.potion.Potion;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.util.TimerUtil;

public class WTap extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final TimerUtil attackCooldownTimer = new TimerUtil();
   private boolean isWTapping = false;
   private boolean isSlowing = false;
   private long delayRemainingMs = 0L;
   private long durationRemainingMs = 0L;
   public final FloatProperty delayProp = new FloatProperty("delay", 5.5F, 0.0F, 10.0F);
   public final FloatProperty durationProp = new FloatProperty("duration", 1.5F, 1.0F, 5.0F);

@EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled()
         && !var1.isCancelled()
         && var1.getType() == EventType.SEND
         && var1.getPacket() instanceof C02PacketUseEntity
         && ((C02PacketUseEntity)var1.getPacket()).getAction() == Action.ATTACK
         && !this.isWTapping
         && this.attackCooldownTimer.hasTimeElapsed(500L)
         && mc.thePlayer.isSprinting()) {
         this.attackCooldownTimer.reset();
         this.isWTapping = true;
         this.isSlowing = false;
         this.delayRemainingMs = this.delayRemainingMs + (long)(50.0F * (Float)this.delayProp.getValue());
         this.durationRemainingMs = this.durationRemainingMs + (long)(50.0F * (Float)this.durationProp.getValue());
      }
   }

   @EventTarget(4)
   public void onMoveInput(MoveInputEvent var1) {
      if (this.isWTapping) {
         if (!this.isSlowing
            && (
               mc.thePlayer.movementInput.moveForward < 0.8F
                  || mc.thePlayer.isCollidedHorizontally
                  || (float)mc.thePlayer.getFoodStats().getFoodLevel() <= 6.0F && !mc.thePlayer.capabilities.allowFlying
                  || !mc.thePlayer.isSprinting()
                     && (mc.thePlayer.isUsingItem() || mc.thePlayer.isPotionActive(Potion.blindness) || !mc.gameSettings.keyBindSprint.isKeyDown())
            )) {
            this.isWTapping = false;

            while (this.delayRemainingMs > 0L) {
               this.delayRemainingMs -= 50L;
            }

            while (this.durationRemainingMs > 0L) {
               this.durationRemainingMs -= 50L;
            }
         } else {
            if (this.delayRemainingMs > 0L) {
               this.delayRemainingMs -= 50L;
               return;
            }

            if (this.durationRemainingMs > 0L) {
               this.durationRemainingMs -= 50L;
               this.isSlowing = true;
               mc.thePlayer.movementInput.moveForward = 0.0F;
            }

            if (this.durationRemainingMs <= 0L) {
               this.isWTapping = false;
            }
         }
      }
   }

   public WTap() {
      super("WTap", false, ModuleCategory.COMBAT);
   }
}
