package valor.module.player;

import com.google.common.base.CaseFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.movement.Sprint;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.ui.impl.clickgui.material.MaterialClickGui;
import valor.util.KeyUtil;
import valor.util.PacketUtil;

public class InvWalk extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final Queue<C0EPacketClickWindow> clickPacketQueue = new ConcurrentLinkedQueue<>();
   private boolean keysPressed = false;
   private C16PacketClientStatus pendingOpenPacket = null;
   private int hypixelCooldownTicks = 0;
   private int openDelayTicks = -1;
   private int closeDelayTicks = -1;
   private final Map<KeyBinding, Boolean> keyStateMap = new HashMap<KeyBinding, Boolean>() {
      {
         this.put(mc.gameSettings.keyBindForward, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindBack, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindLeft, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindRight, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindJump, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindSneak, Boolean.FALSE);
         this.put(mc.gameSettings.keyBindSprint, Boolean.FALSE);
      }
   };

   public final ModeProperty modeProp = new ModeProperty("mode", 1, new String[]{"VANILLA", "LEGIT", "HYPIXEL", "LEGIT+"});
   public final BooleanProperty clickGuiProp = new BooleanProperty("click-gui", Boolean.TRUE);
   public final IntProperty openDelayProp = new IntProperty("open-delay", 0, 0, 20, () -> (Integer)this.modeProp.getValue() == 3);
   public final IntProperty closeDelayProp = new IntProperty("close-delay", 4, 0, 20, () -> (Integer)this.modeProp.getValue() == 3);
   public final BooleanProperty lockMoveDeyProp = new BooleanProperty("lock-move-dey", Boolean.FALSE);

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(4)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled()) {
         if (mc.currentScreen instanceof MaterialClickGui) {
            if ((Boolean)this.clickGuiProp.getValue()) {
               this.updateMovementKeys(true);
            }
         } else {
            boolean allowMove = false;
            if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiContainerCreative)) {
               switch ((Integer)this.modeProp.getValue()) {
                  case 0:
                     allowMove = true;
                     break;
                  case 1:
                     if (mc.currentScreen instanceof GuiInventory) {
                        allowMove = this.pendingOpenPacket != null && this.clickPacketQueue.isEmpty();
                     }
                     break;
                  case 2:
                     allowMove = this.hypixelCooldownTicks == 0 && this.clickPacketQueue.isEmpty();
                     break;
                  case 3:
                     if (mc.currentScreen instanceof GuiInventory) {
                        allowMove = this.closeDelayTicks == -1 && this.clickPacketQueue.isEmpty();
                     }
                     break;
               }
            }

            if (!allowMove) {
               if (this.keysPressed) {
                  if (mc.currentScreen != null) {
                     KeyBinding.unPressAllKeys();
                  } else if (this.hasAnyMovementKeyPressed()) {
                     this.keyStateMap.replaceAll((var0, var1x) -> Boolean.FALSE);
                     this.updateMovementKeys(false);
                  }

                  this.keysPressed = false;
               }

               if (this.pendingOpenPacket != null) {
                  PacketUtil.sendPacketNoEvent(this.pendingOpenPacket);
                  this.pendingOpenPacket = null;
               }

               if (this.hypixelCooldownTicks > 0) {
                  this.hypixelCooldownTicks--;
               }
            } else if (this.hasAnyMovementKeyPressed() && (Boolean)this.lockMoveDeyProp.getValue()) {
               Iterator var2 = this.keyStateMap.entrySet().iterator();

               while (var2.hasNext()) {
                  Entry var3;
                  KeyBinding.setKeyBindState(((KeyBinding)(var3 = (Entry)var2.next()).getKey()).getKeyCode(), (Boolean)var3.getValue());
               }

               if (Valor.moduleManager.modules.get(Sprint.class).isEnabled()) {
                  KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
               }

               this.keysPressed = true;
            } else {
               this.updateMovementKeys(true);
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.SEND) {
         if (var1.getPacket() instanceof C16PacketClientStatus) {
            this.keyStateMap.replaceAll((var0, var1x) -> KeyUtil.isKeyDown(var0.getKeyCode()));
            if ((Integer)this.modeProp.getValue() == 1 || (Integer)this.modeProp.getValue() == 3) {
               C16PacketClientStatus var2 = (C16PacketClientStatus)var1.getPacket();
               if (var2.getStatus() == EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                  var1.setCancelled(true);
                  if ((Integer)this.modeProp.getValue() == 1) {
                     this.pendingOpenPacket = var2;
                  }
               }

               return;
            }
         } else if (!(var1.getPacket() instanceof C0EPacketClickWindow)) {
            if (var1.getPacket() instanceof C0DPacketCloseWindow) {
               if ((Integer)this.modeProp.getValue() == 1) {
                  if (this.pendingOpenPacket != null) {
                     PacketUtil.sendPacketNoEvent(this.pendingOpenPacket);
                     this.pendingOpenPacket = null;
                  }

                  while (!this.clickPacketQueue.isEmpty()) {
                     PacketUtil.sendPacketNoEvent((C0EPacketClickWindow)this.clickPacketQueue.poll());
                  }
               } else if ((Integer)this.modeProp.getValue() == 3) {
                  this.closeDelayTicks = (Integer)this.closeDelayProp.getValue();
               }
            }
         } else {
            if ((Integer)this.modeProp.getValue() == 1) {
               C0EPacketClickWindow var3 = (C0EPacketClickWindow)var1.getPacket();
               if (mc.thePlayer.openContainer instanceof ContainerPlayer) {
                  var1.setCancelled(true);
                  this.clickPacketQueue.add(var3);
                  return;
               }
            }

            if ((Integer)this.modeProp.getValue() == 2) {
               this.hypixelCooldownTicks = 3;
            }
         }
      }
   }

   @Override
   public void onDisabled() {
      if (this.keysPressed) {
         if (mc.currentScreen != null) {
            KeyBinding.unPressAllKeys();
         }

         this.keysPressed = false;
      }

      if (this.pendingOpenPacket != null) {
         PacketUtil.sendPacketNoEvent(this.pendingOpenPacket);
         this.pendingOpenPacket = null;
      }

      this.hypixelCooldownTicks = 0;
   }

   @EventTarget(4)
   public void onTick(TickEvent var1) {
      if (var1.getType() == EventType.PRE) {
         if (this.openDelayTicks >= 0) {
            this.openDelayTicks--;
            return;
         }

         while (!this.clickPacketQueue.isEmpty()) {
            PacketUtil.sendPacketNoEvent((C0EPacketClickWindow)this.clickPacketQueue.poll());
         }

         if (this.closeDelayTicks > 0) {
            boolean canClose;
            if (mc.thePlayer.inventory.getItemStack() != null) {
               canClose = false;
            } else if (mc.thePlayer.inventoryContainer instanceof ContainerPlayer) {
               ContainerPlayer cp = (ContainerPlayer)mc.thePlayer.inventoryContainer;
               canClose = true;
               for (int i = 0; i < cp.craftMatrix.getSizeInventory(); ++i) {
                  if (cp.craftMatrix.getStackInSlot(i) != null) {
                     canClose = false;
                     break;
                  }
               }
            } else {
               canClose = true;
            }

            if (canClose) {
               this.closeDelayTicks--;
               return;
            }
         } else if (this.closeDelayTicks == 0) {
            if (mc.currentScreen instanceof GuiInventory) {
               PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(0));
            }

            this.closeDelayTicks = -1;
         }
      }
   }

   private void updateMovementKeys(boolean allowSneak) {
      this.keyStateMap.keySet().stream().filter((var1x) -> !allowSneak || var1x != mc.gameSettings.keyBindSneak).forEach((var0) -> KeyUtil.updateKeyBindState(var0.getKeyCode()));
      if (Valor.moduleManager.modules.get(Sprint.class).isEnabled()) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
      }

      this.keysPressed = true;
   }

   private boolean hasAnyMovementKeyPressed() {
      return this.keyStateMap.values().stream().anyMatch(Boolean::booleanValue);
   }

   public InvWalk() {
      super("InvWalk", false, ModuleCategory.PLAYER);
   }
}
