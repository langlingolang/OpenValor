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

public class ItemStackTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var8 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "hasEffect", "()Z") || var5.name.equals("t") && var5.desc.equals("()Z")) {
            var8 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/ItemStackHooks", "onHasEffect", "()Ljava/lang/Boolean;", false));
            var6.add(new ASM_VarInsnNode(58, 2));
            var6.add(new ASM_VarInsnNode(25, 2));
            ASM_LabelNode var7 = new ASM_LabelNode();
            var6.add(new ASM_JumpInsnNode(198, var7));
            var6.add(new ASM_VarInsnNode(25, 2));
            var6.add(new ASM_MethodInsnNode(182, "java/lang/Boolean", "booleanValue", "()Z", false));
            var6.add(new ASM_InsnNode(172));
            var6.add(var7);
            var5.instructions.insert(var6);
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

   public ItemStackTransformer() {
      super("net.minecraft.item.ItemStack");
   }
}
