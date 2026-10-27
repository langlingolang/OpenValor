package valor.module.world;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.AttackEvent;
import valor.events.LoadWorldEvent;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ModeProperty;
import valor.util.ChatUtil;
import valor.util.PacketUtil;
import valor.util.ReflectionUtil;

public class Disabler extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final ModeProperty modeProp = new ModeProperty(
      "mode", 0, new String[]{"BLOCKSMC", "MINE_LAND", "GRIM_SPEC", "HYPIXEL", "KK_CRAFT", "CUSTOM", "VERUS", "VULCAN", "MATRIX", "SPARTAN", "GRIM"}
   );
   public final BooleanProperty combatProp = new BooleanProperty("combat", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty waitUntilCombatProp = new BooleanProperty(
      "wait-until-combat", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 0 && (Boolean)this.combatProp.getValue()
   );
   public final BooleanProperty placeProp = new BooleanProperty("place", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty onlySpecProp = new BooleanProperty("only-spec", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 2);
   public final BooleanProperty waitForTeleportProp = new BooleanProperty("wait-for-teleport", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 2);
   public final BooleanProperty prop117Prop = new BooleanProperty("1.17", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 2);
   public final BooleanProperty sprintProp = new BooleanProperty("sprint", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 3);
   public final BooleanProperty autoBanProp = new BooleanProperty("auto-ban", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 4);
   public final BooleanProperty cancelC00Prop = new BooleanProperty("cancel-c00", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC03Prop = new BooleanProperty("cancel-c03", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC04Prop = new BooleanProperty("cancel-c04", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC05Prop = new BooleanProperty("cancel-c05", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC06Prop = new BooleanProperty("cancel-c06", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC08Prop = new BooleanProperty("cancel-c08", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC0aProp = new BooleanProperty("cancel-c0a", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC0bProp = new BooleanProperty("cancel-c0b", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC0fProp = new BooleanProperty("cancel-c0f", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty cancelC16Prop = new BooleanProperty("cancel-c16", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 5);
   public final BooleanProperty ongroundSpoofProp = new BooleanProperty("onground-spoof", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 7);
   public final BooleanProperty cancelSwingProp = new BooleanProperty("cancel-swing", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 7);
   public final BooleanProperty doublePosProp = new BooleanProperty("double-pos", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 8);
   public final BooleanProperty grimAckProp = new BooleanProperty("grim-ack", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 10);

   private boolean inCombat = false;
   private boolean teleported = false;
   private boolean hasPlacedBlock = false;
   private final ConcurrentLinkedQueue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
   private int autoBanStage = 0;
   private int autoBanDelayTicks = 0;
   private boolean isAttacking = false;
   private int verusTickCounter = 0;
      private Field transactionUidField;

   @Override
   public String getDescription() {
      return "Manipulates outgoing/incoming packets to bypass anti-cheat checks";
   }

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if ((Integer)this.modeProp.getValue() == 3 && (Boolean)this.sprintProp.getValue() && mc.thePlayer != null) {
             boolean canSprint = mc.thePlayer != null && (
                mc.thePlayer.capabilities.allowFlying || (
                   mc.thePlayer.getFoodStats().getFoodLevel() > 6
                   && !mc.thePlayer.isPotionActive(Potion.moveSpeed)
                )
             );

             if (!canSprint && mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(false);
             }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && !var1.isCancelled()) {
         Packet var2 = var1.getPacket();
         switch (this.modeProp.getValue()) {
            case 0:
               if (var1.getType() == EventType.SEND) {
                  if ((Boolean)this.placeProp.getValue() && var2 instanceof C08PacketPlayerBlockPlacement) {
                     C08PacketPlayerBlockPlacement var7;
                     if ((var7 = (C08PacketPlayerBlockPlacement)var2).getStack() != null) {
                        PacketUtil.sendPacketNoEvent(
                           new C08PacketPlayerBlockPlacement(
                              var7.getPosition(),
                              var7.getPlacedBlockDirection(),
                              null,
                              var7.getPlacedBlockOffsetX(),
                              var7.getPlacedBlockOffsetY(),
                              var7.getPlacedBlockOffsetZ()
                           )
                        );
                        var1.setCancelled(true);
                     }

                     return;
                  }

                  if ((Boolean)this.combatProp.getValue() && var2 instanceof C0FPacketConfirmTransaction) {
                     if (mc.thePlayer == null || mc.thePlayer.ticksExisted <= 20) {
                        this.teleported = false;
                        return;
                     }

                     if (!(Boolean)this.waitUntilCombatProp.getValue() || this.teleported) {
                        C0FPacketConfirmTransaction var10001 = (C0FPacketConfirmTransaction)var2;
                        short var4 = (short)(this.inCombat ? -1 : 1);
                        C0FPacketConfirmTransaction var10 = var10001;
                        Field var9;
                        if ((var9 = this.getTransactionUidField()) != null) {
                           try {
                              var9.setShort(var10, var4);
                           } catch (Throwable var5) {
                           }
                        }

                        this.inCombat = !this.inCombat;
                     }
                  }
               }

               return;
            case 1:
               if (var1.getType() == EventType.SEND) {
                  if (var2 instanceof C0FPacketConfirmTransaction) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2.getClass() == C03PacketPlayer.class) {
                     var1.setCancelled(true);
                  }
               }

               return;
            case 2:
               if (var1.getType() == EventType.RECEIVE) {
                  if (!(var2 instanceof S08PacketPlayerPosLook)) {
                     if (this.hasPlacedBlock && var2 instanceof S32PacketConfirmTransaction) {
                        this.packetQueue.add(var2);
                        var1.setCancelled(true);
                        if ((Boolean)this.prop117Prop.getValue()) {
                           PacketUtil.sendPacketNoEvent(new C0FPacketConfirmTransaction(0, (short)0, true));
                        }
                     }

                     return;
                  }

                  if ((!(Boolean)this.onlySpecProp.getValue() || mc.thePlayer != null && mc.thePlayer.capabilities.allowFlying) && !this.hasPlacedBlock) {
                     this.hasPlacedBlock = true;
                     return;
                  }

                  if (this.hasPlacedBlock) {
                     ChatUtil.addFormattedChatMessage("&c[Disabler] Flag detected! You may need to disable the Disabler.");
                  }

                  return;
               }

               if (var1.getType() == EventType.SEND && this.hasPlacedBlock && var2 instanceof C0FPacketConfirmTransaction) {
                  var1.setCancelled(true);
               }

               return;
            case 4:
               if (var1.getType() == EventType.SEND && var2 instanceof C0FPacketConfirmTransaction) {
                  var1.setCancelled(true);
               }

               return;
            case 5:
               if (var1.getType() == EventType.SEND) {
                  if (var2 instanceof C00PacketKeepAlive && (Boolean)this.cancelC00Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2.getClass() == C03PacketPlayer.class && (Boolean)this.cancelC03Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C04PacketPlayerPosition && (Boolean)this.cancelC04Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C05PacketPlayerLook && (Boolean)this.cancelC05Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C06PacketPlayerPosLook && (Boolean)this.cancelC06Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C08PacketPlayerBlockPlacement && (Boolean)this.cancelC08Prop.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C0APacketAnimation && (Boolean)this.cancelC0aProp.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C0BPacketEntityAction && (Boolean)this.cancelC0bProp.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C0FPacketConfirmTransaction && (Boolean)this.cancelC0fProp.getValue()) {
                     var1.setCancelled(true);
                     return;
                  }

                  if (var2 instanceof C16PacketClientStatus && (Boolean)this.cancelC16Prop.getValue()) {
                     var1.setCancelled(true);
                  }
               }

               return;
            case 6:
               if (var1.getType() == EventType.SEND) {
                  if (var2 instanceof C0FPacketConfirmTransaction) {
                     var1.setCancelled(true);
                     return;
                  } else if (var2 instanceof C00PacketKeepAlive) {
                     var1.setCancelled(true);
                  }
               }
            case 3:
            default:
               return;
            case 7:
               if (var1.getType() == EventType.SEND) {
                  if ((Boolean)this.ongroundSpoofProp.getValue() && var2 instanceof C03PacketPlayer) {
                     ReflectionUtil.setOnGround((C03PacketPlayer)var2, true);
                  }

                  if ((Boolean)this.cancelSwingProp.getValue() && var2 instanceof C0APacketAnimation) {
                     var1.setCancelled(true);
                  }
               }

               return;
            case 8:
               if (var1.getType() == EventType.SEND && (Boolean)this.doublePosProp.getValue() && var2 instanceof C04PacketPlayerPosition) {
                  C04PacketPlayerPosition var6 = (C04PacketPlayerPosition)var2;
                  PacketUtil.sendPacketNoEvent(new C04PacketPlayerPosition(var6.getPositionX(), var6.getPositionY(), var6.getPositionZ(), var6.isOnGround()));
               }

               return;
            case 9:
               if (var1.getType() == EventType.SEND && var2 instanceof C0FPacketConfirmTransaction) {
                  var1.setCancelled(true);
               }

               return;
            case 10:
               if (var1.getType() == EventType.SEND && var2 instanceof C0FPacketConfirmTransaction) {
                  var1.setCancelled(true);
                  if ((Boolean)this.grimAckProp.getValue() && mc.thePlayer != null) {
                     PacketUtil.sendPacketNoEvent(
                        new C06PacketPlayerPosLook(
                           mc.thePlayer.posX,
                           mc.thePlayer.getEntityBoundingBox().minY,
                           mc.thePlayer.posZ,
                           mc.thePlayer.rotationYaw,
                           mc.thePlayer.rotationPitch,
                           mc.thePlayer.onGround
                        )
                     );
                  }
               }
         }
      }
   }

   @Override
   public void verifyValue(String var1) {
      if (this.isEnabled() && "mode".equals(var1)) {
         this.flushPacketQueue();
         this.resetDisablerState();
      }
   }

   public void flushPacketQueue() {
      for (Iterator var1 = this.packetQueue.iterator(); var1.hasNext(); var1.remove()) {
         Packet var2;
         if ((var2 = (Packet)var1.next()) instanceof S32PacketConfirmTransaction && mc.getNetHandler() != null) {
            try {
               ((S32PacketConfirmTransaction)var2).processPacket((INetHandlerPlayClient)mc.getNetHandler());
            } catch (Throwable var3) {
            }
         }
      }
   }

   public Field getTransactionUidField() {
      if (this.transactionUidField != null) {
         return this.transactionUidField;
      } else {
         for (Class<?> var1 = C0FPacketConfirmTransaction.class; var1 != null; var1 = var1.getSuperclass()) {
            try {
               Field var7;
               (var7 = var1.getDeclaredField("field_149336_a")).setAccessible(true);
               this.transactionUidField = var7;
                              return var7;
            } catch (NoSuchFieldException var5) {
               try {
                  Field var6;
                  (var6 = var1.getDeclaredField("uid")).setAccessible(true);
                  this.transactionUidField = var6;
                                    return var6;
               } catch (NoSuchFieldException var4) {
                  try {
                     Field var2;
                     (var2 = var1.getDeclaredField("b")).setAccessible(true);
                     this.transactionUidField = var2;
                                          return var2;
                  } catch (NoSuchFieldException var3) {
                  }
               }
            }
         }

         return null;
      }
   }

   @Override
   public void onDisabled() {
      this.flushPacketQueue();
      this.resetDisablerState();
   }

   @Override
   public void onEnabled() {
      this.resetDisablerState();
   }

   public void resetDisablerState() {
      this.inCombat = false;
      this.teleported = false;
      this.hasPlacedBlock = false;
      this.packetQueue.clear();
      this.autoBanStage = 0;
      this.autoBanDelayTicks = 20;
      this.isAttacking = false;
      this.verusTickCounter = 0;
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         switch (this.modeProp.getValue()) {
            case 4:
               if (mc.thePlayer != null && mc.playerController != null) {
                  if (this.autoBanDelayTicks > 0) {
                     this.autoBanDelayTicks--;
                                 }

                  if (this.autoBanStage == 1 && !(mc.currentScreen instanceof GuiChest)) {
                     if (mc.thePlayer.inventory.currentItem != 0) {
                        mc.thePlayer.inventory.currentItem = 0;
                     } else if (mc.thePlayer.getHeldItem() != null) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                     }
                  } else if (this.autoBanStage == 1 && mc.currentScreen instanceof GuiChest) {
                     mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 32, 0, 1, mc.thePlayer);
                  }

                  if (this.autoBanStage == 0 && (Boolean)this.autoBanProp.getValue() && !this.isAttacking && this.autoBanDelayTicks == 0) {
                     for (int var2 = 0; var2 < 6; var2++) {
                        PacketUtil.sendPacketNoEvent(new C06PacketPlayerPosLook());
                     }

                     this.isAttacking = true;
                  }

                  return;
               } else {
                  return;
               }
            case 6:
               this.verusTickCounter++;
                        if (this.verusTickCounter >= 20) {
                  this.verusTickCounter = 0;
                  PacketUtil.sendPacketNoEvent(new C0FPacketConfirmTransaction(0, (short)0, true));
               }
         }
      }
   }

   @EventTarget
   public void onAttack(AttackEvent var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 0) {
         this.teleported = true;
      }
   }

   @EventTarget
   public void onWorldLoad(LoadWorldEvent var1) {
      if (this.isEnabled()) {
         this.resetDisablerState();
      }
   }

   public Disabler() {
      super("Disabler", false, ModuleCategory.WORLD);
   }
}
