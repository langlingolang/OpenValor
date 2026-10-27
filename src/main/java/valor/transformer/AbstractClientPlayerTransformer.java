package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class AbstractClientPlayerTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var10 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "getFovModifier")) {
            ASM_AbstractInsnNode[] var6;
            int var7 = (var6 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var7; var8++) {
               ASM_AbstractInsnNode var9;
               ASM_MethodInsnNode var12;
               if ((var9 = var6[var8]) instanceof ASM_MethodInsnNode
                  && isSameName((var12 = (ASM_MethodInsnNode)var9).name, "getAttributeValue")
                  && var12.desc.equals("()D")) {
                  var10 = true;
                  var12.setStatic();
                  var12.itf = false;
                  var12.owner = "valor/hook/AbstractClientPlayerHooks";
                  var12.name = "getFovModifier";
                  var12.desc = "(Lnet/minecraft/entity/ai/attributes/IAttributeInstance;Ljava/lang/Object;)D";
                  var5.instructions.insertBefore(var12, new ASM_VarInsnNode(25, 0));
               }
            }
         }
      }

      if (!var10) {
         return var1;
      } else {
         ASM_ClassWriter var11 = new ASM_ClassWriter();
         var3.accept(var11);
      return var11.toByteArray();
      }
   }

   public AbstractClientPlayerTransformer() {
      super("net.minecraft.client.entity.AbstractClientPlayer");
   }
}
