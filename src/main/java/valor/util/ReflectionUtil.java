package valor.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.Logger;

public final class ReflectionUtil {
   private static final Map<String, Field> FIELD_CACHE = new HashMap<>();
   private static final Map<String, Method> METHOD_CACHE = new HashMap<>();
   private static final Map<String, String> SRG_FIELDS = new HashMap<>();
   private static final Map<String, String> NOTCH_FIELDS = new HashMap<>();

   public static void setPrevSwingProgress(EntityLivingBase var0, float var1) {
      setFloat(var0, "prevSwingProgress", var1);
   }

   public static float getPrevSwingProgress(EntityLivingBase var0) {
      return getFloat(var0, "prevSwingProgress");
   }

   public static double getRenderPosY(RenderManager var0) {
      return getDouble(var0, "renderPosY");
   }

   public static void setRenderPosY(RenderManager var0, double var1) {
      setDouble(var0, "renderPosY", var1);
   }

   public static void resetLeftClickCounter(Minecraft var0) {
      setInt(var0, "leftClickCounter", 0);
   }

   public static int getNextStepDistance(Entity var0) {
      return getInt(var0, "nextStepDistance");
   }

   public static int getCurrentPlayerItem(PlayerControllerMP var0) {
      return getInt(var0, "currentPlayerItem");
   }

   public static void setCurrentPlayerItem(PlayerControllerMP var0, int var1) {
      setInt(var0, "currentPlayerItem", var1);
   }

   public static float getLastReportedPitch(EntityPlayerSP var0) {
      return getFloat(var0, "lastReportedPitch");
   }

   public static void setLastReportedPitch(EntityPlayerSP var0, float var1) {
      setFloat(var0, "lastReportedPitch", var1);
   }

   public static void setMotionZ(S12PacketEntityVelocity var0, int var1) {
      setInt(var0, "motionZ", var1);
   }

   private static Method findMethod(Class<?> var0, String var1, Class<?>... var2) {
      String var3 = var0.getName() + "#" + var1 + var2.length;
      Method var4;
      if ((var4 = METHOD_CACHE.get(var3)) != null) {
         return var4;
      } else {
         for (Class var13 = var0; var13 != null; var13 = var13.getSuperclass()) {
            try {
               Method var5;
               (var5 = var13.getDeclaredMethod(var1, var2)).setAccessible(true);
               METHOD_CACHE.put(var3, var5);
               return var5;
            } catch (NoSuchMethodException var12) {
            }
         }

         for (Class var14 = var0; var14 != null; var14 = var14.getSuperclass()) {
            Method[] var15;
            int var6 = (var15 = var14.getDeclaredMethods()).length;

            for (int var7 = 0; var7 < var6; var7++) {
               Method var8;
               Class[] var9;
               if ((var9 = (var8 = var15[var7]).getParameterTypes()).length == var2.length) {
                  boolean var10 = true;

                  for (int var11 = 0; var11 < var2.length; var11++) {
                     if (var9[var11] != var2[var11]) {
                        var10 = false;
                        break;
                     }
                  }

                  if (var10) {
                     var8.setAccessible(true);
                     METHOD_CACHE.put(var3, var8);
                     return var8;
                  }
               }
            }
         }

         throw new RuntimeException(new NoSuchMethodException(var1 + " in " + var0.getName()));
      }
   }

   public static boolean isInWeb(Entity var0) {
      return getBoolean(var0, "isInWeb");
   }

   public static double getRenderPosZ(RenderManager var0) {
      return getDouble(var0, "renderPosZ");
   }

   public static float getRenderArmYaw(EntityPlayerSP var0) {
      return getFloat(var0, "renderArmYaw");
   }

   public static void setRenderArmYaw(EntityPlayerSP var0, float var1) {
      setFloat(var0, "renderArmYaw", var1);
   }

   public static void setRenderPosZ(RenderManager var0, double var1) {
      setDouble(var0, "renderPosZ", var1);
   }

   public static void setBlockHitDelay(PlayerControllerMP var0, int var1) {
      setInt(var0, "blockHitDelay", var1);
   }

   public static boolean isHittingBlock(PlayerControllerMP var0) {
      return getBoolean(var0, "isHittingBlock");
   }

   public static void setMotionX(S12PacketEntityVelocity var0, int var1) {
      setInt(var0, "motionX", var1);
   }

   public static int getWindowId(C0DPacketCloseWindow var0) {
      return getInt(var0, "field_149534_a");
   }

   public static float getCurBlockDamageMP(PlayerControllerMP var0) {
      return getFloat(var0, "curBlockDamageMP");
   }

   public static void setCurBlockDamageMP(PlayerControllerMP var0, float var1) {
      setFloat(var0, "curBlockDamageMP", var1);
   }

   public static int getBlockHitDelay(PlayerControllerMP var0) {
      return getInt(var0, "blockHitDelay");
   }

   public static float getLastReportedYaw(EntityPlayerSP var0) {
      return getFloat(var0, "lastReportedYaw");
   }

   public static void setLastReportedYaw(EntityPlayerSP var0, float var1) {
      setFloat(var0, "lastReportedYaw", var1);
   }

   public static GuiTextField getInputField(GuiChat var0) {
      return getObject(var0, "inputField", GuiTextField.class);
   }

   public static double getRenderPosX(RenderManager var0) {
      return getDouble(var0, "renderPosX");
   }

   public static void setRenderPosX(RenderManager var0, double var1) {
      setDouble(var0, "renderPosX", var1);
   }

   public static void setOnGround(C03PacketPlayer var0, boolean var1) {
      boolean var2 = var1;
      String var5 = "onGround";
      var0 = var0;
      Field var6;
      if ((var6 = findField(var0.getClass(), var5, boolean.class)) != null) {
         try {
            var6.setBoolean(var0, var2);
            return;
         } catch (Exception var3) {
         }
      }
   }

   public static void syncCurrentPlayItem(PlayerControllerMP var0) {
      try {
         findMethod(var0.getClass(), "syncCurrentPlayItem").invoke(var0);
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }

   public static void syncCurrentPlayItemCompat(PlayerControllerMP var0) {
      syncCurrentPlayItem(var0);
   }

   public static ToolMaterial getToolMaterial(ItemSword var0) {
      return getObject(var0, "material", ToolMaterial.class);
   }

   public static void mouseClicked(GuiScreen var0, int var1, int var2) {
      try {
         findMethod(var0.getClass(), "mouseClicked", int.class, int.class, int.class).invoke(var0, var1, var2, 0);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static Vec3 getVectorForRotation(Entity var0, float var1, float var2) {
      String[] var3;
      for (String var6 : var3 = new String[]{"getVectorForRotation", "func_70676_i"}) {
         try {
            for (Class var7 = var0.getClass(); var7 != null; var7 = var7.getSuperclass()) {
               try {
                  Method var4;
                  (var4 = var7.getDeclaredMethod(var6, float.class, float.class)).setAccessible(true);
                  return (Vec3)var4.invoke(var0, var1, var2);
               } catch (NoSuchMethodException var9) {
               }
            }
         } catch (Exception var10) {
         }
      }

      try {
         for (Class var11 = var0.getClass(); var11 != null; var11 = var11.getSuperclass()) {
            Method[] var12;
            int var13 = (var12 = var11.getDeclaredMethods()).length;

            for (int var14 = 0; var14 < var13; var14++) {
               Method var15;
               if ((var15 = var12[var14]).getReturnType() == Vec3.class
                  && var15.getParameterCount() == 2
                  && var15.getParameterTypes()[0] == float.class
                  && var15.getParameterTypes()[1] == float.class) {
                  var15.setAccessible(true);
                  return (Vec3)var15.invoke(var0, var1, var2);
               }
            }
         }
      } catch (Exception var8) {
      }

      return null;
   }

   private static boolean getBoolean(Object var0, String var1) {
      Field var3;
      if ((var3 = findField(var0.getClass(), var1, boolean.class)) == null) {
         return false;
      } else {
         try {
            return var3.getBoolean(var0);
         } catch (Exception var2) {
            return false;
         }
      }
   }

   private static void setDouble(Object var0, String var1, double var2) {
      Field var5;
      if ((var5 = findField(var0.getClass(), var1, double.class)) != null) {
         try {
            var5.setDouble(var0, var2);
            return;
         } catch (Exception var4) {
         }
      }
   }

   private static double getDouble(Object var0, String var1) {
      Field var3;
      if ((var3 = findField(var0.getClass(), var1, double.class)) == null) {
         return 0.0;
      } else {
         try {
            return var3.getDouble(var0);
         } catch (Exception var2) {
            return 0.0;
         }
      }
   }

   public static ItemStack getItemInUse(EntityPlayer var0) {
      return getObject(var0, "itemInUse", ItemStack.class);
   }

   public static void setItemInUse(EntityPlayer var0, ItemStack var1) {
      Class<ItemStack> var3 = ItemStack.class;
      ItemStack var2 = var1;
      String var6 = "itemInUse";
      var0 = var0;
      Field var7;
      if ((var7 = findField(var0.getClass(), var6, var3)) != null) {
         try {
            var7.set(var0, var2);
            return;
         } catch (Exception var4) {
         }
      }
   }

   public static int getItemInUseCount(EntityPlayer var0) {
      return getInt(var0, "itemInUseCount");
   }

   private static int getInt(Object var0, String var1) {
      Field var3;
      if ((var3 = findField(var0.getClass(), var1, int.class)) == null) {
         return 0;
      } else {
         try {
            return var3.getInt(var0);
         } catch (Exception var2) {
            return 0;
         }
      }
   }

   private static <T> T getObject(Object var0, String var1, Class<T> var2) {
      Field var4;
      if ((var4 = findField(var0.getClass(), var1, var2)) == null) {
         return null;
      } else {
         try {
            return (T)var4.get(var0);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private static Field findField(Class<?> var0, String var1, Class<?> var2) {
      String var3 = var0.getName() + "#" + var1;
      Field var4;
      if ((var4 = FIELD_CACHE.get(var3)) != null) {
         return var4;
      } else {
         Class var14 = var0;

         while (var14 != null) {
            try {
               Field var5;
               if (isTypeCompatible(var5 = var14.getDeclaredField(var1), var2)) {
                  var5.setAccessible(true);
                  FIELD_CACHE.put(var3, var5);
                  return var5;
               }

               var14 = var14.getSuperclass();
            } catch (NoSuchFieldException var10) {
               var14 = var14.getSuperclass();
            }
         }

         String var18;
         if ((var18 = SRG_FIELDS.get(var1)) != null) {
            Class var15 = var0;

            while (var15 != null) {
               try {
                  Field var6;
                  if (isTypeCompatible(var6 = var15.getDeclaredField(var18), var2)) {
                     var6.setAccessible(true);
                     FIELD_CACHE.put(var3, var6);
                     return var6;
                  }

                  var15 = var15.getSuperclass();
               } catch (NoSuchFieldException var9) {
                  var15 = var15.getSuperclass();
               }
            }
         }

         String var20;
         if ((var20 = NOTCH_FIELDS.get(var1)) != null) {
            Class var16 = var0;

            while (var16 != null) {
               try {
                  Field var12;
                  if (isTypeCompatible(var12 = var16.getDeclaredField(var20), var2)) {
                     var12.setAccessible(true);
                     FIELD_CACHE.put(var3, var12);
                     return var12;
                  }

                  var16 = var16.getSuperclass();
               } catch (NoSuchFieldException var8) {
                  var16 = var16.getSuperclass();
               }
            }
         }

         Field var13 = null;

         for (Class var17 = var0; var17 != null; var17 = var17.getSuperclass()) {
            Field[] var11;
            int var19 = (var11 = var17.getDeclaredFields()).length;

            for (int var21 = 0; var21 < var19; var21++) {
               Field var7;
               if (isTypeCompatible(var7 = var11[var21], var2)) {
                  if (var13 != null) {
                     return null;
                  }

                  var13 = var7;
               }
            }
         }

         if (var13 != null) {
            var13.setAccessible(true);
            FIELD_CACHE.put(var3, var13);
         }

         return var13;
      }
   }

   private static boolean isTypeCompatible(Field var0, Class<?> var1) {
      if (var1 == null) {
         return true;
      } else {
         Class var2 = var0.getType();
         return var1.isPrimitive() ? var2 == var1 : var1.isAssignableFrom(var2);
      }
   }

   private static void setInt(Object var0, String var1, int var2) {
      Field var4;
      if ((var4 = findField(var0.getClass(), var1, int.class)) != null) {
         try {
            var4.setInt(var0, var2);
            return;
         } catch (Exception var3) {
         }
      }
   }

   private static float getFloat(Object var0, String var1) {
      Field var3;
      if ((var3 = findField(var0.getClass(), var1, float.class)) == null) {
         return 0.0F;
      } else {
         try {
            return var3.getFloat(var0);
         } catch (Exception var2) {
            return 0.0F;
         }
      }
   }

   private static void setFloat(Object var0, String var1, float var2) {
      Field var4;
      if ((var4 = findField(var0.getClass(), var1, float.class)) != null) {
         try {
            var4.setFloat(var0, var2);
            return;
         } catch (Exception var3) {
         }
      }
   }

   public static Logger getLogger(Minecraft var0) {
      Logger var1;
      if ((var1 = getObject(var0, "logger", Logger.class)) == null) {
         var1 = getObject(var0, "field_71421_D", Logger.class);
      }

      if (var1 == null) {
         var1 = getObject(var0, "LOGGER", Logger.class);
      }

      return var1;
   }

   public static void setupCameraTransform(EntityRenderer var0, float var1, int var2) {
      try {
         findMethod(var0.getClass(), "setupCameraTransform", float.class, int.class).invoke(var0, var1, var2);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static int getFire(Entity var0) {
      return getInt(var0, "fire");
   }

   public static void setJumpTicks(EntityLivingBase var0, int var1) {
      setInt(var0, "jumpTicks", var1);
   }

   public static Timer getTimer(Minecraft var0) {
      return getObject(var0, "timer", Timer.class);
   }

   public static int getRightClickDelayTimer(Minecraft var0) {
      return getInt(var0, "rightClickDelayTimer");
   }

   public static int getRightClickDelayTimerCompat(Minecraft var0) {
      return getRightClickDelayTimer(var0);
   }

   public static void resetRightClickDelayTimer(Minecraft var0) {
      setInt(var0, "rightClickDelayTimer", 0);
   }

   public static void resetRightClickDelayTimerCompat(Minecraft var0) {
      resetRightClickDelayTimer(var0);
   }

   public static void setRightClickDelayTimer(Minecraft var0, int var1) {
      setInt(var0, "rightClickDelayTimer", var1);
   }

   public static float getSwingProgress(EntityLivingBase var0) {
      return getFloat(var0, "swingProgress");
   }

   public static void setItemInUseCount(EntityPlayer var0, int var1) {
      setInt(var0, "itemInUseCount", var1);
   }

   public static void setSwingProgress(EntityLivingBase var0, float var1) {
      setFloat(var0, "swingProgress", var1);
   }

   public static Map<Integer, PotionEffect> getActivePotionsMap(EntityLivingBase var0) {
      return getObject(var0, "activePotionsMap", Map.class);
   }

   public static AttributeModifier getSprintingSpeedBoostModifier(EntityLivingBase var0) {
      return getObject(var0, "sprintingSpeedBoostModifier", AttributeModifier.class);
   }

   public static int getJumpTicks(EntityLivingBase var0) {
      return getInt(var0, "jumpTicks");
   }

   public static void setPrevRenderArmYaw(EntityPlayerSP var0, float var1) {
      setFloat(var0, "prevRenderArmYaw", var1);
   }

   public static float getPrevRenderArmYaw(EntityPlayerSP var0) {
      return getFloat(var0, "prevRenderArmYaw");
   }

   static {
      SRG_FIELDS.put("rightClickDelayTimer", "field_71467_ac");
      SRG_FIELDS.put("leftClickCounter", "field_71429_W");
      SRG_FIELDS.put("timer", "field_71428_T");
      SRG_FIELDS.put("logger", "field_147123_G");
      SRG_FIELDS.put("itemInUse", "field_71074_e");
      SRG_FIELDS.put("itemInUseCount", "field_71072_f");
      SRG_FIELDS.put("swingProgress", "field_70733_aJ");
      SRG_FIELDS.put("prevSwingProgress", "field_70732_aI");
      SRG_FIELDS.put("jumpTicks", "field_70773_bE");
      SRG_FIELDS.put("activePotionsMap", "field_70713_bf");
      SRG_FIELDS.put("curBlockDamageMP", "field_78770_f");
      SRG_FIELDS.put("blockHitDelay", "field_78781_i");
      SRG_FIELDS.put("isHittingBlock", "field_78778_j");
      SRG_FIELDS.put("currentPlayerItem", "field_78777_l");
      SRG_FIELDS.put("renderPosX", "field_78725_b");
      SRG_FIELDS.put("renderPosY", "field_78726_c");
      SRG_FIELDS.put("renderPosZ", "field_78723_d");
      SRG_FIELDS.put("isInWeb", "field_70134_J");
      SRG_FIELDS.put("pressed", "field_74513_e");
      SRG_FIELDS.put("material", "field_150933_b");
      SRG_FIELDS.put("lastReportedYaw", "field_175164_bL");
      SRG_FIELDS.put("lastReportedPitch", "field_175165_bM");
      SRG_FIELDS.put("renderArmYaw", "field_71154_f");
      SRG_FIELDS.put("prevRenderArmYaw", "field_71163_h");
      SRG_FIELDS.put("inputField", "field_146415_a");
      SRG_FIELDS.put("sprintingSpeedBoostModifier", "field_110157_c");
      SRG_FIELDS.put("motionX", "field_149412_b");
      SRG_FIELDS.put("motionY", "field_149413_c");
      SRG_FIELDS.put("motionZ", "field_149411_d");
      SRG_FIELDS.put("yaw", "field_149476_e");
      SRG_FIELDS.put("pitch", "field_149473_f");
      SRG_FIELDS.put("onGround", "field_149474_g");
      SRG_FIELDS.put("rotating", "field_149481_i");
      NOTCH_FIELDS.put("rightClickDelayTimer", "ap");
      NOTCH_FIELDS.put("leftClickCounter", "ag");
      NOTCH_FIELDS.put("timer", "Y");
      NOTCH_FIELDS.put("itemInUse", "g");
      NOTCH_FIELDS.put("itemInUseCount", "h");
      NOTCH_FIELDS.put("swingProgress", "az");
      NOTCH_FIELDS.put("prevSwingProgress", "ay");
      NOTCH_FIELDS.put("jumpTicks", "bn");
      NOTCH_FIELDS.put("activePotionsMap", "g");
      NOTCH_FIELDS.put("curBlockDamageMP", "e");
      NOTCH_FIELDS.put("blockHitDelay", "g");
      NOTCH_FIELDS.put("isHittingBlock", "h");
      NOTCH_FIELDS.put("currentPlayerItem", "j");
      NOTCH_FIELDS.put("renderPosX", "o");
      NOTCH_FIELDS.put("renderPosY", "p");
      NOTCH_FIELDS.put("renderPosZ", "q");
      NOTCH_FIELDS.put("isInWeb", "H");
      NOTCH_FIELDS.put("pressed", "h");
      NOTCH_FIELDS.put("lastReportedYaw", "bN");
      NOTCH_FIELDS.put("lastReportedPitch", "bO");
      NOTCH_FIELDS.put("renderArmYaw", "f");
      NOTCH_FIELDS.put("prevRenderArmYaw", "h");
      NOTCH_FIELDS.put("inputField", "a");
      NOTCH_FIELDS.put("yaw", "d");
      NOTCH_FIELDS.put("pitch", "e");
      NOTCH_FIELDS.put("onGround", "f");
      NOTCH_FIELDS.put("rotating", "h");
   }
}
