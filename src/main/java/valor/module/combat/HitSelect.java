package valor.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.Vec3;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.movement.KeepSprint;
import valor.setting.ModeProperty;

public class HitSelect extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"SECOND", "CRITICALS", "W_TAP"});

private boolean isSprinting = false;
   private boolean packetCancelled = false;
   private boolean waitingForHit = false;
   private double lastDistance = 0.0;
   private int wTapTicks = 0;
   private int cooldownTicks = 0;

   @Override
   public String[] getSuffix() {
      return new String[]{this.modeProp.getModeName()};
   }

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled()) {
         if (var1.getType() == EventType.POST) {
            this.restoreKeepSprint();
         }
      }
   }

   @EventTarget(0)
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.SEND && !var1.isCancelled()) {
         if (var1.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction var7 = (C0BPacketEntityAction)var1.getPacket();
            switch (var7.getAction()) {
               case START_SPRINTING:
                  this.isSprinting = true;
                  return;
               case STOP_SPRINTING:
                  this.isSprinting = false;
            }
         } else {
            if (var1.getPacket() instanceof C02PacketUseEntity) {
               C02PacketUseEntity var2;
               if ((var2 = (C02PacketUseEntity)var1.getPacket()).getAction() != Action.ATTACK) {
                  return;
               }

               Entity var5;
               if ((var5 = var2.getEntityFromWorld(mc.theWorld)) == null || var5 instanceof EntityLargeFireball) {
                  return;
               }

               if (!(var5 instanceof EntityLivingBase)) {
                  return;
               }

               EntityLivingBase var6 = (EntityLivingBase)var5;
               boolean var3 = true;
               switch (this.modeProp.getValue()) {
                  case 0:
                     EntityPlayerSP var10 = mc.thePlayer;
                     boolean var12;
                     if (var6.hurtTime != 0) {
                        var12 = true;
                     } else if (var10.hurtTime <= var10.maxHurtTime - 1) {
                        var12 = true;
                     } else if ((double)var10.getDistanceToEntity(var6) < 2.5) {
                        var12 = true;
                     } else if (!isFacingTarget(var6, var10)) {
                        var12 = true;
                     } else if (!isFacingTarget(var10, var6)) {
                        var12 = true;
                     } else {
                        this.overrideKeepSprint();
                        var12 = false;
                     }

                     var3 = var12;
                     break;
                  case 1:
                     EntityPlayerSP var9 = mc.thePlayer;
                     boolean var11;
                     if (var9.onGround) {
                        var11 = true;
                     } else if (var9.hurtTime != 0) {
                        var11 = true;
                     } else if (var9.fallDistance > 0.0F) {
                        var11 = true;
                     } else {
                        this.overrideKeepSprint();
                        var11 = false;
                     }

                     var3 = var11;
                     break;
                  case 2:
                     boolean var4 = this.isSprinting;
                     EntityPlayerSP var8 = mc.thePlayer;
                     boolean var10000;
                     if (var8.isCollidedHorizontally) {
                        var10000 = true;
                     } else if (!mc.gameSettings.keyBindForward.isKeyDown()) {
                        var10000 = true;
                     } else if (var4) {
                        var10000 = true;
                     } else {
                        this.overrideKeepSprint();
                        var10000 = false;
                     }

                     var3 = var10000;
               }

               if (!var3) {
                  var1.setCancelled(true);
                  this.wTapTicks++;
                  return;
               }

               this.cooldownTicks++;
            }
         }
      }
   }

   private static boolean isFacingTarget(EntityLivingBase var0, EntityLivingBase var1) {
      Vec3 var2 = var0.getPositionVector();
      Vec3 var15 = new Vec3(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ);
      Vec3 var16 = var1.getPositionVector();
      double var3 = var2.xCoord - var15.xCoord;
      double var5 = var2.zCoord - var15.zCoord;
      double var7;
      if ((var7 = Math.sqrt(var3 * var3 + var5 * var5)) == 0.0) {
         return false;
      } else {
         var3 /= var7;
         var5 /= var7;
         double var9 = var16.xCoord - var2.xCoord;
         double var11 = var16.zCoord - var2.zCoord;
         double var13;
         if ((var13 = Math.sqrt(var9 * var9 + var11 * var11)) == 0.0) {
            return false;
         } else {
            var9 /= var13;
            var11 /= var13;
            return var3 * var9 + var5 * var11 >= Math.cos(Math.toRadians(60.0));
         }
      }
   }

   @Override
   public void onDisabled() {
      this.restoreKeepSprint();
      this.isSprinting = false;
      this.packetCancelled = false;
      this.waitingForHit = false;
      this.lastDistance = 0.0;
      this.wTapTicks = 0;
      this.cooldownTicks = 0;
   }

   private void restoreKeepSprint() {
      if (this.packetCancelled) {
         KeepSprint var1;
         if ((var1 = (KeepSprint)Valor.moduleManager.modules.get(KeepSprint.class)) != null) {
            try {
               var1.slowdownProp.setValue((int)this.lastDistance);
               if (this.waitingForHit && var1.isEnabled()) {
                  var1.setEnabled(false);
               }
            } catch (Exception var2) {
               var2.printStackTrace();
            }

            this.packetCancelled = false;
            this.waitingForHit = false;
            this.lastDistance = 0.0;
         }
      }
   }

   public HitSelect() {
      super("HitSelect", false, ModuleCategory.COMBAT);
   }

   private void overrideKeepSprint() {
      if (!this.packetCancelled) {
         KeepSprint var1;
         if ((var1 = (KeepSprint)Valor.moduleManager.modules.get(KeepSprint.class)) != null) {
            try {
               this.lastDistance = ((Integer)var1.slowdownProp.getValue()).doubleValue();
               if (!var1.isEnabled()) {
                  var1.setEnabled(true);
                  this.waitingForHit = true;
               }

               var1.slowdownProp.setValue(0);
               this.packetCancelled = true;
            } catch (Exception var2) {
               var2.printStackTrace();
            }
         }
      }
   }
}
