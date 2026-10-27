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
import valor.asm.ASM_VarInsnNode;

public class FontRendererTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var9 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "renderString")) {
            var9 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var6.add(new ASM_MethodInsnNode(184, "valor/hook/FontRendererHooks", "onRenderString", "(Ljava/lang/String;)Ljava/lang/String;", false));
            var6.add(new ASM_VarInsnNode(58, 1));
            var5.instructions.insert(var6);
         }

         if (matchesMethod(var5, "getStringWidth")) {
            var9 = true;
            ASM_InsnList var12;
            (var12 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var12.add(new ASM_MethodInsnNode(184, "valor/hook/FontRendererHooks", "onGetStringWidth", "(Ljava/lang/String;)Ljava/lang/String;", false));
            var12.add(new ASM_VarInsnNode(58, 1));
            var5.instructions.insert(var12);
            ASM_AbstractInsnNode[] var11;
            int var13 = (var11 = var5.instructions.toArray()).length;

            for (int var7 = 0; var7 < var13; var7++) {
               ASM_AbstractInsnNode var8;
               ASM_MethodInsnNode var14;
               if ((var8 = var11[var7]).getOpcode() == 182 && (var14 = (ASM_MethodInsnNode)var8).name.equals("charAt") && var14.desc.equals("(I)C")) {
                  var14.setStatic();
                  var14.owner = "valor/hook/FontRendererHooks";
                  var14.name = "onCharAt";
                  var14.desc = "(Ljava/lang/String;I)C";
               }
            }
         }
      }

      if (!var9) {
         return var1;
      } else {
         ASM_ClassWriter var10 = new ASM_ClassWriter();
         var3.accept(var10);
      return var10.toByteArray();
      }
   }

   public FontRendererTransformer() {
      super("net.minecraft.client.gui.FontRenderer");
   }
}
