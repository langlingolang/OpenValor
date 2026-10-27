package valor.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import valor.event.EventTarget;
import valor.events.HitBlockEvent;
import valor.events.LeftClickMouseEvent;
import valor.events.RightClickMouseEvent;
import valor.events.SwapItemEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.setting.PercentageProperty;
import valor.util.PacketUtil;
import valor.util.ReflectionUtil;
import valor.util.TimerUtil;

public class AutoHeal extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final TimerUtil healTimer = new TimerUtil();
   private boolean canHeal = false;
   private int originalSlot = -1;
   private int hurtTimeCounter = 0;
   public final PercentageProperty healthProp = new PercentageProperty("health", 35);
   public final IntProperty delayProp = new IntProperty("delay", 4000, 0, 5000);
   public final BooleanProperty regenCheckProp = new BooleanProperty("regen-check", Boolean.FALSE);
   public final BooleanProperty hurtCheckProp = new BooleanProperty("hurt-check", Boolean.FALSE);
   public final IntProperty hurtTimeProp = new IntProperty("hurt-time", 20, 1, 100, this.hurtCheckProp::getValue);

@EventTarget(1)
   public void onTick(TickEvent var1) {
      if (!this.isEnabled()) {
         this.originalSlot = -1;
      } else {
         if ((Boolean)this.hurtCheckProp.getValue()) {
            if (this.hurtTimeCounter > 0) {
               this.hurtTimeCounter--;
            }

            if (mc.thePlayer.hurtTime > 0) {
               this.hurtTimeCounter = (Integer)this.hurtTimeProp.getValue();
            }
         } else {
            this.hurtTimeCounter = 1;
         }

         switch (var1.getType()) {
            case PRE:
               boolean var5 = (float)Math.ceil((double)(mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()))
                     / mc.thePlayer.getMaxHealth()
                  <= (float)((Integer)this.healthProp.getValue()).intValue() / 100.0F;
               if (this.canHeal
                  && var5
                  && (!(Boolean)this.regenCheckProp.getValue() || !mc.thePlayer.isPotionActive(Potion.regeneration))
                  && this.healTimer.hasTimeElapsed((long)((Integer)this.delayProp.getValue()).intValue())
                  && this.hurtTimeCounter > 0) {
                   int var8 = this.findHealItemSlot();
                  if (var8 != -1) {
                     this.originalSlot = mc.thePlayer.inventory.currentItem;
                     mc.thePlayer.inventory.currentItem = var8;
                     ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                     PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                     this.healTimer.reset();
                  }
               }

               this.canHeal = var5;
               return;
            case POST:
               if (this.originalSlot != -1) {
                  mc.thePlayer.inventory.currentItem = this.originalSlot;
                  this.originalSlot = -1;
               }
         }
      }
   }

   @EventTarget
   public void onLeftClick(LeftClickMouseEvent var1) {
      if (this.isEnabled() && this.isHealing()) {
         var1.setCancelled(true);
      }
   }

   public final boolean isHealing() {
      return this.originalSlot != -1;
   }

   @EventTarget
   public void onHitBlock(HitBlockEvent var1) {
      if (this.isEnabled() && this.isHealing()) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onRightClick(RightClickMouseEvent var1) {
      if (this.isEnabled() && this.isHealing()) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onSwap(SwapItemEvent var1) {
      if (this.isEnabled() && this.isHealing()) {
         var1.setCancelled(true);
      }
   }

   public AutoHeal() {
      super("AutoHeal", false, ModuleCategory.PLAYER);
   }
   private int findHealItemSlot() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
         if (stack != null && stack.hasDisplayName()) {
            String name = stack.getDisplayName();
            if (stack.getItem() instanceof ItemSkull && name.contains("§6") && name.contains("Golden Head")) {
               return i;
            }
         }
      }

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
         if (stack != null && stack.hasDisplayName()) {
            String name = stack.getDisplayName();
            if (stack.getItem() instanceof ItemSkull && name.matches("\\S+§c's Head")) {
               return i;
            }
         }
      }

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
         if (stack != null && stack.hasDisplayName()) {
            String name = stack.getDisplayName();
            if (stack.getItem() instanceof ItemFood && name.contains("§6Cornucopia")) {
               return i;
            }
            if (stack.getItem() instanceof ItemSoup
               && (name.contains("§a") && (name.contains("Tasty Soup") || name.contains("Assist Soup")))) {
               return i;
            }
         }
      }

      return -1;
   }

}
