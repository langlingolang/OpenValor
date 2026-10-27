package valor.module.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings.GameType;
import org.apache.commons.lang3.RandomUtils;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.UpdateEvent;
import valor.events.WindowClickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.util.ItemUtil;
import valor.util.TimerUtil;

public class InvManager extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private int clickDelayTicks = 0;
   private int openDelayTicks = 0;
   private boolean inventoryOpen = false;
   private final TimerUtil autoArmorTimer = new TimerUtil();
   public final IntProperty minDelayProp = new IntProperty("min-delay", 1, 0, 20);
   public final IntProperty maxDelayProp = new IntProperty("max-delay", 2, 0, 20);
   public final IntProperty openDelayProp = new IntProperty("open-delay", 1, 0, 20);
   public final BooleanProperty autoArmorProp = new BooleanProperty("auto-armor", Boolean.TRUE);
   public final IntProperty autoArmorIntervalProp = new IntProperty("auto-armor-interval", 0, 0, 100, this.autoArmorProp::getValue);
   public final BooleanProperty dropTrashProp = new BooleanProperty("drop-trash", Boolean.FALSE);
   public final BooleanProperty checkDurabilityProp = new BooleanProperty("check-durability", Boolean.TRUE);
   public final IntProperty swordSlotProp = new IntProperty("sword-slot", 1, 0, 9);
   public final IntProperty pickaxeSlotProp = new IntProperty("pickaxe-slot", 3, 0, 9);
   public final IntProperty shovelSlotProp = new IntProperty("shovel-slot", 4, 0, 9);
   public final IntProperty axeSlotProp = new IntProperty("axe-slot", 5, 0, 9);
   public final IntProperty blocksSlotProp = new IntProperty("blocks-slot", 2, 0, 9);
   public final IntProperty blocksProp = new IntProperty("blocks", 128, 64, 2304);
   public final IntProperty projectileSlotProp = new IntProperty("projectile-slot", 7, 0, 9);
   public final IntProperty projectilesProp = new IntProperty("projectiles", 64, 16, 2304);
   public final IntProperty goldAppleSlotProp = new IntProperty("gold-apple-slot", 9, 0, 9);
   public final IntProperty arrowProp = new IntProperty("arrow", 256, 0, 2304);
   public final IntProperty bowSlotProp = new IntProperty("bow-slot", 8, 0, 9);

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (var1.getType() == EventType.PRE) {
         if (this.clickDelayTicks > 0) {
            this.clickDelayTicks--;
         }

         if (this.openDelayTicks > 0) {
            this.openDelayTicks--;
         }

         if (!(mc.currentScreen instanceof GuiInventory)) {
            this.inventoryOpen = false;
            return;
         }

         if (!(((GuiInventory)mc.currentScreen).inventorySlots instanceof ContainerPlayer)) {
            this.inventoryOpen = false;
            return;
         }

         if (!this.inventoryOpen) {
            this.inventoryOpen = true;
            this.openDelayTicks = (Integer)this.openDelayProp.getValue() + 1;
            this.autoArmorTimer.reset();
         }

         GameType var23;
         if (this.openDelayTicks <= 0
            && this.clickDelayTicks <= 0
            && this.isEnabled()
            && ((var23 = mc.playerController.getCurrentGameType()) == GameType.SURVIVAL || var23 == GameType.ADVENTURE)) {
            ArrayList<Integer> var24 = new ArrayList<>(Arrays.asList(-1, -1, -1, -1));
            ArrayList<Integer> var2 = new ArrayList<>(Arrays.asList(-1, -1, -1, -1));

            for (int var3 = 0; var3 < 4; var3++) {
               var24.set(var3, ItemUtil.findBestArmor(var3, true));
               var2.set(var3, ItemUtil.findBestArmor(var3, false));
            }

            int var4;
            int var25;
            if ((var4 = ItemUtil.findBestSword(var25 = (Integer)this.swordSlotProp.getValue() - 1, (Boolean)this.checkDurabilityProp.getValue())) == -1) {
               var4 = ItemUtil.findBestSword(var25, false);
            }

            int var5 = (Integer)this.pickaxeSlotProp.getValue() - 1;
            int var6;
            if ((var6 = ItemUtil.findBestTool("pickaxe", var5, (Boolean)this.checkDurabilityProp.getValue())) == -1) {
               var6 = ItemUtil.findBestTool("pickaxe", var5, false);
            }

            int var7 = (Integer)this.shovelSlotProp.getValue() - 1;
            int var8;
            if ((var8 = ItemUtil.findBestTool("shovel", var7, (Boolean)this.checkDurabilityProp.getValue())) == -1) {
               var8 = ItemUtil.findBestTool("shovel", var7, false);
            }

            int var9 = (Integer)this.axeSlotProp.getValue() - 1;
            int var10;
            if ((var10 = ItemUtil.findBestTool("axe", var9, (Boolean)this.checkDurabilityProp.getValue())) == -1) {
               var10 = ItemUtil.findBestTool("axe", var9, false);
            }

            int var11;
            int var12 = ItemUtil.findLargestStack(var11 = (Integer)this.blocksSlotProp.getValue() - 1, ItemUtil.ItemCategory.Block);
            int var13;
            int var14;
            if ((var14 = ItemUtil.findLargestStack(var13 = (Integer)this.projectileSlotProp.getValue() - 1, ItemUtil.ItemCategory.Projectile)) == -1) {
               var14 = ItemUtil.findLargestStack(var13, ItemUtil.ItemCategory.FishRod);
            }

            int var15;
            int var16 = ItemUtil.findLargestStack(var15 = (Integer)this.goldAppleSlotProp.getValue() - 1, ItemUtil.ItemCategory.GoldApple);
            int var17;
            int var18;
            if ((var18 = ItemUtil.findBestBow(var17 = (Integer)this.bowSlotProp.getValue() - 1, (Boolean)this.checkDurabilityProp.getValue())) == -1) {
               var18 = ItemUtil.findBestBow(var17, false);
            }

            if ((Boolean)this.autoArmorProp.getValue() && this.autoArmorTimer.hasTimeElapsed((long)((Integer)this.autoArmorIntervalProp.getValue()).intValue() * 50L)) {
               for (int var19 = 0; var19 < 4; var19++) {
                  int var20 = var24.get(var19);
                  int var21 = var2.get(var19);
                  if (var20 != -1 || var21 != -1) {
                     int var22 = 39 - var19;
                     if (var20 != var22 && var21 != var22) {
                        if (mc.thePlayer.inventory.getStackInSlot(var22) != null) {
                           if (mc.thePlayer.inventory.getFirstEmptyStack() != -1) {
                              windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var22), 0, 1);
                              return;
                           }

                           windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var22), 1, 4);
                           return;
                        }

                        var25 = var20 != -1 ? var20 : var21;
                        windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var25), 0, 1);
                        this.autoArmorTimer.reset();
                        return;
                     }
                  }
               }
            }

            LinkedHashSet<Integer> var30 = new LinkedHashSet<>();
            if (var25 >= 0 && var25 <= 8 && var4 != -1) {
               var30.add(Integer.valueOf(var25));
               if (var4 != var25) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var4), var25, 2);
                  return;
               }
            }

            if (var5 >= 0 && var5 <= 8 && !var30.contains(Integer.valueOf(var5)) && var6 != -1) {
               var30.add(Integer.valueOf(var5));
               if (var6 != var5) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var6), var5, 2);
                  return;
               }
            }

            if (var7 >= 0 && var7 <= 8 && !var30.contains(Integer.valueOf(var7)) && var8 != -1) {
               var30.add(Integer.valueOf(var7));
               if (var8 != var7) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var8), var7, 2);
                  return;
               }
            }

            if (var9 >= 0 && var9 <= 8 && !var30.contains(Integer.valueOf(var9)) && var10 != -1) {
               var30.add(Integer.valueOf(var9));
               if (var10 != var9) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var10), var9, 2);
                  return;
               }
            }

            if (var11 >= 0 && var11 <= 8 && !var30.contains(Integer.valueOf(var11)) && var12 != -1) {
               var30.add(Integer.valueOf(var11));
               if (var12 != var11) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var12), var11, 2);
                  return;
               }
            }

            if (var13 >= 0 && var13 <= 8 && !var30.contains(Integer.valueOf(var13)) && var14 != -1) {
               var30.add(Integer.valueOf(var13));
               if (var14 != var13) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var14), var13, 2);
                  return;
               }
            }

            if (var15 >= 0 && var15 <= 8 && !var30.contains(Integer.valueOf(var15)) && var16 != -1) {
               var30.add(Integer.valueOf(var15));
               if (var16 != var15) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var16), var15, 2);
                  return;
               }
            }

            if (var17 >= 0 && var17 <= 8 && !var30.contains(Integer.valueOf(var17)) && var18 != -1) {
               var30.add(Integer.valueOf(var17));
               if (var18 != var17) {
                  windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var18), var17, 2);
                  return;
               }
            }

            if ((Boolean)this.dropTrashProp.getValue()) {
               int var31 = getStackSize(var12);
               int var32 = getStackSize(var14);

               for (int var33 = 0; var33 < 36; var33++) {
                  ItemStack var27;
                  if (!var24.contains(var33)
                     && !var2.contains(var33)
                     && var4 != var33
                     && var6 != var33
                     && var8 != var33
                     && var10 != var33
                     && var12 != var33
                     && var14 != var33
                     && var16 != var33
                     && var18 != var33
                     && (var27 = mc.thePlayer.inventory.getStackInSlot(var33)) != null) {
                     boolean var28 = ItemUtil.isValidBlockItem(var27);
                     boolean var29 = ItemUtil.isProjectileItem(var27);
                     if (var28) {
                        var31 += var27.stackSize;
                     }

                     if (var29) {
                        var32 += var27.stackSize;
                     }

                     if (var28 ? var31 > (Integer)this.blocksProp.getValue() : (var29 ? var32 > (Integer)this.projectilesProp.getValue() : ItemUtil.isHarmfulOrJunkItem(var27))) {
                        windowClick(mc.thePlayer.inventoryContainer.windowId, toWindowSlot(var33), 1, 4);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void verifyValue(String var1) {
      switch (var1) {
         case "min-delay":
            if ((Integer)this.minDelayProp.getValue() > (Integer)this.maxDelayProp.getValue()) {
               this.maxDelayProp.setValue(this.minDelayProp.getValue());
               return;
            }
            break;
         case "max-delay":
            if ((Integer)this.minDelayProp.getValue() > (Integer)this.maxDelayProp.getValue()) {
               this.minDelayProp.setValue(this.maxDelayProp.getValue());
            }
      }
   }

   @EventTarget
   public void onClick(WindowClickEvent var1) {
      this.clickDelayTicks = RandomUtils.nextInt((Integer)this.minDelayProp.getValue() + 1, (Integer)this.maxDelayProp.getValue() + 2);
   }

   public static int toWindowSlot(int var0) {
      if (var0 >= 36) {
         return 8 - (var0 - 36);
      } else {
         return var0 <= 8 ? var0 + 36 : var0;
      }
   }

   public static void windowClick(int windowId, int slot, int mouseButton, int mode) {
      mc.playerController.windowClick(windowId, slot, mouseButton, mode, mc.thePlayer);
   }

   public static int getStackSize(int var0) {
      if (var0 == -1) {
         return 0;
      } else {
         ItemStack var1;
         return (var1 = mc.thePlayer.inventory.getStackInSlot(var0)) != null ? var1.stackSize : 0;
      }
   }

   public InvManager() {
      super("InvManager", false, ModuleCategory.PLAYER);
   }
}
