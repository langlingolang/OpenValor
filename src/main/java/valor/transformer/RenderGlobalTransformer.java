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

public class RenderGlobalTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if ((var5 = (ASM_MethodNode)var4.next()).name.equals("renderEntities") || var5.name.equals("func_180446_a")) {
            var11 = true;
            int var6 = 3;
            if (var5.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V")) {
               var6 = 3;
            } else {
               String[] var7 = var5.desc.substring(1, var5.desc.indexOf(41)).split(";");
               int var8 = 1;

               for (int var9 = 0; var9 < var7.length; var9++) {
                  String var10;
                  if (!(var10 = var7[var9]).isEmpty() && var10.endsWith("F")) {
                     var6 = var8;
                     break;
                  }

                  if (!var10.equals("D") && !var10.equals("J")) {
                     var8++;
                  } else {
                     var8 += 2;
                  }
               }
            }

            ASM_InsnList var14;
            (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, var6));
            var14.add(new ASM_MethodInsnNode(184, "valor/hook/RenderManagerHooks", "onPostRenderEntities", "(F)V", false));
            ASM_AbstractInsnNode[] var15;
            int var16 = (var15 = var5.instructions.toArray()).length;

            for (int var17 = 0; var17 < var16; var17++) {
               ASM_AbstractInsnNode var13;
               if ((var13 = var15[var17]).getOpcode() == 177) {
                  var5.instructions.insertBefore(var13, var14);
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

   public RenderGlobalTransformer() {
      super("net.minecraft.client.renderer.RenderGlobal");
   }
}
