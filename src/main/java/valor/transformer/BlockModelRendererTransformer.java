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

public class BlockModelRendererTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 4);
      boolean var10 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "renderModel")
            && "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Z"
               .equals(var5.desc)
            && var5.instructions.size >= 80) {
            var10 = true;
            int var6 = Math.max(var5.maxStack, 7);
            var5.maxStack = Math.max(var5.maxStack, var6 + 1);
            ASM_InsnList var7;
            (var7 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var7.add(new ASM_VarInsnNode(25, 1));
            var7.add(new ASM_VarInsnNode(25, 2));
            var7.add(new ASM_VarInsnNode(25, 3));
            var7.add(new ASM_VarInsnNode(25, 4));
            var7.add(new ASM_VarInsnNode(25, 5));
            var7.add(new ASM_VarInsnNode(21, 6));
            var7.add(
               new ASM_MethodInsnNode(
                  184,
                  "valor/hook/BlockModelRendererHooks",
                  "onRenderModel",
                  "(Lnet/minecraft/client/renderer/BlockModelRenderer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Ljava/lang/Boolean;",
                  false
               )
            );
            var7.add(new ASM_VarInsnNode(58, var6));
            var7.add(new ASM_VarInsnNode(25, var6));
            ASM_LabelNode var8 = new ASM_LabelNode();
            var7.add(new ASM_JumpInsnNode(198, var8));
            var7.add(new ASM_VarInsnNode(25, var6));
            var7.add(new ASM_MethodInsnNode(182, "java/lang/Boolean", "booleanValue", "()Z", false));
            var7.add(new ASM_InsnNode(172));
            var7.add(var8);
            var5.instructions.insert(var7);
         }
      }

      if (!var10) {
         return var1;
      } else {
         try {
            ASM_ClassWriter var11 = new ASM_ClassWriter();
            var3.accept(var11);
      return var11.toByteArray();
         } catch (Throwable var9) {
            System.err.println("[Valor] BlockModelRenderer transform aborted: ".concat(String.valueOf(var9)));
            return var1;
         }
      }
   }

   public BlockModelRendererTransformer() {
      super("net.minecraft.client.renderer.BlockModelRenderer");
   }
}
