package valor.module.movement;

import com.google.common.base.CaseFormat;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import valor.Valor;
import valor.core.BlinkType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.ChatUtil;
import valor.util.PacketUtil;
import valor.util.PlayerUtil;
import valor.util.ReflectionUtil;
import valor.util.TeamsUtil;
import valor.util.TimerUtil;

public class NoFall extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final TimerUtil delayTimer = new TimerUtil();
   private final TimerUtil teamCombatTimer = new TimerUtil();
   private boolean packetSpoofActive = false;
   private boolean canBlinkNoFall = false;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"PACKET", "BLINK", "NO_GROUND", "SPOOF"});
   public final FloatProperty distanceProp = new FloatProperty("distance", 3.0F, 0.0F, 20.0F);
   public final IntProperty delayProp = new IntProperty("delay", 0, 0, 10000);

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(1)
   public void onPacket(PacketEvent var1) {
      if (var1.getType() == EventType.RECEIVE && var1.getPacket() instanceof S08PacketPlayerPosLook) {
         this.onDisabled();
      } else {
         if (this.isEnabled() && var1.getType() == EventType.SEND && !var1.isCancelled() && var1.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer var5 = (C03PacketPlayer)var1.getPacket();
            switch ((Integer)this.modeProp.getValue()) {
               case 0:
                  if (this.packetSpoofActive) {
                     this.packetSpoofActive = false;
                     ReflectionUtil.getTimer(mc).timerSpeed = 1.0F;
                     return;
                  }

                  if (!var5.isOnGround()) {
                     AxisAlignedBB var6 = mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
                     if (PlayerUtil.willTakeFallDamage((Float)this.distanceProp.getValue()) && !PlayerUtil.isOverVoid(var6) && this.canApplyNoFall()) {
                        this.delayTimer.reset();
                        this.packetSpoofActive = true;
                        ReflectionUtil.getTimer(mc).timerSpeed = 0.5F;
                     }

                     return;
                  }
                  break;
               case 1:
                  boolean var2 = !mc.thePlayer.isOnLadder() && !mc.thePlayer.capabilities.allowFlying && mc.thePlayer.hurtTime == 0;
                  if (Valor.blinkManager.currentType != BlinkType.BLOCK_IN) {
                     if (this.canBlinkNoFall && !var5.isOnGround() && var2 && PlayerUtil.isBlockAirBelow(((Float)this.distanceProp.getValue()).intValue()) && mc.thePlayer.motionY < 0.0) {
                        Valor.blinkManager.setBlinking(false, Valor.blinkManager.currentType);
                        Valor.blinkManager.setBlinking(true, BlinkType.BLOCK_IN);
                     }
                  } else if (!var2) {
                     Valor.blinkManager.setBlinking(false, BlinkType.BLOCK_IN);
                     ChatUtil.addFormattedChatMessage(String.format("%s%s: &cFailed player check!&r", Valor.CHAT_PREFIX, this.getName()));
                  } else if (PlayerUtil.isOverVoid(mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0))) {
                     Valor.blinkManager.setBlinking(false, BlinkType.BLOCK_IN);
                     ChatUtil.addFormattedChatMessage(String.format("%s%s: &cFailed void check!&r", Valor.CHAT_PREFIX, this.getName()));
                  } else if (var5.isOnGround()) {
                     Iterator var7 = Valor.blinkManager.packetQueue.iterator();

                     while (var7.hasNext()) {
                        Packet var4;
                        if ((var4 = (Packet)var7.next()) instanceof C03PacketPlayer) {
                           ReflectionUtil.setOnGround((C03PacketPlayer)var4, true);
                        }
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.BLOCK_IN);
                     this.delayTimer.reset();
                  }

                  this.canBlinkNoFall = var5.isOnGround() && var2 && this.canApplyNoFall();
                  return;
               case 2:
                  ReflectionUtil.setOnGround(var5, false);
                  return;
               case 3:
                  if (!var5.isOnGround()) {
                     AxisAlignedBB var3 = mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
                     if (PlayerUtil.willTakeFallDamage((Float)this.distanceProp.getValue()) && !PlayerUtil.isOverVoid(var3) && this.canApplyNoFall()) {
                        this.delayTimer.reset();
                        ReflectionUtil.setOnGround(var5, true);
                        mc.thePlayer.fallDistance = 0.0F;
                     }
                  }
            }
         }
      }
   }

   @Override
   public void verifyValue(String var1) {
      if (this.isEnabled()) {
         this.onDisabled();
      }
   }

   public boolean canApplyNoFall() {
      return this.teamCombatTimer.hasTimeElapsed(3000L) && this.delayTimer.hasTimeElapsed(((Integer)this.delayProp.getValue()).longValue());
   }

   @Override
   public void onDisabled() {
      this.canBlinkNoFall = false;
      Valor.blinkManager.setBlinking(false, BlinkType.BLOCK_IN);
      if (this.packetSpoofActive) {
         this.packetSpoofActive = false;
         ReflectionUtil.getTimer(mc).timerSpeed = 1.0F;
      }
   }

   @EventTarget(0)
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (TeamsUtil.isInGameLobby()) {
            this.teamCombatTimer.reset();
         }

         if ((Integer)this.modeProp.getValue() == 0 && this.packetSpoofActive) {
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer(true));
            mc.thePlayer.fallDistance = 0.0F;
         }
      }
   }

   public NoFall() {
      super("NoFall", false, ModuleCategory.MOVEMENT);
   }
}
