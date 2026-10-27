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

public class EntityLivingBaseTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var12 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "jump", "()V")) {
            int var6 = 0;
            ASM_AbstractInsnNode[] var7;
            int var8 = (var7 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var7[var9]).getOpcode() == 56 && var6 == 0) {
                  ASM_VarInsnNode var13 = (ASM_VarInsnNode)var10;
                  ASM_InsnList var11;
                  (var11 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var11.add(new ASM_VarInsnNode(23, var13.var));
                  var11.add(new ASM_MethodInsnNode(184, "valor/hook/EntityLivingBaseHooks", "onJump", "(Lnet/minecraft/entity/EntityLivingBase;F)F", false));
                  var11.add(new ASM_VarInsnNode(56, var13.var));
                  var5.instructions.insert(var10, var11);
                  var12 = true;
                  var6++;
               }
            }
         }

         if (matchesMethod(var5, "moveEntityWithHeading", "(FF)V")) {
            ASM_AbstractInsnNode[] var16;
            int var17 = (var16 = var5.instructions.toArray()).length;

            for (int var18 = 0; var18 < var17; var18++) {
               ASM_AbstractInsnNode var19;
               ASM_MethodInsnNode var20;
               if ((var19 = var16[var18]).getOpcode() == 182 && isSameName((var20 = (ASM_MethodInsnNode)var19).name, "moveFlying") && var20.desc.equals("(FFF)V")) {
                  var20.setStatic();
                  var20.desc = "(Lnet/minecraft/entity/EntityLivingBase;FFF)V";
                  var20.owner = "valor/hook/EntityLivingBaseHooks";
                  var20.name = "onMoveFlying";
                  var12 = true;
               }

               if (var19.getOpcode() == 56) {
                  ASM_VarInsnNode var21 = (ASM_VarInsnNode)var19;
                  ASM_InsnList var14;
                  (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var14.add(new ASM_VarInsnNode(23, var21.var));
                  var14.add(
                     new ASM_MethodInsnNode(184, "valor/hook/EntityLivingBaseHooks", "onDepthStrider", "(Lnet/minecraft/entity/EntityLivingBase;F)F", false)
                  );
                  var14.add(new ASM_VarInsnNode(56, var21.var));
                  var5.instructions.insert(var19, var14);
                  var12 = true;
               }
            }
         }
      }

      if (!var12) {
         return var1;
      } else {
         ASM_ClassWriter var15 = new ASM_ClassWriter();
         var3.accept(var15);
      return var15.toByteArray();
      }
   }

   public EntityLivingBaseTransformer() {
      super("net.minecraft.entity.EntityLivingBase");
   }
}
