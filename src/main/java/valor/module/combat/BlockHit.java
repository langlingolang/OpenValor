package valor.module.combat;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Mouse;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;

public class BlockHit extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final Random RANDOM = new Random();
   public final FloatProperty chanceProp = new FloatProperty("chance", 100.0F, 0.0F, 100.0F);
   public final FloatProperty delayProp = new FloatProperty("delay", 0.0F, 0.0F, 500.0F);
   public final FloatProperty holdProp = new FloatProperty("hold", 40.0F, 0.0F, 500.0F);

private long blockDelayTimeMs = -1L;
   private long blockHoldEndTimeMs = -1L;
   private boolean isBlocking = false;

   @Override
   public void onDisabled() {
      this.stopBlocking();
   }

   private void stopBlocking() {
      if (this.isBlocking) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
         this.isBlocking = false;
         this.blockHoldEndTimeMs = -1L;
         this.blockDelayTimeMs = -1L;
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (var1.getType() == EventType.PRE && mc.thePlayer != null && mc.theWorld != null) {
         if (this.isEnabled()) {
            long var3 = System.currentTimeMillis();
            if (this.blockDelayTimeMs != -1L && var3 >= this.blockDelayTimeMs) {
               if (!this.isBlocking) {
                  KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                  this.isBlocking = true;
               }

               this.blockDelayTimeMs = -1L;
               this.blockHoldEndTimeMs = var3 + ((Float)this.holdProp.getValue()).longValue();
            }

            if (this.blockHoldEndTimeMs != -1L && var3 >= this.blockHoldEndTimeMs) {
               this.stopBlocking();
            }

            if (mc.currentScreen == null && Mouse.isButtonDown(0)) {
               boolean canBlockHit = mc.thePlayer.getHeldItem() != null
                  && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                  && mc.objectMouseOver != null
                  && mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY
                  && mc.objectMouseOver.entityHit instanceof EntityLivingBase
                  && !((EntityLivingBase)mc.objectMouseOver.entityHit).isDead
                  && mc.thePlayer.swingProgressInt == 1
                  && RANDOM.nextFloat() * 100.0F <= (Float)this.chanceProp.getValue();

               if (canBlockHit && this.blockDelayTimeMs == -1L) {
                  this.blockDelayTimeMs = System.currentTimeMillis() + ((Float)this.delayProp.getValue()).longValue();
               }
            } else {
               this.blockDelayTimeMs = -1L;
            }
         }
      }
   }

   public BlockHit() {
      super("BlockHit", false, ModuleCategory.COMBAT);
   }
}
