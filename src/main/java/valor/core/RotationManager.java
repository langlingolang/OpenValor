package valor.core;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.TickEvent;

public class RotationManager {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public float lastPartialTicks = Float.NaN;
   public float deltaYaw = Float.NaN;
   public float deltaPitch = Float.NaN;
   public int priority = Integer.MIN_VALUE;
   public boolean silent = false;

   @EventTarget(0)
   public void onRender3D(Render3DEvent event) {
      this.applyRotation(event.getPartialTicks());
   }

   public final void setRotation(float targetYaw, float targetPitch, int priority, boolean silent) {
      if (this.priority <= priority) {
         this.priority = priority;
         this.deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
         this.deltaPitch = MathHelper.clamp_float(targetPitch - mc.thePlayer.rotationPitch, -90.0F, 90.0F);
         this.lastPartialTicks = 0.0F;
         this.silent = silent;
         this.applyRotation(0.0F);
      }
   }

   public void applyRotation(float partialTicks) {
      if (mc.thePlayer != null && !Float.isNaN(this.deltaYaw) && !Float.isNaN(this.deltaPitch) && !Float.isNaN(this.lastPartialTicks)) {
         float yawStep = this.deltaYaw * (partialTicks - this.lastPartialTicks);
         if (yawStep != 0.0F) {
            mc.thePlayer.prevRotationYaw = mc.thePlayer.rotationYaw;
            mc.thePlayer.rotationYaw += yawStep;
         }

         float pitchStep = this.deltaPitch * (partialTicks - this.lastPartialTicks);
         if (pitchStep != 0.0F) {
            mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch;
            mc.thePlayer.rotationPitch += pitchStep;
            mc.thePlayer.rotationPitch = MathHelper.clamp_float(mc.thePlayer.rotationPitch, -90.0F, 90.0F);
         }

         this.lastPartialTicks = partialTicks;
      }
   }

   @EventTarget(0)
   public void onTick(TickEvent event) {
      if (event.getType() == EventType.PRE) {
         this.applyRotation(1.0F);
         this.lastPartialTicks = Float.NaN;
         this.deltaYaw = Float.NaN;
         this.deltaPitch = Float.NaN;
         this.priority = Integer.MIN_VALUE;
         this.silent = false;
      }
   }
}
