package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class BlockRendererDispatcherTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var7 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "renderBlock")) {
            var7 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var6.add(new ASM_VarInsnNode(25, 2));
            var6.add(
               new ASM_MethodInsnNode(
                  184,
                  "valor/hook/BlockRendererDispatcherHooks",
                  "onRenderBlock",
                  "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;)V",
                  false
               )
            );
            var5.instructions.insert(var6);
         }
      }

      if (!var7) {
         return var1;
      } else {
         ASM_ClassWriter var8 = new ASM_ClassWriter();
         var3.accept(var8);
      return var8.toByteArray();
      }
   }

   public BlockRendererDispatcherTransformer() {
      super("net.minecraft.client.renderer.BlockRendererDispatcher");
   }
}
