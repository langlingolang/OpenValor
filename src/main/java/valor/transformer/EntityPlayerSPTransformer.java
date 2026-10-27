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

public class EntityPlayerSPTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var12 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "onUpdateWalkingPlayer", "()V")) {
            var12 = true;
            ASM_InsnList var6;
            (var6 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var6.add(
               new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onWalkingPlayerPre", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false)
            );
            var5.instructions.insert(var6);
         }

         if (matchesMethod(var5, "onUpdate", "()V")) {
            var12 = true;
            ASM_InsnList var14;
            (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var14.add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onUpdatePre", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false));
            var5.instructions.insert(var14);
            ASM_AbstractInsnNode[] var7;
            int var8 = (var7 = var5.instructions.toArray()).length;

            for (int var9 = 0; var9 < var8; var9++) {
               ASM_AbstractInsnNode var10;
               if ((var10 = var7[var9]).getOpcode() == 177) {
                  ASM_InsnList var11;
                  (var11 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var11.add(
                     new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onUpdatePost", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false)
                  );
                  var5.instructions.insertBefore(var10, var11);
               }
            }
         }

         if (matchesMethod(var5, "onLivingUpdate", "()V")) {
            ASM_AbstractInsnNode[] var15;
            int var16 = (var15 = var5.instructions.toArray()).length;

            for (int var17 = 0; var17 < var16; var17++) {
               ASM_AbstractInsnNode var18;
               ASM_MethodInsnNode var19;
               if (((var18 = var15[var17]).getOpcode() == 182 || var18.getOpcode() == 183)
                  && isSameName((var19 = (ASM_MethodInsnNode)var18).name, "onLivingUpdate")
                  && !var19.owner.equals("net/minecraft/client/entity/EntityPlayerSP")) {
                  var12 = true;
                  ASM_InsnList var21;
                  (var21 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onLivingUpdate", "()V", false));
                  var5.instructions.insertBefore(var19, var21);
               }

               if (var18.getOpcode() == 182) {
                  if (isSameName((var19 = (ASM_MethodInsnNode)var18).name, "updatePlayerMoveState") && var19.desc.equals("()V")) {
                     var12 = true;
                     ASM_InsnList var22;
                     (var22 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onMoveInput", "()V", false));
                     var5.instructions.insert(var18, var22);
                  }

                  if (isSameName(var19.name, "isUsingItem") && var19.desc.equals("()Z")) {
                     var12 = true;
                     var19.setStatic();
                     var19.owner = "valor/hook/EntityPlayerSPHooks";
                     var19.name = "isUsingItem";
                     var19.desc = "(Lnet/minecraft/client/entity/EntityPlayerSP;)Z";
                  }

                  if (isSameName(var19.name, "isPotionActive") && var19.desc.equals("(Lnet/minecraft/potion/Potion;)Z")) {
                     var12 = true;
                     var19.setStatic();
                     var19.owner = "valor/hook/EntityPlayerSPHooks";
                     var19.name = "checkPotion";
                     var19.desc = "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/potion/Potion;)Z";
                  }
               }
            }
         }
      }

      if (!var12) {
         return var1;
      } else {
         ASM_ClassWriter var13 = new ASM_ClassWriter();
         var3.accept(var13);
      return var13.toByteArray();
      }
   }

   public EntityPlayerSPTransformer() {
      super("net.minecraft.client.entity.EntityPlayerSP");
   }
}
