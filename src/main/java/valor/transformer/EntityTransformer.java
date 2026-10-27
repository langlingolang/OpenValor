package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
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

public class EntityTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var13 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "setVelocity", "(DDD)V")) {
            var13 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var6.add(new ASM_VarInsnNode(24, 1));
            var6.add(new ASM_VarInsnNode(24, 3));
            var6.add(new ASM_VarInsnNode(24, 5));
            var6.add(new ASM_MethodInsnNode(184, "valor/hook/EntityHooks", "onSetVelocity", "(Lnet/minecraft/entity/Entity;DDD)Z", false));
            ASM_LabelNode var7 = new ASM_LabelNode();
            var6.add(new ASM_JumpInsnNode(153, var7));
            var6.add(new ASM_InsnNode(177));
            var6.add(var7);
            var5.instructions.insert(var6);
         }

         if (matchesMethod(var5, "setAngles", "(FF)V")) {
            var13 = true;
            ASM_InsnList var15;
            (var15 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var15.add(new ASM_MethodInsnNode(184, "valor/hook/EntityHooks", "onSetAngles", "(Lnet/minecraft/entity/Entity;)Z", false));
            ASM_LabelNode var17 = new ASM_LabelNode();
            var15.add(new ASM_JumpInsnNode(153, var17));
            var15.add(new ASM_InsnNode(177));
            var15.add(var17);
            var5.instructions.insert(var15);
         }

         if (matchesMethod(var5, "moveEntity", "(DDD)V")) {
            int var16 = 0;
            ASM_AbstractInsnNode[] var18;
            int var8 = (var18 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var18[var9]).getOpcode() == 54) {
                  if (var16 == 0) {
                     ASM_VarInsnNode var11 = (ASM_VarInsnNode)var10;
                     var13 = true;
                     ASM_InsnList var12;
                     (var12 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                     var12.add(new ASM_VarInsnNode(21, var11.var));
                     var12.add(new ASM_MethodInsnNode(184, "valor/hook/EntityHooks", "onMoveEntitySafeWalk", "(Lnet/minecraft/entity/Entity;Z)Z", false));
                     var12.add(new ASM_VarInsnNode(54, var11.var));
                     var5.instructions.insert(var10, var12);
                  }

                  var16++;
               }
            }
         }
      }

      if (!var13) {
         return var1;
      } else {
         ASM_ClassWriter var14 = new ASM_ClassWriter();
         var3.accept(var14);
      return var14.toByteArray();
      }
   }

   public EntityTransformer() {
      super("net.minecraft.entity.Entity");
   }
}
