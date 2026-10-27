package valor.events;

import valor.event.events.Event;
import valor.event.types.EventType;

public class UpdateEvent implements Event {
   private final EventType type;
   private final float yaw;
   private final float pitch;
   private float newYaw;
   private float newPitch;
   private float prevYaw;
   private int rotationPriority = -1;
   private int prevRotationPriority = -1;
   private boolean rotated = false;

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public boolean isRotated() {
      return this.rotated;
   }

   public void setRotation(float newYaw, float newPitch, int priority) {
      if (this.type == EventType.PRE && this.rotationPriority <= priority) {
         this.newYaw = newYaw;
         this.newPitch = newPitch;
         this.rotationPriority = priority;
         this.rotated = true;
      }
   }

   public float getNewPitch() {
      return this.newPitch;
   }

   public float getNewYaw() {
      return this.newYaw;
   }

   public int isRotating() {
      return this.prevRotationPriority;
   }

   public void setPervRotation(float prevYaw, int priority) {
      if (this.type == EventType.PRE && this.prevRotationPriority < priority) {
         this.prevYaw = prevYaw;
         this.prevRotationPriority = priority;
         this.rotated = true;
      }
   }

   public float getPreYaw() {
      return this.prevYaw;
   }

   public UpdateEvent(EventType type, float yaw, float pitch, float newYaw, float newPitch) {
      this.type = type;
      this.yaw = yaw;
      this.pitch = pitch;
      this.newYaw = newYaw;
      this.newPitch = newPitch;
      this.prevYaw = newYaw;
   }

   public EventType getType() {
      return this.type;
   }
}
