package valor.module.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.lwjgl.input.Keyboard;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.ReflectionUtil;

public class Freecam extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final FloatProperty speedProp = new FloatProperty("speed", 2.5F, 0.5F, 10.0F);
   public final BooleanProperty disableOnDamageProp = new BooleanProperty("disable-on-damage", Boolean.TRUE);
   public final BooleanProperty allowDiggingProp = new BooleanProperty("allow-digging", Boolean.FALSE);
   public final BooleanProperty allowInteractingProp = new BooleanProperty("allow-interacting", Boolean.FALSE);
   public final BooleanProperty allowPlacingProp = new BooleanProperty("allow-placing", Boolean.FALSE);
   public final BooleanProperty showArmProp = new BooleanProperty("show-arm", Boolean.FALSE);

   public EntityOtherPlayerMP dummyPlayer = null;

   public float originalYaw;
   public float originalPitch;

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (this.isEnabled() && event.getType() == EventType.SEND) {
         if (!(Boolean) this.allowDiggingProp.getValue() && event.getPacket() instanceof C07PacketPlayerDigging) {
            event.setCancelled(true);
         }

         if (!(Boolean) this.allowPlacingProp.getValue() && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            event.setCancelled(true);
         }

         if (!(Boolean) this.allowInteractingProp.getValue() && event.getPacket() instanceof C02PacketUseEntity) {
            event.setCancelled(true);
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      EntityOtherPlayerMP player = this.dummyPlayer;
      if (this.isEnabled() && player != null) {
         if (!(Boolean) this.showArmProp.getValue()) {
            mc.thePlayer.renderArmPitch = mc.thePlayer.prevRenderArmPitch = 700.0F;
         }

         try {
            GlStateManager.pushMatrix();
            double renderX = ReflectionUtil.getRenderPosX(mc.getRenderManager());
            double renderY = ReflectionUtil.getRenderPosY(mc.getRenderManager());
            double renderZ = ReflectionUtil.getRenderPosZ(mc.getRenderManager());
            GlStateManager.translate(mc.thePlayer.posX - renderX, mc.thePlayer.posY - renderY, mc.thePlayer.posZ - renderZ);
            mc.getRenderManager().renderEntityStatic(mc.thePlayer, event.getPartialTicks(), false);
            GlStateManager.popMatrix();
         } catch (Exception ignored) {
         }
      }
   }

   @Override
   public void onDisabled() {
      EntityOtherPlayerMP player = this.dummyPlayer;
      if (player != null) {
         mc.setRenderViewEntity(mc.thePlayer);
         mc.thePlayer.rotationYaw = mc.thePlayer.rotationYawHead = this.originalYaw;
         mc.thePlayer.rotationPitch = this.originalPitch;
         mc.theWorld.removeEntity(player);
         this.dummyPlayer = null;
               }
   }

   @Override
   public void onEnabled() {
      if (mc.thePlayer != null && mc.thePlayer.onGround) {
         this.dummyPlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
                  this.dummyPlayer.copyLocationAndAnglesFrom(mc.thePlayer);
         this.originalYaw = mc.thePlayer.rotationYawHead;
         this.originalPitch = mc.thePlayer.rotationPitch;
                           this.dummyPlayer.rotationYawHead = this.originalYaw;
         this.dummyPlayer.setVelocity(0.0, 0.0, 0.0);
         this.dummyPlayer.setInvisible(true);
         mc.theWorld.addEntityToWorld(-8008, this.dummyPlayer);
         mc.setRenderViewEntity(this.dummyPlayer);
      } else {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (this.isEnabled() && event.getType() == EventType.PRE) {
         EntityOtherPlayerMP player = this.dummyPlayer;
         if (mc.thePlayer != null && player != null) {
            if ((Boolean) this.disableOnDamageProp.getValue() && mc.thePlayer.hurtTime != 0) {
               this.setEnabled(false);
            } else {
               mc.thePlayer.setSprinting(false);
               mc.thePlayer.moveForward = 0.0F;
               mc.thePlayer.moveStrafing = 0.0F;
               mc.thePlayer.setSneaking(false);
               player.rotationYaw = player.rotationYawHead = mc.thePlayer.rotationYaw;
               player.rotationPitch = mc.thePlayer.rotationPitch;
               double speed = 0.215 * (double) ((Float) this.speedProp.getValue()).floatValue();
               float yaw = player.rotationYawHead;

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                  player.posX += -Math.sin(Math.toRadians((double) yaw)) * speed;
                  player.posZ += Math.cos(Math.toRadians((double) yaw)) * speed;
               }

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                  player.posX -= -Math.sin(Math.toRadians((double) yaw)) * speed;
                  player.posZ -= Math.cos(Math.toRadians((double) yaw)) * speed;
               }

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
                  player.posX += -Math.sin(Math.toRadians((double) (yaw - 90.0F))) * speed;
                  player.posZ += Math.cos(Math.toRadians((double) (yaw - 90.0F))) * speed;
               }

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
                  player.posX += -Math.sin(Math.toRadians((double) (yaw + 90.0F))) * speed;
                  player.posZ += Math.cos(Math.toRadians((double) (yaw + 90.0F))) * speed;
               }

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                  player.posY += speed * 0.93;
               }

               if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                  player.posY -= speed * 0.93;
               }
            }
         }
      }
   }

   public Freecam() {
      super("Freecam", false, ModuleCategory.VISUAL);
   }
}
