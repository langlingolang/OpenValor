package valor.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import valor.Valor;
import valor.core.NoSlowType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LivingUpdateEvent;
import valor.events.PacketEvent;
import valor.events.PlayerUpdateEvent;
import valor.events.RightClickMouseEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.setting.BooleanProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BlockUtil;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;
import valor.util.PacketUtil;
import valor.util.PlayerUtil;

public class NoSlow extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private int lastSlot = -1;
   private boolean isBlocking = false;

   public final ModeProperty swordModeProp = new ModeProperty("sword-mode", 1, new String[]{"NONE", "VANILLA", "SWITCH"});
   public final PercentageProperty swordMotionProp = new PercentageProperty("sword-motion", 100, () -> (Integer)this.swordModeProp.getValue() != 0);
   public final BooleanProperty swordSprintProp = new BooleanProperty("sword-sprint", Boolean.TRUE, () -> (Integer)this.swordModeProp.getValue() != 0);
   public final ModeProperty foodModeProp = new ModeProperty("food-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
   public final PercentageProperty foodMotionProp = new PercentageProperty("food-motion", 100, () -> (Integer)this.foodModeProp.getValue() != 0);
   public final BooleanProperty foodSprintProp = new BooleanProperty("food-sprint", Boolean.TRUE, () -> (Integer)this.foodModeProp.getValue() != 0);
   public final ModeProperty bowModeProp = new ModeProperty("bow-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
   public final PercentageProperty bowMotionProp = new PercentageProperty("bow-motion", 100, () -> (Integer)this.bowModeProp.getValue() != 0);
   public final BooleanProperty bowSprintProp = new BooleanProperty("bow-sprint", Boolean.TRUE, () -> (Integer)this.bowModeProp.getValue() != 0);

   @EventTarget(4)
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.SEND && !var1.isCancelled() && mc.thePlayer != null) {
         if (var1.getPacket() instanceof C08PacketPlayerBlockPlacement && ItemUtil.isHoldingSword()) {
            this.isBlocking = true;
         } else if (var1.getPacket() instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging)var1.getPacket()).getStatus() == Action.RELEASE_USE_ITEM) {
            this.isBlocking = false;
         } else if (var1.getPacket() instanceof C09PacketHeldItemChange) {
            this.isBlocking = false;
         } else {
            if (var1.getPacket() instanceof C03PacketPlayer && this.isBlocking && (Integer)this.swordModeProp.getValue() == 2 && ItemUtil.isHoldingSword() && !isKillAuraActive() && mc.thePlayer.isUsingItem()) {
               int var2 = mc.thePlayer.inventory.currentItem;
               var1.setCancelled(true);
               PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(findEmptyOrNextHotbarSlot(var2)));
               PacketUtil.sendPacketNoEvent(var1.getPacket());
               PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(var2));
               this.isBlocking = false;
            }
         }
      }
   }

   public static int findEmptyOrNextHotbarSlot(int var0) {
      for (int var1 = 0; var1 < 9; var1++) {
         if (var1 != var0 && mc.thePlayer.inventory.getStackInSlot(var1) == null) {
            return var1;
         }
      }

      return (var0 + 1) % 9;
   }

   public final boolean isBlockingSword() {
      KillAura var1 = (KillAura)Valor.moduleManager.modules.get(KillAura.class);
      return this.isBlocking || var1 != null && var1.isClientSideBlocking();
   }

   public boolean isSwordNoSlow() {
      return (Integer)this.swordModeProp.getValue() != 0 && ItemUtil.isHoldingSword();
   }

   public boolean isBowNoSlow() {
      return (Integer)this.bowModeProp.getValue() != 0 && ItemUtil.isHoldingBow();
   }

   public boolean isFloatActive() {
      return (Integer)this.foodModeProp.getValue() == 2 && ItemUtil.isHoldingEdible() || (Integer)this.bowModeProp.getValue() == 2 && ItemUtil.isHoldingBow();
   }

   @EventTarget(3)
   public void onPlayerUpdate(PlayerUpdateEvent var1) {
      if (this.isEnabled() && this.isFloatActive()) {
         int var2 = mc.thePlayer.inventory.currentItem;
         if (this.lastSlot != var2 && PlayerUtil.isUseItemPressed()) {
            this.lastSlot = var2;
            Valor.noSlowManager.setNoSlow(true, NoSlowType.VANILLA);
         }
      } else {
         this.lastSlot = -1;
         Valor.noSlowManager.setNoSlow(false, NoSlowType.VANILLA);
      }
   }

   public static boolean isKillAuraActive() {
      KillAura var0;
      return (var0 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)) != null && var0.isVanillaBlocking();
   }

   @Override
   public void onDisabled() {
      this.isBlocking = false;
   }

   @EventTarget
   public void onLivingUpdate(LivingUpdateEvent var1) {
      if (this.isEnabled() && this.shouldPreventSlowdown()) {
         float var2 = (float)(
               ItemUtil.isHoldingSword() ? (Integer)this.swordMotionProp.getValue() : (ItemUtil.isHoldingEdible() ? (Integer)this.foodMotionProp.getValue() : (ItemUtil.isHoldingBow() ? (Integer)this.bowMotionProp.getValue() : 100))
            )
            / 100.0F;
         mc.thePlayer.movementInput.moveForward *= var2;
         mc.thePlayer.movementInput.moveStrafe *= var2;
         if (isKillAuraActive() && this.isBlockingSword() && this.isSwordNoSlow()
            || (!this.isSwordNoSlow() || !(Boolean)this.swordSprintProp.getValue()) && (!this.isFoodNoSlow() || !(Boolean)this.foodSprintProp.getValue()) && (!this.isBowNoSlow() || !(Boolean)this.bowSprintProp.getValue())) {
            mc.thePlayer.setSprinting(false);
         }
      }
   }

   public final boolean shouldPreventSlowdown() {
      return mc.thePlayer.isUsingItem() && (this.isSwordNoSlow() || this.isFoodNoSlow() || this.isBowNoSlow());
   }

   public boolean isFoodNoSlow() {
      return (Integer)this.foodModeProp.getValue() != 0 && ItemUtil.isHoldingEdible();
   }

   @EventTarget
   public void onRightClick(RightClickMouseEvent var1) {
      if (this.isEnabled()) {
         if (mc.objectMouseOver != null) {
            switch (mc.objectMouseOver.typeOfHit) {
               case BLOCK:
                  if (BlockUtil.isInteractableBlock(mc.objectMouseOver.getBlockPos()) && !PlayerUtil.isSneakPressed()) {
                     return;
                  }
                  break;
               case ENTITY:
                  Entity var2 = mc.objectMouseOver.entityHit;
                  if (mc.objectMouseOver.entityHit instanceof EntityVillager) {
                     return;
                  }

                  if (var2 instanceof EntityLivingBase && BotCheckUtil.isEntityLoaded((EntityLivingBase)var2)) {
                     return;
                  }
            }
         }

         if (this.isFloatActive() && !Valor.noSlowManager.isNoSlow && mc.thePlayer.onGround) {
            var1.setCancelled(true);
            mc.thePlayer.motionY = 0.42F;
         }
      }
   }

   public NoSlow() {
      super("NoSlow", false, ModuleCategory.MOVEMENT);
   }
}
