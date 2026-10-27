package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_JumpInsnNode;
import valor.asm.ASM_LabelNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class BlockTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 4);
      boolean var9 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "shouldSideBeRendered")
            && var5.desc.equals("(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z")) {
            var9 = true;
            int var6 = Math.max(var5.maxStack, 4);
            var5.maxStack = Math.max(var5.maxStack, var6 + 1);
            ASM_InsnList var7;
            (var7 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var7.add(new ASM_VarInsnNode(25, 2));
            var7.add(new ASM_VarInsnNode(25, 3));
            var7.add(new ASM_VarInsnNode(25, 0));
            var7.add(
               new ASM_MethodInsnNode(
                  184,
                  "valor/hook/BlockHooks",
                  "onShouldSideBeRendered",
                  "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/block/Block;)Ljava/lang/Boolean;",
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

         if (matchesMethod(var5, "getBlockLayer") && var5.desc.equals("()Lnet/minecraft/util/EnumWorldBlockLayer;")) {
            var9 = true;
            int var10 = Math.max(var5.maxStack, 1);
            var5.maxStack = Math.max(var5.maxStack, var10 + 1);
            ASM_InsnList var11;
            (var11 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var11.add(
               new ASM_MethodInsnNode(
                  184, "valor/hook/BlockHooks", "onGetBlockLayer", "(Lnet/minecraft/block/Block;)Lnet/minecraft/util/EnumWorldBlockLayer;", false
               )
            );
            var11.add(new ASM_VarInsnNode(58, var10));
            var11.add(new ASM_VarInsnNode(25, var10));
            ASM_LabelNode var12 = new ASM_LabelNode();
            var11.add(new ASM_JumpInsnNode(198, var12));
            var11.add(new ASM_VarInsnNode(25, var10));
            var11.add(new ASM_InsnNode(176));
            var11.add(var12);
            var5.instructions.insert(var11);
         }
      }

      return !var9 ? var1 : writeClass(var3, var1);
   }

   public BlockTransformer() {
      super("net.minecraft.block.Block");
   }
}
