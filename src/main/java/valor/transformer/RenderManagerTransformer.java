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

public class RenderManagerTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "renderEntityStatic")) {
            var11 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var6.add(new ASM_MethodInsnNode(184, "valor/hook/RenderManagerHooks", "preRenderEntityStatic", "(Lnet/minecraft/entity/Entity;)V", false));
            var5.instructions.insert(var6);
            ASM_AbstractInsnNode[] var7;
            int var8 = (var7 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var7[var9]).getOpcode() == 172) {
                  (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
                  var6.add(new ASM_MethodInsnNode(184, "valor/hook/RenderManagerHooks", "postRenderEntityStatic", "(Lnet/minecraft/entity/Entity;)V", false));
                  var5.instructions.insertBefore(var10, var6);
               }
            }
         }

         if (matchesMethod(var5, "cacheActiveRenderInfo")) {
            ASM_AbstractInsnNode[] var14;
            int var15 = (var14 = var5.instructions.toArray()).length;

            for (int var16 = 0; var16 < var15; var16++) {
               ASM_AbstractInsnNode var17;
               if ((var17 = var14[var16]).getOpcode() == 177) {
                  var11 = true;
                  ASM_InsnList var18;
                  (var18 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, 6));
                  var18.add(new ASM_MethodInsnNode(184, "valor/hook/RenderManagerHooks", "onCacheActiveRenderInfo", "(F)V", false));
                  var5.instructions.insertBefore(var17, var18);
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

   public RenderManagerTransformer() {
      super("net.minecraft.client.renderer.entity.RenderManager");
   }
}
