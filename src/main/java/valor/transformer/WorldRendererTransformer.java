package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;

public class WorldRendererTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var9 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "putColorMultiplier")) {
            ASM_AbstractInsnNode[] var11;
            int var6 = (var11 = var5.instructions.toArray()).length;

            for (int var7 = 0; var7 < var6; var7++) {
               ASM_AbstractInsnNode var8;
               ASM_MethodInsnNode var12;
               if ((var8 = var11[var7]).getOpcode() == 182 && (var12 = (ASM_MethodInsnNode)var8).name.equals("put") && var12.desc.equals("(II)Ljava/nio/IntBuffer;")) {
                  var9 = true;
                  var12.setStatic();
                  var12.owner = "valor/hook/WorldRendererHooks";
                  var12.name = "onPutColorMultiplier";
                  var12.desc = "(Ljava/nio/IntBuffer;II)Ljava/nio/IntBuffer;";
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

   public WorldRendererTransformer() {
      super("net.minecraft.client.renderer.WorldRenderer");
   }
}
