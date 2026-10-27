package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_JumpInsnNode;
import valor.asm.ASM_LabelNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class MinecraftTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 4);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "startGame", "()V")) {
            var11 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onStartGame", "()V", false));
            var5.instructions.insert(var6);
            ASM_AbstractInsnNode[] var7;
            int var8 = (var7 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var7[var9]).getOpcode() == 177) {
                  (var6 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "postStartGame", "()V", false));
                  var5.instructions.insertBefore(var10, var6);
               }
            }
         }

         if (matchesMethod(var5, "runTick", "()V")) {
            var11 = true;
            ASM_InsnList var14;
            (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var14.add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onRunTickPre", "(Lnet/minecraft/client/Minecraft;)V", false));
            var5.instructions.insert(var14);
            ASM_AbstractInsnNode[] var25;
            int var34 = (var25 = var5.instructions.toArray()).length;

            for (int var39 = 0; var39 < var34; var39++) {
               ASM_AbstractInsnNode var44;
               if ((var44 = var25[var39]).getOpcode() == 177) {
                  (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var14.add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onRunTickPost", "(Lnet/minecraft/client/Minecraft;)V", false));
                  var5.instructions.insertBefore(var44, var14);
               }
            }

            var34 = (var25 = var5.instructions.toArray()).length;

            for (int var40 = 0; var40 < var34; var40++) {
               ASM_MethodInsnNode var16;
               ASM_AbstractInsnNode var45;
               if ((var45 = var25[var40]).getOpcode() == 184
                  && isSameName((var16 = (ASM_MethodInsnNode)var45).name, "setKeyBindState")
                  && var16.owner.equals("net/minecraft/client/settings/KeyBinding")) {
                  var16.owner = "valor/hook/MinecraftHooks";
                  var16.name = "onSetKeyBindState";
                  var16.desc = "(IZLnet/minecraft/client/Minecraft;)V";
                  var16.itf = false;
                  var5.instructions.insertBefore(var16, new ASM_VarInsnNode(25, 0));
               }
            }

            var34 = (var25 = var5.instructions.toArray()).length;

            for (int var41 = 0; var41 < var34; var41++) {
               ASM_MethodInsnNode var17;
               ASM_AbstractInsnNode var46;
               if ((var46 = var25[var41]).getOpcode() == 182
                  && isSameName((var17 = (ASM_MethodInsnNode)var46).name, "changeCurrentItem")
                  && var17.owner.equals("net/minecraft/entity/player/InventoryPlayer")) {
                  var17.setStatic();
                  var17.owner = "valor/hook/MinecraftHooks";
                  var17.name = "onChangeCurrentItem";
                  var17.desc = "(Lnet/minecraft/entity/player/InventoryPlayer;I)Z";
                  ASM_LabelNode var47 = new ASM_LabelNode();
                  var5.instructions.insert(var17, new ASM_JumpInsnNode(153, var47));
                  var5.instructions.insert(var17.next, var47);
               }
            }
         }

         if (matchesMethod(var5, "runGameLoop", "()V")) {
            var11 = true;
            var5.instructions.insert(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "beginHudFrame", "()V", false));
            ASM_AbstractInsnNode[] var19;
            int var29 = (var19 = var5.instructions.toArray()).length;

            for (int var37 = 0; var37 < var29; var37++) {
               ASM_AbstractInsnNode var42;
               if ((var42 = var19[var37]).getOpcode() == 177) {
                  var5.instructions.insertBefore(var42, new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "renderHudAtDisplay", "()V", false));
               }
            }
         }

         if (matchesMethod(var5, "loadWorld")) {
            var11 = true;
            ASM_InsnList var20;
            (var20 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onLoadWorld", "()V", false));
            var5.instructions.insert(var20);
         }

         if (matchesMethod(var5, "updateFramebufferSize", "()V")) {
            var11 = true;
            ASM_AbstractInsnNode[] var21;
            int var30 = (var21 = var5.instructions.toArray()).length;

            for (int var38 = 0; var38 < var30; var38++) {
               ASM_AbstractInsnNode var43;
               if ((var43 = var21[var38]).getOpcode() == 177) {
                  ASM_InsnList var48;
                  (var48 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onResize", "()V", false));
                  var5.instructions.insertBefore(var43, var48);
               }
            }
         }

         if (matchesMethod(var5, "clickMouse", "()V")) {
            var11 = true;
            ASM_InsnList var22;
            (var22 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var22.add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onClickMouse", "(Lnet/minecraft/client/Minecraft;)Z", false));
            ASM_LabelNode var31 = new ASM_LabelNode();
            var22.add(new ASM_JumpInsnNode(153, var31));
            var22.add(new ASM_InsnNode(177));
            var22.add(var31);
            var5.instructions.insert(var22);
         }

         if (matchesMethod(var5, "rightClickMouse", "()V")) {
            var11 = true;
            ASM_InsnList var23;
            (var23 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onRightClickMouse", "()Z", false));
            ASM_LabelNode var32 = new ASM_LabelNode();
            var23.add(new ASM_JumpInsnNode(153, var32));
            var23.add(new ASM_InsnNode(177));
            var23.add(var32);
            var5.instructions.insert(var23);
         }

         if (matchesMethod(var5, "sendClickBlockToController", "()V")) {
            var11 = true;
            ASM_InsnList var24;
            (var24 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var24.add(new ASM_MethodInsnNode(184, "valor/hook/MinecraftHooks", "onSendClickBlockToController", "(Lnet/minecraft/client/Minecraft;)Z", false));
            ASM_LabelNode var33 = new ASM_LabelNode();
            var24.add(new ASM_JumpInsnNode(153, var33));
            var24.add(new ASM_InsnNode(177));
            var24.add(var33);
            var5.instructions.insert(var24);
         }
      }

      if (!var11) {
         return var1;
      } else {
         ASM_ClassWriter var12 = new ASM_ClassWriter();
         var3.accept(var12);
      return var12.toByteArray();
      }
   }

   public MinecraftTransformer() {
      super("net.minecraft.client.Minecraft");
   }
}
