package valor.module.movement;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.AxisAlignedBB;
import valor.Valor;
import valor.core.BlinkType;
import valor.event.EventTarget;
import valor.events.KeyEvent;
import valor.events.LivingUpdateEvent;
import valor.events.PlayerUpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.PlayerUtil;
import valor.util.RandomUtil;

public class AntiVoid extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int MODE_BLINK = 0;
   private static final int MODE_AIRWALK = 1;

   private boolean isOverVoid = false;
   private boolean wasOverVoid = false;
   private double[] lastSafePos = null;
   private double lastGroundY = 0.0;
   private double airWalkY = Double.NaN;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"BLINK", "AIRWALK"});
   public final FloatProperty distanceProp = new FloatProperty("distance", 5.0F, 0.0F, 16.0F, () -> (Integer)this.modeProp.getValue() == 0);

   @EventTarget
   public void onKey(KeyEvent var1) {
      ItemStack var2;
      if (var1.getKey() == mc.gameSettings.keyBindUseItem.getKeyCode()
         && (var2 = mc.thePlayer.inventory.getCurrentItem()) != null
         && var2.getItem() instanceof ItemEnderPearl) {
         this.resetBlink();
      }
   }

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(4)
   public void onUpdate(PlayerUpdateEvent var1) {
      if (this.isEnabled() && mc.thePlayer != null) {
         this.isOverVoid = !mc.thePlayer.capabilities.allowFlying && PlayerUtil.isOverVoid();
         if ((Integer)this.modeProp.getValue() == 0) {
            if (!this.isOverVoid) {
               this.resetBlink();
            }

            if (this.lastSafePos != null) {
               float var2 = mc.thePlayer.width / 2.0F;
               float var3 = mc.thePlayer.height;
               if (PlayerUtil.isOverVoid(
                  new AxisAlignedBB(
                     this.lastSafePos[0] - (double)var2,
                     this.lastSafePos[1],
                     this.lastSafePos[2] - (double)var2,
                     this.lastSafePos[0] + (double)var2,
                     this.lastSafePos[1] + (double)var3,
                     this.lastSafePos[2] + (double)var2
                  )
               )) {
                  this.resetBlink();
               }
            }

            if (!this.wasOverVoid && this.isOverVoid && canAntiVoid()) {
               Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
               if (Valor.blinkManager.setBlinking(true, BlinkType.MANUAL)) {
                  this.lastSafePos = new double[]{mc.thePlayer.prevPosX, mc.thePlayer.prevPosY, mc.thePlayer.prevPosZ};
               }
            }

            if (Valor.blinkManager.currentType == BlinkType.MANUAL && this.lastSafePos != null && this.lastSafePos[1] - (double)((Float)this.distanceProp.getValue()).floatValue() > mc.thePlayer.posY) {
               Valor.blinkManager.packetQueue.offerFirst(new C04PacketPlayerPosition(this.lastSafePos[0], this.lastSafePos[1] - RandomUtil.nextDouble(10.0, 20.0), this.lastSafePos[2], false));
               this.resetBlink();
            }
         } else if ((Integer)this.modeProp.getValue() == 1) {
            if (!this.isOverVoid) {
               if (mc.thePlayer.onGround) {
                  this.lastGroundY = mc.thePlayer.posY;
               }

               this.airWalkY = Double.NaN;
            } else if (canAntiVoid()) {
               if (Double.isNaN(this.airWalkY)) {
                  this.airWalkY = this.lastGroundY;
               }

               applyAirWalk(this.airWalkY);
            }
         }

         this.wasOverVoid = this.isOverVoid;
      }
   }

   private static boolean canAntiVoid() {
      return !((LongJump)Valor.moduleManager.modules.get(LongJump.class)).isLongJumping;
   }

   @Override
   public void verifyValue(String var1) {
      if (this.isEnabled()) {
         this.onDisabled();
         this.onEnabled();
      }
   }

   private void resetBlink() {
      Valor.blinkManager.setBlinking(false, BlinkType.MANUAL);
      this.lastSafePos = null;
   }

   private static void applyAirWalk(double targetY) {
      mc.thePlayer.fallDistance = 0.0F;
      if (mc.thePlayer.posY <= targetY + 0.125) {
         if (mc.thePlayer.motionY < 0.0) {
            mc.thePlayer.motionY = 0.0;
         }

         mc.thePlayer.onGround = true;
         if (mc.thePlayer.posY < targetY - 0.01) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, targetY, mc.thePlayer.posZ);
         }
      }
   }

   @Override
   public void onDisabled() {
      Valor.blinkManager.setBlinking(false, BlinkType.MANUAL);
      this.airWalkY = Double.NaN;
   }

   @Override
   public void onEnabled() {
      this.isOverVoid = false;
      this.wasOverVoid = false;
      this.lastGroundY = mc.thePlayer != null ? mc.thePlayer.posY : 0.0;
      this.resetBlink();
      this.airWalkY = Double.NaN;
   }

   @EventTarget(1)
   public void onLivingUpdate(LivingUpdateEvent var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 1 && mc.thePlayer != null) {
         if (this.isOverVoid && !Double.isNaN(this.airWalkY) && canAntiVoid()) {
            applyAirWalk(this.airWalkY);
         }
      }
   }

   public AntiVoid() {
      super("AntiVoid", false, ModuleCategory.MOVEMENT);
   }
}
