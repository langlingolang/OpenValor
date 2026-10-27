package valor.util;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

public final class ItemUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static final List<Integer> POSITIVE_POTION_EFFECTS = Arrays.asList(1, 3, 5, 6, 8, 10, 11, 12, 14, 21, 22);

   public static boolean isHoldingSword() {
      ItemStack stack = mc.thePlayer.getHeldItem();
      return stack != null && stack.getItem() instanceof ItemSword;
   }

   public static double getArmorRating(ItemStack stack) {
      double rating = 0.0;
      if (stack != null && stack.getItem() instanceof ItemArmor) {
         rating += (double) ((ItemArmor) stack.getItem()).damageReduceAmount;
         if (stack.isItemEnchanted()) {
            rating += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.8
               + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack) * 0.05
               + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.01;
         }
      }
      return rating;
   }

   public static int findBestBow(int startSlot, boolean checkDamaged) {
      int bestSlot = -1;
      double bestRating = 0.0;
      if (startSlot < 0) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            int slot = (startSlot + i) % 36;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            double rating;
            if (stack != null
               && stack.getItem() instanceof ItemBow
               && (!checkDamaged || !stack.isItemDamaged() || stack.getMaxDamage() - stack.getItemDamage() >= 30)
               && (rating = getBowRating(stack)) > bestRating) {
               bestSlot = slot;
               bestRating = rating;
            }
         }
         return bestSlot;
      }
   }

   public static boolean isValidBlockItem(ItemStack stack) {
      if (stack != null && stack.stackSize > 0) {
         Item item = stack.getItem();
         if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            return !BlockUtil.isInteractableBlock(block) && BlockUtil.isFullSolidBlock(block);
         }
      }
      return false;
   }

   public static int findBestArmor(int armorType, boolean checkDamaged) {
      int bestSlot = -1;
      double bestRating = 0.0;

      for (int slot = 0; slot < 40; slot++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
         double rating;
         if (stack != null
            && stack.getItem() instanceof ItemArmor
            && ((ItemArmor) stack.getItem()).armorType == armorType
            && (!checkDamaged || !stack.isItemDamaged() || stack.getMaxDamage() - stack.getItemDamage() >= 30)
            && (rating = getArmorRating(stack)) >= bestRating) {
            bestSlot = slot;
            bestRating = rating;
         }
      }

      return bestSlot;
   }

   public static double getBowRating(ItemStack stack) {
      double rating = 0.0;
      if (stack != null && stack.getItem() instanceof ItemBow) {
         rating = 2.0;
         if (stack.isItemEnchanted()) {
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            if (power > 0) {
               rating = 2.0 + (double) (power + 1) * 0.25;
            }

            rating += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.25
               + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) * 0.05;
         }
      }
      return rating;
   }

   public static boolean isProjectileItem(ItemStack stack) {
      if (stack != null && stack.stackSize > 0) {
         Item item = stack.getItem();
         return item instanceof ItemEgg || item instanceof ItemSnowball;
      }
      return false;
   }

   public static int findBestTool(String toolType, int startSlot, boolean checkDamaged) {
      int bestSlot = -1;
      float bestEfficiency = 1.0F;
      if (startSlot < 0) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            int slot = (startSlot + i) % 36;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemTool) {
               String actualType;
               if (stack.getItem() instanceof ItemPickaxe) {
                  actualType = "pickaxe";
               } else if (stack.getItem() instanceof ItemAxe) {
                  actualType = "axe";
               } else if (stack.getItem() instanceof ItemSpade) {
                  actualType = "shovel";
               } else {
                  continue;
               }

               float efficiency;
               if (actualType.equals(toolType)
                  && (!checkDamaged || !stack.isItemDamaged() || stack.getMaxDamage() - stack.getItemDamage() >= 30)
                  && (efficiency = getToolRating(stack)) > bestEfficiency) {
                  bestSlot = slot;
                  bestEfficiency = efficiency;
               }
            }
         }
         return bestSlot;
      }
   }

   public static int findBestToolForBlock(int currentSlot, Block block) {
      float bestSpeed = getBlockDigSpeed(mc.thePlayer.inventory.getStackInSlot(currentSlot), block);
      int bestSlot = currentSlot;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
         float speed;
         if (stack != null && (speed = getBlockDigSpeed(stack, block)) > bestSpeed) {
            bestSlot = slot;
            bestSpeed = speed;
         }
      }

      return bestSlot;
   }

   public static int getItemCount(ItemType type) {
      int total = 0;
      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
         if (stack != null && type.matches(stack)) {
            total += stack.stackSize;
         }
      }
      return total;
   }

   public static int findLargestStack(int startSlot, ItemType type) {
      int bestSlot = -1;
      int maxCount = 0;
      if (startSlot < 0) {
         startSlot = 0;
      }

      for (int i = 0; i < 36; i++) {
         int slot = (startSlot + i) % 36;
         ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
         if (stack != null && type.matches(stack) && maxCount < stack.stackSize) {
            bestSlot = slot;
            maxCount = stack.stackSize;
         }
      }

      return bestSlot;
   }

   public static boolean isHarmfulOrJunkItem(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item instanceof ItemPotion) {
            return ((ItemPotion) item).getEffects(stack).stream().map(PotionEffect::getPotionID).noneMatch(POSITIVE_POTION_EFFECTS::contains);
         } else if (item instanceof ItemEnderPearl) {
            return false;
         } else if (item instanceof ItemFood && item != Items.spider_eye) {
            return false;
         } else if (item instanceof ItemMonsterPlacer) {
            return false;
         } else {
            return item != Items.nether_star;
         }
      }
   }

   public static double getSwordRating(ItemStack stack) {
      double rating = 0.0;
      if (stack == null) {
         return 0.0;
      } else {
         Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers();
         for (String key : modifiers.keySet()) {
            if (key.equals("generic.attackDamage")) {
               for (AttributeModifier mod : modifiers.get(key)) {
                  rating += mod.getAmount();
                  break;
               }
               break;
            }
         }

         if (stack.isItemEnchanted()) {
            rating += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
               + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
         }

         return rating;
      }
   }

   public static float getToolRating(ItemStack stack) {
      float rating = 1.0F;
      int efficiency;
      if (stack != null
         && stack.getItem() instanceof ItemTool
         && (rating = ((ItemTool) stack.getItem()).getToolMaterial().getEfficiencyOnProperMaterial()) > 1.0F
         && (efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack)) > 0) {
         rating += (float) (efficiency * efficiency + 1);
      }
      return rating;
   }

   public static int findBestSword(int startSlot, boolean checkDamaged) {
      int bestSlot = -1;
      double bestDamage = 0.0;
      if (startSlot < 0) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            int slot = (startSlot + i) % 36;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            double damage;
            if (stack != null
               && stack.getItem() instanceof ItemSword
               && (!checkDamaged || !stack.isItemDamaged() || stack.getMaxDamage() - stack.getItemDamage() >= 30)
               && (damage = getSwordRating(stack)) > bestDamage) {
               bestSlot = slot;
               bestDamage = damage;
            }
         }
         return bestSlot;
      }
   }

   public static float getBlockDigSpeed(ItemStack stack, Block block) {
      float speed = 1.0F;
      if (stack != null) {
         speed = !stack.canHarvestBlock(block) && stack.getItem() instanceof ItemPickaxe ? 1.0F : stack.getStrVsBlock(block);
         int efficiency;
         if (stack.getItem() instanceof ItemTool && speed > 1.0F && (efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack)) > 0) {
            speed += (float) (efficiency * efficiency + 1);
         }
      }
      return speed;
   }

   public static boolean isHoldingTool() {
      ItemStack stack = mc.thePlayer.getHeldItem();
      return stack != null && stack.getItem() instanceof ItemTool;
   }

   public static boolean isHoldingWeapon() {
      ItemStack stack = mc.thePlayer.getHeldItem();
      if (stack == null) {
         return false;
      } else {
         if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("ExtraAttributes")) {
               NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
               if (extra.hasKey("UHCid")) {
                  long uhcId = extra.getLong("UHCid");
                  if (uhcId == 50006L || uhcId == 50009L) {
                     return true;
                  }
               }
            }

            if (tag.hasKey("HideFlags") && stack.getItem() instanceof ItemSpade && ((ItemSpade) stack.getItem()).getToolMaterial() == ToolMaterial.EMERALD) {
               return true;
            }
         }

         if (stack.getItem() instanceof ItemEnchantedBook) {
            return false;
         } else {
            return EnchantmentHelper.getEnchantments(stack).containsKey(19) || stack.getItem() instanceof ItemSword;
         }
      }
   }

   public static boolean isHoldingBow() {
      ItemStack stack = mc.thePlayer.getHeldItem();
      return stack != null && stack.getItem() instanceof ItemBow;
   }

   public static boolean isHoldingEdible() {
      ItemStack stack = mc.thePlayer.getHeldItem();
      if (stack == null) {
         return false;
      } else {
         return !ItemPotion.isSplash(stack.getItemDamage()) && (stack.getItemUseAction() == EnumAction.EAT || stack.getItemUseAction() == EnumAction.DRINK);
      }
   }

   public static boolean isHoldingValidBlock() {
      return isValidBlockItem(mc.thePlayer.getHeldItem());
   }

   public interface ItemType {
      boolean matches(ItemStack stack);
   }

   public static enum ItemCategory implements ItemType {
      Block {
         @Override
         public boolean matches(ItemStack stack) {
            return ItemUtil.isValidBlockItem(stack);
         }
      },
      Projectile {
         @Override
         public boolean matches(ItemStack stack) {
            return ItemUtil.isProjectileItem(stack);
         }
      },
      FishRod {
         @Override
         public boolean matches(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemFishingRod;
         }
      },
      GoldApple {
         @Override
         public boolean matches(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemAppleGold;
         }
      },
      Arrow {
         @Override
         public boolean matches(ItemStack stack) {
            return stack != null && stack.getItem() == Items.arrow;
         }
      };

   }
}
