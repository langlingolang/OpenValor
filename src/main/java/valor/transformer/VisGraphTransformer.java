package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
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

public class VisGraphTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var8 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "setOpaqueCube") || var5.name.equals("func_178606_a")) {
            var8 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/VisGraphHooks", "shouldCancelFunc", "()Z", false));
            ASM_LabelNode var7 = new ASM_LabelNode();
            var6.add(new ASM_JumpInsnNode(153, var7));
            var6.add(new ASM_InsnNode(177));
            var6.add(var7);
            var5.instructions.insert(var6);
         }

         if (matchesMethod(var5, "computeVisibility")) {
            var8 = true;
            ASM_InsnList var10;
            (var10 = new ASM_InsnList())
               .add(
                  new ASM_MethodInsnNode(
                     184, "valor/hook/VisGraphHooks", "overrideComputeVisibility", "()Lnet/minecraft/client/renderer/chunk/SetVisibility;", false
                  )
               );
            var10.add(new ASM_VarInsnNode(58, 1));
            var10.add(new ASM_VarInsnNode(25, 1));
            ASM_LabelNode var11 = new ASM_LabelNode();
            var10.add(new ASM_JumpInsnNode(198, var11));
            var10.add(new ASM_VarInsnNode(25, 1));
            var10.add(new ASM_InsnNode(176));
            var10.add(var11);
            var5.instructions.insert(var10);
         }
      }

      if (!var8) {
         return var1;
      } else {
         ASM_ClassWriter var9 = new ASM_ClassWriter();
         var3.accept(var9);
      return var9.toByteArray();
      }
   }

   public VisGraphTransformer() {
      super("net.minecraft.client.renderer.chunk.VisGraph");
   }
}
