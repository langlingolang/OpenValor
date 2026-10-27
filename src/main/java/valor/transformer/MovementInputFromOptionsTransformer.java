package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;

public class MovementInputFromOptionsTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if ((var5 = (ASM_MethodNode)var4.next()).name.equals("updatePlayerMoveState") || var5.name.equals("func_78898_a")) {
            var11 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onMoveInput", "()V", false));
            ASM_AbstractInsnNode[] var7;
            int var8 = (var7 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var7[var9]).getOpcode() == 177) {
                  var5.instructions.insertBefore(var10, var6);
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

   public MovementInputFromOptionsTransformer() {
      super("net.minecraft.util.MovementInputFromOptions");
   }
}
