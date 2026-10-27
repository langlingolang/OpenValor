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

public class PlayerControllerMPTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "attackEntity")) {
            ASM_AbstractInsnNode[] var6;
            int var7 = (var6 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var7; var8++) {
               ASM_AbstractInsnNode var9;
               ASM_MethodInsnNode var10;
               if (((var9 = var6[var8]).getOpcode() == 182 || var9.getOpcode() == 183)
                  && isSameName((var10 = (ASM_MethodInsnNode)var9).name, "syncCurrentPlayItem")
                  && var10.desc.equals("()V")) {
                  var11 = true;
                  ASM_InsnList var17;
                  (var17 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 2));
                  var17.add(new ASM_MethodInsnNode(184, "valor/hook/PlayerControllerMPHooks", "onAttackEntity", "(Lnet/minecraft/entity/Entity;)V", false));
                  var5.instructions.insert(var9, var17);
               }
            }
         }

         if (matchesMethod(var5, "windowClick")) {
            var11 = true;
            ASM_InsnList var13;
            (var13 = new ASM_InsnList()).add(new ASM_VarInsnNode(21, 1));
            var13.add(new ASM_VarInsnNode(21, 2));
            var13.add(new ASM_VarInsnNode(21, 3));
            var13.add(new ASM_VarInsnNode(21, 4));
            var13.add(new ASM_MethodInsnNode(184, "valor/hook/PlayerControllerMPHooks", "onWindowClick", "(IIII)Z", false));
            ASM_LabelNode var15 = new ASM_LabelNode();
            var13.add(new ASM_JumpInsnNode(153, var15));
            var13.add(new ASM_InsnNode(1));
            var13.add(new ASM_InsnNode(176));
            var13.add(var15);
            var5.instructions.insert(var13);
         }

         if (matchesMethod(var5, "onStoppedUsingItem")) {
            var11 = true;
            ASM_InsnList var14;
            (var14 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/PlayerControllerMPHooks", "onStoppedUsingItem", "()Z", false));
            ASM_LabelNode var16 = new ASM_LabelNode();
            var14.add(new ASM_JumpInsnNode(153, var16));
            var14.add(new ASM_InsnNode(177));
            var14.add(var16);
            var5.instructions.insert(var14);
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

   public PlayerControllerMPTransformer() {
      super("net.minecraft.client.multiplayer.PlayerControllerMP");
   }
}
