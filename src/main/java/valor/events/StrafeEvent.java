package valor.events;

import valor.event.events.Event;

public class StrafeEvent implements Event {
   private float strafe;
   private float forward;
   private float friction;

   public float getFriction() {
      return this.friction;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public float getForward() {
      return this.forward;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public void setFriction(float friction) {
      this.friction = friction;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public StrafeEvent(float strafe, float forward, float friction) {
      this.strafe = strafe;
      this.forward = forward;
      this.friction = friction;
   }
}
