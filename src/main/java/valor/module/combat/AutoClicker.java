package valor.module.combat;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LeftClickMouseEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.util.ItemUtil;
import valor.util.KeyUtil;
import valor.util.RandomUtil;
import valor.util.RotationUtil;
import valor.util.Win32MouseUtil;

public class AutoClicker extends Module {
   public static final Minecraft mc = Minecraft.getMinecraft();
private boolean clickPending = false;
   private long nextClickTimeMs = 0L;
   private long lastClickTimeMs = 0L;
   private long clickDelayMs = 0L;
   private int clickCount = 0;
   public final IntProperty minCpsProp = new IntProperty("min-cps", 8, 1, 20);
   public final IntProperty maxCpsProp = new IntProperty("max-cps", 12, 1, 20);
   public final BooleanProperty weaponsOnlyProp = new BooleanProperty("weapons-only", Boolean.TRUE);
   public final BooleanProperty allowToolsProp = new BooleanProperty("allow-tools", Boolean.FALSE, this.weaponsOnlyProp::getValue);
   public final BooleanProperty breakBlocksProp = new BooleanProperty("break-blocks", Boolean.FALSE);
   public final BooleanProperty forceBlockAnimProp = new BooleanProperty("force-block-anim", Boolean.FALSE, this.weaponsOnlyProp::getValue);
   public final FloatProperty rangeProp = new FloatProperty("range", 3.0F, 3.0F, 8.0F, this.breakBlocksProp::getValue);
   public final FloatProperty hitBoxVerticalProp = new FloatProperty("hit-box-vertical", 0.1F, 0.0F, 1.0F, this.breakBlocksProp::getValue);
   public final FloatProperty hitBoxHorizontalProp = new FloatProperty("hit-box-horizontal", 0.2F, 0.0F, 1.0F, this.breakBlocksProp::getValue);

@Override
   public String[] getSuffix() {
      return Objects.equals(this.minCpsProp.getValue(), this.maxCpsProp.getValue())
         ? new String[]{((Integer)this.minCpsProp.getValue()).toString()}
         : new String[]{String.format("%d-%d", this.minCpsProp.getValue(), this.maxCpsProp.getValue())};
   }

   @Override
   public void verifyValue(String var1) {
      if (this.minCpsProp.getName().equals(var1)) {
         if ((Integer)this.minCpsProp.getValue() > (Integer)this.maxCpsProp.getValue()) {
            this.maxCpsProp.setValue(this.minCpsProp.getValue());
            return;
         }
      } else if (this.maxCpsProp.getName().equals(var1) && (Integer)this.minCpsProp.getValue() > (Integer)this.maxCpsProp.getValue()) {
         this.minCpsProp.setValue(this.maxCpsProp.getValue());
      }
   }

   private long getRandomClickDelayMs() {
      return (long)(1000.0 / (double)RandomUtil.nextLong((long)((Integer)this.minCpsProp.getValue()).intValue(), (long)((Integer)this.maxCpsProp.getValue()).intValue()));
   }

   private boolean isTargetInReach(EntityPlayer var1) {
      if (var1 == mc.thePlayer || var1 == mc.thePlayer.ridingEntity) {
         return false;
      } else if (var1 == mc.getRenderViewEntity() || var1 == mc.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (var1.deathTime > 0) {
         return false;
      } else {
         float var2 = var1.getCollisionBorderSize();
         return RotationUtil.calculateBoxIntercept(
               var1.getEntityBoundingBox()
                  .expand(
                     (double)(var2 + (Float)this.hitBoxHorizontalProp.getValue()),
                     (double)(var2 + (Float)this.hitBoxVerticalProp.getValue()),
                     (double)(var2 + (Float)this.hitBoxHorizontalProp.getValue())
                  ),
               mc.thePlayer.rotationYaw,
               mc.thePlayer.rotationPitch,
               (double)((Float)this.rangeProp.getValue()).floatValue()
            )
            != null;
      }
   }

   @Override
   public void onEnabled() {
      this.nextClickTimeMs = 0L;
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (var1.getType() == EventType.PRE) {
         if (mc.currentScreen != null) {
            this.clickPending = false;
            return;
         }

         if (this.clickPending) {
            this.clickPending = false;
            KeyUtil.updateKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode());
         }

         int var5 = mc.gameSettings.keyBindAttack.getKeyCode();
         GameType var2;
         if (this.isEnabled()
            && (
               !this.weaponsOnlyProp.getValue() || ItemUtil.isHoldingWeapon() || this.allowToolsProp.getValue() && ItemUtil.isHoldingTool()
                  ? (
                     this.breakBlocksProp.getValue()
                           && isLookingAtBlock()
                           && !mc.theWorld
                              .loadedEntityList
                              .stream()
                              .filter(var0 -> var0 instanceof EntityPlayer)
                              .map(var0 -> (EntityPlayer)var0)
                              .anyMatch(this::isTargetInReach)
                        ? (var2 = mc.playerController.getCurrentGameType()) != GameType.SURVIVAL && var2 != GameType.CREATIVE
                        : true
                  )
                  : false
            )
            && KeyUtil.isKeyDown(var5)) {
            if (!mc.thePlayer.isUsingItem() || ItemUtil.isHoldingSword()) {
               long var3 = System.currentTimeMillis();
               if (this.nextClickTimeMs <= 0L) {
                  this.clickDelayMs = this.getRandomClickDelayMs();
                  this.clickCount = 0;
                  this.nextClickTimeMs = var3 + this.clickDelayMs;
               }

               for (int var6 = 0; var3 >= this.nextClickTimeMs && var6 < 5; var6++) {
                  if (this.clickCount % 5 == 0) {
                     this.clickDelayMs = this.getRandomClickDelayMs();
                  }

                  this.nextClickTimeMs = this.nextClickTimeMs + this.clickDelayMs;
                  this.clickCount++;
                  this.lastClickTimeMs = var3;
                  this.clickPending = true;
                  if (!isLookingAtBlock()) {
                     KeyBinding.setKeyBindState(var5, false);
                  }

                  KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
               }

               return;
            }
         } else {
            this.nextClickTimeMs = 0L;
         }
      }
   }

   @EventTarget(4)
   public void onCLick(LeftClickMouseEvent var1) {
      if (this.isEnabled() && !var1.isCancelled() && !this.clickPending) {
         this.nextClickTimeMs = System.currentTimeMillis() + this.getRandomClickDelayMs();
      }
   }

   private static boolean isLookingAtBlock() {
      return mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK;
   }

   public AutoClicker() {
      super("AutoClicker", false, ModuleCategory.COMBAT);
   }
}
