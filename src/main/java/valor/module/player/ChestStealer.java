package valor.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.world.WorldSettings.GameType;
import org.apache.commons.lang3.RandomUtils;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.UpdateEvent;
import valor.events.WindowClickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.util.ChatUtil;
import valor.util.ItemUtil;
import valor.util.ReflectionUtil;

public class ChestStealer extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private int stealDelayTicks = 0;
   private int openDelayTicks = 0;
   private boolean chestOpened = false;
   private boolean isLooting = false;
   public final IntProperty minDelayProp = new IntProperty("min-delay", 1, 0, 20);
   public final IntProperty maxDelayProp = new IntProperty("max-delay", 2, 0, 20);
   public final IntProperty openDelayProp = new IntProperty("open-delay", 1, 0, 20);
   public final BooleanProperty autoCloseProp = new BooleanProperty("auto-close", Boolean.FALSE);
   public final BooleanProperty nameCheckProp = new BooleanProperty("name-check", Boolean.TRUE);
   public final BooleanProperty skipTrashProp = new BooleanProperty("skip-trash", Boolean.TRUE);
   public final BooleanProperty moreArmorProp = new BooleanProperty("more-armor", Boolean.FALSE);
   public final BooleanProperty moreSwordProp = new BooleanProperty("more-sword", Boolean.FALSE);

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (var1.getType() == EventType.PRE) {
         if (this.stealDelayTicks > 0) {
            this.stealDelayTicks--;
         }

         if (this.openDelayTicks > 0) {
            this.openDelayTicks--;
         }

         if (!(mc.currentScreen instanceof GuiChest)) {
            this.chestOpened = false;
            return;
         }

         Container var26 = ((GuiChest)mc.currentScreen).inventorySlots;
         if (!(((GuiChest)mc.currentScreen).inventorySlots instanceof ContainerChest)) {
            this.chestOpened = false;
            return;
         }

         if (!this.chestOpened) {
            this.chestOpened = true;
            this.isLooting = false;
            this.openDelayTicks = (Integer)this.openDelayProp.getValue() + 1;
         }

         GameType var6;
         if (this.openDelayTicks <= 0
            && this.stealDelayTicks <= 0
            && this.isEnabled()
            && ((var6 = mc.playerController.getCurrentGameType()) == GameType.SURVIVAL || var6 == GameType.ADVENTURE)) {
            IInventory var2 = ((ContainerChest)var26).getLowerChestInventory();
            String var3;
            if ((Boolean)this.nameCheckProp.getValue()
               && !(var3 = var2.getName()).equals(I18n.format("container.chest"))
               && !var3.equals(I18n.format("container.chestDouble"))) {
               return;
            }

            if (mc.thePlayer.inventory.getFirstEmptyStack() == -1) {
               if (!this.isLooting) {
                  ChatUtil.addFormattedChatMessage(String.format("%s%s: &cYour inventory is full!&r", Valor.CHAT_PREFIX, this.getName()));
                  this.isLooting = true;
               }

               if ((Boolean)this.autoCloseProp.getValue()) {
                  mc.thePlayer.closeScreen();
                  return;
               }
            } else {
               if ((Boolean)this.skipTrashProp.getValue()) {
                  int var27 = -1;
                  double var5 = 0.0;
                  int[] var4 = new int[]{-1, -1, -1, -1};
                  double[] var7 = new double[]{0.0, 0.0, 0.0, 0.0};
                  int var8 = -1;
                  float var9 = 1.0F;
                  int var10 = -1;
                  float var11 = 1.0F;
                  int var12 = -1;
                  float var13 = 1.0F;
                  int var14 = -1;
                  double var16 = 0.0;

                  for (int var15 = 0; var15 < var2.getSizeInventory(); var15++) {
                     if (var26.getSlot(var15).getHasStack()) {
                        ItemStack var18;
                        Item var19;
                        if ((var19 = (var18 = var26.getSlot(var15).getStack()).getItem()) instanceof ItemSword) {
                           double var20 = ItemUtil.getSwordRating(var18);
                           if (var27 == -1 || var20 > var5) {
                              var27 = var15;
                              var5 = var20;
                           }
                        } else if (var19 instanceof ItemArmor) {
                           int var38 = ((ItemArmor)var19).armorType;
                           double var21 = ItemUtil.getArmorRating(var18);
                           if (var4[var38] == -1 || var21 > var7[var38]) {
                              var4[var38] = var15;
                              var7[var38] = var21;
                           }
                        } else if (var19 instanceof ItemPickaxe) {
                           float var39 = ItemUtil.getToolRating(var18);
                           if (var8 == -1 || var39 > var9) {
                              var8 = var15;
                              var9 = var39;
                           }
                        } else if (var19 instanceof ItemSpade) {
                           float var40 = ItemUtil.getToolRating(var18);
                           if (var10 == -1 || var40 > var11) {
                              var10 = var15;
                              var11 = var40;
                           }
                        } else if (var19 instanceof ItemAxe) {
                           float var41 = ItemUtil.getToolRating(var18);
                           if (var12 == -1 || var41 > var13) {
                              var12 = var15;
                              var13 = var41;
                           }
                        } else if (var19 instanceof ItemBow) {
                           double var42 = ItemUtil.getBowRating(var18);
                           if (var14 == -1 || var42 > var16) {
                              var14 = var15;
                              var16 = var42;
                           }
                        }
                     }
                  }

                  int var36;
                  double var37 = (var36 = ItemUtil.findBestSword(0, true)) != -1 ? ItemUtil.getSwordRating(mc.thePlayer.inventory.getStackInSlot(var36)) : 0.0;
                  if (var5 > var37) {
                     shiftClick(var26.windowId, var27);
                     return;
                  }

                  for (int var43 = 0; var43 < 4; var43++) {
                     int var45;
                     double var22 = (var45 = ItemUtil.findBestArmor(var43, true)) != -1 ? ItemUtil.getArmorRating(mc.thePlayer.inventory.getStackInSlot(var45)) : 0.0;
                     if (var7[var43] > var22) {
                        shiftClick(var26.windowId, var4[var43]);
                        return;
                     }
                  }

                  int var44;
                  float var46 = (var44 = ItemUtil.findBestTool("pickaxe", 0, true)) != -1 ? ItemUtil.getToolRating(mc.thePlayer.inventory.getStackInSlot(var44)) : 1.0F;
                  if (var9 > var46) {
                     shiftClick(var26.windowId, var8);
                     return;
                  }

                  int var47;
                  float var23 = (var47 = ItemUtil.findBestTool("shovel", 0, true)) != -1 ? ItemUtil.getToolRating(mc.thePlayer.inventory.getStackInSlot(var47)) : 1.0F;
                  if (var11 > var23) {
                     shiftClick(var26.windowId, var10);
                     return;
                  }

                  int var28;
                  float var29 = (var28 = ItemUtil.findBestTool("axe", 0, true)) != -1 ? ItemUtil.getToolRating(mc.thePlayer.inventory.getStackInSlot(var28)) : 1.0F;
                  if (var13 > var29) {
                     shiftClick(var26.windowId, var12);
                     return;
                  }

                  int var30;
                  double var24 = (var30 = ItemUtil.findBestBow(0, true)) != -1 ? ItemUtil.getBowRating(mc.thePlayer.inventory.getStackInSlot(var30)) : 0.0;
                  if (var16 > var24) {
                     shiftClick(var26.windowId, var14);
                     return;
                  }
               }

               for (int slot = 0; slot < var2.getSizeInventory(); slot++) {
                  if (var26.getSlot(slot).getHasStack()) {
                     ItemStack stack = var26.getSlot(slot).getStack();
                     if (this.shouldStealItem(stack)) {
                        shiftClick(var26.windowId, slot);
                        return;
                     }
                  }
               }

               if ((Boolean)this.autoCloseProp.getValue()) {
                  mc.thePlayer.closeScreen();
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

   public static void shiftClick(int windowId, int slot) {
      mc.playerController.windowClick(windowId, slot, 0, 1, mc.thePlayer);
   }

   @EventTarget
   public void onWindowClick(WindowClickEvent var1) {
      this.stealDelayTicks = RandomUtils.nextInt((Integer)this.minDelayProp.getValue() + 1, (Integer)this.maxDelayProp.getValue() + 2);
   }

   public ChestStealer() {
      super("ChestStealer", false, ModuleCategory.PLAYER);
   }
   private boolean shouldStealItem(ItemStack stack) {
      if (stack == null) return false;
      if (!(Boolean)this.skipTrashProp.getValue() || !ItemUtil.isHarmfulOrJunkItem(stack)) {
         return true;
      }
      if ((Boolean)this.moreArmorProp.getValue() && stack.getItem() instanceof ItemArmor) {
         ArmorMaterial mat = ((ItemArmor)stack.getItem()).getArmorMaterial();
         if (mat == ArmorMaterial.DIAMOND || (mat == ArmorMaterial.IRON && stack.isItemEnchanted())) {
            return true;
         }
      }
      if ((Boolean)this.moreSwordProp.getValue() && stack.getItem() instanceof ItemSword) {
         ToolMaterial mat = ReflectionUtil.getToolMaterial((ItemSword)stack.getItem());
         if (mat == ToolMaterial.EMERALD || EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) != 0 || (mat == ToolMaterial.IRON && stack.isItemEnchanted())) {
            return true;
         }
      }
      InvManager invManager = (InvManager)Valor.moduleManager.modules.get(InvManager.class);
      if (ItemUtil.ItemCategory.Block.matches(stack)) {
         return !invManager.isEnabled() || ItemUtil.getItemCount(ItemUtil.ItemCategory.Block) < (Integer)invManager.blocksProp.getValue();
      }
      if (ItemUtil.ItemCategory.Projectile.matches(stack)) {
         return !invManager.isEnabled() || ItemUtil.getItemCount(ItemUtil.ItemCategory.Projectile) < (Integer)invManager.projectilesProp.getValue();
      }
      if (ItemUtil.ItemCategory.FishRod.matches(stack)) {
         return ItemUtil.getItemCount(ItemUtil.ItemCategory.Projectile) == 0;
      }
      if (ItemUtil.ItemCategory.Arrow.matches(stack)) {
         return !invManager.isEnabled() || ItemUtil.getItemCount(ItemUtil.ItemCategory.Arrow) < (Integer)invManager.arrowProp.getValue();
      }
      return false;
   }

}
