package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class KeyBindingTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "isPressed")) {
            ASM_AbstractInsnNode[] var6;
            int var7 = (var6 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var7; var8++) {
               ASM_AbstractInsnNode var9;
               if ((var9 = var6[var8]).getOpcode() == 172) {
                  var11 = true;
                  ASM_InsnList var10;
                  (var10 = new ASM_InsnList()).add(new ASM_InsnNode(89));
                  var10.add(new ASM_VarInsnNode(25, 0));
                  var10.add(new ASM_MethodInsnNode(184, "valor/hook/KeyBindingHooks", "onIsPressed", "(ILnet/minecraft/client/settings/KeyBinding;)I", false));
                  var5.instructions.insertBefore(var9, var10);
               }
            }
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

   public KeyBindingTransformer() {
      super("net.minecraft.client.settings.KeyBinding");
   }
}
